package com.aim.project.ssp.heuristics;

import com.aim.project.ssp.interfaces.HeuristicInterface;
import com.aim.project.ssp.interfaces.SSPSolutionInterface;

import java.util.Random;

/**
 * SwapMutation: performs one or more random swaps of two cities in the tour
 * where the number of swaps is determined by the intensityOfMutation.
 */
public class SwapMutation extends HeuristicOperators implements HeuristicInterface {

    public SwapMutation(Random rng) {
        super(rng);
    }

    @Override
    public double apply(SSPSolutionInterface solution, double depthOfSearch, double intensityOfMutation) {
        int[] rep = solution.getSolutionRepresentation().getSolutionRepresentation();
        int n = rep.length;

        int numSwaps;
        if (intensityOfMutation < 0.2) numSwaps = 1;
        else if (intensityOfMutation < 0.4) numSwaps = 2;
        else if (intensityOfMutation < 0.6) numSwaps = 4;
        else if (intensityOfMutation < 0.8) numSwaps = 8;
        else if (intensityOfMutation < 1.0) numSwaps = 16;
        else numSwaps = 32;

        // perform swaps - swaps random
        for (int k = 0; k < numSwaps; k++) {
            int i = m_oRandom.nextInt(n);
            int j = m_oRandom.nextInt(n);
            if (i == j) continue;
            swap(rep, i, j);
        }

        // recompute objective value
        int newCost = m_oObjectiveFunction.getObjectiveFunctionValue(solution.getSolutionRepresentation());
        solution.setObjectiveFunctionValue(newCost);
        return newCost;
    }

    @Override
    public boolean isCrossover() {
        return false;
    }

    @Override
    public boolean usesIntensityOfMutation() {
        return true;
    }

    @Override
    public boolean usesDepthOfSearch() {
        return false;
    }
}
