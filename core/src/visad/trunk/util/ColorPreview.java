/*

@(#) $Id: ColorPreview.java,v 1.8 2000-02-18 20:44:02 dglo Exp $

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

import java.awt.*;

/** 
 * A small preview bar generated for a color widget
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.8 $, $Date: 2000-02-18 20:44:02 $
 * @since Visad Utility Library, 0.5
 */

public class ColorPreview extends Panel implements ColorChangeListener {

	/** The ColorWidget that this is attached to */
	private ColorMap map;
	
	/** The height of the ColorPreview */
	private int height;

	/**
         * Constructs a ColorPreview that Listens to the specified
         * widget and has the default height
         *
         * @deprecated Specify the ColorMap instead.
	 */
	ColorPreview(ColorWidget widget) {
		this(widget, 15);
	}

	/**
         * Constructs a ColorPreview that listens to the specified
         * ColorWidget and has the specified height
         *
         * @deprecated Specify the ColorMap instead.
	 */
	public ColorPreview(ColorWidget widget, int height) {
		this(widget.getColorMap(), height);
        }

	/**
         * Constructs a ColorPreview that Listens to the specified
         * ColorMap and has the default height
	 */
	ColorPreview(ColorMap map) {
		this(map, 15);
	}

	/**
         * Constructs a ColorPreview that listens to the specified
         * ColorMap and has the specified height
	 */
	public ColorPreview(ColorMap map, int height) {
		this.map = map;
		this.height = height;
		map.addColorChangeListener(this);
	}

	/** Overridden to maintain the preview at the specified height */
	public Dimension getMaximumSize() {
		return new Dimension(Integer.MAX_VALUE, height);
	}

	/** Redraw the entire panel */	
	public void paint(Graphics g) {
		updateLeft = 0;
		updateRight = 1;
		update(g);
	}

	/** The location to begin an update */	
	private float updateLeft;
	/** The location to end an update */
	private float updateRight;

	/** Updates the nessecary areas of the panel after ColorChangeEvents and paint()
	 * @see ColorChangeEvent
	 */
	public void update(Graphics g) {
	
		int leftIndex;
		int rightIndex;
	
		synchronized(this) {
			leftIndex = (int) Math.floor(updateLeft * getBounds().width);
			rightIndex = (int) Math.floor(updateRight * getBounds().width);
			updateLeft = 1;
			updateRight = 0;
		}
		
		if (leftIndex > rightIndex) {
			int tmp = leftIndex;
			leftIndex = rightIndex;
			rightIndex = tmp;
		}

		if (leftIndex < 0) {
			leftIndex = 0;
		}
		if (leftIndex >= getBounds().width) {
			leftIndex = getBounds().width - 1;
		}
		if (rightIndex < 0) {
			rightIndex = 0;
		}
		if (rightIndex >= getBounds().width) {
			rightIndex = getBounds().width - 1;
		}

		for (int i = leftIndex; i <= rightIndex; i++) {
			float percent = (float) i / (float) getBounds().width;
			g.setColor(map.getColor(percent));
			g.drawLine(i,0,i,getBounds().height - 1);
		}
	}

	/** Implementation of the ColorChangeListener interface
	 * @see ColorChangeListener
	 */
	public void colorChanged(ColorChangeEvent e) {
		synchronized(this) {
			if (e.getStart() < updateLeft) {
				updateLeft = e.getStart();
			}
			if (e.getEnd() > updateRight) {
				updateRight = e.getEnd();
			}
		}

                // redraw
                validate();
		repaint();	
	}

	/** Finds the preferred width of the ColorMap, and returns it with the specified
	 * height
	 */
	public Dimension getPreferredSize() {
		Dimension d = map.getPreferredSize();
		return new Dimension(d.width, height);
	}

	public void setMap(ColorMap newMap)
	{
	  map.removeColorChangeListener(this);
	  newMap.addColorChangeListener(this);
	  map = newMap;
	}
}
