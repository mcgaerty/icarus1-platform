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
package de.ims.icarus.model.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus.model.api.Context;
import de.ims.icarus.model.api.Corpus;
import de.ims.icarus.model.api.edit.CorpusEditModel;
import de.ims.icarus.model.api.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.model.api.layer.AnnotationLayer;
import de.ims.icarus.model.api.layer.FragmentLayer;
import de.ims.icarus.model.api.layer.Layer;
import de.ims.icarus.model.api.layer.LayerGroup;
import de.ims.icarus.model.api.layer.MarkableLayer;
import de.ims.icarus.model.api.manifest.AnnotationLayerManifest;
import de.ims.icarus.model.api.manifest.AnnotationManifest;
import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ContextManifest;
import de.ims.icarus.model.api.manifest.ContextManifest.PrerequisiteManifest;
import de.ims.icarus.model.api.manifest.HighlightLayerManifest;
import de.ims.icarus.model.api.manifest.LayerManifest;
import de.ims.icarus.model.api.manifest.ManifestOwner;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.ItemLayerManifest;
import de.ims.icarus.model.api.manifest.MemberManifest;
import de.ims.icarus.model.api.manifest.StructureLayerManifest;
import de.ims.icarus.model.api.manifest.StructureManifest;
import de.ims.icarus.model.api.members.Container;
import de.ims.icarus.model.api.members.CorpusMember;
import de.ims.icarus.model.api.members.Fragment;
import de.ims.icarus.model.api.members.Item;
import de.ims.icarus.model.api.members.MemberType;
import de.ims.icarus.model.api.raster.Position;
import de.ims.icarus.model.api.raster.PositionOutOfBoundsException;
import de.ims.icarus.model.api.raster.Rasterizer;
import de.ims.icarus.model.io.LocationType;
import de.ims.icarus.model.io.ResourcePath;
import de.ims.icarus.model.types.ValueType;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public final class CorpusUtils {

	private static final AtomicInteger uidGenerator = new AtomicInteger(1);

	private CorpusUtils() {
		throw new AssertionError();
	}

	public static int getNewUID() {
		return uidGenerator.getAndIncrement();
	}

	private static final Pattern idPattern = Pattern.compile(
			"^\\p{Alpha}[:_\\-\\w]{2,}$"); //$NON-NLS-1$

	private static Matcher idMatcher;

	/**
	 * Verifies the validity of the given {@code id} string.
	 * <p>
	 * Valid ids are defined as follows:
	 * <ul>
	 * <li>they have a minimum length of 3 characters</li>
	 * <li>they start with an alphabetic character (lower and upper case are allowed)</li>
	 * <li>subsequent characters may be alphabetic or digits</li>
	 * <li>no whitespaces, control characters or code points with 2 or more bytes are allowed</li>
	 * <li>no special characters are allowed besides the following 2: _- (underscore, hyphen)</li>
	 * </ul>
	 *
	 * Attempting to use any other string as an identifier for arbitrary members of a corpus will
	 * result in them being rejected by the registry.
	 */
	public static boolean isValidId(String id) {
		synchronized (idPattern) {
			if(idMatcher==null) {
				idMatcher = idPattern.matcher(id);
			} else {
				idMatcher.reset(id);
			}

			return idMatcher.matches();
		}
	}

	public static boolean isValidValue(Object value, AnnotationManifest manifest) {
		if(value==null) {
			return true;
		}

		ValueType type = manifest.getValueType();
		switch (type) {
		case CUSTOM:
			ContentType contentType = manifest.getContentType();
			return ContentTypeRegistry.isCompatible(contentType, type);

		case UNKNOWN:
			throw new IllegalArgumentException("Manifest declares annotation value type as unknown: "+manifest); //$NON-NLS-1$

		default:
			return type.isValidValue(value);
		}
	}

	public static ContextManifest getContextManifest(MemberManifest manifest) {
		if (manifest == null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		switch (manifest.getManifestType()) {
		case ANNOTATION_LAYER_MANIFEST:
			return ((AnnotationLayerManifest)manifest).getContextManifest();
		case MARKABLE_LAYER_MANIFEST:
			return ((ItemLayerManifest)manifest).getContextManifest();
		case STRUCTURE_LAYER_MANIFEST:
			return ((StructureLayerManifest)manifest).getContextManifest();
		case HIGHLIGHT_LAYER_MANIFEST:
			return ((HighlightLayerManifest)manifest).getContextManifest();

		case CONTEXT_MANIFEST:
			return (ContextManifest) manifest;

		case CONTAINER_MANIFEST:
			return ((ContainerManifest) manifest).getLayerManifest().getContextManifest();
		case STRUCTURE_MANIFEST:
			return ((StructureManifest) manifest).getLayerManifest().getContextManifest();

		default:
			throw new IllegalArgumentException("MemberManifest does not procide scope to a context: "+manifest); //$NON-NLS-1$
		}
	}

//	public static String getText(Container c) {
//		StringBuilder sb = new StringBuilder(c.getMarkableCount()*10);
//
//		sb.append("["); //$NON-NLS-1$
//		for(int i=0; i<c.getMarkableCount(); i++) {
//			if(i>0) {
//				sb.append(", "); //$NON-NLS-1$
//			}
//			sb.append(c.getMarkableAt(i).getText());
//		}
//		sb.append("]"); //$NON-NLS-1$
//
//		return sb.toString();
//	}

	public static boolean isVirtual(Item item) {
		return item.getBeginOffset()==-1 || item.getEndOffset()==-1;
	}

	public static boolean isOverlayContainer(Container container) {
		return container.getCorpus().getOverlayContainer()==container;
	}

	public static boolean isMarkableLayer(Layer layer) {
		ManifestType type = layer.getManifest().getManifestType();
		return type==ManifestType.MARKABLE_LAYER_MANIFEST
				|| type==ManifestType.STRUCTURE_LAYER_MANIFEST
				|| type==ManifestType.FRAGMENT_LAYER_MANIFEST;
	}

	public static boolean isOverlayLayer(MarkableLayer layer) {
		return layer.getCorpus().getOverlayLayer()==layer;
	}

	public static boolean isOverlayMember(Item item) {
		return isOverlayLayer(item.getLayer());
	}

	public static boolean isLayerMember(CorpusMember member) {
		return member.getMemberType()==MemberType.LAYER;
	}

	public static boolean isItemMember(CorpusMember member) {
		return member.getMemberType()!=MemberType.LAYER;
	}

	public static boolean isContainerMember(CorpusMember member) {
		return member.getMemberType()==MemberType.CONTAINER
				|| member.getMemberType()==MemberType.STRUCTURE;
	}

	public static boolean isElementMember(CorpusMember member) {
		return member.getMemberType()==MemberType.MARKABLE
				|| member.getMemberType()==MemberType.EDGE
				|| member.getMemberType()==MemberType.FRAGMENT;
	}

	public static boolean isResolvedPrerequisite(PrerequisiteManifest manifest) {
		return manifest.getContextId()!=null && manifest.getLayerId()!=null;
	}

	public static void dispatchChange(CorpusMember source, AtomicChange change) {
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$
		if (change == null)
			throw new NullPointerException("Invalid change"); //$NON-NLS-1$

		Corpus corpus = source.getCorpus();

		if(corpus==null) {
			change.execute();
			return;
		}

		CorpusEditModel editModel = corpus.getEditModel();

		if(editModel==null) {
			change.execute();
			return;
		}

		editModel.execute(change);
	}

	public static ContainerManifest getContainerManifest(Container container) {
		if (container == null)
			throw new NullPointerException("Invalid container"); //$NON-NLS-1$

		// Fetch the container level and ask the
		// hosting markable layer manifest for the container
		// manifest at the specific level
		int level = 0;

		while(container.getContainer()!=null) {
			level++;
			container = container.getContainer();
		}

		ItemLayerManifest manifest = container.getLayer().getManifest();

		return manifest.getContainerManifest(level);
	}

	public static Layer getLayer(Corpus corpus, LayerManifest manifest) {
		ContextManifest contextManifest = manifest.getContextManifest();
		Context context = corpus.getContext(contextManifest.getId());
		return context.getLayer(manifest.getId());
	}

	public static String getName(PrerequisiteManifest prerequisite) {
		String id = prerequisite.getLayerId();
		if(id!=null)
			return "Required layer-id: "+id; //$NON-NLS-1$

		String typeName = prerequisite.getTypeId();
		if(typeName!=null && !typeName.isEmpty())
			return "Required type-id: "+typeName; //$NON-NLS-1$

		return prerequisite.toString();
	}

	public static String getName(LayerGroup layerGroup) {
		return layerGroup.getManifest().getName();
	}

	public static <M extends MemberManifest> String getName(ManifestOwner<M> owner) {
		return owner.getManifest().getName();
	}

	public static Set<MarkableLayer> getMarkableLayers(Corpus corpus) {
		return getLayers(MarkableLayer.class, corpus.getLayers());
	}

	public static Set<AnnotationLayer> getAnnotationLayers(Corpus corpus) {
		return getLayers(AnnotationLayer.class, corpus.getLayers());
	}

	public static <L extends Layer> Set<L> getLayers(Class<L> clazz, Collection<Layer> layers) {
		if(clazz==null)
			throw new NullPointerException("Invalid layer class"); //$NON-NLS-1$
		if(layers==null)
			throw new NullPointerException("Invalid layers collection"); //$NON-NLS-1$

		Set<L> result = new HashSet<>();

		for(Layer layer : layers) {
			if(clazz.isAssignableFrom(layer.getClass())) {
				result.add(clazz.cast(layer));
			}
		}

		return result;
	}

	public static Map<String, Object> getProperties(MemberManifest manifest) {
		if(manifest==null)
			throw new NullPointerException("Invalid manifest"); //$NON-NLS-1$

		Map<String, Object> result = new HashMap<>();

		for(String name : manifest.getPropertyNames()) {
			result.put(name, manifest.getProperty(name));
		}

		return result;
	}

	public static Context getContext(CorpusMember member) {
		if(member==null)
			throw new NullPointerException("Invalid member"); //$NON-NLS-1$

		Layer layer = null;

		if(member instanceof Item) {
			layer = ((Item)member).getLayer();
		} else if(member instanceof Layer) {
			layer = (Layer)member;
		}

		return layer==null ? null : layer.getContext();
	}

	private static char getTypePrefix(MemberType type) {
		switch (type) {
		case MARKABLE:
			return 'M';
		case FRAGMENT:
			return 'F';
		case CONTAINER:
			return 'C';
		case STRUCTURE:
			return 'S';
		case LAYER:
			return 'L';
		case EDGE:
			return 'E';

		default:
			throw new IllegalArgumentException();
		}
	}

	public static String toString(CorpusMember m) {
		MemberType type = m.getMemberType();

		if(type==MemberType.LAYER) {
			Layer layer = (Layer)m;
			return "[Layer: "+layer.getName()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			Item item = (Item)m;
			return "["+getTypePrefix(type)+"_"+item.getBeginOffset()+"-"+item.getEndOffset()+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
	}

	public static int compare(Item m1, Item m2) {
		long result = m1.getBeginOffset()-m2.getBeginOffset();

		if(result==0) {
			result = m1.getEndOffset()-m2.getEndOffset();
		}

		return (int) result;
	}

	public static int compare(Fragment f1, Fragment f2) {
		if(f1.getLayer()!=f2.getLayer())
			throw new IllegalArgumentException("Cannot compare fragments from different fragment layers"); //$NON-NLS-1$

		if(f1.getItem()!=f2.getItem()) {
			return f1.getItem().compareTo(f2.getItem());
		}

		Rasterizer rasterizer = f1.getLayer().getRasterizer();

		int result = rasterizer.compare(f1.getFragmentBegin(), f2.getFragmentBegin());

		if(result==0) {
			result = rasterizer.compare(f1.getFragmentEnd(), f2.getFragmentEnd());
		}

		return result;
	}

	/**
	 * Returns {@code true} if {@code m2} is located within the span
	 * defined by {@code m1}.
	 */
	public static boolean contains(Item m1, Item m2) {
		return m2.getBeginOffset()>=m1.getBeginOffset()
				&& m2.getEndOffset()<=m1.getEndOffset();
	}

	public static void checkFragmentPositions(Fragment fragment, Position begin, Position end) {
		if(begin==null && end==null)
			throw new IllegalArgumentException("At least one position must be non-null!"); //$NON-NLS-1$

		Item item = fragment.getItem();
		FragmentLayer layer = fragment.getLayer();
		Rasterizer rasterizer = layer.getRasterizer();

		int dimensionality = rasterizer.getAxisCount();
		if(begin!=null && begin.getDimensionality()!=dimensionality)
			throw new IllegalArgumentException("Begin position dimensionality mismatch: expected " //$NON-NLS-1$
					+dimensionality+" - got "+begin.getDimensionality()); //$NON-NLS-1$
		if(end!=null && end.getDimensionality()!=dimensionality)
			throw new IllegalArgumentException("End position dimensionality mismatch: expected " //$NON-NLS-1$
					+dimensionality+" - got "+end.getDimensionality()); //$NON-NLS-1$

		for(int axis=0; axis<dimensionality; axis++) {
			long size = layer.getRasterSize(item, axis);
			checkPosition(size, begin, axis);
			checkPosition(size, end, axis);
		}

		if(begin!=null && end!=null && rasterizer.compare(begin, end)>0)
			throw new IllegalArgumentException("Begin position must not exceed end position: "+begin+" - "+end); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static void checkPosition(long size, Position p, int axis) {
		if(p==null) {
			return;
		}

		long value = p.getValue(axis);

		if(value<0 || value>=size)
			throw new PositionOutOfBoundsException("Invalid value for axis "+axis+" on position "+p+" - max size "+size); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public static Path pathToFile(ResourcePath path) {
		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$
		if(path.getType()!=LocationType.FILE)
			throw new IllegalArgumentException("ResourcePath needs to be a file: "+path.getPath()); //$NON-NLS-1$

		return Paths.get(path.getPath());
	}

	public static URL pathToURL(ResourcePath path) throws MalformedURLException {
		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$
		if(path.getType()!=LocationType.NETWORK)
			throw new IllegalArgumentException("ResourcePath needs to be a url: "+path.getPath()); //$NON-NLS-1$

		return new URL(path.getPath());
	}



	public static InputStream openPath(ResourcePath path) throws IOException {
		if (path == null)
			throw new NullPointerException("Invalid path"); //$NON-NLS-1$

		switch (path.getType()) {
		case FILE:
			return Files.newInputStream(pathToFile(path));

		case NETWORK:
			return new URL(path.getPath()).openStream();

		default:
			throw new IllegalArgumentException("Cannot handle source type: "+path.getType()); //$NON-NLS-1$
		}
	}
}
