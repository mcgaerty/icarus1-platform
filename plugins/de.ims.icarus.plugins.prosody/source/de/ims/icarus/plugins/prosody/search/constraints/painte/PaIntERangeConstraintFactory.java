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
package de.ims.icarus.plugins.prosody.search.constraints.painte;

import de.ims.icarus.language.LanguageConstants;
import de.ims.icarus.plugins.prosody.ProsodyConstants;
import de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams;
import de.ims.icarus.plugins.prosody.painte.PaIntEOperator.NumberOperator;
import de.ims.icarus.plugins.prosody.painte.PaIntEUtils;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultSearchOperator;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id: DependencyRelationConstraintFactory.java 269 2014-07-07 22:09:53Z mcgaerty $
 *
 */
public class PaIntERangeConstraintFactory extends AbstractConstraintFactory implements ProsodyConstants {

	public static final String TOKEN = "painteRange"; //$NON-NLS-1$

	public PaIntERangeConstraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE,
				"plugins.prosody.constraints.painteRange.name",  //$NON-NLS-1$
				"plugins.prosody.constraints.painteRange.description"); //$NON-NLS-1$
	}

	@Override
	public Object[] getSupportedSpecifiers() {
		return new Object[0];
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return DefaultSearchOperator.comparing();
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Object specifier, Options options) {
		return new ProsodyPaIntERangeConstraint(value, operator, specifier);
	}

	private static class ProsodyPaIntERangeConstraint extends AbstractPaIntEConstraint {

		private static final long serialVersionUID = 7309790344228778387L;

		private transient PaIntEConstraintParams specifierParams;
		private transient NumberOperator numberOperator;

		public ProsodyPaIntERangeConstraint(Object value, SearchOperator operator, Object specifier) {
			super(TOKEN, value, operator, specifier);
		}

		@Override
		public PaIntEConstraintParams[] getPaIntEConstraints() {
			return new PaIntEConstraintParams[]{specifierParams};
		}

		@Override
		public SearchConstraint clone() {
			return new ProsodyPaIntERangeConstraint(getValue(), getOperator(), getSpecifier());
		}

		@Override
		public void setSpecifier(Object specifier) {
			super.setSpecifier(specifier);

			String s = (String)specifier;
			if(s!=null && !LanguageConstants.DATA_UNDEFINED_LABEL.equals(s)) {
				if(specifierParams==null) {
					specifierParams = new PaIntEConstraintParams();
				}

				parseConstraint(s, specifierParams);

				specifierParams.checkNonEmpty("PaIntE-constraint range"); //$NON-NLS-1$
			}
		}

		@Override
		public void setOperator(SearchOperator operator) {
			super.setOperator(operator);

			numberOperator = PaIntEUtils.getNumberOperator(operator);
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.painte.BoundedSyllableConstraint#getConfigPath()
		 */
		@Override
		protected String getConfigPath() {
			return null;
		}

		/**
		 * @see de.ims.icarus.plugins.prosody.search.constraints.painte.AbstractPaIntEConstraint#applyOperator(de.ims.icarus.plugins.prosody.painte.PaIntEConstraintParams)
		 */
		@Override
		protected boolean applyOperator(PaIntEConstraintParams target) {
			if(!specifierParams.isCompact()) {
				if(specifierParams.isA1Active() && constraintParams.isA1Active()
						&& !numberOperator.apply(Math.abs(target.getA1()-specifierParams.getA1()), constraintParams.getA1())) {
					return false;
				}
				if(specifierParams.isA2Active() && constraintParams.isA2Active()
						&& !numberOperator.apply(Math.abs(target.getA2()-specifierParams.getA2()), constraintParams.getA2())) {
					return false;
				}
				if(specifierParams.isAlignmentActive() && constraintParams.isAlignmentActive()
						&& !numberOperator.apply(Math.abs(target.getAlignment()-specifierParams.getAlignment()), constraintParams.getAlignment())) {
					return false;
				}
			}

			if(specifierParams.isBActive() && constraintParams.isBActive()
					&& !numberOperator.apply(Math.abs(target.getB()-specifierParams.getB()), constraintParams.getB())) {
				return false;
			}
			if(specifierParams.isC1Active() && constraintParams.isC1Active()
					&& !numberOperator.apply(Math.abs(target.getC1()-specifierParams.getC1()), constraintParams.getC1())) {
				return false;
			}
			if(specifierParams.isC2Active() && constraintParams.isC2Active()
					&& !numberOperator.apply(Math.abs(target.getC2()-specifierParams.getC2()), constraintParams.getC2())) {
				return false;
			}
			if(specifierParams.isDActive() && constraintParams.isDActive()
					&& !numberOperator.apply(Math.abs(target.getD()-specifierParams.getD()), constraintParams.getD())) {
				return false;
			}

			return true;
		}
	}
}