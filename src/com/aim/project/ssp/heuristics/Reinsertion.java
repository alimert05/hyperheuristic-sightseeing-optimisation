package com.aim.project.ssp.heuristics;

import java.util.Random;

import com.aim.project.ssp.interfaces.HeuristicInterface;
import com.aim.project.ssp.interfaces.SSPSolutionInterface;

/**
 * @author Warren G Jackson
 * @since 17/03/2025
 *
 * Re-insertion mutation: removes a location at position i and reinserts at position j
 */
public class Reinsertion extends HeuristicOperators implements HeuristicInterface {

	public Reinsertion(Random oRandom) {
		super(oRandom);
	}

	@Override
	public double apply(SSPSolutionInterface solution,
						double depthOfSearch,
						double intensityOfMutation) {

		int[] rep = solution.getSolutionRepresentation()
				.getSolutionRepresentation();
		int length = rep.length;
		if (length < 2) return m_oObjectiveFunction
				.getObjectiveFunctionValue(solution
						.getSolutionRepresentation());

		int numReinsertions;
		if      (intensityOfMutation < 0.2) numReinsertions = 1;
		else if (intensityOfMutation < 0.4) numReinsertions = 2;
		else if (intensityOfMutation < 0.6) numReinsertions = 3;
		else if (intensityOfMutation < 0.8) numReinsertions = 4;
		else                                 numReinsertions = 5;

		for (int k = 0; k < numReinsertions; k++) {
			int removeIndex = m_oRandom.nextInt(length);
			int insertIndex = m_oRandom.nextInt(length);
			if (removeIndex == insertIndex)
				continue;

			int value = rep[removeIndex];

			if (removeIndex < insertIndex) {
				// shift left
				System.arraycopy(
						rep, removeIndex+1,
						rep, removeIndex,
						insertIndex - removeIndex
				);
				rep[insertIndex] = value;
			} else {
				// shift right
				System.arraycopy(
						rep, insertIndex,
						rep, insertIndex+1,
						removeIndex - insertIndex
				);
				rep[insertIndex] = value;
			}
		}

		int newCost = m_oObjectiveFunction
				.getObjectiveFunctionValue(
						solution.getSolutionRepresentation());
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
