/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.search_tools;



/**
 * Describes and
 * 
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ConstraintFactory {
	
	public static final int EDGE_CONSTRAINT_TYPE = 1;
	public static final int NODE_CONSTRAINT_TYPE = 2;
	
	public static final int IGNORE_CASE = (1 << 0);

	SearchConstraint createConstraint(Object value, SearchOperator operator, int flags);
	
	SearchOperator[] getSupportedOperators();
	
	String getName();
	
	String getDescription();
	
	String getToken();
	
	/**
	 * Returns the class of supported values. This is a hint for editors
	 * or other user interface elements on what kind of component should
	 * be used to present the constraint. If the return value is {@code null}
	 * than only the values returned by {@link #getValueSet()} are considered 
	 * legal!
	 */
	Class<?> getValueClass();
	
	Object getDefaultValue();

	/**
	 * Returns a collection of possible values that should be displayed to the
	 * user when editing the constraint. If {@link #getValueClass()} returns
	 * {@code null} these values are considered to be the only legal collection
	 * of possible values!
	 */	
	Object[] getLabelSet();
	
	/**
	 * Transforms or parses the given {@code label} into a value
	 * suitable for {@code SearchConstraint} objects created by this factory.
	 */
	Object labelToValue(Object label);
	
	/**
	 * Transforms the given {@code value} into a {@code label} object
	 * that can be used for interface elements presented to the user.
	 */
	Object valueToLabel(Object value);
	
	int getConstraintType();
}
