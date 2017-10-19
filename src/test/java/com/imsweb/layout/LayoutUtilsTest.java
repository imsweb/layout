package com.imsweb.layout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.layout.hl7.xml.Hl7ComponentXmlDto;
import com.imsweb.layout.hl7.xml.Hl7FieldXmlDto;
import com.imsweb.layout.hl7.xml.Hl7LayoutXmlDto;
import com.imsweb.layout.hl7.xml.Hl7SegmentXmlDto;
import com.imsweb.layout.hl7.xml.Hl7SubComponentXmlDto;
import com.imsweb.layout.record.csv.xml.CommaSeparatedLayoutFieldXmlDto;
import com.imsweb.layout.record.csv.xml.CommaSeparatedLayoutXmlDto;
import com.imsweb.layout.record.fixed.xml.FixedColumnLayoutFieldXmlDto;
import com.imsweb.layout.record.fixed.xml.FixedColumnLayoutXmlDto;

public class LayoutUtilsTest {

    @Test
    public void testFormatNumber() {
        Assert.assertEquals("1,000", LayoutUtils.formatNumber(1000));
    }

    @Test
    public void testFormatTime() {
        Assert.assertEquals("2 seconds", LayoutUtils.formatTime(2000));
    }

    @Test
    public void testFormatFileSize() {
        Assert.assertEquals("1 KB", LayoutUtils.formatFileSize(1024));
    }

    @Test
    public void testPad() {
        Assert.assertEquals(" 1", LayoutUtils.pad("1", 2, " ", true));
        Assert.assertEquals("1 ", LayoutUtils.pad("1", 2, " ", false));
        Assert.assertEquals("01", LayoutUtils.pad("1", 2, "0", true));
        Assert.assertEquals("10", LayoutUtils.pad("1", 2, "0", false));
        Assert.assertEquals("123", LayoutUtils.pad("123", 2, "0", false));
    }

    @Test
    public void testInputOutputStreamCreation() throws IOException {

        // write to file
        File file = new File(System.getProperty("user.dir") + "/build/create-input-stream-test.txt");
        try (OutputStream os = LayoutUtils.createOutputStream(file)) {
            Assert.assertNotNull(os);
            os.write("A".getBytes());
        }

        // read from file
        try (InputStream is = LayoutUtils.createInputStream(file)) {
            Assert.assertNotNull(is);
            Assert.assertEquals('A', is.read());
            Assert.assertEquals(-1, is.read());
        }

        // write to gzipped file
        File gzippedFile = new File(System.getProperty("user.dir") + "/build/create-input-stream-test.txt.gz");
        try (OutputStream os = LayoutUtils.createOutputStream(gzippedFile)) {
            Assert.assertNotNull(os);
            os.write("A".getBytes());
        }

        // read from gzipped file
        try (InputStream is = LayoutUtils.createInputStream(gzippedFile)) {
            Assert.assertNotNull(is);
            Assert.assertEquals('A', is.read());
            Assert.assertEquals(-1, is.read());
        }

        // write to zipped file
        File zippedFile = new File(System.getProperty("user.dir") + "/build/create-input-stream-test.zip");
        try (OutputStream os = LayoutUtils.createOutputStream(zippedFile)) {
            Assert.assertNotNull(os);
            ((ZipOutputStream)os).putNextEntry(new ZipEntry("create-input-stream-test"));
            os.write("A".getBytes());
        }

        // read from zipped file
        try (InputStream is = LayoutUtils.createInputStream(zippedFile, "create-input-stream-test")) {
            Assert.assertNotNull(is);
            Assert.assertEquals('A', is.read());
            Assert.assertEquals(-1, is.read());
        }
    }

    @Test
    public void testReadWriteFixedColumnsLayout() throws IOException {

        FixedColumnLayoutXmlDto layout = new FixedColumnLayoutXmlDto();
        layout.setId("test-utils-fixed");
        layout.setName("Test Rec Layout");
        layout.setDescription("Description");
        layout.setVersion("1.0");
        layout.setLength(1);
        FixedColumnLayoutFieldXmlDto field = new FixedColumnLayoutFieldXmlDto();
        field.setName("field1");
        field.setStart(1);
        field.setEnd(1);
        field.setNaaccrItemNum(1);
        field.setShortLabel("short");
        field.setLongLabel("long");
        field.setAlign("left");
        field.setDefaultValue("default");
        field.setPadChar("X");
        field.setSection("section");
        layout.setField(Collections.singletonList(field));

        File file = new File(System.getProperty("user.dir") + "/build/fixed-layout-test.xml");
        try (OutputStream fos = new FileOutputStream(file)) {
            LayoutUtils.writeFixedColumnsLayout(fos, layout);
        }

        try (InputStream fis = new FileInputStream(file)) {
            FixedColumnLayoutXmlDto layout2 = LayoutUtils.readFixedColumnsLayout(fis);
            Assert.assertEquals(layout.getId(), layout2.getId());
            Assert.assertEquals(layout.getName(), layout2.getName());
            Assert.assertEquals(layout.getDescription(), layout2.getDescription());
            Assert.assertEquals(layout.getVersion(), layout2.getVersion());
            Assert.assertEquals(layout.getLength(), layout2.getLength());
            Assert.assertEquals(layout.getField().size(), layout2.getField().size());
            Assert.assertEquals(layout.getField().get(0).getName(), layout2.getField().get(0).getName());
            Assert.assertEquals(layout.getField().get(0).getStart(), layout2.getField().get(0).getStart());
            Assert.assertEquals(layout.getField().get(0).getEnd(), layout2.getField().get(0).getEnd());
            Assert.assertEquals(layout.getField().get(0).getNaaccrItemNum(), layout2.getField().get(0).getNaaccrItemNum());
            Assert.assertEquals(layout.getField().get(0).getShortLabel(), layout2.getField().get(0).getShortLabel());
            Assert.assertEquals(layout.getField().get(0).getLongLabel(), layout2.getField().get(0).getLongLabel());
            Assert.assertEquals(layout.getField().get(0).getAlign(), layout2.getField().get(0).getAlign());
            Assert.assertEquals(layout.getField().get(0).getDefaultValue(), layout2.getField().get(0).getDefaultValue());
            Assert.assertEquals(layout.getField().get(0).getPadChar(), layout2.getField().get(0).getPadChar());
            Assert.assertEquals(layout.getField().get(0).getSection(), layout2.getField().get(0).getSection());

        }
    }

    @Test
    public void testReadWriteColumnsSeparatedLayout() throws IOException {

        CommaSeparatedLayoutXmlDto layout = new CommaSeparatedLayoutXmlDto();
        layout.setId("test-utils-fixed");
        layout.setName("Test Rec Layout");
        layout.setDescription("Description");
        layout.setVersion("1.0");
        layout.setNumFields(1);
        CommaSeparatedLayoutFieldXmlDto field = new CommaSeparatedLayoutFieldXmlDto();
        field.setName("field1");
        field.setIndex(1);
        field.setMaxLength(1);
        field.setNaaccrItemNum(1);
        field.setShortLabel("short");
        field.setLongLabel("long");
        field.setDefaultValue("default");
        layout.setField(Collections.singletonList(field));

        File file = new File(System.getProperty("user.dir") + "/build/fixed-layout-test.xml");
        try (OutputStream fos = new FileOutputStream(file)) {
            LayoutUtils.writeCommaSeparatedLayout(fos, layout);
        }

        try (InputStream fis = new FileInputStream(file)) {
            CommaSeparatedLayoutXmlDto layout2 = LayoutUtils.readCommaSeparatedLayout(fis);
            Assert.assertEquals(layout.getId(), layout2.getId());
            Assert.assertEquals(layout.getName(), layout2.getName());
            Assert.assertEquals(layout.getDescription(), layout2.getDescription());
            Assert.assertEquals(layout.getVersion(), layout2.getVersion());
            Assert.assertEquals(layout.getNumFields(), layout2.getNumFields());
            Assert.assertEquals(layout.getField().size(), layout2.getField().size());
            Assert.assertEquals(layout.getField().get(0).getName(), layout2.getField().get(0).getName());
            Assert.assertEquals(layout.getField().get(0).getIndex(), layout2.getField().get(0).getIndex());
            Assert.assertEquals(layout.getField().get(0).getMaxLength(), layout2.getField().get(0).getMaxLength());
            Assert.assertEquals(layout.getField().get(0).getNaaccrItemNum(), layout2.getField().get(0).getNaaccrItemNum());
            Assert.assertEquals(layout.getField().get(0).getShortLabel(), layout2.getField().get(0).getShortLabel());
            Assert.assertEquals(layout.getField().get(0).getLongLabel(), layout2.getField().get(0).getLongLabel());
            Assert.assertEquals(layout.getField().get(0).getDefaultValue(), layout2.getField().get(0).getDefaultValue());
        }
    }

    @Test
    public void testReadWriteHl7Layout() throws IOException {

        Hl7LayoutXmlDto layout = new Hl7LayoutXmlDto();
        layout.setId("test-hl7");
        layout.setName("Test HL7-Layout");
        layout.setDescription("Description");
        layout.setVersion("1.0");
        Hl7SegmentXmlDto segment = new Hl7SegmentXmlDto();
        segment.setIdentifier("MSH");
        Hl7FieldXmlDto field = new Hl7FieldXmlDto();
        field.setName("field1");
        field.setIdentifier("MSH-1");
        field.setLongLabel("Field 1");
        field.setType("F1");
        field.setMinOccurrence(1);
        field.setMaxOccurrence(1);
        Hl7ComponentXmlDto component = new Hl7ComponentXmlDto();
        component.setName("component1");
        component.setIdentifier("MSH-1.1");
        component.setLongLabel("Component 1");
        component.setType("C1");
        Hl7SubComponentXmlDto subComponent = new Hl7SubComponentXmlDto();
        subComponent.setName("subcomponent1");
        subComponent.setIdentifier("MSH-1.1.1");
        subComponent.setLongLabel("Subcomponent 1");
        subComponent.setType("S1");
        component.setHl7SubComponents(Collections.singletonList(subComponent));
        field.setHl7Components(Collections.singletonList(component));
        segment.setHl7Fields(Collections.singletonList(field));
        layout.setHl7Segments(Collections.singletonList(segment));

        File file = new File(System.getProperty("user.dir") + "/build/hl7-layout-test.xml");
        try (OutputStream fos = new FileOutputStream(file)) {
            LayoutUtils.writeHl7Layout(fos, layout);
        }

        try (InputStream fis = new FileInputStream(file)) {
            // layout
            Hl7LayoutXmlDto layout2 = LayoutUtils.readHl7Layout(fis);
            Assert.assertEquals(layout.getId(), layout2.getId());
            Assert.assertEquals(layout.getName(), layout2.getName());
            Assert.assertEquals(layout.getDescription(), layout2.getDescription());
            Assert.assertEquals(layout.getVersion(), layout2.getVersion());

            // segment
            Assert.assertEquals(layout.getHl7Segments().size(), layout2.getHl7Segments().size());
            Hl7SegmentXmlDto segment1 = layout.getHl7Segments().get(0);
            Hl7SegmentXmlDto segment2 = layout2.getHl7Segments().get(0);
            Assert.assertEquals(segment1.getIdentifier(), segment2.getIdentifier());

            // field
            Assert.assertEquals(segment1.getHl7Fields().size(), segment2.getHl7Fields().size());
            Hl7FieldXmlDto field1 = segment1.getHl7Fields().get(0);
            Hl7FieldXmlDto field2 = segment2.getHl7Fields().get(0);
            Assert.assertEquals(field1.getName(), field2.getName());
            Assert.assertEquals(field1.getIdentifier(), field2.getIdentifier());
            Assert.assertEquals(field1.getLongLabel(), field2.getLongLabel());
            Assert.assertEquals(field1.getType(), field2.getType());
            Assert.assertEquals(field1.getMinOccurrence(), field2.getMinOccurrence());
            Assert.assertEquals(field1.getMaxOccurrence(), field2.getMaxOccurrence());

            // component
            Assert.assertEquals(field1.getHl7Components().size(), field2.getHl7Components().size());
            Hl7ComponentXmlDto component1 = field1.getHl7Components().get(0);
            Hl7ComponentXmlDto component2 = field2.getHl7Components().get(0);
            Assert.assertEquals(component1.getName(), component2.getName());
            Assert.assertEquals(component1.getIdentifier(), component2.getIdentifier());
            Assert.assertEquals(component1.getLongLabel(), component2.getLongLabel());
            Assert.assertEquals(component1.getType(), component2.getType());

            // subcomponent
            Assert.assertEquals(component1.getHl7SubComponents().size(), component2.getHl7SubComponents().size());
            Hl7SubComponentXmlDto subComponent1 = component1.getHl7SubComponents().get(0);
            Hl7SubComponentXmlDto subComponent2 = component2.getHl7SubComponents().get(0);
            Assert.assertEquals(subComponent1.getName(), subComponent2.getName());
            Assert.assertEquals(subComponent1.getIdentifier(), subComponent2.getIdentifier());
            Assert.assertEquals(subComponent1.getLongLabel(), subComponent2.getLongLabel());
            Assert.assertEquals(subComponent1.getType(), subComponent2.getType());
        }
    }

    @Test
    public void testReadLayout() throws IOException {
        //Run similar tests as above, reading different layouts using generic method

        //Test with comma separated layout
        CommaSeparatedLayoutXmlDto commaSeparatedLayout = new CommaSeparatedLayoutXmlDto();
        commaSeparatedLayout.setId("test-utils-fixed");
        commaSeparatedLayout.setName("Test Rec Layout");
        commaSeparatedLayout.setNumFields(1);
        CommaSeparatedLayoutFieldXmlDto commaSeparatedField = new CommaSeparatedLayoutFieldXmlDto();
        commaSeparatedField.setName("field1");
        commaSeparatedField.setIndex(1);
        commaSeparatedLayout.setField(Collections.singletonList(commaSeparatedField));

        File file = new File(System.getProperty("user.dir") + "/build/fixed-layout-test.xml");
        try (OutputStream fos = new FileOutputStream(file)) {
            LayoutUtils.writeCommaSeparatedLayout(fos, commaSeparatedLayout);
        }

        try (InputStream fis = new FileInputStream(file)) {
            Assert.assertEquals(commaSeparatedLayout.getId(), LayoutUtils.readLayout(fis).getLayoutId());
        }

        //Test with fixed column layout
        FixedColumnLayoutXmlDto fixedColumnLayout = new FixedColumnLayoutXmlDto();
        fixedColumnLayout.setId("test-utils-fixed");
        fixedColumnLayout.setName("Test Rec Layout");
        fixedColumnLayout.setLength(1);
        FixedColumnLayoutFieldXmlDto fixedColumnField = new FixedColumnLayoutFieldXmlDto();
        fixedColumnField.setName("field1");
        fixedColumnField.setStart(1);
        fixedColumnField.setEnd(1);
        fixedColumnLayout.setField(Collections.singletonList(fixedColumnField));

        file = new File(System.getProperty("user.dir") + "/build/fixed-layout-test.xml");
        try (OutputStream fos = new FileOutputStream(file)) {
            LayoutUtils.writeFixedColumnsLayout(fos, fixedColumnLayout);
        }

        try (InputStream fis = new FileInputStream(file)) {
            Assert.assertEquals(fixedColumnLayout.getId(), LayoutUtils.readLayout(fis).getLayoutId());
        }

        //Test with Hl7 layout
        Hl7LayoutXmlDto layout = new Hl7LayoutXmlDto();
        layout.setId("test-hl7");
        Hl7SegmentXmlDto segment = new Hl7SegmentXmlDto();
        segment.setIdentifier("MSH");
        Hl7FieldXmlDto field = new Hl7FieldXmlDto();
        field.setName("field1");
        field.setMinOccurrence(1);
        field.setMaxOccurrence(1);
        segment.setHl7Fields(Collections.singletonList(field));
        layout.setHl7Segments(Collections.singletonList(segment));

        file = new File(System.getProperty("user.dir") + "/build/hl7-layout-test.xml");
        try (OutputStream fos = new FileOutputStream(file)) {
            LayoutUtils.writeHl7Layout(fos, layout);
        }

        try (InputStream fis = new FileInputStream(file)) {
            Assert.assertEquals(layout.getId(), LayoutUtils.readLayout(fis).getLayoutId());
        }
    }
}
