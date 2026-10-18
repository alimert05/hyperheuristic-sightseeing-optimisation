package com.aim.project.ssp.heuristics;


import java.util.Random;

import com.aim.project.ssp.interfaces.HeuristicInterface;
import com.aim.project.ssp.interfaces.SSPSolutionInterface;


/**
 *
 * @author Warren G Jackson
 * @since 17/03/2025
 *
 * See `COMP2001-Project-2025.docx` for further details.
 *
 */
public class NextDescent extends HeuristicOperators implements HeuristicInterface {

	public NextDescent(Random random) {

		super(random);
	}

	@Override
	public double apply(SSPSolutionInterface solution, double dos, double iom) {
		int[] rep = solution.getSolutionRepresentation().getSolutionRepresentation();
		int currentCost = m_oObjectiveFunction.getObjectiveFunctionValue(solution.getSolutionRepresentation());

		int numIterations = 0;
		if (dos >= 0.0 && dos < 0.2){
			numIterations = 1;
		} else if (dos >= 0.2 && dos < 0.4){
			numIterations = 2;
		} else if (dos >= 0.4 && dos < 0.6){
			numIterations = 3;
		} else if (dos >= 0.6 && dos < 0.8){
			numIterations = 4;
		} else if (dos >= 0.8 && dos <= 1.0){
			numIterations = 5;
		}

		for (int i = 0; i < numIterations; i++) {
			boolean improved = false;
			// breaks when finds better solution
			for (int j = 0; j < rep.length - 1; j++) {
				swap(rep, j, j+1);
				int newCost = m_oObjectiveFunction.getObjectiveFunctionValue(solution.getSolutionRepresentation());
				if (newCost < currentCost) {
					currentCost = newCost;
					improved = true;
					break;
				} else {
					swap(rep, j, j+1);
				}
			}
			if (!improved) {
				break;
			}
		}
		solution.setObjectiveFunctionValue(currentCost);
		return currentCost;
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
