//
// Gridded1DSet.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1999 Bill Hibbard, Curtis Rueden, Tom
Rink, Dave Glowacki, Steve Emmerson, Tom Whittaker, Don Murray, and
Tommy Jasmin.
 
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this library; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
MA 02111-1307, USA
*/

package visad;

import java.io.*;

/**
   Gridded1DSet represents a finite set of samples of R.<P>
*/
public class Gridded1DSet extends GriddedSet {

  int LengthX;
  float LowX, HiX;

  /** Whether this set is ascending or descending */
  boolean Ascending;

  /** a 1-D sequence with no regular interval with null errors,
      CoordinateSystem and Units are defaults from type */
  public Gridded1DSet(MathType type, float[][] samples, int lengthX)
         throws VisADException {
    this(type, samples, lengthX, null, null, null);
  }

  public Gridded1DSet(MathType type, float[][] samples, int lengthX,
                      CoordinateSystem coord_sys, Unit[] units,
                      ErrorEstimate[] errors) throws VisADException {
    this(type, samples, lengthX, coord_sys, units, errors, true);
  }

  /** a 1-D sorted sequence with no regular interval; samples array
      is organized float[1][number_of_samples] where lengthX =
      number_of_samples; samples must be sorted (either increasing
      or decreasing); coordinate_system and units must be compatible
      with defaults for type, or may be null; errors may be null */
  Gridded1DSet(MathType type, float[][] samples, int lengthX,
               CoordinateSystem coord_sys, Unit[] units,
               ErrorEstimate[] errors, boolean copy)
               throws VisADException {
    super(type, samples, make_lengths(lengthX), coord_sys, units,
          errors, copy);
    LowX = Low[0];
    HiX = Hi[0];
    LengthX = Lengths[0];

    if (Samples != null && Lengths[0] > 1) {
      // samples consistency test
      for (int i=0; i<Length; i++) {
        if (Samples[0][i] != Samples[0][i]) {
          throw new SetException(
           "Gridded1DSet: samples values may not be missing");
        }
      }
      Ascending = (Samples[0][LengthX-1] > Samples[0][0]);
      if (Ascending) {
        for (int i=1; i<LengthX; i++) {
          if (Samples[0][i] < Samples[0][i-1]) {
            throw new SetException(
             "Gridded1DSet: samples do not form a valid grid ("+i+")");
          }
        }
      }
      else { // !Pos
        for (int i=1; i<LengthX; i++) {
          if (Samples[0][i] > Samples[0][i-1]) {
            throw new SetException(
             "Gridded1DSet: samples do not form a valid grid ("+i+")");
          }
        }
      }
    }
  }

  static int[] make_lengths(int lengthX) {
    int[] lens = new int[1];
    lens[0] = lengthX;
    return lens;
  }

  /** convert an array of 1-D indices to an array of values in R^DomainDimension */
  public float[][] indexToValue(int[] index) throws VisADException {
    int length = index.length;
    if (Samples == null) {
      // not used - over-ridden by Linear1DSet.indexToValue
      float[][] grid = new float[ManifoldDimension][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          grid[0][i] = (float) index[i];
        }
        else {
          grid[0][i] = -1;
        }
      }
      return gridToValue(grid);
    }
    else {
      float[][] values = new float[1][length];
      for (int i=0; i<length; i++) {
        if (0 <= index[i] && index[i] < Length) {
          values[0][i] = Samples[0][index[i]];
        }
        else {
          values[0][i] = Float.NaN;
        }
      }
      return values;
    }
  }

  /**
   * Convert an array of values in R^DomainDimension to an array of
   * 1-D indices.  This Gridded1DSet must have at least two points in the
   * set.
   * @param value	An array of coordinates.  <code>value[i][j]
   *			</code> contains the <code>i</code>th component of the
   *			<code>j</code>th point.
   * @return		Indices of nearest points.  RETURN_VALUE<code>[i]</code>
   *			will contain the index of the point in the set closest
   *			to <code>value[][i]</code> or <code>-1</code> if 
   *			<code>value[][i]</code> lies outside the set.
   */
  public int[] valueToIndex(float[][] value) throws VisADException {
    if (value.length != DomainDimension) {
      throw new SetException("Gridded1DSet.valueToIndex: bad dimension");
    }
    int length = value[0].length;
    int[] index = new int[length];

    float[][] grid = valueToGrid(value);
    float[] grid0 = grid[0];
    float g;
    for (int i=0; i<length; i++) {
      g = grid0[i];
      index[i] = Float.isNaN(g) ? -1 : ((int) (g + 0.5));
    }
    return index;
  }

  /** transform an array of non-integer grid coordinates to an array
      of values in R^DomainDimension */
  public float[][] gridToValue(float[][] grid) throws VisADException {
    if (grid.length < DomainDimension) {
      throw new SetException("Gridded1DSet.gridToValue: bad dimension");
    }
    if (Lengths[0] < 2) {
      throw new SetException("Gridded1DSet.gridToValue: requires all grid " +
                             "dimensions to be > 1");
    }
    int length = grid[0].length;
    float[][] value = new float[1][length];
    for (int i=0; i<length; i++) {
      // let g be the current grid coordinate
      float g = grid[0][i];
      if ( (g < -0.5) || (g > LengthX-0.5) ) {
        value[0][i] = Float.NaN;
        continue;
      }
      // calculate closest integer variable
      int ig;
      if (g < 0) ig = 0;
      else if (g >= LengthX-1) ig = LengthX - 2;
      else ig = (int) g;
      float A = g - ig;  // distance variable
      // do the linear interpolation
      value[0][i] = (1-A)*Samples[0][ig] + A*Samples[0][ig+1];
    }
    return value;
  }

  /** transform an array of values in R^DomainDimension to an array
      of non-integer grid coordinates */
  public float[][] valueToGrid(float[][] value) throws VisADException {
    if (value.length < DomainDimension) {
      throw new SetException("Gridded1DSet.valueToGrid: bad dimension");
    }
    if (Lengths[0] < 2) {
      throw new SetException("Gridded1DSet.valueToGrid: requires all grid " +
                             "dimensions to be > 1");
    }
    float[] vals = value[0];
    int length = vals.length;
    float[] samps = Samples[0];
    float[][] grid = new float[1][length];
    int ig = (LengthX - 1)/2;
    for (int i=0; i<length; i++) {
      if (Float.isNaN(vals[i])) grid[0][i] = Float.NaN;
      else {
	int lower = 0;
	int upper = LengthX-1;
	while (lower < upper) {
	  if ((vals[i] - samps[ig]) * (vals[i] - samps[ig+1]) <= 0) break;
	  if (Ascending ? samps[ig+1] < vals[i] : samps[ig+1] > vals[i]) {
	    lower = ig+1;
          }
	  else if (Ascending ? samps[ig] > vals[i] : samps[ig] < vals[i]) {
	    upper = ig;
          }
	  if (lower < upper) ig = (lower + upper) / 2;
	}
        // Newton's method
	float solv = ig + (vals[i] - samps[ig]) / (samps[ig+1] - samps[ig]);
        if (solv >= -0.5 && solv <= LengthX - 0.5) grid[0][i] = solv;
        else {
          grid[0][i] = Float.NaN;
          // next guess should be in the middle if previous value was missing
          ig = (LengthX - 1)/2;
        }
      }
    }
    return grid;
  }

  public float getLowX() {
    return LowX;
  }

  public float getHiX() {
    return HiX;
  }

  public Object clone() {
    try {
      return new Gridded1DSet(Type, Samples, Length, DomainCoordinateSystem,
                              SetUnits, SetErrors);
    }
    catch (VisADException e) {
      throw new VisADError("Gridded1DSet.clone: " + e.toString());
    }
  }

  public Object cloneButType(MathType type) throws VisADException {
    return new Gridded1DSet(type, Samples, Length, DomainCoordinateSystem,
                             SetUnits, SetErrors);
  }

  /* run 'java visad.Gridded1DSet < formatted_input_stream'
     to test the Gridded1DSet class */
  public static void main(String[] args) throws VisADException {

    // Define input stream
    InputStreamReader inStr = new InputStreamReader(System.in);

    // Define temporary integer array
    int[] ints = new int[80];
    try {
      ints[0] = inStr.read();
    }
    catch(Exception e) {
      System.out.println("Gridded1DSet: "+e);
    }
    int l = 0;
    while (ints[l] != 10) {
      try {
        ints[++l] = inStr.read();
      }
      catch (Exception e) {
        System.out.println("Gridded1DSet: "+e);
      }
    }
    // convert array of integers to array of characters
    char[] chars = new char[l];
    for (int i=0; i<l; i++) {
      chars[i] = (char) ints[i];
    }
    int num_coords = Integer.parseInt(new String(chars));

    // Define size of Samples array
    float[][] samp = new float[1][num_coords];
    System.out.println("num_dimensions = 1, num_coords = "+num_coords+"\n");

    // Skip blank line
    try {
      ints[0] = inStr.read();
    }
    catch (Exception e) {
      System.out.println("Gridded1DSet: "+e);
    }

    for (int c=0; c<num_coords; c++) {
      l = 0;
      try {
        ints[0] = inStr.read();
      }
      catch (Exception e) {
        System.out.println("Gridded1DSet: "+e);
      }
      while (ints[l] != 32) {
        try {
          ints[++l] = inStr.read();
        }
        catch (Exception e) {
          System.out.println("Gridded1DSet: "+e);
        }
      }
      chars = new char[l];
      for (int i=0; i<l; i++) {
        chars[i] = (char) ints[i];
      }
      samp[0][c] = (Float.valueOf(new String(chars))).floatValue();
    }

    // do EOF stuff
    try {
      inStr.close();
    }
    catch (Exception e) {
      System.out.println("Gridded1DSet: "+e);
    }

    // Set up instance of Gridded1DSet
    RealType vis_data = new RealType("vis_data", null, null);
    RealType[] vis_array = {vis_data};
    RealTupleType vis_tuple = new RealTupleType(vis_array);
    Gridded1DSet gSet1D = new Gridded1DSet(vis_tuple, samp, num_coords);

    System.out.println("Lengths = " + num_coords + " wedge = ");
    int[] wedge = gSet1D.getWedge();
    for (int i=0; i<wedge.length; i++) System.out.println(" " + wedge[i]);

    // Print out Samples information
    System.out.println("Samples ("+gSet1D.LengthX+"):");
    for (int i=0; i<gSet1D.LengthX; i++) {
      System.out.println("#"+i+":\t"+gSet1D.Samples[0][i]);
    }

    // Test gridToValue function
    System.out.println("\ngridToValue test:");
    int myLength = gSet1D.LengthX+1;
    float[][] myGrid = new float[1][myLength];
    for (int i=0; i<myLength; i++) {
      myGrid[0][i] = i-0.5f;
    }
    myGrid[0][0] += 0.1;          // don't let grid values get too
    myGrid[0][myLength-1] -= 0.1; // close to interpolation limits
    float[][] myValue = gSet1D.gridToValue(myGrid);
    for (int i=0; i<myLength; i++) {
        System.out.println("("+((float) Math.round(1000000
                                        *myGrid[0][i]) /1000000)+")\t-->  "
                              +((float) Math.round(1000000
                                        *myValue[0][i]) /1000000));
    }

    // Test valueToGrid function
    System.out.println("\nvalueToGrid test:");
    float[][] gridTwo = gSet1D.valueToGrid(myValue);
    for (int i=0; i<gridTwo[0].length; i++) {
      System.out.println(((float) Math.round(1000000
                                  *myValue[0][i]) /1000000)+"  \t-->  ("
                        +((float) Math.round(1000000
                                  *gridTwo[0][i]) /1000000)+")");
    }
    System.out.println();

  }

/* Here's the output with sample file Gridded1D.txt:

iris 25% java visad.Gridded1DSet < Gridded1D.txt
num_dimensions = 1, num_coords = 20
 
Lengths = 20 wedge = 
 0
 1
. . .
 18
 19
Samples (20):
#0:     -40.54849
#1:     -39.462048
. . .
#18:    26.026154
#19:    38.3012

gridToValue test:
(-0.4)  -->  -40.983063
(0.5)   -->  -40.00527
. . .
(18.5)  -->  32.163677
(19.4)  -->  43.211212

valueToGrid test:
-40.983063      -->  (-0.399998)
-40.00527       -->  (0.5)
. . .
32.163677       -->  (18.5)
43.211212       -->  (19.4)
 
iris 26% 

*/

}

