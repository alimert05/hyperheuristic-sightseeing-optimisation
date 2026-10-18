package com.aim.project.ssp.heuristics;

import com.aim.project.ssp.interfaces.HeuristicInterface;
import com.aim.project.ssp.interfaces.SSPSolutionInterface;

import java.util.Random;


/**
 *
 * @author Warren G Jackson
 * @since 17/03/2025
 *
 * See `COMP2001-Project-2025.docx` for further details.
 *
 */
public class AdjacentSwap extends HeuristicOperators implements HeuristicInterface {

	public AdjacentSwap(Random random) {

		super(random);
	}

	@Override
	public double apply(SSPSolutionInterface solution, double depthOfSearch, double intensityOfMutation) {

		int[] rep = solution.getSolutionRepresentation().getSolutionRepresentation();

		int numSwaps = 0;
		if (intensityOfMutation >= 0.0 && intensityOfMutation < 0.2) {
			numSwaps = 1;
		} else if (intensityOfMutation >= 0.2 && intensityOfMutation < 0.4) {
			numSwaps = 2;
		} else if (intensityOfMutation >= 0.4 && intensityOfMutation < 0.6) {
			numSwaps = 4;
		} else if (intensityOfMutation >= 0.6 && intensityOfMutation < 0.8) {
			numSwaps = 8;
		} else if (intensityOfMutation >= 0.8 && intensityOfMutation < 1.0) {
			numSwaps = 16;
		} else if (intensityOfMutation == 1.0) {
			numSwaps = 32;
		}

		// Simple swap - swaps neighbour
		for (int i = 0; i < numSwaps; i++){
			int index = m_oRandom.nextInt(rep.length - 1);
			swap(rep, index, index + 1);
		}

		int newcost = m_oObjectiveFunction.getObjectiveFunctionValue(solution.getSolutionRepresentation());
		solution.setObjectiveFunctionValue(newcost);
		return newcost;
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
