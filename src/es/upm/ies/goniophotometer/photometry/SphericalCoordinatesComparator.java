package es.upm.ies.goniophotometer.photometry;

import java.util.Comparator;

/**
 * Comparator of <code>SphericalCoordinates</code> class.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class SphericalCoordinatesComparator implements Comparator<SphericalCoordinates> {

	/**
	 * li1 is greater than li2 if its theta coordinate is greater. In case their
	 * theta coordinates are equal, li1 is greater than li2 if its phi
	 * coordinate is greater. They are equal if their coordinates are the same.
	 */
	public int compare(SphericalCoordinates li1, SphericalCoordinates li2) {
		int res = 0;
		if (li1.getTheta() != li2.getTheta()) {
			if (li1.getTheta() > li2.getTheta()) {
				res = 1;
			} else {
				res = -1;
			}
		} else {
			if (li1.getPhi() > li2.getPhi()) {
				res = 1;
			} else if (li1.getPhi() < li2.getPhi()) {
				res = -1;
			}
		}
		return res;
	}

}
