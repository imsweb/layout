/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.layout.record.fixed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.LayoutUtils;
import com.imsweb.layout.TestingUtils;
import com.imsweb.layout.record.RecordLayout;
import com.imsweb.layout.record.RecordLayoutOptions;
import com.imsweb.layout.record.fixed.xml.FixedColumnLayoutFieldXmlDto;
import com.imsweb.layout.record.fixed.xml.FixedColumnLayoutXmlDto;
import com.imsweb.seerutils.SeerUtils;

@SuppressWarnings("ALL")
public class FixedColumnsLayoutTest {

    @Test
    @SuppressWarnings("java:S5961")
    public void testLayout() throws Exception {

        // test loading the layout from a URL
        FixedColumnsLayout layout = new FixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-fixed-columns.xml"));
        Assert.assertEquals("test", layout.getLayoutId());
        Assert.assertEquals("Test", layout.getLayoutName());
        Assert.assertEquals("1.0", layout.getLayoutVersion());
        Assert.assertEquals("Just for testing...", layout.getLayoutDescription());
        Assert.assertEquals(16, layout.getLayoutLineLength().intValue());

        // test loading the layout from a file
        layout = new FixedColumnsLayout(new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/testing-layout-fixed-columns.xml"));
        Assert.assertEquals("test", layout.getLayoutId());
        Assert.assertEquals("Test", layout.getLayoutName());
        Assert.assertEquals("1.0", layout.getLayoutVersion());
        Assert.assertEquals("Just for testing...", layout.getLayoutDescription());
        Assert.assertEquals(16, layout.getLayoutLineLength().intValue());

        // test loading the layout from an object
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("testing-layout-fixed-columns.xml")) {
            layout = new FixedColumnsLayout(LayoutUtils.readFixedColumnsLayout(is));
            Assert.assertEquals("test", layout.getLayoutId());
            Assert.assertEquals("Test", layout.getLayoutName());
            Assert.assertEquals("1.0", layout.getLayoutVersion());
            Assert.assertEquals("Just for testing...", layout.getLayoutDescription());
            Assert.assertEquals(16, layout.getLayoutLineLength().intValue());
        }

        // let's trim values, let's enforce the format
        RecordLayoutOptions options = new RecordLayoutOptions();
        options.setTrimValues(true);
        options.setEnforceStrictFormat(true);

        // test field getters
        Assert.assertEquals(6, layout.getAllFields().size());
        Assert.assertEquals("recordType", layout.getFieldByName("recordType").getName());
        Assert.assertEquals("Rec Type", layout.getFieldByName("recordType").getShortLabel());
        Assert.assertEquals("Record Type", layout.getFieldByName("recordType").getLongLabel());
        Assert.assertEquals(1, layout.getFieldByName("recordType").getLength().intValue());
        Assert.assertNull(layout.getFieldByName(null));
        Assert.assertNull(layout.getFieldByName(""));
        Assert.assertNull(layout.getFieldByName("?"));
        Assert.assertEquals("recordType", layout.getFieldByNaaccrItemNumber(10).getName());
        Assert.assertEquals("Section 1", layout.getFieldByName("field1").getSection());
        Assert.assertEquals(3, layout.getFieldByName("field1").getLength().intValue());
        Assert.assertNull(layout.getFieldByName("field2").getSection());

        // test validate line
        Assert.assertNull(layout.validateLine("0123456789019999", 1));
        Assert.assertNull(layout.validateLine("                ", null));
        Assert.assertNotNull(layout.validateLine(null, 1));
        Assert.assertNotNull(layout.validateLine("xxx", 1));
        Assert.assertNotNull(layout.validateLine("xxxxxxxxxxxxxxxxxxxxxxxx", 1));

        // test main reading method
        Map<String, String> rec = layout.createRecordFromLine("0123456789019999", null, options);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("123", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        Assert.assertEquals("789", rec.get("field3"));
        Assert.assertEquals("01", rec.get("field4"));
        Assert.assertEquals("0", rec.get("field4a"));
        Assert.assertEquals("1", rec.get("field4b"));
        Assert.assertEquals("9999", rec.get("field5"));
        Assert.assertEquals("99", rec.get("field5a"));
        Assert.assertEquals("99", rec.get("field5b"));
        rec = layout.createRecordFromLine("0  12   3       ", null, options);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("1", rec.get("field1"));
        Assert.assertEquals("2", rec.get("field2"));
        Assert.assertEquals("3", rec.get("field3"));
        Assert.assertEquals("    ", rec.get("field5"));
        Assert.assertNull(rec.get("field5a")); // this field is trimmed, so it shouldn't be in the result
        Assert.assertEquals("  ", rec.get("field5b"));
        rec = layout.createRecordFromLine("                ", null, options);
        Assert.assertEquals(2, rec.size());  // the record should still contain the fields that aren't trimmed
        Assert.assertTrue(rec.containsKey("field5"));
        Assert.assertTrue(rec.containsKey("field5b"));
        boolean exception = false;
        try {
            layout.createRecordFromLine("x", null, options); // we are using strict validation, so a line too short should generate an exception
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertTrue(exception);
        exception = false;
        try {
            layout.createRecordFromLine("xxxxxxxxxxxxxxxxx", null, options); // we are using strict validation, so a line too long should generate an exception
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertTrue(exception);
        exception = false;
        try {
            layout.createRecordFromLine(null, null, options);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertTrue(exception);
        exception = false;
        try {
            layout.createRecordFromLine("", null, options);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertTrue(exception);

        // test main writing method
        rec.clear();
        rec.put("recordType", "0");
        rec.put("field1", "123");
        rec.put("field2", "456");
        rec.put("field3", "789");
        rec.put("field4", "01"); // doesn't matter becuase the children are used...
        Assert.assertEquals("0123456789      ", layout.createLineFromRecord(rec,  options));
        rec.clear();
        Assert.assertEquals("X               ", layout.createLineFromRecord(rec,  options)); // first field has a default value
        rec.put("field1", "a"); // should be left-align with spaces (default)
        rec.put("field2", "b"); // should be left-align with X-char
        rec.put("field3", "c"); // should be right-align with 0-char
        rec.put("field4a", "Y");
        rec.put("field4b", "Z");
        Assert.assertEquals("Xa  bXX00cYZ    ", layout.createLineFromRecord(rec,  options));
        // same test but disable the padding
        options.setApplyPadding(false);
        Assert.assertEquals("Xa  b    cYZ    ", layout.createLineFromRecord(rec,  options));
        // same test but disable alignment
        options.setApplyAlignment(false);
        Assert.assertEquals("Xa  b  c  YZ    ", layout.createLineFromRecord(rec,  options));
        options.setApplyPadding(true);
        options.setApplyAlignment(true);

        // now parent and children don't agree -> no exception, use the children
        rec.put("field4", "01");
        layout.createLineFromRecord(rec,  options);
        Assert.assertEquals("Xa  bXX00cYZ    ", layout.createLineFromRecord(rec,  options));
        rec.put("field4", null);

        exception = false;
        try {
            rec.put("field1", "xxxx"); // field too long
            layout.createLineFromRecord(rec,  options);
            rec.put("field1", null);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertTrue(exception);
        exception = false;
        try {
            rec.put("field4a", "XX"); // child field too long
            layout.createLineFromRecord(rec,  options);
            rec.put("field4a", null);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertTrue(exception);

        // **** re-do some of the read/write test with a layout that doesn't trim and doesn't enforce the format
        options.setTrimValues(false);
        options.setEnforceStrictFormat(false);

        // test validate line
        Assert.assertNull(layout.validateLine("012345678901    ", 1));
        Assert.assertNull(layout.validateLine("                ", null));
        Assert.assertNotNull(layout.validateLine(null, 1));
        Assert.assertNotNull(layout.validateLine("xxx", 1));
        Assert.assertNotNull(layout.validateLine("xxxxxxxxxxxxxxxxxxxxxxxx", 1));

        // test main reading method
        rec = layout.createRecordFromLine("012345678901    ", null,  options);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("123", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        Assert.assertEquals("789", rec.get("field3"));
        Assert.assertEquals("01", rec.get("field4"));
        Assert.assertEquals("0", rec.get("field4a"));
        Assert.assertEquals("1", rec.get("field4b"));
        rec = layout.createRecordFromLine("0  12   3       ", null,  options);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("  1", rec.get("field1"));
        Assert.assertEquals("2  ", rec.get("field2"));
        Assert.assertEquals(" 3 ", rec.get("field3"));
        rec = layout.createRecordFromLine("                ", null,  options);
        Assert.assertFalse(rec.isEmpty()); // we are not trimming, so the fields should not be ignored...
        exception = false;
        try {
            layout.createRecordFromLine("x", null,  options); // we are NOT using strict validation, so a line too short should NOT generate an exception
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertFalse(exception);
        exception = false;
        try {
            layout.createRecordFromLine("xxxxxxxxxxxxxxxxxxxxx", null,  options); // we are NOT using strict validation, so a line too long should NOT generate an exception
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertFalse(exception);
        exception = false;
        try {
            layout.createRecordFromLine(null, null,  options);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertFalse(exception);
        exception = false;
        try {
            layout.createRecordFromLine("", null,  options);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertFalse(exception);

        // test main writing method
        options.setTrimValues(true);
        options.setEnforceStrictFormat(false);
        rec = layout.createRecordFromLine("01234567890199  ", null,  options);
        Assert.assertEquals("01234567890199  ", layout.createLineFromRecord(rec, options));
        rec.clear();
        rec.put("recordType", "0");
        rec.put("field1", "123");
        rec.put("field2", "456");
        rec.put("field3", "789");
        rec.put("field4", "01"); // doesn't matter, we only use the children
        rec.put("field5", "9999"); // doesn't matter, we only use the children
        Assert.assertEquals("0123456789      ", layout.createLineFromRecord(rec, options));
        rec.clear();
        Assert.assertEquals("X               ", layout.createLineFromRecord(rec, options)); // first field has a default value
        rec.put("field1", "a"); // should be left-align with spaces (default)
        rec.put("field2", "b"); // should be left-align with X-char
        rec.put("field3", "c"); // should be right-align with 0-char
        rec.put("field4a", "Y");
        rec.put("field4b", "Z");
        rec.put("field5a", "dd");
        rec.put("field5b", "dd");
        Assert.assertEquals("Xa  bXX00cYZdddd", layout.createLineFromRecord(rec, options));
        exception = false;
        try {
            rec.put("field4", "01"); // now parent and children don't agree -> no exception (children should be used)
            layout.createLineFromRecord(rec, options);
            rec.put("field4", null);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertFalse(exception);
        rec.clear();
        rec.put("field1", "xxxx"); // field too long
        options.setValueTooLongHandling(RecordLayoutOptions.VAL_TOO_LONG_NULLIFY);
        Assert.assertEquals("X               ", layout.createLineFromRecord(rec, options));
        options.setValueTooLongHandling(RecordLayoutOptions.VAL_TOO_LONG_CUTOFF);
        Assert.assertEquals("Xxxx            ", layout.createLineFromRecord(rec, options));
        options.setValueTooLongHandling(RecordLayoutOptions.VAL_TOO_LONG_EXCEPTION);
        exception = false;
        try {
            layout.createLineFromRecord(rec, options);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertTrue(exception);
        rec.put("field1", null);
        exception = false;
        try {
            rec.put("field4a", "XX"); // child field too long
            layout.createLineFromRecord(rec, options);
            rec.put("field4a", null);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertTrue(exception);
    }

    @Test
    public void testLayoutExtension() throws Exception {
        if (!LayoutFactory.isLayoutRegister("test"))
            LayoutFactory.registerLayout(new FixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-fixed-columns.xml")));

        FixedColumnsLayout layout = new FixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-extend.xml"));

        Assert.assertEquals(7, layout.getAllFields().size());
        Assert.assertEquals("Field 1 (Extended)", layout.getFieldByName("field1").getLongLabel());
        Assert.assertNotNull(layout.getFieldByName("field10"));

        // try loading a layout that defines an overlapping field (field1 is supposed to go from 2 to 4, let's make it go to 5 instead
        FixedColumnLayoutXmlDto xmlDto = LayoutUtils.readFixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResourceAsStream("testing-layout-extend.xml"));
        FixedColumnLayoutFieldXmlDto targetField = null;
        for (FixedColumnLayoutFieldXmlDto f : xmlDto.getField())
            if (f.getName().equals("field1"))
                targetField = f;
        Assert.assertNotNull(targetField);
        targetField.setEnd(5);
        boolean exception = false;
        try {
            new FixedColumnsLayout(xmlDto);
        }
        catch (Exception e) {
            exception = true;
        }
        Assert.assertTrue(exception);
    }

    @Test
    public void testStateRequestorItems() throws IOException {
        Map<String, String> rec = new HashMap<>();
        rec.put("registryField1", "X");

        // this layout defines a 1-char field at the beginning of the state requestor items
        RecordLayout layout = new FixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResource("testing-state-requestor-items-1.xml"));
        String line = layout.createLineFromRecord(rec, null);
        Assert.assertEquals(3339, line.length());
        rec = layout.createRecordFromLine(line, null, null);
        Assert.assertEquals("X", rec.get("registryField1"));

        // this layout defines a 1-char field in the middle of the state requestor items
        layout = new FixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResource("testing-state-requestor-items-2.xml"));
        line = layout.createLineFromRecord(rec, null);
        Assert.assertEquals(3339, line.length());
        rec = layout.createRecordFromLine(line, null, null);
        Assert.assertEquals("X", rec.get("registryField1"));

        // this layout defines a 1-char field at the end of the state requestor items
        layout = new FixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResource("testing-state-requestor-items-3.xml"));
        line = layout.createLineFromRecord(rec, null);
        Assert.assertEquals(3339, line.length());
        rec = layout.createRecordFromLine(line, null, null);
        Assert.assertEquals("X", rec.get("registryField1"));
    }

    @Test
    public void testFieldEquality() {
        FixedColumnsField f1 = new FixedColumnsField();
        f1.setName("f1");
        f1.setLongLabel("Field 1");

        FixedColumnsField f2 = new FixedColumnsField();
        f2.setName("f2");
        f2.setLongLabel("Field 2");

        Assert.assertNotEquals(f1, f2);
        Assert.assertNotEquals(f1.hashCode(), f2.hashCode());

        f2.setName("f1");
        Assert.assertEquals(f1, f2);
        Assert.assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testBuildFileInfo() throws IOException {
        if (!LayoutFactory.isLayoutRegister("test"))
            LayoutFactory.registerLayout(new FixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-fixed-columns.xml")));

        Layout layout = LayoutFactory.getLayout("test");

        LayoutInfoDiscoveryOptions options = new LayoutInfoDiscoveryOptions();
        options.setFixedColumnAllowDiscoveryFromLineLength(true);

        File file = new File(TestingUtils.getBuildDirectory(), "fixed-column-data.txt");
        SeerUtils.writeFile("1234567890123456", file);
        Assert.assertNotNull(layout.buildFileInfo(file, null, options));

        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(file, null, options));
    }
}
