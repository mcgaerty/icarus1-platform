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
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.search_tools.corpus;

import javax.swing.event.ChangeListener;

import de.ims.icarus.language.AvailabilityObserver;
import de.ims.icarus.language.DataType;
import de.ims.icarus.language.SentenceData;
import de.ims.icarus.language.SentenceDataList;
import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.ConstraintFactory;
import de.ims.icarus.search_tools.Search;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.annotation.AnnotationBuffer;
import de.ims.icarus.search_tools.result.ResultEntry;
import de.ims.icarus.search_tools.result.SearchResult;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.util.CompactProperties;
import de.ims.icarus.util.SubstitutionSupport;
import de.ims.icarus.util.annotation.AnnotatedData;
import de.ims.icarus.util.data.ContentType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractCorpusSearchResult implements SearchResult, SentenceDataList {
	
	protected transient final Search search;
	
	protected String[] groupTokens;
	protected SubstitutionSupport[] groupInstances;
	protected SearchConstraint[] groupConstraints;
	protected int[] groupIndexMap;
	
	protected CompactProperties properties;
	
	protected AnnotationBuffer annotationBuffer;
	
	protected boolean finalized = false;
	
	protected final Object lock = new Object();

	protected AbstractCorpusSearchResult(Search search, SearchConstraint[] groupConstraints) {
		/*if(search==null)
			throw new IllegalArgumentException("Invalid search"); //$NON-NLS-1$*/
		/*if(!(descriptor.getTarget() instanceof SentenceDataList))
			throw new IllegalArgumentException("Invalid target: "+descriptor.getTarget()); //$NON-NLS-1$*/
		
		this.search = search;
		this.groupConstraints = SearchUtils.cloneSimple(groupConstraints);
		
		if(groupConstraints!=null && groupConstraints.length>0) {
			int size = groupConstraints.length;
			groupTokens = new String[size];
			groupInstances = new SubstitutionSupport[size];
			
			int maxIndex = 0;
			
			// Init token and instance mapping and find max group index
			for(int i=0; i<size; i++) {
				groupTokens[i] = groupConstraints[i].getToken();
				groupInstances[i] = new SubstitutionSupport();
				maxIndex = Math.max(maxIndex, (int)groupConstraints[i].getValue());
			}
			
			groupIndexMap = new int[maxIndex+1];

			// Fill reverse mapping from group id to id 
			// in current list of group constraints
			for(int i=0; i<size; i++) {
				int index = (int) groupConstraints[i].getValue();
				groupIndexMap[index] = i;
			}
		}
	}

	@Override
	public Object getProperty(String key) {
		return properties==null ? null : properties.get(key);
	}

	@Override
	public void setProperty(String key, Object value) {
		if(properties==null) {
			properties = new CompactProperties();
		}
		properties.put(key, value);
	}

	public AnnotationBuffer getAnnotationBuffer() {
		return annotationBuffer;
	}

	public void setAnnotationBuffer(AnnotationBuffer annotationBuffer) {
		this.annotationBuffer = annotationBuffer;
	}

	public SentenceDataList getTarget() {
		return (SentenceDataList) SearchManager.getTarget(search);
	}

	@Override
	public boolean reorder(int[] permutation) {
		// Per default we do not support reordering
		return false;
	}

	@Override
	public boolean canReorder() {
		return false;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getDimension()
	 */
	@Override
	public int getDimension() {
		return groupConstraints==null ? 0 : groupConstraints.length;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getSource()
	 */
	@Override
	public Search getSource() {
		return search;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getInstanceCount(int)
	 */
	@Override
	public int getInstanceCount(int groupId) {
		return groupInstances[groupId].size();
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getGroupConstraint(int)
	 */
	@Override
	public SearchConstraint getGroupConstraint(int groupId) {
		return groupConstraints[groupId];
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getContentType()
	 */
	@Override
	public ContentType getContentType() {
		return getTarget().getContentType();
	}
	
	public ConstraintContext getContext() {
		return search==null ? null : search.getFactory().getConstraintContext();
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getGroupLabel(int)
	 */
	@Override
	public Object getGroupLabel(int groupId) {
		String token = groupTokens[groupId];
		ConstraintFactory factory = getContext().getFactory(token);
		return factory.getName();
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getInstanceLabel(int, int)
	 */
	@Override
	public Object getInstanceLabel(int groupId, int index) {
		return groupInstances[groupId].resubstitute(index);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getIndexOf(int, java.lang.Object)
	 */
	@Override
	public int getIndexOf(int groupId, Object label) {
		return groupInstances[groupId].getSubstitution((String) label);
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getSubResult(int[])
	 */
	@Override
	public SearchResult getSubResult(int... groupInstances) {
		// Per default there is no support for sub-results
		return null;
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#isFinal()
	 */
	@Override
	public boolean isFinal() {
		return finalized;
	}
	
	public void finish() {
		if(finalized)
			throw new IllegalStateException("Result is already final!"); //$NON-NLS-1$
		
		finalized = true;
	}
	
	public Object getPlainEntry(ResultEntry entry) {
		return getTarget().get(entry.getIndex());
	}

	/**
	 * @see de.ims.icarus.search_tools.result.SearchResult#getAnnotatedEntry(de.ims.icarus.search_tools.result.ResultEntry)
	 */
	@Override
	public AnnotatedData getAnnotatedEntry(ResultEntry entry) {
		return annotationBuffer==null ? null : annotationBuffer.getAnnotatedData(entry);
	}

	@Override
	public ContentType getAnnotationType() {
		return annotationBuffer==null ? null : annotationBuffer.getAnnotationType();
	}

	@Override
	public int size() {
		return getTotalMatchCount();
	}

	@Override
	public SentenceData get(int index) {
		return get(index, DataType.SYSTEM, null);
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		// no-op
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		// no-op
	}

	@Override
	public boolean supportsType(DataType type) {
		return getTarget().supportsType(type);
	}

	@Override
	public SentenceData get(int index, DataType type) {
		return get(index, type, null);
	}

	@Override
	public SentenceData get(int index, DataType type,
			AvailabilityObserver observer) {
		ResultEntry entry = getRawEntry(index);
		return getTarget().get(entry.getIndex(), type, observer);
	}
}
