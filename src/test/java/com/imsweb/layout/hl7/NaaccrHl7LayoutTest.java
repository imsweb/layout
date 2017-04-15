/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutUtils;
import com.imsweb.layout.hl7.xml.Hl7ComponentXmlDto;
import com.imsweb.layout.hl7.xml.Hl7FieldXmlDto;
import com.imsweb.layout.hl7.xml.Hl7LayoutXmlDto;
import com.imsweb.layout.hl7.xml.Hl7SegmentXmlDto;
import com.imsweb.layout.hl7.xml.Hl7SubComponentXmlDto;

public class NaaccrHl7LayoutTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testNaaccrHl7Layout() throws Exception {
        NaaccrHl7Layout layout = (NaaccrHl7Layout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_HL7_2_5_1);

        // make sure the first field belongs to MSH segment
        List<NaaccrHl7Field> fields = (List<NaaccrHl7Field>)layout.getAllFields();
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
        Assert.assertTrue(identifiers.containsAll(Arrays.asList("MSH", "SFT", "PID", "NK1", "PV1", "ORC", "OBR", "OBX", "NTE", "SPM")));

        // test the validity of the XML
        Hl7LayoutXmlDto layoutXmlDto;
        try (InputStream fis = new FileInputStream(Paths.get("src/main/resources/layout/hl7/naaccr/naaccr-hl7-2.5.1-layout.xml").toFile())) {
            layoutXmlDto = LayoutUtils.readHl7Layout(fis);
        }
        Assert.assertNotNull(layoutXmlDto);

        List<Hl7SegmentXmlDto> segments = layoutXmlDto.getHl7Segments();
        Assert.assertEquals(10, segments.size());
        List<String> supportedIdentifiers = Arrays.asList("MSH", "SFT", "PID", "NK1", "PV1", "ORC", "OBR", "OBX", "NTE", "SPM");
        for (int i = 0; i < segments.size(); i++)
            Assert.assertEquals(supportedIdentifiers.get(i), segments.get(i).getIdentifier());

        for (Hl7SegmentXmlDto segment : segments) {
            // test non-null segment parameters
            Assert.assertNotNull(segment.getIdentifier());

            fieldIndex = 1;
            for (Hl7FieldXmlDto field : segment.getHl7Fields()) {
                // test non-null field parameters
                Assert.assertFalse(field.getName().isEmpty());
                Assert.assertFalse(field.getIdentifier().isEmpty());
                Assert.assertFalse(field.getLongLabel().isEmpty());
                Assert.assertFalse(field.getType().isEmpty()); // UNK exists in the XML file (OBX-5, OBX-20, OBX-21, OBX-22)
                Assert.assertNotNull(field.getMinOccurrence());
                Assert.assertNotNull(field.getMaxOccurrence());

                // test field identifier
                Assert.assertEquals(segment.getIdentifier(), field.getIdentifier().substring(0, 3));
                Assert.assertEquals(fieldIndex, Integer.parseInt(parseIdentifier(field.getIdentifier())[0]));

                // test field min and max occurrences
                Assert.assertTrue(field.getMinOccurrence() <= field.getMaxOccurrence());

                int componentIndex = 1;
                for (Hl7ComponentXmlDto component : field.getHl7Components()) {
                    // test non-null field parameters
                    Assert.assertFalse(component.getName().isEmpty());
                    Assert.assertFalse(component.getIdentifier().isEmpty());
                    Assert.assertFalse(component.getLongLabel().isEmpty());
                    Assert.assertFalse(component.getType().isEmpty()); // UNK exists in the XML file (PID-13.1, PID-14.1, NK1-5.1, ORC-23.1, OBR-17.1)

                    // test component identifier
                    Assert.assertEquals(segment.getIdentifier(), component.getIdentifier().substring(0, 3));
                    Assert.assertEquals(fieldIndex, Integer.parseInt(parseIdentifier(component.getIdentifier())[0]));
                    Assert.assertEquals(componentIndex, Integer.parseInt(parseIdentifier(component.getIdentifier())[1]));

                    int subComponentIndex = 1;
                    for (Hl7SubComponentXmlDto subComponent : component.getHl7SubComponents()) {
                        // test non-null field parameters
                        Assert.assertFalse(subComponent.getName().isEmpty());
                        Assert.assertFalse(subComponent.getIdentifier().isEmpty());
                        Assert.assertFalse(subComponent.getLongLabel().isEmpty());
                        Assert.assertFalse(subComponent.getType().isEmpty());

                        // test subcomponent identifier
                        Assert.assertEquals(segment.getIdentifier(), subComponent.getIdentifier().substring(0, 3));
                        Assert.assertEquals(fieldIndex, Integer.parseInt(parseIdentifier(subComponent.getIdentifier())[0]));
                        Assert.assertEquals(componentIndex, Integer.parseInt(parseIdentifier(subComponent.getIdentifier())[1]));
                        Assert.assertEquals(subComponentIndex, Integer.parseInt(parseIdentifier(subComponent.getIdentifier())[2]));

                        subComponentIndex++;
                    }
                    componentIndex++;
                }
                fieldIndex++;
            }
        }
    }

    private String[] parseIdentifier(String identifier) {
        // field identifiers do not contain "."
        return !identifier.contains(".") ? new String[] {identifier.substring(4)} : identifier.substring(4).split("\\.");
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
