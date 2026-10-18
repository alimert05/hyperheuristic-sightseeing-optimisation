package com.aim.project.ssp;

import com.aim.project.ssp.instance.Location;
import com.aim.project.ssp.interfaces.ObjectiveFunctionInterface;
import com.aim.project.ssp.interfaces.SSPInstanceInterface;
import com.aim.project.ssp.interfaces.SolutionRepresentationInterface;

/**
 * @author Warren G Jackson
 * @since 17/03/2025
 */
public class SSPObjectiveFunction implements ObjectiveFunctionInterface {

	private final SSPInstanceInterface oInstance;

	public SSPObjectiveFunction(SSPInstanceInterface oInstance) {

		this.oInstance = oInstance;
	}

	// solution cost
	@Override
	public int getObjectiveFunctionValue(SolutionRepresentationInterface oSolution) {
		int[] rep = oSolution.getSolutionRepresentation();
		int totalCost = getCostBetweenHotelAnd(rep[0]);
		for (int i = 0; i < rep.length - 1; i++) {
			totalCost += getCost(rep[i], rep[i + 1]);
		}
		totalCost += getCostBetweenAirportAnd(rep[rep.length - 1]);
		return totalCost;
	}

	@Override
	public int getCost(int iLocationA, int iLocationB) {
		Location a = oInstance.getSightseeingLocation(iLocationA);
		Location b = oInstance.getSightseeingLocation(iLocationB);
		double dx = a.x() - b.x();
		double dy = a.y() - b.y();
		return (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
	}

	@Override
	public int getCostBetweenHotelAnd(int iLocation) {
		Location a = oInstance.getSightseeingLocation(iLocation);
		Location hotelLocation = oInstance.getHotelLocation();
		double dx = hotelLocation.x() - a.x();
		double dy = hotelLocation.y() - a.y();
		return (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
	}

	@Override
	public int getCostBetweenAirportAnd(int iLocation) {
		Location a = oInstance.getSightseeingLocation(iLocation);
		Location airportLocation = oInstance.getAirportLocation();
		double dx = airportLocation.x() - a.x();
		double dy = airportLocation.y() - a.y();
		return (int) Math.ceil(Math.sqrt(dx * dx + dy * dy));
	}
}
