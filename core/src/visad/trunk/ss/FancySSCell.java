
//
// FancySSCell.java
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

package visad.ss;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import visad.*;
import visad.data.BadFormException;
import visad.util.*;

/** FancySSCell is an extension of BasicSSCell with extra options, such
    as a file loader dialog and a dialog to set up ScalarMaps.  It
    provides an example of GUI extensions to BasicSSCell.<P> */
public class FancySSCell extends BasicSSCell implements SSCellListener {

  /** border for cell with no data */
  static final Border B_EMPTY = new LineBorder(Color.gray, 3);

  /** border for selected cell */
  static final Border B_HIGH = new LineBorder(Color.yellow, 3);

  /** border for cell with formula */
  static final Border B_FORM = new LineBorder(new Color(0.5f, 0f, 0f), 3);

  /** border for cell with RMI address */
  static final Border B_RMI = new LineBorder(new Color(0f, 0f, 0.5f), 3);

  /** border for cell with file or URL */
  static final Border B_URL = new LineBorder(new Color(0f, 0.5f, 0f), 3);

  /** this variable is static so that the previous directory is remembered */
  static FileDialog FileBox = null;

  /** parent frame */
  Frame Parent;

  /** associated JFrame, for use with VisAD Controls */
  JFrame WidgetFrame;

  /** whether this cell is selected */
  boolean Selected = false;

  /** whether this cell should auto-switch to 3-D */
  boolean AutoSwitch = true;

  /** whether this cell should auto-detect mappings for data */
  boolean AutoDetect = true;

  /** whether this cell should auto-display its widget frame */
  boolean AutoShowControls = true;

  /** constructor for non-null RemoteServer */
  public FancySSCell(String name, RemoteServer rs, Frame parent)
                                  throws VisADException, RemoteException {
    super(name, rs);
    finishConstruction(name, parent);
  }

  /** constructor for non-null info string */
  public FancySSCell(String name, String info, Frame parent)
                                  throws VisADException, RemoteException {
    super(name, info);
    finishConstruction(name, parent);
  }

  /** constructor for null RemoteServer and null info string */
  public FancySSCell(String name, Frame parent) throws VisADException,
                                                       RemoteException {
    super(name);
    finishConstruction(name, parent);
  }

  /** constructor for null RemoteServer, info string, and parent Frame */
  public FancySSCell(String name) throws VisADException, RemoteException {
    super(name);
    finishConstruction(name, null);
  }

  /** used by constructors */
  private void finishConstruction(String name, Frame parent) {
    Parent = parent;
    setHighlighted(false);
    WidgetFrame = new JFrame("Controls (" + name + ")");
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    WidgetFrame.setContentPane(pane);
    addSSCellChangeListener(this);
  }

  /** re-auto-detect mappings when this cell's data changes */
  public void ssCellChanged(SSCellChangeEvent e) {
    if (e.getChangeType() == SSCellChangeEvent.DATA_CHANGE) {
      if (!IsRemote) {
        // attempt to auto-detect mappings for new data
        Data value = null;
        try {
          value = (Data) fm.getThing(Name);
        }
        catch (ClassCastException exc) { }
        catch (VisADException exc) { }
        try {
          if (value != null) autoDetectMappings();
        }
        catch (VisADException exc) { }
        catch (RemoteException exc) { }
      }

      // refresh border color
      setHighlighted(Selected);
    }
  }

  /** switch to 3-D mode if necessary and available, then call setMaps() */
  public void setMapsAuto(ScalarMap[] maps) throws VisADException,
                                                   RemoteException {
    if (AutoSwitch && maps != null) {
      int need = 0;
      for (int i=0; i<maps.length; i++) {
        DisplayRealType drt = maps[i].getDisplayScalar();
        if (drt.equals(Display.ZAxis) || drt.equals(Display.Latitude)) {
          need = 2;
        }
        if (drt.equals(Display.Alpha) || drt.equals(Display.RGBA)) {
          if (need < 1) need = 1;
        }
      }
      // switch to Java3D mode if needed
      setDimension(need == 1, need == 0);
    }
    setMaps(maps);
  }

  /** set the ScalarMaps for this cell and creates needed control widgets */
  public void setMaps(ScalarMap[] maps) throws VisADException,
                                               RemoteException {
    super.setMaps(maps);
    synchronized (WidgetFrame) {
      clearWidgetFrame();
      if (maps == null) return;

      // create GraphicsModeControl widget
      GMCWidget gmcw = new GMCWidget(VDisplay.getGraphicsModeControl());
      addToFrame(gmcw, false);

      // create any other necessary widgets
      for (int i=0; i<maps.length; i++) {
        DisplayRealType drt = maps[i].getDisplayScalar();
        try {
          RangeWidget rw = new RangeWidget(maps[i]);
          addToFrame(rw, true);
        }
        catch (VisADException exc) { }
        if (drt.equals(Display.RGB)) {
          LabeledRGBWidget lw = new LabeledRGBWidget(maps[i]);
          addToFrame(lw, true);
        }
        else if (drt.equals(Display.RGBA)) {
          LabeledRGBAWidget lw = new LabeledRGBAWidget(maps[i]);
          addToFrame(lw, true);
        }
        else if (drt.equals(Display.SelectValue)) {
          VisADSlider vs = new VisADSlider(maps[i]);
          vs.setAlignmentX(JPanel.CENTER_ALIGNMENT);
          addToFrame(vs, true);
        }
        else if (drt.equals(Display.SelectRange)) {
          SelectRangeWidget srw = new SelectRangeWidget(maps[i]);
          addToFrame(srw, true);
        }
        else if (drt.equals(Display.IsoContour)) {
          ContourWidget cw = new ContourWidget(maps[i]);
          WidgetFrame.getContentPane().add(cw);
          addToFrame(cw, true);
        }
        else if (drt.equals(Display.Animation)) {
          AnimationWidget aw = new AnimationWidget(maps[i]);
          addToFrame(aw, true);
        }
      }

      // show widget frame
      WidgetFrame.pack();
      if (AutoShowControls) showWidgetFrame();
    }
  }

  /** used by setMaps() method */
  private void addToFrame(Component c, boolean divide) {
    JPanel pane = (JPanel) WidgetFrame.getContentPane();
    if (divide) pane.add(new Divider());
    pane.add(c);
  }

  /** show the widgets for altering controls (if there are any) */
  public void showWidgetFrame() {
    JPanel pane = (JPanel) WidgetFrame.getContentPane();
    if (pane.getComponentCount() > 0) WidgetFrame.setVisible(true);
  }

  /** hide the widgets for altering controls */
  public void hideWidgetFrame() {
    WidgetFrame.setVisible(false);
  }

  /** remove all widgets for altering controls and hide widget frame */
  private void clearWidgetFrame() {
    hideWidgetFrame();
    JPanel pane = (JPanel) WidgetFrame.getContentPane();
    pane.removeAll();
  }

  /** guess a good set of mappings for this cell's Data and apply them */
  void autoDetectMappings() throws VisADException, RemoteException {
    if (AutoDetect) {
      Data data = null;
      data = getData();
      MathType mt = null;
      try {
        if (data != null) mt = data.getType();
      }
      catch (VisADException exc) { }
      catch (RemoteException exc) { }
      if (mt != null) {
        boolean allow3D = (Dim != JAVA2D_2D || AutoSwitch);
        setMapsAuto(mt.guessMaps(allow3D));
      }
    }
  }

  /** set this cell's formula */
  public void setFormula(String f) throws VisADException, RemoteException {
    super.setFormula(f);
  }

  /** specify whether the FancySSCell has a highlighted border */
  public void setSelected(boolean value) {
    if (Selected == value) return;
    Selected = value;
    setHighlighted(Selected);
    if (!Selected) hideWidgetFrame();
    else if (AutoShowControls) showWidgetFrame();
    validate();
    repaint();
  }

  /** specify whether this FancySSCell should auto-switch to 3-D */
  public void setAutoSwitch(boolean value) {
    AutoSwitch = value;
  }

  /** return whether this FancySSCell auto-switches to 3-D */
  public boolean getAutoSwitch() {
    return AutoSwitch;
  }

  /** specify whether this FancySSCell should auto-detect its mappings */
  public void setAutoDetect(boolean value) {
    AutoDetect = value;
  }

  /** return whether this FancySSCell auto-detects its mappings */
  public boolean getAutoDetect() {
    return AutoDetect;
  }

  /** specify whether this FancySSCell should auto-display its widget frame */
  public void setAutoShowControls(boolean value) {
    AutoShowControls = value;
  }

  /** return whether this FancySSCell auto-displays its widget frame */
  public boolean getAutoShowControls() {
    return AutoShowControls;
  }

  /** ask user to confirm clearing the cell if any other cell depends on it */
  public boolean confirmClear() {
    if (othersDepend()) {
      int ans = JOptionPane.showConfirmDialog(null, "Other cells depend on " +
        "this cell. Are you sure you want to clear it?", "Warning",
        JOptionPane.YES_NO_OPTION);
      if (ans != JOptionPane.YES_OPTION) return false;
    }
    return true;
  }

  /** clear the cell if no other cell depends on it; otherwise, ask the
      user &quot;Are you sure?&quot; return true if the cell was cleared */
  public boolean smartClear() throws VisADException, RemoteException {
    if (confirmClear()) {
      clearWidgetFrame();
      clearCell();
      return true;
    }
    else return false;
  }

  /** permanently destroy this cell, asking user for confirmation first
      if other cells depend on it; return true if the cell was destroyed */
  public boolean smartDestroy() throws VisADException, RemoteException {
    if (confirmClear()) {
      clearWidgetFrame();
      destroyCell();
      return true;
    }
    else return false;
  }

  /** let the user create ScalarMaps from the current SSPanel's Data
      to its Display */
  public void addMapDialog() {
    // check whether this cell has data
    Data data = getData();
    if (data == null) {
      JOptionPane.showMessageDialog(Parent, "This cell has no data",
        "FancySSCell error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // get mappings from mapping dialog
    ScalarMap[] maps = null;
    if (VDisplay != null) {
      Vector mapVector = VDisplay.getMapVector();
      int len = mapVector.size();
      maps = (len > 0 ? new ScalarMap[len] : null);
      for (int i=0; i<len; i++) maps[i] = (ScalarMap) mapVector.elementAt(i);
    }
    MappingDialog mapDialog = new MappingDialog(Parent, data, maps,
                              Dim != JAVA2D_2D || AutoSwitch,
                              Dim == JAVA3D_3D || AutoSwitch);
    mapDialog.pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension mds = mapDialog.getSize();
    mapDialog.setLocation(screenSize.width/2 - mds.width/2,
                          screenSize.height/2 - mds.height/2);
    mapDialog.setVisible(true);

    // make sure user did not cancel the operation
    if (!mapDialog.Confirm) return;

    if (IsRemote) {
      JOptionPane.showMessageDialog(Parent, "Cannot change mappings on a " +
        "cloned cell (yet)", "FancySSCell error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // clear old mappings
    try {
      clearMaps();
    }
    catch (VisADException exc) { }
    catch (RemoteException exc) { }

    // set up new mappings
    try {
      setMapsAuto(mapDialog.ScalarMaps);
    }
    catch (VisADException exc) {
      JOptionPane.showMessageDialog(Parent,
        "This combination of mappings is not valid: " + exc.getMessage(),
        "Cannot assign mappings", JOptionPane.ERROR_MESSAGE);
    }
    catch (RemoteException exc) {
      JOptionPane.showMessageDialog(Parent,
        "This combination of mappings is not valid: " + exc.getMessage(),
        "Cannot assign mappings", JOptionPane.ERROR_MESSAGE);
    }
  }

  /** import a data object from a given URL, in a separate thread */
  public void loadDataURL(URL u) {
    final URL url = u;
    final BasicSSCell cell = this;
    Runnable loadFile = new Runnable() {
      public void run() {
        try {
          cell.loadData(url);
          if (!cell.hasData()) {
            JOptionPane.showMessageDialog(Parent, "Unable to import data",
              "Error importing data", JOptionPane.ERROR_MESSAGE);
          }
        }
        catch (VisADException exc) {
          JOptionPane.showMessageDialog(Parent, exc.getMessage(),
            "Error importing data", JOptionPane.ERROR_MESSAGE);
        }
        catch (RemoteException exc) {
          JOptionPane.showMessageDialog(Parent, exc.getMessage(),
            "Error importing data", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(loadFile);
    t.start();
  }

  /** import a data object from a server using RMI, in a separate thread */
  public void loadDataRMI(String s) {
    final String sname = s;
    Runnable loadRMI = new Runnable() {
      public void run() {
        try {
          loadRMI(sname);
        }
        catch (RemoteException exc) {
          JOptionPane.showMessageDialog(Parent, exc.getMessage(),
            "Error importing data", JOptionPane.ERROR_MESSAGE);
        }
        catch (VisADException exc) {
          JOptionPane.showMessageDialog(Parent, exc.getMessage(),
            "Error importing data", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(loadRMI);
    t.start();
  }

  /** load a file selected by the user */
  public void loadDataDialog() {
    // get file name from file dialog
    if (FileBox == null) FileBox = new FileDialog(Parent);
    FileBox.setMode(FileDialog.LOAD);
    FileBox.setVisible(true);

    // make sure file exists
    String file = FileBox.getFile();
    if (file == null) return;
    String directory = FileBox.getDirectory();
    if (directory == null) return;
    File f = new File(directory, file);
    if (!f.exists()) {
      JOptionPane.showMessageDialog(Parent, file + " does not exist",
        "Cannot load file", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // load file
    String filename = "file:/" + f.getAbsolutePath();
    URL u = null;
    try {
      u = new URL(filename);
    }
    catch (MalformedURLException exc) { }
    if (u != null) loadDataURL(u);
  }

  /** save to a file selected by the user, in netCDF or serialized format */
  public void saveDataDialog(boolean netcdf) {
    if (!hasData()) {
      JOptionPane.showMessageDialog(Parent, "This cell is empty.",
        "Nothing to save", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // get file name from file dialog
    if (FileBox == null) FileBox = new FileDialog(Parent);
    FileBox.setMode(FileDialog.SAVE);
    FileBox.setVisible(true);

    // make sure file is valid
    String file = FileBox.getFile();
    if (file == null) return;
    String directory = FileBox.getDirectory();
    if (directory == null) return;
    File f = new File(directory, file);

    // start new thread to save the file
    final File fn = f;
    final BasicSSCell cell = this;
    final boolean nc = netcdf;
    Runnable saveFile = new Runnable() {
      public void run() {
        String msg = "Could not save the dataset \"" + fn.getName() +
                     "\" as a " + (nc ? "netCDF file"
                                      : "serialized data file") + ". ";
        try {
          cell.saveData(fn, nc);
        }
        catch (BadFormException exc) {
          msg = msg + "An error occurred: " + exc.getMessage();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
            JOptionPane.ERROR_MESSAGE);
        }
        catch (RemoteException exc) {
          msg = msg + "A remote error occurred: " + exc.getMessage();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
            JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException exc) {
          msg = msg + "An I/O error occurred: " + exc.getMessage();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
            JOptionPane.ERROR_MESSAGE);
        }
        catch (VisADException exc) {
          msg = msg + "An error occurred: " + exc.getMessage();
          JOptionPane.showMessageDialog(Parent, msg, "Error saving data",
            JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    Thread t = new Thread(saveFile);
    t.start();
  }

  /** set whether this cell is highlighted */
  private void setHighlighted(boolean hl) {
    if (hl) setBorder(B_HIGH);
    else {
      if (hasFormula()) setBorder(B_FORM);
      else if (RMIAddress != null) setBorder(B_RMI);
      else if (hasData()) setBorder(B_URL);
      else setBorder(B_EMPTY);
    }
  }

  /** thin, horizontal divider for separating widget frame components */
  private class Divider extends JComponent {

    public void paint(Graphics g) {
      int w = getSize().width;
      g.setColor(Color.white);
      g.drawRect(0, 0, w-2, 6);
      g.drawRect(2, 2, w-4, 2);
      g.setColor(Color.black);
      g.drawRect(1, 1, w-3, 3);
    }

    public Dimension getMinimumSize() {
      return new Dimension(0, 6);
    }

    public Dimension getPreferredSize() {
      return new Dimension(0, 6);
    }

    public Dimension getMaximumSize() {
      return new Dimension(Integer.MAX_VALUE, 6);
    }

  }

}

