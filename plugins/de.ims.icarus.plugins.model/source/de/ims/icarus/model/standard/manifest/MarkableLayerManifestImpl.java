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

import de.ims.icarus.model.api.manifest.ContainerManifest;
import de.ims.icarus.model.api.manifest.ManifestType;
import de.ims.icarus.model.api.manifest.MarkableLayerManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class MarkableLayerManifestImpl extends AbstractLayerManifest<MarkableLayerManifest> implements MarkableLayerManifest {

	private final List<ContainerManifest> containerManifests = new ArrayList<>();

	private TargetLayerManifest boundaryLayerManifest;

	/**
	 * @see de.ims.icarus.model.api.manifest.MemberManifest#getManifestType()
	 */
	@Override
	public ManifestType getManifestType() {
		return ManifestType.MARKABLE_LAYER_MANIFEST;
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#getContainerDepth()
	 */
	@Override
	public int getContainerDepth() {
//		if(containerManifests==null)
//			throw new IllegalStateException("Missing root container manifest"); //$NON-NLS-1$

		return containerManifests==null ? 0 : containerManifests.size();
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#getRootContainerManifest()
	 */
	@Override
	public ContainerManifest getRootContainerManifest() {
		return getContainerManifest(0);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#getContainerManifest(int)
	 */
	@Override
	public ContainerManifest getContainerManifest(int level) {
		if(containerManifests==null)
			throw new IllegalStateException("Missing root container manifest"); //$NON-NLS-1$

		return containerManifests.get(level);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#indexOfContainerManifest(de.ims.icarus.model.api.manifest.ContainerManifest)
	 */
	@Override
	public int indexOfContainerManifest(ContainerManifest containerManifest) {
		if (containerManifest == null)
			throw new NullPointerException("Invalid containerManifest"); //$NON-NLS-1$

		return containerManifests.indexOf(containerManifest);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#removeContainerManifest(de.ims.icarus.model.api.manifest.ContainerManifest)
	 */
	@Override
	public void removeContainerManifest(ContainerManifest containerManifest) {
		if (containerManifest == null)
			throw new NullPointerException("Invalid containerManifest"); //$NON-NLS-1$

		containerManifests.remove(containerManifest);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#setContainerManifest(de.ims.icarus.model.api.manifest.ContainerManifest, int)
	 */
	@Override
	public void setContainerManifest(ContainerManifest containerManifest,
			int level) {
		if (containerManifest == null)
			throw new NullPointerException("Invalid containerManifest"); //$NON-NLS-1$

		containerManifests.set(level, containerManifest);
	}

	public void addContainerManifest(ContainerManifest containerManifest) {
		if (containerManifest == null)
			throw new NullPointerException("Invalid containerManifest"); //$NON-NLS-1$

		containerManifests.add(containerManifest);
	}

	/**
	 * @see de.ims.icarus.model.api.manifest.MarkableLayerManifest#getBoundaryLayerManifest()
	 */
	@Override
	public TargetLayerManifest getBoundaryLayerManifest() {
		return boundaryLayerManifest;
	}

	/**
	 * @param boundaryLayerManifest the boundaryLayerManifest to set
	 */
	@Override
	public void setBoundaryLayerManifest(TargetLayerManifest boundaryLayerManifest) {
		if (boundaryLayerManifest == null)
			throw new NullPointerException("Invalid boundaryLayerManifest"); //$NON-NLS-1$

		this.boundaryLayerManifest = boundaryLayerManifest;
	}

	/**
	 * Attention:
	 * This implementation does <b>not</b> inherit the container manifests of the given template!
	 *
	 * @see de.ims.icarus.model.standard.manifest.AbstractMemberManifest#copyFrom(de.ims.icarus.model.api.manifest.MemberManifest)
	 */
	@Override
	protected void copyFrom(MarkableLayerManifest template) {
		super.copyFrom(template);

		boundaryLayerManifest = template.getBoundaryLayerManifest();

//		for(int i=0; i<template.getContainerDepth(); i++) {
//			addContainerManifest(template.getContainerManifest(i));
//		}
	}
}
