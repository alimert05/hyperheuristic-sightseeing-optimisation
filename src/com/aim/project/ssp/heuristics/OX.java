package com.aim.project.ssp.heuristics;

import java.util.Random;

import com.aim.project.ssp.interfaces.ObjectiveFunctionInterface;
import com.aim.project.ssp.interfaces.SSPSolutionInterface;
import com.aim.project.ssp.interfaces.XOHeuristicInterface;

/**
 *
 * @author Warren G Jackson
 * @since 17/03/2025
 *
 * See `COMP2001-Project-2025.docx` for further details.
 *
 */
public class OX implements XOHeuristicInterface {

	private final Random m_oRandom;

	private ObjectiveFunctionInterface m_oObjectiveFunction;

	public OX(Random random) {

		this.m_oRandom = random;
	}

	@Override
	public double apply(SSPSolutionInterface solution, double depthOfSearch, double intensityOfMutation) {

		return -1;
	}

	@Override
	public double apply(SSPSolutionInterface p1, SSPSolutionInterface p2,
						SSPSolutionInterface c, double depthOfSearch, double intensityOfMutation) {

		int[] parent1 = p1.getSolutionRepresentation().getSolutionRepresentation();
		int[] parent2 = p2.getSolutionRepresentation().getSolutionRepresentation();
		int[] child = c.getSolutionRepresentation().getSolutionRepresentation();
		int parentLength = parent1.length;

		for (int i = 0; i < parentLength; i++) {
			child[i] = -1;
		}

		int cut1, cut2;
		do {
			cut1 = m_oRandom.nextInt(parentLength);
			cut2 = m_oRandom.nextInt(parentLength);
			if (cut1 > cut2) {
				int tmp = cut1;
				cut1 = cut2;
				cut2 = tmp;
			}
		} while (cut2 - cut1 + 1 >= parentLength); // can be ==

		for (int i = cut1; i <= cut2; i++) {
			child[i] = parent1[i];
		}

		int childIndex = (cut2 + 1) % parentLength;
		int p2idx = (cut2 + 1) % parentLength;
		while (childIndex != cut1) {
			int candidate = parent2[p2idx];
			boolean found = false;
			for (int i = cut1; i <= cut2; i++) {
				if (child[i] == candidate) {
					found = true;
					break;
				}
			}
			if (!found) {
				child[childIndex] = candidate;
				childIndex = (childIndex + 1) % parentLength;
			}
			p2idx = (p2idx + 1) % parentLength;
		}

		int newCost = m_oObjectiveFunction.getObjectiveFunctionValue(c.getSolutionRepresentation());
		c.setObjectiveFunctionValue(newCost);
		return newCost;
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

	@Override
	public void setObjectiveFunction(ObjectiveFunctionInterface f) {

		this.m_oObjectiveFunction = f;
	}
}
