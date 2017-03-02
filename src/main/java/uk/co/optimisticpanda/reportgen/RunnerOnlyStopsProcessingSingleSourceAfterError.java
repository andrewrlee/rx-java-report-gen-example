package uk.co.optimisticpanda.reportgen;

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

/**
 * This fails on row 1_500 in a random source - it allows other sources to continue processing but doesn't process same source any more.
 */
public class RunnerOnlyStopsProcessingSingleSourceAfterError {

	private static final Logger L = LoggerFactory.getLogger(RunnerOnlyStopsProcessingSingleSourceAfterError.class);
	
	private final TestDataSource source1 = TestDataSource.withName("A")
													.numberOfRows(1_500)
													.generatingRowTakesBetweenMillis(8, 10)
													.create();
	
	private final TestDataSource source2 = TestDataSource.withName("B")
													.numberOfRows(1_500)
													.generatingRowTakesBetweenMillis(8, 10)
													.create();

	private final TestDataSource source3 = TestDataSource.withName("C")
													.numberOfRows(1_000)
													.generatingRowTakesBetweenMillis(2, 12)
													.create();
	
	public void generate() throws Exception {
	
		Observable<String[]> source1Observable = source1.asObservable();
		Observable<String[]> source2Observable = source2.asObservable();
		Observable<String[]> source3Observable = source3.asObservable();
		
		TestDataSource.setFailOnRow(1_500);
		
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
		new RunnerOnlyStopsProcessingSingleSourceAfterError().generate();
	}
}
