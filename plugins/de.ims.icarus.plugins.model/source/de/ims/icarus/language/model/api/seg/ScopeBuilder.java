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
package de.ims.icarus.language.model.api.seg;

import java.util.ArrayList;
import java.util.List;

import de.ims.icarus.language.model.api.Context;
import de.ims.icarus.language.model.api.Corpus;
import de.ims.icarus.language.model.api.layer.Layer;
import de.ims.icarus.language.model.api.layer.MarkableLayer;
import de.ims.icarus.util.collections.CollectionUtils;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ScopeBuilder {

	private List<Layer> layers = new ArrayList<>();
	private List<Context> contexts = new ArrayList<>();
	private MarkableLayer primaryLayer;
	private final Corpus corpus;

	public ScopeBuilder(Corpus corpus) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		this.corpus = corpus;
	}

	public ScopeBuilder(Corpus corpus, Scope source) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$
		if (source == null)
			throw new NullPointerException("Invalid source"); //$NON-NLS-1$

		this.corpus = corpus;

	}

	/**
	 * @return the layers
	 */
	public List<Layer> getLayers() {
		return CollectionUtils.getListProxy(layers);
	}

	/**
	 * @return the contexts
	 */
	public List<Context> getContexts() {
		return CollectionUtils.getListProxy(contexts);
	}

	/**
	 * @return the primaryLayer
	 */
	public MarkableLayer getPrimaryLayer() {
		return primaryLayer;
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @param layers the layers to set
	 */
	public ScopeBuilder setLayers(List<Layer> layers) {
		if (layers == null)
			throw new NullPointerException("Invalid layers"); //$NON-NLS-1$

		this.layers.clear();
		this.layers.addAll(layers);

		return this;
	}

	/**
	 * @param contexts the contexts to set
	 */
	public ScopeBuilder setContexts(List<Context> contexts) {
		if (contexts == null)
			throw new NullPointerException("Invalid contexts"); //$NON-NLS-1$

		this.contexts.clear();
		this.contexts.addAll(contexts);

		return this;
	}

	/**
	 * @param primaryLayer the primaryLayer to set
	 */
	public ScopeBuilder setPrimaryLayer(MarkableLayer primaryLayer) {
		if (primaryLayer == null)
			throw new NullPointerException("Invalid primaryLayer"); //$NON-NLS-1$

		this.primaryLayer = primaryLayer;

		return this;
	}

	public ScopeBuilder addContext(Context context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		if(contexts.contains(context))
			throw new IllegalArgumentException("Context already contained: "+context); //$NON-NLS-1$

		contexts.add(context);

		return this;
	}

	public ScopeBuilder addContext(List<Context> contexts) {
		if (contexts == null)
			throw new NullPointerException("Invalid contexts"); //$NON-NLS-1$

		for(Context context : contexts) {
			addContext(context);
		}

		return this;
	}

	public ScopeBuilder removeContext(Context context) {
		if (context == null)
			throw new NullPointerException("Invalid context"); //$NON-NLS-1$

		if(!contexts.remove(context))
			throw new IllegalArgumentException("Unknown context: "+context); //$NON-NLS-1$

		return this;
	}

	public ScopeBuilder removeContexts(List<Context> contexts) {
		if (contexts == null)
			throw new NullPointerException("Invalid contexts"); //$NON-NLS-1$

		for(Context context : contexts) {
			removeContext(context);
		}

		return this;
	}

	public ScopeBuilder removeAllContexts() {
		contexts.clear();

		return this;
	}

	public ScopeBuilder addLayer(Layer layer) {
		if (layer == null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

		if(layers.contains(layer))
			throw new IllegalArgumentException("Layer already contained: "+layer); //$NON-NLS-1$

		if(!contexts.contains(layer.getContext()))
			throw new IllegalArgumentException("Unknown context for layer: "+layer); //$NON-NLS-1$

		layers.add(layer);

		return this;
	}

	public ScopeBuilder addLayers(List<Layer> layers) {
		if (layers == null)
			throw new NullPointerException("Invalid layess"); //$NON-NLS-1$

		for(Layer layer : layers) {
			addLayer(layer);
		}

		return this;
	}

	public ScopeBuilder removeLayer(Layer layer) {
		if (layer == null)
			throw new NullPointerException("Invalid layer"); //$NON-NLS-1$

		if(!layers.remove(layer))
			throw new IllegalArgumentException("Unknown layer: "+layer); //$NON-NLS-1$

		return this;
	}

	public ScopeBuilder removeLayers(List<Layer> layers) {
		if (layers == null)
			throw new NullPointerException("Invalid layers"); //$NON-NLS-1$

		for(Layer layer : layers) {
			removeLayer(layer);
		}

		return this;
	}

	public ScopeBuilder removeAllLayers() {
		layers.clear();

		return this;
	}

	public Scope build() {
		if(primaryLayer==null)
			throw new IllegalStateException("No primary layer defined"); //$NON-NLS-1$
		if(contexts.isEmpty())
			throw new IllegalStateException("No contexts defined"); //$NON-NLS-1$

		return new Scope(corpus, contexts, primaryLayer, layers);
	}

}
