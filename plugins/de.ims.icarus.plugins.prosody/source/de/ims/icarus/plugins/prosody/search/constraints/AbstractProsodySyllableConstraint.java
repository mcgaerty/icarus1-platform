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
package de.ims.icarus.plugins.prosody.search.constraints;

import java.util.Locale;

import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.GroupCache;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractProsodySyllableConstraint extends DefaultConstraint implements SyllableConstraint {

	private static final long serialVersionUID = 8333873086091026549L;

	public AbstractProsodySyllableConstraint(String token, Object value,
			SearchOperator operator, Object specifier) {
		super(token, value, operator, specifier);
	}


	/**
	 * @see de.ims.icarus.search_tools.SearchConstraint#matches(java.lang.Object)
	 */
	@Override
	public boolean matches(Object value) {
		ProsodyTargetTree tree = (ProsodyTargetTree) value;

		if(tree.hasSyllables()) {

			SearchOperator operator = getOperator();
			Object constraint = getConstraint();

			for(int i=0; i<tree.getSyllableCount(); i++) {
				if(operator.apply(getInstance(tree, i), constraint)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean matches(Object value, int syllable) {
		ProsodyTargetTree tree = (ProsodyTargetTree) value;

		if(tree.hasSyllables()) {

			SearchOperator operator = getOperator();
			Object constraint = getConstraint();

			return operator.apply(getInstance(tree, syllable), constraint);
		}

		return false;
	}


	@Override
	public boolean isMultiplexing() {
		return true;
	}

	@Override
	public void group(GroupCache cache, int groupId, Object value) {
		ProsodyTargetTree tree = (ProsodyTargetTree) value;

		// Remember to handle the "empty" cache in the result set implementations!
		if(tree.hasSyllables()) {
			for(int i=0; i<tree.getSyllableCount(); i++) {
				Object instance = getInstance(tree, i);

				if(instance instanceof Float) {
					instance = (float)Math.floor((float)instance*100F)*0.01F;
				} else if(instance instanceof Double) {
					instance = Math.floor((double)instance*100D)*0.01D;
				}

				cache.cacheGroupInstance(groupId, getLabel(instance));
			}
		}
	}

	@Override
	public Object getLabel(Object value) {
		return (value instanceof Float || value instanceof Double) ?
				String.format(Locale.ENGLISH, "%.02f", value) : super.getLabel(value); //$NON-NLS-1$
	}


	@Override
	public Object getInstance(Object value) {
		throw new UnsupportedOperationException("Syllable constraints do not operate on word level"); //$NON-NLS-1$
	}


	protected abstract Object getInstance(ProsodyTargetTree tree, int syllable);
}