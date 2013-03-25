/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import net.ikarus_systems.icarus.util.location.Location;
import net.ikarus_systems.icarus.util.location.Locations;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class LocationAdapter extends XmlAdapter<String, Location> {
	
	public Location unmarshal(String s) {
		try {
			return Locations.getLocation(s);
		} catch (Exception e) {
			return null;
		}
	}

	public String marshal(Location loc) {
		return Locations.getPath(loc);
	}
}
