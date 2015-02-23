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

 * $Revision: 332 $
 * $Date: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $URL: https://subversion.assembla.com/svn/icarusplatform/trunk/Icarus/plugins/de.ims.icarus.plugins.model/test/de/ims/icarus/language/model/test/manifest/OptionsManifestImplTest.java $
 *
 * $LastChangedDate: 2014-12-16 13:55:39 +0100 (Di, 16 Dez 2014) $
 * $LastChangedRevision: 332 $
 * $LastChangedBy: mcgaerty $
 */
package de.ims.icarus.language.model.test.manifest;

import static de.ims.icarus.language.model.test.TestUtils.assertHashEquals;
import static de.ims.icarus.language.model.test.TestUtils.assertTemplateGetters;
import static de.ims.icarus.language.model.test.TestUtils.getTestValues;
import static de.ims.icarus.language.model.test.manifest.ManifestTestUtils.assertIdSetterSpec;
import static de.ims.icarus.language.model.test.manifest.ManifestXmlTestUtils.assertSerializationEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Test;

import de.ims.icarus.language.model.test.TestUtils.TestEnum;
import de.ims.icarus.model.api.manifest.OptionsManifest;
import de.ims.icarus.model.api.manifest.OptionsManifest.Option;
import de.ims.icarus.model.standard.manifest.DefaultModifiableIdentity;
import de.ims.icarus.model.standard.manifest.OptionsManifestImpl;
import de.ims.icarus.model.standard.manifest.OptionsManifestImpl.OptionImpl;
import de.ims.icarus.model.standard.manifest.ValueRangeImpl;
import de.ims.icarus.model.standard.manifest.ValueSetImpl;
import de.ims.icarus.model.types.ValueType;
import de.ims.icarus.util.id.Identity;

/**
 * @author Markus Gärtner
 * @version $Id: OptionsManifestImplTest.java 332 2014-12-16 12:55:39Z mcgaerty $
 *
 */
public class OptionsManifestImplTest extends ManifestTestCase<OptionsManifestImpl> {

	/**
	 * @see de.ims.icarus.language.model.test.manifest.ManifestTestCase#newInstance()
	 */
	@Override
	protected OptionsManifestImpl newInstance() {
		return new OptionsManifestImpl(location, registry);
	}

	private void fillGroups(OptionsManifestImpl manifest) {
		manifest.addGroupIdentifier(new DefaultModifiableIdentity("group1", "Default empty group")); //$NON-NLS-1$ //$NON-NLS-2$
		manifest.addGroupIdentifier(new DefaultModifiableIdentity("group2", null, TEST_ICON)); //$NON-NLS-1$
	}

	private void fillOptions(OptionsManifestImpl manifest) {
		// String option
		OptionImpl option1 = new OptionImpl("option1", ValueType.STRING); //$NON-NLS-1$
		option1.setDefaultValue("default test string"); //$NON-NLS-1$
		option1.setPublished(false);

		manifest.addOption(option1);

		// Integer option
		OptionImpl option2 = new OptionImpl("option2", ValueType.INTEGER); //$NON-NLS-1$
		option2.setDefaultValue(234);
		option2.setSupportedRange(new ValueRangeImpl(ValueType.INTEGER, 1, 9999999, true, false));
		option2.setMultiValue(true);

		manifest.addOption(option2);

		// Double option
		OptionImpl option3 = new OptionImpl("option3", ValueType.DOUBLE); //$NON-NLS-1$
		option3.setSupportedValues(new ValueSetImpl(ValueType.DOUBLE, getTestValues(ValueType.DOUBLE)));

		manifest.addOption(option3);

		// Enum option
		OptionImpl option4 = new OptionImpl("option4", ValueType.ENUM); //$NON-NLS-1$
		option4.setDefaultValue(TestEnum.TEST2);
		option4.setSupportedValues(new ValueSetImpl(ValueType.ENUM, TestEnum.class));

		manifest.addOption(option4);
	}

	private void fillAll(OptionsManifestImpl manifest) {
		fillId(manifest);
		fillGroups(manifest);
		fillOptions(manifest);
	}

	// GENERAL TESTS

	@Test
	public void testConstructorConsistency() throws Exception {
		testConsistency();
	}

	@Test
	public void testEquals() throws Exception {

		OptionsManifestImpl other = new OptionsManifestImpl(location, registry);

		assertHashEquals(manifest, other);

		assertHashEquals(manifest, manifest);
	}

	@Test
	public void testGetOptionInherited() throws Exception {

		String optionId = "test-option"; //$NON-NLS-1$

		Option option = mock(Option.class);

		OptionsManifest template = mock(OptionsManifest.class);
		when(template.isTemplate()).thenReturn(true);
		when(template.getOption(optionId)).thenReturn(option);
		when(template.getId()).thenReturn(TEST_TEMPLATE_ID);

		registry.registerTemplate(template);

		manifest.setTemplateId(TEST_TEMPLATE_ID);

		assertSame(option, manifest.getOption(optionId));
	}

	@Test
	public void testGetOptionNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.getOption(null);
	}

	@Test
	public void testGetOptionUnknown() throws Exception {
		thrown.expect(IllegalArgumentException.class);
		manifest.getOption(UNKNOWN_ID);
	}

	// MODIFICATION TESTS

	@Test
	public void testId() throws Exception {
		assertIdSetterSpec(manifest);
	}

	@Test
	public void testAddOption() throws Exception {
		Option option = mock(Option.class);
		when(option.getId()).thenReturn(TEST_ID);

		manifest.addOption(option);

		assertSame(option, manifest.getOption(TEST_ID));
	}

	@Test
	public void testAddInvalidOptionIdNull() throws Exception {
		Option option = mock(Option.class);
		when(option.getId()).thenReturn(null);

		thrown.expect(IllegalArgumentException.class);
		manifest.addOption(option);
	}

	@Test
	public void testAddInvalidOptionNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.addOption(null);
	}

	@Test
	public void testAddMultipleOptions() throws Exception {
		Option option1 = mock(Option.class);
		when(option1.getId()).thenReturn("option1"); //$NON-NLS-1$

		Option option2 = mock(Option.class);
		when(option2.getId()).thenReturn("option2"); //$NON-NLS-1$

		assertNotEquals(option1, option2);

		manifest.addOption(option1);

		manifest.addOption(option2);
	}

	@Test
	public void testAddDuplicateOptions() throws Exception {
		Option option1 = mock(Option.class);
		when(option1.getId()).thenReturn("option1"); //$NON-NLS-1$

		Option option2 = mock(Option.class);
		when(option2.getId()).thenReturn("option1"); //$NON-NLS-1$

		manifest.addOption(option1);

		thrown.expect(IllegalArgumentException.class);
		manifest.addOption(option2);
	}

	@Test
	public void testAddGroup() throws Exception {

		Identity group = mock(Identity.class);
		when(group.getId()).thenReturn(LEGAL_ID);

		manifest.addGroupIdentifier(group);

		Set<Identity> groups = manifest.getGroupIdentifiers();

		assertEquals("Group count mismatch", 1, groups.size()); //$NON-NLS-1$

		Identity stored = groups.iterator().next();

		assertSame("Saved group mismatch", group, stored); //$NON-NLS-1$
	}

	@Test
	public void testAddDuplicateGroups() throws Exception {

		Identity group = mock(Identity.class);
		when(group.getId()).thenReturn(LEGAL_ID);

		manifest.addGroupIdentifier(group);
		thrown.expect(IllegalArgumentException.class);
		manifest.addGroupIdentifier(group);
	}

	@Test
	public void testAddInvalidGroupNull() throws Exception {
		thrown.expect(NullPointerException.class);
		manifest.addGroupIdentifier(null);
	}

	@Test
	public void testAddInvalidGroupIdLength() throws Exception {
		Identity group = mock(Identity.class);
		when(group.getId()).thenReturn(INVALID_ID_LENGTH);

		thrown.expect(IllegalArgumentException.class);
		manifest.addGroupIdentifier(group);
	}

	@Test
	public void testAddInvalidGroupIdContent() throws Exception {
		Identity group = mock(Identity.class);
		when(group.getId()).thenReturn(INVALID_ID_CONTENT);

		thrown.expect(IllegalArgumentException.class);
		manifest.addGroupIdentifier(group);
	}

	@Test
	public void testAddInvalidGroupIdBegin() throws Exception {
		Identity group = mock(Identity.class);
		when(group.getId()).thenReturn(INVALID_ID_BEGIN);

		thrown.expect(IllegalArgumentException.class);
		manifest.addGroupIdentifier(group);
	}

	@Test
	public void testAddInvalidGroupIdNull() throws Exception {
		Identity group = mock(Identity.class);
		when(group.getId()).thenReturn(INVALID_ID_NULL);

		thrown.expect(IllegalArgumentException.class);
		manifest.addGroupIdentifier(group);
	}

	@Test
	public void testTemplate() throws Exception {

		// Prepare template
		OptionsManifestImpl template = new OptionsManifestImpl(location, registry);
		fillAll(template);
		template.setId(TEST_TEMPLATE_ID);

		registry.registerTemplate(template);

		// Link template
		manifest.setId(TEST_ID);
		manifest.setTemplateId(TEST_TEMPLATE_ID);

		// Manifest is empty except for id and templateId

		assertTemplateGetters(OptionsManifest.class, manifest, template);
	}

	@Test
	public void testXmlGroups() throws Exception {

		fillId(manifest);
		fillGroups(manifest);

		assertSerializationEquals(manifest);
	}

	@Test
	public void testXmlOptions() throws Exception {

		fillId(manifest);
		fillOptions(manifest);

		assertSerializationEquals(manifest);
	}

	@Test
	public void testXmlFull() throws Exception {

		fillAll(manifest);

		assertSerializationEquals(manifest);
	}
}