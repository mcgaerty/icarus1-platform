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
package de.ims.icarus.plugins.prosody;

import java.util.ArrayList;

import org.java.plugin.registry.Extension;

import de.ims.icarus.config.ConfigBuilder;
import de.ims.icarus.config.ConfigConstants;
import de.ims.icarus.config.ConfigUtils;
import de.ims.icarus.plugins.ExtensionListCellRenderer;
import de.ims.icarus.plugins.jgraph.JGraphPreferences;
import de.ims.icarus.plugins.prosody.annotation.ProsodyHighlighting;
import de.ims.icarus.plugins.prosody.painte.PaIntEParams;
import de.ims.icarus.plugins.prosody.pattern.LabelPattern;
import de.ims.icarus.plugins.prosody.ui.TextArea;
import de.ims.icarus.plugins.prosody.ui.geom.AntiAliasingType;
import de.ims.icarus.plugins.prosody.ui.geom.Axis;
import de.ims.icarus.plugins.prosody.ui.geom.GridStyle;
import de.ims.icarus.plugins.prosody.ui.geom.PaIntEGraph;
import de.ims.icarus.plugins.prosody.ui.list.ProsodyListCellRenderer;
import de.ims.icarus.plugins.prosody.ui.view.PreviewSize;
import de.ims.icarus.plugins.prosody.ui.view.outline.SentencePanel.PanelConfig;
import de.ims.icarus.ui.list.TooltipListCellRenderer;
import de.ims.icarus.util.annotation.HighlightType;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public class ProsodyPreferences {

	private static String escapePattern(LabelPattern pattern) {
		String s = pattern.getPattern();
		return s==null ? "" : LabelPattern.escapePattern(s); //$NON-NLS-1$
	}

	private static String escapePattern(String pattern) {
		return pattern==null ? "" : LabelPattern.escapePattern(pattern); //$NON-NLS-1$
	}

	public ProsodyPreferences() {
		ConfigBuilder builder = new ConfigBuilder();

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// JGRAPH GROUP
		builder.addGroup("jgraph", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		// DEFAULT GROUP
		builder.addGroup("prosody", true); //$NON-NLS-1$
		// DEPENDENCY GRAPH GROUP
		builder.addBooleanEntry("showIndex", true); //$NON-NLS-1$
		builder.addBooleanEntry("showLemma", true); //$NON-NLS-1$
		builder.addBooleanEntry("showFeatures", true); //$NON-NLS-1$
		builder.addBooleanEntry("showForm", true); //$NON-NLS-1$
		builder.addBooleanEntry("showPos", true); //$NON-NLS-1$
		builder.addBooleanEntry("showRelation", true); //$NON-NLS-1$
		builder.addBooleanEntry("showDirection", false); //$NON-NLS-1$
		builder.addBooleanEntry("showDistance", false); //$NON-NLS-1$
		builder.addBooleanEntry("markRoot", true); //$NON-NLS-1$
		builder.addBooleanEntry("markNonProjective", false); //$NON-NLS-1$
		JGraphPreferences.buildDefaultGraphConfig(builder, null);
		builder.reset();

		// PLUGINS GROUP
		builder.addGroup("plugins", true); //$NON-NLS-1$
		// COREF GROUP
		builder.addGroup("prosody", true); //$NON-NLS-1$
		// APPEARANCE GROUP
		builder.addGroup("appearance", true); //$NON-NLS-1$
		// OUTLINE GROUP
		builder.addGroup("outline", true); //$NON-NLS-1$

		builder.addOptionsEntry("antiAliasingType", PanelConfig.DEFAULT_ANTIALIASING_TYPE.ordinal(), //$NON-NLS-1$
				(Object[])AntiAliasingType.values());
		builder.addBooleanEntry("mouseWheelScrollSupported", PanelConfig.DEFAULT_MOUSE_WHEEL_SCROLL_SUPPORTED); //$NON-NLS-1$
		builder.addColorEntry("backgroundColor", PanelConfig.DEFAULT_BACKGROUND_COLOR); //$NON-NLS-1$N-NLS-1$
		builder.addBooleanEntry("loopSound", PanelConfig.DEFAULT_LOOP_SOUND); //$NON-NLS-1$
		builder.addColorEntry("wordAlignmentColor", PanelConfig.DEFAULT_WORD_ALIGNMENT_COLOR); //$NON-NLS-1$N-NLS-1$
		builder.addColorEntry("syllableAlignmentColor", PanelConfig.DEFAULT_SYLLABLE_ALIGNMENT_COLOR); //$NON-NLS-1$

		// TEXT GROUP
		builder.addGroup("text", true); //$NON-NLS-1$

		builder.addStringEntry("sentencePattern", escapePattern(PanelConfig.DEFAULT_SENTENCE_PATTERN)); //$NON-NLS-1$
		builder.addStringEntry("headerPattern", escapePattern(PanelConfig.DEFAULT_HEADER_PATTERN)); //$NON-NLS-1$
		builder.addBooleanEntry("showAlignment", PanelConfig.DEFAULT_TEXT_SHOW_ALIGNMENT); //$NO //$NON-NLS-1$
		// FONT SUBGROUP
		builder.addGroup("font", true); //$NON-NLS-1$
		ConfigUtils.buildDefaultFontConfig(builder,
				TextArea.DEFAULT_FONT.getName(),
				TextArea.DEFAULT_FONT.getSize(),
				TextArea.DEFAULT_TEXT_COLOR);
		// END FONT SUBGROUP
		builder.back();

		// END TEXT GROUP
		builder.back();

		// PREVIEW GROUP
		builder.addGroup("preview", true); //$NON-NLS-1$

		builder.addOptionsEntry("previewSize", PanelConfig.DEFAULT_PREVIEW_SIZE.ordinal(), //$NON-NLS-1$
				(Object[])PreviewSize.values());
		builder.addDoubleEntry("leftSyllableBound", PanelConfig.DEFAULT_LEFT_SYLLABLE_BOUND, -2D, 0D, 0.05); //$NON-NLS-1$
		builder.addDoubleEntry("rightSyllableBound", PanelConfig.DEFAULT_RIGHT_SYLLABLE_BOUND, 0D, 2D, 0.05); //$NON-NLS-1$
		builder.addColorEntry("curveColor", PanelConfig.DEFAULT_CURVE_COLOR); //$NON-NLS-1$
		builder.addBooleanEntry("showAlignment", PanelConfig.DEFAULT_PREVIEW_SHOW_ALIGNMENT); //$NO //$NON-NLS-1$

		// END PREVIEW GROUP
		builder.back();

		// DETAIL GROUP
		builder.addGroup("detail", true); //$NON-NLS-1$

		builder.addIntegerEntry("wordScope", PanelConfig.DEFAULT_WORD_SCOPE, 0, 5); //$NON-NLS-1$
		builder.addIntegerEntry("syllableScope", PanelConfig.DEFAULT_SYLLABLE_SCOPE, 1, 10); //$NON-NLS-1$
		builder.addIntegerEntry("graphHeight", PanelConfig.DEFAULT_GRAPH_HEIGHT, 50, 400); //$NON-NLS-1$
		builder.addIntegerEntry("graphWidth", PanelConfig.DEFAULT_GRAPH_WIDTH, 100, 500); //$NON-NLS-1$
		builder.addIntegerEntry("wordSpacing", PanelConfig.DEFAULT_WORD_SPACING, 0, 50); //$NON-NLS-1$
		builder.addIntegerEntry("graphSpacing", PanelConfig.DEFAULT_GRAPH_SPACING, 0, 50); //$NON-NLS-1$
		builder.addBooleanEntry("clearLabelBackground", PanelConfig.DEFAULT_CLEAR_LABEL_BACKGROUND); //$NON-NLS-1$
		builder.addStringEntry("detailPattern", escapePattern(PanelConfig.DEFAULT_DETAIL_PATTERN)); //$NON-NLS-1$
		// FONT SUBGROUP
		builder.addGroup("font", true); //$NON-NLS-1$
		ConfigUtils.buildDefaultFontConfig(builder,
				TextArea.DEFAULT_FONT.getName(),
				TextArea.DEFAULT_FONT.getSize(),
				TextArea.DEFAULT_TEXT_COLOR);
		// END FONT SUBGROUP
		builder.back();
		builder.addColorEntry("axisColor", Axis.DEFAULT_AXIS_COLOR); //$NON-NLS-1$
		builder.addColorEntry("axisMarkerColor", Axis.DEFAULT_MARKER_COLOR); //$NON-NLS-1$
		builder.addIntegerEntry("axisMarkerHeight", Axis.DEFAULT_MARKER_HEIGHT, 3, 10); //$NON-NLS-1$
		// AXIS FONT SUBGROUP
		builder.addGroup("axisFont", true); //$NON-NLS-1$
		ConfigUtils.buildDefaultFontConfig(builder,
				Axis.DEFAULT_FONT.getName(),
				Axis.DEFAULT_FONT.getSize(),
				Axis.DEFAULT_LABEL_COLOR);
		// END AXIS FONT SUBGROUP
		builder.back();
		builder.addBooleanEntry("paintBorder", PaIntEGraph.DEFAULT_PAINT_BORDER); //$NON-NLS-1$
		builder.addColorEntry("borderColor", PaIntEGraph.DEFAULT_BORDER_COLOR); //$NON-NLS-1$
		builder.addBooleanEntry("paintGrid", PaIntEGraph.DEFAULT_PAINT_GRID); //$NON-NLS-1$
		builder.addColorEntry("gridColor", PaIntEGraph.DEFAULT_GRID_COLOR); //$NON-NLS-1$
		builder.addOptionsEntry("gridStyle", PaIntEGraph.DEFAULT_GRID_STYLE.ordinal(), //$NON-NLS-1$
				(Object[])GridStyle.values());

		// END DETAIL GROUP
		builder.back();
		// END OUTLINE GROUP
		builder.back();

		// PAINTE EDITOR GROUP
		builder.addGroup("painteEditor", true); //$NON-NLS-1$

		//TODO add missing bounds below!!!

		builder.addDoubleEntry("a1LowerBound", -20.0, -1000.0, 0.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("a1UpperBound", 20.0, 0.0, 1000.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("a1Default", 4.4, -1000.0, 1000.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("a2LowerBound", -20.0, -1000.0, 0.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("a2UpperBound", 20.0, 0.0, 1000.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("a2Default", 6.72, -1000.0, 1000.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("bLowerBound", -3.0, -5.0, 0.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("bUpperBound", 3.0, 0.0, 5.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("bDefault", 0.57, -5.0, 5.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("c1LowerBound", 0.0, 0.0, 100.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("c1UpperBound", 150.0, 100.0, 300.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("c1Default", 133.35, 0.0, 300.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("c2LowerBound", 0.0, 0.0, 100.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("c2UpperBound", 150.0, 100.0, 300.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("c2Default", 66.30, 0.0, 300.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("dLowerBound", 50.0, 0.0, 100.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("dUpperBound", 200.0, 100.0, 300.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("dDefault", 189.0, 100.0, 300.0, 1.0); //$NON-NLS-1$
		builder.addDoubleEntry("alignmentLowerBound", 0.0, 0.0, 5.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("alignmentUpperBound", 10.0, 5.0, 20.0, 0.01); //$NON-NLS-1$
		builder.addDoubleEntry("alignmentDefault", PaIntEParams.DEFAULT_ALIGNMENT, 0.0, 20.0, 0.01); //$NON-NLS-1$

		// END PAINTE EDITOR GROUP
		builder.back();

		// SEARCH GROUP
		builder.addGroup("search", true); //$NON-NLS-1$

		// LIST GROUP
		builder.addGroup("list", true); //$NON-NLS-1$

		builder.addStringEntry("headerPattern", escapePattern(ProsodyListCellRenderer.DEFAULT_HEADER_PATTERN)); //$NON-NLS-1$
		builder.addBooleanEntry("showCurvePreview", true); //$NON-NLS-1$

		// END LIST GROUP
		builder.back();

		// RESULT GROUP
		builder.addGroup("result", true); //$NON-NLS-1$
		builder.setProperties(
				builder.addOptionsEntry("defaultSentencePresenter", 0,  //$NON-NLS-1$
						collectPresenterExtensions()),
				ConfigConstants.RENDERER, new ExtensionListCellRenderer());

		// END RESULT GROUP
		builder.back();

		// END SEARCH GROUP
		builder.back();

		// END APPEARANCE GROUP
		builder.back();

		// SEARCH GROUP
		builder.addGroup("search", true); //$NON-NLS-1$

		// ACCENT SHAPE SUBGROUP
		builder.addGroup("accentShape", true); //$NON-NLS-1$
		builder.virtual();

		// Minimum difference between c values
		builder.addIntegerEntry("delta", 10, 0, 50); //$NON-NLS-1$
		// => Minimum values for c1 and c2
		builder.addIntegerEntry("excursion", 30, 10, 150); //$NON-NLS-1$
		builder.addDoubleEntry("minB", 0.0, -3.0, 2.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("maxB", 1.1, -2.0, 3.0, 0.1); //$NON-NLS-1$

		// END ACCENT SHAPE SUBGROUP
		builder.back();

		// PAINTE INTEGRAL SUBGROUP
		builder.addGroup("painteIntegral", true); //$NON-NLS-1$
		builder.virtual();

		builder.addDoubleEntry("leftBorder", -1.0, -3.0, 2.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("rightBorder", 1.0, -2.0, 3.0, 0.1); //$NON-NLS-1$

		// END PAINTE INTEGRAL SUBGROUP
		builder.back();

		// PAINTE CURVE SUBGROUP
		builder.addGroup("painteCurve", true); //$NON-NLS-1$
		builder.virtual();

		builder.addDoubleEntry("leftBorder", -1.0, -3.0, 2.0, 0.1); //$NON-NLS-1$
		builder.addDoubleEntry("rightBorder", 1.0, -2.0, 3.0, 0.1); //$NON-NLS-1$
		builder.addIntegerEntry("resolution", 100, 20, 300, 1); //$NON-NLS-1$

		// END PAINTE CURVE SUBGROUP
		builder.back();

		// END SEARCH GROUP
		builder.back();

		// HIGHLIGHTING GROUP
		builder.addGroup("highlighting", true); //$NON-NLS-1$
//		builder.addBooleanEntry("showIndex", true); //$NON-NLS-1$
//		builder.addBooleanEntry("showCorpusIndex", false); //$NON-NLS-1$
		builder.setProperties(builder.addOptionsEntry("highlightType", 0,  //$NON-NLS-1$
				(Object[])HighlightType.values()),
				ConfigConstants.RENDERER, new TooltipListCellRenderer());
		builder.setProperties(builder.addOptionsEntry("groupHighlightType", 0,  //$NON-NLS-1$
				(Object[])HighlightType.values()),
				ConfigConstants.RENDERER, new TooltipListCellRenderer());
		builder.addBooleanEntry("markMultipleAnnotations", true); //$NON-NLS-1$
		builder.addColorEntry("nodeHighlight", ProsodyHighlighting.getInstance().getNodeHighlightColor().getRGB()); //$NON-NLS-1$
		builder.addColorEntry("edgeHighlight", ProsodyHighlighting.getInstance().getEdgeHighlightColor().getRGB()); //$NON-NLS-1$
		builder.addColorEntry("transitiveHighlight", ProsodyHighlighting.getInstance().getTransitiveHighlightColor().getRGB()); //$NON-NLS-1$
		for(String token : ProsodyHighlighting.getInstance().getTokens()) {
			builder.addColorEntry(token+"Highlight", ProsodyHighlighting.getInstance().getHighlightColor(token).getRGB()); //$NON-NLS-1$
		}
		builder.back();
		// END HIGHLIGHTING GROUP

		ProsodyHighlighting.getInstance().loadConfig();
	}

	private Object[] collectPresenterExtensions() {
		java.util.List<Object> items = new ArrayList<>();

		for(Extension extension : ProsodyPlugin.getProsodySentencePresenterExtensions()) {
			items.add(extension.getUniqueId());
		}

		return items.toArray();
	}

}