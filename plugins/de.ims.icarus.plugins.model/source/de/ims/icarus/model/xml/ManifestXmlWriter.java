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
package de.ims.icarus.model.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.rowset.spi.XmlWriter;
import javax.swing.Icon;

import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.model.api.layer.LayerType;
import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.AnnotationManifest;
import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.CorpusManifest;
import de.ims.icarus.model.api.manifest.Derivable;
import de.ims.icarus.model.api.manifest.DriverManifest;
import de.ims.icarus.model.api.manifest.FragmentLayerManifest;
import de.ims.icarus.model.api.manifest.ImplementationManifest;
import de.ims.icarus.model.api.manifest.ImplementationManifest.SourceType;
import de.ims.icarus.model.api.manifest.IndexManifest;
import de.ims.icarus.model.api.manifest.LayerGroupManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.LayerManifest.TargetLayerManifest;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;
import de.ims.icarus.model.api.manifest.ModifiableManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.model.api.manifest.RasterizerManifest;
import de.ims.icarus.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.model.api.manifest.StructureManifest;
import de.ims.icarus.model.api.manifest.ValueManifest;
import de.ims.icarus.model.api.manifest.ValueRange;
import de.ims.icarus.model.api.manifest.ValueSet;
import de.ims.icarus.model.io.LocationType;
import de.ims.icarus.model.util.ValueType;
import de.ims.icarus.util.ClassUtils;
import de.ims.icarus.util.collections.CollectionUtils;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ManifestXmlWriter implements ModelXmlTags, ModelXmlAttributes {

	private final XmlSerializer serializer;
	private final boolean templateMode;
	private final String rootTag;

	public ManifestXmlWriter(XmlSerializer serializer, boolean templateMode) {
		if (serializer == null)
			throw new NullPointerException("Invalid serializer"); //$NON-NLS-1$

		this.serializer = serializer;
		this.templateMode = templateMode;

		rootTag = templateMode ? TAG_TEMPLATES : TAG_CORPORA;
	}

	private int depth = 0;

	// Called for all manifests except the top level corpus manifests!
	private void startManifest(String tag) throws Exception {
		if(depth==0 && !templateMode && !TAG_CORPUS.equals(tag))
			throw new IllegalStateException("Only '"+TAG_CORPUS+"' elements allowed on top level outside template mode: "+tag); //$NON-NLS-1$ //$NON-NLS-2$

		depth++;

		serializer.startElement(tag);
	}

	private void endManifest(String tag) throws Exception {
		depth--;

		serializer.endElement(tag);
	}

	private void checkTemplate(Derivable derivable) {
		if (derivable == null)
			throw new NullPointerException("Invalid derivable"); //$NON-NLS-1$

		if(depth==0 && derivable.isTemplate()!=templateMode)
			throw new IllegalArgumentException("Illegal element (template mode mismatch): "+derivable.getId()); //$NON-NLS-1$
	}

	public void beginDocument() throws Exception {
		serializer.startDocument();
		serializer.startElement(rootTag);
	}

	public void endDocument() throws Exception {
		serializer.endElement(rootTag);
		serializer.endDocument();
		serializer.close();
	}

	private boolean tryWrite(Object object) throws Exception {
		if(object==null) {
			return true;
		}
		if(object instanceof XmlElement) {
			((XmlElement)object).writeXml(serializer);
			return true;
		}

		return false;
	}

	/**
	 * Returns the value to be serialized if a template value is given.
	 */
	private static <T extends Object> T diff(T value, T template) {
		return (value!=null && !value.equals(template)) ? value : null;
	}


	//**************************************************
	//		MANIFEST PARTS USABLE FOR TEMPLATING
	//**************************************************

	/**
	 * Write id, name, description and icon from the given identity after
	 * making a diff check against the optional template identity.
	 */
	private void writeIdentityAttributes(Identity identity) throws Exception {

		String id = identity.getId();
		String name = identity.getName();
		String description = identity.getDescription();
		Icon icon = identity.getIcon();

		Identity template = null;
		if(identity instanceof Derivable) {
			template = (Identity) ((Derivable)identity).getTemplate();
		}

		if(template!=null) {
			id = diff(id, template.getId());
			name = diff(name, template.getName());
			description = diff(description, template.getDescription());
			icon = diff(icon, template.getIcon());
		}

		serializer.writeAttribute(ATTR_ID, id);
		serializer.writeAttribute(ATTR_NAME, name);
		serializer.writeAttribute(ATTR_DESCRIPTION, description);

		if(icon instanceof XmlResource) {
			serializer.writeAttribute(ATTR_ICON, ((XmlResource)icon).getValue());
		} else if(icon != null) {
			LoggerFactory.warning(XmlWriter.class, "Skipping serialization of icon for identity: "+identity); //$NON-NLS-1$
		}
	}

	/**
	 * Write template id if the given derivable is depending on a template
	 */
	private void writeTemplateAttribute(Derivable derivable) throws Exception {
		Derivable template = derivable.getTemplate();

		if(template!=null) {
			serializer.writeAttribute(ATTR_TEMPLATE_ID, template.getId());
		}
	}

	/**
	 * Write options manifest and properties, after making diff check against template.
	 */
	private void writeModifiableManifest(ModifiableManifest manifest) throws Exception {
		ModifiableManifest template = (ModifiableManifest) manifest.getTemplate();

		OptionsManifest optionsManifest = manifest.getOptionsManifest();

		Set<String> properties = manifest.getPropertyNames();

		if(template!=null) {
			serializer.writeAttribute(ATTR_TEMPLATE_ID, template.getId());

			optionsManifest = diff(optionsManifest, template.getOptionsManifest());

			properties = new HashSet<>(properties);

			for(Iterator<String> it = properties.iterator(); it.hasNext();) {
				String property = it.next();

				Object value = diff(manifest.getProperty(property), template.getProperty(property));
				if(value==null) {
					it.remove();
				}
			}
		}

		if(optionsManifest!=null) {
			writeOptionsManifest(optionsManifest);
		}

		if(!properties.isEmpty()) {
			List<String> names = CollectionUtils.asSortedList(properties);

			for(String name : names) {
				ValueType type = optionsManifest==null ?
						ValueType.STRING : optionsManifest.getValueType(name);

				writePropertyElement(name, manifest.getProperty(name), type);
			}
		}
	}

	private String getSerializedForm(TargetLayerManifest manifest) {
		return manifest.getPrerequisite()!=null ?
				manifest.getPrerequisite().getAlias()
				: manifest.getResolvedLayerManifest().getId();
	}

	/**
	 * Writes the given target layer. Uses as layer id the alias of the prerequisite of the
	 * link if present, or the resolved layer's id otherwise.
	 */
	private void writeTargetLayerManifestElement(String name, TargetLayerManifest manifest) throws Exception {
		if(manifest==null) {
			return;
		}

		//FIXME empty element
		serializer.startElement(name);

		// ATTRIBUTES
		serializer.writeAttribute("layer-id", getSerializedForm(manifest)); //$NON-NLS-1$

		serializer.endElement(name);
	}

	private void writeLayerTypeAttribute(LayerManifest manifest) throws Exception {
		LayerType layerType = manifest.getLayerType();
		if(layerType!=null && manifest.getTemplate()!=null) {
			layerType = diff(layerType, ((LayerManifest)manifest.getTemplate()).getLayerType());
		}

		// ATTRIBUTES
		if(layerType!=null) {
			serializer.writeAttribute(ATTR_LAYER_TYPE, layerType.getId());
		}
	}

	private void writeAnnotationKeyAttribute(FragmentLayerManifest manifest) throws Exception {
		String annotationKey = manifest.getAnnotationKey();
		if(annotationKey!=null && manifest.getTemplate()!=null) {
			annotationKey = diff(annotationKey, ((FragmentLayerManifest)manifest.getTemplate()).getAnnotationKey());
		}

		// ATTRIBUTES
		if(annotationKey!=null) {
			serializer.writeAttribute(ATTR_ANNOTATION_KEY, annotationKey);
		}
	}

	private void writeLocationAttribute(DriverManifest manifest) throws Exception {
		LocationType locationType = manifest.getLocationType();
		if(locationType!=null && manifest.getTemplate()!=null) {
			locationType = diff(locationType, ((DriverManifest)manifest.getTemplate()).getLocationType());
		}

		// ATTRIBUTES
		if(locationType!=null) {
			serializer.writeAttribute(ATTR_LOCATION_TYPE, locationType.getValue());
		}
	}

	private void writeBaseLayerElements(LayerManifest manifest) throws Exception {
		List<TargetLayerManifest> layers = manifest.getBaseLayerManifests();
		LayerManifest template = (LayerManifest) manifest.getTemplate();

		if(template!=null && !template.getBaseLayerManifests().isEmpty()) {
			layers = new ArrayList<>();
			layers.removeAll(template.getBaseLayerManifests());
		}

		// ELEMENTS
		for(TargetLayerManifest targetLayerManifest : layers) {
			writeTargetLayerManifestElement(TAG_BASE_LAYER, targetLayerManifest);
		}
	}

	private void writeContainerManifestElement(ContainerManifest manifest) throws Exception {
		if(manifest==null) {
			return;
		}

		serializer.startElement(TAG_CONTAINER);

		// ATTRIBUTES
		writeIdentityAttributes(manifest);
		serializer.writeAttribute(ATTR_CONTAINER_TYPE, manifest.getContainerType().getValue());

		// ELEMENTS
		writeModifiableManifest(manifest);

		serializer.endElement(TAG_CONTAINER);
	}

	private void writeStructureManifestElement(StructureManifest manifest) throws Exception {
		if(manifest==null) {
			return;
		}

		serializer.startElement(TAG_STRUCTURE);

		// ATTRIBUTES
		writeIdentityAttributes(manifest);
		serializer.writeAttribute(ATTR_CONTAINER_TYPE, manifest.getContainerType().getValue());
		serializer.writeAttribute(ATTR_STRUCTURE_TYPE, manifest.getStructureType().getValue());

		StructureManifest template = (StructureManifest) manifest.getTemplate();
		boolean multiRoot = manifest.isMultiRootAllowed();
		if(multiRoot && template!=null && !template.isMultiRootAllowed()) {
			serializer.writeAttribute(ATTR_MULTI_ROOT, true);
		}

		// ELEMENTS
		writeModifiableManifest(manifest);

		serializer.endElement(TAG_STRUCTURE);
	}

	private void writeBoundaryLayerElement(MarkableLayerManifest manifest) throws Exception {

		TargetLayerManifest boundaryLayer = manifest.getBoundaryLayerManifest();
		if(boundaryLayer!=null && manifest.getTemplate()!=null) {
			boundaryLayer = diff(boundaryLayer, ((MarkableLayerManifest)manifest.getTemplate()).getBoundaryLayerManifest());
		}

		writeTargetLayerManifestElement(TAG_BOUNDARY_LAYER, boundaryLayer);
	}

	private void writeValueLayerElement(FragmentLayerManifest manifest) throws Exception {

		TargetLayerManifest valueLayer = manifest.getBoundaryLayerManifest();
		if(valueLayer!=null && manifest.getTemplate()!=null) {
			valueLayer = diff(valueLayer, ((FragmentLayerManifest)manifest.getTemplate()).getValueLayerManifest());
		}

		writeTargetLayerManifestElement(TAG_VALUE_LAYER, valueLayer);
	}

	private void writeNestedContainerElements(MarkableLayerManifest manifest) throws Exception {
		if(manifest==null) {
			return;
		}

		for(int i=0; i<manifest.getContainerDepth(); i++) {
			ContainerManifest container = manifest.getContainerManifest(i);

			if(container.getManifestType()==ManifestType.STRUCTURE_MANIFEST) {
				writeStructureManifestElement((StructureManifest) container);
			} else {
				writeContainerManifestElement(container);
			}
		}
	}

	private void writeMarkableLayerManifest0(String name, MarkableLayerManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(name);

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);
		writeLayerTypeAttribute(manifest);

		// ELEMENTS
		writeModifiableManifest(manifest);

		// Write base layers
		writeBaseLayerElements(manifest);

		// Write boundary layer
		writeBoundaryLayerElement(manifest);

		// Write containers
		writeNestedContainerElements(manifest);

		endManifest(name);
	}


	//**************************************************
	//		STATIC MANIFEST PARTS
	//**************************************************

	private void writeLayerGroupElement(LayerGroupManifest manifest) throws Exception {
		//TODO
	}

	private void writeIndexElement(IndexManifest manifest) throws Exception {

		// Never serialize inverted manifests, they are created by the parser!
		if(manifest.getOriginal()!=null) {
			return;
		}

		serializer.startElement(TAG_INDEX);

		serializer.writeAttribute(ATTR_SOURCE_LAYER, getSerializedForm(manifest.getSourceLayerManifest()));
		serializer.writeAttribute(ATTR_TARGET_LAYER, getSerializedForm(manifest.getTargetLayerManifest()));
		serializer.writeAttribute(ATTR_RELATION, manifest.getRelation().getValue());
		serializer.writeAttribute(ATTR_COVERAGE, manifest.getCoverage().getValue());
		if(manifest.getInverse()!=null) {
			serializer.writeAttribute(ATTR_INCLUDE_REVERSE, true);
		}

		serializer.endElement(TAG_INDEX);
	}

	private void writeOptionElement(String option, OptionsManifest manifest) throws Exception {

		ValueType type = manifest.getValueType(option);

		serializer.startElement(TAG_OPTION);

		serializer.writeAttribute(ATTR_ID, option);
		serializer.writeAttribute(ATTR_TYPE, getSerializedForm(type));
		serializer.writeAttribute(ATTR_NAME, manifest.getName(option));
		serializer.writeAttribute(ATTR_DESCRIPTION, manifest.getDescription(option));
		if(!manifest.isPublished(option)) {
			serializer.writeAttribute(ATTR_PUBLISHED, false);
		}
		if(manifest.isMultiValue(option)) {
			serializer.writeAttribute(ATTR_MULTI_VALUE, true);
		}
		serializer.writeAttribute(ATTR_GROUP, manifest.getOptionGroup(option));

		writeValueElement(TAG_DEFAULT_VALUE, manifest.getDefaultValue(option), type);
		writeValuesElement(manifest.getSupportedValues(option), type);
		writeValueRangeElement(manifest.getSupportedRange(option), type);

		serializer.endElement(TAG_OPTION);
	}

	private void writeValueElement(String name, Object value, ValueType type) throws Exception {
		if(value==null) {
			return;
		}

		serializer.startElement(name);

		switch (type) {
		case UNKNOWN:
			throw new IllegalArgumentException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		case CUSTOM:
			throw new IllegalArgumentException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		default:
			if(value instanceof XmlElement) {
				((XmlElement)value).writeXml(serializer);
			} else {
				serializer.writeText(String.valueOf(value));
			}
			break;
		}
		serializer.endElement(name);
	}

	private String getSerializedForm(ValueType type) {
		return type==ValueType.STRING ? null : type.getValue();
	}

	private void writePropertyElement(String name, Object value, ValueType type) throws Exception {
		if(value==null) {
			return;
		}

		serializer.startElement(TAG_PROPERTY);

		switch (type) {
		case UNKNOWN:
			throw new IllegalArgumentException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		case CUSTOM:
			throw new IllegalArgumentException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		default:
			serializer.writeAttribute(ATTR_NAME, name);
			serializer.writeAttribute(ATTR_TYPE, getSerializedForm(type));
			serializer.writeText(String.valueOf(value));
			break;
		}
		serializer.endElement(TAG_PROPERTY);
	}

	private void writeValueRangeElement(ValueRange range, ValueType type) throws Exception {
		if(range==null) {
			return;
		}

		serializer.startElement(TAG_RANGE);
		serializer.writeAttribute(ATTR_INCLUDE_MIN, range.isLowerBoundInclusive());
		serializer.writeAttribute(ATTR_INCLUDE_MAX, range.isUpperBoundInclusive());
		writeValueElement(TAG_MIN, range.getLowerBound(), type);
		writeValueElement(TAG_MAX, range.getUpperBound(), type);
		serializer.endElement(TAG_RANGE);
	}

	private void writeValuesElement(ValueSet values, ValueType type) throws Exception {
		if(values==null) {
			return;
		}

		serializer.startElement(TAG_VALUES);
		for(int i=0; i<values.valueCount(); i++) {
			Object value = values.getValueAt(i);

			if(value instanceof ValueManifest) {
				writeValueManifestElement((ValueManifest) value, type);
			} else {
				writeValueElement(TAG_VALUE, value, type);
			}
		}
		serializer.endElement(TAG_VALUES);
	}

	private void writeValueManifestElement(ValueManifest manifest, ValueType type) throws Exception {

		serializer.startElement(TAG_VALUE);
		serializer.writeAttribute(ATTR_NAME, manifest.getName());
		serializer.writeAttribute(ATTR_DESCRIPTION, manifest.getDescription());

		Object value = manifest.getValue();

		switch (type) {
		case UNKNOWN:
			throw new IllegalArgumentException("Cannot serialize unknown value: "+value); //$NON-NLS-1$
		case CUSTOM:
			throw new IllegalArgumentException("Cannot serialize custom value: "+value); //$NON-NLS-1$

		default:
			serializer.writeText(String.valueOf(value));
			break;
		}

		serializer.endElement(TAG_VALUE);
	}


	//**************************************************
	//		PUBLIC MANIFEST SERIALIZATION
	//**************************************************

	public void writeCorpusManifest(CorpusManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_CORPUS);

		// Write default stuff
		writeIdentityAttributes(manifest);
		writeModifiableManifest(manifest);

		// Root context and editable flag
		serializer.writeAttribute(ATTR_ROOT_CONTEXT, manifest.getRootContextManifest().getId());
		serializer.writeAttribute(ATTR_EDITABLE, manifest.isEditable());

		// Now write all context manifests
		writeContextManifest(manifest.getRootContextManifest());
		for(ContextManifest contextManifest : manifest.getCustomContextManifests()) {
			writeContextManifest(contextManifest);
		}

		endManifest(TAG_CORPUS);
	}

	public void writeContextManifest(ContextManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_CONTEXT);

		ContextManifest template = (ContextManifest) manifest.getTemplate();

		// Write default stuff
		writeIdentityAttributes(manifest);

		// Write independent flag
		if (manifest.isIndependentContext() && template!=null
				&& !template.isIndependentContext()) {
			serializer.writeAttribute(ATTR_INDEPENDENT, true);
		}

		writeModifiableManifest(manifest);

		//TODO write layer groups, driver, prerequisites and stuff

		endManifest(TAG_CONTEXT);
	}

	public void writeOptionsManifest(OptionsManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_OPTIONS);

		// ATTRIBUTES
		serializer.writeAttribute(ATTR_ID, manifest.getId());

		OptionsManifest template = (OptionsManifest) manifest.getTemplate();

		Set<String> options = manifest.getOptionNames();
		Set<Identity> groups = manifest.getGroupIdentifiers();

		// Make diff between current manifest and template
		if(template!=null) {
			serializer.writeAttribute(ATTR_TEMPLATE_ID, template.getId());

			options = new HashSet<>(options);
			groups = new HashSet<>(groups);

			for(String option : template.getOptionNames()) {
				if(options.contains(option)
						&& ClassUtils.equals(manifest.getName(option), template.getName(option))
						&& ClassUtils.equals(manifest.getDescription(option), template.getDescription(option))
						&& ClassUtils.equals(manifest.getDefaultValue(option), template.getDefaultValue(option))
						&& ClassUtils.equals(manifest.getValueType(option), template.getValueType(option))
						&& ClassUtils.equals(manifest.getSupportedValues(option), template.getSupportedValues(option))
						&& ClassUtils.equals(manifest.getSupportedRange(option), template.getSupportedRange(option))
						&& ClassUtils.equals(manifest.getOptionGroup(option), template.getOptionGroup(option))
						&& manifest.isPublished(option)==template.isPublished(option)
						&& manifest.isMultiValue(option)==template.isMultiValue(option)) {

					options.remove(option);
				}
			}

			groups.removeAll(template.getGroupIdentifiers());
		}

		// Write options in alphabetic order
		if(!options.isEmpty()) {
			List<String> names = CollectionUtils.asSortedList(options);

			for(String option : names) {
				writeOptionElement(option, manifest);
			}
		}

		// Write groups in alphabetic order
		if(!groups.isEmpty()) {
			List<Identity> idents = CollectionUtils.asSortedList(groups, Identity.COMPARATOR);

			for(Identity group : idents) {
				//FIXME empty element
				serializer.startElement(TAG_GROUP);

				// ATTRIBUTES
				writeIdentityAttributes(group);
				serializer.endElement(TAG_GROUP);
			}
		}

		endManifest(TAG_OPTIONS);
	}

	public void writeMarkableLayerManifest(MarkableLayerManifest manifest) throws Exception {
		writeMarkableLayerManifest0(TAG_MARKABLE_LAYER, manifest);
	}

	public void writeStructureLayerManifest(StructureLayerManifest manifest) throws Exception {
		writeMarkableLayerManifest0(TAG_STRUCTURE_LAYER, manifest);
	}

	private void writeAliasElement(String alias) throws Exception {
		//FIXME empty element
		serializer.startElement(TAG_ALIAS);
		serializer.writeAttribute(ATTR_NAME, alias);
		serializer.endElement(TAG_ALIAS);
	}

	public void writeAnnotationManifestElement(AnnotationManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_ANNOTATION);

		// ATTRIBUTES
		writeIdentityAttributes(manifest);
		serializer.writeAttribute(ATTR_KEY, manifest.getKey());

		// ELEMENTS
		writeModifiableManifest(manifest);

		for(String alias : manifest.getAliases()) {
			writeAliasElement(alias);
		}

		writeValuesElement(manifest.getSupportedValues(), manifest.getValueType());
		writeValueRangeElement(manifest.getSupportedRange(), manifest.getValueType());

		serializer.endElement(TAG_ANNOTATION);
	}

	public void writeAnnotationLayerManifest(AnnotationLayerManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_ANNOTATION_LAYER);

		AnnotationLayerManifest template = (AnnotationLayerManifest) manifest.getTemplate();

		String defaultKey = manifest.getDefaultKey();
		if(template!=null) {
			defaultKey = diff(defaultKey, template.getDefaultKey());
		}

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);
		writeLayerTypeAttribute(manifest);

		// Deep annotation
		if(manifest.isDeepAnnotation() && template!=null
				&& !template.isDeepAnnotation()) {
			serializer.writeAttribute(ATTR_DEEP_ANNOTATION, true);
		}

		// Unknown keys
		if(manifest.allowUnknownKeys() && template!=null
				&& !template.allowUnknownKeys()) {
			serializer.writeAttribute(ATTR_UNKNOWN_KEYS, true);
		}

		// Searchable
		if(!manifest.isSearchable() && template!=null
				&& template.isSearchable()) {
			serializer.writeAttribute(ATTR_SERCH, false);
		}

		// Indexable
		if(!manifest.isIndexable() && template!=null
				&& template.isIndexable()) {
			serializer.writeAttribute(ATTR_INDEX, false);
		}

		// Default key
		if(defaultKey!=null) {
			serializer.writeAttribute(ATTR_DEFAULT_KEY, defaultKey);
		}

		// ELEMENTS

		// Options and properties
		writeModifiableManifest(manifest);

		// Write base layers
		writeBaseLayerElements(manifest);

		// Write annotations
		Set<String> keys = manifest.getAvailableKeys();
		if(template!=null && !template.getAvailableKeys().isEmpty()) {
			keys = new HashSet<>(keys);

			keys.removeAll(template.getAvailableKeys());
		}

		if(!keys.isEmpty()) {

			List<String> names = CollectionUtils.asSortedList(keys);

			for(String name : names) {
				writeAnnotationManifestElement(manifest.getAnnotationManifest(name));
			}
		}

		endManifest(TAG_ANNOTATION_LAYER);
	}

	public void writeFragmentLayerManifest(FragmentLayerManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_FRAGMENT_LAYER);

		FragmentLayerManifest template = (FragmentLayerManifest) manifest.getTemplate();

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);
		writeLayerTypeAttribute(manifest);

		// Write annotation key
		writeAnnotationKeyAttribute(manifest);

		// ELEMENTS

		writeModifiableManifest(manifest);

		// Write base layers
		writeBaseLayerElements(manifest);

		// Write boundary layer
		writeBoundaryLayerElement(manifest);

		// Write value layer
		writeValueLayerElement(manifest);

		// Write containers
		writeNestedContainerElements(manifest);

		// Write rasterizer
		RasterizerManifest rasterizerManifest = manifest.getRasterizerManifest();
		if(template!=null) {
			rasterizerManifest = diff(rasterizerManifest, template.getRasterizerManifest());
		}

		if(rasterizerManifest!=null) {
			writeRasterizerManifest(rasterizerManifest);
		}

		endManifest(TAG_FRAGMENT_LAYER);
	}

	public void writeHighlightLayerManifest(StructureLayerManifest manifest) throws Exception {
		//TODO
	}

	public void writeImplementationManifest(ImplementationManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_IMPLEMENTATION);

		ImplementationManifest template = (ImplementationManifest) manifest.getTemplate();

		String source = manifest.getSource();
		String classname = manifest.getClassname();
		SourceType sourceType = manifest.getSourceType();

		if(template!=null) {
			source = diff(source, template.getSource());
			classname = diff(classname, template.getClassname());
			sourceType = diff(sourceType, template.getSourceType());
		}

		serializer.writeAttribute(ATTR_SOURCE, source);
		serializer.writeAttribute(ATTR_CLASSNAME, classname);
		if(sourceType!=null && sourceType!=SourceType.DEFAULT) {
			serializer.writeAttribute(ATTR_SOURCE_TYPE, sourceType.getValue());
		}

		endManifest(TAG_IMPLEMENTATION);
	}

	public void writeRasterizerManifest(RasterizerManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_RASTERIZER);

		RasterizerManifest template = (RasterizerManifest) manifest.getTemplate();

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);

		// ELEMENTS

		writeModifiableManifest(manifest);

		// Write implementaton manifest
		ImplementationManifest implementationManifest = manifest.getImplementationManifest();
		if(template!=null) {
			implementationManifest = diff(implementationManifest, template.getImplementationManifest());
		}

		if(implementationManifest!=null) {
			writeImplementationManifest(implementationManifest);
		}

		endManifest(TAG_RASTERIZER);
	}

	//TODO write connector?
	public void writeDriverManifest(DriverManifest manifest) throws Exception {
		checkTemplate(manifest);

		startManifest(TAG_DRIVER);

		DriverManifest template = (DriverManifest) manifest.getTemplate();

		// ATTRIBUTES

		// Write default stuff
		writeIdentityAttributes(manifest);
		writeLocationAttribute(manifest);

		// ELEMENTS

		writeModifiableManifest(manifest);

		// Write implementaton manifest
		ImplementationManifest implementationManifest = manifest.getImplementationManifest();
		if(template!=null) {
			implementationManifest = diff(implementationManifest, template.getImplementationManifest());
		}

		if(implementationManifest!=null) {
			writeImplementationManifest(implementationManifest);
		}

		// Write index manifests
		List<IndexManifest> indices = manifest.getIndexManifests();
		if(template!=null && !template.getIndexManifests().isEmpty()) {
			indices = new ArrayList<>(indices);

			indices.removeAll(template.getIndexManifests());
		}

		for(IndexManifest indexManifest : indices) {
			writeIndexElement(indexManifest);
		}

		endManifest(TAG_DRIVER);
	}
}
