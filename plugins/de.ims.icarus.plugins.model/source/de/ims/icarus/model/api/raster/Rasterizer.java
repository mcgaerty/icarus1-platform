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
package de.ims.icarus.model.api.raster;

import java.util.Comparator;

import de.ims.icarus.model.api.Markable;
import de.ims.icarus.model.api.layer.FragmentLayer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface Rasterizer extends Comparator<Position> {

	int getAxisCount();

	Axis getAxisAt(int index);

	long getRasterSize(Markable markable, FragmentLayer layer, Object value, int axis);

	long getGranularity(int axis);

	Position createPosition(long...values);

//	FragmentLayer getFragmentLayer();
}