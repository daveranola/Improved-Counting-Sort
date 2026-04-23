# CountingSort_Improved

Java implementation of the Paper 2 algorithm, "Improving Counting Sort Algorithm Via Data Locality". The repository contains:

- `src/Improved_CountingSort.java`: the hybrid quicksort + counting sort implementation.
- `test/Improved_CountingSortTest.java`: correctness checks plus the benchmark code used to reproduce the paper tables.
- `graphs/`: exported plots for the benchmark tables.
- `poster/`: poster assets.

## Requirements

- Java 17
- Maven 3.9+

## Build And Test

Compile everything:

```powershell
mvn clean compile
```

Run correctness tests only:

```powershell
mvn '-Dtest=Improved_CountingSortTest$AccuracyTests' test
```

Run the benchmark tables used for replication:

```powershell
mvn '-Dtest=Improved_CountingSortTest$ResultsReplicationTests' test
```

Run the full suite:

```powershell
mvn test
```

`mvn test` includes the benchmark-style replication runs for Tables 1-3, so it takes noticeably longer than the accuracy-only command.

## Tuning The Threshold `C`

The paper treats `C` as machine-dependent. This repository exposes an overload so you can benchmark different thresholds without editing the algorithm:

```java
Improved_CountingSort.sort(values, threshold);
```

To sweep candidate thresholds on the current machine:

```powershell
mvn -q -DskipTests compile
java -cp target/classes Improved_CountingSortThresholdSweep
```

Use `paper` to optimize for the published Paper 2 workloads and `extended` to bias toward larger in-repo benchmark sizes:

```powershell
java -cp target/classes Improved_CountingSortThresholdSweep paper
java -cp target/classes Improved_CountingSortThresholdSweep extended
```

The current default is `C = 1000`, which matches the paper's published experiments. Because `C` is machine-dependent, use the sweep tool on the machine where you plan to benchmark or present results if you want to retune it for a different workload mix.
