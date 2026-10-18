# Hyper-Heuristic Sightseeing Optimisation

A Java search and optimisation project that solves a route-planning problem — the **Sightseeing Problem (SSP)** — using low-level heuristics controlled by a hyper-heuristic. The problem domain is implemented on the [HyFlex](#third-party-dependency-hyflex) hyper-heuristic framework, exposing mutation, local search and crossover operators to two hyper-heuristics: a random-selection baseline and an adaptive, Choice-Function-inspired selector that scores heuristics online by improvement, acceptance and recency.

The project was originally completed as undergraduate coursework for an AI Methods module (see [Coursework and framework attribution](#coursework-and-framework-attribution)).

---

## Contents

- [Problem definition](#problem-definition)
- [Architecture](#architecture)
- [Low-level heuristics](#low-level-heuristics)
- [Hyper-heuristics](#hyper-heuristics)
- [AI and search techniques](#ai-and-search-techniques)
- [Instances](#instances)
- [Build and run](#build-and-run)
- [Project structure](#project-structure)
- [Limitations](#limitations)
- [Coursework and framework attribution](#coursework-and-framework-attribution)
- [Third-party dependency: HyFlex](#third-party-dependency-hyflex)
- [License](#license)

---

## Problem definition

A traveller starts at a **hotel**, must visit **every point of interest (POI) exactly once**, and finishes at the **airport**.

- **Solution representation:** a permutation of the POI indices `0 … n-1`. The hotel and airport are fixed endpoints and are not part of the permutation.
- **Objective (minimised):** the total path length

  ```
  f(s) = d(hotel, s[0]) + Σ_{i=0}^{n-2} d(s[i], s[i+1]) + d(s[n-1], airport)
  ```

  where `d(a, b) = ceil( sqrt( (a.x - b.x)² + (a.y - b.y)² ) )`, i.e. the Euclidean distance rounded up to an integer.
- **Lower objective values are better.**
- **Feasibility:** any permutation of the POIs is a feasible solution; all operators preserve the permutation property.

This makes the SSP an **open-path, TSP-style Hamiltonian path problem with fixed start and end points**. Every POI must be visited, there are no scores, budgets or time windows, and the goal is purely to minimise distance.

---

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│  Hyper-heuristic  (ChoiceFunctionHH, SR_IE_HH)            │
│  extends HyFlex AbstractClasses.HyperHeuristic            │
│  – chooses which heuristic to apply, decides acceptance   │
└──────────────────────────┬───────────────────────────────┘
                           │ applyHeuristic(h, …), getFunctionValue(i),
                           │ copySolution(a, b), initialiseSolution(i)
┌──────────────────────────▼───────────────────────────────┐
│  SightseeingProblemDomain                                 │
│  extends HyFlex AbstractClasses.ProblemDomain             │
│  – low-level heuristic registry (6 + 2 crossover)         │
│  – solution memory (indexed slots)                        │
│  – best-solution tracking, depth of search / intensity    │
└───────────────┬──────────────────────────┬───────────────┘
                │                          │
┌───────────────▼──────────────┐ ┌─────────▼────────────────┐
│  Low-level heuristics         │ │  SSPObjectiveFunction     │
│  mutation · local search ·    │ │  ceil-Euclidean path cost │
│  crossover                    │ │  (hotel → POIs → airport) │
└───────────────┬──────────────┘ └─────────┬────────────────┘
                │                          │
┌───────────────▼──────────────────────────▼───────────────┐
│  SSPInstance / SSPSolution                                │
│  read from instances/ssp/*.ssp by SSPInstanceReader       │
└──────────────────────────────────────────────────────────┘

Runners (HH_Runner_Visual and subclasses) load an instance, run a
hyper-heuristic under a time limit, print the result and open the
Swing visualiser (SSPView).
```

- **Hyper-heuristic layer:** subclasses of HyFlex's `HyperHeuristic`. They only see heuristic indices and objective values, never the route itself.
- **Problem domain:** `SightseeingProblemDomain` subclasses HyFlex's `ProblemDomain`. It holds the heuristic registry, an array of solution memory slots, and the best solution found. It passes HyFlex's *depth of search* and *intensity of mutation* parameters to each heuristic.
- **Objective function:** `SSPObjectiveFunction` computes the path cost, plus the hotel→POI, POI→POI and POI→airport costs that the local search heuristics use for delta evaluation.
- **Instances:** `SSPInstanceReader` parses `.ssp` files into an `SSPInstance`. That instance also creates initial solutions (a random permutation via Fisher–Yates shuffle).
- **Visual runner:** `HH_Runner_Visual` runs a hyper-heuristic and opens `SSPView`, a Swing window that draws the best route.

---

## Low-level heuristics

Six non-crossover heuristics (indices 0–5) and two crossover operators are registered with the problem domain.

| Heuristic | Type | Description | Parameter |
|---|---|---|---|
| `AdjacentSwap` | Mutation | Swaps randomly chosen neighbouring POIs in the route. | Intensity of mutation → 1, 2, 4, 8, 16 or 32 swaps |
| `Reinsertion` | Mutation | Removes a POI from a random position and reinserts it at another random position. | Intensity of mutation → 1–5 reinsertions |
| `NextDescent` | Local search | Scans adjacent swaps and accepts the first improving move; repeats until no improvement or the iteration budget is used. | Depth of search → 1–5 iterations |
| `DavissHillClimbing` | Local search | Sweeps all adjacent swaps in a pass, keeping every improving swap; repeats until a pass yields no improvement or the budget is used. | Depth of search → 1–5 passes |
| `TwoOpt` | Local search | Best-improvement 2-opt: evaluates every segment reversal using delta evaluation (including the hotel and airport endpoints) and applies the best one per pass. | Depth of search → 1–5 passes |
| `SwapMutation` | Mutation | Swaps two POIs at random positions. | Intensity of mutation → 1, 2, 4, 8, 16 or 32 swaps |
| `OX` | Crossover | Order crossover: copies a random segment from parent 1, then fills the remaining positions with the missing POIs in parent 2's order. | — |
| `EdgeRecombination` | Crossover | Edge recombination: builds an adjacency table from both parents and constructs the child by repeatedly moving to the neighbour with the fewest remaining edges. | — |

Shared operator code (the RNG, objective-function reference and `swap`) lives in `HeuristicOperators`.

---

## Hyper-heuristics

### `SR_IE_HH` — baseline

**Simple Random selection with Improving-or-Equal acceptance.**

- Picks one of the non-crossover heuristics uniformly at random each iteration.
- Accepts the candidate if its objective value is less than or equal to the current one.
- Uses no learning or adaptive behaviour. It's the reference point for the adaptive method.

### `ChoiceFunctionHH` — adaptive heuristic selection

A **simplified, Choice-Function-inspired** hyper-heuristic. It keeps a score for each heuristic (the six non-crossover heuristics plus the two crossovers) and, at every iteration, greedily applies the one with the highest score:

```
score(h) = 0.5 · f1[h] + 0.3 · f2[h] + 0.2 · f3[h]
```

| Term | Meaning | Update rule (per iteration) |
|---|---|---|
| `f1` | Recent improvement magnitude | All values decay by ×0.9; the applied heuristic gains `0.1 · improvement` when it improves the current solution |
| `f2` | Recent acceptance / success signal | All values decay by ×0.9; the applied heuristic gains `0.1` when its candidate is accepted |
| `f3` | Recency | `iterations since the heuristic was last applied`, which rewards heuristics that haven't been tried recently |

The decay makes `f1` and `f2` favour recent behaviour. `f3` pushes the search towards heuristics that haven't been used for a while, balancing exploitation of heuristics that are working well against exploring the others.

**Acceptance: decreasing threshold.** A candidate is accepted when

```
candidate − current ≤ τ,    τ = 11000 · max(0, 1 − iteration / 100000)
```

Early on, worsening moves within the threshold are accepted, which allows exploration. As τ falls linearly to zero, the method converges to improving-or-equal acceptance.

**Memory and crossover.** The method uses three memory slots: current, best and candidate. Crossover operators combine the **current solution** with the **stored best solution**.

**Termination.** The run ends when HyFlex's time limit expires.

This follows the general Choice Function idea of combining individual performance, success and recency terms. It is a simplified variant with fixed weights, not a reproduction of the canonical formulation.

### `InitialisationTest_HH`

A utility hyper-heuristic that creates an initial solution and stops. It's used to check initialisation.

---

## AI and search techniques

- **Local search**: neighbourhood search over adjacent swaps and segment reversals
- **Hill climbing**: pass-based acceptance of improving adjacent swaps
- **Next descent**: first-improvement local search
- **Best-improvement 2-opt** with delta evaluation
- **Mutation**: swap, adjacent swap and reinsertion perturbations, scaled by intensity
- **Crossover**: order crossover (OX) and edge recombination (ERX) on permutations
- **Hyper-heuristics**: heuristic selection that works only on heuristic indices and objective values, independent of the problem domain
- **Adaptive heuristic selection**: online scoring of heuristics with decay
- **Threshold acceptance**: a linearly decreasing acceptance threshold
- **Random initialisation**: Fisher–Yates shuffled permutations

---

## Instances

All instances are in `instances/ssp/`. Counts below are the number of POIs listed in each file; every instance also has one hotel and one airport.

| File | POIs | Description (from file header and contents) |
|---|---|---|
| `square.ssp` | 6 | Small hand-made instance with points on the perimeter of a square |
| `libraries-15.ssp` | 12 | Nottingham library locations; header cites Open Data Nottingham (Open Government Licence) |
| `carparks-40.ssp` | 38 | Nottingham car park locations; header cites the Open Government Licence |
| `tramstops-85.ssp` | 83 | Nottingham tram stop locations; header cites the Open Government Licence |
| `clustered.ssp` | 34 | Synthetic instance with clustered and sporadic locations |
| `grid.ssp` | 208 | Synthetic large grid |
| `chatgpt-instance-100.ssp` | 98 | Synthetic clustered instance; header states it was generated with ChatGPT |

The Nottingham instances store coordinates as scaled integer longitude/latitude values, and distances are computed on them as planar coordinates. The visualiser labels its output "not to scale".

**Instance file format**

```
NAME : <name>
COMMENT : <comment>
HOTEL_LOCATION
<x> <y>
AIRPORT_LOCATION
<x> <y>
POINTS_OF_INTEREST
<x> <y>
...
EOF
```

---

## Build and run

**Requirements**

- Java **16 or later** (the code uses Java records)
- `lib/HyFlex_1.0.0_ThreadTimer.jar` on the classpath (included)
- No Maven or Gradle; compile with `javac`
- Run from the **repository root**, because instance paths are relative (`instances/ssp/...`)

### Compile

**macOS / Linux**

```bash
javac -d out -cp lib/HyFlex_1.0.0_ThreadTimer.jar $(find src -name '*.java')
```

**Windows (PowerShell)**

```powershell
javac -d out -cp lib/HyFlex_1.0.0_ThreadTimer.jar (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName })
```

### Run

The classpath separator is `:` on macOS/Linux and `;` on Windows.

**macOS / Linux**

```bash
java -cp "out:lib/HyFlex_1.0.0_ThreadTimer.jar" com.aim.project.ssp.runners.ChoiceFunction_VisualRunner
```
```bash
java -cp "out:lib/HyFlex_1.0.0_ThreadTimer.jar" com.aim.project.ssp.runners.SR_IE_VisualRunner
```

**Windows (PowerShell)**

```powershell
java -cp "out;lib/HyFlex_1.0.0_ThreadTimer.jar" com.aim.project.ssp.runners.ChoiceFunction_VisualRunner
```
```powershell
java -cp "out;lib/HyFlex_1.0.0_ThreadTimer.jar" com.aim.project.ssp.runners.SR_IE_VisualRunner
```

### What the runners do

The runner configuration is defined in `HH_Runner_Visual`:

- loads instance index 0 (`square.ssp`) from the list in `SightseeingProblemDomain`
- uses the fixed seed `17032025`
- runs the hyper-heuristic for a time limit of 300,000 ms (5 minutes), measured by HyFlex
- prints the initial solution, the best objective value `f(s_best)` and the best route as `(hotel) - (POI) - … - (airport)`
- opens a Swing window (**SSP Solution Visualiser**) that draws the best route from hotel to airport. Closing the window ends the program.

The visualiser needs a graphical desktop environment. To run on a different instance or with a different time limit, change the values in `HH_Runner_Visual`.

**IntelliJ IDEA:** open the repository folder, mark `src` as a source root, add `lib/HyFlex_1.0.0_ThreadTimer.jar` as a library, and make the run configuration's working directory the repository root.

---

## Project structure

```
.
├── src/com/aim/project/ssp/
│   ├── SightseeingProblemDomain.java   # HyFlex ProblemDomain for the SSP
│   ├── SSPObjectiveFunction.java       # ceil-Euclidean path cost
│   ├── heuristics/                     # mutation, local search and crossover operators
│   ├── hyperheuristics/                # ChoiceFunctionHH, SR_IE_HH, InitialisationTest_HH
│   ├── instance/                       # SSPInstance, Location, InitialisationMode
│   │   └── reader/                     # SSPInstanceReader (.ssp parser)
│   ├── solution/                       # SSPSolution, SolutionRepresentation
│   ├── interfaces/                     # domain, solution, heuristic and visualisation contracts
│   ├── runners/                        # visual runners (entry points)
│   └── visualiser/                     # SSPView (Swing route visualiser)
├── instances/ssp/                      # SSP instance files
├── lib/HyFlex_1.0.0_ThreadTimer.jar    # HyFlex framework (third party)
├── LICENSE
└── README.md
```

---

## Limitations

- The runners use a fixed configuration (instance, seed and time limit are set in code) rather than command-line options.
- The repository holds the submitted implementation and visual runners. It doesn't include an archived multi-seed benchmark study or results tables.
- There is no Maven or Gradle build; compilation is done directly with `javac`.
- The visualiser requires a graphical environment and doesn't run headless.
- It is a coursework-scale implementation, focused on the heuristics and hyper-heuristic design rather than large-scale benchmarking.

---

## Coursework and framework attribution

This project was completed as coursework for an undergraduate **AI Methods** module. It was built inside a framework provided for the coursework:

- Interfaces, scaffolding and parts of the problem model, runners and visualiser were **provided as part of the coursework**. Some provided files were templates that the coursework required completing.
- The repository contains **my completed implementation within that framework**: the problem-domain implementation, low-level heuristics and hyper-heuristics.
- Original `@author` tags in the source files have been preserved. Not every file in this repository was written entirely by me.
- **HyFlex** is a separate third-party framework (see below).

The code is kept as it was submitted.

---

## Third-party dependency: HyFlex

The hyper-heuristic layer and problem domain are built on **HyFlex** (Hyper-heuristics Flexible framework), using its `AbstractClasses.HyperHeuristic` and `AbstractClasses.ProblemDomain` classes.

- Bundled JAR: `lib/HyFlex_1.0.0_ThreadTimer.jar`
- HyFlex comes from the University of Nottingham hyper-heuristics research community and was the framework used for the Cross-domain Heuristic Search Challenge (CHeSC 2011). The bundled JAR contains HyFlex 1.0 classes and example problem domains from that ecosystem.
- HyFlex is **not** covered by this repository's licence. All rights in HyFlex belong to its respective authors.
- **This repository doesn't include licensing or redistribution information for HyFlex.** The JAR is included only so the project can be built as submitted.

---

## License

**All rights reserved.** This repository is public for portfolio and educational reference only. You may not copy, reuse, or submit it as academic coursework, in whole or in part, without prior written permission. See [`LICENSE`](LICENSE).

This notice covers my original contributions only. It does not claim ownership of module-provided framework or template material, or of third-party software such as HyFlex, which remain the property of their respective owners.

Please respect your institution's academic integrity policies.
