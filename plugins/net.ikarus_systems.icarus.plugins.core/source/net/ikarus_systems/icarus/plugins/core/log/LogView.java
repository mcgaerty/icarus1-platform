/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.core.log;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.ManagementConstants;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.CompoundIcon;
import net.ikarus_systems.icarus.ui.IconRegistry;
import net.ikarus_systems.icarus.ui.UIDummies;
import net.ikarus_systems.icarus.ui.UIUtil;
import net.ikarus_systems.icarus.ui.actions.ActionManager;
import net.ikarus_systems.icarus.ui.events.EventObject;
import net.ikarus_systems.icarus.ui.helper.UIHelperRegistry;
import net.ikarus_systems.icarus.ui.view.AWTPresenter;
import net.ikarus_systems.icarus.util.CorruptedStateException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.id.DefaultIdentity;
import net.ikarus_systems.icarus.util.id.Identity;
import net.ikarus_systems.icarus.util.opi.Message;
import net.ikarus_systems.icarus.util.opi.ResultMessage;

/**
 * @author Markus Gärtner 
 * @version $Id$
 *
 */
public class LogView extends View {
	
	public static final String VIEW_ID = ManagementConstants.DEFAULT_LOG_VIEW_ID;
	
	private Identity identity;
	private JLabel infoLabel;
	private JList<LogRecord> logRecordList;
	private JPanel contentPanel;
	private CompoundIcon viewIcon;
	private JPopupMenu popupMenu;
	private Handler handler;
	
	private LogListHandler loggingModel;
	
	private boolean scrollLock = false;
	private boolean showOnError = true;
	private boolean showOnWarning = true;
	
	private CallbackHandler callbackHandler;
	
	static {
		UIHelperRegistry.globalRegistry().registerHelper(AWTPresenter.class, 
				"java.util.logging.LogRecord", LogRecordPresenter.class); //$NON-NLS-1$
	}
	
	private static Icon errorIcon, warningIcon, infoIcon, debugIcon;
	private static Icon errorExcIcon, warningExcIcon, infoExcIcon, debugExcIcon;
	
	// load icons used by the renderer
	static {
		IconRegistry iconRegistry = IconRegistry.getGlobalRegistry();
		Icon excIcon = iconRegistry.getIcon("error_co.gif"); //$NON-NLS-1$
		CompoundIcon cpIcon;
		
		errorIcon = iconRegistry.getIcon("error_tsk.gif"); //$NON-NLS-1$
		cpIcon = new CompoundIcon(errorIcon);
		cpIcon.setOverlay(CompoundIcon.BOTTOM_LEFT, excIcon);
		errorExcIcon = cpIcon;
		
		warningIcon = iconRegistry.getIcon("warning_obj.gif"); //$NON-NLS-1$
		cpIcon = new CompoundIcon(warningIcon);
		cpIcon.setOverlay(CompoundIcon.BOTTOM_LEFT, excIcon);
		warningExcIcon = cpIcon;
		
		infoIcon = iconRegistry.getIcon("information.gif"); //$NON-NLS-1$
		cpIcon = new CompoundIcon(infoIcon);
		cpIcon.setOverlay(CompoundIcon.BOTTOM_LEFT, excIcon);
		infoExcIcon = cpIcon;
		
		debugIcon = iconRegistry.getIcon("debug_exc.gif"); //$NON-NLS-1$
		cpIcon = new CompoundIcon(debugIcon);
		cpIcon.setOverlay(CompoundIcon.BOTTOM_LEFT, excIcon);
		debugExcIcon = cpIcon;
	}
	
	/**
	 * Fetches the {@code Icon} that should be used to present the
	 * given {@code LogRecord}. There are four basic icons stored 
	 * for displaying entries of type {@code error}, {@code warning},
	 * {@code info} and {@code debug}. When the provided record contains
	 * a {@code Throwable} object accessible via {@link LogRecord#getThrown()}
	 * then the returned icon will be decorated with an 'error icon' at the
	 * lower right corner.
	 */
	public static Icon getLogIcon(LogRecord record) {
		if(record==null) {
			return null;
		}
		
		int level = record.getLevel().intValue();
		boolean isExc = record.getThrown()!=null;
		
		if(level>=Level.SEVERE.intValue())
			return isExc ? errorExcIcon : errorIcon;
		else if(level>=Level.WARNING.intValue())
			return isExc ? warningExcIcon : warningIcon;
		else if(level>=Level.INFO.intValue())
			return isExc ? infoExcIcon : infoIcon;
		else
			return isExc ? debugExcIcon : debugIcon;
	}

	/**
	 * 
	 */
	public LogView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.util.id.Identifiable#getIdentity()
	 */
	@Override
	public Identity getIdentity() {
		if(identity==null) {
			viewIcon = new CompoundIcon(new ImageIcon(
					LogView.class.getResource("log-view.gif"))); //$NON-NLS-1$
			
			// create identity object
			DefaultIdentity id = new DefaultIdentity(super.getIdentity(), this);
			id.setIcon(viewIcon);
			id.lock();
			
			identity = id;
		}
		
		return identity;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {
		
		// load actions
		URL actionLocation = LogView.class.getResource("log-view-actions.xml"); //$NON-NLS-1$
		if(actionLocation==null)
			throw new CorruptedStateException("Missing resources: log-view-actions.xml"); //$NON-NLS-1$
		
		try {
			getDefaultActionManager().loadActions(actionLocation);
		} catch (IOException e) {
			LoggerFactory.getLogger(LogView.class).log(LoggerFactory.record(
					Level.SEVERE, "Failed to load actions from file", e)); //$NON-NLS-1$
			UIDummies.createDefaultErrorOutput(container, e);
			return;
		}
		
		// build ui
		container.setLayout(new BorderLayout());
		
		infoLabel = new JLabel();
		infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		infoLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		ResourceManager.getInstance().getGlobalDomain().prepareComponent(
				infoLabel, "plugins.core.logView.notAvailable", null); //$NON-NLS-1$
		ResourceManager.getInstance().getGlobalDomain().addComponent(infoLabel);
		
		container.add(infoLabel, BorderLayout.NORTH);
		
		loggingModel = new LogListHandler(1000);
		
		logRecordList = new JList<LogRecord>(loggingModel){

			private static final long serialVersionUID = -8060619873446334826L;

			@Override
			public boolean getScrollableTracksViewportWidth() {
				return true;
			}
		};
		
		handler = new Handler();
		
		logRecordList.addMouseListener(handler);
		logRecordList.addListSelectionListener(handler);
		logRecordList.setCellRenderer(new LogListCellRenderer());
		logRecordList.setBorder(UIUtil.defaultContentBorder);
		logRecordList.setPrototypeCellValue(new LogRecord(Level.SEVERE, 
				"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")); //$NON-NLS-1$

		// Make icon reflect the presence of errors or warnings in the log
		loggingModel.addChangeListener(handler);
		
		/*
		 * IMPORTANT
		 * 
		 * For 'scrollLock' to work properly we need to add the corresponding
		 * listener AFTER the list added its own ListDataListener since otherwise
		 * the list won't be able to compute the cell bounds before the new value
		 * was added!
		 */
		loggingModel.addListDataListener(handler);
		
		LoggerFactory.getRootLogger().addHandler(loggingModel);
		
		JScrollPane logScrollpane = new JScrollPane(logRecordList);
		logScrollpane.setBorder(UIUtil.topLineBorder);
		
		// Make the tool-bar align the buttons to the right
		Options options = new Options(
				"glue", Box.createHorizontalGlue()); //$NON-NLS-1$
		JToolBar toolBar = getDefaultActionManager().createToolBar(
				"plugins.core.logView.toolBarList", options); //$NON-NLS-1$
		
		contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(toolBar, BorderLayout.NORTH);
		contentPanel.add(logScrollpane, BorderLayout.CENTER);
		contentPanel.setVisible(false);
		
		container.add(contentPanel, BorderLayout.CENTER);
		container.setMinimumSize(new Dimension(350, 80));
		container.setPreferredSize(new Dimension(400, 120));
		
		registerActionCallbacks();
	}
	
	private void refreshIcon() {
		if(loggingModel==null || viewIcon==null) {
			return;
		}
		
		if(loggingModel.getErrorCount()>0)
			viewIcon.setOverlay(CompoundIcon.BOTTOM_LEFT, 
					IconRegistry.getGlobalRegistry().getIcon("error_co.gif")); //$NON-NLS-1$
		else if(loggingModel.getWarningCount()>0)
			viewIcon.setOverlay(CompoundIcon.BOTTOM_LEFT, 
					IconRegistry.getGlobalRegistry().getIcon("warning_co.gif")); //$NON-NLS-1$
		else
			viewIcon.setOverlay(CompoundIcon.BOTTOM_LEFT, null);
		
		reloadViewTab();
	}
	
	private void showPopup(MouseEvent trigger) {
		if(popupMenu==null) {
			// Create new popup menu
			
			popupMenu = getDefaultActionManager().createPopupMenu(
					"plugins.core.logView.popupMenuList", null); //$NON-NLS-1$
			
			if(popupMenu!=null) {
				popupMenu.pack();
			} else {
				LoggerFactory.getLogger(LogView.class).log(LoggerFactory.record(
						Level.SEVERE, "Unable to create popup menu")); //$NON-NLS-1$
			}
		}
		
		if(popupMenu!=null) {			
			popupMenu.show(logRecordList, trigger.getX(), trigger.getY());
		}
	}
	
	private void registerActionCallbacks() {
		if(callbackHandler==null) {
			callbackHandler = new CallbackHandler();
		}
		
		ActionManager actionManager = getDefaultActionManager();
		
		actionManager.addHandler("plugins.core.logView.clearLogAction",  //$NON-NLS-1$
				callbackHandler, "clearLog"); //$NON-NLS-1$

		actionManager.addHandler("plugins.core.logView.copyRecordAction",  //$NON-NLS-1$
				callbackHandler, "copyRecord"); //$NON-NLS-1$

		actionManager.setSelected(scrollLock, "plugins.core.logView.scrollLockAction"); //$NON-NLS-1$
		actionManager.addHandler("plugins.core.logView.scrollLockAction",  //$NON-NLS-1$
				callbackHandler, "scrollLock"); //$NON-NLS-1$

		actionManager.setSelected(showOnWarning, "plugins.core.logView.showOnWarningAction"); //$NON-NLS-1$
		actionManager.addHandler("plugins.core.logView.showOnWarningAction",  //$NON-NLS-1$
				callbackHandler, "showOnWarning"); //$NON-NLS-1$

		actionManager.setSelected(showOnError, "plugins.core.logView.showOnErrorAction"); //$NON-NLS-1$
		actionManager.addHandler("plugins.core.logView.showOnErrorAction",  //$NON-NLS-1$
				callbackHandler, "showOnError"); //$NON-NLS-1$
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#close()
	 */
	@Override
	public void close() {
		LoggerFactory.getRootLogger().removeHandler(loggingModel);
		loggingModel.close();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#isClosable()
	 */
	@Override
	public boolean isClosable() {
		return true;
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#reset()
	 */
	@Override
	public void reset() {
		if(loggingModel==null) {
			return;
		}
		loggingModel.clear();
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#receiveData(net.ikarus_systems.icarus.plugins.core.View, java.lang.Object, net.ikarus_systems.icarus.util.Options)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		// Unknown request
		return new ResultMessage(message);
	}
	
	private LogRecord getSelectedRecord() {
		if(logRecordList==null) {
			return null;
		}
		
		return logRecordList.getSelectedValue();
	}
	
	/**
	 * Scrolls the greatest index in the list displaying
	 * the log records to visible if {@link #scrollLock}
	 * is set to {@code true}.
	 */
	private void checkScrollLock() {		
		if(scrollLock && logRecordList!=null) {
			logRecordList.ensureIndexIsVisible(loggingModel.getSize()-1);
		}
	}
	
	/**
	 * Requests this view's tab to be selected in the enclosing
	 * tabbed pane of the perspective that hosts this view.
	 * This request is only send when one of the following occurs:
	 * <ul>
	 * <li>level of {@code record} is greater or equal than {@link Level#SEVERE}
	 * and {@link #showOnError} is {@code true}</li>
	 * <li>level of {@code record} is less than {@link Level#SEVERE} and
	 * greater or equal than {@link Level#WARNING}
	 * and {@link #showOnWarning} is {@code true}</li>
	 * </ul>
	 */
	private void checkShowRecord(LogRecord record) {
		if(record==null) {
			return;
		}
		
		int level = record.getLevel().intValue();
		if((showOnError && level>=Level.SEVERE.intValue())
				|| (showOnWarning && level>=Level.WARNING.intValue() && level<Level.SEVERE.intValue())) {
			requestFocusInPerspective();
		}
	}
	
	public final class CallbackHandler implements ClipboardOwner {
		
		private CallbackHandler() {
			// no-op
		}
		
		public void clearLog(ActionEvent e) {
			if(logRecordList!=null) {
				loggingModel.clear();
			}
		}
		
		public void copyRecord(ActionEvent e) {
			LogRecord record = getSelectedRecord();
			
			if(record==null) {
				return;
			}
			
			// TODO convert record to string and copy to clipboard
		}
		
		public void scrollLock(boolean b) {
			scrollLock = b;
			checkScrollLock();
		}
		
		public void scrollLock(ActionEvent e) {
			// no-op
		}
		
		public void showOnWarning(boolean b) {
			showOnWarning = b;
		}
		
		public void showOnWarning(ActionEvent e) {
			// no-op
		}
		
		public void showOnError(boolean b) {
			showOnError = b;
		}
		
		public void showOnError(ActionEvent e) {
			// no-op
		}

		/**
		 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
		 */
		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			// no-op
		}
	}
	
	private final class Handler extends MouseAdapter implements
			ListDataListener, ChangeListener, ListSelectionListener, ManagementConstants {

		private void maybeShowList() {
			if (contentPanel == null) {
				return;
			}

			if (loggingModel.getSize() == 0) {
				infoLabel.setVisible(true);
				contentPanel.setVisible(false);
			} else {
				infoLabel.setVisible(false);
				contentPanel.setVisible(true);
			}
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
			maybeShowList();
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			maybeShowList();
			checkScrollLock();
			checkShowRecord(loggingModel.getElementAt(e.getIndex0()));
		}

		@Override
		public void contentsChanged(ListDataEvent e) {
			maybeShowList();
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			refreshIcon();
		}
		
		private void maybeShowPopup(MouseEvent e) {

			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// Make all mouse buttons act as selector
			int index = logRecordList.locationToIndex(e.getPoint());
			Rectangle bounds = logRecordList.getCellBounds(index, index);
			if (bounds != null && bounds.contains(e.getPoint())) {
				logRecordList.setSelectedIndex(index);
			} else {
				logRecordList.clearSelection();
			}
			
			maybeShowPopup(e);
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			LogRecord selectedRecord = getSelectedRecord();

			getDefaultActionManager().setEnabled(selectedRecord != null,
					"plugins.core.logView.copyRecordAction"); //$NON-NLS-1$
			
			if(selectedRecord!=null) {
				String title = ResourceManager.getInstance().get(
						"plugins.core.logView.outlineTitle"); //$NON-NLS-1$
				Options options = new Options(
						TITLE_OPTION, title, 
						REUSE_TAB_OPTION, true, 
						OWNER_OPTION, LogView.this); 
				
				fireBroadcastEvent(new EventObject(LOG_SELECTION_CHANGED, 
						"item", selectedRecord, "options", options)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
}
