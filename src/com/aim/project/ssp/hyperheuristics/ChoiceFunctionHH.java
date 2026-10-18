package com.aim.project.ssp.hyperheuristics;

import AbstractClasses.HyperHeuristic;
import AbstractClasses.ProblemDomain;

/**
 * Choice‐Function HH with iteration‐based, deterministic threshold acceptance.
 * All randomness comes only from the single fixed RNG seed.
 */
public class ChoiceFunctionHH extends HyperHeuristic {

    private double[] f1, f2, f3;
    private int[]    lastUsed;
    private int      iter = 0;

    private final double alpha = 0.5;   // f1 weight
    private final double beta  = 0.3;   // f2 weight
    private final double gamma = 0.2;   // f3 weight

    private final double tau0    = 11000;  // initial threshold
    private final int    MAX_ITER = 100_000; // iterations until tau→0

    public ChoiceFunctionHH(long seed) {
        super(seed);
    }

    @Override
    protected void solve(ProblemDomain problem) {
        problem.setMemorySize(3);
        int current    = 0,
                bestParent = 1,
                child       = 2;

        problem.initialiseSolution(current);
        problem.copySolution(current, bestParent);

        double currentValue = problem.getFunctionValue(current);
        double bestValue    = currentValue;

        int baseCount = problem.getNumberOfHeuristics();
        int xoCount   = problem.getHeuristicsOfType(ProblemDomain.HeuristicType.CROSSOVER).length;
        int totalHeur = baseCount + xoCount;

        f1 = new double[totalHeur];
        f2 = new double[totalHeur];
        f3 = new double[totalHeur];
        lastUsed = new int[totalHeur];
        for (int h = 0; h < totalHeur; h++) {
            f1[h] = f2[h] = 1.0;
            f3[h] = 0;
            lastUsed[h] = 0;
        }

        while (!hasTimeExpired()) {
            // deterministic threshold decay
            double tau = tau0 * Math.max(0.0, 1.0 - (double)iter / MAX_ITER);

            // compute CF scores
            int hBest = 0;
            double bestScore = alpha*f1[0] + beta*f2[0] + gamma*f3[0];
            for (int h = 1; h < totalHeur; h++) {
                double score = alpha*f1[h] + beta*f2[h] + gamma*f3[h];
                if (score > bestScore) {
                    bestScore = score;
                    hBest = h;
                }
            }

            // apply chosen heuristic
            double candValue;
            if (hBest < baseCount) {
                candValue = problem.applyHeuristic(hBest, current, child);
            } else {
                // crossover index
                candValue = problem.applyHeuristic(
                        hBest, current, bestParent, child
                );
            }

            // deterministic threshold acceptance
            double delta = candValue - currentValue; // ≥0 if worse
            boolean accept = delta <= tau;
            if (accept) {
                // swap in child
                int tmp = current; current = child; child = tmp;
                currentValue = candValue;
                if (currentValue < bestValue) {
                    bestValue = currentValue;
                    problem.copySolution(current, bestParent);
                }
            }

            // update CF stats
            // f1: magnitude
            for (int h = 0; h < totalHeur; h++) f1[h] *= 0.9;
            if (delta < 0) f1[hBest] += 0.1 * (-delta);

            // f2: success rate
            for (int h = 0; h < totalHeur; h++) f2[h] *= 0.9;
            if (delta <= tau) f2[hBest] += 0.1;

            // f3: recency
            for (int h = 0; h < totalHeur; h++) {
                f3[h] = iter - lastUsed[h];
            }
            lastUsed[hBest] = iter;

            iter++;
        }
    }

    @Override
    public String toString() {
        return "ChoiceFunction_HH_Deterministic";
    }
}