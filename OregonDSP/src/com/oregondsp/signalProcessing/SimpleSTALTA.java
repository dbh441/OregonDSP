package com.oregondsp.signalProcessing;

//Copyright (c) 2011, 2022  Deschutes Signal Processing LLC

//Author:  David B. Harris

//This file is part of OregonDSP.
//
// OregonDSP is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// OregonDSP is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with OregonDSP.  If not, see <http://www.gnu.org/licenses/>.

/**
 * This class is provided for seismological detection applications, though could be applied to power
 * detection in any input sequence.
 * 
 * It instantiates and implements a simple algorithm for calculating an STA/LTA (short-term average to 
 * long-term average) power detection statistic.  The STA is calculated as the output of a simple two-pole 
 * filter (with two real identical poles).  The poles are chosen to provide an approximate integration 
 * time of T_STA seconds.  The LTA part of the statistic is calculated as the output of a simple three-pole 
 * filter, with three real identical poles.  These poles are chosen to provide an approximate integration
 * time of T_LTA seconds, which should be chosen to be larger that T_STA.  The class provides for a delay
 * between the calculation of the STA and LTA calculations of T_gap seconds.  For correct operation, the
 * input sequence must be squared (i.e. the filter method does not square the input datum).  In addition,
 * the user must specify an intial condition for the LTA calculation to prevent a divide by zero error
 * at the start of the calculation.
 * 
 */
public class SimpleSTALTA {

	double   a1_s;
	double   a2_s;
	double   b_s;
	double[] ys;

	double   a1_l;
	double   a2_l;
	double   a3_l;
	double   b_l;
	int      d_l;
	double[] yl;

	float[]  x;

	 /**
	   * Instantiates a new STA/LTA detection statistic calculator.
	   *
	   * @param T_STA              (double) Integration time of the short-term average in seconds.
	   * @param T_LTA              (double) Integration time of the long-term average in seconds.
	   * @param T_gap              (double) Delay between the STA and LTA calculations in seconds.
	   * @param delta              (double) Data sampling interval in seconds.
	   * @param initialCondition   (double) LTA initial value to prevent divide-by-zero.
	   */
	public SimpleSTALTA( double T_STA, double T_LTA, double T_gap, double delta, double initialCondition ) {

		int delay = (int) Math.round( T_gap / delta );
		d_l       = delay;
		x         = new float[ delay + 1 ];
	
		ys    = new double[3];
		yl    = new double[4];
		yl[0] = initialCondition;
		yl[1] = initialCondition;
		yl[2] = initialCondition;
		yl[3] = initialCondition;

		double a = Math.exp(-delta / T_STA);
		a1_s     = 2.0 * a;
		a2_s     = -a * a;
		b_s      = (1 - a) * (1 - a);

		a    = Math.exp(-delta / T_LTA);
		a1_l = 3.0 * a;
		a2_l = -3.0 * a * a;
		a3_l = a * a * a;
		b_l  = (1 - a) * (1 - a) * (1 - a);

	}
 
 
	 /**
	   * Method implementing a time step.  Takes a single input datum (assumed to be a squared value
	   * from an input sequence and returns an STA/LTA ratio.  This method is designed to be applied
	   * to consecutive, contiguous values of a power sequence (squared input sequence).
	   *
	   * @param datum    (float) Current squared value of the input sequence.
	   */	
	public float filter( float datum ) {

		x[0]  = datum;

		// sta computation

		ys[0] = a1_s * ys[1] + a2_s * ys[2] + b_s * x[0];

		// lta computation

		yl[0] = a1_l * yl[1] + a2_l * yl[2] + a3_l * yl[3] + b_l * x[d_l];

		// shift input data

		for (int i = d_l; i > 0; i--) {
			x[i] = x[i - 1];
		}

		// shift state

		ys[2] = ys[1];
		ys[1] = ys[0];
   
		yl[3] = yl[2];
		yl[2] = yl[1];
		yl[1] = yl[0];

		if (yl[0] < ys[0] * 1.0e-9) {
			return 0.0f;
		} else {
			return (float) (ys[0] / yl[0]);
		}

	}

}
