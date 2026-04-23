# Analysis Notes

These notes summarize the current state of the repository against Paper 2 and the module rubric. They are not a substitute for the required 3-page report, but they capture the main benchmark outcomes and the reasons the results differ from the paper.

## Benchmark Environment

- CPU: AMD Ryzen 9 7845HX with Radeon Graphics
- Cores / threads: 12 / 24
- Cache reported by Windows: L2 12288 KB, L3 65536 KB
- OS: Windows 11
- JDK: Temurin 17.0.18
- Benchmark note: the numeric examples below come from the current repository run and should be regenerated on the final submission machine if you want report-grade figures.

## Threshold `C`

- Paper 2 states that `C` is machine-dependent and uses `C = 1000` in its experiments.
- The repository keeps `C = 1000` as the default to stay aligned with the paper while still exposing `sort(values, threshold)` and `Improved_CountingSortThresholdSweep` for retuning.
- If you want the report to include threshold tuning, rerun the sweep tool on the final benchmark machine rather than reusing old numbers.

## Comparison To Paper 2

### Table 1

- The qualitative result matches the paper: classic counting sort is much faster on already-sorted input than on random input.
- On this machine the gap grows with input size. For example, at `n = r = 1,000,000`, random input took `12.978 ms` while sorted input took `2.326 ms`.

### Table 2

- The repository partially matches the paper on the published `r >> n` cases.
- In the current run, the hybrid algorithm was faster than classic counting sort at `n = 1000`, but classic counting sort was faster from `n = 2000` onward.
- For larger values beyond the paper's table, classic counting sort often becomes faster overall because the preprocessing overhead grows.

### Table 3

- The main claim of the paper is reproduced well on this machine.
- For every tested `n = r` size in the repository, quicksort + counting sort remained faster than both classic quicksort and quicksort + insertion sort.
- Example results from the current run:
  - `n = r = 1,000,000`: quicksort `60.997 ms`, quick+insertion `56.054 ms`, quick+counting `36.897 ms`
  - `n = r = 2,000,000`: quicksort `125.876 ms`, quick+insertion `116.019 ms`, quick+counting `78.012 ms`

## Why The Numbers Differ From The Paper

- The paper's measurements were collected on a different CPU and cache hierarchy.
- The paper does not fully specify every benchmarking detail, such as JVM effects, OS scheduling controls, or whether CPU affinity and frequency scaling were constrained.
- This repository is implemented in Java and measured under a modern JIT-managed runtime, while the paper presents low-level cache arguments that are sensitive to the exact execution environment.
- Repeated threshold sweeps on this machine show normal benchmark noise, so the safest submission choice is to keep the default close to the paper's published `C`.
