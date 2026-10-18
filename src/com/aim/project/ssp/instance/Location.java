package com.aim.project.ssp.instance;

/**
 * @author Warren G Jackson
 * @since 17/03/2025
 * 
 * Keeps a record of the x and y coordinates of a Location
 *
 * @param x The x-coordinate.
 * @param y The y-coordinate.
 */
public record Location (int x, int y) {

	public String toString() {
		return "(" + x + "," + y + ")";
	}
}
