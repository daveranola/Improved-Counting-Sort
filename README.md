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
mvn "-Dtest=Improved_CountingSortTest\$AccuracyTests" test
```

Run the benchmark tables used for replication:

```powershell
mvn "-Dtest=Improved_CountingSortTest\$ResultsReplicationTests" test
```

Run the full suite:

```powershell
mvn test
```

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

The current default is `C = 1000`. Paper-focused sweeps on this repository's Ryzen 9 7845HX / JDK 17 setup kept the best region close to the paper's original value (`768-1000`), so the source stays aligned with the paper while the sweep tool lets you retune for other workload profiles.
