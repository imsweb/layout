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
import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.TestingUtils;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.FixedColumnsLayout;

public class Naaccr18LayoutTest {

    //        //@Test
    //        public void testDummy() throws Exception {
    //
    //            SortedMap<String, String> map = new TreeMap<>();
    //            for (String version : Arrays.asList("180")) {
    //                NaaccrDictionary dictionary = NaaccrXmlDictionaryUtils.getMergedDictionaries(version);
    //
    //                Map<Integer, String> startToName = new HashMap<>();
    //                for (NaaccrDictionaryItem item : dictionary.getItems())
    //                    startToName.put(item.getStartColumn(), item.getNaaccrId());
    //                Map<Integer, String> startToNameGroup = new HashMap<>();
    //                for (NaaccrDictionaryGroupedItem item : dictionary.getGroupedItems())
    //                    startToNameGroup.put(item.getStartColumn(), item.getNaaccrId());
    //
    //                NaaccrLayout layout = (NaaccrLayout)LayoutFactory.getLayout("naaccr-" + version.substring(0, 2) + "-abstract", true);
    //                for (FixedColumnsField field : layout.getAllFields()) {
    //                    String xmlName = startToNameGroup.getOrDefault(field.getStart(), startToName.get(field.getStart()));
    //                    if (xmlName == null)
    //                        System.out.println("!!! " + field.getName());
    //                    else if (!xmlName.equals(field.getName()))
    //                        map.put(xmlName, field.getName());
    //
    //                    if (field.getSubFields() != null && !"eodOld4Digit".equals(field.getName())) {
    //                        for (FixedColumnsField sField : field.getSubFields()) {
    //                            if (sField.getName().endsWith("Year")) {
    //                                if (xmlName != null && !xmlName.equals(field.getName()))
    //                                    map.put(xmlName + "Year", field.getName() + "Year");
    //                            }
    //                            else if (sField.getName().endsWith("Month")) {
    //                                if (xmlName != null && !xmlName.equals(field.getName()))
    //                                    map.put(xmlName + "Month", field.getName() + "Month");
    //                            }
    //                            else if (sField.getName().endsWith("Day")) {
    //                                if (xmlName != null && !xmlName.equals(field.getName()))
    //                                    map.put(xmlName + "Day", field.getName() + "Day");
    //                            }
    //                            else {
    //                                String sXmlName = startToName.get(sField.getStart());
    //                                if (sXmlName == null)
    //                                    System.out.println("!!! " + sField.getName());
    //                                else if (!sXmlName.equals(sField.getName()))
    //                                    map.put(sXmlName, sField.getName());
    //                            }
    //                        }
    //                    }
    //                }
    //            }
    //
    //            // TODO FD use the map to rewrite the actual XML definition files (one version at a time)
    //
    //            // map is new XML prop -> old (deprecated) layout name
    //            //for (Map.Entry<String, String> entry : map.entrySet())
    //            //  System.out.println("        _XML_TO_LAYOUT_MAPPING.put(\"" + entry.getKey() + "\", \"" + entry.getValue() + "\");");
    //
    //            SortedMap<String, String> oldToNew = new TreeMap<>();
    //            map.forEach((k, v) -> oldToNew.put(v, k));
    //    //        for (Map.Entry<String, String> entry : oldToNew.entrySet())
    //    //            System.out.println(entry.getKey() + "," + entry.getValue());
    //
    ////                    Pattern p = Pattern.compile("(.+field name=\")(.+?)(\".+)");
    ////
    ////                    File file = new File(TestingUtils.getWorkingDirectory() + "/src/main/resources/layout/fixed/naaccr/naaccr-18-layout.xml");
    ////                    for (String line : SeerUtils.readFile(file).split("\r\n")) {
    ////                        Matcher m = p.matcher(line);
    ////                        if (m.matches()) {
    ////                            System.out.println(m.group(1) + oldToNew.getOrDefault(m.group(2), m.group(2)) + m.group(3));
    ////                        }
    ////                        else
    ////                            System.out.println(line);
    ////                    }
    //
    ////            for (Map.Entry<String, String> entry : oldToNew.entrySet()) {
    ////                File file = new File(TestingUtils.getWorkingDirectory() + "/src/main/resources/layout/fixed/naaccr/doc/naaccr18/" + entry.getKey() + ".html");
    ////                if (file.exists())
    ////                    if (!file.renameTo(new File(TestingUtils.getWorkingDirectory() + "/src/main/resources/layout/fixed/naaccr/doc/naaccr18/" + entry.getValue() + ".html")))
    ////                        throw new RuntimeException(entry.getKey());
    ////            }
    //        }

    @Test
    public void testStandardNaaccrLayout() {

        // NAACCR 18
        Layout layout = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18_INCIDENCE);
        // only root level fields are available via the "getAllFields" method
        Assert.assertFalse(layout.getAllFields().isEmpty());
        Assert.assertTrue(layout.getAllFields().stream().anyMatch(f -> "primarySite".equals(f.getName())));
        Assert.assertFalse(layout.getAllFields().stream().anyMatch(f -> "nameLast".equals(f.getName())));
        Assert.assertTrue(layout.getAllFields().stream().anyMatch(f -> "dateOfDiagnosis".equals(f.getName())));
        Assert.assertFalse(layout.getAllFields().stream().anyMatch(f -> "dateOfDiagnosisYear".equals(f.getName())));
        // regular tumor field
        Assert.assertNotNull(layout.getFieldByName("primarySite"));
        Assert.assertNotNull(layout.getFieldByNaaccrItemNumber(400));
        Assert.assertNotNull(layout.getFieldDocByName("primarySite"));
        Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(400));
        // incidence format should not contain last name
        Assert.assertNull(layout.getFieldByName("nameLast"));
        Assert.assertNull(layout.getFieldByNaaccrItemNumber(2230));
        Assert.assertNull(layout.getFieldDocByName("nameLast"));
        Assert.assertNull(layout.getFieldDocByNaaccrItemNumber(2230));
        // sub-fields should be returned, but don't have their own documentation
        Assert.assertNotNull(layout.getFieldByName("dateOfDiagnosis"));
        Assert.assertNotNull(layout.getFieldByNaaccrItemNumber(390));
        Assert.assertNotNull(layout.getFieldDocByName("dateOfDiagnosis"));
        Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(390));
        Assert.assertNotNull(layout.getFieldByName("dateOfDiagnosisYear"));
        Assert.assertNull(layout.getFieldDocByName("dateOfDiagnosisYear"));
        Assert.assertNotNull(layout.getFieldByName("addrAtDxCity")); // new name based on XML ID
        Assert.assertNotNull(layout.getFieldDocByName("addrAtDxCity"));
        Assert.assertNull(layout.getFieldByName("addressAtDxCity")); // old (deprecated) name
        Assert.assertNull(layout.getFieldDocByName("addressAtDxCity"));

        LayoutFactory.unregisterAllLayouts();
        layout = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18_INCIDENCE, true);
        Assert.assertNull(layout.getFieldByName("addrAtDxCity")); // new name based on XML ID
        Assert.assertNull(layout.getFieldDocByName("addrAtDxCity"));
        Assert.assertNotNull(layout.getFieldByName("addressAtDxCity")); // old (deprecated) name
        Assert.assertNotNull(layout.getFieldDocByName("addressAtDxCity"));

        // NAACCR 16
        layout = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_INCIDENCE);
        // only root level fields are available via the "getAllFields" method
        Assert.assertFalse(layout.getAllFields().isEmpty());
        Assert.assertTrue(layout.getAllFields().stream().anyMatch(f -> "primarySite".equals(f.getName())));
        Assert.assertFalse(layout.getAllFields().stream().anyMatch(f -> "nameLast".equals(f.getName())));
        Assert.assertTrue(layout.getAllFields().stream().anyMatch(f -> "dateOfDiagnosis".equals(f.getName())));
        Assert.assertFalse(layout.getAllFields().stream().anyMatch(f -> "dateOfDiagnosisYear".equals(f.getName())));
        // regular tumor field
        Assert.assertNotNull(layout.getFieldByName("primarySite"));
        Assert.assertNotNull(layout.getFieldByNaaccrItemNumber(400));
        Assert.assertNotNull(layout.getFieldDocByName("primarySite"));
        Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(400));
        // incidence format should not contain last name
        Assert.assertNull(layout.getFieldByName("nameLast"));
        Assert.assertNull(layout.getFieldByNaaccrItemNumber(2230));
        Assert.assertNull(layout.getFieldDocByName("nameLast"));
        Assert.assertNull(layout.getFieldDocByNaaccrItemNumber(2230));
        // sub-fields should be returned, but don't have their own documentation
        Assert.assertNotNull(layout.getFieldByName("dateOfDiagnosis"));
        Assert.assertNotNull(layout.getFieldByNaaccrItemNumber(390));
        Assert.assertNotNull(layout.getFieldDocByName("dateOfDiagnosis"));
        Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(390));
        Assert.assertNotNull(layout.getFieldByName("dateOfDiagnosisYear"));
        Assert.assertNull(layout.getFieldDocByName("dateOfDiagnosisYear"));
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
        File file = new File(TestingUtils.getBuildDirectory(), "naaccr18.txt");
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
        Assert.assertEquals(6154, layout.createLineFromRecord(rec, null).length());
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
        LayoutFactory.unregisterAllLayouts();
        FixedColumnsLayout layout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18, false);

        for (FixedColumnsField field : layout.getAllFields()) {
            if (field.getNaaccrItemNum() != null) {
                if (layout.getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum()) == null) {
                    Field f = layout.getFieldByNaaccrItemNumber(field.getNaaccrItemNum()); // getting the field will ensure that we try to use the name for a legit (non-retired) field first
                    ((NaaccrLayout)layout).getFieldDocByNameOrNumber2(f.getName(), f.getNaaccrItemNum());
                }
                Assert.assertNotNull(field.getName(), layout.getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum()));
            }
            if (field.getSubFields() != null) {
                for (FixedColumnsField f : field.getSubFields()) {
                    if (f.getNaaccrItemNum() != null)
                        Assert.assertNotNull(f.getName(), layout.getFieldDocByNaaccrItemNumber(f.getNaaccrItemNum()));
                }
            }
        }

        LayoutFactory.unregisterAllLayouts();
        layout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18, true);

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

        LayoutFactory.unregisterAllLayouts();
    }

    @Test
    public void testNaaccr18Dates() {
        FixedColumnsLayout layout = (FixedColumnsLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18);

        for (FixedColumnsField field : layout.getAllFields()) {
            if (field.getEnd() - field.getStart() + 1 == 8 && field.getName().toLowerCase().contains("date")) {
                Assert.assertEquals(3, field.getSubFields().size());
                Assert.assertEquals(field.getName() + "Year", field.getSubFields().get(0).getName());
                Assert.assertEquals(field.getName() + "Month", field.getSubFields().get(1).getName());
                Assert.assertEquals(field.getName() + "Day", field.getSubFields().get(2).getName());

                Assert.assertEquals(4, field.getSubFields().get(0).getEnd() - field.getSubFields().get(0).getStart() + 1);
                Assert.assertEquals(2, field.getSubFields().get(1).getEnd() - field.getSubFields().get(1).getStart() + 1);
                Assert.assertEquals(2, field.getSubFields().get(2).getEnd() - field.getSubFields().get(2).getStart() + 1);
            }
        }
    }
}
