
package com.aim.project.ssp;

import com.aim.project.ssp.instance.InitialisationMode;
import com.aim.project.ssp.instance.Location;
import com.aim.project.ssp.instance.reader.SSPInstanceReader;
import com.aim.project.ssp.interfaces.*;
import AbstractClasses.ProblemDomain;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SightseeingProblemDomain extends ProblemDomain
		implements Visualisable, InLabPracticalExamInterface {

	private static final String[] INSTANCE_FILES = {
			"instances/ssp/square.ssp",
			"instances/ssp/libraries-15.ssp",
			"instances/ssp/carparks-40.ssp",
			"instances/ssp/tramstops-85.ssp",
			"instances/ssp/grid.ssp",
			"instances/ssp/clustered.ssp",
			"instances/ssp/chatgpt-instance-100.ssp"
	};

	public SSPInstanceInterface m_oInstance;
	private SSPSolutionInterface[] memory;
	private SSPSolutionInterface m_oInitialSolution;
	private int initialSolutionIndex = -1;
	private int bestSolutionIndex = -1;
	private final HeuristicInterface[] lowLevelHeuristics;
	private final XOHeuristicInterface[] xoHeuristics;

	public SightseeingProblemDomain(long seed) {
		super(seed);

		setMemorySize(2);

		// 6 mutation and local search
		lowLevelHeuristics = new HeuristicInterface[]{
				new com.aim.project.ssp.heuristics.AdjacentSwap(rng),
				new com.aim.project.ssp.heuristics.Reinsertion(rng),
				new com.aim.project.ssp.heuristics.NextDescent(rng),
				new com.aim.project.ssp.heuristics.DavissHillClimbing(rng),
				new com.aim.project.ssp.heuristics.TwoOpt(rng),
				new  com.aim.project.ssp.heuristics.SwapMutation(rng)
		};
		// crossover operators
		xoHeuristics = new XOHeuristicInterface[]{
				new com.aim.project.ssp.heuristics.OX(rng),
				new com.aim.project.ssp.heuristics.EdgeRecombination(rng)
		};
	}


	public SSPSolutionInterface getSolution(int index) {
		return memory[index];
	}


	public SSPSolutionInterface getBestSolution() {
		return memory[bestSolutionIndex];
	}
	// non-crossover
	@Override
	public double applyHeuristic(int hIndex, int currentIndex, int candidateIndex) {
		SSPSolutionInterface src  = memory[currentIndex];
		SSPSolutionInterface dest = src.clone();
		memory[candidateIndex]    = dest;

		double val = lowLevelHeuristics[hIndex].apply(dest, getDepthOfSearch(), getIntensityOfMutation());

		if (val < memory[bestSolutionIndex].getObjectiveFunctionValue()) {
			bestSolutionIndex = candidateIndex;
		}
		return val;
	}

	// crossover
	@Override
	public double applyHeuristic(int hIndex, int parent1Index, int parent2Index, int candidateIndex) {
		int xoIdx = hIndex - lowLevelHeuristics.length;
		SSPSolutionInterface child = memory[parent1Index].clone();
		memory[candidateIndex] = child;

		double val = xoHeuristics[xoIdx].apply(memory[parent1Index], memory[parent2Index], child, getDepthOfSearch(), getIntensityOfMutation());

		if (val < memory[bestSolutionIndex].getObjectiveFunctionValue()) {
			bestSolutionIndex = candidateIndex;
		}
		return val;
	}

	@Override
	public String bestSolutionToString() {
		return solutionToString(bestSolutionIndex);
	}

	@Override
	public boolean compareSolutions(int iIndex1, int iIndex2) {
		return memory[iIndex1].getObjectiveFunctionValue() < memory[iIndex2].getObjectiveFunctionValue();
	}

	@Override
	public void copySolution(int a, int b) {
		memory[b] = memory[a].clone();
	}

	@Override
	public double getBestSolutionValue() {
		return memory[bestSolutionIndex].getObjectiveFunctionValue();
	}

	@Override
	public double getFunctionValue(int index) {
		return memory[index].getObjectiveFunctionValue();
	}

	@Override
	public int[] getHeuristicsOfType(HeuristicType type) {
		List<Integer> list = new ArrayList<>();

		// include mutation
		if (type == HeuristicType.MUTATION) {
			for (int i = 0; i < lowLevelHeuristics.length; i++) {
				if (!lowLevelHeuristics[i].isCrossover()
						&& lowLevelHeuristics[i].usesIntensityOfMutation()) {
					list.add(i);
				}
			}
		}

		// include local search
		if (type == HeuristicType.LOCAL_SEARCH) {
			for (int i = 0; i < lowLevelHeuristics.length; i++) {
				if (!lowLevelHeuristics[i].isCrossover() && lowLevelHeuristics[i].usesDepthOfSearch()) {
					list.add(i);
				}
			}
		}
		// include crossover
		if(type == HeuristicType.CROSSOVER) {
			for (int i = 0; i < xoHeuristics.length; i++) {
				if (xoHeuristics[i].isCrossover()){
					list.add(i);
				}
			}
		}
		return list.stream().mapToInt(x -> x).toArray();
	}

	@Override
	public int[] getHeuristicsThatUseDepthOfSearch() {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < lowLevelHeuristics.length; i++) {
			if (lowLevelHeuristics[i].usesDepthOfSearch()) {
				list.add(i);
			}
		}
		return list.stream().mapToInt(x -> x).toArray();
	}

	@Override
	public int[] getHeuristicsThatUseIntensityOfMutation() {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < lowLevelHeuristics.length; i++) {
			if (lowLevelHeuristics[i].usesIntensityOfMutation()) {
				list.add(i);
			}
		}
		return list.stream().mapToInt(x -> x).toArray();
	}

	@Override
	public int getNumberOfHeuristics() {
		return 6;
	}

	@Override
	public int getNumberOfInstances() {
		return INSTANCE_FILES.length;
	}

	@Override
	public void initialiseSolution(int index) {
		// Initialising in random mode
		SSPSolutionInterface sol = m_oInstance.createSolution(InitialisationMode.RANDOM);
		memory[index] = sol;

		if (initialSolutionIndex < 0) {
			initialSolutionIndex = index;
			bestSolutionIndex    = index;
			m_oInitialSolution = sol.clone();
		}
	}

	@Override
	public void loadInstance(int instanceId) {
		String file = INSTANCE_FILES[instanceId];
		m_oInstance = new SSPInstanceReader().readSSPInstance(Path.of(file), rng);

		// set objective in all heuristics
		ObjectiveFunctionInterface f = m_oInstance.getSSPObjectiveFunction();
		for (var h : lowLevelHeuristics) h.setObjectiveFunction(f);
		for (var xo : xoHeuristics)   xo.setObjectiveFunction(f);

		// clear memory and reset
		initialSolutionIndex = bestSolutionIndex = -1;
		for (int i = 0; i < memory.length; i++) memory[i] = null;
	}

	@Override
	public void setMemorySize(int size) {
		memory = new SSPSolutionInterface[size];
	}

	@Override
	public String solutionToString(int index) {
		StringBuilder sb = new StringBuilder();
		Location hotel   = m_oInstance.getHotelLocation();
		Location airport = m_oInstance.getAirportLocation();

		sb.append("(").append(hotel.x()).append(",").append(hotel.y()).append(")");

		for (Location l : m_oInstance.getSolutionAsListOfLocations(memory[index])) {
			sb.append(" - (").append(l.x()).append(",").append(l.y()).append(")");
		}

		sb.append(" - (").append(airport.x()).append(",").append(airport.y()).append(")");
		return sb.toString();
	}

	@Override
	public String toString() {
		return "[Ali's] SSP Domain";
	}

	@Override
	public SSPInstanceInterface getLoadedInstance() {
		return m_oInstance;
	}

	@Override
	public Location[] getRouteOrderedByLocations() {
		List<Location> route = m_oInstance.getSolutionAsListOfLocations(memory[bestSolutionIndex]);
		return route.toArray(new Location[0]);
	}

	@Override
	public void printBestSolutionFound() {
		System.out.println(bestSolutionToString());
	}

	@Override
	public void printObjectiveValueOfTheSolutionFound() {
		System.out.println(memory[bestSolutionIndex].getObjectiveFunctionValue());
	}

	@Override
	public void printInitialSolution() {
		StringBuilder sb = new StringBuilder();
		Location h = m_oInstance.getHotelLocation();
		sb.append("(").append(h.x()).append(",").append(h.y()).append(")");
		for (Location l : m_oInstance.getSolutionAsListOfLocations(m_oInitialSolution)) {
			sb.append(" - (").append(l.x()).append(",").append(l.y()).append(")");
		}
		Location a = m_oInstance.getAirportLocation();
		sb.append(" - (").append(a.x()).append(",").append(a.y()).append(")");
		System.out.println(sb);
	}


	@Override
	public void printObjectiveValueOfTheInitialSolution() {
		System.out.println(m_oInitialSolution.getObjectiveFunctionValue());
	}
}
