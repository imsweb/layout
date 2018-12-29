/*
 * Copyright (C) 2012 Information Management Services, Inc.
 */
package com.imsweb.layout.record.fixed.naaccr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.imsweb.layout.Field;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.TestingUtils;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.FixedColumnsLayout;

public class Naaccr18LayoutTest {

    @BeforeClass
    public static void setup() {
        File buildFolder = new File(TestingUtils.getWorkingDirectory() + "/build/");
        if (!buildFolder.exists() && !buildFolder.mkdir())
            Assert.fail("Can't create build folder");
    }

    @Test
    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    public void testNaaccr18() throws IOException {
        FixedColumnsLayout layout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18);

        // test layout properties
        Assert.assertEquals("naaccr-18-abstract", layout.getLayoutId());
        Assert.assertEquals("NAACCR 18 Abstract", layout.getLayoutName());
        Assert.assertEquals("180", layout.getLayoutVersion());
        Assert.assertNotNull(layout.getLayoutDescription());
        Assert.assertEquals(24194, layout.getLayoutLineLength().intValue());

        // test fields
        Assert.assertEquals(746, layout.getAllFields().size()); // includes the reserved gaps
        Assert.assertEquals("recordType", layout.getFieldByName("recordType").getName());
        Assert.assertEquals("Rec Type", layout.getFieldByName("recordType").getShortLabel());
        Assert.assertEquals("Record Type", layout.getFieldByName("recordType").getLongLabel());
        Assert.assertNull(layout.getFieldByName(null));
        Assert.assertNull(layout.getFieldByName(""));
        Assert.assertNull(layout.getFieldByName("?"));
        Assert.assertEquals("recordType", layout.getFieldByNaaccrItemNumber(10).getName());

        // test doc
        Assert.assertNull(layout.getFieldByNaaccrItemNumber(null));
        Assert.assertNull(layout.getFieldByNaaccrItemNumber(1));
        Assert.assertNull(layout.getFieldByNaaccrItemNumber(-1));
        Assert.assertNotNull(layout.getFieldDocByName("recordType"));
        Assert.assertNull(layout.getFieldDocByName(null));
        Assert.assertNull(layout.getFieldDocByName(""));
        Assert.assertNull(layout.getFieldDocByName("?"));
        Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(10));
        Assert.assertNull(layout.getFieldDocByNaaccrItemNumber(null));
        Assert.assertNull(layout.getFieldDocByNaaccrItemNumber(1));
        Assert.assertNull(layout.getFieldDocByNaaccrItemNumber(-1));
        Assert.assertNotNull(layout.getFieldDocDefaultCssStyle());

        // test read methods
        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-naaccr18-1-rec.txt");
        List<Map<String, String>> list = layout.readAllRecords(new File(url.getPath().replace("%20", " ")));
        Assert.assertEquals(1, list.size()); // file
        Map<String, String> rec = list.get(0);
        Assert.assertEquals("180", (rec.get("naaccrRecordVersion")));
        Assert.assertEquals("I", (rec.get("recordType")));
        Assert.assertEquals("C400", (rec.get("primarySite")));
        Assert.assertNull(rec.get("nameLast"));
        Assert.assertNull(rec.get("reserved04"));

        // test write methods
        File file = new File(TestingUtils.getWorkingDirectory() + "/build/naaccr18.txt");
        rec.clear();
        rec.put("naaccrRecordVersion", "180");
        rec.put("recordType", "I");
        rec.put("primarySite", "C400");
        rec.put("nameLast", "depry");
        rec.put("reserved04", "This is a test with a few spaces at the end   ");
        layout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18_INCIDENCE);
        Assert.assertEquals(4048, layout.createLineFromRecord(rec, null).length());
        layout.writeRecord(file, rec); // write into a file
        rec = layout.readAllRecords(file).get(0);
        Assert.assertEquals("180", rec.get("naaccrRecordVersion"));
        Assert.assertEquals("I", rec.get("recordType"));
        Assert.assertEquals("C400", rec.get("primarySite"));
        Assert.assertNull(rec.get("nameLast"));
        Assert.assertEquals("This is a test with a few spaces at the end       ", rec.get("reserved04")); // four extra space at the end - field length =49, text length = 45
        file.delete();

        rec.clear();
        rec.put("primarySite", "C400");
        rec.put("nameLast", "depry");
        layout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18_MODIFIED);
        Assert.assertEquals(24194, layout.createLineFromRecord(rec, null).length());
        FileWriter writer = new FileWriter(file);
        layout.writeRecord(writer, rec); // write into a writer
        writer.close();
        rec = layout.readAllRecords(file).get(0);
        Assert.assertEquals("180", rec.get("naaccrRecordVersion"));
        Assert.assertEquals("M", rec.get("recordType"));
        Assert.assertEquals("C400", rec.get("primarySite"));
        Assert.assertEquals("depry", rec.get("nameLast"));
        file.delete();

        rec.clear();
        rec.put("recordType", "C");
        rec.put("primarySite", "C400");
        rec.put("nameLast", "depry");
        layout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18_CONFIDENTIAL);
        Assert.assertEquals(6934, layout.createLineFromRecord(rec, null).length());
        FileOutputStream stream = new FileOutputStream(file);
        layout.writeRecord(stream, rec); // write into an output stream
        stream.close();
        rec = layout.readAllRecords(file).get(0);
        Assert.assertEquals("180", rec.get("naaccrRecordVersion"));
        Assert.assertEquals("C", rec.get("recordType"));
        Assert.assertEquals("C400", rec.get("primarySite"));
        Assert.assertEquals("depry", rec.get("nameLast"));
        file.delete();

        //Test that all fields have a section value and that subfields have the same section as their parent field
        layout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT);
        FixedColumnsField f2 = null;
        for (FixedColumnsField f1 : layout.getAllFields()) {
            Assert.assertNotNull(f1.getSection());
            List<FixedColumnsField> subFields = f1.getSubFields();
            if (subFields != null)
                for (Field sf : subFields) {
                    Assert.assertNotNull(sf.getSection());
                    Assert.assertEquals(f1.getSection(), sf.getSection());
                }

            //Check for gaps between fields
            if (f2 != null)
                Assert.assertEquals("There is a gap between fields " + f2.getName() + " and " + f1.getName(), 1, f1.getStart() - f2.getEnd());
            f2 = f1;
        }
    }

    @Test
    public void testNaaccr18Documentation() {
        FixedColumnsLayout layout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18);

        for (FixedColumnsField field : layout.getAllFields()) {
            if (field.getNaaccrItemNum() != null)
                Assert.assertNotNull(field.getName(), layout.getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum()));
            if (field.getSubFields() != null) {
                for (FixedColumnsField f : field.getSubFields()) {
                    if (f.getNaaccrItemNum() != null)
                        Assert.assertNotNull(f.getName(), layout.getFieldDocByNaaccrItemNumber(f.getNaaccrItemNum()));
                }
            }
        }
    }
}
