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

import net.ikarus_systems.icarus.search_tools.SearchConstraint;
import net.ikarus_systems.icarus.search_tools.SearchOperator;
import net.ikarus_systems.icarus.search_tools.standard.AbstractConstraintFactory;
import net.ikarus_systems.icarus.search_tools.standard.DefaultConstraint;
import net.ikarus_systems.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyRelationContraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "relation"; //$NON-NLS-1$

	public DependencyRelationContraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.relation.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.relation.description"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, net.ikarus_systems.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, false))
			return new DependencyRelationCIConstraint(value, operator);
		else
			return new DependencyRelationConstraint(value, operator);
	}

	private static class DependencyRelationConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 1716609613318759367L;

		public DependencyRelationConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).getRelation();
		}

		@Override
		public SearchConstraint clone() {
			return new DependencyRelationConstraint(getValue(), getOperator());
		}
	}

	private static class DependencyRelationCIConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -3611860983057645172L;

		public DependencyRelationCIConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).getRelation().toLowerCase();
		}
	}
}
