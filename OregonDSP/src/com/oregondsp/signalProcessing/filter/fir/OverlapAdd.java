// Copyright (c) 2011  Deschutes Signal Processing LLC
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


package com.oregondsp.signalProcessing.filter.fir;


import java.util.Arrays;

import com.oregondsp.signalProcessing.Sequence;
import com.oregondsp.signalProcessing.fft.RDFT;



/**
 * Implements a finite impulse response (FIR) filter using the overlap-add algorithm.
 * 
 * <p>The overlap add algorithm (see Oppenheim and Schafer, Digital Signal Processing, 1975) enables 
 * FIR filters to operate on sequences of arbitrary length when the sequences are available in a series 
 * of uniform consecutive, contiguous blocks.  The output is produced incrementally with the filter()
 * method, filtering one block at a time.  The algorithm is capable of filtering data from continuous,
 * real-time streams or streams from very large archive files.  However, it is suitable for filtering
 * data segments of any length (including short, extracted event segments).</p>
 * 
 * <p>The size of the sequential data blocks must be uniform (i.e. the same from one invocation of
 * filter() to the next).  The block size is specified in the first constructor, which uses it and the 
 * filter kernel size to calculate the size of the forward and inverse FFT algorithms required to
 * implement the convolution operation.</p>
 * 
 * <p>The DFT object may be large if the chosen processing block size is large.  If more than one OverlapAdd
 * instance is to be used in an application, it may be worthwhile to share fft resources among instances, 
 * which is possible provided the kernel sizes of the FIR filters are identical.  For this case, a second 
 * constructor is provided that constructs a "slave" instance containing a reference to the DFT used in 
 * another "master" OverlapAdd instance.  Slave instances use the DFT resources of their associated master
 * instances.  Care should be taken to keep master and slave instances in the same thread.</p>
 * 
 * <p>The OverlapAdd class keeps state information from one invocation of filter() to the next on
 * consecutive blocks of the data stream.  This state information allows the algorithm to perform
 * continuous convolutions on streams of arbitrary length, including real-time streams.  If and when
 * the end of the stream is reached, the remaining state information may be dumped using the flush() 
 * method. </p> 
 * 
 * <p>An example of the application of the OverlapAdd class is available in the Interpolator class.</p>
 * 
 *  @author David B. Harris,   Deschutes Signal Processing LLC
 */
public class OverlapAdd {
  
  private float[]  shiftRegister;
  private RDFT     fft;
  private int      nfft;
  private float[]  kernel;
  private int      kernelLength;
  private int      blockSize;
  
  private float[]  segment;
  private float[]  transform;
  


  /** Constructor for master OverlapAdd instance - this one has the fft instances.
   * @param H          float[] containing convolutional kernel
   * @param blockSize  int specifying size of data blocks to be filtered
   */
  public OverlapAdd( float[] H, int blockSize ) {
    
    kernelLength = H.length;
    
    this.blockSize = blockSize;
    
    // compute fft size
    
    int clength = H.length + blockSize - 1;
    int log2nfft = 0;
    nfft = 1;
    while ( nfft < clength ) {
      log2nfft++;
      nfft *= 2;
    }
    fft  = new RDFT( log2nfft );
   
    shiftRegister = new float[ nfft ];
    kernel        = new float[ nfft ];
    segment       = new float[ nfft ];
    transform     = new float[ nfft ];
    
    System.arraycopy( H, 0, segment, 0, H.length );
    fft.evaluate( segment, kernel );
  }
  
  
  
  /** Constructor for slave OverlapAdd instance - this one uses the fft instance contained in the master
   * @param H          Float array containing kernel
   * @param master     Master OverlapAdd instance - slave obtains fft instances from the master
   */
  public OverlapAdd( float[] H, OverlapAdd master ) {
    
    if ( H.length != master.kernelLength ) 
      throw new IllegalArgumentException( "Slave kernel length inconsistent with master OverlapAdd kernel length" );
    
    kernelLength = H.length;
    
    this.blockSize = master.blockSize;
    fft            = master.fft;
    nfft           = master.nfft;
    
    shiftRegister  = new float[ nfft ];
    kernel         = new float[ nfft ];
    segment        = new float[ nfft ];
    transform      = new float[ nfft ];
    
    System.arraycopy( H, 0, segment, 0, H.length );
    fft.evaluate( segment, kernel );
  }
  
  
  
  /** Filtering operation to produce an incremental convolution result from one block of data
   * @param src    float[] array containing data block
   * @param sptr   int specifying point within data array to begin block (usually 0).
   *               Array length must be at least sptr + blocksize.
   * @param dst    float[] containing increment of convolution result - array length must be at
   *                  least dptr + blockSize
   * @param dptr   Point within destination array where convolution result starts
   */
  public void filter( float[] src, int sptr, float[] dst, int dptr ) {

    if ( src.length < sptr + blockSize )
      throw new IllegalArgumentException( "Source array length less than sptr + blockSize" );

    if ( dst.length < dptr + blockSize )
        throw new IllegalArgumentException( "Destination array length less than dptr + blockSize" );
    
    // copy data
    
    Arrays.fill( segment, 0.0f );
    System.arraycopy( src, sptr, segment, 0, blockSize );
    
    // circular convolution by dft
    
    fft.evaluate( segment, transform );
    RDFT.dftProduct( kernel, transform, 1.0f );
    fft.evaluateInverse( transform, segment );
    
    // overlap add
    
    for ( int i = 0;  i < nfft;  i++ ) {
      shiftRegister[i] += segment[i];
    }
    
    // save incremental result
    
    System.arraycopy( shiftRegister, 0, dst, dptr, blockSize );
    
    // shift state information
    
    Sequence.zeroShift( shiftRegister, -blockSize );
  }
  
  
  
  /** Flushes state information buffer - i.e. left over convolution results when no further data blocks are available
   * @param dst       float[] where convolution results are returned.  Length of dst must be >= dptr + blockSize.
   * @param dptr      int specifying point in dst where convolution results begin.
   */
  public void flush( float[] dst, int dptr ) {
    
   // save incremental result
    
    System.arraycopy( shiftRegister, 0, dst, dptr, blockSize );
    
    // shift state information
    
    Sequence.zeroShift( shiftRegister, -blockSize );
    
  }
  
}
