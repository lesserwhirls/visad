/*

@(#) $Id: ColorMapWidget.java,v 1.24 1999-09-20 19:17:25 dglo Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 1998 Nick Rasmussen
VisAD is Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden, Tom
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

package visad.util;

import visad.*;
import java.rmi.RemoteException;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.util.Vector;

import javax.swing.*;

/**
 * A color widget that allows users to interactively map numeric data to
 * RGB/RGBA tuples based on the Vis5D color widget
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.24 $, $Date: 1999-09-20 19:17:25 $
 * @since Visad Utility Library v0.7.1
 */
public class LabeledColorWidget
  extends Panel
  implements ActionListener, ColorChangeListener, ControlListener,
             ScalarMapListener
{

  private final int TABLE_SIZE;
  private final float SCALE;

  private ArrowSlider slider;

  private ColorWidget widget;

  private SliderLabel label;

  private float[][] orig_table;

  BaseColorControl control;

  private int components;

  /** this will be labeled with the name of smap's RealType and
      linked to the color control in smap;
      the range of RealType values mapped to color is taken from
      smap.getRange() - this allows a color widget to be used with
      a range of values defined by auto-scaling from displayed Data;
      if smap's range values are not available at the time this
      constructor is invoked, the LabeledColorWidget becomes a
      ScalarMapListener and sets its range when smap's range is set;
      the DisplayRealType of smap must be Display.RGB or Display.RGBA
      and should already be added to a Display */
  public LabeledColorWidget(ScalarMap smap)
    throws VisADException, RemoteException
  {
    this(smap, null, true);
  }

  /** this will be labeled with the name of smap's RealType and
      linked to the color control in smap;
      the range of RealType values (min, max) is mapped to color
      as defined by an interactive color widget;
      the DisplayRealType of smap must be Display.RGB or Display.RGBA
      and should already be added to a Display
      @deprecated - 'min' and 'max' are ignored
  */
  public LabeledColorWidget(ScalarMap smap, float min, float max)
    throws VisADException, RemoteException
  {
    this(smap, null, true);
  }

  /** this will be labeled with the name of smap's RealType and
      linked to the color control in smap;
      the range of RealType values mapped to color is taken from
      smap.getRange() - this allows a color widget to be used with
      a range of values defined by auto-scaling from displayed Data;
      if smap's range values are not available at the time this
      constructor is invoked, the LabeledColorWidget becomes a
      ScalarMapListener and sets its range when smap's range is set;
      table initializes
      the color lookup table, organized as float[TABLE_SIZE][n]
      with values between 0.0f and 1.0f;
      the DisplayRealType of smap must be Display.RGB or Display.RGBA
      and should already be added to a Display */
  public LabeledColorWidget(ScalarMap smap, float[][] table)
    throws VisADException, RemoteException
  {
    this(smap, table, true);
  }

  /** this will be labeled with the name of smap's RealType and
      linked to the color control in smap;
      the range of RealType values (min, max) is mapped to color
      as defined by an interactive color widget; table initializes
      the color lookup table, organized as float[TABLE_SIZE][n]
      with values between 0.0f and 1.0f;
      the DisplayRealType of smap must be Display.RGB or Display.RGBA
      and should already be added to a Display
      @deprecated - 'min' and 'max' are ignored
  */
  public LabeledColorWidget(ScalarMap smap, float min, float max,
                            float[][] table)
    throws VisADException, RemoteException
  {
    this(smap, table, true);
  }

  /** construct a LabeledColorWidget linked to the color control
      in map (which must be to Display.RGB), with range of
      values (min, max), initial color table in format
      float[TABLE_SIZE][n] with values between 0.0f and 1.0f, and
      specified auto-scaling min and max behavior */
  public LabeledColorWidget(ScalarMap smap, float[][] in_table, boolean update)
    throws VisADException, RemoteException
  {
    Control ctl = smap.getControl();
    if (!(ctl instanceof BaseColorControl)) {
      throw new DisplayException("LabeledColorWidget: ScalarMap must " +
                                 "be Display.RGB or Display.RGBA");
    }

    control = (BaseColorControl )ctl;
    components = control.getNumberOfComponents();

    String name = smap.getScalar().getName();
    float[][] table = table_reorg(in_table);

    double[] range = smap.getRange();
    float min = (float )range[0];
    float max = (float )(range[1] + 1.0);

    // set up user interface
    ColorWidget c = new ColorWidget(new BaseRGBMap(table, components > 3));
    ArrowSlider s = new ArrowSlider(min, max, (min + max) / 2, name);
    SliderLabel l = new SliderLabel(s);
    widget = c;
    slider = s;
    label = l;
    Button reset = new Button("Reset") {
        public Dimension getMinimumSize() {
          return new Dimension(0, 18);
        }
        public Dimension getPreferredSize() {
          return new Dimension(0, 18);
        }
        public Dimension getMaximumSize() {
          return new Dimension(Integer.MAX_VALUE, 18);
        }
      };
    reset.setActionCommand("reset");
    reset.addActionListener(this);
    Button grey = new Button("Grey Scale") {
        public Dimension getMinimumSize() {
          return new Dimension(0, 18);
        }
        public Dimension getPreferredSize() {
          return new Dimension(0, 18);
        }
        public Dimension getMaximumSize() {
          return new Dimension(Integer.MAX_VALUE, 18);
        }
      };
    grey.setActionCommand("grey");
    grey.addActionListener(this);
    Panel panel = new Panel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.add(reset);
    panel.add(grey);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(widget);
    add(slider);
    add(label);
    add(panel);

    // enable auto-scaling
    if (update) {
      smap.addScalarMapListener(this);
    } else {
      smap.setRange(min, max);
      updateWidget(min, max);
    }

    // set up color table
    ColorMap map = widget.getColorMap();
    TABLE_SIZE = map.getMapResolution();
    SCALE = 1.0f / (TABLE_SIZE - 1.0f);
    if (table == null) {
      in_table = control.getTable();
      table = table_reorg(in_table);
    } else {
      control.setTable(in_table);
    }
    orig_table = copy_table(in_table);
    ((BaseRGBMap) map).setValues(table);
    widget.addColorChangeListener(this);
    control.addControlListener(this);
  }

  private Dimension maxSize = null;

  public Dimension getMaximumSize()
  {
    if (maxSize != null) return maxSize;
    else return super.getMaximumSize();
  }

  /** set maximum size of widget using java.awt.Dimension */
  public void setMaximumSize(Dimension size)
  {
    maxSize = size;
  }

  private void updateWidget(float min, float max)
  {
    float val = slider.getValue();
    if (val != val || val <= min || val >= max) val = (min+max)/2;
    slider.setBounds(min, max, val);
  }

  /** ScalarMapListener method used with delayed auto-scaling */
  public void mapChanged(ScalarMapEvent e)
  {
    ScalarMap s = e.getScalarMap();
    double[] range = s.getRange();
    updateWidget((float) range[0], (float) range[1]);
  }

  /** ColorChangeListener method */
  public void colorChanged(ColorChangeEvent e)
  {
    ColorMap map_e = widget.getColorMap();
    float[][] table_e = new float[components][TABLE_SIZE];
    for (int i=0; i<TABLE_SIZE; i++) {
      float[] t = map_e.getTuple(SCALE * i);
      table_e[0][i] = t[0];
      table_e[1][i] = t[1];
      table_e[2][i] = t[2];
      if (components > 3) {
        table_e[3][i] = t[3];
      }
    }
    try {
      control.setTable(table_e);
    }
    catch (VisADException f) { }
    catch (RemoteException f) { }
  }

  /** ActionListener method used with resetting color table */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals("reset")) {
      // reset color table to original values
      try {
        float[][] table = copy_table(orig_table);
        control.setTable(table);
        ((BaseRGBMap) widget.getColorMap()).setValues(table_reorg(table));
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
    else if (e.getActionCommand().equals("grey")) {
      // reset color table to grey wedge
      try {
        float[][] table = copy_table(orig_table);
        float a = 1.0f / (table[0].length - 1.0f);
        for (int j=0; j<table[0].length; j++) {
          table[0][j] = table[1][j] = table[2][j] = j * a;
          if (components > 3) {
            table[3][j] = 1.0f;
          }
        }
        control.setTable(table);
        ((BaseRGBMap) widget.getColorMap()).setValues(table_reorg(table));
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
    }
  }

  public void controlChanged(ControlEvent e)
    throws VisADException, RemoteException
  {
    float[][] table = control.getTable();

    ColorMap map_e = widget.getColorMap();
    boolean identical = true;
    for (int i=0; i<TABLE_SIZE; i++) {
      float[] t = map_e.getTuple(SCALE * i);
      if (Math.abs(table[0][i] - t[0]) > 0.0001 ||
          Math.abs(table[1][i] - t[1]) > 0.0001 ||
          Math.abs(table[2][i] - t[2]) > 0.0001 ||
          (components > 3 && Math.abs(table[3][i] - t[3]) > 0.0001))
      {
        identical = false;
        break;
      }
    }
    if (!identical) {
      ((BaseRGBMap) map_e).setValues(table_reorg(table));
    }
  }

  private static float[][] copy_table(float[][] table)
  {
    if (table == null || table[0] == null) return null;
    final int dim = table.length;
    int len = table[0].length;
    float[][] new_table = new float[dim][len];
    try {
      for (int i=0; i<dim; i++) {
        System.arraycopy(table[i], 0, new_table[i], 0, len);
      }
      return new_table;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  private static float[][] table_reorg(float[][] table)
  {
    if (table == null || table[0] == null) return null;
    try {
      final int dim = table.length;
      int len = table[0].length;
      float[][] out = new float[len][dim];
      for (int i=0; i<len; i++) {
        out[i][0] = table[0][i];
        out[i][1] = table[1][i];
        out[i][2] = table[2][i];
        if (dim > 3) {
          out[i][3] = table[3][i];
        }
      }
      return out;
    }
    catch (ArrayIndexOutOfBoundsException e) {
      return null;
    }
  }

  /** Returns the ColorMap that the color widget is currently pointing to */
  public ColorWidget getColorWidget()
  {
    return widget;
  }
}
