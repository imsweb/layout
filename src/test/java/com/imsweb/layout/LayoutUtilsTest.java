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

import com.imsweb.layout.record.csv.xml.CommaSeparatedLayoutFieldXmlDto;
import com.imsweb.layout.record.csv.xml.CommaSeparatedLayoutXmlDto;
import com.imsweb.layout.record.fixed.xml.FixedColumnLayoutFieldXmlDto;
import com.imsweb.layout.record.fixed.xml.FixedColumnLayoutXmlDto;

public class LayoutUtilsTest {

    @Test
    public void testFormaNumber() {
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
}
