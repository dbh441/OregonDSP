package com.oregondsp.signalProcessing.filter.fir;

//Copyright (c) 2011, 2022  Deschutes Signal Processing LLC

//Author:  David B. Harris

//This file is part of OregonDSP.
//
//OregonDSP is free software: you can redistribute it and/or modify
//it under the terms of the GNU Lesser General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//OregonDSP is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public License
//along with OregonDSP.  If not, see <http://www.gnu.org/licenses/>.

import java.util.Arrays;

import com.oregondsp.signalProcessing.Sequence;
import com.oregondsp.signalProcessing.fft.RDFTdp;

public class OverlapAdd_dp {
  
  private double[]  shiftRegister;
  private RDFTdp    fft;
  private int       nfft;
  private double[]  hxfm;
  private double[]  tmp;
  private int       blockSize;
  private int       kernelLength;
  
  private double[]  segment;
  


  /** Constructor for OverlapAdd_dp instance.
   * @param h          double[] containing convolutional kernel
   * @param blockSize  int specifying size of data blocks to be filtered
   */
  public OverlapAdd_dp( double[] h, int blockSize, RDFTdp fft  ) {
    
    this.blockSize = blockSize;
    this.fft       = fft;
    nfft           = fft.getFFTSize();
   
    shiftRegister = new double[ nfft ];
    hxfm          = new double[ nfft ];
    segment       = new double[ nfft ];
    tmp           = new double[ nfft ];
    
    kernelLength  = h.length;
    
    System.arraycopy( h, 0, tmp, 0, kernelLength );
    fft.evaluate( tmp, hxfm );
    
  }

  
  
  /** Filtering operation to produce an incremental convolution result from one block of data
   * @param transform double[] array containing transform of data block
   * @param dst       double[] containing increment of convolution result - array length must be at 
   *                    least dptr + blockSize
   * @param dptr      Point within destination array where convolution result starts
   */
  public void filter( double[] transform, double[] dst, int dptr ) {
    
    System.arraycopy( transform, 0, tmp, 0, transform.length );
    
    // product of kernel transform and data transform

    RDFTdp.dftProduct( hxfm, tmp, 1.0f );
    fft.evaluateInverse( tmp, segment );
    
    // overlap add
    
    for ( int i = 0;  i < nfft;  i++ ) {
      shiftRegister[i] += segment[i];
    }
    
    // save incremental result
    
    System.arraycopy( shiftRegister, 0, dst, dptr, blockSize );
    
    // shift state information
    
    Sequence.zeroShift( shiftRegister, -blockSize );
  }
  
  
  
  /** Initialization - initializes filter state to zero, but does not alter the kernel
   * 
   */
  public void initialize() {
    Arrays.fill( shiftRegister, 0.0f );
  }
  
  
  
  /** Flushes state information buffer - i.e. left over convolution results when no further data blocks are available
   * @param dst       double[] where convolution results are returned.  Length of dst must be >= dptr + blockSize.
   * @param dptr      int specifying point in dst where convolution results begin.
   */
  public void flush( double[] dst, int dptr ) {
    
   // save incremental result
    
    System.arraycopy( shiftRegister, 0, dst, dptr, blockSize );
    
    // shift state information
    
    Sequence.zeroShift( shiftRegister, -blockSize );
    
  }
  
  
  
  public int getKernelLength() { return kernelLength; }
  
}
