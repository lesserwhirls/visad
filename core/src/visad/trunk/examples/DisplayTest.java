
//
// DisplayTest.java
//

/*
VisAD system for interactive analysis and visualization of numerical
data.  Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
Rink and Dave Glowacki.
 
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 1, or (at your option)
any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License in file NOTICE for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

import visad.*;

import visad.java3d.DisplayImplJ3D;
import visad.java3d.DirectManipulationRendererJ3D;
import visad.java3d.TwoDDisplayRendererJ3D;

import visad.java2d.DisplayImplJ2D;
import visad.java2d.DisplayRendererJ2D;
import visad.java2d.DirectManipulationRendererJ2D;

import visad.util.*;

import java.util.Vector;
import java.util.Enumeration;
import java.rmi.*;
import java.io.*;

import java.awt.*;
import java.awt.event.*;

import javax.media.j3d.*;
// import com.sun.j3d.utils.applet.AppletFrame;

// GUI handling
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

// file format adapters
import visad.data.netcdf.Plain;
import visad.data.gif.GIFForm;
import visad.data.fits.FitsForm;

/**
    DisplayTest is the general class for testing Displays.<P>
*/
public class DisplayTest extends Object {

  static int no_self = 0;

  /** run 'java visad.java3d.DisplayImplJ3D to test list options */
  public static void main(String args[])
         throws IOException, VisADException, RemoteException {


    final RealType vis_radiance = new RealType("vis_radiance", null, null);
    RealType ir_radiance = new RealType("ir_radiance", null, null);
    RealType count = new RealType("count", null, null);

    RealType[] types = {RealType.Latitude, RealType.Longitude};
    RealTupleType earth_location = new RealTupleType(types);

    RealType[] types3d = {RealType.Latitude, RealType.Longitude, RealType.Radius};
    RealTupleType earth_location3d = new RealTupleType(types3d);

    RealType[] types2 = {vis_radiance, ir_radiance};
    RealTupleType radiance = new RealTupleType(types2);
    RealType[] types4 = {ir_radiance, vis_radiance};
    RealTupleType ecnaidar = new RealTupleType(types4);

    FunctionType image_bumble = new FunctionType(earth_location, ecnaidar);
    FunctionType image_tuple = new FunctionType(earth_location, radiance);
    FunctionType image_vis = new FunctionType(earth_location, vis_radiance);
    FunctionType image_ir = new FunctionType(earth_location, ir_radiance);

    FunctionType ir_histogram = new FunctionType(ir_radiance, count);

    FunctionType grid_tuple = new FunctionType(earth_location3d, radiance);

    RealType[] time = {RealType.Time};
    RealTupleType time_type = new RealTupleType(time);
    FunctionType time_images = new FunctionType(time_type, image_tuple);
    FunctionType time_bee = new FunctionType(time_type, image_bumble);
    RealType[] scatter_list = {vis_radiance, ir_radiance, count,
                               RealType.Latitude, RealType.Longitude,
                               RealType.Radius};
    RealTupleType scatter = new RealTupleType(scatter_list);
    FunctionType scatter_function = new FunctionType(time_type, scatter);

    FunctionType ftype;
    RealTupleType dtype;
    RealType rtype;

    int test_case = -1;
    if (args.length > 0) {
      try {
        test_case = Integer.parseInt(args[0]);
      }
      catch(NumberFormatException e) {
        test_case = -1;
      }
    }

    DisplayImpl display1;
    int size;
    FlatField imaget1;
    DataReferenceImpl ref_imaget1;

    switch(test_case) {
      default:
 
        System.out.println("to test VisAD's displays, run\n");
        System.out.println("  java DisplayTest N, where N =\n");
        System.out.println("  0: direct manipulation");
        System.out.println("  1: colored iso-surfaces from regular grids");
        System.out.println("  2: colored iso-surfaces from irregular grids");
        System.out.println("  3: Animation different time resolutions");
        System.out.println("  4: spherical coordinates");
        System.out.println("  5: colored 2-D contours from regular grids");
        System.out.println("  6: colored 2-D contours from irregular grids");
        System.out.println("  7: variable transparency");
        System.out.println("  8: offset");
        System.out.println("  9 file_name: GIF / JPEG reader");
        System.out.println("  10 file_name: netCDF adapter");
        System.out.println("  11: CoordinateSystem and Unit");
        System.out.println("  12: 2-D surface and ColorWidget");
        System.out.println("  13: Exception display");
        System.out.println("  14: collaborative direct manipulation server");
        System.out.println("        run rmiregistry first");
        System.out.println("        any number of clients may connect");
        System.out.println("  15 ip.name: collaborative direct manipulation client");
        System.out.println("        second parameter is server IP name");
        System.out.println("  16: texture mapping");
        System.out.println("  17: constant transparency");
        System.out.println("  18: Animation different time extents");
        System.out.println("  19: SelectValue");
        System.out.println("  20: 2-D surface and ColorAlphaWidget");
        System.out.println("  21: SelectRange");
        System.out.println("  22: Hue & Saturation");
        System.out.println("  23: Cyan & Magenta");
        System.out.println("  24: HSV");
        System.out.println("  25: CMY");
        System.out.println("  26: scale");
        System.out.println("  27: interactive scale");
        System.out.println("  28: flow");
        System.out.println("  29: 2-D irregular surface");
        System.out.println("  30: time stack");
        System.out.println("  31: scatter diagram");
        System.out.println("  32 file_name: FITS adapter");
        System.out.println("  33: ColorWidget with non-default table");
        System.out.println("  34: direct manipulation in Java2D");
        System.out.println("  35: direct manipulation linking Java2D and Java3D");
        System.out.println("  36: polar coordinates in Java2D");
        System.out.println("  37: colored contours from regular grids in Java2D");
        System.out.println("  38: colored contours from irregular grids in Java2D");
        System.out.println("  39: color array and ColorWidget in Java2D");
        System.out.println("  40: polar direct manipulation in Java2D");
        System.out.println("  41: image / contour alignment in Java2D");
        System.out.println("  42: image / contour alignment in Java3D");

        return;

      case 0:

        System.out.println(test_case + ": test direct manipulation");
        size = 64;
        FlatField histogram1 = FlatField.makeField(ir_histogram, size, false);
        Real direct = new Real(ir_radiance, 2.0);
        Real[] reals3 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                         new Real(vis_radiance, 1.0)};
        RealTuple direct_tuple = new RealTuple(reals3);

        display1 = new DisplayImplJ3D("display1");
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.XAxis));
        display1.addMap(new ScalarMap(count, Display.YAxis));
        display1.addMap(new ScalarMap(count, Display.Green));
    
        GraphicsModeControl mode = display1.getGraphicsModeControl();
        mode.setPointSize(5.0f);
        mode.setPointMode(false);

        DataReferenceImpl ref_direct = new DataReferenceImpl("ref_direct");
        ref_direct.setData(direct);
        DataReference[] refs1 = {ref_direct};
        display1.addReferences(new DirectManipulationRendererJ3D(), refs1, null);
     
        DataReferenceImpl ref_direct_tuple =
          new DataReferenceImpl("ref_direct_tuple");
        ref_direct_tuple.setData(direct_tuple);
        DataReference[] refs2 = {ref_direct_tuple};
        display1.addReferences(new DirectManipulationRendererJ3D(), refs2, null);
     
        DataReferenceImpl ref_histogram1 = new DataReferenceImpl("ref_histogram1");
        ref_histogram1.setData(histogram1);
        DataReference[] refs3 = {ref_histogram1};
        display1.addReferences(new DirectManipulationRendererJ3D(), refs3, null);

        DisplayImpl display2 = new DisplayImplJ3D("display2");
        display2.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display2.addMap(new ScalarMap(ir_radiance, Display.XAxis));
        display2.addMap(new ScalarMap(count, Display.YAxis));
        display2.addMap(new ScalarMap(count, Display.Green));
     
        GraphicsModeControl mode2 = display2.getGraphicsModeControl();
        mode2.setPointSize(5.0f);
        mode2.setPointMode(false);
     
        display2.addReferences(new DirectManipulationRendererJ3D(), refs1, null);
        display2.addReferences(new DirectManipulationRendererJ3D(), refs2, null);
        display2.addReferences(new DirectManipulationRendererJ3D(), refs3, null);

        JFrame jframe = new JFrame("Java3D direct manipulation");
        jframe.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        JPanel big_panel = new JPanel();
        big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
        big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        big_panel.add(display1.getComponent());
        big_panel.add(display2.getComponent());
        jframe.getContentPane().add(big_panel);
        jframe.setSize(512, 256);
        jframe.setVisible(true);

        break;

      case 1:

        System.out.println(test_case + ": test colored iso-surfaces from " +
                           "regular grids");
        int size3d = 6;
        float level = 2.5f;
        FlatField grid3d = FlatField.makeField(grid_tuple, size3d, false);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);

        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(RealType.Radius, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        ScalarMap map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
        display1.addMap(map1contour);
        ContourControl control1contour = (ContourControl) map1contour.getControl();
        control1contour.setSurfaceValue(level);
        control1contour.enableContours(true);

        DataReferenceImpl ref_grid3d = new DataReferenceImpl("ref_grid3d");
        ref_grid3d.setData(grid3d);
        display1.addReference(ref_grid3d, null);

        break;

      case 2:
 
        System.out.println(test_case + ": test colored iso-surfaces from " +
                           "irregular grids");
        size3d = 6;
        level = 2.5f;
        grid3d = FlatField.makeField(grid_tuple, size3d, true);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
 
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(RealType.Radius, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
        display1.addMap(map1contour);
        control1contour = (ContourControl) map1contour.getControl();
        control1contour.setSurfaceValue(level);
        control1contour.enableContours(true);
 
        ref_grid3d = new DataReferenceImpl("ref_grid3d");
        ref_grid3d.setData(grid3d);
        display1.addReference(ref_grid3d, null);
 
        break;

      case 3:

        System.out.println(test_case + ": test animation different " +
                           "time resolutions");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);
        FlatField wasp = FlatField.makeField(image_bumble, size, false);

        int ntimes1 = 4;
        int ntimes2 = 6;

        // different time resolution test
        Set time_set =
          new Linear1DSet(time_type, 0.0, 1.0, ntimes1);
        Set time_hornet =
          new Linear1DSet(time_type, 0.0, 1.0, ntimes2);

        FieldImpl image_sequence = new FieldImpl(time_images, time_set);
        FieldImpl image_stinger = new FieldImpl(time_bee, time_hornet);
        FlatField temp = imaget1;
        FlatField tempw = wasp;
        Real[] reals = {new Real(vis_radiance, (float) size / 4.0f),
                        new Real(ir_radiance, (float) size / 8.0f)};
        RealTuple val = new RealTuple(reals);
        for (int i=0; i<ntimes1; i++) {
          image_sequence.setSample(i, temp);
          temp = (FlatField) temp.add(val);
        }
        for (int i=0; i<ntimes2; i++) {
          image_stinger.setSample(i, tempw);
          tempw = (FlatField) tempw.add(val);
        }
        FieldImpl[] images = {image_sequence, image_stinger};
        Tuple big_tuple = new Tuple(images);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
     
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        ScalarMap map1animation = new ScalarMap(RealType.Time, Display.Animation);
        display1.addMap(map1animation);
        AnimationControl animation1control =
          (AnimationControl) map1animation.getControl();
        animation1control.setOn(true);
        animation1control.setStep(3000);

        DataReferenceImpl ref_big_tuple =
          new DataReferenceImpl("ref_big_tuple");
        ref_big_tuple.setData(big_tuple);
        display1.addReference(ref_big_tuple, null);

        break;

      case 4:

        System.out.println(test_case + ": test spherical coordinates");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.Latitude));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
        display1.addMap(new ScalarMap(vis_radiance, Display.RGB));
        // display1.addMap(new ScalarMap(vis_radiance, Display.Radius));
     
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 5:

        System.out.println(test_case + ": test colored 2-D contours from " +
                           "regular grids");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
        display1.addMap(map1contour);
        control1contour = (ContourControl) map1contour.getControl();
        control1contour.enableContours(true);
        control1contour.enableLabels(true);
     
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 6:
 
        System.out.println(test_case + ": test colored 2-D contours from " +
                           "irregular grids");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, true);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
        display1.addMap(map1contour);
        control1contour = (ContourControl) map1contour.getControl();
        control1contour.enableContours(true);
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 7:

        System.out.println(test_case + ": test variable transparency");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.Green));
        display1.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Alpha));
        // display1.addMap(new ConstantMap(0.5, Display.Alpha));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
     
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 8:
 
        System.out.println(test_case + ": test Offset");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.Green));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxisOffset));
        display1.addMap(new ScalarMap(ir_radiance, Display.ZAxisOffset));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
     
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;
 
      case 9:
 
        System.out.println(test_case + ": test GIF / JPEG reader");

        if (args.length < 2) {
          System.out.println("must specify GIF or JPEG file name");
          return;
        }
        String name = args[1];

        GIFForm gif_form = new GIFForm();
        imaget1 = (FlatField) gif_form.open(name);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);

        // compute ScalarMaps from type components
        ftype = (FunctionType) imaget1.getType();
        dtype = ftype.getDomain();
        RealTupleType rtype9 = (RealTupleType) ftype.getRange();
        display1.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                      Display.XAxis));
        display1.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                      Display.YAxis));
        display1.addMap(new ScalarMap((RealType) rtype9.getComponent(0),
                                       Display.Red));
        display1.addMap(new ScalarMap((RealType) rtype9.getComponent(1),
                                       Display.Green));
        display1.addMap(new ScalarMap((RealType) rtype9.getComponent(2),
                                       Display.Blue));

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;
 
      case 10:

        System.out.println(test_case + ": test netCDF adapter");

        if (args.length < 2) {
          System.out.println("must specify netCDF file name");
          return;
        }
        // "pmsl.nc"
        name = args[1];

        Plain plain = new Plain();
        FlatField netcdf_data = (FlatField) plain.open(name);
        // System.out.println("netcdf_data type = " + netcdf_data.getType());
        // prints: FunctionType (Real): (lon, lat) -> P_msl

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        // compute ScalarMaps from type components
        ftype = (FunctionType) netcdf_data.getType();
        dtype = ftype.getDomain();
        MathType rntype = ftype.getRange();
        int n = dtype.getDimension();
        display1.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                      Display.XAxis));
        if (n > 1) {
          display1.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                        Display.YAxis));
        }
        if (n > 2) {
          display1.addMap(new ScalarMap((RealType) dtype.getComponent(2),
                                        Display.ZAxis));
        }
        if (rntype instanceof RealType) {
          display1.addMap(new ScalarMap((RealType) rntype, Display.Green));
          if (n <= 2) {
            display1.addMap(new ScalarMap((RealType) rntype, Display.ZAxis));
          }
        }
        else if (rntype instanceof RealTupleType) {
          int m = ((RealTupleType) rntype).getDimension();
          RealType rr = (RealType) ((RealTupleType) rntype).getComponent(0);
          display1.addMap(new ScalarMap(rr, Display.Green));
          if (n <= 2) {
            if (m > 1) {
              rr = (RealType) ((RealTupleType) rntype).getComponent(1);
            }
            display1.addMap(new ScalarMap(rr, Display.ZAxis));
          }
        }
        display1.addMap(new ConstantMap(0.5, Display.Red));
        display1.addMap(new ConstantMap(0.0, Display.Blue));

        DataReferenceImpl ref_netcdf = new DataReferenceImpl("ref_netcdf");
        ref_netcdf.setData(netcdf_data);
        display1.addReference(ref_netcdf, null);

        System.out.println("now save and re-read data");
        plain.save("save.nc", netcdf_data, true);
        netcdf_data = (FlatField) plain.open("save.nc");

        break;

      case 11:
 
        System.out.println(test_case + ": test CoordinateSystem and Unit");
        RealType x = new RealType("x", null, null);
        RealType y = new RealType("y", null, null);
        Unit super_degree = CommonUnit.degree.scale(2.5);
        RealType lon = new RealType("lon", super_degree, null);
        RealType radius = new RealType("radius", null, null);
        RealTupleType cartesian = new RealTupleType(x, y);
        PolarCoordinateSystem polar_coord_sys =
          new PolarCoordinateSystem(cartesian);
        RealTupleType polar =
          new RealTupleType(lon, radius, polar_coord_sys, null);

        FunctionType image_polar = new FunctionType(polar, radiance);
        Unit[] units = {super_degree, null};
        Linear2DSet domain_set =
          new Linear2DSet(polar, 0.0, 60.0, 61, 0.0, 60.0, 61,
                          polar_coord_sys, units, null);
        imaget1 = new FlatField(image_polar, domain_set);
        FlatField.fillField(imaget1, 1.0, 30.0);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(x, Display.XAxis));
        display1.addMap(new ScalarMap(y, Display.YAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        display1.addMap(new ConstantMap(0.0, Display.Blue));

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 12:
 
        System.out.println(test_case + ": test 2-D surface and ColorWidget");

        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
 
        ScalarMap color1map = new ScalarMap(ir_radiance, Display.RGB);
        display1.addMap(color1map);

        LabeledRGBWidget lw =
          new LabeledRGBWidget(color1map, 0.0f, 32.0f);

        Frame frame = new Frame("VisAD Color Widget");
        frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        frame.add(lw);
        frame.setSize(lw.getPreferredSize());
        frame.setVisible(true);

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 13:
 
        System.out.println(test_case + ": test Exception display");
        size = 64;
        histogram1 = FlatField.makeField(ir_histogram, size, false);
        direct = new Real(ir_radiance, 2.0);
        Real[] realsx3 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                          new Real(vis_radiance, 1.0)};
        direct_tuple = new RealTuple(realsx3);
    
        // these ScalarMap should generate 3 Exceptions
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(vis_radiance, Display.XAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.RGB));
        display1.addMap(new ScalarMap(count, Display.Animation));
    
        ref_direct = new DataReferenceImpl("ref_direct");
        ref_direct.setData(direct);
        DataReference[] refsx1 = {ref_direct};
        display1.addReferences(new DirectManipulationRendererJ3D(), refsx1, null);
     
        ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
        ref_direct_tuple.setData(direct_tuple);
        DataReference[] refsx2 = {ref_direct_tuple};
        display1.addReferences(new DirectManipulationRendererJ3D(), refsx2, null);
     
        ref_histogram1 = new DataReferenceImpl("ref_histogram1");
        ref_histogram1.setData(histogram1);
        DataReference[] refsx3 = {ref_histogram1};
        display1.addReferences(new DirectManipulationRendererJ3D(), refsx3, null);

        break;

      case 14:

        System.out.println(test_case + ": collaborative visualization server");
        DataReferenceImpl[] data_refs;
        RemoteDataReferenceImpl[] rem_data_refs;
     
        try {

          size = 64;
          histogram1 = FlatField.makeField(ir_histogram, size, false); 
          direct = new Real(ir_radiance, 2.0);
          Real[] reals14 = {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                           new Real(vis_radiance, 1.0)};
          direct_tuple = new RealTuple(reals14);

          display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
          display1.addMap(new ScalarMap(vis_radiance, Display.XAxis));
          display1.addMap(new ScalarMap(ir_radiance, Display.YAxis));
          display1.addMap(new ScalarMap(count, Display.ZAxis));

          mode = display1.getGraphicsModeControl();
          mode.setPointSize(5.0f);
          mode.setPointMode(false);

          ref_direct = new DataReferenceImpl("ref_direct");
          ref_direct.setData(direct);
          DataReference[] refs141 = {ref_direct};
          display1.addReferences(new DirectManipulationRendererJ3D(), refs141, null);

          ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
          ref_direct_tuple.setData(direct_tuple);
          DataReference[] refs142 = {ref_direct_tuple};
          display1.addReferences(new DirectManipulationRendererJ3D(), refs142, null);

          ref_histogram1 = new DataReferenceImpl("ref_histogram1");
          ref_histogram1.setData(histogram1);
          DataReference[] refs143 = {ref_histogram1};
          display1.addReferences(new DirectManipulationRendererJ3D(), refs143, null);

          // create local DataReferenceImpls
          data_refs = new DataReferenceImpl[3];
          data_refs[0] = ref_histogram1;
          data_refs[1] = ref_direct;
          data_refs[2] = ref_direct_tuple;

          // create RemoteDataReferences
          rem_data_refs = new RemoteDataReferenceImpl[3];
          rem_data_refs[0] = new RemoteDataReferenceImpl(data_refs[0]);
          rem_data_refs[1] = new RemoteDataReferenceImpl(data_refs[1]);
          rem_data_refs[2] = new RemoteDataReferenceImpl(data_refs[2]);

          RemoteServerImpl obj = new RemoteServerImpl(rem_data_refs);
          Naming.rebind("//:/RemoteServerTest", obj);

          System.out.println("RemoteServer bound in registry");
        }
        catch (Exception e) {
          System.out.println("\n\nDid you run 'rmiregistry &' first?\n\n");
          System.out.println("collaboration server exception: " + e.getMessage());
          e.printStackTrace();
        }

        break;

      case 15:
 
        System.out.println(test_case + ": collaborative visualization client");
        String domain = null;
        if (args.length > 1) {
          domain = args[1];
        }
    
        try {
     
          System.out.println("RemoteClientTestImpl.main: begin remote activity");
          System.out.println("  to " + domain);
     
          if (domain == null) {
            domain = "//:/RemoteServerTest";
          }
          else {
            domain = "//" + domain + "/RemoteServerTest";
          }
          RemoteServer remote_obj = (RemoteServer) Naming.lookup(domain);
     
          System.out.println("connected");
     
          RemoteDataReference histogram_ref = remote_obj.getDataReference(0);
          RemoteDataReference direct_ref = remote_obj.getDataReference(1);
          RemoteDataReference direct_tuple_ref = remote_obj.getDataReference(2);
 
          dtype = (RealTupleType) direct_tuple_ref.getData().getType();
 
          display1 = new DisplayImplJ3D("display", DisplayImplJ3D.APPLETFRAME);
          display1.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                        Display.XAxis));
          display1.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                        Display.YAxis));
          display1.addMap(new ScalarMap((RealType) dtype.getComponent(2),
                                        Display.ZAxis));
 
          mode = display1.getGraphicsModeControl();
          mode.setPointSize(5.0f);
          mode.setPointMode(false);

          RemoteDisplayImpl remote_display1 = new RemoteDisplayImpl(display1);
          DataReference[] refs151 = {histogram_ref};
          remote_display1.addReferences(new DirectManipulationRendererJ3D(),
                                        refs151, null);

          DataReference[] refs152 = {direct_ref};
          remote_display1.addReferences(new DirectManipulationRendererJ3D(),
                                        refs152, null);

          DataReference[] refs153 = {direct_tuple_ref};
          remote_display1.addReferences(new DirectManipulationRendererJ3D(),
                                        refs153, null);
        }
        catch (Exception e) {
          System.out.println("collaboration client exception: " + e.getMessage());
          e.printStackTrace(System.out);
        }

        break;
 
      case 16:
 
        System.out.println(test_case + ": test texture mapping");
 
        size = 47;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        display1.addMap(new ConstantMap(0.5, Display.Blue));

        mode = display1.getGraphicsModeControl();
        mode.setTextureEnable(true);

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 17:
 
        System.out.println(test_case + ": test constant transparency");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.Green));
        display1.addMap(new ScalarMap(ir_radiance, Display.ZAxis));
        display1.addMap(new ConstantMap(0.5, Display.Alpha));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 18:

        System.out.println(test_case + ": test animation different time extents");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);
        wasp = FlatField.makeField(image_bumble, size, false);

        ntimes1 = 4;
        ntimes2 = 6;

        // different time extents test
        time_set =
          new Linear1DSet(time_type, 0.0, (double) (ntimes1 - 1.0), ntimes1);
        time_hornet =
          new Linear1DSet(time_type, 0.0, (double) (ntimes2 - 1.0), ntimes2);

        image_sequence = new FieldImpl(time_images, time_set);
        image_stinger = new FieldImpl(time_bee, time_hornet);
        temp = imaget1;
        tempw = wasp;
        Real[] reals18 = {new Real(vis_radiance, (float) size / 4.0f),
                          new Real(ir_radiance, (float) size / 8.0f)};
        val = new RealTuple(reals18);
        for (int i=0; i<ntimes1; i++) {
          image_sequence.setSample(i, temp);
          temp = (FlatField) temp.add(val);
        }
        for (int i=0; i<ntimes2; i++) {
          image_stinger.setSample(i, tempw);
          tempw = (FlatField) tempw.add(val);
        }
        FieldImpl[] images18 = {image_sequence, image_stinger};
        big_tuple = new Tuple(images18);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
     
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        map1animation = new ScalarMap(RealType.Time, Display.Animation);
        display1.addMap(map1animation);
        animation1control = (AnimationControl) map1animation.getControl();
        animation1control.setOn(true);
        animation1control.setStep(3000);

        ref_big_tuple = new DataReferenceImpl("ref_big_tuple");
        ref_big_tuple.setData(big_tuple);
        display1.addReference(ref_big_tuple, null);

        break;

      case 19:

        System.out.println(test_case + ": test select value");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);
        wasp = FlatField.makeField(image_bumble, size, false);

        ntimes1 = 4;
        ntimes2 = 6;
        // different time resolutions for test
        time_set =
          new Linear1DSet(time_type, 0.0, 1.0, ntimes1);
        time_hornet =
          new Linear1DSet(time_type, 0.0, 1.0, ntimes2);

        image_sequence = new FieldImpl(time_images, time_set);
        image_stinger = new FieldImpl(time_bee, time_hornet);
        temp = imaget1;
        tempw = wasp;
        Real[] reals19 = {new Real(vis_radiance, (float) size / 4.0f),
                          new Real(ir_radiance, (float) size / 8.0f)};
        val = new RealTuple(reals19);
        for (int i=0; i<ntimes1; i++) {
          image_sequence.setSample(i, temp);
          temp = (FlatField) temp.add(val);
        }
        for (int i=0; i<ntimes2; i++) {
          image_stinger.setSample(i, tempw);
          tempw = (FlatField) tempw.add(val);
        }
        FieldImpl[] images19 = {image_sequence, image_stinger};
        big_tuple = new Tuple(images19);

        final DataReference value_ref = new DataReferenceImpl("value");

        VisADSlider slider =
          new VisADSlider("value", 0, 100, 0, 0.01, value_ref, RealType.Generic);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        ScalarMap map1value = new ScalarMap(RealType.Time, Display.SelectValue);
        display1.addMap(map1value);
        final ValueControl value1control =
          (ValueControl) map1value.getControl();
        value1control.setValue(0.0);

        ref_big_tuple = new DataReferenceImpl("ref_big_tuple");
        ref_big_tuple.setData(big_tuple);
        display1.addReference(ref_big_tuple, null);

        CellImpl cell = new CellImpl() {
          public void doAction() throws VisADException, RemoteException {
            value1control.setValue(((Real) value_ref.getData()).getValue());
          }
        };
        cell.addReference(value_ref);

        frame = new Frame("VisAD select slider");
        frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        frame.add(slider);
        frame.setSize(300, 60);
        frame.setVisible(true);

        break;

      case 20:
 
        System.out.println(test_case + ": test 2-D surface and ColorAlphaWidget");
        System.out.println(" (known problems with Java3D transparency)");
 
        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
 
        color1map = new ScalarMap(ir_radiance, Display.RGBA);
        display1.addMap(color1map);
 
        LabeledRGBAWidget lwa =
          new LabeledRGBAWidget(color1map, 0.0f, 32.0f);
 
        frame = new Frame("VisAD Color Alpha Widget");
        frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        frame.add(lwa);
        frame.setSize(lwa.getPreferredSize());
        frame.setVisible(true);
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 21:

        System.out.println(test_case + ": test select range");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        final DataReference value_low_ref = new DataReferenceImpl("value_low");
        final DataReference value_hi_ref = new DataReferenceImpl("value_hi");

        VisADSlider slider_low =
          new VisADSlider("value low", 0, 64, 0, 1.0, value_low_ref,
                          RealType.Generic);
        VisADSlider slider_hi =
          new VisADSlider("value hi", 0, 64, 64, 1.0, value_hi_ref,
                          RealType.Generic);

        jframe = new JFrame("VisAD select slider");
        jframe.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });

        big_panel = new JPanel();
        big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.Y_AXIS));
        big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        big_panel.add(slider_low);
        big_panel.add(slider_hi);
        jframe.getContentPane().add(big_panel);
        jframe.setSize(300, 120);
        jframe.setVisible(true);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
 
        ScalarMap range1map = new ScalarMap(ir_radiance, Display.SelectRange);
        display1.addMap(range1map);

        mode = display1.getGraphicsModeControl();
        mode.setPointSize(2.0f);
        mode.setPointMode(false);

        final RangeControl range1control =
          (RangeControl) range1map.getControl();
        range1control.setRange(new float[] {0.0f, 100.0f});

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);


        cell = new CellImpl() {
          public void doAction() throws VisADException, RemoteException {
            range1control.setRange(new float[]
             {(float) ((Real) value_low_ref.getData()).getValue(),
              (float) ((Real) value_hi_ref.getData()).getValue()});
          }
        };
        cell.addReference(value_low_ref);
        cell.addReference(value_hi_ref);

        break;

      case 22:

        System.out.println(test_case + ": test Hue & Saturation");
        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(RealType.Latitude, Display.Saturation));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.Hue));
        display1.addMap(new ConstantMap(1.0, Display.Value));
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 23:

        System.out.println(test_case + ": test Cyan & Magenta");
        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(RealType.Latitude, Display.Cyan));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.Magenta));
        display1.addMap(new ConstantMap(0.5, Display.Yellow));
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 24:

        System.out.println(test_case + ": test HSV");
        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.HSV));
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 25:

        System.out.println(test_case + ": test CMY");
        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.CMY));
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 26:
 
        System.out.println(test_case + ": test scale");
        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        ScalarMap map1lat = new ScalarMap(RealType.Latitude, Display.YAxis);
        display1.addMap(map1lat);
        ScalarMap map1lon = new ScalarMap(RealType.Longitude, Display.XAxis);
        display1.addMap(map1lon);
        ScalarMap map1vis = new ScalarMap(vis_radiance, Display.ZAxis);
        display1.addMap(map1vis);
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));

        mode = display1.getGraphicsModeControl();
        mode.setScaleEnable(true);
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        boolean forever = true;
        while (forever) {
          // delay(5000);
          try {
            Thread.sleep(5000);
          }
          catch (InterruptedException e) {
          }
          System.out.println("\ndelay\n");
          double[] range1lat = map1lat.getRange();
          double[] range1lon = map1lon.getRange();
          double[] range1vis = map1vis.getRange();
          double inclat = 0.05 * (range1lat[1] - range1lat[0]);
          double inclon = 0.05 * (range1lon[1] - range1lon[0]);
          double incvis = 0.05 * (range1vis[1] - range1vis[0]);
          map1lat.setRange(range1lat[1] + inclat, range1lat[0] - inclat);
          map1lon.setRange(range1lon[1] + inclon, range1lon[0] - inclon);
          map1vis.setRange(range1vis[1] + incvis, range1vis[0] - incvis);
        }

        break;

      case 27:
 
        System.out.println(test_case + ": test interactive scale");
        System.out.println("  drag yellow points with right mouse button");
        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        final ScalarMap map2lat = new ScalarMap(RealType.Latitude, Display.YAxis);
        display1.addMap(map2lat);
        final ScalarMap map2lon = new ScalarMap(RealType.Longitude, Display.XAxis);
        display1.addMap(map2lon);
        final ScalarMap map2vis = new ScalarMap(vis_radiance, Display.ZAxis);
        display1.addMap(map2vis);
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
 
        mode = display1.getGraphicsModeControl();
        mode.setScaleEnable(true);
        mode.setPointSize(5.0f);
        mode.setPointMode(false);

        mode.setProjectionPolicy(javax.media.j3d.View.PARALLEL_PROJECTION);
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        try {
          Thread.sleep(2000);
        }
        catch (InterruptedException e) {
        }
        double[] range1lat = map2lat.getRange();
        double[] range1lon = map2lon.getRange();
        double[] range1vis = map2vis.getRange();

        RealTuple direct_low = new RealTuple(new Real[]
                         {new Real(RealType.Latitude, range1lat[0]),
                          new Real(RealType.Longitude, range1lon[0]),
                          new Real(vis_radiance, range1vis[0])});
        RealTuple direct_hi = new RealTuple(new Real[]
                         {new Real(RealType.Latitude, range1lat[1]),
                          new Real(RealType.Longitude, range1lon[1]),
                          new Real(vis_radiance, range1vis[1])});

        final DataReferenceImpl ref_direct_low =
          new DataReferenceImpl("ref_direct_low");
        ref_direct_low.setData(direct_low);
        // color low and hi tuples yellow
        ConstantMap[][] maps = {{new ConstantMap(1.0f, Display.Red),
                                 new ConstantMap(1.0f, Display.Green),
                                 new ConstantMap(0.0f, Display.Blue)}};
        display1.addReferences(new DirectManipulationRendererJ3D(),
                               new DataReference[] {ref_direct_low}, maps);
 
        final DataReferenceImpl ref_direct_hi =
          new DataReferenceImpl("ref_direct_hi");
        ref_direct_hi.setData(direct_hi);
        display1.addReferences(new DirectManipulationRendererJ3D(),
                               new DataReference[] {ref_direct_hi}, maps);

        no_self = 0;

        cell = new CellImpl() {
          public synchronized void doAction()
                 throws VisADException, RemoteException {
            if (no_self > 0) {
              no_self--;
              if (no_self > 0) return;
            }
            RealTuple low = (RealTuple) ref_direct_low.getData();
            RealTuple hi = (RealTuple) ref_direct_hi.getData();
            double[] lows = {((Real) low.getComponent(0)).getValue(),
                             ((Real) low.getComponent(1)).getValue(),
                             ((Real) low.getComponent(2)).getValue()};
            double[] his = {((Real) hi.getComponent(0)).getValue(),
                            ((Real) hi.getComponent(1)).getValue(),
                            ((Real) hi.getComponent(2)).getValue()};
            boolean changed = false;
            for (int i=0; i<3; i++) {
              if (his[i] < lows[i] + 0.00001) {
                double m = 0.5 * (lows[i] + his[i]);
                lows[i] = m - 0.000005;
                his[i] = m + 0.000005;
                changed = true;
              }
            }

            if (changed) {
              RealTuple dlow = new RealTuple(new Real[]
                         {new Real(RealType.Latitude, lows[0]),
                          new Real(RealType.Longitude, lows[1]),
                          new Real(vis_radiance, lows[2])});
              RealTuple dhi = new RealTuple(new Real[]
                         {new Real(RealType.Latitude, his[0]),
                          new Real(RealType.Longitude, his[1]),
                          new Real(vis_radiance, his[2])});
              ref_direct_low.setData(dlow);
              ref_direct_hi.setData(dhi);
              no_self += 2;
            }

            map2lat.setRange(lows[0], his[0]);
            map2lon.setRange(lows[1], his[1]);
            map2vis.setRange(lows[2], his[2]);
          }
        };
        cell.addReference(ref_direct_low);
        cell.addReference(ref_direct_hi);

        break;

      case 28:
 
        System.out.println(test_case + ": test flow");
 
        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        ScalarMap map28flow = new ScalarMap(vis_radiance, Display.Flow1X);
        display1.addMap(map28flow);
        display1.addMap(new ScalarMap(ir_radiance, Display.Flow1Y));
 
        FlowControl control28flow = (FlowControl) map28flow.getControl();
        control28flow.setFlowScale(0.06f);

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 29:
 
        System.out.println(test_case + ": test 2-D irregular surface");
 
        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, true);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 30:

        System.out.println(test_case + ": test time stack");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);
        wasp = FlatField.makeField(image_bumble, size, false);

        ntimes1 = 4;

        time_set =
          new Linear1DSet(time_type, 0.0, (double) (ntimes1 - 1.0), ntimes1);

        image_sequence = new FieldImpl(time_images, time_set);
        temp = imaget1;
        Real[] reals30 = {new Real(vis_radiance, (float) size / 4.0f),
                          new Real(ir_radiance, (float) size / 8.0f)};
        val = new RealTuple(reals30);
        for (int i=0; i<ntimes1; i++) {
          image_sequence.setSample(i, temp);
          temp = (FlatField) temp.add(val);
        }

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
     
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.Red));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ScalarMap(RealType.Time, Display.ZAxis));

        DataReference ref_image_sequence = new DataReferenceImpl("ref_big_tuple");
        ref_image_sequence.setData(image_sequence);
        display1.addReference(ref_image_sequence, null);

        break;

      case 31:
 
        System.out.println(test_case + ": test scatter diagram");
        size = 64;

        imaget1 = FlatField.makeField(scatter_function, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
 
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.Green));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.XAxis));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
 
        mode = display1.getGraphicsModeControl();
        mode.setPointSize(5.0f);

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 32:

        System.out.println(test_case + ": test FITS adapter");

        if (args.length < 2) {
          System.out.println("must specify FITS file name");
          return;
        }
        // "ngc1316o.fits"
        name = args[1];
 
        FitsForm fits = new FitsForm();
        FlatField fits_data = (FlatField) fits.open(name);
        // System.out.println("fits_data type = " + fits_data.getType());

        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        // display1 = new DisplayImplJ3D("display1", new TwoDDisplayRendererJ3D(),
        //                               DisplayImplJ3D.APPLETFRAME);
        // compute ScalarMaps from type components
        ftype = (FunctionType) fits_data.getType();
        dtype = ftype.getDomain();
        rntype = ftype.getRange();
        n = dtype.getDimension();
        display1.addMap(new ScalarMap((RealType) dtype.getComponent(0),
                                      Display.XAxis));
        if (n > 1) {
          display1.addMap(new ScalarMap((RealType) dtype.getComponent(1),
                                        Display.YAxis));
        }
        if (n > 2) {
          display1.addMap(new ScalarMap((RealType) dtype.getComponent(2),
                                        Display.ZAxis));
        }
        if (rntype instanceof RealType) {
          display1.addMap(new ScalarMap((RealType) rntype, Display.Green));
        }
        else if (rntype instanceof RealTupleType) {
          int m = ((RealTupleType) rntype).getDimension();
          RealType rr = (RealType) ((RealTupleType) rntype).getComponent(0);
          display1.addMap(new ScalarMap(rr, Display.Green));
          if (n <= 2) {
            if (m > 1) {
              rr = (RealType) ((RealTupleType) rntype).getComponent(1);
            }
            display1.addMap(new ScalarMap(rr, Display.ZAxis));
          }
        }
        display1.addMap(new ConstantMap(0.5, Display.Red));
        display1.addMap(new ConstantMap(0.0, Display.Blue));

        DataReferenceImpl ref_fits = new DataReferenceImpl("ref_fits");
        ref_fits.setData(fits_data);
        display1.addReference(ref_fits, null);

        break;

      case 33:
 
        System.out.println(test_case + ": test ColorWidget with non-default table");
 
        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ3D("display1", DisplayImplJ3D.APPLETFRAME);
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
 
        color1map = new ScalarMap(ir_radiance, Display.RGB);
        display1.addMap(color1map);
 
        float[][] table = new float[3][256];
        for (int i=0; i<256; i++) {
          float a = ((float) i) / 256.0f;
          table[0][i] = a;
          table[1][i] = 1.0f - a;
          table[2][i] = 0.5f;
        }

        lw = new LabeledRGBWidget(color1map, 0.0f, 32.0f, table);
 
        frame = new Frame("VisAD Color Widget");
        frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        frame.add(lw);
        frame.setSize(lw.getPreferredSize());
        frame.setVisible(true);
 
        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 34:

        System.out.println(test_case + ": test direct manipulation in Java2D");
        size = 64;
        histogram1 = FlatField.makeField(ir_histogram, size, false);
        direct = new Real(ir_radiance, 2.0);
        reals3 = new Real[] {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                             new Real(vis_radiance, 1.0)};
        direct_tuple = new RealTuple(reals3);
    
        display1 = new DisplayImplJ2D("display1");
        display1.addMap(new ScalarMap(ir_radiance, Display.XAxis));
        display1.addMap(new ScalarMap(count, Display.YAxis));
        display1.addMap(new ScalarMap(count, Display.Green));
    
        mode = display1.getGraphicsModeControl();
        mode.setPointSize(5.0f);
        mode.setPointMode(false);

        ref_direct = new DataReferenceImpl("ref_direct");
        ref_direct.setData(direct);
        refs1 = new DataReferenceImpl[] {ref_direct};
        display1.addReferences(new DirectManipulationRendererJ2D(), refs1, null);
     
        ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
        ref_direct_tuple.setData(direct_tuple);
        refs2 = new DataReference[] {ref_direct_tuple};
        display1.addReferences(new DirectManipulationRendererJ2D(), refs2, null);
     
        ref_histogram1 = new DataReferenceImpl("ref_histogram1");
        ref_histogram1.setData(histogram1);
        refs3 = new DataReference[] {ref_histogram1};
        display1.addReferences(new DirectManipulationRendererJ2D(), refs3, null);

        display2 = new DisplayImplJ2D("display2");
        display2.addMap(new ScalarMap(ir_radiance, Display.XAxis));
        display2.addMap(new ScalarMap(count, Display.YAxis));
        display2.addMap(new ScalarMap(count, Display.Green));
     
        mode2 = display2.getGraphicsModeControl();
        mode2.setPointSize(5.0f);
        mode2.setPointMode(false);
     
        display2.addReferences(new DirectManipulationRendererJ2D(), refs1, null);
        display2.addReferences(new DirectManipulationRendererJ2D(), refs2, null);
        display2.addReferences(new DirectManipulationRendererJ2D(), refs3, null);

        jframe = new JFrame("Java2D direct manipulation");
        jframe.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        big_panel = new JPanel();
        big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
        big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        big_panel.add(display1.getComponent());
        big_panel.add(display2.getComponent());
        jframe.getContentPane().add(big_panel);
        jframe.setSize(512, 256);
        jframe.setVisible(true);

        break;

      case 35:

        System.out.println(test_case + ": test direct manipulation linking " +
                           "Java2D and Java3D");
        size = 64;
        histogram1 = FlatField.makeField(ir_histogram, size, false);
        direct = new Real(ir_radiance, 2.0);
        reals3 = new Real[] {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                             new Real(vis_radiance, 1.0)};
        direct_tuple = new RealTuple(reals3);
    
        display1 = new DisplayImplJ3D("display1");
        display1.addMap(new ScalarMap(vis_radiance, Display.ZAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.XAxis));
        display1.addMap(new ScalarMap(count, Display.YAxis));
        display1.addMap(new ScalarMap(count, Display.Green));
    
        mode = display1.getGraphicsModeControl();
        mode.setPointSize(5.0f);
        mode.setPointMode(false);

        ref_direct = new DataReferenceImpl("ref_direct");
        ref_direct.setData(direct);
        refs1 = new DataReferenceImpl[] {ref_direct};
        display1.addReferences(new DirectManipulationRendererJ3D(), refs1, null);
     
        ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
        ref_direct_tuple.setData(direct_tuple);
        refs2 = new DataReference[] {ref_direct_tuple};
        display1.addReferences(new DirectManipulationRendererJ3D(), refs2, null);
     
        ref_histogram1 = new DataReferenceImpl("ref_histogram1");
        ref_histogram1.setData(histogram1);
        refs3 = new DataReference[] {ref_histogram1};
        display1.addReferences(new DirectManipulationRendererJ3D(), refs3, null);

        display2 = new DisplayImplJ2D("display2");
        display2.addMap(new ScalarMap(ir_radiance, Display.XAxis));
        display2.addMap(new ScalarMap(count, Display.YAxis));
        display2.addMap(new ScalarMap(count, Display.Green));
     
        mode2 = display2.getGraphicsModeControl();
        mode2.setPointSize(5.0f);
        mode2.setPointMode(false);
     
        display2.addReferences(new DirectManipulationRendererJ2D(), refs1, null);
        display2.addReferences(new DirectManipulationRendererJ2D(), refs2, null);
        display2.addReferences(new DirectManipulationRendererJ2D(), refs3, null);

        jframe = new JFrame("Java3D -- Java2D direct manipulation");
        jframe.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        big_panel = new JPanel();
        big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
        big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        big_panel.add(display1.getComponent());
        big_panel.add(display2.getComponent());
        jframe.getContentPane().add(big_panel);
        jframe.setSize(512, 256);
        jframe.setVisible(true);

        break;

      case 36:

        System.out.println(test_case + ": test polar coordinates in Java2D");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);

        display1 = new DisplayImplJ2D("display1");
        display1.addMap(new ScalarMap(RealType.Latitude, Display.Radius));
        ScalarMap lonmap = new ScalarMap(RealType.Longitude, Display.Longitude);
        lonmap.setRangeByUnits();
        display1.addMap(lonmap);
        // display1.addMap(new ScalarMap(RealType.Longitude, Display.Longitude));
        display1.addMap(new ScalarMap(vis_radiance, Display.RGB));
     
        jframe = new JFrame("polar coordinates in Java2D");
        jframe.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        jframe.getContentPane().add(display1.getComponent());
        jframe.setSize(256, 256);
        jframe.setVisible(true);

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 37:

        System.out.println(test_case + ": test colored contours from " +
                           "regular grids in Java2D");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, false);

        display1 = new DisplayImplJ2D("display1");
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
        display1.addMap(map1contour);
        control1contour = (ContourControl) map1contour.getControl();
        control1contour.enableContours(true);
        control1contour.enableLabels(true);
     
        mode = display1.getGraphicsModeControl();
        mode.setScaleEnable(true);

        jframe = new JFrame("regular contours in Java2D");
        jframe.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        jframe.getContentPane().add(display1.getComponent());
        jframe.setSize(256, 256);
        jframe.setVisible(true);

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        break;

      case 38:
 
        System.out.println(test_case + ": test colored contours from " +
                           "irregular grids in Java2D");
        size = 64;
        imaget1 = FlatField.makeField(image_tuple, size, true);
 
        display1 = new DisplayImplJ2D("display1");
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
        display1.addMap(new ScalarMap(ir_radiance, Display.Green));
        display1.addMap(new ConstantMap(0.5, Display.Blue));
        display1.addMap(new ConstantMap(0.5, Display.Red));
        map1contour = new ScalarMap(vis_radiance, Display.IsoContour);
        display1.addMap(map1contour);
        control1contour = (ContourControl) map1contour.getControl();
        control1contour.enableContours(true);
 
        jframe = new JFrame("irregular contours in Java2D");
        jframe.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        jframe.getContentPane().add(display1.getComponent());
        jframe.setSize(256, 256);
        jframe.setVisible(true);

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 39:
 
        System.out.println(test_case + ": test color array and ColorWidget " +
                           "in Java2D");

        size = 32;
        imaget1 = FlatField.makeField(image_tuple, size, false);
 
        display1 = new DisplayImplJ2D("display1");
        display1.addMap(new ScalarMap(RealType.Latitude, Display.YAxis));
        display1.addMap(new ScalarMap(RealType.Longitude, Display.XAxis));
 
        color1map = new ScalarMap(vis_radiance, Display.RGB);
        display1.addMap(color1map);

        lw = new LabeledRGBWidget(color1map, 0.0f, 32.0f);

        ((DisplayRendererJ2D) display1.getDisplayRenderer()).getCanvas().
          setPreferredSize(new Dimension(256, 256));

        jframe = new JFrame("VisAD Color Widget in Java2D");
        jframe.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        big_panel = new JPanel();
        big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.Y_AXIS));
        big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        big_panel.add(lw);
        big_panel.add(display1.getComponent());
        jframe.getContentPane().add(big_panel);
        jframe.setSize(400, 400);
        jframe.setVisible(true);

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);
 
        break;

      case 40:

        System.out.println(test_case + ": test polar direct manipulation in Java2D");
        size = 64;
        histogram1 = FlatField.makeField(ir_histogram, size, false);
        reals3 = new Real[] {new Real(count, 1.0), new Real(ir_radiance, 2.0),
                             new Real(vis_radiance, 1.0)};
        direct_tuple = new RealTuple(reals3);
    
        display1 = new DisplayImplJ2D("display1");
        display1.addMap(new ScalarMap(ir_radiance, Display.Radius));
        display1.addMap(new ScalarMap(count, Display.Longitude));
        display1.addMap(new ScalarMap(count, Display.Green));
    
        mode = display1.getGraphicsModeControl();
        mode.setPointSize(5.0f);
        mode.setPointMode(false);

        ref_direct_tuple = new DataReferenceImpl("ref_direct_tuple");
        ref_direct_tuple.setData(direct_tuple);
        refs2 = new DataReference[] {ref_direct_tuple};
        display1.addReferences(new DirectManipulationRendererJ2D(), refs2, null);
     
        ref_histogram1 = new DataReferenceImpl("ref_histogram1");
        ref_histogram1.setData(histogram1);
        refs3 = new DataReference[] {ref_histogram1};
        display1.addReferences(new DirectManipulationRendererJ2D(), refs3, null);

        display2 = new DisplayImplJ2D("display2");
        display2.addMap(new ScalarMap(ir_radiance, Display.XAxis));
        display2.addMap(new ScalarMap(count, Display.YAxis));
        display2.addMap(new ScalarMap(count, Display.Green));
     
        mode2 = display2.getGraphicsModeControl();
        mode2.setPointSize(5.0f);
        mode2.setPointMode(false);
     
        display2.addReferences(new DirectManipulationRendererJ2D(), refs2, null);
        display2.addReferences(new DirectManipulationRendererJ2D(), refs3, null);

        jframe = new JFrame("Java2D direct manipulation");
        jframe.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        big_panel = new JPanel();
        big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
        big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        big_panel.add(display1.getComponent());
        big_panel.add(display2.getComponent());
        jframe.getContentPane().add(big_panel);
        jframe.setSize(512, 256);
        jframe.setVisible(true);

        break;

      case 41:

        System.out.println(test_case + ": image / contour alignment in " +
                           "Java2D");

        // construct types
        int isize = 16;
        RealType dom0 = new RealType("dom0");
        RealType dom1 = new RealType("dom1");
        RealType ran = new RealType("ran");
        RealTupleType dom = new RealTupleType(dom0, dom1);
        ftype = new FunctionType(dom, ran);
        imaget1 = new FlatField(ftype, new Integer2DSet(isize, isize));
        double[][] vals = new double[1][isize * isize];
        for (int i=0; i<isize; i++) {
          for (int j=0; j<isize; j++) {
            vals[0][j + isize * i] = (i + 1) * (j + 1);
          }
        }
        imaget1.setSamples(vals, false);

        RealType oogle = new RealType("oogle");
        FunctionType ftype2 = new FunctionType(dom, oogle);
        FlatField imaget2 = new FlatField(ftype2, imaget1.getDomainSet());
        imaget2.setSamples(vals, false);

        display1 = new DisplayImplJ2D("display1");
        display1.addMap(new ScalarMap(dom0, Display.XAxis));
        display1.addMap(new ScalarMap(dom1, Display.YAxis));
        display1.addMap(new ScalarMap(ran, Display.Green));
        display1.addMap(new ConstantMap(0.3, Display.Blue));
        display1.addMap(new ConstantMap(0.3, Display.Red));
        display1.addMap(new ScalarMap(oogle, Display.IsoContour));

        mode = display1.getGraphicsModeControl();
        mode.setTextureEnable(false);

        ConstantMap[] omaps1 = {new ConstantMap(1.0, Display.Blue),
                                new ConstantMap(1.0, Display.Red),
                                new ConstantMap(0.0, Display.Green)};

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        DataReferenceImpl ref_imaget2 = new DataReferenceImpl("ref_imaget2");
        ref_imaget2.setData(imaget2);
        display1.addReference(ref_imaget2, omaps1);

        display2 = new DisplayImplJ2D("display2");
        display2.addMap(new ScalarMap(dom0, Display.XAxis));
        display2.addMap(new ScalarMap(dom1, Display.YAxis));
        display2.addMap(new ScalarMap(ran, Display.Green));
        display2.addMap(new ConstantMap(0.3, Display.Blue));
        display2.addMap(new ConstantMap(0.3, Display.Red));
        display2.addMap(new ScalarMap(oogle, Display.IsoContour));

        ConstantMap[] omaps2 = {new ConstantMap(1.0, Display.Blue),
                                new ConstantMap(1.0, Display.Red),
                                new ConstantMap(0.0, Display.Green)};

        display2.addReference(ref_imaget1, null);
        display2.addReference(ref_imaget2, omaps2);

        jframe = new JFrame("Java3D -- Java2D direct manipulation");
        jframe.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        big_panel = new JPanel();
        big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
        big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        big_panel.add(display1.getComponent());
        big_panel.add(display2.getComponent());
        jframe.getContentPane().add(big_panel);
        jframe.setSize(800, 400);
        jframe.setVisible(true);

        break;

      case 42:

        System.out.println(test_case + ": image / contour alignment in " +
                           "Java3D");

        // construct types
        isize = 16;
        dom0 = new RealType("dom0");
        dom1 = new RealType("dom1");
        ran = new RealType("ran");
        dom = new RealTupleType(dom0, dom1);
        ftype = new FunctionType(dom, ran);
        imaget1 = new FlatField(ftype, new Integer2DSet(isize, isize));
        vals = new double[1][isize * isize];
        for (int i=0; i<isize; i++) {
          for (int j=0; j<isize; j++) {
            vals[0][j + isize * i] = (i + 1) * (j + 1);
          }
        }
        imaget1.setSamples(vals, false);

        oogle = new RealType("oogle");
        ftype2 = new FunctionType(dom, oogle);
        imaget2 = new FlatField(ftype2, imaget1.getDomainSet());
        imaget2.setSamples(vals, false);

        display1 = new DisplayImplJ3D("display1");
        display1.addMap(new ScalarMap(dom0, Display.XAxis));
        display1.addMap(new ScalarMap(dom1, Display.YAxis));
        display1.addMap(new ScalarMap(ran, Display.Green));
        display1.addMap(new ConstantMap(0.3, Display.Blue));
        display1.addMap(new ConstantMap(0.3, Display.Red));
        display1.addMap(new ScalarMap(oogle, Display.IsoContour));

        mode = display1.getGraphicsModeControl();
        mode.setTextureEnable(false);

        omaps1 = new ConstantMap[] {new ConstantMap(1.0, Display.Blue),
                                    new ConstantMap(1.0, Display.Red),
                                    new ConstantMap(0.0, Display.Green)};

        ref_imaget1 = new DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(imaget1);
        display1.addReference(ref_imaget1, null);

        ref_imaget2 = new DataReferenceImpl("ref_imaget2");
        ref_imaget2.setData(imaget2);
        display1.addReference(ref_imaget2, omaps1);

        display2 = new DisplayImplJ3D("display2");
        display2.addMap(new ScalarMap(dom0, Display.XAxis));
        display2.addMap(new ScalarMap(dom1, Display.YAxis));
        display2.addMap(new ScalarMap(ran, Display.Green));
        display2.addMap(new ConstantMap(0.3, Display.Blue));
        display2.addMap(new ConstantMap(0.3, Display.Red));
        display2.addMap(new ScalarMap(oogle, Display.IsoContour));

        omaps2 = new ConstantMap[] {new ConstantMap(1.0, Display.Blue),
                                    new ConstantMap(1.0, Display.Red),
                                    new ConstantMap(0.0, Display.Green)};

        display2.addReference(ref_imaget1, null);
        display2.addReference(ref_imaget2, omaps2);

        jframe = new JFrame("Java3D -- Java2D direct manipulation");
        jframe.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {System.exit(0);}
        });
 
        big_panel = new JPanel();
        big_panel.setLayout(new BoxLayout(big_panel, BoxLayout.X_AXIS));
        big_panel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        big_panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        big_panel.add(display1.getComponent());
        big_panel.add(display2.getComponent());
        jframe.getContentPane().add(big_panel);
        jframe.setSize(800, 400);
        jframe.setVisible(true);

        break;
/*
  J2D and J3D behave the same.
  ImageElement = 0 at bottom and 224 at top
  ImageLine = 0 at left and 193 at right
    (xv thinks 0,0 is top left)
  image seen right side up
  contours seen upside down
  image and contour agree left to right,
    and are oriented correctly (words readable)
 - - - - - - - - -
  J2D, if mode.setTextureEnable(false)
       then image matches contours
  J3D, I recall (?) that texture true matches texture false
*/

    } // end switch(test_case)

    while (true) {
      // delay(5000);
      try {
        Thread.sleep(5000);
      }
      catch (InterruptedException e) {
      }
      // System.out.println("\ndelay\n");
    }
  }

}

