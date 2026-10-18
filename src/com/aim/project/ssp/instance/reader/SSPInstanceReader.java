package com.aim.project.ssp.instance.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.aim.project.ssp.instance.Location;
import com.aim.project.ssp.instance.SSPInstance;
import com.aim.project.ssp.interfaces.SSPInstanceInterface;
import com.aim.project.ssp.interfaces.SSPInstanceReaderInterface;

/**
 * Robust SSP instance reader: skips arbitrary header/comment lines and
 * reads hotel, airport, and all points-of-interest until EOF.
 */
public class SSPInstanceReader implements SSPInstanceReaderInterface {

	@Override
	public SSPInstanceInterface readSSPInstance(Path path, Random random) {
		try (BufferedReader reader = Files.newBufferedReader(path)) {
			String line;
			// Read until HOTEL_LOCATION
			while ((line = reader.readLine()) != null) {
				if (line.trim().equalsIgnoreCase("HOTEL_LOCATION")) {
					break;
				}
			}
			// Next line is hotel coords
			line = reader.readLine();
			String[] tokens = line.trim().split("\\s+");
			Location hotel = new Location(
					Integer.parseInt(tokens[0]),
					Integer.parseInt(tokens[1])
			);

			// Read until AIRPORT_LOCATION
			while ((line = reader.readLine()) != null) {
				if (line.trim().equalsIgnoreCase("AIRPORT_LOCATION")) {
					break;
				}
			}
			// Next line is airport coords
			line = reader.readLine();
			tokens = line.trim().split("\\s+");
			Location airport = new Location(
					Integer.parseInt(tokens[0]),
					Integer.parseInt(tokens[1])
			);

			// Read until POINTS_OF_INTEREST
			while ((line = reader.readLine()) != null) {
				if (line.trim().equalsIgnoreCase("POINTS_OF_INTEREST")) {
					break;
				}
			}
			// Now read all POI coords until EOF
			List<Location> poiList = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.equalsIgnoreCase("EOF")) break;
				if (line.isEmpty()) continue;
				tokens = line.split("\\s+");
				poiList.add(new Location(
						Integer.parseInt(tokens[0]),
						Integer.parseInt(tokens[1])
				));
			}

			// Convert list to array and instantiate
			Location[] locations = poiList.toArray(new Location[0]);
			return new SSPInstance(locations.length, locations, hotel, airport, random);

		} catch (IOException e) {
			throw new RuntimeException("Failed to read SSP instance: " + path, e);
		}
	}
}