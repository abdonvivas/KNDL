package es.upm.ies.goniophotometer.photometry;

import java.util.ArrayList;
import java.util.Iterator;

import es.upm.ies.goniophotometer.exceptions.InconsistentMeasureException;

/**
 * Class with static methods to calculate the luminous flux and the horizontal
 * and vertical FWHM.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class PhotometryCalculations {
	/**
	 * Computes the luminous flux from a list of the luminous intensities as
	 * the radial coordinates of <code>lumints</code>.
	 * 
	 * @param lumints
	 *            <code>ArrayList&#60;SphericalCoordinates&#62;</code> with the
	 *            luminous intensities as radial coordinates.
	 * @param resTheta
	 *            The resolution of the polar angle, in degrees per sample.
	 * @param resPhi
	 *            The resolution of the azimuth angle, in degrees per sample.
	 * 
	 * @return The luminous flux.
	 * @throws InconsistentMeasureException
	 *             If the number of measures exceeds the maximum possible number
	 *             of measures.
	 */
	public static double computeLumFlux(ArrayList<SphericalCoordinates> lumints, float resTheta, float resPhi)
			throws InconsistentMeasureException {

		double lumFlux = 0;// Luminous flux
		// Number of Measures
		int nMeasuresSemiSphere = (int) ((360 / resPhi) * (90 / resTheta) + 1);
		int maxNumberOfMeasures = (int) ((360 / resPhi) * ((180 / resTheta) - 1) + 2);

		if (lumints.size() <= nMeasuresSemiSphere) {
			lumFlux = semiSphereLumFlux(lumints, resTheta, resPhi);
		} else if (lumints.size() <= maxNumberOfMeasures) {
			lumFlux = fullSphereLumFlux(lumints, resTheta, resPhi);
		} else {
			throw new InconsistentMeasureException();
		}

		return lumFlux;
	}

	/*
	 * Calculates the luminous flux for measures that fits inside a semi-sphere.
	 */
	private static double semiSphereLumFlux(ArrayList<SphericalCoordinates> lumints, float resTheta, float resPhi) {
		double lumFlux = 0;// Luminous flux
		// (*) See at the end of this method for a description of the array
		// bellow
		double[] slice = new double[(int) (90 / resTheta + 1)];

		float[] rings = new float[(int) (90 / resTheta + 1)];
		// Parallel sums
		double[] ringSums = new double[(int) (90 / resTheta + 1)];

		/*--- CALCULATE NECESSARY SURFACE AREAS ---*/
		slice[0] = Math.cos(Math.toRadians(0)) - Math.cos(Math.toRadians(resTheta / 2));
		slice[0] *= 2 * Math.PI;
		rings[0] = 0;
		for (int n = 1; n < slice.length - 1; n++) {
			double thetaL = (2 * (n - 1) + 1) * resTheta / 2;
			double thetaH = (2 * n + 1) * resTheta / 2;
			slice[n] = Math.cos(Math.toRadians(thetaL)) - Math.cos(Math.toRadians(thetaH));
			slice[n] *= Math.toRadians(resPhi);
			rings[n] = resTheta * n;
		}
		slice[slice.length - 1] = Math.cos(Math.toRadians(90 - (resTheta / 2))) - Math.cos(Math.toRadians(90));
		slice[slice.length - 1] *= Math.toRadians(resPhi);
		rings[rings.length - 1] = resTheta * (rings.length - 1);
		/*--- CALCULATE NECESSARY SURFACE AREAS ---*/

		/*--- COMPUTE INTEGRAL ---*/
		Iterator<SphericalCoordinates> itr = lumints.iterator();
		SphericalCoordinates sc = new SphericalCoordinates();
		float currentTheta = 0;
		while (itr.hasNext()) {
			sc = itr.next();
			currentTheta = sc.getTheta();
			for (int i = 0; i < rings.length; i++) {
				if (currentTheta == rings[i]) {
					ringSums[i] += sc.getR();
					break;
				}
			}
		}

		for (int i = 0; i < ringSums.length; i++) {
			lumFlux += ringSums[i] * slice[i];
		}
		/*--- COMPUTE INTEGRAL ---*/
		return lumFlux;
		/*
		 * (*) 'slice' is an array with the surface areas of portions of one
		 * slice of semi-sphere divided by r^2 (because we are considering
		 * luminous intensity instead of illuminance). Actually, they are not
		 * divided by r^2. It just omits r^2 from the area equation
		 */
	}

	/*
	 * Calculates the luminous flux for measures that doesn't fit inside a
	 * semi-sphere.
	 */
	private static double fullSphereLumFlux(ArrayList<SphericalCoordinates> lumints, float resTheta, float resPhi) {

		double lumFlux = 0;// Luminous flux

		// (*) See at the end of this method for a description of the array
		// bellow
		double[] slice = new double[(int) (180 / resTheta + 1)];

		float[] rings = new float[(int) (180 / resTheta + 1)];
		// Parallel sums
		double[] ringSums = new double[(int) (180 / resTheta + 1)];

		/*--- CALCULATE NECESSARY SURFACE AREAS ---*/
		slice[0] = Math.cos(Math.toRadians(0)) - Math.cos(Math.toRadians(resTheta / 2));
		slice[0] *= 2 * Math.PI;
		rings[0] = 0;
		for (int n = 1; n < slice.length - 1; n++) {
			double thetaL = (2 * (n - 1) + 1) * resTheta / 2;
			double thetaH = (2 * n + 1) * resTheta / 2;
			slice[n] = Math.cos(Math.toRadians(thetaL)) - Math.cos(Math.toRadians(thetaH));
			slice[n] *= Math.toRadians(resPhi);
			rings[n] = resTheta * n;
		}
		slice[slice.length - 1] = slice[0];
		rings[rings.length - 1] = resTheta * (rings.length - 1);
		/*--- CALCULATE NECESSARY SURFACE AREAS ---*/

		/*--- COMPUTE INTEGRAL ---*/
		Iterator<SphericalCoordinates> itr = lumints.iterator();
		SphericalCoordinates sc = new SphericalCoordinates();
		float currentTheta = 0;
		while (itr.hasNext()) {
			sc = itr.next();
			currentTheta = sc.getTheta();
			for (int i = 0; i < rings.length; i++) {
				if (currentTheta == rings[i]) {
					ringSums[i] += sc.getR();
					break;
				}
			}
		}

		for (int i = 0; i < ringSums.length; i++) {
			lumFlux += ringSums[i] * slice[i];
		}
		/*--- COMPUTE INTEGRAL ---*/
		return lumFlux;
		/*
		 * (*) 'slice' is an array with the surface areas of portions of one
		 * slice of semi-sphere divided by r^2 (because we are considering
		 * luminous intensity instead of illuminance). Actually, they are not
		 * divided by r^2. It just omits r^2 from the area equation
		 */
	}

	/**
	 * Computes the vertical and horizontal Full Width at half maximum (FWHM).
	 * 
	 * @param lumints
	 *            <code>ArrayList&#60;SphericalCoordinates&#62;</code> with the
	 *            luminous intensities as radial coordinates.
	 * @param resTheta
	 *            The resolution of the polar angle, in degrees per sample.
	 * @param resPhi
	 *            The resolution of the azimuth angle, in degrees per sample.
	 * @return The horizontal and vertical FWHM.
	 */
	public static HVFWHM computeHVFWHM(ArrayList<SphericalCoordinates> lumints, float resTheta, float resPhi) {

		HVFWHM hvFWHM = new HVFWHM();

		double halfMax = 0;
		float halfWidthH1;
		float halfWidthH2;
		float halfWidthV1;
		float halfWidthV2;
		ArrayList<SphericalCoordinates> horizontalCut1 = new ArrayList<SphericalCoordinates>();
		ArrayList<SphericalCoordinates> horizontalCut2 = new ArrayList<SphericalCoordinates>();
		ArrayList<SphericalCoordinates> verticalCut1 = new ArrayList<SphericalCoordinates>();
		ArrayList<SphericalCoordinates> verticalCut2 = new ArrayList<SphericalCoordinates>();

		for (SphericalCoordinates lumint : lumints) {
			if (lumint.getR() > halfMax) {
				halfMax = lumint.getR();
			}
			if (lumint.getPhi() == 0) {
				horizontalCut1.add(lumint);
			} else if (lumint.getPhi() == 180) {
				horizontalCut2.add(lumint);
			} else if (lumint.getPhi() == 90) {
				verticalCut1.add(lumint);
			} else if (lumint.getPhi() == 270) {
				verticalCut2.add(lumint);
			}
			if (lumint.getTheta() == 180) {
				horizontalCut1.add(lumint);
				horizontalCut2.add(lumint);
				verticalCut1.add(lumint);
				verticalCut2.add(lumint);
			}
		}
		halfMax = halfMax / 2;

		halfWidthH1 = closestToHalfPolar(halfMax, horizontalCut1);
		halfWidthH2 = closestToHalfPolar(halfMax, horizontalCut2);
		halfWidthV1 = closestToHalfPolar(halfMax, verticalCut1);
		halfWidthV2 = closestToHalfPolar(halfMax, verticalCut2);

		hvFWHM.setHFWHM(halfWidthH1 + halfWidthH2);
		hvFWHM.setVFWHM(halfWidthV1 + halfWidthV2);

		return hvFWHM;
	}

	/*
	 * Returns the polar angle of the Luminous intensity contained in lumints
	 * whose value is the closest to the half maximum.
	 */
	private static float closestToHalfPolar(double halfMax, ArrayList<SphericalCoordinates> lumints) {
		float theta = 0;
		double currentValue = 0;
		double closestValue = Double.POSITIVE_INFINITY;
		for (SphericalCoordinates lumint : lumints) {
			currentValue = Math.abs(halfMax - lumint.getR());
			if (currentValue <= closestValue) {
				closestValue = currentValue;
				theta = lumint.getTheta();
			}
		}
		return theta;
	}
}
