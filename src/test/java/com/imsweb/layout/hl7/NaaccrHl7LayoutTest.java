/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutUtils;

public class NaaccrHl7LayoutTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testNaaccrHl7Layout() throws Exception {
        // test naaccr-hl7-2.5.1-layout.xml
        NaaccrHl7Layout layout = new NaaccrHl7Layout(LayoutFactory.LAYOUT_ID_NAACCR_HL7, "2.5.1", true);
        List<NaaccrHl7Field> fields = (List<NaaccrHl7Field>)layout.getAllFields();

        // make sure the first field belongs to MSH segment
        Assert.assertEquals("MSH-1", fields.get(0).getIdentifier());

        // check identifiers
        Set<String> identifiers = new HashSet<>();
        int fieldIndex = 1;
        for (NaaccrHl7Field field : fields) {
            // for segment identifier assertion
            identifiers.add(field.getIdentifier().substring(0, 3));

            // check incrementation for field index in identifier
            fieldIndex = Integer.parseInt(field.getIdentifier().substring(4)) == fieldIndex ? fieldIndex : 1;
            Assert.assertEquals(fieldIndex, Integer.parseInt(field.getIdentifier().substring(4)));

            // test non-null field parameters
            Assert.assertFalse(field.getName().isEmpty());
            Assert.assertFalse(field.getIdentifier().isEmpty());
            Assert.assertFalse(field.getLongLabel().isEmpty());
            Assert.assertFalse(field.getType().isEmpty()); // UNK exists in the XML file (OBX-5, OBX-20, OBX-21, OBX-22)
            Assert.assertNotNull(field.getMinOccurrence());
            Assert.assertNotNull(field.getMaxOccurrence());

            // test field min and max occurrences
            Assert.assertTrue(field.getMinOccurrence() <= field.getMaxOccurrence());

            fieldIndex++;
        }
        Assert.assertEquals(10, identifiers.size());
        Assert.assertTrue(identifiers.containsAll(Hl7Utils._SUPPORTED_IDENTIFIERS));
    }

    @Test
    public void testFakeLayout() throws Exception {

        // test loading the layout from a URL
        NaaccrHl7Layout layout = new NaaccrHl7Layout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-hl7.xml"));
        Assert.assertEquals("test", layout.getLayoutId());
        Assert.assertEquals("Test", layout.getLayoutName());
        Assert.assertEquals("1.0", layout.getLayoutVersion());
        Assert.assertEquals("Just for testing...", layout.getLayoutDescription());

        // test loading the layout from a file
        layout = new NaaccrHl7Layout(new File(System.getProperty("user.dir") + "/src/test/resources/testing-layout-hl7.xml"));
        Assert.assertEquals("test", layout.getLayoutId());
        Assert.assertEquals("Test", layout.getLayoutName());
        Assert.assertEquals("1.0", layout.getLayoutVersion());
        Assert.assertEquals("Just for testing...", layout.getLayoutDescription());

        // test loading the layout from an object
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("testing-layout-hl7.xml")) {
            layout = new NaaccrHl7Layout(LayoutUtils.readHl7Layout(is));
            Assert.assertEquals("test", layout.getLayoutId());
            Assert.assertEquals("Test", layout.getLayoutName());
            Assert.assertEquals("1.0", layout.getLayoutVersion());
            Assert.assertEquals("Just for testing...", layout.getLayoutDescription());
        }

        // test field getters
        Assert.assertEquals(4, layout.getAllFields().size());
        assertFieldParameters(layout, "field1", "Field 1");
        assertFieldParameters(layout, "field2", "Field 2");
        assertFieldParameters(layout, "field3", "Field 3");
        assertFieldParameters(layout, "field4", "Field 4");
        Assert.assertNull(layout.getFieldByName(null));
        Assert.assertNull(layout.getFieldByName(""));
        Assert.assertNull(layout.getFieldByName("?"));
    }

    private void assertFieldParameters(NaaccrHl7Layout layout, String name, String longLabel) {
        Assert.assertEquals(name, layout.getFieldByName(name).getName());
        Assert.assertNull(layout.getFieldByName(name).getShortLabel());
        Assert.assertEquals(longLabel, layout.getFieldByName(name).getLongLabel());
        Assert.assertNull(layout.getFieldByName(name).getNaaccrItemNum());
        Assert.assertNull(layout.getFieldByName(name).getAlign());
        Assert.assertNull(layout.getFieldByName(name).getPadChar());
        Assert.assertNull(layout.getFieldByName(name).getDefaultValue());
        Assert.assertTrue(layout.getFieldByName(name).getTrim());
        Assert.assertNull(layout.getFieldByName(name).getSection());
    }
}
