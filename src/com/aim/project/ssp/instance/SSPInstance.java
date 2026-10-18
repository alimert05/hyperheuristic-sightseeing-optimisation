package com.aim.project.ssp.instance;


import java.util.ArrayList;
import java.util.Random;

import com.aim.project.ssp.SSPObjectiveFunction;
import com.aim.project.ssp.interfaces.ObjectiveFunctionInterface;
import com.aim.project.ssp.interfaces.SSPInstanceInterface;
import com.aim.project.ssp.interfaces.SSPSolutionInterface;
import com.aim.project.ssp.interfaces.SolutionRepresentationInterface;
import com.aim.project.ssp.solution.SSPSolution;
import com.aim.project.ssp.solution.SolutionRepresentation;

/**
 * @author Warren G. Jackson
 * @since 17/03/2025
 *
 */
public class SSPInstance implements SSPInstanceInterface {

	private final Location[] aoLocations;

	private final Location oHotelLocation;

	private final Location oAirportLocation;

	private final int iNumberOfLocations;

	private final Random oRandom;

	private ObjectiveFunctionInterface f;

	public SSPInstance(int iNumberOfLocations, Location[] aoLocations, Location oHotelLocation, Location oAirportLocation, Random random) {

		this.iNumberOfLocations = iNumberOfLocations;
		this.oRandom = random;
		this.aoLocations = aoLocations;
		this.oHotelLocation = oHotelLocation;
		this.oAirportLocation = oAirportLocation;
	}

	@Override
	public SSPSolution createSolution(InitialisationMode mode) {
		int numLoc = iNumberOfLocations;
		int[] representation = new int[numLoc];

		// random
		if (mode == InitialisationMode.RANDOM) {
			for (int i = 0; i < numLoc; i++){
				representation[i] = i;
			}
			// fisher yates shuffle
			for (int i = numLoc - 1; i > 0; i--){
				int j = oRandom.nextInt(i + 1);
				int temp = representation[i];
				representation[i] = representation[j];
				representation[j] = temp;
			}
		} else if (mode == InitialisationMode.CONSTRUCTIVE){ // nearest neighbour greedy
			boolean[] used = new boolean[numLoc];
			double currentX = oHotelLocation.x();
			double currentY = oHotelLocation.y();
			for (int i = 0; i < numLoc; i++){
				double minDist = Double.MAX_VALUE;
				int best = -1;
				for (int j = 0; j < numLoc; j++){
					if (!used[j]){
						Location loc = aoLocations[j];
						double distX = loc.x() - currentX;
						double distY = loc.y() - currentY;
						double dist = Math.ceil(Math.sqrt(distX*distX + distY*distY));
						if (dist < minDist){
							minDist = dist;
							best = j;
						}
					}
				}
				used[best] = true;
				representation[i] = best;
				currentX = aoLocations[best].x();
				currentY = aoLocations[best].y();
			}
		}
		SolutionRepresentationInterface solRep = new SolutionRepresentation(representation);
		SSPSolution solution = new SSPSolution(solRep, 0);
		ObjectiveFunctionInterface objective = getSSPObjectiveFunction();
		int value = objective.getObjectiveFunctionValue(solution.getSolutionRepresentation());
		solution.setObjectiveFunctionValue(value);
		return solution;
	}

	@Override
	public ObjectiveFunctionInterface getSSPObjectiveFunction() {
		if (f == null) {
			f = new SSPObjectiveFunction(this);
		}
		return f;
	}


	@Override
	public int getNumberOfLocations() {
		return iNumberOfLocations;
	}

	@Override
	public Location getSightseeingLocation(int iLocationId) {
		// aoLocation stores sightSeeing locations
		return aoLocations[iLocationId];
	}

	@Override
	public Location getHotelLocation() {
		return oHotelLocation;
	}

	@Override
	public Location getAirportLocation() {
		return oAirportLocation;
	}

	@Override
	public ArrayList<Location> getSolutionAsListOfLocations(SSPSolutionInterface oSolution) {
		int[] representation = oSolution.getSolutionRepresentation().getSolutionRepresentation();
		ArrayList<Location> list = new ArrayList<>(representation.length);
		for (int idx : representation) {
			list.add(aoLocations[idx]);
		}
		return list;
	}
}
