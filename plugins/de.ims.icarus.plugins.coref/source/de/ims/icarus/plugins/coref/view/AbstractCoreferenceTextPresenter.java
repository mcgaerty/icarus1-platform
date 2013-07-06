/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.coref.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;

import de.ims.icarus.config.ConfigEvent;
import de.ims.icarus.config.ConfigListener;
import de.ims.icarus.config.ConfigRegistry;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.config.ConfigRegistry.Handle;
import de.ims.icarus.language.coref.helper.SpanFilters;
import de.ims.icarus.language.coref.text.CoreferenceDocument;
import de.ims.icarus.language.coref.text.HighlightType;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.actions.ActionManager;
import de.ims.icarus.ui.config.ConfigDialog;
import de.ims.icarus.ui.helper.TooltipListCellRenderer;
import de.ims.icarus.ui.tasks.TaskManager;
import de.ims.icarus.ui.tasks.TaskPriority;
import de.ims.icarus.ui.view.AWTPresenter;
import de.ims.icarus.ui.view.PresenterUtils;
import de.ims.icarus.ui.view.UnsupportedPresentationDataException;
import de.ims.icarus.util.CorruptedStateException;
import de.ims.icarus.util.Filter;
import de.ims.icarus.util.Options;
import de.ims.icarus.util.data.ContentType;
import de.ims.icarus.util.data.ContentTypeRegistry;
import de.ims.icarus.util.id.Identity;


/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public abstract class AbstractCoreferenceTextPresenter implements AWTPresenter {
	
	protected JComponent contentPanel;
	protected JTextPane textPane;
	
	private static ActionManager sharedActionManager;

	protected static final String configPath = "plugins.coref.appearance"; //$NON-NLS-1$
	
	protected ActionManager actionManager;
	protected JPopupMenu popupMenu;
	
	protected Handler handler;
	
	protected CallbackHandler callbackHandler;
	
	// Filter to be applied when the user decides to filter certain spans
	protected Filter pendingFilter = null; 
	
	protected RefreshJob refreshJob;

	protected AbstractCoreferenceTextPresenter() {
		// no-op
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	
	protected CallbackHandler createCallbackHandler() {
		return new CallbackHandler();
	}
	
	protected Handler getHandler() {
		if(handler==null) {
			handler = createHandler();
		}
		return handler;
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#supports(de.ims.icarus.util.data.ContentType)
	 */
	@Override
	public boolean supports(ContentType type) {
		return ContentTypeRegistry.isCompatible(getContentType(), type);
	}
	
	public abstract ContentType getContentType();

	/**
	 * @see de.ims.icarus.ui.view.Presenter#present(java.lang.Object, de.ims.icarus.util.Options)
	 */
	@Override
	public void present(Object data, Options options)
			throws UnsupportedPresentationDataException {
		if(data==null)
			throw new IllegalArgumentException("Invalid data"); //$NON-NLS-1$
		if(!PresenterUtils.presenterSupports(this, data))
			throw new UnsupportedPresentationDataException("Data not supported: "+data.getClass()); //$NON-NLS-1$
		
		setData(data);
		
		if(contentPanel==null) {
			return;
		}
		
		pendingFilter = null;
		
		refresh();
	}
	
	protected abstract void setData(Object data);
	
	protected synchronized void refresh() {
		RefreshJob job = refreshJob;
		if(job!=null) {
			return;
		}
		
		job = new RefreshJob();
		refreshJob = job;
		
		TaskManager.getInstance().schedule(job, TaskPriority.DEFAULT, true);
	}
	
	protected abstract boolean buildDocument(CoreferenceDocument doc) throws Exception;
	
	protected static synchronized final ActionManager getSharedActionManager() {
		if(sharedActionManager==null) {
			sharedActionManager = ActionManager.globalManager().derive();

			URL actionLocation = AbstractCoreferenceTextPresenter.class.getResource("coreference-text-presenter-actions.xml"); //$NON-NLS-1$
			if(actionLocation==null)
				throw new CorruptedStateException("Missing resources: coreference-text-presenter-actions.xml"); //$NON-NLS-1$
			
			try {
				sharedActionManager.loadActions(actionLocation);
			} catch (IOException e) {
				LoggerFactory.log(AbstractCoreferenceTextPresenter.class, Level.SEVERE, 
						"Failed to load actions from file", e); //$NON-NLS-1$
			}
		}
		
		return sharedActionManager;
	}
	
	protected ActionManager getActionManager() {
		if(actionManager==null) {
			actionManager = getSharedActionManager().derive();
			
			registerActionCallbacks();
		}
		
		return actionManager;
	}
	
	protected void registerActionCallbacks() {
		ActionManager actionManager = getActionManager();
		
		if(callbackHandler==null) {
			callbackHandler = createCallbackHandler();
		}
		
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.refreshAction",  //$NON-NLS-1$
				callbackHandler, "refresh"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.toggleMarkSpansAction",  //$NON-NLS-1$
				callbackHandler, "toggleMarkSpans"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.toggleShowOffsetAction",  //$NON-NLS-1$
				callbackHandler, "toggleShowOffset"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.toggleShowClusterIdAction",  //$NON-NLS-1$
				callbackHandler, "toggleShowClusterId"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.filterSpanAction",  //$NON-NLS-1$
				callbackHandler, "filterSpan"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.clearFilterAction",  //$NON-NLS-1$
				callbackHandler, "clearFilter"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.openPreferencesAction",  //$NON-NLS-1$
				callbackHandler, "openPreferences"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.toggleForceLinebreaksAction",  //$NON-NLS-1$
				callbackHandler, "toggleForceLinebreaks"); //$NON-NLS-1$
		actionManager.addHandler("plugins.coref.coreferenceDocumentPresenter.toggleShowDocumentHeaderAction",  //$NON-NLS-1$
				callbackHandler, "toggleShowDocumentHeader"); //$NON-NLS-1$
	}
	
	protected void refreshActions() {
		
		ActionManager actionManager = getActionManager();
		
		CoreferenceDocument doc = getDocument();
		
		actionManager.setSelected(doc.isMarkSpans(), "plugins.coref.coreferenceDocumentPresenter.toggleMarkSpansAction"); //$NON-NLS-1$
		actionManager.setSelected(doc.isShowOffset(), "plugins.coref.coreferenceDocumentPresenter.toggleShowOffsetAction"); //$NON-NLS-1$
		actionManager.setSelected(doc.isShowClusterId(), "plugins.coref.coreferenceDocumentPresenter.toggleShowClusterIdAction"); //$NON-NLS-1$
		actionManager.setSelected(doc.isShowDocumentHeader(), "plugins.coref.coreferenceDocumentPresenter.toggleShowDocumentHeaderAction"); //$NON-NLS-1$
		actionManager.setSelected(doc.isForceLinebreaks(), "plugins.coref.coreferenceDocumentPresenter.toggleForceLinebreaksAction"); //$NON-NLS-1$
		
		actionManager.setEnabled(doc.getFilter()!=null, "plugins.coref.coreferenceDocumentPresenter.clearFilterAction"); //$NON-NLS-1$
		actionManager.setEnabled(pendingFilter!=null, "plugins.coref.coreferenceDocumentPresenter.filterSpanAction"); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#clear()
	 */
	@Override
	public void clear() {
		setData(null);
		refresh();
	}

	/**
	 * @see de.ims.icarus.ui.view.Presenter#close()
	 */
	@Override
	public void close() {
		// no-op
	}
	
	protected CoreferenceDocument getDocument() {
		// Make sure our components are created
		if(textPane==null) {
			getPresentingComponent();
		}
		
		return (CoreferenceDocument) textPane.getDocument();
	}
	
	protected String toolBarListId = "plugins.coref.coreferenceDocumentPresenter.toolBarList"; //$NON-NLS-1$
	
	protected JToolBar createToolBar() {
		Options options = new Options();
		
		CoreferenceDocument doc = (CoreferenceDocument)textPane.getDocument();
		
		JComboBox<HighlightType> cb = new JComboBox<>(HighlightType.values());
		cb.setSelectedItem(doc.getHighlightType());
		cb.addActionListener(getHandler());
		cb.setEditable(false);
		cb.setRenderer(new TooltipListCellRenderer());
		UIUtil.fitToContent(cb, 90, 140, 24);
		
		options.put("selectHighlightType", cb); //$NON-NLS-1$
		
		return getActionManager().createToolBar( toolBarListId, options);
	}
	
	protected JTextPane createTextPane() {
		JTextPane textPane = new JTextPane(){

			private static final long serialVersionUID = -6631566613380189123L;

			@Override
			public boolean getScrollableTracksViewportWidth() {
				Document doc = getDocument();
				if(doc instanceof CoreferenceDocument) {
					return !((CoreferenceDocument)doc).isForceLinebreaks();
				} else {
					return true;
				}
			}
		};
		
		textPane.setEditable(false);
		textPane.setEditorKit(CoreferenceStyling.getSharedEditorKit());
		textPane.addMouseListener(getHandler());
		textPane.addCaretListener(getHandler());
		textPane.setBorder(UIUtil.defaultContentBorder);
		UIUtil.disableCaretScroll(textPane);
		
		return textPane;
	}
	
	protected JComponent createContentPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		textPane = createTextPane();
		
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.getViewport().addChangeListener(getHandler());
		UIUtil.defaultSetUnitIncrement(scrollPane);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		JToolBar toolBar = createToolBar();
		if(toolBar!=null) {
			panel.add(toolBar, BorderLayout.NORTH);
		}
		
		return panel;
	}

	/**
	 * @see de.ims.icarus.ui.view.AWTPresenter#getPresentingComponent()
	 */
	@Override
	public Component getPresentingComponent() {
		if(contentPanel==null) {
			ConfigRegistry.getGlobalRegistry().addGroupListener(configPath, getHandler());
			contentPanel = createContentPanel();
			reloadConfig(ConfigRegistry.getGlobalRegistry().getHandle(configPath));
			refresh();
		}
		
		return contentPanel;
	}
	
	protected Filter createFilterForLocation(Point p) {
		if(textPane==null) {
			return null;
		}
		
		int offset = textPane.viewToModel(p);
		
		return offset==-1 ? null : createFilterForOffset(offset);
	}
	
	protected Filter createFilterForOffset(int offset) {
		if(textPane==null) {
			return null;
		}
		
		Document doc = textPane.getDocument();
		Element elem = doc.getDefaultRootElement();
		int childIndex = elem.getElementIndex(offset);
		if(childIndex==-1) {
			return null;
		}
		while(!elem.isLeaf()) {
			elem = elem.getElement(elem.getElementIndex(offset));
		}
		
		AttributeSet attr = elem.getAttributes();
		if(attr.isDefined(CoreferenceDocument.PARAM_CLUSTER_ID)) {
			int clusterId = (int) attr.getAttribute(CoreferenceDocument.PARAM_CLUSTER_ID);
			return new SpanFilters.ClusterIdFilter(clusterId);
		}
		
		return null;
	}
	
	protected void showPopup(MouseEvent trigger) {
		if(contentPanel==null) {
			return;
		}
		
		if(popupMenu==null) {
			// Create new popup menu
			
			Options options = new Options();
			popupMenu = getActionManager().createPopupMenu(
					"plugins.coref.coreferenceDocumentPresenter.popupMenuList", options); //$NON-NLS-1$
			
			if(popupMenu!=null) {
				popupMenu.addSeparator();
				popupMenu.add(new UIUtil.TextAction(DefaultEditorKit.copyAction, textPane));
				popupMenu.addPropertyChangeListener("visible", getHandler()); //$NON-NLS-1$
				popupMenu.pack();
			} else {
				LoggerFactory.log(this, Level.SEVERE, "Unable to create popup menu"); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {
			refreshActions();
			
			popupMenu.show(textPane, trigger.getX(), trigger.getY());
		}
	}
	
	protected void reloadConfig(Handle handle) {
		if(textPane==null) {
			return;
		}
		
		ConfigRegistry config = handle.getSource();
		
		Color fg = new Color(config.getInteger(config.getChildHandle(handle, "fontColor"))); //$NON-NLS-1$
		Color bg = new Color(config.getInteger(config.getChildHandle(handle, "background"))); //$NON-NLS-1$
		Font font = ConfigUtils.defaultReadFont(handle);
		
		textPane.setFont(font);
		textPane.setForeground(fg);
		textPane.setBackground(bg);
		
		// Refresh is required to allow the underlying document
		// to adjust its style definitions to the new font and color settings
		refresh();
	}
	
	protected CoreferenceDocument createNewDocument() {
		if(textPane==null)
			throw new IllegalStateException();
		
		CoreferenceDocument oldDoc = (CoreferenceDocument) textPane.getDocument();
		CoreferenceDocument newDoc = (CoreferenceDocument) textPane.getEditorKit().createDefaultDocument();
		
		newDoc.copySettings(oldDoc);
		
		return newDoc;
	}
	
	protected class Handler extends MouseAdapter implements ChangeListener, 
			ActionListener, PropertyChangeListener, ConfigListener, CaretListener {
		
		protected void maybeShowPopup(MouseEvent e) {
			if(e.isPopupTrigger() && e.getSource()==textPane) {
				pendingFilter = createFilterForLocation(e.getPoint());
				
				showPopup(e);
			}
		}

		/**
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		/**
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			if(textPane==null) {
				return;
			}
			
			textPane.repaint(textPane.getVisibleRect());
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JComboBox<?> cb = (JComboBox<?>) e.getSource();
			HighlightType highlightType = (HighlightType) cb.getSelectedItem();
			
			CoreferenceDocument doc = getDocument();
			if(doc.getHighlightType()==highlightType) {
				return;
			}
			
			try {
				doc.setHighlightType(highlightType);
				refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to switch highlight type", ex); //$NON-NLS-1$
			}
		}

		/**
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			
			// TODO think of a way to quickly dispose of the pending filter in case we don't need it
			// Clear pending filter when popup gets closed
			/*if(evt.getSource()==popupMenu) {
				if("visible".equals(evt.getPropertyName())  //$NON-NLS-1$
						&& Boolean.FALSE.equals(evt.getNewValue())) {
					pendingFilter = null;
				}
			}*/
		}

		/**
		 * @see de.ims.icarus.config.ConfigListener#invoke(de.ims.icarus.config.ConfigRegistry, de.ims.icarus.config.ConfigEvent)
		 */
		@Override
		public void invoke(ConfigRegistry sender, ConfigEvent event) {
			reloadConfig(event.getHandle());
		}

		/**
		 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
		 */
		@Override
		public void caretUpdate(CaretEvent e) {
			if(e.getDot()!=e.getMark()) {
				pendingFilter = createFilterForOffset(e.getMark());
			}
			refreshActions();
		}
		
	}

	public class CallbackHandler {
		
		protected CallbackHandler() {
			// no-op
		}
		
		public void toggleMarkSpans(boolean b) {
			CoreferenceDocument doc = getDocument();
			
			if(doc.isMarkSpans()==b) {
				return;
			}
			
			try {
				doc.setMarkSpans(b);
				AbstractCoreferenceTextPresenter.this.refresh();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'markSpans' flag", e); //$NON-NLS-1$
			}
		}

		public void toggleMarkSpans(ActionEvent e) {
			// ignore
		}
		
		public void toggleShowClusterId(boolean b) {
			CoreferenceDocument doc = getDocument();
			
			if(doc.isShowClusterId()==b) {
				return;
			}
			
			try {
				doc.setShowClusterId(b);
				AbstractCoreferenceTextPresenter.this.refresh();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'showClusterId' flag", e); //$NON-NLS-1$
			}
		}

		public void toggleShowClusterId(ActionEvent e) {
			// ignore
		}
		
		public void toggleShowOffset(boolean b) {
			CoreferenceDocument doc = getDocument();
			
			if(doc.isShowOffset()==b) {
				return;
			}
			
			try {
				doc.setShowOffset(b);
				AbstractCoreferenceTextPresenter.this.refresh();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'showOffset' flag", e); //$NON-NLS-1$
			}
		}

		public void toggleShowOffset(ActionEvent e) {
			// ignore
		}

		public void openPreferences(ActionEvent e) {
			try {
				new ConfigDialog(ConfigRegistry.getGlobalRegistry(), 
						"plugins.coref.appearance").setVisible(true); //$NON-NLS-1$
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to open preferences", ex); //$NON-NLS-1$
			}
		}

		public void toggleShowDocumentHeader(ActionEvent e) {
			// ignore
		}

		public void toggleShowDocumentHeader(boolean b) {
			CoreferenceDocument doc = getDocument();
			
			if(doc.isShowDocumentHeader()==b) {
				return;
			}
			
			try {
				doc.setShowDocumentHeader(b);
				AbstractCoreferenceTextPresenter.this.refresh();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'showDocumentHeader' flag", e); //$NON-NLS-1$
			}
		}

		public void toggleForceLinebreaks(ActionEvent e) {
			// ignore
		}

		public void toggleForceLinebreaks(boolean b) {
			CoreferenceDocument doc = getDocument();
			
			if(doc.isForceLinebreaks()==b) {
				return;
			}
			
			try {
				doc.setForceLinebreaks(b);
				AbstractCoreferenceTextPresenter.this.refresh();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to toggle 'floatingText' flag", e); //$NON-NLS-1$
			}
		}

		public void filterSpan(ActionEvent e) {
			if(textPane==null) {
				return;
			}
			if(pendingFilter==null) {
				return;
			}
			
			try {
				getDocument().setFilter(pendingFilter);
				AbstractCoreferenceTextPresenter.this.refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to apply span filter", ex); //$NON-NLS-1$
			}
		}

		public void clearFilter(ActionEvent e) {
			CoreferenceDocument doc = getDocument();
			
			if(doc.getFilter()==null) {
				return;
			}
			
			try {
				doc.setFilter(null);
				AbstractCoreferenceTextPresenter.this.refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to clear filter", ex); //$NON-NLS-1$
			}
		}

		public void refresh(ActionEvent e) {
			try {
				AbstractCoreferenceTextPresenter.this.refresh();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to refresh document", ex); //$NON-NLS-1$
			}
		}
	}
	
	protected class RefreshJob extends SwingWorker<CoreferenceDocument, Integer>
			implements Identity {

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof RefreshJob) {
				return owner()==((RefreshJob)obj).owner();
			}
			return false;
		}
		
		private Object owner() {
			return AbstractCoreferenceTextPresenter.this;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getId()
		 */
		@Override
		public String getId() {
			return getClass().getSimpleName();
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getName()
		 */
		@Override
		public String getName() {
			return ResourceManager.getInstance().get(
					"plugins.coref.coreferenceDocumentPresenter.refreshJob.name"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getDescription()
		 */
		@Override
		public String getDescription() {
			return ResourceManager.getInstance().get(
					"plugins.coref.coreferenceDocumentPresenter.refreshJob.description"); //$NON-NLS-1$
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getIcon()
		 */
		@Override
		public Icon getIcon() {
			return null;
		}

		/**
		 * @see de.ims.icarus.util.id.Identity#getOwner()
		 */
		@Override
		public Object getOwner() {
			return this;
		}

		/**
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected CoreferenceDocument doInBackground() throws Exception {
			if(textPane==null) {
				return null;
			}
			
			TaskManager.getInstance().setIndeterminate(this, true);
			CoreferenceDocument doc = createNewDocument();
			boolean done = buildDocument(doc);
			TaskManager.getInstance().setIndeterminate(this, false);
			
			if(done) {
				textPane.setDocument(doc);
			}
			
			return doc;
		}

		@Override
		protected void done() {			
			try {
				if(textPane==null) {
					return;
				}
				
				refreshActions();
			} catch(Exception e) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to rebuild document", e); //$NON-NLS-1$
			} finally {
				refreshJob = null;
			}
		}
		
	}
}
