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
import org.junit.Test;

import com.imsweb.layout.Field;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.record.RecordLayout;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.FixedColumnsLayout;

public class Naaccr16LayoutTest {

    @Test
    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    public void testNaaccr16() throws IOException {
        RecordLayout layout = (RecordLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16);

        // test layout properties
        Assert.assertEquals("naaccr-16-abstract", layout.getLayoutId());
        Assert.assertEquals("NAACCR 16 Abstract", layout.getLayoutName());
        Assert.assertEquals("160", layout.getLayoutVersion());
        Assert.assertNotNull(layout.getLayoutDescription());
        Assert.assertEquals(22824, ((FixedColumnsLayout)layout).getLayoutLineLength().intValue());

        // test fields
        Assert.assertEquals(553, layout.getAllFields().size()); // includes the reserved gaps
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
        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-naaccr16-1-rec.txt");
        List<Map<String, String>> list = layout.readAllRecords(new File(url.getPath().replace("%20", " ")));
        Assert.assertEquals(1, list.size()); // file
        Map<String, String> rec = list.get(0);
        Assert.assertEquals("160", (rec.get("naaccrRecordVersion")));
        Assert.assertEquals("I", (rec.get("recordType")));
        Assert.assertEquals("C400", (rec.get("primarySite")));
        Assert.assertNull(rec.get("nameLast"));
        Assert.assertNull(rec.get("reserved01"));

        // test write methods        
        File file = new File(System.getProperty("user.dir") + "/build/naaccr16.txt");
        rec.clear();
        rec.put("naaccrRecordVersion", "160");
        rec.put("recordType", "I");
        rec.put("primarySite", "C400");
        rec.put("nameLast", "depry");
        rec.put("reserved01", "Some test with spaces at the end   ");
        layout = (RecordLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_INCIDENCE);
        Assert.assertEquals(3339, layout.createLineFromRecord(rec).length());
        layout.writeRecord(file, rec); // write into a file
        rec = layout.readAllRecords(file).get(0);
        Assert.assertEquals("160", rec.get("naaccrRecordVersion"));
        Assert.assertEquals("I", rec.get("recordType"));
        Assert.assertEquals("C400", rec.get("primarySite"));
        Assert.assertNull(rec.get("nameLast"));
        Assert.assertEquals("Some test with spaces at the end     ", rec.get("reserved01")); // two extra space at the end since 
        file.delete();

        rec.clear();
        rec.put("primarySite", "C400");
        rec.put("nameLast", "depry");
        layout = (RecordLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_MODIFIED);
        Assert.assertEquals(22824, layout.createLineFromRecord(rec).length());
        FileWriter writer = new FileWriter(file);
        layout.writeRecord(writer, rec); // write into a writer
        writer.close();
        rec = layout.readAllRecords(file).get(0);
        Assert.assertEquals("160", rec.get("naaccrRecordVersion"));
        Assert.assertEquals("M", rec.get("recordType"));
        Assert.assertEquals("C400", rec.get("primarySite"));
        Assert.assertEquals("depry", rec.get("nameLast"));
        file.delete();

        rec.clear();
        rec.put("recordType", "C");
        rec.put("primarySite", "C400");
        rec.put("nameLast", "depry");
        layout = (RecordLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_CONFIDENTIAL);
        Assert.assertEquals(5564, layout.createLineFromRecord(rec).length());
        FileOutputStream stream = new FileOutputStream(file);
        layout.writeRecord(stream, rec); // write into an output stream
        stream.close();
        rec = layout.readAllRecords(file).get(0);
        Assert.assertEquals("160", rec.get("naaccrRecordVersion"));
        Assert.assertEquals("C", rec.get("recordType"));
        Assert.assertEquals("C400", rec.get("primarySite"));
        Assert.assertEquals("depry", rec.get("nameLast"));
        file.delete();

        //Test that all fields have a section value and that subfields have the same section as their parent field
        List<FixedColumnsField> fields = ((FixedColumnsLayout)layout).getAllFields();
        for (FixedColumnsField f : fields) {
            Assert.assertNotNull(f.getSection());
            List<FixedColumnsField> subFields = f.getSubFields();
            if (subFields != null) {
                for (Field sf : subFields) {
                    Assert.assertNotNull(sf.getSection());
                    Assert.assertEquals(f.getSection(), sf.getSection());
                }
            }
        }
    }

    @Test
    public void testNaaccr16Documentation() {
        FixedColumnsLayout layout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16);

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
