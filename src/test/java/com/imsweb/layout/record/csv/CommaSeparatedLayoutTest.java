/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.layout.record.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.opencsv.CSVParserBuilder;

import com.imsweb.layout.Field.FieldAlignment;
import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.LayoutUtils;
import com.imsweb.layout.TestingUtils;
import com.imsweb.layout.record.RecordLayoutOptions;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.FixedColumnsLayout;
import com.imsweb.seerutils.SeerUtils;

/**
 * Created on Jun 25, 2012 by depryf
 * @author depryf
 */
public class CommaSeparatedLayoutTest {

    /**
     * Created on Jun 25, 2012 by depryf
     */
    @Test
    public void testLayout() throws Exception {

        // test loading the layout from a URL
        CommaSeparatedLayout layout = new CommaSeparatedLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-comma-separated.xml"));
        Assert.assertEquals("test-csv", layout.getLayoutId());
        Assert.assertEquals("Test CSV", layout.getLayoutName());
        Assert.assertEquals("1.0", layout.getLayoutVersion());
        Assert.assertEquals("Just for testing...", layout.getLayoutDescription());
        Assert.assertEquals(',', layout.getSeparator());
        Assert.assertTrue(layout.ignoreFirstLine());
        Assert.assertEquals(3, layout.getLayoutNumberOfFields().intValue());

        // test loading the layout from a file
        layout = new CommaSeparatedLayout(new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/testing-layout-comma-separated.xml"));
        Assert.assertEquals("test-csv", layout.getLayoutId());
        Assert.assertEquals("Test CSV", layout.getLayoutName());
        Assert.assertEquals("1.0", layout.getLayoutVersion());
        Assert.assertEquals("Just for testing...", layout.getLayoutDescription());
        Assert.assertEquals(',', layout.getSeparator());
        Assert.assertTrue(layout.ignoreFirstLine());
        Assert.assertEquals(3, layout.getLayoutNumberOfFields().intValue());

        // test loading the layout from an object
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("testing-layout-comma-separated.xml")) {
            layout = new CommaSeparatedLayout(LayoutUtils.readCommaSeparatedLayout(is));
            Assert.assertEquals("test-csv", layout.getLayoutId());
            Assert.assertEquals("Test CSV", layout.getLayoutName());
            Assert.assertEquals("1.0", layout.getLayoutVersion());
            Assert.assertEquals("Just for testing...", layout.getLayoutDescription());
            Assert.assertEquals(',', layout.getSeparator());
            Assert.assertTrue(layout.ignoreFirstLine());
            Assert.assertEquals(3, layout.getLayoutNumberOfFields().intValue());
        }

        // let's enforce the format
        RecordLayoutOptions options = new RecordLayoutOptions();
        options.setTrimValues(true);
        options.setEnforceStrictFormat(true);

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
        Map<String, String> rec = layout.createRecordFromLine("0,123,456", null, options);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("123", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine(",,", null, options);
        Assert.assertTrue(rec.isEmpty());

        boolean exception = false;
        try {
            layout.createRecordFromLine("x", null, options); // we are using strict validation, so a bad line should generate an exception
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
        Assert.assertEquals("0,123,456", layout.createLineFromRecord(rec, options));
        rec.clear();
        Assert.assertEquals("X,,", layout.createLineFromRecord(rec, options)); // first field has a default value

        // **** re-do some of the read/write test with a layout that doesn't trim and doesn't enforce the format
        options.setTrimValues(false);
        options.setEnforceStrictFormat(false);

        // test validate line
        Assert.assertNull(layout.validateLine("1,2,3", 1));
        Assert.assertNull(layout.validateLine(",,", null));
        Assert.assertNotNull(layout.validateLine(null, 1));
        Assert.assertNotNull(layout.validateLine("xxx", 1));
        Assert.assertNotNull(layout.validateLine("1,2,3,4,5,6", 1));

        // test main reading method
        rec = layout.createRecordFromLine("0,123", null, options); // only 2 fields instead of 3 but we are not enforcing the format
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("123", rec.get("field1"));
        Assert.assertNull(rec.get("field2"));
        rec = layout.createRecordFromLine(",,", null, options);
        Assert.assertTrue(rec.isEmpty());

        exception = false;
        try {
            layout.createRecordFromLine("x", null, options); // we are not using strict validation, so a bad line should not generate an exception
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertFalse(exception);
        try {
            layout.createRecordFromLine(null, null, options);
        }
        catch (IOException e) {
            exception = true;
        }
        Assert.assertFalse(exception);
        try {
            layout.createRecordFromLine("", null, options);
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
        Assert.assertEquals("0,123,456", layout.createLineFromRecord(rec, options));
        rec.clear();
        Assert.assertEquals("X,,", layout.createLineFromRecord(rec, options)); // first field has a default value
    }

    @Test
    public void testPartialNaaccrLayout() throws Exception {

        // this is how the CSV layout will be used in the Data Viewer: creating a fake CSV layout from the NAACCR fields
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-naaccr-csv.txt");

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
        for (String header : new CSVParserBuilder().build().parseLine(firstLine)) {
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
                newField.setLength(field.getEnd() - field.getStart() + 1);
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
            public Map<String, String> createRecordFromLine(String line, Integer lineNumber, RecordLayoutOptions options) throws IOException {
                Map<String, String> result = super.createRecordFromLine(line, lineNumber, options);

                // handle subfields
                for (CommaSeparatedField field : _fields) {
                    List<FixedColumnsField> fields = subFields.get(field.getIndex());
                    if (fields != null) {
                        String originalValue = result.get(field.getName());
                        if (originalValue != null && originalValue.length() >= fields.get(fields.size() - 1).getEnd()) {
                            for (FixedColumnsField child : fields) {
                                String value = originalValue.substring(child.getStart() - 1, child.getEnd());
                                if (trimValues(options))
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
            public String createLineFromRecord(Map<String, String> rec, RecordLayoutOptions options) throws IOException {

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

                            String value = rec.get(child.getName());
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
                        rec.put(field.getName(), result.toString().trim());
                    }
                }

                return super.createLineFromRecord(rec, options);
            }
        };
        layout.setLayoutId("naaccr-partial-csv");
        layout.setLayoutName("NAACCR Partial CSV");
        layout.setIgnoreFirstLine(true);
        layout.setLayoutNumberOfFields(fields.size());
        layout.setSeparator(',');
        layout.setFields(fields);
        LayoutFactory.registerLayout(layout);

        // make sure we can now recognize the file
        LayoutInfo info = LayoutFactory.discoverFormat(file).get(0);
        Assert.assertNotNull(info);

        // read the data file using the new layout
        List<Map<String, String>> records = layout.readAllRecords(file);
        Assert.assertEquals(3, records.size());
        for (Map<String, String> record : records) {
            Assert.assertNotNull(record.get("recordType"));
            Assert.assertNotNull(record.get("patientIdNumber"));
            Assert.assertNotNull(record.get("race1"));
        }
        Assert.assertEquals("20100615", records.get(0).get("dateOfBirth"));
        Assert.assertEquals("2010", records.get(0).get("dateOfBirthYear"));
        Assert.assertEquals("06", records.get(0).get("dateOfBirthMonth"));
        Assert.assertEquals("15", records.get(0).get("dateOfBirthDay"));
        Assert.assertEquals("2010", records.get(1).get("dateOfBirth"));
        Assert.assertFalse(records.get(1).containsKey("dateOfBirthYear"));
        Assert.assertFalse(records.get(1).containsKey("dateOfBirthMonth"));
        Assert.assertFalse(records.get(1).containsKey("dateOfBirthDay"));
        Assert.assertFalse(records.get(2).containsKey("dateOfBirth"));

        // same test, but this time the values aren't trimmed...
        RecordLayoutOptions options = new RecordLayoutOptions();
        options.setTrimValues(false);
        records = layout.readAllRecords(file, options);
        Assert.assertEquals("20100615", records.get(0).get("dateOfBirth"));
        Assert.assertEquals("2010", records.get(0).get("dateOfBirthYear"));
        Assert.assertEquals("06", records.get(0).get("dateOfBirthMonth"));
        Assert.assertEquals("15", records.get(0).get("dateOfBirthDay"));
        Assert.assertEquals("2010    ", records.get(1).get("dateOfBirth"));
        Assert.assertEquals("2010", records.get(1).get("dateOfBirthYear"));
        Assert.assertEquals("  ", records.get(1).get("dateOfBirthMonth"));
        Assert.assertEquals("  ", records.get(1).get("dateOfBirthDay"));
        Assert.assertFalse(records.get(2).containsKey("dateOfBirth"));

        // test reading an empty file
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-naaccr-csv-empty.txt");
        records = layout.readAllRecords(file);
        Assert.assertEquals(0, records.size());

        // try to write some values with that layout: the children are written, not the parent values
        Map<String, String> toWrite = new HashMap<>();
        toWrite.put("recordType", "1");
        toWrite.put("patientIdNumber", "2");
        toWrite.put("race1", "3");
        toWrite.put("dateOfBirthYear", "2010");
        toWrite.put("dateOfBirthMonth", "6");
        toWrite.put("dateOfBirthDay", "15");
        Assert.assertEquals("1,2,3,20100615", layout.createLineFromRecord(toWrite, options));

        toWrite.put("dateOfBirth", "xxxxxxxx");
        Assert.assertEquals("1,2,3,20100615", layout.createLineFromRecord(toWrite, options));

        toWrite.clear();
        Assert.assertEquals("A,,,", layout.createLineFromRecord(toWrite, options)); // weird, but the record type has a default value of 'A' in the NAACCR layout...
    }

    @Test
    public void testFieldEquality() {
        CommaSeparatedField f1 = new CommaSeparatedField();
        f1.setName("f1");
        f1.setLongLabel("Field 1");

        CommaSeparatedField f2 = new CommaSeparatedField();
        f2.setName("f2");
        f2.setLongLabel("Field 2");

        Assert.assertNotEquals(f1, f2);
        Assert.assertNotEquals(f1.hashCode(), f2.hashCode());

        f2.setName("f1");
        Assert.assertEquals(f1, f2);
        Assert.assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    public void testReadingSpecialCases() throws IOException {
        CommaSeparatedLayout layout = new CommaSeparatedLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-comma-separated.xml"));
        Assert.assertEquals(',', layout.getSeparator());

        Map<String, String> rec = layout.createRecordFromLine("0,123,456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("123", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine("0,\"12,3\",456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("12,3", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine("0,\"123\",456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("123", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine("0,1\"2\"3,456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("1\"2\"3", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine("0,\"1\"\"2\"\",3\",456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("1\"2\",3", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));

        layout.setSeparator('|');
        rec = layout.createRecordFromLine("0|123|456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("123", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine("0|\"12|3\"|456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("12|3", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine("0|12,3|456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("12,3", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine("0|\"123\"|456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("123", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine("0|1\"2\"3|456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("1\"2\"3", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine("0|\"1\"\"2\"\"|3\"|456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("1\"2\"|3", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));

        // the tricky part with new lines is actually reading all the lines together for a given value; but
        // in this test, we provide the line, and so it doesn't matter if it starts with quotes or not...
        layout.setSeparator(',');
        rec = layout.createRecordFromLine("0,\"1\r\n2\r\n3\",456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("1\r\n2\r\n3", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));
        rec = layout.createRecordFromLine("0,1\r\n2\r\n3,456", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("1\r\n2\r\n3", rec.get("field1"));
        Assert.assertEquals("456", rec.get("field2"));

        // escape character
        rec = layout.createRecordFromLine("0,\"1\\2\\3\",\\", null, null);
        Assert.assertEquals("0", rec.get("recordType"));
        Assert.assertEquals("1\\2\\3", rec.get("field1"));
        Assert.assertEquals("\\", rec.get("field2"));

        try {
            layout.createRecordFromLine("0,12\"3,456", 2, null);
            Assert.fail("Should have been an exception!");
        }
        catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Line 2"));
        }
    }

    @Test
    public void testWritingSpecialCases() throws IOException {
        CommaSeparatedLayout layout = new CommaSeparatedLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-comma-separated.xml"));
        Assert.assertEquals(',', layout.getSeparator());

        RecordLayoutOptions options = new RecordLayoutOptions();
        options.setQuoteAllValues(true);

        Map<String, String> rec = new HashMap<>();
        rec.put("recordType", "0");
        rec.put("field1", "123");
        rec.put("field2", "456");
        Assert.assertEquals("0,123,456", layout.createLineFromRecord(rec, null));
        Assert.assertEquals("\"0\",\"123\",\"456\"", layout.createLineFromRecord(rec, options));
        rec.put("field1", "12,3");
        Assert.assertEquals("0,\"12,3\",456", layout.createLineFromRecord(rec, null));
        rec.put("field1", "1\"2\"3");
        Assert.assertEquals("0,\"1\"\"2\"\"3\",456", layout.createLineFromRecord(rec, null));
        rec.put("field1", "1\"2\",3");
        Assert.assertEquals("0,\"1\"\"2\"\",3\",456", layout.createLineFromRecord(rec, null));

        layout.setSeparator('|');
        rec = new HashMap<>();
        rec.put("recordType", "0");
        rec.put("field1", "123");
        rec.put("field2", "456");
        Assert.assertEquals("0|123|456", layout.createLineFromRecord(rec, null));
        rec.put("field1", "12,3");
        Assert.assertEquals("0|12,3|456", layout.createLineFromRecord(rec, null));
        rec.put("field1", "12|3");
        Assert.assertEquals("0|\"12|3\"|456", layout.createLineFromRecord(rec, null));
        rec.put("field1", "1\"2\"3");
        Assert.assertEquals("0|\"1\"\"2\"\"3\"|456", layout.createLineFromRecord(rec, null));
        rec.put("field1", "1\"2\"|3");
        Assert.assertEquals("0|\"1\"\"2\"\"|3\"|456", layout.createLineFromRecord(rec, null));

        layout.setSeparator(',');
        rec = new HashMap<>();
        rec.put("recordType", "0");
        rec.put("field1", "1\r\n2\r\n3");
        rec.put("field2", "456");
        Assert.assertEquals("0,\"1\r\n2\r\n3\",456", layout.createLineFromRecord(rec, null));
        rec.put("field1", "\n123\n");
        Assert.assertEquals("0,\"\n123\n\",456", layout.createLineFromRecord(rec, null));
        Assert.assertEquals("\"0\",\"\n123\n\",\"456\"", layout.createLineFromRecord(rec, options));
    }

    @Test
    public void testBuildFileInfo() throws IOException {
        if (!LayoutFactory.isLayoutRegister("test-csv"))
            LayoutFactory.registerLayout(new CommaSeparatedLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-comma-separated.xml")));

        Layout layout = LayoutFactory.getLayout("test-csv");

        LayoutInfoDiscoveryOptions options = new LayoutInfoDiscoveryOptions();
        options.setCommaSeparatedAllowDiscoveryFromNumFields(true);

        File file = new File(TestingUtils.getBuildDirectory(), "csv-data-test.txt");
        SeerUtils.writeFile("A,B,C", file);
        Assert.assertNotNull(layout.buildFileInfo(file, null, options));

        options.setCommaSeparatedAllowDiscoveryFromNumFields(false);
        Assert.assertNull(layout.buildFileInfo(file, null, options));
    }
}
