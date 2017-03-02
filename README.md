## Asynchronously generating a report from multiples sources using rx java. 

Asynchronously generating a report from multiples sources using rx java. 

Rows from each data-source are represented by separate observables. 
As subscribers are single threaded this makes safely synchronising writes to a single file trivial. 

The test runners shows 3 different situations:

 * Simple example of all sources being processed successfully: `MultiThreadedReportRunner.java`
 * A single failure from one source terminates the job entirely: `RunnerStopsAfterFirstError.java`
 * A single failure from one source terminates that source but allows other sources to continue to contribute to the report: `RunnerOnlyStopsProcessingSingleSourceAfterError.java`
   
###Test data sources

Test data sources simulate generating records that should be included in the report. 
They have attributes that can be configured:
 * The name that is incorporated in each cell values
 * The number of rows that the datasource contains
 * The max/min duration of how long it should take to generate a row (the calling thread will wait a random amount of time in this range before returning a row).  

```java
private final TestDataSource source3 = TestDataSource.withName("C")
                                                     .numberOfRows(1_000)
                                                     .generatingRowTakesBetweenMillis(2, 12)
                                                     .create();
```

### Example out put
Example output of all rows being processed:

```java
22:30:42.093 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Generating report: /tmp/report-7258233068139114036.csv
22:30:43.124 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 291 events/s
22:30:44.124 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 325 events/s
22:30:45.124 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 328 events/s
22:30:46.125 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 329 events/s
22:30:47.125 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 334 events/s
22:30:48.124 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 333 events/s
22:30:49.125 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 343 events/s
22:30:50.124 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 333 events/s
22:30:51.125 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 211 events/s
22:30:52.124 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 209 events/s
22:30:53.124 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 205 events/s
22:30:54.124 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 203 events/s
22:30:55.125 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 205 events/s
22:30:56.124 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 208 events/s
22:30:56.820 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - Processed 143 events/s
22:30:56.849 [main] INFO uk.co.optimisticpanda.reportgen.MultiThreadedReportRunner - The report has now completed: 2017-03-02T22:30:56.840
```

