package com.oregondsp.signalProcessing.test;

import java.util.Random;

import com.oregondsp.signalProcessing.fft.RDFTdp;

public class TestFFT {
	
	public static void main( String[] args ) {
		
		
		// log2n    0  1  2  3   4   5   6    7    8    9    10    11    12    13     14
		int[] n = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384 };
		
		for ( int log2n = 8;  log2n <= 14;  log2n++ ) {
			
			int N = n[ log2n ];
			
			double[] x = new double[ N ];
			double[] X = new double[ N ];
			
			Random R = new Random();
			
			for ( int i = 0;  i < N;  i++ ) x[i] = R.nextGaussian();
			
			RDFTdp rdft = new RDFTdp( log2n );
			
			// burn-in
			
			for ( int i = 0;  i < 10001;  i++ ) {
				rdft.evaluate( x, X );
				rdft.evaluateInverse( X, x );
			}
			
			// timed
			
			long t0 = System.currentTimeMillis();
			
			for ( int i = 0;  i < 100000;  i++ ) {
				rdft.evaluate( x, X );
				rdft.evaluateInverse( X, x );
			}		
			
			long t1 = System.currentTimeMillis();
			
			System.out.println( N + "    " + ((t1-t0)/100000.0) );
			
			
		}
		
		
		
		
	}

}
