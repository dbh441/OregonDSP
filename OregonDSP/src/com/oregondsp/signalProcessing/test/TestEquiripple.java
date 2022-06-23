package com.oregondsp.signalProcessing.test;

import com.oregondsp.signalProcessing.filter.fir.OverlapAdd;
import com.oregondsp.signalProcessing.filter.fir.equiripple.CenteredDifferentiator;
import com.oregondsp.signalProcessing.filter.fir.equiripple.CenteredHilbertTransform;

public class TestEquiripple {
	
	public static void main( String[] args ) {
		
		int order = 100;
		
		CenteredHilbertTransform ch = new CenteredHilbertTransform( order, 0.025, 0.975 );
		
		System.out.println( "\n\n\n" );
		
		float[] c = ch.getCoefficients();
		for ( int i = 0;  i < c.length;  i++ ) {
			System.out.println( c[i] );
		}
		
		System.out.println( "\n\n\n" );
		
		float[] x = new float[ 2000 ];
		x[1000] = 1.0f;
		
	    int N = x.length;
	    int M = N + 2*order;
	    
	    int blocksize = 1500;
	    
	    int nblocks = 2;
	    
	    float[] tmp = new float[ nblocks*blocksize ];
	    System.arraycopy( x,  0,  tmp,  0,  N );

	    OverlapAdd F = ch.getImplementation( blocksize );
	    
	    int ptr = 0;
	    F.filter( tmp,  ptr,  tmp,  ptr );
	    ptr += blocksize;
	    F.filter( tmp, ptr, tmp, ptr );
	    
	    float dt = 0.01f;
	    
	    for ( int i = 0;  i < tmp.length;  i++ ) tmp[i] /= dt;
	    
	    System.arraycopy( tmp, order, x, 0, N );
	    
	    for ( int i = 900;  i <= 1100;  i++ )
	    	System.out.println( i + "  " +  x[i] );
				
	}

}
