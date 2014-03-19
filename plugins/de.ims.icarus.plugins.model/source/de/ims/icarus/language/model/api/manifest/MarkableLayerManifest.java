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
package de.ims.icarus.language.model.api.manifest;



/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface MarkableLayerManifest extends LayerManifest {

	/**
	 * Returns the number of nested containers and/or structures within this
	 * layer.
	 * @return
	 */
	int getContainerDepth();

	/**
	 * Returns the manifest for the top-level container in this layer.
	 *
	 * @return
	 */
	ContainerManifest getRootContainerManifest();

	/**
	 * Returns the manifest for the container at depth {@code level}.
	 * For a {@code level} value of {@code 0} the result is equal to
	 * {@link #getRootContainerManifest()}.
	 *
	 * @param level the depth for which the manifest should be returned
	 * @return the manifest for the container at the given depth
     * @throws IndexOutOfBoundsException if the level is out of range
     *         (<tt>level &lt; 0 || level &gt;= getContainerDepth()</tt>)
	 */
	ContainerManifest getContainerManifest(int level);

	/**
	 * Returns the {@code MarkableLayerManifest} that describes the layer hosting
	 * <i>boundary containers</i> for the markables in this manifests'
	 * {@code MarkableLayer}. If the markables are not restricted by <i>boundary containers</i>
	 * this method should return {@code null}.
	 * <p>
	 * Being restricted by a <i>boundary container</i> means that all non-virtual members of a
	 * container (or structure) must reside within the same range of indices defined by the boundary.
	 * So for example in the case of containers they are not allowed to span across borders of
	 * their respective <i>boundary container</i>.
	 * @return
	 */
	MarkableLayerManifest getBoundaryLayerManifest();
}