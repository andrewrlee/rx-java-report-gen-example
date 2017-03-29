package uk.co.optimisticpanda.reportgen.lesssimple;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.append;
import static java.io.File.createTempFile;
import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.joining;
import static uk.co.optimisticpanda.reportgen.Utils.propagateAnyError;

import java.io.File;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.schedulers.Schedulers;

public class Runner {

    private static final Logger L = LoggerFactory.getLogger(Runner.class);
    
    private final DataSource source1 = DataSource.withName("A")
                                                    .numberOfRows(1500)
                                                    .generatingRowTakesBetweenMillis(8, 10)
                                                    .create();
    
    private final DataSource source2 = DataSource.withName("B")
                                                    .numberOfRows(1500)
                                                    .generatingRowTakesBetweenMillis(8, 10)
                                                    .create();

    private final DataSource source3 = DataSource.withName("C")
                                                    .numberOfRows(1000)
                                                    .generatingRowTakesBetweenMillis(2, 12)
                                                    .create();
    
    private Observable<String[]> getObservable(DataSource source) {
        return source.asIds()
                .subscribeOn(Schedulers.io())
                .buffer(10)
                .flatMap(
                        ids -> Observable.from(ids).subscribeOn(Schedulers.io()).flatMap(id -> source.getLine()));
    }
    
    public void generate() throws Exception {
    
        Observable<String[]> source1Observable = getObservable(source1);
        Observable<String[]> source2Observable = getObservable(source2);
        Observable<String[]> source3Observable = getObservable(source3);
        
        File report = createTempFile("report-", ".csv");

        L.info("Generating report: {}", report.getAbsolutePath());
        
        Observable
            .merge(source1Observable, source2Observable, source3Observable)
            
            // transform from array to comma separate string lines
            .map(line -> stream(line).collect(joining(",","","\n")))
            
            // write each line to file
            .doOnNext(line -> propagateAnyError(() -> append(line, report, UTF_8)))
            
            // convert to rows written / second
            .window(1, SECONDS).flatMap(Observable::count)
            
            // block to ensure main doesn't exit
            .toBlocking()
            
            .subscribe(
                    count -> L.info("Processed {} events/s", count),
                    error -> L.error("An error has occurred: {}", error.getMessage(), error),
                    () -> L.info("The report has now completed: {}", LocalDateTime.now()));
    }

    public static void main(String[] args) throws Exception {
        new Runner().generate();
    }
}
