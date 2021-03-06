// Copyright (c) 2011-2013  Deschutes Signal Processing LLC
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

package com.oregondsp.signalProcessing.fft;


/**
 * Package-private class implementing a length-8 complex DFT with a split-radix algorithm.  Double precision version.
 * 
 * @author David B. Harris,   Deschutes Signal Processing LLC
 */
class CDFTsr8dp extends CDFTsrdp {

  /** Constant twiddle factor. */
  static final double SQRT2BY2 = Math.sqrt(2.0) / 2.0;

  /** Input sequence array indices. */
  private int n0, n1, n2, n3, n4, n5, n6, n7;
  
  /** Output transform array indices. */
  private int m0, m1, m2, m3, m4, m5, m6, m7;
  

  /**
   * Instantiates a new CDFTsr8dp.
   *
   * @param xoffset  int specifying offset into the top-level length-N sequence array.
   * @param xstride  int specifying the stride of butterflies into the top-level length-N sequence array.
   * @param Xoffset  int specifying the offset into the length-N transform array.
   */
  CDFTsr8dp( int xoffset, int xstride, int Xoffset ) {

     m = 3;
     N = 8;
     this.xoffset = xoffset;
     this.xstride = xstride;
     this.Xoffset = Xoffset;
     
     n0 = xoffset;
     n1 = n0 + xstride;
     n2 = n1 + xstride;
     n3 = n2 + xstride;
     n4 = n3 + xstride;
     n5 = n4 + xstride;
     n6 = n5 + xstride;
     n7 = n6 + xstride;

     m0 = Xoffset;
     m1 = m0 + 1;
     m2 = m1 + 1;
     m3 = m2 + 1;
     m4 = m3 + 1;
     m5 = m4 + 1;
     m6 = m5 + 1;
     m7 = m6 + 1;

  }
  
  
  
  /**
   * Links the user-supplied input sequence and output transform arrays.
   * 
   * @param xr  double[] containing the input sequence real part.
   * @param xi  double[] containing the input sequence imaginary part.
   * @param Xr  double[] containing the output sequence real part.
   * @param Xi  double[] containing the output sequence imaginary part.
   */
  void link( double[] xr, double[] xi, double[] Xr, double[] Xi ) {
    this.xr = xr;
    this.xi = xi;
    this.Xr = Xr;
    this.Xi = Xi;
  }



  /**
   * Evaluates the length-8 complex DFT.
   */
  void evaluate() {
     
    double T1r, T1i, T3r, T3i; 
    double Rr, Ri, Sr, Si;

// Length 2 DFT

    Xr[m0] = xr[n0] + xr[n4];
    Xi[m0] = xi[n0] + xi[n4];
    Xr[m1] = xr[n0] - xr[n4];
    Xi[m1] = xi[n0] - xi[n4];

  // length 4 dft

  // k = 0 butterfly

    Rr = xr[n2]  + xr[n6];
    Ri = xi[n2]  + xi[n6];
    Sr = xi[n6]  - xi[n2];
    Si = xr[n2]  - xr[n6];
  
    Xr[m2] = Xr[m0] - Rr;
    Xi[m2] = Xi[m0] - Ri;
    Xr[m3] = Xr[m1] + Sr;
    Xi[m3] = Xi[m1] + Si;
  
    Xr[m0] += Rr;
    Xi[m0] += Ri;
    Xr[m1] -= Sr;
    Xi[m1] -= Si;

// Length 2 DFT

    Xr[m4] = xr[n1] + xr[n5];
    Xi[m4] = xi[n1] + xi[n5];
    Xr[m5] = xr[n1] - xr[n5];
    Xi[m5] = xi[n1] - xi[n5];

// Length 2 DFT

    Xr[m6] = xr[n3] + xr[n7];
    Xi[m6] = xi[n3] + xi[n7];
    Xr[m7] = xr[n3] - xr[n7];
    Xi[m7] = xi[n3] - xi[n7];



  // length 8 dft


  // k = 0 butterfly

    Rr = Xr[m4]  + Xr[m6];
    Ri = Xi[m4]  + Xi[m6];
    Sr = Xi[m6]  - Xi[m4];
    Si = Xr[m4]  - Xr[m6];
  
    Xr[m4] = Xr[m0] - Rr;
    Xi[m4] = Xi[m0] - Ri;
    Xr[m6] = Xr[m2] + Sr;
    Xi[m6] = Xi[m2] + Si;
  
    Xr[m0] += Rr;
    Xi[m0] += Ri;
    Xr[m2] -= Sr;
    Xi[m2] -= Si;


  // k = 1 butterfly

  // T1 = Wk*O1
  // T3 = W3k*O3

    T1r =  SQRT2BY2 * ( Xr[m5]+ Xi[m5] );
    T1i =  SQRT2BY2 * ( Xi[m5]- Xr[m5] );
    T3r =  SQRT2BY2 * ( Xi[m7] - Xr[m7] );
    T3i = -SQRT2BY2 * ( Xi[m7] + Xr[m7] );

  // R = T1 + T3
  // S = i*(T1 - T3)

    Rr = T1r + T3r;
    Ri = T1i + T3i;
    Sr = T3i - T1i;
    Si = T1r - T3r;

    Xr[m5] = Xr[m1] - Rr;
    Xi[m5] = Xi[m1] - Ri;
    Xr[m7] = Xr[m3] + Sr;
    Xi[m7] = Xi[m3] + Si;

    Xr[m1] += Rr;
    Xi[m1] += Ri;
    Xr[m3] -= Sr;
    Xi[m3] -= Si;

  }

}
