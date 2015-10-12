/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.layout.record.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVParser;

import com.imsweb.layout.Field.FieldAlignment;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutUtils;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.FixedColumnsLayout;

/**
 * Created on Jun 25, 2012 by depryf
 * @author depryf
 */
public class CommaSeparatedLayoutTest {

    /**
     * Created on Jun 25, 2012 by depryf
     * @throws Exception
     */
    @Test
    public void testLayout() throws Exception {

        // for this test, let's use a fake layout (let's trim values, let's enforce the format)
        CommaSeparatedLayout layout = new CommaSeparatedLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-comma-separated.xml"));
        layout.setTrimValues(true);
        layout.setEnforceStrictFormat(true);

        // test layout info
        Assert.assertEquals("test-csv", layout.getLayoutId());
        Assert.assertEquals("Test CSV", layout.getLayoutName());
        Assert.assertEquals("1.0", layout.getLayoutVersion());
        Assert.assertEquals("Just for testing...", layout.getLayoutDescription());
        Assert.assertEquals(',', layout.getSeparator());
        Assert.assertTrue(layout.ignoreFirstLine());
        Assert.assertEquals(3, layout.getLayoutNumberOfFields().intValue());

        // test field getters
        Assert.assertEquals(3, layout.getAllFields().size());
        Assert.assertEquals("recordType", layout.getFieldByName("recordType").getName());
        Assert.assertEquals("Rec Type", layout.getFieldByName("recordType").getShortLabel());
        Assert.assertEquals("Record Type", layout.getFieldByName("recordType").getLongLabel());
        Assert.assertNull(layout.getFieldByName(null));
        Assert.assertNull(layout.getFieldByName(""));
        Assert.assertNull(layout.getFieldByName("?"));
        Assert.assertEquals("recordType", layout.getFieldByNaaccrItemNumber(10).getName());

        // test validate line
        Assert.assertNull(layout.validateLine("1,2,3", 1));
        Assert.assertNull(layout.validateLine(",,", null));
        Assert.assertNotNull(layout.validateLine(null, 1));
        Assert.assertNotNull(layout.validateLine("xxx", 1));
        Assert.assertNotNull(layout.validateLine("1,2,3,4,5,6", 1));

        // test main reading method
        Map<String, String> rec = layout.createRecordFromLine("0,123,456");
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("123", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine(",,");
        Assert.assertTrue(rec.isEmpty());
        rec = layout.createRecordFromLine(" ,   ,   ", null, false, true); // test overriding the trimming options
        Assert.assertEquals(" ", rec.get("recordType"));
        Assert.assertEquals("   ", rec.get("field1"));
        Assert.assertEquals("   ", rec.get("field2"));
        boolean exception = false;
        try {
            layout.createRecordFromLine("x"); // we are using strict validation, so a bad line should generate an exception
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertTrue(exception);
        exception = false;
        try {
            layout.createRecordFromLine(null);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertTrue(exception);
        exception = false;
        try {
            layout.createRecordFromLine("");
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
        Assert.assertEquals("0,123,456", layout.createLineFromRecord(rec));
        rec.clear();
        Assert.assertEquals("X,,", layout.createLineFromRecord(rec)); // first field has a default value

        // **** re-do some of the read/write test with a layout that doesn't trim and doesn't enforce the format
        layout.setTrimValues(false);
        layout.setEnforceStrictFormat(false);

        // test validate line
        Assert.assertNull(layout.validateLine("1,2,3", 1));
        Assert.assertNull(layout.validateLine(",,", null));
        Assert.assertNotNull(layout.validateLine(null, 1));
        Assert.assertNotNull(layout.validateLine("xxx", 1));
        Assert.assertNotNull(layout.validateLine("1,2,3,4,5,6", 1));

        // test main reading method
        rec = layout.createRecordFromLine("0,123"); // only 2 fields instead of 3 but we are not enforcing the format
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("123", rec.get("field1"));
        Assert.assertNull(rec.get("field2"));
        rec = layout.createRecordFromLine(",,");
        Assert.assertTrue(rec.isEmpty());
        rec = layout.createRecordFromLine(" ,   ,   ", null, false, true); // test overriding the trimming options
        Assert.assertEquals(" ", rec.get("recordType"));
        Assert.assertEquals("   ", rec.get("field1"));
        Assert.assertEquals("   ", rec.get("field2"));
        exception = false;
        try {
            layout.createRecordFromLine("x"); // we are not using strict validation, so a bad line should not generate an exception
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertFalse(exception);
        try {
            layout.createRecordFromLine(null);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertFalse(exception);
        try {
            layout.createRecordFromLine("");
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertFalse(exception);

        // test main writing method
        rec.clear();
        rec.put("recordType", "0");
        rec.put("field1", "123");
        rec.put("field2", "456");
        Assert.assertEquals("0,123,456", layout.createLineFromRecord(rec));
        rec.clear();
        Assert.assertEquals("X,,", layout.createLineFromRecord(rec)); // first field has a default value
    }

    @Test
    @SuppressWarnings("RedundantStringConstructorCall")
    public void testPartialNaaccrLayout() throws Exception {

        // this is how the CSV layout will be used in the Data Viewer: creating a fake CSV layout from the NAACCR fields
        File file = new File(System.getProperty("user.dir") + "/src/test/resources/fake-naaccr-csv.txt");

        // first, let's get the first line
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String firstLine = reader.readLine();
        reader.close();

        // we are going to base the layout on NAACCR12
        FixedColumnsLayout naaccrLayout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_12);

        // go through each field on the first line and map it to a NAACCR field
        List<CommaSeparatedField> fields = new ArrayList<>();
        // regular CSV fields don't have subfields; only fixed-column fields have those, so we have to tweak the design a bit...
        final Map<Integer, List<FixedColumnsField>> subFields = new HashMap<>();
        int idx = 1;
        for (String header : new CSVParser().parseLine(firstLine)) {
            FixedColumnsField field = null;
            if (header.matches("#\\d+"))
                field = naaccrLayout.getFieldByNaaccrItemNumber(Integer.valueOf(header.substring(1)));
            if (field == null)
                Assert.fail("Unable to parse header '" + header + "'");
            else {
                CommaSeparatedField newField = new CommaSeparatedField();
                newField.setName(field.getName());
                newField.setNaaccrItemNum(field.getNaaccrItemNum());
                newField.setShortLabel(field.getShortLabel());
                newField.setLongLabel(field.getLongLabel());
                newField.setTrim(field.getTrim());
                newField.setDefaultValue(field.getDefaultValue());
                newField.setPadChar(field.getPadChar());
                newField.setIndex(idx++);
                if (field.getSubFields() != null && !field.getSubFields().isEmpty()) {
                    List<FixedColumnsField> newSubFields = new ArrayList<>();
                    subFields.put(newField.getIndex(), newSubFields);
                    for (FixedColumnsField ff : field.getSubFields()) {
                        FixedColumnsField newSubField = new FixedColumnsField();
                        newSubField.setName(ff.getName());
                        newSubField.setNaaccrItemNum(ff.getNaaccrItemNum());
                        newSubField.setShortLabel(ff.getShortLabel());
                        newSubField.setLongLabel(ff.getLongLabel());
                        newSubField.setTrim(ff.getTrim());
                        newSubField.setDefaultValue(ff.getDefaultValue());
                        newSubField.setAlign(ff.getAlign());
                        newSubField.setPadChar(ff.getPadChar());
                        newSubField.setStart(ff.getStart() - field.getSubFields().get(0).getStart() + 1);
                        newSubField.setEnd(ff.getEnd() - field.getSubFields().get(0).getStart() + 1);
                        newSubFields.add(newSubField);
                    }
                }
                fields.add(newField);
            }
        }

        // create the fake layout
        CommaSeparatedLayout layout = new CommaSeparatedLayout() {
            @Override
            public Map<String, String> createRecordFromLine(String line, Integer lineNumber, boolean trimValues, boolean enforceStrictFormat) throws IOException {
                Map<String, String> result = super.createRecordFromLine(line, lineNumber, trimValues, enforceStrictFormat);

                // handle subfields
                for (CommaSeparatedField field : _fields) {
                    List<FixedColumnsField> fields = subFields.get(field.getIndex());
                    if (fields != null) {
                        String originalValue = result.get(field.getName());
                        if (originalValue != null && originalValue.length() >= fields.get(fields.size() - 1).getEnd()) {
                            for (FixedColumnsField child : fields) {
                                String value = new String(originalValue.substring(child.getStart() - 1, child.getEnd()));
                                if (trimValues)
                                    value = value.trim();
                                if (!value.isEmpty())
                                    result.put(child.getName(), value);
                            }
                        }
                    }
                }

                return result;
            }

            @Override
            public String createLineFromRecord(Map<String, String> record) throws IOException {

                // handle subfields
                for (CommaSeparatedField field : _fields) {
                    List<FixedColumnsField> fields = subFields.get(field.getIndex());
                    if (fields != null) {
                        int start, end, currentIndex = 1;
                        StringBuilder result = new StringBuilder();
                        for (FixedColumnsField child : fields) {

                            // adjust for the gaps within subfields
                            start = child.getStart();
                            end = child.getEnd();
                            if (start > currentIndex + 1)
                                for (int i = 0; i < start - currentIndex - 1; i++)
                                    result.append(' ');
                            currentIndex = end;

                            String value = record.get(child.getName());
                            if (value == null)
                                value = child.getDefaultValue() != null ? child.getDefaultValue() : "";
                            int length = child.getEnd() - child.getStart() + 1;
                            if (value.length() > length)
                                throw new IOException("value too long for field '" + child.getName() + "'");
                            if (child.getAlign() == FieldAlignment.RIGHT)
                                value = LayoutUtils.pad(value, length, value.isEmpty() ? " " : child.getPadChar(), true);
                            else
                                value = LayoutUtils.pad(value, length, value.isEmpty() ? " " : child.getPadChar(), false);
                            result.append(value);
                        }
                        record.put(field.getName(), result.toString().trim());
                    }
                }

                return super.createLineFromRecord(record);
            }
        };
        layout.setLayoutId("naaccr-partial-csv");
        layout.setLayoutName("NAACCR Partial CSV");
        layout.setLayoutNumberOfFields(fields.size());
        layout.setSeparator(',');
        layout.setFields(fields);
        layout.setTrimValues(true);
        LayoutFactory.registerLayout(layout);

        // make sure we can now recognize the file
        LayoutInfo info = LayoutFactory.discoverFormat(file).get(0);
        Assert.assertNotNull(info);

        // read the data file using the new layout
        List<Map<String, String>> records = layout.readAllRecords(file);
        records.remove(0);
        Assert.assertEquals(3, records.size());
        for (Map<String, String> record : records) {
            Assert.assertNotNull(record.get("recordType"));
            Assert.assertNotNull(record.get("patientIdNumber"));
            Assert.assertNotNull(record.get("race1"));
        }
        Assert.assertEquals("20100615", records.get(0).get("birthDate"));
        Assert.assertEquals("2010", records.get(0).get("birthDateYear"));
        Assert.assertEquals("06", records.get(0).get("birthDateMonth"));
        Assert.assertEquals("15", records.get(0).get("birthDateDay"));
        Assert.assertEquals("2010", records.get(1).get("birthDate"));
        Assert.assertFalse(records.get(1).containsKey("birthDateYear"));
        Assert.assertFalse(records.get(1).containsKey("birthDateMonth"));
        Assert.assertFalse(records.get(1).containsKey("birthDateDay"));
        Assert.assertFalse(records.get(2).containsKey("birthDate"));

        // same test, but this time the values aren't trimmed...
        layout.setTrimValues(false);
        records = layout.readAllRecords(file);
        records.remove(0);
        Assert.assertEquals("20100615", records.get(0).get("birthDate"));
        Assert.assertEquals("2010", records.get(0).get("birthDateYear"));
        Assert.assertEquals("06", records.get(0).get("birthDateMonth"));
        Assert.assertEquals("15", records.get(0).get("birthDateDay"));
        Assert.assertEquals("2010    ", records.get(1).get("birthDate"));
        Assert.assertEquals("2010", records.get(1).get("birthDateYear"));
        Assert.assertEquals("  ", records.get(1).get("birthDateMonth"));
        Assert.assertEquals("  ", records.get(1).get("birthDateDay"));
        Assert.assertFalse(records.get(2).containsKey("birthDate"));

        // try to write some values with that layout: the children are written, not the parent values
        Map<String, String> toWrite = new HashMap<>();
        toWrite.put("recordType", "1");
        toWrite.put("patientIdNumber", "2");
        toWrite.put("race1", "3");
        toWrite.put("birthDateYear", "2010");
        toWrite.put("birthDateMonth", "6");
        toWrite.put("birthDateDay", "15");
        Assert.assertEquals("1,2,3,20100615", layout.createLineFromRecord(toWrite));

        toWrite.put("birthDate", "xxxxxxxx");
        Assert.assertEquals("1,2,3,20100615", layout.createLineFromRecord(toWrite));

        toWrite.clear();
        Assert.assertEquals("A,,,", layout.createLineFromRecord(toWrite)); // weird, but the record type has a default value of 'A' in the NAACCR layout...
    }

    @Test
    public void testFieldEquality() {
        CommaSeparatedField f1 = new CommaSeparatedField();
        f1.setName("f1");
        f1.setLongLabel("Field 1");

        CommaSeparatedField f2 = new CommaSeparatedField();
        f2.setName("f2");
        f2.setLongLabel("Field 2");

        Assert.assertFalse(f1.equals(f2));
        Assert.assertFalse(f1.hashCode() == f2.hashCode());

        f2.setName("f1");
        Assert.assertTrue(f1.equals(f2));
        Assert.assertTrue(f1.hashCode() == f2.hashCode());
    }
}
