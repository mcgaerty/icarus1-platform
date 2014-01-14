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
package de.ims.icarus.language.model.io;

/**
 * Utility class for accessing distributed data or for loading
 * little chunks from a very big database.
 *
 * @author Markus Gärtner
 * @version $Id$
 * @see Path
 *
 */
public interface PathResolver {

	/**
	 * Translates the given {@code chunkIndex} into a {@code Path}
	 * information that can be used to access data from an abstract
	 * data source.
	 *
	 * @param chunkIndex
	 * @return
	 * @throws IndexOutOfBoundsException if the {@code chunkIndex} violates
	 * the bounds of this resolver. For example a resolver translating chunk indices
	 * into row values of a database table might check for the overall size of that
	 * table to make sure the returned row does not exceed the table's row count.
	 */
	Path getPath(int chunkIndex);
}