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
package de.ims.icarus.model.api.layer;

import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.ManifestOwner;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.api.members.Annotation;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.api.members.Structure;
import de.ims.icarus.util.Collector;

/**
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface AnnotationLayer extends Layer, ManifestOwner<AnnotationLayerManifest> {

	/**
	 * Returns the shared {@code AnnotationLayerManifest} that holds
	 * information about keys and possible values in this annotation.
	 *
	 * @return The manifest that describes this annotation
	 */
	@Override
	AnnotationLayerManifest getManifest();

//	/**
//	 * Returns the annotation for a given markable or {@code null} if that markable
//	 * has not been assigned an annotation value in this layer. Note that the returned
//	 * object can be either an actual value or an {@link Annotation} instance that wraps
//	 * a value and provides further information.
//	 *
//	 * @param markable
//	 * @return
//	 * @throws NullPointerException if the {@code markable} is {@code null}
//	 */
//	Object getValue(Item markable);

	/**
	 * Collects all the keys in this layer which are mapped to valid annotation values for
	 * the given markable. This method returns {@code true} iff at least one key was added
	 * to the supplied {@code buffer}. Note that this method does <b>not</b> take
	 * default annotations into consideration, since they are not accessed via a dedicated
	 * key!
	 *
	 * @param item
	 * @param buffer
	 * @return
	 * @throws NullPointerException if any one of the two arguments is {@code null}
	 * @throws UnsupportedOperationException if this layer does not support additional keys
	 */
	boolean collectKeys(Item item, Collector<String> buffer);

	/**
	 * Returns the annotation for a given markable and key or {@code null} if that markable
	 * has not been assigned an annotation value for the specified key in this layer.
	 * Note that the returned object can be either an actual value or an {@link Annotation}
	 * instance that wraps a value and provides further information.
	 *
	 * @param item
	 * @param key
	 * @return
	 * @throws NullPointerException if either the {@code markable} or {@code key}
	 * is {@code null}
	 * @throws UnsupportedOperationException if this layer does not support additional keys
	 */
	Object getValue(Item item, String key);

	int getIntValue(Item item, String key);
	float getFloatValue(Item item, String key);
	double getDoubleValue(Item item, String key);
	long getLongValue(Item item, String key);
	boolean getBooleanValue(Item item, String key);

	/**
	 * Deletes all annotations in this layer
	 * <p>
	 * Note that this does include all annotations for all keys,
	 * not only those declares for the default annotation.
	 *
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	void removeAllValues();

	/**
	 * Deletes in this layer all annotations for
	 * the given {@code key}.
	 *
	 * @param key The key for which annotations should be
	 * deleted
	 * @throws UnsupportedOperationException if this layer does not allow multiple keys
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	void removeAllValues(String key);

	/**
	 * Removes from this layer all annotations for the given
	 * markable.
	 * <p>
	 * If the {@code recursive} parameter is {@code true} and the supplied
	 * {@code markable} is a {@link Container} or {@link Structure} then all
	 * annotations defined for members of it should be removed as well.
	 *
	 * @param item the {@code Item} for which annotations should be removed
	 * @param recursive if {@code true} removes all annotations defined for
	 * elements ({@code Item}s and {@code Edge}s alike) in the supplied
	 * {@code Item}
	 * @throws NullPointerException if the {@code markable} argument is {@code null}
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	void removeAllValues(Item item, boolean recursive);

//	/**
//	 * Assigns the given {@code value} as new annotation for the specified
//	 * {@code Item}, replacing any previously defined value. If the
//	 * {@code value} argument is {@code null} any stored annotation for the
//	 * {@code markable} will be deleted.
//	 *
//	 * @param markable The {@code Item} to change the annotation value for
//	 * @param value the new annotation value or {@code null} if the annotation
//	 * for the given {@code markable} should be deleted
//	 * @throws NullPointerException if the {@code markable} argument is {@code null}
//	 * @throws IllegalArgumentException if the supplied {@code value} is not
//	 * contained in the {@link ValueSet} of this layer's manifest. This is only
//	 * checked if the manifest actually defines such restrictions.
//	 * @throws UnsupportedOperationException if the corpus
//	 * is not editable
//	 */
//	void setValue(Item markable, Object value);


	/**
	 * Assigns the given {@code value} as new annotation for the specified
	 * {@code Item} and {@code key}, replacing any previously defined value.
	 * If the {@code value} argument is {@code null} any stored annotation
	 * for the combination of {@code markable} and {@code key} will be deleted.
	 * <p>
	 * This is an optional method
	 *
	 * @param item The {@code Item} to change the annotation value for
	 * @param key the key for which the annotation should be changed
	 * @param value the new annotation value or {@code null} if the annotation
	 * for the given {@code markable} and {@code key} should be deleted
	 * @throws UnsupportedOperationException if this layer does not allow multiple keys
	 * @throws NullPointerException if the {@code markable} or {@code key}
	 * argument is {@code null}
	 * @throws IllegalArgumentException if the supplied {@code value} is not
	 * contained in the {@link ValueSet} of this layer's manifest for the given {@code key}.
	 * This is only checked if the manifest actually defines such restrictions.
	 * @throws UnsupportedOperationException if the corpus
	 * is not editable
	 */
	void setValue(Item item, String key, Object value);

	/**
	 *
	 * @return {@code true} iff this layer holds at least one valid annotation object.
	 */
	boolean hasAnnotations();
}