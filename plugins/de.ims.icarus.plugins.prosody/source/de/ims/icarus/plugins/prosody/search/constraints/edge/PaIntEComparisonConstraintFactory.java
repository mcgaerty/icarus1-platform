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
 *
 * $Revision: 269 $
 * $Date: 2014-07-08 00:09:53 +0200 (Di, 08 Jul 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.dependency/source/de/ims/icarus/language/dependency/search/constraints/DependencyRelationConstraintFactory.java $
 *
 * $LastChangedDate: 2014-07-08 00:09:53 +0200 (Di, 08 Jul 2014) $
 * $LastChangedRevision: 269 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.plugins.prosody.search.constraints.edge;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.plugins.prosody.search.ProsodyTargetTree;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: DependencyRelationConstraintFactory.java 269 2014-07-07 22:09:53Z mcgaerty $
 *
 */
public class PaIntEComparisonConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "painteCompare"; //$NON-NLS-1$

	public PaIntEComparisonConstraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.painteCompare.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.painteCompare.description"); //$NON-NLS-1$
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return DefaultSearchOperator.numerical();
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		// TODO Auto-generated method stub
		return super.getSupportedSpecifiers();
	}

	@Override
	public Class<?> getValueClass(Object specifier) {
		return Integer.class;
	}

	@Override
	public Object getDefaultValue(Object specifier) {
		return LanguageConstants.DATA_UNDEFINED_VALUE;
	}

	@Override
	public Object labelToValue(Object label, Object specifier) {
		return LanguageUtils.parseIntegerLabel((String) label);
	}

	@Override
	public Object valueToLabel(Object value, Object specifier) {
		return LanguageUtils.getLabel((int)value);
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new PaIntEComparisonConstraint(value, operator);
	}

	private static class PaIntEComparisonConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -2819824788436110048L;

		public PaIntEComparisonConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
			return ((ProsodyTargetTree)value).getSyllableCount();
		}

		@Override
		public SearchConstraint clone() {
			return new PaIntEComparisonConstraint(getValue(), getOperator());
		}
	}
}
