// Copyright (c) 2011, 2022  Deschutes Signal Processing LLC

// Author:  David B. Harris

//  This file is part of OregonDSP.
//
//    OregonDSP is free software: you can redistribute it and/or modify
//    it under the terms of the GNU Lesser General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    OregonDSP is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public License
//    along with OregonDSP.  If not, see <http://www.gnu.org/licenses/>.

package com.oregondsp.signalProcessing.filter.iir;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;


/**
 * Class to implement digital Butterworth filters.
 * 
 * @author David B. Harris,  Deschutes Signal Processing LLC
 */
public class Butterworth extends IIRFilter {
  
  
  /**
   * Instantiates a new Butterworth digital filter.
   *
   * @param order      int specifying the order (number of poles) of the filter.
   * @param type       PassbandType specifying whether the filter is a lowpass, bandpass or highpass filter.
   * @param f1         double specifying the low cutoff frequency (must always be present, but used only for 
   *                   bandpass and highpass filters).
   * @param f2         double specifying the high cutoff frequency (must always be present, but used only for
   *                   bandpass and lowpass filters).
   * @param delta      double specifying the sampling interval of the data to which this filter will be applied.
   */
  public Butterworth( int order, PassbandType type, double f1, double f2, double delta ) {
    
    super( new AnalogButterworth( order ), type, f1, f2, delta );
      
  }

  
  
  public static void main( String[] args ) {
	  
	Butterworth B = new Butterworth( 3, PassbandType.BANDPASS, 2.0, 5.0, 0.025 );
	B.print( System.out );
	float[] tmp = new float[201];
	for ( int i = 0;  i < 201;  i++ ) {
		Complex C = B.evaluate( Math.PI/200.0*i );
		tmp[i] = (float) Complex.abs( C );
	}
	
	float[] x = new float[ 1001 ];
	x[200] = 1.0f;
	float[] y = new float[ 1001 ];
	B.filter( x, y );
	
	PrintStream ps;
	try {
		ps = new PrintStream( new FileOutputStream( "C:\\DATA\\Test\\Response.m" ) );
		ps.print( "R = [ " );
		for ( int i = 0;  i < 200;  i++ ) {
			ps.println( tmp[i] + "; ..." );
		}
		ps.println( tmp[200] + "];" );
		ps.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	
	try {
		ps = new PrintStream( new FileOutputStream( "C:\\DATA\\Test\\ImpulseResponse.m" ) );
		ps.print( "IR = [ " );
		for ( int i = 0;  i < 1000;  i++ ) {
			ps.println( y[i] + "; ..." );
		}
		ps.println( y[1000] + "];" );
		ps.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}

  }
  
}
