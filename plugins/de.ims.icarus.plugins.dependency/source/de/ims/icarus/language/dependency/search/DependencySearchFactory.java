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

import de.ims.icarus.language.dependency.DependencyData;
import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchFactory;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchQuery;
import de.ims.icarus.search_tools.standard.DefaultParameterEditor;
import de.ims.icarus.search_tools.standard.DefaultSearchQuery;
import de.ims.icarus.ui.helper.Editor;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.UnsupportedFormatException;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DependencySearchFactory implements SearchFactory {

	public DependencySearchFactory() {
		// no-op
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createSearch(de.ims.icarus.search_tools.SearchQuery, de.ims.icarus.util.Options)
	 */
	@Override
	public Search createSearch(SearchQuery query, Object target, Options options)
			throws UnsupportedFormatException {
		return new DependencySearch(this, query, options, target);
	}

	public ContentType getContentType() {
		return ContentTypeRegistry.getInstance().getTypeForClass(DependencyData.class);
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createQuery()
	 */
	@Override
	public SearchQuery createQuery() {
		return new DefaultSearchQuery(getContentType());
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#getConstraintContext()
	 */
	@Override
	public ConstraintContext getConstraintContext() {
		return SearchManager.getInstance().getConstraintContext(getContentType());
	}

	/**
	 * @see de.ims.icarus.search_tools.SearchFactory#createParameterEditor()
	 */
	@Override
	public Editor<Options> createParameterEditor() {
		return new DefaultParameterEditor();
	}

}