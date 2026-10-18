package com.aim.project.ssp.heuristics;

import com.aim.project.ssp.interfaces.XOHeuristicInterface;
import com.aim.project.ssp.interfaces.SSPSolutionInterface;
import com.aim.project.ssp.interfaces.ObjectiveFunctionInterface;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Edge-Recombination Crossover (ERX) for SSP.
 * Builds an edge table from both parents and constructs a child,
 * choosing next city based on fewest remaining neighbors.
 */
public class EdgeRecombination extends HeuristicOperators implements XOHeuristicInterface {

    private ObjectiveFunctionInterface m_oObjectiveFunction;
    private final Random m_oRandom;

    public EdgeRecombination(Random random) {
        super(random);
        this.m_oRandom = random;
    }

    @Override
    public double apply(SSPSolutionInterface solution, double dos, double iom) {
        return solution.getObjectiveFunctionValue();
    }

    @Override
    public double apply(SSPSolutionInterface p1,
                        SSPSolutionInterface p2,
                        SSPSolutionInterface c,
                        double dos,
                        double iom) {
        int[] parent1 = p1.getSolutionRepresentation().getSolutionRepresentation();
        int[] parent2 = p2.getSolutionRepresentation().getSolutionRepresentation();
        int n = parent1.length;
        int[] childRep = new int[n];

        // Build adjacency lists
        Map<Integer, Set<Integer>> adj = new HashMap<>();
        for (int i = 0; i < n; i++) {
            adj.put(i, new HashSet<>());
        }
        // Helper to add bi-directional edge
        java.util.function.BiConsumer<Integer,Integer> addEdge = (a,b) -> {
            if (a != b) {
                adj.get(a).add(b);
                adj.get(b).add(a);
            }
        };
        // From parent1
        for (int i = 0; i < n; i++) {
            int city = parent1[i];
            int left = parent1[(i - 1 + n) % n];
            int right = parent1[(i + 1) % n];
            addEdge.accept(city, left);
            addEdge.accept(city, right);
        }
        // From parent2
        for (int i = 0; i < n; i++) {
            int city = parent2[i];
            int left = parent2[(i - 1 + n) % n];
            int right = parent2[(i + 1) % n];
            addEdge.accept(city, left);
            addEdge.accept(city, right);
        }

        boolean[] used = new boolean[n];
        // Start child at p1[0] for determinism
        int current = parent1[0];
        for (int pos = 0; pos < n; pos++) {
            childRep[pos] = current;
            used[current] = true;
            // Remove current from all adjacency lists
            for (Set<Integer> neighbors : adj.values()) {
                neighbors.remove(current);
            }
            if (pos == n - 1) break;
            Set<Integer> neighbors = adj.get(current);
            int next;
            if (neighbors.isEmpty()) {
                // pick random unused
                List<Integer> avail = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    if (!used[i]) avail.add(i);
                }
                next = avail.get(m_oRandom.nextInt(avail.size()));
            } else {
                // choose neighbor with fewest neighbors
                int minSize = Integer.MAX_VALUE;
                List<Integer> candidates = new ArrayList<>();
                for (int v : neighbors) {
                    int sz = adj.get(v).size();
                    if (sz < minSize) {
                        minSize = sz;
                        candidates.clear();
                        candidates.add(v);
                    } else if (sz == minSize) {
                        candidates.add(v);
                    }
                }
                next = candidates.get(m_oRandom.nextInt(candidates.size()));
            }
            current = next;
        }

        // Write child representation
        c.getSolutionRepresentation().setSolutionRepresentation(childRep);
        int cost = m_oObjectiveFunction.getObjectiveFunctionValue(c.getSolutionRepresentation());
        c.setObjectiveFunctionValue(cost);
        return cost;
    }

    @Override
    public void setObjectiveFunction(ObjectiveFunctionInterface f) {
        this.m_oObjectiveFunction = f;
    }

    @Override
    public boolean isCrossover() {
        return true;
    }

    @Override
    public boolean usesIntensityOfMutation() {
        return false;
    }

    @Override
    public boolean usesDepthOfSearch() {
        return false;
    }
}
