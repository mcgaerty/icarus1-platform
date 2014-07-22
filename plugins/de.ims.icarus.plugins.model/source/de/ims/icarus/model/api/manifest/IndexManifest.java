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
package de.ims.icarus.model.api.manifest;

import java.util.HashMap;
import java.util.Map;

import de.ims.icarus.model.api.driver.IndexUtils;
import de.ims.icarus.model.iql.access.AccessControl;
import de.ims.icarus.model.iql.access.AccessMode;
import de.ims.icarus.model.iql.access.AccessPolicy;
import de.ims.icarus.model.iql.access.AccessRestriction;
import de.ims.icarus.model.xml.XmlResource;

/**
 * Models the description of an indexable mapping in the form of either a
 * direct dependency or a bounding relation. The index maps from elements in
 * the source layer to elements in the target layer.
 *
 * Depending on the layer types of both the source and target layer, very specific
 * data structures are needed to model the actual index.
 *
 * TODO mention fragment layers
 *
 * Note that the indices described by this manifest are only about directly related
 * markable layers, not the contents of annotation layers!
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@AccessControl(AccessPolicy.DENY)
public interface IndexManifest {

	public static final boolean DEFAULT_INCLUDE_REVERSE_VALUE = false;

	@AccessRestriction(AccessMode.READ)
	DriverManifest getDriverManifest();

	@AccessRestriction(AccessMode.READ)
	String getSourceLayerId();

	@AccessRestriction(AccessMode.READ)
	String getTargetLayerId();

	@AccessRestriction(AccessMode.READ)
	Relation getRelation();

	@AccessRestriction(AccessMode.READ)
	Coverage getCoverage();

	@AccessRestriction(AccessMode.READ)
	boolean isIncludeReverse();

//	/**
//	 * If defined, returns the manifest describing the inverse index, or {@code null} otherwise.
//	 * @return
//	 */
//	@AccessRestriction(AccessMode.READ)
//	IndexManifest getInverse();
//
//	/**
//	 * If this manifest describes an inverse index, returns the original index, or {@code null} otherwise.
//	 * @return
//	 */
//	@AccessRestriction(AccessMode.READ)
//	IndexManifest getOriginal();

	// Modification methods

//	void setDriverManifest(DriverManifest driverManifest);
//
//	void setSourceLayerManifest(TargetLayerManifest layerManifest);
//
//	void setTargetLayerManifest(TargetLayerManifest layerManifest);
//
//	void setRelation(Relation relation);
//
//	void setCoverage(Coverage coverage);

	/**
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public enum Relation implements XmlResource {

		/**
		 * Elements from the source and target layer are mapped by one by one. If the
		 * corresponding {@code Coverage} is {@link Coverage#TOTAL_MONOTONIC} this
		 * equals the identity function.
		 */
		ONE_TO_ONE("one-to-one"), //$NON-NLS-1$

		/**
		 * A single element in the source layer may hold an arbitrary number of elements from
		 * the target layer. Typical examples are all kinds of aggregating markable layers that
		 * feature containers as top level elements. Possible lookup structures include span lists
		 * (begin- and end-index for each source element) for source layers that host span elements
		 * and complete content lists (a list of exact target indices) for non-monotonic source
		 * layer members. While span lists are fairly easy to map to memory chunks or arrays, content
		 * lists pose some serious drawbacks, potentially requiring an additional layer of indices to
		 * map source elements to their respective sublist in a data block.
		 * The corresponding index function is injective.
		 */
		ONE_TO_MANY("one-to-many"), //$NON-NLS-1$

		/**
		 * An arbitrary number of (not necessarily monotonic) elements in the source layer map to
		 * a common member of the target layer.
		 *
		 * If the target elements are spans, than an efficient
		 * lookup can be created by dividing the source layer into chunks of markables and then save
		 * for each such chunk the first and last element in the target layer that is truly contained
		 * in this chunk (with respect to its begin- and end-offset). To lookup a target the algorithm
		 * then first determines the correct chunk according to the source elements index and then
		 * performs a binary search on the spans in that chunk to find the target element.
		 * Performance can be controlled by adjusting chunk size to a value that provides a good tradeoff
		 * between required memory space to store the index information and the speed incurred by the
		 * binary search (which serves as a constant cost factor in the performance formula).
		 *
		 * In the case of non-monotonic elements in the target layer (e.g. clusters of source markables)
		 * the above technique fails and it might be required to store a dedicated target index value for
		 * each source element.
		 */
		MANY_TO_ONE("many-to-one"), //$NON-NLS-1$

		/**
		 * As the most complex relation version, this one maps an arbitrary number of source elements to
		 * an again arbitrary number of target elements/containers. As an example imagine entities in the
		 * source layer being grouped into category containers in the target layer, allowing each entity to
		 * be assigned many different categories at once.
		 *
		 * Depending on the container type of the target elements, this version gets easy or very expensive.
		 *
		 * If the target elements are spans, than it is possible to use the strategy proposed for the
		 * {@link #MANY_TO_ONE} relation with a slight addition: When the first target container is found
		 * using binary search within the chunk, then neighbors to both sides are added to the result collection,
		 * until containers are encountered for both ends, that do not contain the source element. The complexity
		 * in this case is limited by the maximum "nesting depth" of spans in the target layer, which remains
		 * to be evaluated as a proper upper bound. Looking in the neighborhood of the first successful match
		 * is possible due to the sorted nature of top-level layer elements and the sorting rules for spans
		 * (span locality).
		 *
		 * For non-monotonic target elements the rules for the {@link #ONE_TO_MANY} relation apply.
		 */
		MANY_TO_MANY("many-to-many"); //$NON-NLS-1$

		private final String xmlForm;

		private Relation(String xmlForm) {
			this.xmlForm = xmlForm;
		}

		/**
		 * Returns the inverse version of the current relation. This method is used to obtain the required
		 * information when an {@code IndexManifest}'s {@link IndexManifest#includeReverse()} method returns
		 * {@code true} and indicates the necessity of an index for the inverse relation.
		 * <p>
		 * Note that {@link #ONE_TO_ONE} and {@link #MANY_TO_MANY} relations do not change when inverted!
		 * @return
		 */
		public Relation invert() {
			switch (this) {
			case MANY_TO_ONE:
				return ONE_TO_MANY;
			case ONE_TO_MANY:
				return MANY_TO_ONE;

			default:
				return this;
			}
		}

		/**
		 * @see de.ims.icarus.model.api.xml.XmlResource#getXmlValue()
		 */
		@Override
		public String getXmlValue() {
			return xmlForm;
		}

		private static Map<String, Relation> xmlLookup;

		public static Relation parseRelation(String s) {
			if(xmlLookup==null) {
				Map<String, Relation> map = new HashMap<>();
				for(Relation type : values()) {
					map.put(type.xmlForm, type);
				}
				xmlLookup = map;
			}

			return xmlLookup.get(s);
		}
	}

	/**
	 * Describes the areal coverage of the target layer's index space. This information
	 * is intended to be exploited by {@code Index} implementations to optimize access
	 * and lookup times. Coverage is expressed as a combination of two boolean flags,
	 * <i>totality</i> and <i>monotonicity</i>:
	 * <p>
	 * <i>Totality</i> means that the available target space is completely covered by the
	 * elements in the source layer (i.e. for every index value in the target layer there
	 * is guaranteed to be at least one matching index value in the source layer, meaning
	 * the index implementation describes an surjective function).
	 * <p>
	 * <i>Monotonicity</i> of an index is given, when for any two distinct source indices the
	 * respective collections of target indices are also distinct (i.e. the index describes
	 * an injective function). In addition the index function preserves order relations on both
	 * index value spaces. For any two source index values {@code j} and {@code k}
	 * with <tt>j &lt; k</tt> the corresponding target {@code IndexSet}s {@code t(j)} and {@code t(k)}
	 * are also ordered <tt>t(j) &lt; t(k)</tt> according to {@link IndexUtils#INDEX_SET_SORTER}.
	 * Note that this means target index collections for continuous source indices are again
	 * a continuous subset of the target value space.
	 *
	 * @author Markus Gärtner
	 * @version $Id$
	 *
	 */
	public enum Coverage implements XmlResource {

		/**
		 * The entire target index space is covered, but the mapped areas might overlap or be
		 * in a somewhat "random" fashion.
		 */
		TOTAL("total", true, false), //$NON-NLS-1$

		/**
		 * No exploitable patterns available in the way of index mapping.
		 */
		PARTIAL("partial", false, false), //$NON-NLS-1$

		/**
		 *
		 */
		MONOTONIC("monotonic", false, true), //$NON-NLS-1$

		/**
		 *
		 */
		TOTAL_MONOTONIC("total-monotonic", true, true), //$NON-NLS-1$
		;

		private final boolean total, monotonic;
		private String xmlForm;

		private Coverage(String xmlForm, boolean total, boolean monotonic) {
			this.xmlForm = xmlForm;
			this.total = total;
			this.monotonic = monotonic;
		}

		/**
		 * <i>Totality</i> means that the available target space is completely covered by the
		 * elements in the source layer (i.e. for every index value in the target layer there
		 * is guaranteed to be at least one matching index value in the source layer, meaning
		 * the index implementation describes an surjective function).
		 *
		 * @return the total
		 */
		public boolean isTotal() {
			return total;
		}

		/**
		 * <i>Monotonicity</i> of an index is given, when for any two distinct source indices the
		 * respective collections of target indices are also distinct (i.e. the index describes
		 * an injective function). In addition the index function preserves order relations on both
		 * index value spaces. For any two source index values {@code j} and {@code k}
		 * with <tt>j &lt; k</tt> the corresponding target {@code IndexSet}s {@code t(j)} and {@code t(k)}
		 * are also ordered <tt>t(j) &lt; t(k)</tt> according to {@link IndexUtils#INDEX_SET_SORTER}.
		 * Note that this means target index collections for continuous source indices are again
		 * a continuous subset of the target value space.
		 *
		 * @return the monotonic
		 */
		public boolean isMonotonic() {
			return monotonic;
		}

		/**
		 * @see de.ims.icarus.model.api.xml.XmlResource#getXmlValue()
		 */
		@Override
		public String getXmlValue() {
			return xmlForm;
		}

		private static Map<String, Coverage> xmlLookup;

		public static Coverage parseCoverage(String s) {
			if(xmlLookup==null) {
				Map<String, Coverage> map = new HashMap<>();
				for(Coverage type : values()) {
					map.put(type.xmlForm, type);
				}
				xmlLookup = map;
			}

			return xmlLookup.get(s);
		}
	}
}