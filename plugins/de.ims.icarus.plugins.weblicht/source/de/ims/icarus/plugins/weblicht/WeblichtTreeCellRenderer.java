package de.ims.icarus.plugins.weblicht;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.ims.icarus.plugins.weblicht.webservice.Webchain;
import de.ims.icarus.plugins.weblicht.webservice.WebchainInputType;
import de.ims.icarus.plugins.weblicht.webservice.WebchainOutputType;
import de.ims.icarus.plugins.weblicht.webservice.WebchainRegistry;
import de.ims.icarus.plugins.weblicht.webservice.Webservice;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.ui.CompoundIcon;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.util.Wrapper;


public class WeblichtTreeCellRenderer extends DefaultTreeCellRenderer{


	private static final long serialVersionUID = -2589454462089491253L;
	private static CompoundIcon outputIcon;
	private static CompoundIcon webchainIcon;

	public WeblichtTreeCellRenderer() {
		setLeafIcon(null);
		setClosedIcon(null);
		setOpenIcon(null);
	}

	/**
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {	
		
		/*if(value instanceof DefaultMutableTreeNode) {
			value = ((DefaultMutableTreeNode)value).getUserObject();
		}*/
		
		if (value instanceof Wrapper) {
			value = ((Wrapper<?>)value).get();
		}
		
		Icon icon = null;
		String tooltip = ""; //$NON-NLS-1$
		if(value instanceof Webchain) {
			Webchain chain = (Webchain)value;
			value = chain.getName();
			tooltip = chain.getName();
			if (webchainIcon == null){
				webchainIcon = new CompoundIcon(IconRegistry.getGlobalRegistry().getIcon("link_obj_dark.gif")); //$NON-NLS-1$
			}
			if (WebchainRegistry.getInstance().hasChainOutput(chain)) {
				webchainIcon.setBottomLeftOverlay(null);
			} else {
				webchainIcon.setBottomLeftOverlay(IconRegistry.getGlobalRegistry().getIcon("warning_co.gif")); //$NON-NLS-1$
				tooltip = tooltip + ResourceManager.getInstance().get("plugins.weblicht.tooltip.noOutputInChain"); //$NON-NLS-1$
			}
			icon = webchainIcon;
			
		} else if(value instanceof Webservice) {
			Webservice webservice = (Webservice)value;
			value = webservice.getName();			
			icon = IconRegistry.getGlobalRegistry().getIcon("repository_rep.gif"); //$NON-NLS-1$
			tooltip = webservice.getDescription();
		
		} else if(value instanceof WebchainOutputType) {
			WebchainOutputType wo = (WebchainOutputType)value;
			value = ResourceManager.getInstance().get("output") //$NON-NLS-1$
					+ wo.getOutputType();			
			if (outputIcon == null){
				outputIcon = new CompoundIcon(IconRegistry.getGlobalRegistry().getIcon("outrepo_rep.gif")); //$NON-NLS-1$
			}
			if (wo.getIsOutputUsed()) {
				outputIcon.setBottomLeftOverlay(IconRegistry.getGlobalRegistry().getIcon("task_complete")); //$NON-NLS-1$
				tooltip = ResourceManager.getInstance().get("enabledOutput"); //$NON-NLS-1$
			} else {
				outputIcon.setBottomLeftOverlay(IconRegistry.getGlobalRegistry().getIcon("unconfigured_co.gif")); //$NON-NLS-1$
				tooltip = ResourceManager.getInstance().get("disabledOutput"); //$NON-NLS-1$
			}
			icon = outputIcon;
			
		} else if(value instanceof WebchainInputType) {
			WebchainInputType wit = (WebchainInputType)value;
			value = ResourceManager.getInstance().get("input") //$NON-NLS-1$
					+ wit.getInputType();
			icon = IconRegistry.getGlobalRegistry().getIcon("addrepo_rep.gif"); //$NON-NLS-1$
			tooltip = ResourceManager.getInstance().get("input")  //$NON-NLS-1$
					+ wit.getInputType();
		}
		
		
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		
		//FIXME set max width of Tooltip
		setToolTipText(UIUtil.toSwingTooltip(tooltip));
		setIcon(icon);
		
		return this;
	}

}