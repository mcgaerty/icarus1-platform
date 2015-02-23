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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.ims.icarus.model.ModelError;
import de.ims.icarus.model.ModelException;
import de.ims.icarus.model.api.layer.LayerType;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.standard.manifest.Links.Link;
import de.ims.icarus.model.standard.manifest.Links.MemoryLink;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractLayerManifest<L extends LayerManifest> extends AbstractMemberManifest<L> implements LayerManifest {

	private final LayerGroupManifest layerGroupManifest;
	private final List<TargetLayerManifest> baseLayerManifests = new ArrayList<>(3);
	private LayerTypeLink layerType;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	protected AbstractLayerManifest(ManifestLocation manifestLocation,
			CorpusRegistry registry, LayerGroupManifest layerGroupManifest) {
		super(manifestLocation, registry);

		verifyEnvironment(manifestLocation, layerGroupManifest, LayerGroupManifest.class);

		this.layerGroupManifest = layerGroupManifest;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#isEmpty()
	 */
	@Override
	protected boolean isEmpty() {
		return super.isEmpty() && baseLayerManifests.isEmpty();
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#writeAttributes(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		// Write layer type
		if(layerType!=null) {
			serializer.writeAttribute(ATTR_LAYER_TYPE, layerType.getId());
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractModifiableManifest#writeElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		// Write base layers
		for(TargetLayerManifest layerManifest : baseLayerManifests) {
			ModelXmlUtils.writeTargetLayerManifestElement(serializer, TAG_BASE_LAYER, layerManifest);
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		String layerTypeId = ModelXmlUtils.normalize(attributes, ATTR_LAYER_TYPE);
		if(layerTypeId!=null) {
			setLayerTypeId(layerTypeId);
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#startElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		switch (qName) {
		case TAG_BASE_LAYER: {
			String baseLayerId = ModelXmlUtils.normalize(attributes, ATTR_LAYER_ID);
			addBaseLayerId(baseLayerId);
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#endElement(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
			throws SAXException {
		switch (qName) {
		case TAG_BASE_LAYER: {
			// no-op
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerManifest#getContextManifest()
	 */
	@Override
	public ContextManifest getContextManifest() {
		return layerGroupManifest==null ? null : layerGroupManifest.getContextManifest();
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerManifest#getGroupManifest()
	 */
	@Override
	public LayerGroupManifest getGroupManifest() {
		return layerGroupManifest;
	}

//	/**
//	 * @see de.ims.icarus.model.api.manifest.LayerManifest#setGroupManifest(de.ims.icarus.model.api.manifest.LayerGroupManifest)
//	 */
//	@Override
//	public void setGroupManifest(LayerGroupManifest layerGroupManifest) {
//		if (layerGroupManifest == null)
//			throw new NullPointerException("Invalid layerGroupManifest"); //$NON-NLS-1$
//
//		this.layerGroupManifest = layerGroupManifest;
//	}

	/**
	 * @param layerType the layerType to set
	 */
//	@Override
	public void setLayerTypeId(String layerTypeId) {
		layerType = new LayerTypeLink(layerTypeId);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerManifest#getLayerType()
	 */
	@Override
	public LayerType getLayerType() {
		return layerType==null ? null : layerType.get();
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.LayerManifest#getBaseLayerManifests()
	 */
	@Override
	public List<TargetLayerManifest> getBaseLayerManifests() {
		return CollectionUtils.getListProxy(baseLayerManifests);
	}

//	@Override
//	public void addBaseLayerManifest(TargetLayerManifest manifest) {
//		if (manifest == null)
//			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$
//
//		baseLayerManifests.remove(manifest);
//		baseLayerManifests.add(manifest);
//	}

//	@Override
//	public void removeBaseLayerManifest(TargetLayerManifest manifest) {
//		if (manifest == null)
//			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$
//
//		baseLayerManifests.remove(manifest);
//	}

//	protected TargetLayerManifest createTargetLayer(String targetId, boolean localOnly) {
//		TargetLayerManifestImpl targetLayerManifest = new TargetLayerManifestImpl(targetId);
//
////		ContextManifest contextManifest = getContextManifest();
////
////		if(!localOnly && contextManifest!=null) {
////			targetLayerManifest.setPrerequisiteManifest(contextManifest.getPrerequisite(targetId));
////		}
//
//		return targetLayerManifest;
//	}

//	protected boolean isLocalGroup() {
//		LayerGroupManifest groupManifest = this.layerGroupManifest;
//		if(groupManifest==null || !groupManifest.isIndependent()) {
//			return false;
//		}
//
//		ContextManifest contextManifest = groupManifest.getContextManifest();
//
//		return contextManifest!=null && contextManifest.isIndependentContext();
//	}

	protected void checkAllowsTargetLayer() throws ModelException {
		if(layerGroupManifest==null || layerGroupManifest.getContextManifest()==null)
			throw new ModelException(ModelError.MANIFEST_MISSING_ENVIRONMENT,
					"Cannot make links to other layers without enclosing context: "+getId()); //$NON-NLS-1$
	}

	public TargetLayerManifest addBaseLayerId(String baseLayerId) {
		checkAllowsTargetLayer();
		TargetLayerManifest targetLayerManifest = new TargetLayerManifestImpl(baseLayerId);
		baseLayerManifests.add(targetLayerManifest);
		return targetLayerManifest;
	}

	protected class LayerLink extends Link<LayerManifest> {

		/**
		 * @param id
		 */
		public LayerLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.Links.Link#resolve()
		 */
		@Override
		protected LayerManifest resolve() {
			// This takes care of layer id resolution in terms of prerequisite aliases
			return getContextManifest().getLayerManifest(getId());
		}

	}

	protected class PrerequisiteLink extends MemoryLink<PrerequisiteManifest> {

		/**
		 * @param id
		 */
		public PrerequisiteLink(String id) {
			super(id);
		}

		/**
		 * @see de.ims.icarus.model.standard.manifest.Links.Link#resolve()
		 */
		@Override
		protected PrerequisiteManifest resolve() {
			return getContextManifest().getPrerequisite(getId());
		}

	}

	public class TargetLayerManifestImpl implements TargetLayerManifest {

//		private PrerequisiteManifest prerequisiteManifest;
		private LayerLink resolvedLayer;
		private PrerequisiteLink prerequisite;

		public TargetLayerManifestImpl(String targetId) {
			resolvedLayer = new LayerLink(targetId);
			prerequisite = new PrerequisiteLink(targetId);
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return getLayerId().hashCode();
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof TargetLayerManifest) {
				TargetLayerManifest other = (TargetLayerManifest) obj;
				return getLayerId().equals(other.getLayerId());
			}
			return false;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "TargetLayerManifest@"+getLayerId(); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest#getLayerId()
		 */
		@Override
		public String getLayerId() {
			return resolvedLayer.getId();
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest#getLayerManifest()
		 */
		@Override
		public LayerManifest getLayerManifest() {
			return AbstractLayerManifest.this;
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest#getPrerequisite()
		 */
		@Override
		public PrerequisiteManifest getPrerequisite() {
//			return getContextManifest().getPrerequisite(resolvedLayer.getId());
			return prerequisite.get();
		}

		/**
		 * @see de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest#getResolvedLayerManifest()
		 */
		@Override
		public LayerManifest getResolvedLayerManifest() {
			return resolvedLayer.get();
		}

//		/**
//		 * @param prerequisiteManifest the prerequisiteManifest to set
//		 */
//		public void setPrerequisiteManifest(PrerequisiteManifest prerequisiteManifest) {
//			this.prerequisiteManifest = prerequisiteManifest;
//		}
	}
}