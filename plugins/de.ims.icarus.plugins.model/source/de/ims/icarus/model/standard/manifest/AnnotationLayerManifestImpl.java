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
package de.ims.icarus.model.standard.manifest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.AnnotationManifest;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class AnnotationLayerManifestImpl extends AbstractLayerManifest<AnnotationLayerManifest> implements AnnotationLayerManifest {

	private Map<String, AnnotationManifest> annotationManifests;
	private AnnotationManifest defaultAnnotationManifest;
	private boolean deepAnnotation = false;
	private boolean allowUnknownKeys = false;
	private boolean searchable = true;
	private boolean indexable = true;

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.ANNOTATION_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#getAvailableKeys()
	 */
	@Override
	public Set<String> getAvailableKeys() {
		Set<String> keys = null;

		if(annotationManifests!=null) {
			keys = annotationManifests.keySet();
		}

		if(keys==null) {
			keys = Collections.emptySet();
		} else {
			keys = CollectionUtils.getSetProxy(keys);
		}

		return keys;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#getAnnotationManifest(java.lang.String)
	 */
	@Override
	public AnnotationManifest getAnnotationManifest(String key) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$

		if(annotationManifests==null)
			throw new UnsupportedOperationException();

		AnnotationManifest manifest = annotationManifests.get(key);
		if(manifest==null)
			throw new IllegalArgumentException("Unknown annotation key: "+key); //$NON-NLS-1$

		return manifest;
	}

	public void addAnnotationManifest(String key, AnnotationManifest manifest) {
		if (key == null)
			throw new NullPointerException("Invalid key"); //$NON-NLS-1$
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		if(annotationManifests==null) {
			annotationManifests = new LinkedHashMap<>();
		}

		if(annotationManifests.containsKey(key))
			throw new IllegalArgumentException("Duplicate manifest for annotation key: "+key); //$NON-NLS-1$

		annotationManifests.put(key, manifest);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#getDefaultAnnotationManifest()
	 */
	@Override
	public AnnotationManifest getDefaultAnnotationManifest() {
		return defaultAnnotationManifest;
	}

	/**
	 * @param defaultAnnotationManifest the defaultAnnotationManifest to set
	 */
	public void setDefaultAnnotationManifest(
			AnnotationManifest defaultAnnotationManifest) {
		if (defaultAnnotationManifest == null)
			throw new NullPointerException("Invalid defaultAnnotationManifest"); //$NON-NLS-1$

		this.defaultAnnotationManifest = defaultAnnotationManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#isDeepAnnotation()
	 */
	@Override
	public boolean isDeepAnnotation() {
		return deepAnnotation;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#allowUnknownKeys()
	 */
	@Override
	public boolean allowUnknownKeys() {
		return allowUnknownKeys;
	}

	/**
	 * @param deepAnnotation the deepAnnotation to set
	 */
	public void setDeepAnnotation(boolean deepAnnotation) {
		this.deepAnnotation = deepAnnotation;
	}

	/**
	 * @param allowUnknownKeys the allowUnknownKeys to set
	 */
	public void setAllowUnknownKeys(boolean allowUnknownKeys) {
		this.allowUnknownKeys = allowUnknownKeys;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#isIndexable()
	 */
	@Override
	public boolean isIndexable() {
		return indexable;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.AnnotationLayerManifest#isSearchable()
	 */
	@Override
	public boolean isSearchable() {
		return searchable;
	}

	/**
	 * @param searchable the searchable to set
	 */
	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	/**
	 * @param indexable the indexable to set
	 */
	public void setIndexable(boolean indexable) {
		this.indexable = indexable;
	}
}
