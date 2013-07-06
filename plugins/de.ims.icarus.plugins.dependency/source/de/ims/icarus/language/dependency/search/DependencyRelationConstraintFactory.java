/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.language.dependency.search;

import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.AbstractConstraintFactory;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.util.Options;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyRelationConstraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "relation"; //$NON-NLS-1$

	public DependencyRelationConstraintFactory() {
		super(TOKEN, EDGE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.relation.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.relation.description"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, de.ims.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator, Options options) {
		if(options.get(SEARCH_CASESENSITIVE, DEFAULT_SEARCH_CASESENSITIVE))
			return new DependencyRelationConstraint(value, operator);
		else
			return new DependencyRelationCIConstraint(value, operator);
	}

	private static class DependencyRelationConstraint extends DefaultConstraint {

		private static final long serialVersionUID = 1716609613318759367L;

		public DependencyRelationConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		public Object getInstance(Object value) {
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
		public Object getInstance(Object value) {
			return ((DependencyTargetTree)value).getRelation().toLowerCase();
		}

		@Override
		public DependencyRelationCIConstraint clone() {
			return new DependencyRelationCIConstraint(getValue(), getOperator());
		}
	}
}
