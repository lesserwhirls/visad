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

import java.awt.Component;

import java.rmi.RemoteException;

import visad.*;

import visad.util.LabeledColorWidget;
import visad.java3d.DisplayImplJ3D;

public class Test33
	extends UISkeleton
{
  public Test33() { }

  public Test33(String args[])
	throws VisADException, RemoteException
  {
    super(args);
  }

  DisplayImpl[] setupData()
	throws VisADException, RemoteException
  {
    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);
    RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);

    int size = 32;
    FlatField imaget1 = FlatField.makeField(image_tuple, size, false);

    DisplayImpl display1;
    display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
    display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
    display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
    display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));

    ScalarMap color1map = new ScalarMap(ir_radiance, Display.RGB);
    display1.addMap(color1map);

    DataReferenceImpl ref_imaget1 = new DataReferenceImpl("ref_imaget1");
    ref_imaget1.setData(imaget1);
    display1.addReference(ref_imaget1, null);

    DisplayImpl[] dpys = new DisplayImpl[1];
    dpys[0] = display1;

    return dpys;
  }

  String getFrameTitle() { return "VisAD Color Widget"; }

  Component getSpecialComponent(DisplayImpl[] dpys)
	throws VisADException, RemoteException
  {
    ScalarMap color1map = (ScalarMap )dpys[0].getMapVector().lastElement();

    float[][] table = new float[3][256];
    for (int i=0; i<256; i++) {
      float a = ((float) i) / 255.0f;
      table[0][i] = a;
      table[1][i] = 1.0f - a;
      table[2][i] = 0.5f;
    }

    return new LabeledColorWidget(color1map, table);
  }

  public String toString() { return ": ColorWidget with non-default table"; }

  public static void main(String args[])
	throws VisADException, RemoteException
  {
    Test33 t = new Test33(args);
  }
}
