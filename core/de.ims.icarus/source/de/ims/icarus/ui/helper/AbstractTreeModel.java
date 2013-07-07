/*
 * $Revision: 33 $
 * $Date: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/core/de.ims.icarus/source/net/ikarus_systems/icarus/ui/helper/AbstractTreeModel.java $
 *
 * $LastChangedDate: 2013-05-13 14:33:31 +0200 (Mo, 13 Mai 2013) $ 
 * $LastChangedRevision: 33 $ 
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.ui.helper;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author Markus Gärtner
 * @version $Id: AbstractTreeModel.java 33 2013-05-13 12:33:31Z mcgaerty $
 * 
 */
public abstract class AbstractTreeModel implements TreeModel {

	protected EventListenerList listeners;

	protected AbstractTreeModel() {
		// no-op
	}

	protected void fireNewRoot() {
		if(listeners==null) {
			return;
		}
		
        Object[] pairs = listeners.getListenerList();

		TreePath path = new TreePath(getRoot());

		TreeModelEvent event = null;

		for (int i = pairs.length - 2; i >= 0; i -= 2) {
			if (pairs[i] == TreeModelListener.class) {
				if (event == null) {
					event = new TreeModelEvent(this, path, null, null);
				}

				((TreeModelListener) pairs[i + 1]).treeStructureChanged(event);
			}
		}
	}

	
	protected void fireStructureChanged() {
		fireTreeStructureChanged(new TreePath(getRoot()));
	}

	/** 
	 * Call when a node has changed its leaf state. 
	 */
	protected void firePathLeafStateChanged(TreePath path) {
		fireTreeStructureChanged(path);
	}

	/** 
	 * Call when the tree structure below the path has completely changed. 
	 */
	protected void fireTreeStructureChanged(TreePath parentPath) {
		Object[] pairs = listeners.getListenerList();

		TreeModelEvent event = null;

		for (int i = pairs.length - 2; i >= 0; i -= 2) {
			if (pairs[i] == TreeModelListener.class) {
				if (event == null) {
					event = new TreeModelEvent(this, parentPath, null, null);
				}

				((TreeModelListener) pairs[i + 1]).treeStructureChanged(event);
			}
		}
	}

	/**
	 * Call when the path itself has changed, but no structure changes have
	 * occurred.
	 */
	protected void firePathChanged(TreePath path) {
		Object node = path.getLastPathComponent();
		TreePath parentPath = path.getParentPath();

		if (parentPath == null) {
			fireChildrenChanged(path, null, null);
		} else {
			Object parent = parentPath.getLastPathComponent();

			fireChildChanged(parentPath, getIndexOfChild(parent, node), node);
		}
	}

	protected void fireChildAdded(TreePath parentPath, int index, Object child) {
		fireChildrenAdded(parentPath, new int[] { index }, new Object[] { child });
	}

	protected void fireChildChanged(TreePath parentPath, int index, Object child) {
		fireChildrenChanged(parentPath, new int[] { index }, new Object[] { child });
	}

	protected void fireChildRemoved(TreePath parentPath, int index, Object child) {
		fireChildrenRemoved(parentPath, new int[] { index }, new Object[] { child });
	}

	protected void fireChildrenAdded(TreePath parentPath, int[] indices, Object[] children) {
		Object[] pairs = listeners.getListenerList();

		TreeModelEvent event = null;

		for (int i = pairs.length - 2; i >= 0; i -= 2) {
			if (pairs[i] == TreeModelListener.class) {
				if (event == null) {
					event = new TreeModelEvent(this, parentPath, indices, children);
				}

				((TreeModelListener) pairs[i + 1]).treeNodesInserted(event);
			}
		}
	}

	protected void fireChildrenChanged(TreePath parentPath, int[] indices, Object[] children) {
		Object[] pairs = listeners.getListenerList();

		TreeModelEvent event = null;

		for (int i = pairs.length - 2; i >= 0; i -= 2) {
			if (pairs[i] == TreeModelListener.class) {
				if (event == null) {
					event = new TreeModelEvent(this, parentPath, indices, children);
				}

				((TreeModelListener) pairs[i + 1]).treeNodesChanged(event);
			}
		}
	}

	protected void fireChildrenRemoved(TreePath parentPath, int[] indices, Object[] children) {
		Object[] pairs = listeners.getListenerList();

		TreeModelEvent event = null;

		for (int i = pairs.length - 2; i >= 0; i -= 2) {
			if (pairs[i] == TreeModelListener.class) {
				if (event == null) {
					event = new TreeModelEvent(this, parentPath, indices, children);
				}
				((TreeModelListener) pairs[i + 1]).treeNodesRemoved(event);
			}
		}
	}

	/**
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	@Override
	public void addTreeModelListener(TreeModelListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("Invalid listener"); //$NON-NLS-1$

		if (listeners == null) {
			listeners = new EventListenerList();
		}
        listeners.add(TreeModelListener.class, listener);
	}

	/**
	 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	@Override
	public void removeTreeModelListener(TreeModelListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("Invalid listener"); //$NON-NLS-1$

		if (listeners == null) {
			return;
		}
        listeners.remove(TreeModelListener.class, listener);
	}

}