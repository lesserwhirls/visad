/*

@(#) $Id: SliderChangeListener.java,v 1.2 1998-02-20 16:55:30 billh Exp $

VisAD Utility Library: Widgets for use in building applications with
the VisAD interactive analysis and visualization library
Copyright (C) 1998 Nick Rasmussen
VisAD is Copyright (C) 1996 - 1998 Bill Hibbard, Curtis Rueden and Tom
Rink.
 
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

import java.util.EventListener;

/**
 * The interface that any object that wishes to listen to SliderChangeEvents
 * must implement
 *
 * @author Nick Rasmussen nick@cae.wisc.edu
 * @version $Revision: 1.2 $, $Date: 1998-02-20 16:55:30 $
 * @since Visad Utility Library v0.7.1
 */

public interface SliderChangeListener extends EventListener {

	/** The nessecary function to implement */ 
	public abstract void sliderChanged(SliderChangeEvent e);
	
}