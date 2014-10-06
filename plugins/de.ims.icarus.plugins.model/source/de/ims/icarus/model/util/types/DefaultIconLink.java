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
package de.ims.icarus.model.util.types;

import javax.swing.Icon;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DefaultIconLink implements IconLink {

	private final Icon icon;
	private final String title;
	private final String description;

	public DefaultIconLink(Icon icon, String title, String description) {
		if (icon == null)
			throw new NullPointerException("Invalid icon"); //$NON-NLS-1$
		if (title == null)
			throw new NullPointerException("Invalid title"); //$NON-NLS-1$

		if(title.isEmpty())
			throw new IllegalArgumentException("Empty title"); //$NON-NLS-1$

		this.icon = icon;
		this.title = title;
		this.description = description;
	}

	public DefaultIconLink(Icon icon, String title) {
		this(icon, title, null);
	}


	/**
	 * @see de.ims.icarus.model.util.types.IconLink#getIcon()
	 */
	@Override
	public Icon getIcon() {
		return icon;
	}

	/**
	 * @see de.ims.icarus.model.util.types.IconLink#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * @see de.ims.icarus.model.util.types.IconLink#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return title.hashCode()*icon.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof IconLink) {
			IconLink other = (IconLink) obj;
			return title.equals(other.getTitle()) && icon.equals(other.getIcon());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IconLink@"+title; //$NON-NLS-1$
	}

}