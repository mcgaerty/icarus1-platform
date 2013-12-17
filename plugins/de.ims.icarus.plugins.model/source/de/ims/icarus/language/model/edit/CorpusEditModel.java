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
package de.ims.icarus.language.model.edit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.ims.icarus.language.model.Corpus;
import de.ims.icarus.language.model.edit.UndoableCorpusEdit.AtomicChange;
import de.ims.icarus.ui.events.EventObject;
import de.ims.icarus.ui.events.WeakEventSource;

/**
 *
 *
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class CorpusEditModel extends WeakEventSource {

	private int updateLevel = 0;
	private boolean endingUpdate = false;
	private UndoableCorpusEdit currentEdit;

	private final Corpus corpus;

	private final List<CorpusUndoListener> undoListeners = new CopyOnWriteArrayList<>();

	public CorpusEditModel(Corpus corpus) {
		if (corpus == null)
			throw new NullPointerException("Invalid corpus"); //$NON-NLS-1$

		this.corpus = corpus;

		currentEdit = createUndoableEdit(null);
	}

	public void beginUpdate() {
		updateLevel++;
		fireEvent(new EventObject(CorpusEditEvents.BEGIN_UPDATE));
	}

	public void beginUpdate(String nameKey) {
		if(nameKey==null)
			throw new NullPointerException("Invalid edit name"); //$NON-NLS-1$
		if(updateLevel!=0)
			throw new IllegalStateException("Cannot start named edit '"+nameKey+"'- edit already in progress."); //$NON-NLS-1$ //$NON-NLS-2$

		currentEdit = createUndoableEdit(nameKey);
		beginUpdate();
	}

	public void endUpdate() {
		updateLevel--;

		if (!endingUpdate) {
			endingUpdate = updateLevel == 0;
			fireEvent(new EventObject(CorpusEditEvents.END_UPDATE,
					"edit", currentEdit)); //$NON-NLS-1$

			try {
				if (endingUpdate && !currentEdit.isEmpty()) {
					fireEvent(new EventObject(CorpusEditEvents.BEFORE_UNDO, "edit", //$NON-NLS-1$
							currentEdit));
					UndoableCorpusEdit tmp = currentEdit;
					currentEdit = createUndoableEdit(null);
					tmp.dispatch();
					fireEvent(new EventObject(CorpusEditEvents.UNDO, "edit", tmp)); //$NON-NLS-1$

					fireUndoableCorpusEdit(tmp);
				}
			} finally {
				endingUpdate = false;
			}
		}
	}

	/**
	 * @return
	 */
	protected UndoableCorpusEdit createUndoableEdit(String nameKey) {
		return new UndoableCorpusEdit(getCorpus(), nameKey){

			private static final long serialVersionUID = -471363052764925086L;

			/**
			 * @see de.ims.icarus.language.model.edit.UndoableCorpusEdit#dispatch()
			 */
			@Override
			public void dispatch() {
				getCorpus().getEditModel().fireEvent(
						new EventObject(CorpusEditEvents.CHANGE, "edit", this)); //$NON-NLS-1$
			}

		};
	}

	/**
	 * @return the corpus
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	public void addCorpusUndoListener(CorpusUndoListener listener) {
		if(!undoListeners.contains(listener)) 	{
			undoListeners.add(listener);
		}
	}

	public void removeCorpusUndoListener(CorpusUndoListener listener) {
		undoListeners.remove(listener);
	}

	protected void fireUndoableCorpusEdit(UndoableCorpusEdit edit) {
		if(edit==null)
			throw new NullPointerException("Invalid edit"); //$NON-NLS-1$

		if(undoListeners.isEmpty()) {
			return;
		}

		for(CorpusUndoListener listener : undoListeners) {
			listener.undoableEditHappened(edit);
		}
	}

	/**
	 * Executes the given atomic change and adds it to the edit currently in
	 * progress. This is considered as a micro-transaction and therefore
	 * a full cycle of update level relate methods is called. Note that if
	 * the change fails in its {@link AtomicChange#execute()} method by
	 * throwing an exception, the update level will remain unaffected.
	 *
	 * @param change
	 */
	public void execute(UndoableCorpusEdit.AtomicChange change) {
		change.execute();
		beginUpdate();
		currentEdit.add(change);
		fireEvent(new EventObject(CorpusEditEvents.EXECUTE, "change", change)); //$NON-NLS-1$
		endUpdate();
	}
}