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
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.IndexManifest;
import de.ims.icarus.model.api.manifest.ManifestLocation;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.io.LocationType;
import de.ims.icarus.model.registry.CorpusRegistry;
import de.ims.icarus.model.xml.ModelXmlHandler;
import de.ims.icarus.model.xml.ModelXmlUtils;
import de.ims.icarus.model.xml.XmlSerializer;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class DriverManifestImpl extends AbstractForeignImplementationManifest<DriverManifest> implements DriverManifest {

	private LocationType locationType;
	private final List<IndexManifest> indexManifests = new ArrayList<>();
	private final ContextManifest contextManifest;

	public DriverManifestImpl(ManifestLocation manifestLocation,
			CorpusRegistry registry, ContextManifest contextManifest) {
		super(manifestLocation, registry);

		this.contextManifest = contextManifest;
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#isEmpty()
	 */
	@Override
	protected boolean isEmpty() {
		return super.isEmpty() && indexManifests.isEmpty();
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#writeAttributes(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeAttributes(XmlSerializer serializer) throws Exception {
		super.writeAttributes(serializer);

		if(locationType!=null) {
			serializer.writeAttribute(ATTR_LOCATION_TYPE, locationType.getXmlValue());
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractModifiableManifest#writeElements(de.ims.icarus.model.xml.XmlSerializer)
	 */
	@Override
	protected void writeElements(XmlSerializer serializer) throws Exception {
		super.writeElements(serializer);

		// Write index manifests
		for(IndexManifest indexManifest : indexManifests) {
			ModelXmlUtils.writeIndexElement(serializer, indexManifest);
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#readAttributes(org.xml.sax.Attributes)
	 */
	@Override
	protected void readAttributes(Attributes attributes) {
		super.readAttributes(attributes);

		String locationType = ModelXmlUtils.normalize(attributes, ATTR_LOCATION_TYPE);
		if(locationType!=null) {
			setLocationType(LocationType.parseLocationType(locationType));
		}
	}

	@Override
	public ModelXmlHandler startElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
		switch (qName) {
		case TAG_DRIVER: {
			readAttributes(attributes);
		} break;

		case TAG_INDEX: {
			return new IndexManifestImpl(this);
		}

		case TAG_CONNECTOR: {
			// no-op
		} break;

		default:
			return super.startElement(manifestLocation, uri, localName, qName, attributes);
		}

		return this;
	}

	@Override
	public ModelXmlHandler endElement(ManifestLocation manifestLocation,
			String uri, String localName, String qName, String text)
					throws SAXException {
		switch (qName) {
		case TAG_DRIVER: {
			return null;
		}

		case TAG_CONNECTOR: {
			// no-op
		} break;

		default:
			return super.endElement(manifestLocation, uri, localName, qName, text);
		}

		return this;
	}

	/**
	 * @see de.ims.icarus.model.xml.ModelXmlHandler#endNestedHandler(de.ims.icarus.model.api.manifest.ManifestLocation, java.lang.String, java.lang.String, java.lang.String, de.ims.icarus.model.xml.ModelXmlHandler)
	 */
	@Override
	public void endNestedHandler(ManifestLocation manifestLocation, String uri,
			String localName, String qName, ModelXmlHandler handler)
			throws SAXException {
		switch (qName) {

		case TAG_INDEX: {
			addIndexManifest((IndexManifest) handler);
		} break;

		default:
			super.endNestedHandler(manifestLocation, uri, localName, qName, handler);
			break;
		}
	}

	/**
	 * @see de.ims.icarus.model.standard.manifest.AbstractManifest#xmlTag()
	 */
	@Override
	protected String xmlTag() {
		return TAG_DRIVER;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getContextManifest()
	 */
	@Override
	public ContextManifest getContextManifest() {
		return contextManifest;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.DRIVER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getImplementationManifest()
	 */
	@Override
	public ImplementationManifest getImplementationManifest() {
		ImplementationManifest result = super.getImplementationManifest();
		if(result==null && hasTemplate()) {
			result = getTemplate().getImplementationManifest();
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getIndexManifests()
	 */
	@Override
	public List<IndexManifest> getIndexManifests() {
		List<IndexManifest> result = new ArrayList<>(indexManifests);

		if(hasTemplate()) {
			result.addAll(getTemplate().getIndexManifests());
		}

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#getLocationType()
	 */
	@Override
	public LocationType getLocationType() {
		LocationType result = locationType;
		if(locationType==null && hasTemplate()) {
			result = getTemplate().getLocationType();
		}

		if(locationType==null)
			throw new ModelException(ModelError.MANIFEST_MISSING_LOCATION,
					"No location type available for driver manifest: "+getId()); //$NON-NLS-1$

		return result;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#addIndexManifest(de.ims.icarus.model.api.manifest.IndexManifest)
	 */
//	@Override
	public void addIndexManifest(IndexManifest indexManifest) {
		if (indexManifest == null)
			throw new NullPointerException("Invalid indexManifest");  //$NON-NLS-1$

		if(indexManifests.contains(indexManifest))
			throw new IllegalArgumentException("Duplicate index manifest: "+indexManifest); //$NON-NLS-1$

		indexManifests.add(indexManifest);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#removeIndexManifest(de.ims.icarus.model.api.manifest.IndexManifest)
	 */
//	@Override
	public void removeIndexManifest(IndexManifest indexManifest) {
		if (indexManifest == null)
			throw new NullPointerException("Invalid indexManifest");  //$NON-NLS-1$

		if(!indexManifests.remove(indexManifest))
			throw new IllegalArgumentException("Unknown index manifest: "+indexManifest); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.DriverManifest#setLocationType(de.ims.icarus.model.io.LocationType)
	 */
//	@Override
	public void setLocationType(LocationType locationType) {
		if (locationType == null)
			throw new NullPointerException("Invalid locationType"); //$NON-NLS-1$

		this.locationType = locationType;
	}

}