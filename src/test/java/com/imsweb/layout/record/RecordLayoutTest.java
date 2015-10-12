/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.layout.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.FixedColumnsLayout;

public class RecordLayoutTest {

    private static String _TEST_LAYOUT_ID = "test-rec-layout";

    @BeforeClass
    public static void setup() throws IOException {
        // register testing layout
        FixedColumnsLayout layout = new FixedColumnsLayout();
        layout.setLayoutId(_TEST_LAYOUT_ID);
        layout.setLayoutName("Test Rec Layout");
        layout.setLayoutDescription("Description");
        layout.setLayoutVersion("1.0");
        layout.setLayoutLineLength(1);
        layout.setTrimValues(false);
        layout.setEnforceStrictFormat(true);
        FixedColumnsField field = new FixedColumnsField();
        field.setName("field1");
        field.setStart(1);
        field.setEnd(1);
        layout.setFields(Collections.singletonList(field));
        LayoutFactory.registerLayout(layout);
    }

    @Test
    public void testLayoutRegistering() {
        RecordLayout layout = (RecordLayout)LayoutFactory.getLayout(_TEST_LAYOUT_ID);

        Assert.assertEquals("test-rec-layout", layout.getLayoutId());
        Assert.assertEquals("Test Rec Layout", layout.getLayoutName());
        Assert.assertEquals("Description", layout.getLayoutDescription());
        Assert.assertEquals("1.0", layout.getLayoutVersion());
        Assert.assertNull(layout.getParentLayoutId());
        Assert.assertEquals(1, layout.getAllFields().size());
        Assert.assertNull(layout.getFieldDocByName("field1"));
        Assert.assertNull(layout.getFieldDocByNaaccrItemNumber(10));
        Assert.assertEquals("", layout.getFieldDocDefaultCssStyle());
    }

    @Test
    public void testReadMethods() throws IOException {
        RecordLayout layout = (RecordLayout)LayoutFactory.getLayout(_TEST_LAYOUT_ID);

        // create fake data file going with that testing layout
        File file = new File(System.getProperty("user.dir") + "/build/rec-layout-read-test.txt");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write("A\nB\n".getBytes()); // two data lines
        }

        // create fake zipped data file going with that testing layout
        File zippedFile = new File(System.getProperty("user.dir") + "/build/rec-layout-read-test.zip");
        try (ZipOutputStream fos = new ZipOutputStream(new FileOutputStream(zippedFile))) {
            fos.putNextEntry(new ZipEntry(file.getName()));
            fos.write("A\nB\n".getBytes()); // two data lines
        }

        // read next from reader
        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            Map<String, String> rec = layout.readNextRecord(reader);
            Assert.assertNotNull(rec);
            Assert.assertEquals(1, rec.size());
            Assert.assertEquals("A", rec.get("field1"));
            rec = layout.readNextRecord(reader);
            Assert.assertNotNull(rec);
            rec = layout.readNextRecord(reader);
            Assert.assertNull(rec);
        }

        // readd all from file
        List<Map<String, String>> recs = layout.readAllRecords(file);
        Assert.assertEquals(2, recs.size());
        Assert.assertEquals("A", recs.get(0).get("field1"));
        Assert.assertEquals("B", recs.get(1).get("field1"));

        // read all from zipped file
        List<Map<String, String>> recs2 = layout.readAllRecords(zippedFile, file.getName());
        Assert.assertEquals(recs, recs2);

        // read all from reader
        try (FileReader reader = new FileReader(file)) {
            List<Map<String, String>> recs3 = layout.readAllRecords(reader);
            Assert.assertEquals(recs, recs3);
        }

        // read all from input stream
        try (FileInputStream fis = new FileInputStream(file)) {
            List<Map<String, String>> recs3 = layout.readAllRecords(fis, StandardCharsets.US_ASCII.name());
            Assert.assertEquals(recs, recs3);
        }
    }

    @Test
    public void testWriteMethods() throws IOException {
        RecordLayout layout = (RecordLayout)LayoutFactory.getLayout(_TEST_LAYOUT_ID);

        Map<String, String> rec1 = new HashMap<>();
        rec1.put("field1", "A");
        rec1.put("otherField", "B"); // should be ignored

        File file = new File(System.getProperty("user.dir") + "/build/rec-layout-write-test.txt");

        // write single record to file
        layout.writeRecord(file, rec1);
        List<Map<String, String>> recs = layout.readAllRecords(file);
        Assert.assertEquals(1, recs.size());
        Assert.assertEquals("A", recs.get(0).get("field1"));

        // write single record to writer
        try (FileWriter writer = new FileWriter(file)) {
            layout.writeRecord(writer, rec1);
            writer.flush();
            recs = layout.readAllRecords(file);
            Assert.assertEquals(1, recs.size());
            Assert.assertEquals("A", recs.get(0).get("field1"));
        }

        // write single record to output stream
        try (FileOutputStream fos = new FileOutputStream(file)) {
            layout.writeRecord(fos, rec1);
            fos.flush();
            recs = layout.readAllRecords(file);
            Assert.assertEquals(1, recs.size());
            Assert.assertEquals("A", recs.get(0).get("field1"));
        }

        Map<String, String> rec2 = new HashMap<>();
        rec2.put("field1", "B");
        List<Map<String, String>> list = new ArrayList<>();
        list.add(rec1);
        list.add(rec2);

        // write multiple records to file
        layout.writeRecords(file, list);
        recs = layout.readAllRecords(file);
        Assert.assertEquals(2, recs.size());
        Assert.assertEquals("A", recs.get(0).get("field1"));
        Assert.assertEquals("B", recs.get(1).get("field1"));

        // write multiple records to writer
        try (FileWriter writer = new FileWriter(file)) {
            layout.writeRecords(writer, list);
            writer.flush();
            recs = layout.readAllRecords(file);
            Assert.assertEquals(2, recs.size());
            Assert.assertEquals("A", recs.get(0).get("field1"));
            Assert.assertEquals("B", recs.get(1).get("field1"));
        }

        // write multiple records to output stream
        try (FileOutputStream fos = new FileOutputStream(file)) {
            layout.writeRecords(fos, list);
            fos.flush();
            recs = layout.readAllRecords(file);
            Assert.assertEquals(2, recs.size());
            Assert.assertEquals("A", recs.get(0).get("field1"));
            Assert.assertEquals("B", recs.get(1).get("field1"));
        }
    }
}
