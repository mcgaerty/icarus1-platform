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

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencyLemmaContraintFactory extends AbstractConstraintFactory {

	public static final String TOKEN = "lemma"; //$NON-NLS-1$

	public DependencyLemmaContraintFactory() {
		super(TOKEN, NODE_CONSTRAINT_TYPE, "plugins.languageTools.constraints.lemma.name",  //$NON-NLS-1$
				"plugins.languageTools.constraints.lemma.description"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.search_tools.ConstraintFactory#createConstraint(java.lang.Object, net.ikarus_systems.icarus.search_tools.SearchOperator)
	 */
	@Override
	public SearchConstraint createConstraint(Object value,
			SearchOperator operator) {
		return new DependencyLemmaConstraint(value, operator);
	}

	private static class DependencyLemmaConstraint extends DefaultConstraint {

		private static final long serialVersionUID = -2816057046153547371L;

		public DependencyLemmaConstraint(Object value, SearchOperator operator) {
			super(TOKEN, value, operator);
		}

		@Override
		protected Object prepareValue(Object value) {
			return ((DependencyTargetTree)value).getLemma();
		}
	}
}
