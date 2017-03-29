package uk.co.optimisticpanda.reportgen.lesssimple;

import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

public class DataSource {
    private static final Logger L = LoggerFactory
            .getLogger(DataSource.class);
    private static final AtomicInteger TOTAL_ROW_COUNT = new AtomicInteger(0);
    private static int FAILS_ON = -1;

    private static final String[] FIELDS = new String[] { "name", "age",
            "phone", "address-line-1", "address-line-2", "postcode" };
    private final AtomicInteger rowCount = new AtomicInteger(0);
    private final String name;
    private final int min;
    private final int max;
    private final int totalRows;

    public static void setFailOnRow(int failsOn) {
        FAILS_ON = failsOn;
    }

    public static Builder withName(String name) {
        return new Builder(name);
    }

    private DataSource(String name, int totalRows, int min, int max) {
        this.name = name;
        this.totalRows = totalRows;
        this.min = min;
        this.max = max;
    }

    Observable<String[]> getLine() {
        try {
            MILLISECONDS.sleep(ThreadLocalRandom.current()
                    .nextInt(min, max + 1));
        } catch (InterruptedException e) {
            Thread currentThread = Thread.currentThread();
            L.warn("Thread interrupted: " + currentThread);
            currentThread.interrupt();
        }
        int rowNumber = rowCount.getAndIncrement();
        int thisRow = TOTAL_ROW_COUNT.incrementAndGet();
        if (thisRow == FAILS_ON) {
            throw new RuntimeException("An Error occured! in source: " + name
                    + ", after: " + rowNumber + " rows");
        }
        return Observable.just(stream(FIELDS).map(
                field -> String.format("%s-%s-%s", field, name, rowNumber))
                .toArray(String[]::new));
    }

    Observable<String> asIds() {
        AtomicInteger i = new  AtomicInteger();
        return Observable.fromCallable(() -> {
            Thread.sleep(5);
            return "id-" + i.incrementAndGet(); 
        })
        .repeat()
        .takeUntil(row -> rowCount.get() > totalRows);
    }

    static class Builder {

        private final String name;
        private int rows;
        private int min;
        private int max;

        private Builder(String name) {
            this.name = name;
        }

        Builder numberOfRows(int rows) {
            this.rows = rows;
            return this;
        }

        Builder generatingRowTakesBetweenMillis(int min, int max) {
            this.min = min;
            this.max = max;
            return this;
        }

        DataSource create() {
            return new DataSource(name, rows, min, max);
        }
    }
}
