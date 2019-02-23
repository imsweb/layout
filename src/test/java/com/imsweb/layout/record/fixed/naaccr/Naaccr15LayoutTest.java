/*
 * Copyright (C) 2012 Information Management Services, Inc.
 */
package com.imsweb.layout.record.fixed.naaccr;

import com.imsweb.layout.Field;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.TestingUtils;
import com.imsweb.layout.record.RecordLayout;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.FixedColumnsLayout;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Naaccr15LayoutTest {

    @Test
    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    public void testNaaccr15() throws IOException {
        RecordLayout layout = (RecordLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_15);

        // test layout properties
        Assert.assertEquals("naaccr-15-abstract", layout.getLayoutId());
        Assert.assertEquals("NAACCR 15 Abstract", layout.getLayoutName());
        Assert.assertEquals("150", layout.getLayoutVersion());
        Assert.assertNotNull(layout.getLayoutDescription());
        Assert.assertEquals(22824, ((FixedColumnsLayout)layout).getLayoutLineLength().intValue());

        // test fields
        Assert.assertEquals(503, layout.getAllFields().size());
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
        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-naaccr15-1-rec.txt");
        List<Map<String, String>> list = layout.readAllRecords(new File(url.getPath().replace("%20", " ")));
        Assert.assertEquals(1, list.size()); // file
        Map<String, String> rec = list.get(0);
        Assert.assertEquals("150", (rec.get("naaccrRecordVersion")));
        Assert.assertEquals("I", (rec.get("recordType")));
        Assert.assertEquals("C400", (rec.get("primarySite")));
        Assert.assertNull(rec.get("nameLast"));

        // test write methods        
        File file = new File(TestingUtils.getBuildDirectory(), "naaccr15.txt");
        rec.clear();
        rec.put("naaccrRecordVersion", "150");
        rec.put("recordType", "I");
        rec.put("primarySite", "C400");
        rec.put("nameLast", "depry");
        layout = (RecordLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_15_INCIDENCE);
        Assert.assertEquals(3339, layout.createLineFromRecord(rec, null).length());
        layout.writeRecord(file, rec); // write into a file
        rec = layout.readAllRecords(file).get(0);
        Assert.assertEquals("150", rec.get("naaccrRecordVersion"));
        Assert.assertEquals("I", rec.get("recordType"));
        Assert.assertEquals("C400", rec.get("primarySite"));
        Assert.assertNull(rec.get("nameLast"));
        file.delete();

        rec.clear();
        rec.put("primarySite", "C400");
        rec.put("nameLast", "depry");
        layout = (RecordLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_15_MODIFIED);
        Assert.assertEquals(22824, layout.createLineFromRecord(rec, null).length());
        FileWriter writer = new FileWriter(file);
        layout.writeRecord(writer, rec); // write into a writer
        writer.close();
        rec = layout.readAllRecords(file).get(0);
        Assert.assertEquals("150", rec.get("naaccrRecordVersion"));
        Assert.assertEquals("M", rec.get("recordType"));
        Assert.assertEquals("C400", rec.get("primarySite"));
        Assert.assertEquals("depry", rec.get("nameLast"));
        file.delete();

        rec.clear();
        rec.put("recordType", "C");
        rec.put("primarySite", "C400");
        rec.put("nameLast", "depry");
        layout = (RecordLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_15_CONFIDENTIAL);
        Assert.assertEquals(5564, layout.createLineFromRecord(rec, null).length());
        FileOutputStream stream = new FileOutputStream(file);
        layout.writeRecord(stream, rec); // write into an output stream
        stream.close();
        rec = layout.readAllRecords(file).get(0);
        Assert.assertEquals("150", rec.get("naaccrRecordVersion"));
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
    public void testNaaccr15Documentation() {
        FixedColumnsLayout layout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_15);

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

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testNaaccr15LayoutExtension() throws Exception {

        // since NAACCR12 is an internal layout, it doesn't need to be registered (it will be registered automatically), so let's test that too...
        LayoutFactory.unregisterAllLayouts();
        FixedColumnsLayout naaccrAbstractExtendedLayout = new FixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-extend-naaccr15.xml"));
        // as a side effect of creating a customized layout, the parent one will be loaded...
        Assert.assertTrue(LayoutFactory.isLayoutRegister(LayoutFactory.LAYOUT_ID_NAACCR_15_ABSTRACT));
        FixedColumnsLayout naaccrAbsractLayout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_15_ABSTRACT);

        // make sure the state requestor items was correctly defined
        Assert.assertFalse(naaccrAbstractExtendedLayout.getFieldByName("stateRequestorItems").getSubFields().isEmpty());
        Assert.assertNotNull(naaccrAbstractExtendedLayout.getFieldByName("registryField1"));
        Assert.assertNotNull(naaccrAbstractExtendedLayout.getFieldByName("registryField2"));
        Assert.assertNotNull(naaccrAbstractExtendedLayout.getFieldByName("registryField3"));

        Assert.assertNull(naaccrAbsractLayout.getFieldByName("stateRequestorItems").getSubFields());
        Assert.assertNull(naaccrAbsractLayout.getFieldByName("registryField1"));
        Assert.assertNull(naaccrAbsractLayout.getFieldByName("registryField2"));
        Assert.assertNull(naaccrAbsractLayout.getFieldByName("registryField3"));

        // let's use a testing data file, it's actually a NAACCR12 Incidence file, but one can import an incidence file using an abstract layout
        // if the strict format is not enforced; let's test that too...
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("fake-naaccr-1-rec.txt").getPath().replace("%20", " "));
        // whether we enforce strict format or not, the file should be recognized as a NAACCR12 Incidence
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_12_INCIDENCE, LayoutFactory.discoverFormat(file).get(0).getLayoutId());

        // read with regular layout -> value should be a long almost empty string
        Map<String, String> rec = naaccrAbsractLayout.readAllRecords(file).get(0);
        Assert.assertNotNull(rec.get("stateRequestorItems"));
        Assert.assertTrue(rec.get("stateRequestorItems").startsWith("123456"));
        Assert.assertEquals(1000, rec.get("stateRequestorItems").length()); // this field is not trimmed
        Assert.assertNull(rec.get("registryField1"));
        Assert.assertNull(rec.get("registryField2"));
        Assert.assertNull(rec.get("registryField3"));

        // same test, but with the extended layout
        rec = naaccrAbstractExtendedLayout.readAllRecords(file).get(0);
        Assert.assertNotNull(rec.get("stateRequestorItems"));
        Assert.assertTrue(rec.get("stateRequestorItems").startsWith("123456"));
        Assert.assertEquals(1000, rec.get("stateRequestorItems").length()); // this field is not trimmed
        Assert.assertEquals("1", rec.get("registryField1")); // with the extended layout, the registry fields should be populated
        Assert.assertEquals("2", rec.get("registryField2"));
        Assert.assertEquals("34", rec.get("registryField3"));

        // modify one of subfields, and write the record
        rec.put("registryField2", "X");
        String line = naaccrAbstractExtendedLayout.createLineFromRecord(rec, null);
        Assert.assertEquals(22824, line.length()); // line length for a Abstract

        // re-parse the created line into a record and check the requestor item again
        rec = naaccrAbstractExtendedLayout.createRecordFromLine(line, null, null);
        Assert.assertNotNull(rec.get("stateRequestorItems"));
        Assert.assertTrue(rec.get("stateRequestorItems").startsWith("1X34"));
        Assert.assertFalse(rec.get("stateRequestorItems").startsWith("1X3456")); // this makes sense because 56 was in a gap before writing the file, so now it's gone...
        Assert.assertEquals(1000, rec.get("stateRequestorItems").length()); // this field is not trimmed
        Assert.assertEquals("1", rec.get("registryField1")); // with the extended layout, the registry fields should be populated
        Assert.assertEquals("X", rec.get("registryField2"));
        Assert.assertEquals("34", rec.get("registryField3"));

        // what happens if we define subfields for the state requestor item, but don't override that field? -> exception
        boolean exception = false;
        try {
            new FixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-extend-naaccr15-bad.xml"));
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertTrue(exception);
    }
}
