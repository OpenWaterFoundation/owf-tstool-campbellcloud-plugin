package org.openwaterfoundation.tstool.plugin.campbellcloud.dao;

import java.util.Comparator;

/**
 * Comparator for Collections.sort to sort Station, currently by name.
 */
public class StationComparator implements Comparator<Station> {

	/**
	 * Constructor.
	 */
	public StationComparator () {
	}
	
	/**
	 * If stationA is < stationB, return -1.
	 * If stationA = stationB, return 0.
	 * If stationA is > stationB, return 1
	 */
	public int compare(Station stationA, Station stationB) {
		String nameA = stationA.getMetadata().getName();
		String nameB = stationB.getMetadata().getName();

		return nameA.compareTo(nameB);
	}
}