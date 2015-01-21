/*
 *  ICARUS -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2012-2013 Markus Gärtner and Gregor Thiele
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus.model.api.highlight;

import java.awt.Color;

import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.Item;

/**
 * Models a set of highlight informations for a given {@link Container}
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Highlight {

	/**
	 * Returns the container that contains legal {@code Item} objects
	 * that can be passed to the various methods in this interface.
	 */
	Container getContainer();

	HighlightCursor getHighlightCursor();

	boolean isHighlighted(Item item);

	boolean isHighlighted(Item item, int layerIndex);

	Color getHighlightColor(Item item);

	Color getHighlightColor(Item item, int layerIndex);

	int getGroupId(Item item);

	int getGroupId(Item item, int layerIndex);
}
