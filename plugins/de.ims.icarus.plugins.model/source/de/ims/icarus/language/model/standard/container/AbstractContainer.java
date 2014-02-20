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
package de.ims.icarus.language.model.standard.container;

import de.ims.icarus.language.model.Container;
import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.Markable;
import de.ims.icarus.language.model.MemberType;
import de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.language.model.util.CorpusUtils;
import de.ims.icarus.util.mem.HeapMember;
import de.ims.icarus.util.mem.Primitive;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
@HeapMember
public abstract class AbstractContainer implements Container {

	@Primitive
	private final long id;

	/**
	 * @param id
	 * @param container
	 */
	public AbstractContainer(long id) {
		this.id = id;
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getId()
	 */
	@Override
	public long getId() {
		return id;
	}

	/**
	 * @see de.ims.icarus.language.model.CorpusMember#getMemberType()
	 */
	@Override
	public MemberType getMemberType() {
		return MemberType.CONTAINER;
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getBeginOffset()
	 */
	@Override
	public int getBeginOffset() {
		if(getMarkableCount()==0)
			throw new IllegalStateException("Container is empty"); //$NON-NLS-1$

		return getMarkableAt(0).getBeginOffset();
	}

	/**
	 * @see de.ims.icarus.language.model.Markable#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		if(getMarkableCount()==0)
			throw new IllegalStateException("Container is empty"); //$NON-NLS-1$

		return getMarkableAt(getMarkableCount()-1).getEndOffset();
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Markable o) {
		return CorpusUtils.compare(this, o);
	}

	/**
	 * @see de.ims.icarus.language.model.Container#containsMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public boolean containsMarkable(Markable markable) {
		return indexOfMarkable(markable)!=-1;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#getBaseContainer()
	 */
	@Override
	public Container getBaseContainer() {
		return null;
	}

	/**
	 * @see de.ims.icarus.language.model.Container#indexOfMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public int indexOfMarkable(Markable markable) {
		if (markable == null)
			throw new NullPointerException("Invalid markable");  //$NON-NLS-1$

		int size = getMarkableCount();

		for(int i=0; i<size; i++) {
			if(markable.equals(getMarkableAt(i))) {
				return i;
			}
		}

		return -1;
	}

	// Shorthand edit methods

	/**
	 * @see de.ims.icarus.language.model.Container#addMarkable()
	 */
	@Override
	public void addMarkable(Markable markable) {
		addMarkable(getMarkableCount(), markable);
	}

	/**
	 * @see de.ims.icarus.language.model.Container#removeMarkable(de.ims.icarus.language.model.Markable)
	 */
	@Override
	public Markable removeMarkable(Markable markable) {
		return removeMarkable(indexOfMarkable(markable));
	}

	/**
	 * @see de.ims.icarus.language.model.Container#moveMarkable(de.ims.icarus.language.model.Markable, int)
	 */
	@Override
	public void moveMarkable(Markable markable, int index) {
		moveMarkable(indexOfMarkable(markable), index);
	}

	/**
	 * Helper method to check whether or not the enclosing corpus is editable
	 * and to forward an atomic change to the edit model.
	 *
	 * @param change
	 * @throws UnsupportedOperationException if the corpus is not editable
	 */
	protected void execute(AtomicChange change) {
		Corpus corpus = getCorpus();

		if(!corpus.getManifest().isEditable())
			throw new UnsupportedOperationException("Corpus does not support modifications"); //$NON-NLS-1$

		corpus.getEditModel().execute(change);
	}
}
