/*
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package net.ikarus_systems.icarus.plugins.jgraph.view;

import java.awt.BorderLayout;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.ikarus_systems.icarus.logging.LoggerFactory;
import net.ikarus_systems.icarus.plugins.core.View;
import net.ikarus_systems.icarus.resources.ResourceManager;
import net.ikarus_systems.icarus.ui.helper.UIHelperRegistry;
import net.ikarus_systems.icarus.ui.view.ListPresenter;
import net.ikarus_systems.icarus.ui.view.PresenterUtils;
import net.ikarus_systems.icarus.ui.view.UnsupportedPresentationDataException;
import net.ikarus_systems.icarus.util.Options;
import net.ikarus_systems.icarus.util.data.ContentType;
import net.ikarus_systems.icarus.util.data.ContentTypeRegistry;
import net.ikarus_systems.icarus.util.data.DataList;
import net.ikarus_systems.icarus.util.mpi.Commands;
import net.ikarus_systems.icarus.util.mpi.Message;
import net.ikarus_systems.icarus.util.mpi.ResultMessage;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ListGraphView extends View {
	
	protected GraphPresenter graphPresenter;
	protected ListPresenter listPresenter;
	
	protected JLabel infoLabel;
	protected JSplitPane splitPane;
	
	protected Handler handler;
	
	public ListGraphView() {
		// no-op
	}

	/**
	 * @see net.ikarus_systems.icarus.plugins.core.View#init(javax.swing.JComponent)
	 */
	@Override
	public void init(JComponent container) {		
		container.setLayout(new BorderLayout());
		
		handler = createHandler();
		
		infoLabel = new JLabel();
		infoLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		container.add(infoLabel, BorderLayout.NORTH);
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerSize(5);
		splitPane.setBorder(null);
		splitPane.setResizeWeight(1);
		container.add(splitPane, BorderLayout.CENTER);
		
		showInfo(null);
	}
	
	protected Handler createHandler() {
		return new Handler();
	}
	
	protected void setGraphPresenter(GraphPresenter graphPresenter) {
		if(this.graphPresenter==graphPresenter) {
			return;
		}
		
		if(this.graphPresenter!=null) {
			this.graphPresenter.close();
		}
		
		this.graphPresenter = graphPresenter;
		
		if(this.graphPresenter!=null) {
			this.graphPresenter.init();
			splitPane.setLeftComponent(graphPresenter.getPresentingComponent());
		} else {
			showGraphInfo(null);
		}
	}
	
	protected void setListPresenter(ListPresenter listPresenter) {
		if(this.listPresenter==listPresenter) {
			return;
		}
		
		if(this.listPresenter!=null) {
			this.listPresenter.getSelectionModel().removeListSelectionListener(handler);
		}
		
		this.listPresenter = listPresenter;
		
		if(this.listPresenter!=null) {
			this.listPresenter.getSelectionModel().addListSelectionListener(handler);
			
			splitPane.setRightComponent(listPresenter.getPresentingComponent());
		} else {
			showInfo(null);
		}
	}
	
	protected void displaySelectedData() throws Exception {
		if(listPresenter==null || graphPresenter==null) {
			return;
		}
		
		ListSelectionModel selectionModel = listPresenter.getSelectionModel();
		
		if(selectionModel.getValueIsAdjusting()) {
			return;
		}
		
		int selectedIndex = selectionModel.getMinSelectionIndex();
		Object selectedObject = null;
		
		if(selectedIndex!=-1) {
			selectedObject = listPresenter.getListModel().getElementAt(selectedIndex);
		}
		
		if(selectedObject==null) {
			graphPresenter.clear();
		} else {
			Options options = new Options();
			options.put(Options.INDEX, selectedIndex);
			options.put(Options.CONTENT_TYPE, listPresenter.getContentType());
			
			graphPresenter.present(selectedObject, options);
		}
	}
	
	protected void displayData(Object data, Options options) {
		
		// Show default info if nothing available to be displayed
		if(data==null) {
			showInfo(null);
			return;
		}
		
		selectViewTab();
		
		DataList<?> dataList = (DataList<?>) data;
		
		if(options==null) {
			options = Options.emptyOptions;
		}
		
		// Ensure list presenter
		ListPresenter listPresenter = this.listPresenter;
		if(listPresenter==null || !PresenterUtils.presenterSupports(listPresenter, data)) {
			listPresenter = UIHelperRegistry.globalRegistry().findHelper(ListPresenter.class, data);
		}
		
		// Signal missing list presenter
		if(listPresenter==null) {
			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.unsupportedListType", data.getClass()); //$NON-NLS-1$
			showInfo(text);
			return;
		}
		
		// Ensure graph presenter
		ContentType entryType = dataList.getContentType();
		entryType = ContentTypeRegistry.getInstance().getType("DependencyDataContentType");
		GraphPresenter graphPresenter = this.graphPresenter;
		if(graphPresenter==null || !PresenterUtils.presenterSupports(graphPresenter, data)) {
			graphPresenter = UIHelperRegistry.globalRegistry().findHelper(GraphPresenter.class, entryType, true, true);
		}
		
		// Signal missing list presenter
		if(graphPresenter==null) {
			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.unsupportedEntryType", entryType.getId()); //$NON-NLS-1$
			showInfo(text);
			return;
		}
		
		// Now present data
		try {
			listPresenter.present(dataList, options);
		} catch (UnsupportedPresentationDataException e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to present data list: "+dataList, e); //$NON-NLS-1$

			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.presentationFailed", data.getClass()); //$NON-NLS-1$
			showInfo(text);
			return;
		}
		
		setListPresenter(listPresenter);
		setGraphPresenter(graphPresenter);
		
		try {
			displaySelectedData();
		} catch (Exception e) {
			LoggerFactory.log(this, Level.SEVERE, 
					"Failed to present selected item", e); //$NON-NLS-1$

			String text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.presentationFailed", entryType.getId()); //$NON-NLS-1$
			showGraphInfo(text);
			return;
		}
		
		infoLabel.setVisible(false);
		splitPane.setVisible(true);
	}
	
	protected void showInfo(String text) {
		if(text==null) {
			text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.notAvailable"); //$NON-NLS-1$
		}
		infoLabel.setText(text);
		
		infoLabel.setVisible(true);
		splitPane.setVisible(false);
		splitPane.setLeftComponent(null);
		splitPane.setRightComponent(null);
		
		// Close any active presenter and discard its reference
		if(graphPresenter!=null) {
			graphPresenter.close();
			graphPresenter = null;
		}		
		if(listPresenter!=null) {
			listPresenter.close();
			listPresenter = null;
		}
	}
	
	protected void showGraphInfo(String text) {
		if(text==null) {
			text = ResourceManager.getInstance().get(
					"plugins.jgraph.listGraphView.notAvailable"); //$NON-NLS-1$
		}
		
		JLabel label = new JLabel(text);
		label.setBorder(new EmptyBorder(10, 10, 10, 10));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		
		splitPane.setLeftComponent(label);
		
		// Close any active presenter and discard its reference
		if(graphPresenter!=null) {
			graphPresenter.close();
			graphPresenter = null;
		}
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		super.reset();
	}

	/**
	 * Accepted commands:
	 * <ul>
	 * <li>{@link Commands#DISPLAY}</li>
	 * <li>{@link Commands#PRESENT}</li>
	 * <li>{@link Commands#SELECT}</li>
	 * <li>{@link Commands#CLEAR}</li>
	 * </ul>
	 * 
	 * @see net.ikarus_systems.icarus.plugins.core.View#handleRequest(net.ikarus_systems.icarus.util.mpi.Message)
	 */
	@Override
	protected ResultMessage handleRequest(Message message) throws Exception {
		if(Commands.PRESENT.equals(message.getCommand())
				|| Commands.DISPLAY.equals(message.getCommand())) {
			
			Object data = message.getData();
			if(!(data instanceof DataList)) {
				return message.unsupportedDataResult(this);
			}
			
			displayData(data, message.getOptions());
			
			return message.successResult(this, null);
		} else if(Commands.SELECT.equals(message.getCommand())) {
			if(listPresenter==null) {
				return message.errorResult(this, null);
			}
			
			// Accept index 
			int selectedIndex = -1;
			if(message.getData() instanceof Integer) {
				selectedIndex = (int)message.getData();
			}
			//TODO convert data object into index
			
			Object selectedItem = null;
			if(selectedIndex==-1) {
				listPresenter.getSelectionModel().clearSelection();
			} else {
				listPresenter.getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
			}
			
			return message.successResult(this, selectedItem);
		} else if(Commands.CLEAR.equals(message.getCommand())) {
			reset();
			return message.successResult(this, null);
		} else {
			return message.unknownRequestResult(this);
		}
	}
	
	protected class Handler implements ListSelectionListener {

		/**
		 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
		 */
		@Override
		public void valueChanged(ListSelectionEvent e) {
			try {
				displaySelectedData();
			} catch(Exception ex) {
				LoggerFactory.log(this, Level.SEVERE, 
						"Failed to handle change in selection: "+e, ex); //$NON-NLS-1$
			}
		}
		
	}
}