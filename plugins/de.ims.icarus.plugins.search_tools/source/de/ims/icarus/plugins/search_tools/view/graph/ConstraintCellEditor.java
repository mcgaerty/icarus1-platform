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
 *
 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$ 
 * $LastChangedRevision$ 
 * $LastChangedBy$
 */
package de.ims.icarus.plugins.search_tools.view.graph;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.view.mxGraph;

import de.ims.icarus.language.LanguageUtils;
import de.ims.icarus.logging.LoggerFactory;
import de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor;
import de.ims.icarus.plugins.search_tools.view.SearchUtilityListCellRenderer;
import de.ims.icarus.resources.Localizable;
import de.ims.icarus.resources.ResourceManager;
import de.ims.icarus.search_tools.ConstraintContext;
import de.ims.icarus.search_tools.ConstraintFactory;
import de.ims.icarus.search_tools.EdgeType;
import de.ims.icarus.search_tools.Grouping;
import de.ims.icarus.search_tools.NodeType;
import de.ims.icarus.search_tools.SearchConstraint;
import de.ims.icarus.search_tools.SearchManager;
import de.ims.icarus.search_tools.SearchOperator;
import de.ims.icarus.search_tools.standard.DefaultConstraint;
import de.ims.icarus.search_tools.util.SearchUtils;
import de.ims.icarus.ui.IconRegistry;
import de.ims.icarus.ui.UIUtil;
import de.ims.icarus.ui.dialog.ChoiceFormEntry;
import de.ims.icarus.ui.dialog.ControlFormEntry;
import de.ims.icarus.ui.dialog.FormBuilder;
import de.ims.icarus.ui.dialog.FormBuilder.FormEntry;
import de.ims.icarus.util.HtmlUtils.HtmlTableBuilder;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ConstraintCellEditor extends HeavyWeightCellEditor implements PropertyChangeListener {
	
	private static AtomicInteger idGen = new AtomicInteger();
	
	public ConstraintCellEditor(ConstraintGraphPresenter presenter) {
		super(presenter);
		
		presenter.addPropertyChangeListener("constraintContext", this); //$NON-NLS-1$
	}

	@Override
	protected void buildEditors() {
		if(getPresenter().getConstraintContext()==null) {
			return;
		}
		
		super.buildEditors();
	}

	@Override
	public ConstraintGraphPresenter getPresenter() {
		return (ConstraintGraphPresenter) super.getPresenter();
	}

	@Override
	public FormBuilder getVertexEditor() {
		return (FormBuilder) vertexEditor;
	}

	@Override
	public FormBuilder getEdgeEditor() {
		return (FormBuilder) edgeEditor;
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if("constraintContext".equals(evt.getPropertyName())) { //$NON-NLS-1$
			buildEditors();
		}
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#getEditorComponent(java.lang.Object)
	 */
	@Override
	protected JComponent getEditorComponent(Object editor) {
		if(editor instanceof FormBuilder) {
			return (JComponent) ((FormBuilder)editor).getContainer();
		} else 
			throw new IllegalArgumentException("Editor is not a form builder instance: "+editor); //$NON-NLS-1$
	}
	
	protected JLabel createInfoLabel() {
		
		final JLabel label = new JLabel();
		label.addMouseListener(new MouseAdapter() {
			
			private int dismissDelayReminder = -1;
			private int initialDelayReminder = -1;
			
			/**
			 * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseEntered(MouseEvent e) {
				initialDelayReminder = ToolTipManager.sharedInstance().getInitialDelay();
				dismissDelayReminder = ToolTipManager.sharedInstance().getDismissDelay();
				
				ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
				ToolTipManager.sharedInstance().setInitialDelay(0);
			}
			
			/**
			 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseExited(MouseEvent e) {
				if(dismissDelayReminder!=-1) {
					ToolTipManager.sharedInstance().setDismissDelay(dismissDelayReminder);
					dismissDelayReminder = -1;
				}
				if(initialDelayReminder!=-1) {
					ToolTipManager.sharedInstance().setInitialDelay(initialDelayReminder);
					initialDelayReminder = -1;
				}
			}
		});
		label.setIcon(IconRegistry.getGlobalRegistry().getIcon("smartmode_co.gif")); //$NON-NLS-1$
		
		Localizable localizable = new Localizable() {
			
			@Override
			public void localize() {
				label.setToolTipText(createOperatorTooltip());
			}
		};
		
		localizable.localize();
		ResourceManager.getInstance().getGlobalDomain().addItem(localizable);
		
		return label;
	}
	
	protected String createOperatorTooltip() {
		HtmlTableBuilder builder = new HtmlTableBuilder();
		
		builder.start(3);
		
		builder.addRow(
				ResourceManager.getInstance().get("plugins.searchTools.labels.symbol"), //$NON-NLS-1$
				ResourceManager.getInstance().get("plugins.searchTools.labels.operator"), //$NON-NLS-1$
				ResourceManager.getInstance().get("plugins.searchTools.labels.explanation")); //$NON-NLS-1$
		
		for(SearchOperator operator : SearchOperator.values()) {
			String name = operator.getName();
			String desc = operator.getDescription();
			if(name.equals(desc)) {
				desc = ""; //$NON-NLS-1$
			}
			builder.addRowEscaped(operator.getSymbol(), name, desc);
		}
		
		builder.finish();
		
		return builder.getResult();
	}
	
	protected NodeType[] allowedNodeTypes = {
		NodeType.GENERAL,
		NodeType.DISJUNCTION,
		NodeType.ROOT,
		NodeType.LEAF,
		NodeType.INTERMEDIATE,
	};

	protected EdgeType[] allowedEdgeTypes = {
		EdgeType.DOMINANCE,
		EdgeType.PRECEDENCE,
		EdgeType.TRANSITIVE,
	};
	
	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#createVertexEditor()
	 */
	@Override
	protected Object createVertexEditor() {
		FormBuilder vertexForms = FormBuilder.newLocalizingBuilder();
		
		List<ConstraintFactory> factories = new ArrayList<>(
				getPresenter().getConstraintContext().getNodeFactories());
		Collections.sort(factories, SearchUtils.factorySorter);
		
		if(!factories.isEmpty()) {
			// NEGATED
			vertexForms.addToggleFormEntry("negated", "plugins.searchTools.labels.negated"); //$NON-NLS-1$ //$NON-NLS-2$

			// NODE TYPE
			ChoiceFormEntry entry = new ChoiceFormEntry(
					"plugins.searchTools.labels.nodeType",  //$NON-NLS-1$
					NodeType.values());
			entry.setResizeMode(FormBuilder.RESIZE_HORIZONTAL);
			entry.getComboBox().setRenderer(sharedRenderer);
			UIUtil.resizeComponent(entry.getComboBox(), 100, 20);
			vertexForms.addEntry("nodeType", entry); //$NON-NLS-1$
			
			// CONSTRAINTS
			vertexForms.addEntry("constraints", new ConstraintGroupFormEntry(true)); //$NON-NLS-1$
//			for(ConstraintFactory factory : factories) {
//				int min = SearchUtils.getMinInstanceCount(factory);
//				int max = factory.getMaxInstanceCount();
//				if(max!=-1 && max<min)
//					throw new IllegalArgumentException("Max instance count of factory is too small: "+factory.getClass()); //$NON-NLS-1$
//				
//				for(int i=0; i<=min; i++) {
//					vertexForms.addEntry(createConstraintId(),
//						new ConstraintFormEntry(factory));
//				}
//			}
		}
		
		// BUTTONS
		vertexForms.addEntry("control", new ControlFormEntry( //$NON-NLS-1$
				cancelEditingAction, textSubmitAction));
		
		vertexForms.buildForm();
		
		JLabel label = createInfoLabel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = GridBagConstraints.REMAINDER;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		
		vertexForms.getContainer().add(label, gbc);
		
		vertexForms.pack();
		
		return vertexForms;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#createEdgeEditor()
	 */
	@Override
	protected Object createEdgeEditor() {
		FormBuilder edgeForms = FormBuilder.newLocalizingBuilder();
		
		List<ConstraintFactory> factories = new ArrayList<>(
				getPresenter().getConstraintContext().getEdgeFactories());
		Collections.sort(factories, SearchUtils.factorySorter);
		
		if(!factories.isEmpty()) {
			// NEGATED
			edgeForms.addToggleFormEntry("negated", "plugins.searchTools.labels.negated"); //$NON-NLS-1$ //$NON-NLS-2$
			
			// EDGE TYPE
			ChoiceFormEntry entry = new ChoiceFormEntry(
					"plugins.searchTools.labels.edgeType",  //$NON-NLS-1$
					EdgeType.values());
			entry.setResizeMode(FormBuilder.RESIZE_HORIZONTAL);
			entry.getComboBox().setRenderer(sharedRenderer);
			UIUtil.resizeComponent(entry.getComboBox(), 100, 20);
			edgeForms.addEntry("edgeType", entry); //$NON-NLS-1$
			
			// CONSTRAINTS
			edgeForms.addEntry("constraints", new ConstraintGroupFormEntry(false)); //$NON-NLS-1$
//			for(ConstraintFactory factory : factories) {
//				int min = SearchUtils.getMinInstanceCount(factory);
//				int max = factory.getMaxInstanceCount();
//				if(max!=-1 && max<min)
//					throw new IllegalArgumentException("Max instance count of factory is too small: "+factory.getClass()); //$NON-NLS-1$
//				
//				for(int i=0; i<=min; i++) {
//					edgeForms.addEntry(createConstraintId(),
//						new ConstraintFormEntry(factory));
//				}
//			}
		}
		
		// BUTTONS
		edgeForms.addEntry("control", new ControlFormEntry( //$NON-NLS-1$
				cancelEditingAction, textSubmitAction));
		
		edgeForms.buildForm();
		
		JLabel label = createInfoLabel();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = GridBagConstraints.REMAINDER;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.NORTHEAST;
		
		edgeForms.getContainer().add(label, gbc);
		
		edgeForms.pack();
		
		return edgeForms;
	}
	
	public static int getInstanceCount(FormBuilder formBuilder, ConstraintFactory factory) {
		if(formBuilder==null) {
			return 0;
		}
		
		int count = 0;
		for(int i=0; i<formBuilder.getEntryCount(); i++) {
			FormEntry entry = formBuilder.getEntry(i);
			if(entry instanceof ConstraintFormEntry) {
				ConstraintFormEntry constraintEntry = (ConstraintFormEntry) entry;
				if(constraintEntry.getFactory()==factory) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#initVertexEditor(java.lang.Object)
	 */
	@Override
	protected void initVertexEditor(Object value) {
		ConstraintNodeData nodeData = (ConstraintNodeData) value;
		
		FormBuilder formBuilder = getVertexEditor();
		
		formBuilder.setValue("negated", nodeData.isNegated()); //$NON-NLS-1$
		formBuilder.setValue("nodeType", nodeData.getNodeType()); //$NON-NLS-1$
		
//		SearchConstraint[] constraints = nodeData.getConstraints();
//		for(int i=0; i<constraints.length; i++) {
//			formBuilder.setValue("constraint_"+i, constraints[i]); //$NON-NLS-1$
//		}
		formBuilder.setValue("constraints", nodeData.getConstraints()); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#initEdgeEditor(java.lang.Object)
	 */
	@Override
	protected void initEdgeEditor(Object value) {
		ConstraintEdgeData edgeData = (ConstraintEdgeData) value;
		
		FormBuilder formBuilder = getEdgeEditor();
		
		formBuilder.setValue("negated", edgeData.isNegated()); //$NON-NLS-1$
		formBuilder.setValue("edgeType", edgeData.getEdgeType()); //$NON-NLS-1$
		
//		SearchConstraint[] constraints = edgeData.getConstraints();
//		for(int i=0; i<constraints.length; i++) {
//			formBuilder.setValue("constraint_"+i, constraints[i]); //$NON-NLS-1$
//		}
		formBuilder.setValue("constraints", edgeData.getConstraints()); //$NON-NLS-1$
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#readVertexEditor(java.lang.Object)
	 */
	@Override
	protected void readVertexEditor(Object cell) {
		List<ConstraintFactory> factories = getPresenter().getConstraintContext().getNodeFactories();
		if(factories.isEmpty()) {
			LoggerFactory.log(this, Level.WARNING, "Missing node constraint factories on constraint presenter"); //$NON-NLS-1$
			return;
		}
		
		ConstraintNodeData nodeData = new ConstraintNodeData(factories.size());
		FormBuilder formBuilder = getVertexEditor();
		
		// Negated state
		nodeData.setNegated((boolean) formBuilder.getValue("negated")); //$NON-NLS-1$
		
		// Node type
		nodeData.setNodeType((NodeType) formBuilder.getValue("nodeType")); //$NON-NLS-1$
		
		// Constraints
//		for(int i=0; i<factories.size(); i++) {
//			nodeData.setConstraint(i, (SearchConstraint) formBuilder.getValue("constraint_"+i)); //$NON-NLS-1$
//		}
		nodeData.setConstraints((SearchConstraint[]) formBuilder.getValue("constraints")); //$NON-NLS-1$

		mxGraph graph = getPresenter().getGraph();
		mxIGraphModel model = graph.getModel();
		
		model.beginUpdate();
		try {
			model.setValue(cell, nodeData);
			getPresenter().refreshCells(cell);
		} finally {
			model.endUpdate();
		}
	}

	/**
	 * @see de.ims.icarus.plugins.jgraph.view.HeavyWeightCellEditor#readEdgeEditor(java.lang.Object)
	 */
	@Override
	protected void readEdgeEditor(Object cell) {
		List<ConstraintFactory> factories = getPresenter().getConstraintContext().getEdgeFactories();
		if(factories.isEmpty()) {
			LoggerFactory.log(this, Level.WARNING, "Missing node constraint factories on constraint presenter"); //$NON-NLS-1$
			return;
		}
		
		ConstraintEdgeData edgeData = new ConstraintEdgeData(factories.size());
		FormBuilder formBuilder = getEdgeEditor();
		
		// Negated state
		edgeData.setNegated((boolean) formBuilder.getValue("negated")); //$NON-NLS-1$
		
		// Edge style
		edgeData.setEdgeType((EdgeType) formBuilder.getValue("edgeType")); //$NON-NLS-1$
		
		// Constraints
//		for(int i=0; i<factories.size(); i++) {
//			edgeData.setConstraint(i, (SearchConstraint) formBuilder.getValue("constraint_"+i)); //$NON-NLS-1$
//		}
		edgeData.setConstraints((SearchConstraint[]) formBuilder.getValue("constraints")); //$NON-NLS-1$

		mxGraph graph = getPresenter().getGraph();
		mxIGraphModel model = graph.getModel();
		
		model.beginUpdate();
		try {
			model.setValue(cell, edgeData);
			getPresenter().refreshCells(cell);
		} finally {
			model.endUpdate();
		}

	}
	
	public static String createConstraintId() {
		return "constraint_"+idGen.incrementAndGet(); //$NON-NLS-1$
	}
	
	@SuppressWarnings("serial")
	private static class NumberDocument extends PlainDocument {

		@Override
		public void insertString(int offset, String str, AttributeSet a)
				throws BadLocationException {
			super.insertString(offset, str, a);
			String newText = getText(0, getLength());
			if (!(newText.equals(LanguageUtils.DATA_UNDEFINED_LABEL) 
					|| newText.matches("^\\d*$"))) { //$NON-NLS-1$
				remove(offset, str.length());
			}
		}
	}

	private static class GroupingCellRenderer extends DefaultListCellRenderer implements Icon {

		private static final long serialVersionUID = 9171727683720878063L;
		
		private Grouping grouping;
		
		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			grouping = Grouping.getGrouping((int) value);
			setText(String.valueOf(grouping.getIndex()+1));
			setIcon(this);
			
			return this;
		}

		/**
		 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
		 */
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			x += 4;
			y += 4;
			
			g.setColor(grouping.getColor());
			g.fillOval(x, y, 8, 8);
		}

		/**
		 * @see javax.swing.Icon#getIconWidth()
		 */
		@Override
		public int getIconWidth() {
			return 16;
		}

		/**
		 * @see javax.swing.Icon#getIconHeight()
		 */
		@Override
		public int getIconHeight() {
			return 16;
		}
		
	}
	
	private static SearchUtilityListCellRenderer sharedRenderer =
			new SearchUtilityListCellRenderer();
	
	private static GroupingCellRenderer sharedGroupingRenderer =
			new GroupingCellRenderer();
	
	private class ConstraintGroupFormEntry extends FormEntry implements ActionListener {
		
		private FormBuilder formBuilder;
		private JPanel container;
		private final boolean isVertex;

		ConstraintGroupFormEntry(boolean isVertex) {
			container = new JPanel();
			formBuilder = FormBuilder.newLocalizingBuilder(container);
			this.isVertex = isVertex;
		}
		
		private List<ConstraintFactory> getFactories() {
			ConstraintContext context = getPresenter().getConstraintContext();
			return isVertex ? context.getNodeFactories() : context.getEdgeFactories();
		}
		
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			ConstraintFormEntry entry = (ConstraintFormEntry) button.getClientProperty("owner"); //$NON-NLS-1$
			boolean add = Boolean.parseBoolean(e.getActionCommand());
			
			if(add) {
				ConstraintFormEntry newEntry = cloneEntry(entry);
				formBuilder.insertEntry(createConstraintId(), newEntry, entry.getId());
				newEntry.refreshButtons();
			} else {
				formBuilder.removeEntry(entry);
			}
			
			formBuilder.buildForm();
			formBuilder.pack();
			
			FormBuilder rootBuilder = getBuilder();
			rootBuilder.pack();
			rootBuilder.getContainer().revalidate();
			rootBuilder.getContainer().repaint();
		}
		
		private ConstraintFormEntry cloneEntry(ConstraintFormEntry entry) {
			return new ConstraintFormEntry(entry.getFactory(), this);
		}
		
		private ConstraintFormEntry newEntry(ConstraintFactory factory) {
			return new ConstraintFormEntry(factory, this);
		}

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#addToForm(de.ims.icarus.ui.dialog.FormBuilder)
		 */
		@Override
		public ConstraintGroupFormEntry addToForm(FormBuilder builder) {
			builder.feedComponent(container, null, FormBuilder.RESIZE_REMAINDER);
			return this;
		}

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
		 */
		@Override
		public ConstraintGroupFormEntry setValue(Object value) {
			
			formBuilder.removeAllEntries();
			
			SearchConstraint[] constraints = (SearchConstraint[])value;
			if(constraints==null) {
				constraints = new SearchConstraint[0];
			}
			
			Map<String, List<SearchConstraint>> cmap = new HashMap<>();
			for(SearchConstraint constraint : constraints) {
				List<SearchConstraint> list = cmap.get(constraint.getToken());
				if(list==null) {
					list = new LinkedList<>();
					cmap.put(constraint.getToken(), list);
				}
				list.add(constraint);
			}
			
			List<ConstraintFactory> factories = getFactories();
			for(ConstraintFactory factory : factories) {
				int min = SearchUtils.getMinInstanceCount(factory);
				int max = SearchUtils.getMaxInstanceCount(factory);
				List<SearchConstraint> items = cmap.get(factory.getToken());
				if(items==null) {
					items = Collections.emptyList();
				}
				
				max = Math.min(max, items.size());
				min = Math.max(min, max);
				for(int i=0; i<min; i++) {
					ConstraintFormEntry entry = newEntry(factory);
					if(i<items.size()) {
						entry.setValue(items.get(i));
					}
					formBuilder.addEntry(createConstraintId(), entry);
					entry.refreshButtons();
				}
			}
			
			formBuilder.buildForm();
			formBuilder.pack();
			
			return this;
		}

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
		 */
		@Override
		public Object getValue() {
			List<SearchConstraint> constraints = new ArrayList<>();
			
			for(int i=0; i<formBuilder.getEntryCount(); i++) {
				SearchConstraint constraint = (SearchConstraint) formBuilder.getValue(i);
				
				if(constraint!=null) {
					constraints.add(constraint);
				}
			}
			
			return SearchUtils.toArray(constraints);
		}

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#clear()
		 */
		@Override
		public ConstraintGroupFormEntry clear() {
			formBuilder.clear();
			return this;
		}
		
	}
	
	private static class ConstraintFormEntry extends FormEntry implements ActionListener {
		
		private JComboBox<SearchOperator> operatorSelect;
		private JComboBox<Object> valueSelect;
		private JComboBox<Object> specifierSelect;
		private JLabel label;
		
		private JButton addButton, removeButton;
		private JCheckBox toggleInclude;
		
		private ConstraintFactory factory;
		private boolean displayingGroups = false;
		
		ConstraintFormEntry(ConstraintFactory factory, ActionListener listener) {
			this.factory = factory;
			
			SearchOperator[] operators = factory.getSupportedOperators();
			
			operatorSelect = new JComboBox<>(operators);
			operatorSelect.setEditable(false);
			operatorSelect.setRenderer(sharedRenderer);
			operatorSelect.setMaximumRowCount(operators.length);
			operatorSelect.addActionListener(this);
			UIUtil.fitToContent(operatorSelect, 60, 100, 20);
			
			Object[] specifiers = factory.getSupportedSpecifiers();
			specifierSelect = specifiers==null ? new JComboBox<>() : new JComboBox<>(specifiers);
			specifierSelect.setEditable(true);
			specifierSelect.setRenderer(sharedRenderer);
			UIUtil.fitToContent(specifierSelect, 60, 100, 20);
			specifierSelect.setVisible(specifiers!=null);
			
			addButton = createHelperButton(true, listener);
			removeButton = createHelperButton(false, listener);
			
			toggleInclude = new JCheckBox();
			
			Object[] labelSet = factory.getLabelSet();
			Class<?> valueClass = factory.getValueClass();
			Object defaultValue = factory.getDefaultValue();

			valueSelect = labelSet==null ? new JComboBox<>() : new JComboBox<>(labelSet);
			
			if(Integer.class.equals(valueClass)) {
				JTextComponent editor = (JTextComponent) valueSelect.getEditor().getEditorComponent();
				editor.setDocument(new NumberDocument());
			}
			
			valueSelect.setEditable(valueClass!=null);
			
			valueSelect.setSelectedItem(defaultValue);
			
			
			UIUtil.resizeComponent(valueSelect, 100, 20);
			
			label = new JLabel(factory.getName());
			
			refreshButtons();
		}
		
		private JButton createHelperButton(boolean add, ActionListener listener) {
			JButton b = new JButton();
			String iconName = add ? "add_stat.gif" : "del_stat.gif"; //$NON-NLS-1$ //$NON-NLS-2$
			b.setIcon(IconRegistry.getGlobalRegistry().getIcon(iconName));
			b.setHorizontalAlignment(SwingConstants.CENTER);
			b.setVerticalAlignment(SwingConstants.CENTER);
			b.setFocusable(false);
			b.setFocusPainted(false);
			b.addActionListener(listener);
			b.putClientProperty("owner", this); //$NON-NLS-1$
			b.setActionCommand(String.valueOf(add));
			
			UIUtil.resizeComponent(b, 13, 12);
			
			return b;
		}

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#setValue(java.lang.Object)
		 */
		@Override
		public ConstraintFormEntry setValue(Object value) {
			displayingGroups = false;
			
			SearchConstraint constraint = (SearchConstraint)value;
			
			if(!constraint.getToken().equals(factory.getToken()))
				throw new IllegalArgumentException("Factory not designed to handle constraints for token: "+constraint.getToken()); //$NON-NLS-1$
			
			Object currentValue = factory.valueToLabel(constraint.getValue());
			
			operatorSelect.setSelectedItem(constraint.getOperator());
			label.setText(factory.getName());
			label.setToolTipText(factory.getDescription());
			toggleInclude.setSelected(constraint.isActive());
			
			displayingGroups = SearchManager.isGroupingOperator(constraint.getOperator());
			if(displayingGroups) {
				displayGroups((int)constraint.getValue());
			} else {
				displayValue(currentValue);
			}
			specifierSelect.setSelectedItem(constraint.getSpecifier());
			
			return this;
		}

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#getValue()
		 */
		@Override
		public Object getValue() {			
			SearchOperator operator = (SearchOperator) operatorSelect.getSelectedItem();
			Object value = null;
			
			if(!displayingGroups) {
				value = factory.labelToValue(valueSelect.getSelectedItem());
				if(value instanceof String && Integer.class.equals(factory.getValueClass())) {
					value = Integer.parseInt((String)value);
				}
			} else {
				value = valueSelect.getSelectedIndex();
			}

			Object specifier = specifierSelect.isVisible() ? specifierSelect.getSelectedItem() : null;
			
			SearchConstraint constraint = new DefaultConstraint(
					factory.getToken(), value, operator, specifier);
			constraint.setActive(toggleInclude.isSelected());
			return constraint;
		}

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#clear()
		 */
		@Override
		public ConstraintFormEntry clear() {
			operatorSelect.setSelectedIndex(0);
			
			if(valueSelect.isEditable()) {
				valueSelect.setSelectedItem(null);
			} else {
				valueSelect.setSelectedIndex(0);
			}
			
			return this;
		}

		/**
		 * @see de.ims.icarus.ui.dialog.FormBuilder.FormEntry#addToForm(de.ims.icarus.ui.dialog.FormBuilder)
		 */
		@Override
		public ConstraintFormEntry addToForm(FormBuilder builder) {
			builder.feedComponent(label);
			builder.feedComponent(specifierSelect);
			builder.feedComponent(operatorSelect, new Insets(1, 4, 1, 4));
			builder.feedComponent(valueSelect, null, FormBuilder.RESIZE_HORIZONTAL);
			builder.feedComponent(toggleInclude, new Insets(1, 2, 1, 2));
			builder.feedComponent(removeButton, new Insets(1, 1, 1, 1), GridBagConstraints.CENTER, -1);
			builder.feedComponent(addButton, new Insets(1, 1, 1, 1), GridBagConstraints.CENTER, -1);
			builder.newLine();
			
			return this;
		}
		
		public void refreshButtons() {
			int min = SearchUtils.getMinInstanceCount(factory);
			int max = SearchUtils.getMaxInstanceCount(factory);
			int current = getInstanceCount(getBuilder(), factory);
			
			addButton.setEnabled(current<max);
			removeButton.setEnabled(current>min);
		}
		
		private void displayValue(Object value) {
			valueSelect.setRenderer(sharedRenderer);
			DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>) valueSelect.getModel();
			model.removeAllElements();
			
			Object[] labelSet = factory.getLabelSet();
			if(labelSet!=null) {
				for(Object label : labelSet) {
					model.addElement(label);
				}
			}
			
			if(value==null) {
				value = factory.valueToLabel(factory.getDefaultValue());
			}

			valueSelect.setEditable(factory.getValueClass()!=null);
			valueSelect.setSelectedItem(value);
			
			displayingGroups = false;
		}
		
		private void displayGroups(int groupId) {
			displayingGroups = true;
			valueSelect.setRenderer(sharedGroupingRenderer);
			
			DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>) valueSelect.getModel();
			model.removeAllElements();
			for(int i=0; i<=Grouping.getMaxGroupIndex(); i++) {
				model.addElement(i);
			}
			valueSelect.setSelectedIndex(groupId);
			valueSelect.setEditable(false);
		}

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource()==operatorSelect) {
				SearchOperator operator = (SearchOperator) operatorSelect.getSelectedItem();
				if(SearchManager.isGroupingOperator(operator) && !displayingGroups) {
					// Switch to group selection
					displayGroups(0);
				} else if(!SearchManager.isGroupingOperator(operator) && displayingGroups) {
					// Switch to old mode
					displayValue(null);
				}
				
			}
		}
		
		public ConstraintFactory getFactory() {
			return factory;
		}
	}
}
