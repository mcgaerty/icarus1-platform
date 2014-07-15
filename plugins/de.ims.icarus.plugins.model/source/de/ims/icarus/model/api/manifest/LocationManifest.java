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
package de.ims.icarus.model.api.manifest;

import java.util.List;

import de.ims.icarus.model.iql.access.AccessControl;
import de.ims.icarus.model.iql.access.AccessMode;
import de.ims.icarus.model.iql.access.AccessPolicy;
import de.ims.icarus.model.iql.access.AccessRestriction;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface LocationManifest {

//	LocationType getType();

	/**
	 * Returns the "root" path to the location described by this manifest.
	 * Depending on the exact location type, the meaning of this root path
	 * may vary. It can denote a single corpus file, an entire folder or the
	 * identifier of a database, for example.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	String getPath();

	/**
	 * If the data source is distributed then this method returns the manifest
	 * that describes the resolver to use when accessing different chunks of data.
	 * If the data is not of distributed nature this method returns {@code null}.
	 *
	 * @return
	 */
	@AccessRestriction(AccessMode.READ)
	PathResolverManifest getPathResolverManifest();

	@AccessRestriction(AccessMode.READ)
	List<PathEntry> getPathEntries();

	// Modification methods

	void setPath(String path);

	void setPathResolverManifest(PathResolverManifest pathResolverManifest);

	void addPathEntry(PathEntry entry);

	void removePathEntry(PathEntry entry);

	public enum PathType {
		FILE,
		FOLDER,
		PATTERN,
		IDENTIFIER,
		CUSTOM;

		/**
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}


		public static PathType parsePathType(String s) {
			return valueOf(s.toUpperCase());
		}
	}

	@AccessControl(AccessPolicy.DENY)
	public interface PathEntry {

		@AccessRestriction(AccessMode.READ)
		PathType getType();

		@AccessRestriction(AccessMode.READ)
		String getValue();
	}
}
