# Analysis Notes

These notes summarize the current state of the repository against Paper 2 and the module rubric. They are not a substitute for the required 3-page report, but they capture the main benchmark outcomes and the reasons the results differ from the paper.

## Benchmark Environment

- CPU: AMD Ryzen 9 7845HX with Radeon Graphics
- Cores / threads: 12 / 24
- Cache reported by Windows: L2 12288 KB, L3 65536 KB
- OS: Windows 11
- JDK: Temurin 17.0.18

## Threshold `C`

- Paper 2 states that `C` is machine-dependent and uses `C = 1000` in its experiments.
- A paper-focused sweep on this machine kept the best region close to the paper value, typically between `768` and `1000`.
- Larger thresholds can improve very large `n = r` workloads on this CPU, but they make the paper's small `r >> n` Table 2 cases worse.
- The repository therefore keeps `C = 1000` as the default to stay aligned with the paper while still exposing `sort(values, threshold)` and `Improved_CountingSortThresholdSweep` for retuning.

## Comparison To Paper 2

### Table 1

- The qualitative result matches the paper: classic counting sort is much faster on already-sorted input than on random input.
- On this machine the gap grows with input size. For example, at `n = r = 1,000,000`, random input took `4.752 ms` while sorted input took `2.859 ms`.

### Table 2

- The repository partially matches the paper on the published `r >> n` cases.
- With the current default, the hybrid algorithm is faster than classic counting sort at `n = 1000` and `n = 2000`, but slower at `n = 3000`.
- For larger values beyond the paper's table, classic counting sort often becomes faster overall because the preprocessing overhead grows.

### Table 3

- The main claim of the paper is reproduced well on this machine.
- For every tested `n = r` size in the repository, quicksort + counting sort remained faster than both classic quicksort and quicksort + insertion sort.
- Example results from the current run:
  - `n = r = 1,000,000`: quicksort `64.568 ms`, quick+insertion `56.091 ms`, quick+counting `36.049 ms`
  - `n = r = 2,000,000`: quicksort `135.353 ms`, quick+insertion `118.545 ms`, quick+counting `77.113 ms`

## Why The Numbers Differ From The Paper

- The paper's measurements were collected on a different CPU and cache hierarchy.
- The paper does not fully specify every benchmarking detail, such as JVM effects, OS scheduling controls, or whether CPU affinity and frequency scaling were constrained.
- This repository is implemented in Java and measured under a modern JIT-managed runtime, while the paper presents low-level cache arguments that are sensitive to the exact execution environment.
- Repeated threshold sweeps on this machine show normal benchmark noise, so the safest submission choice is to keep the default close to the paper's published `C`.

## Submission Status

- The repository now includes a README with build, test, benchmark, and tuning instructions.
- The code passes the accuracy tests and the benchmark suite runs successfully.
- The formal report is still a separate deliverable and still needs to be submitted.
