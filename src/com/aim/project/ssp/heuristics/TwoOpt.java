package com.aim.project.ssp.heuristics;

import com.aim.project.ssp.interfaces.HeuristicInterface;
import com.aim.project.ssp.interfaces.SSPSolutionInterface;

/**
 * 2-opt local search heuristic for the SSP problem.
 * Reverses the segment between two indices to find the best tour improvement.
 * Number of passes is determined by depthOfSearch.
 */
public class TwoOpt extends HeuristicOperators implements HeuristicInterface {

    public TwoOpt(java.util.Random random) {
        super(random);
    }

    @Override
    public double apply(SSPSolutionInterface solution, double depthOfSearch, double intensityOfMutation) {
        int[] rep = solution.getSolutionRepresentation().getSolutionRepresentation();
        int n = rep.length;

        int maxPasses;
        if      (depthOfSearch < 0.2) maxPasses = 1;
        else if (depthOfSearch < 0.4) maxPasses = 2;
        else if (depthOfSearch < 0.6) maxPasses = 3;
        else if (depthOfSearch < 0.8) maxPasses = 4;
        else                          maxPasses = 5;

        double currentCost = m_oObjectiveFunction.getObjectiveFunctionValue(solution.getSolutionRepresentation());
        for (int pass = 0; pass < maxPasses; pass++) {
            boolean improved = false;
            int bestI = -1, bestJ = -1;
            double bestDelta = 0;
            for (int i = 0; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    double delta = deltaCost(rep, i, j);
                    if (delta < bestDelta) {
                        bestDelta = delta;
                        bestI = i;
                        bestJ = j;
                        improved = true;
                    }
                }
            }
            if (!improved) break;
            reverse(rep, bestI, bestJ);
            currentCost += bestDelta;
        }
        solution.setObjectiveFunctionValue((int) Math.ceil(currentCost));
        return currentCost;
    }

    private double deltaCost(int[] rep, int i, int j) {
        int n = rep.length;
        int nodeB = rep[i];
        int nodeC = rep[j];

        // cost removing
        double remove = 0;
        if (i == 0) {
            remove += m_oObjectiveFunction.getCostBetweenHotelAnd(nodeB);
        } else {
            int nodeA = rep[i-1];
            remove += m_oObjectiveFunction.getCost(nodeA, nodeB);
        }
        if (j == n-1) {
            remove += m_oObjectiveFunction.getCostBetweenAirportAnd(nodeC);
        } else {
            int nodeD = rep[j+1];
            remove += m_oObjectiveFunction.getCost(nodeC, nodeD);
        }

        // cost adding
        double add = 0;
        if (i == 0) {
            add += m_oObjectiveFunction.getCostBetweenHotelAnd(nodeC);
        } else {
            int nodeA = rep[i-1];
            add += m_oObjectiveFunction.getCost(nodeA, nodeC);
        }
        if (j == n-1) {
            add += m_oObjectiveFunction.getCostBetweenAirportAnd(nodeB);
        } else {
            int nodeD = rep[j+1];
            add += m_oObjectiveFunction.getCost(nodeB, nodeD);
        }

        return add - remove;
    }

    private void reverse(int[] rep, int i, int j) {
        while (i < j) {
            int tmp = rep[i]; rep[i] = rep[j]; rep[j] = tmp;
            i++; j--;
        }
    }

    @Override
    public boolean isCrossover() {
        return false;
    }

    @Override
    public boolean usesIntensityOfMutation() {
        return false;
    }

    @Override
    public boolean usesDepthOfSearch() {
        return true;
    }
}
