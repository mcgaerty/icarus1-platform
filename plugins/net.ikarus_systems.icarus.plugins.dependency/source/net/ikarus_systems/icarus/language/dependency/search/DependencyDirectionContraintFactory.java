/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.language.dependency.search;

import net.ikarus_systems.icarus.language.LanguageUtils;
import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.standard.AbstractConstraintFactory;
import net.ikarus_systems.icarus.search_tools.standard.DefaultConstraint;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyDirectionContraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "direction"; //$NON-NLS-1$

	public DependencyDirectionContraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.direction.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.direction.description"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, net.ikarus_systems.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator) {
		return new DependencyDirectionConstraint(value, operator);
	}

	@Override
	public Class<?> getValueClass() {
		return null;
	}

	@Override
	public SearchOperator[] getSupportedOperators() {
		return new SearchOperator[]{
				SearchOperator.EQUALS,
				SearchOperator.GROUPING,
		};
	}

	@Override
	public Object getDefaultValue() {
		return LanguageUtils.DATA_UNDEFINED_VALUE;
	}

	@Override
	public Object labelToValue(Object label) {
		return LanguageUtils.parseDirectionLabel((String)label);
	}

	@Override
	public Object valueToLabel(Object value) {
		return LanguageUtils.getDirectionLabel((int)value);
	}

	@Override
	public Object[] getLabelSet() {
		return new Object[]{
				LanguageUtils.DATA_UNDEFINED_LABEL,
				LanguageUtils.DATA_LEFT_LABEL,
				LanguageUtils.DATA_RIGHT_LABEL,
		};
	}

	private static class DependencyDirectionConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 8874429868140453623L;

		public DependencyDirectionConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).getDirection();
		}
	}
}
