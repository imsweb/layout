/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutUtils;
import com.imsweb.layout.TestingUtils;
import com.imsweb.layout.hl7.entity.Hl7Field;
import com.imsweb.layout.hl7.entity.Hl7Message;
import com.imsweb.layout.hl7.entity.Hl7Segment;
import com.imsweb.layout.hl7.xml.Hl7ComponentXmlDto;
import com.imsweb.layout.hl7.xml.Hl7FieldXmlDto;
import com.imsweb.layout.hl7.xml.Hl7LayoutXmlDto;
import com.imsweb.layout.hl7.xml.Hl7SegmentXmlDto;
import com.imsweb.layout.hl7.xml.Hl7SubComponentXmlDto;

public class NaaccrHl7LayoutTest {

    @Test
    public void testNaaccrHl7Layout251() throws Exception {
        NaaccrHl7Layout layout = (NaaccrHl7Layout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_HL7_2_5_1);
        assertInternalLayoutValidity(layout);

        Hl7Message msg = Hl7MessageBuilder.createMessage()
                .withSegment("MSH")
                .withSegment("PID")
                .withField(2, "WEIRD-ID")
                .withField(3)
                .withRepeatedField()
                .withComponent(1, "010203040")
                .withComponent(5, "MR")
                .withComponent(6, "STJ", "03D1234567", "AHA")
                .withRepeatedField()
                .withComponent(1, "111223333")
                .withComponent(5, "SS")
                .withRepeatedField()
                .withComponent(1, "97 810430")
                .withComponent(5, "PI")
                .withComponent(6, "HITECK PATH LAB-ATL", "3D932840", "CLIA")
                .withField(5)
                .withComponent(1, "DEPRY")
                .withComponent(2, "FABIAN")
                .withComponent(3, "P")
                .build();

        File file = Paths.get("build/test-2.5.1.hl7").toFile();

        layout.writeMessages(file, Collections.singletonList(msg));
        Assert.assertTrue(file.exists());

        LayoutInfo info = layout.buildFileInfo(file, null, null);
        Assert.assertNotNull(info);
        Assert.assertEquals(layout.getLayoutId(), info.getLayoutId());

        Hl7Message msg2 = layout.readAllMessages(file).get(0);
        Assert.assertEquals("2.5.1", msg2.getSegment("MSH").getField(12).getValue());
        Assert.assertEquals("WEIRD-ID", msg2.getSegment("PID").getValue(2));
        Assert.assertEquals("010203040", msg2.getSegment("PID").getValue(3, 1, 1, 1));
    }

    @SuppressWarnings("unchecked")
    private void assertInternalLayoutValidity(NaaccrHl7Layout layout) throws IOException {

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
        try (InputStream fis = new FileInputStream(
                Paths.get(TestingUtils.getWorkingDirectory() + "/src/main/resources/layout/hl7/naaccr/naaccr-hl7-" + layout.getLayoutVersion() + "-layout.xml").toFile())) {
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
        layout = new NaaccrHl7Layout(new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/testing-layout-hl7.xml"));
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

    @Test
    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    public void testHl7Layout() throws Exception {
        // test read messages (uses a modified example in PDF)
        NaaccrHl7Layout layout = (NaaccrHl7Layout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_HL7_2_5_1);
        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-naaccr-hl7.txt");
        List<Hl7Message> messages = layout.readAllMessages(new File(url.getPath()));
        Assert.assertEquals(2, messages.size());

        int messageIndex = 0;
        for (Hl7Message message : messages) {
            // test message
            Assert.assertNotNull(message);
            Assert.assertEquals("|", message.getFieldSeparator());
            Assert.assertEquals("~", message.getRepetitionSeparator());
            Assert.assertEquals("^", message.getComponentSeparator());
            Assert.assertEquals("&", message.getSubComponentSeparator());

            int segmentIndex = 0;
            for (Hl7Segment segment : message.getSegments()) {
                // test segments
                Assert.assertNotNull(segment);
                Assert.assertEquals(3, segment.getId().length());
                if (segmentIndex == 0)
                    Assert.assertEquals("MSH", segment.getId());
                else
                    Assert.assertTrue(Arrays.asList("PID", "ORC", "OBR", "OBX").contains(segment.getId()));

                // test fields
                Map<Integer, Hl7Field> fields = segment.getFields();
                switch (segment.getId()) {
                    case "MSH":
                        // test important MSH fields
                        if (messageIndex == 0)
                            Assert.assertEquals("INDEPENDENT LAB SERVICES^33D1234567^CLIA", fields.get(4).getValue());
                        else
                            Assert.assertEquals("IND LAB SERVICES^33D1234567^CLIA", fields.get(4).getValue());
                        Assert.assertEquals("2.5.1", fields.get(12).getValue());
                        break;
                    case "PID":
                        // test repeated fields
                        Assert.assertEquals(2, fields.get(3).getRepeatedFields().size());

                        // test patient first and last name components
                        Assert.assertEquals("McMuffin", fields.get(5).getComponent(1).getValue());
                        Assert.assertEquals("Candy", fields.get(5).getComponent(2).getValue());
                        break;
                    case "OBR":
                        // test sub-components
                        Assert.assertEquals(6, fields.get(32).getComponent(1).getSubComponents().size());
                        break;
                }

                segmentIndex++;
            }

            messageIndex++;
        }

        // test write message to file
        File file = new File(TestingUtils.getWorkingDirectory() + "/build/naaccr16.txt");
        layout.writeMessages(file, Collections.singletonList(messages.get(1)));
        Assert.assertTrue(file.exists());

        // test version
        LayoutInfo info = layout.buildFileInfo(file, null, null);
        Assert.assertNotNull(info);
        Assert.assertEquals(layout.getLayoutId(), info.getLayoutId());

        // test some fields
        Hl7Message msg2 = layout.readAllMessages(file).get(0);
        Assert.assertNull(msg2.getSegment("PID").getValue(2));
        Assert.assertEquals("123456789", msg2.getSegment("PID").getValue(3, 1, 1, 1));
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
