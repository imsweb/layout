/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.naaccrxml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.TestingUtils;
import com.imsweb.naaccrxml.NaaccrOptions;
import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.NaaccrXmlUtils;
import com.imsweb.naaccrxml.PatientXmlReader;
import com.imsweb.naaccrxml.PatientXmlWriter;
import com.imsweb.naaccrxml.entity.Item;
import com.imsweb.naaccrxml.entity.NaaccrData;
import com.imsweb.naaccrxml.entity.Patient;
import com.imsweb.naaccrxml.entity.Tumor;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;

public class NaaccrXmlLayoutTest {

    @Test
    public void testStandardNaaccrXmlLayout() {

        // NAACCR 18
        Layout layout = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_18_INCIDENCE);
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
    public void testNaaccrXmlLayout() {

        //Test Version 160 - don't load fields/dictionaries
        NaaccrXmlLayout layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, null, false);
        Assert.assertEquals(Collections.emptyList(), layout.getAllFields());
        Assert.assertEquals("160", layout.getLayoutVersion());
        Assert.assertEquals("test-id", layout.getLayoutId());
        Assert.assertEquals("test-name", layout.getLayoutName());
        Assert.assertEquals("A", layout.getRecordType());
        Assert.assertNull(layout.getFieldByNaaccrItemNumber(10));
        Assert.assertNull(layout.getFieldByName("recordType"));

        //Test version 160 - load fields/dictionaries
        layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, null, true);
        Assert.assertEquals(587, layout.getAllFields().size());
        Assert.assertEquals("160", layout.getBaseDictionary().getNaaccrVersion());
        Assert.assertEquals(1, layout.getUserDictionaries().size());
        Assert.assertEquals("160", layout.getUserDictionaries().get(0).getNaaccrVersion());
        Assert.assertNotNull(layout.getFieldByNaaccrItemNumber(10));
        Assert.assertNotNull(layout.getFieldByName("recordType"));
        Assert.assertNotNull(layout.getFieldByNaaccrItemNumber(37));
        Assert.assertNotNull(layout.getFieldByName("reserved00"));
        Assert.assertEquals(3, layout.getFieldByName("dateOfDiagnosis").getSubFields().size());
        Assert.assertNull(layout.getFieldByName("recordType").getSubFields());

        //Check subfield values
        NaaccrXmlField yearFld = layout.getFieldByName("dateOfDiagnosis").getSubFields().get(0);
        Assert.assertEquals("dateOfDiagnosisYear", yearFld.getNaaccrId());
        Assert.assertEquals("Date of Diagnosis (Year)", yearFld.getNaaccrName());
        Assert.assertEquals(NaaccrXmlUtils.NAACCR_XML_TAG_TUMOR, yearFld.getParentXmlElement());

        //User dictionary for testing
        NaaccrDictionary userDictionary = new NaaccrDictionary();
        userDictionary.setDictionaryUri("http://mycompany.org/my-very-own-naaccr-dictionary.xml");
        userDictionary.setSpecificationVersion("1.3");
        NaaccrDictionaryItem item = new NaaccrDictionaryItem();
        item.setNaaccrId("itemId");
        item.setNaaccrNum(10003);
        item.setNaaccrName("Item name");
        item.setLength(1);
        item.setParentXmlElement("Patient");
        NaaccrDictionaryItem dateItem = new NaaccrDictionaryItem();
        dateItem.setNaaccrId("dateId");
        dateItem.setNaaccrNum(10004);
        dateItem.setNaaccrName("Date Name");
        dateItem.setLength(8);
        dateItem.setParentXmlElement("Patient");
        dateItem.setDataType("date");
        userDictionary.setItems(Arrays.asList(item, dateItem));

        layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, Collections.singletonList(userDictionary), true);
        Assert.assertEquals(566, layout.getAllFields().size());
        Assert.assertNotNull(layout.getFieldByNaaccrItemNumber(10003));
        Assert.assertNotNull(layout.getFieldByName("itemId"));
        Assert.assertEquals(1, layout.getUserDictionaries().size());
        Assert.assertEquals(3, layout.getFieldByName("dateId").getSubFields().size());
        Assert.assertNull(layout.getFieldByName("itemId").getSubFields());
        Assert.assertEquals(3, layout.getFieldByName("dateOfDiagnosis").getSubFields().size());
        Assert.assertNull(layout.getFieldByName("recordType").getSubFields());

        layout = new NaaccrXmlLayout("160", "M", "test-id", "test-name", null, null, true);
        Assert.assertEquals("M", layout.getRecordType());
        Assert.assertEquals(587, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("160", "C", "test-id", "test-name", null, null, true);
        Assert.assertEquals("C", layout.getRecordType());
        Assert.assertEquals(568, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("160", "I", "test-id", "test-name", null, null, true);
        Assert.assertEquals("I", layout.getRecordType());
        Assert.assertEquals(496, layout.getAllFields().size());

        //Test version 150
        layout = new NaaccrXmlLayout("150", "A", "test-id", "test-name", null, null, true);
        Assert.assertEquals("150", layout.getLayoutVersion());
        Assert.assertEquals("150", layout.getBaseDictionary().getNaaccrVersion());
        Assert.assertEquals("A", layout.getRecordType());
        Assert.assertEquals(1, layout.getUserDictionaries().size());
        Assert.assertEquals("150", layout.getUserDictionaries().get(0).getNaaccrVersion());
        Assert.assertEquals(555, layout.getAllFields().size());
        Assert.assertEquals(3, layout.getFieldByName("dateOfDiagnosis").getSubFields().size());
        Assert.assertNull(layout.getFieldByName("recordType").getSubFields());

        layout = new NaaccrXmlLayout("150", "M", "test-id", "test-name", null, null, true);
        Assert.assertEquals("M", layout.getRecordType());
        Assert.assertEquals(555, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("150", "C", "test-id", "test-name", null, null, true);
        Assert.assertEquals("C", layout.getRecordType());
        Assert.assertEquals(536, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("150", "I", "test-id", "test-name", null, null, true);
        Assert.assertEquals("I", layout.getRecordType());
        Assert.assertEquals(464, layout.getAllFields().size());

        //Test Version 140
        layout = new NaaccrXmlLayout("140", "A", "test-id", "test-name", null, null, true);
        Assert.assertEquals("140", layout.getLayoutVersion());
        Assert.assertEquals("140", layout.getBaseDictionary().getNaaccrVersion());
        Assert.assertEquals("A", layout.getRecordType());
        Assert.assertEquals(1, layout.getUserDictionaries().size());
        Assert.assertEquals("140", layout.getUserDictionaries().get(0).getNaaccrVersion());
        Assert.assertEquals(548, layout.getAllFields().size());
        Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(10));
        Assert.assertEquals(3, layout.getFieldByName("dateOfDiagnosis").getSubFields().size());
        Assert.assertNull(layout.getFieldByName("recordType").getSubFields());

        layout = new NaaccrXmlLayout("140", "M", "test-id", "test-name", null, null, true);
        Assert.assertEquals("M", layout.getRecordType());
        Assert.assertEquals(548, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("140", "C", "test-id", "test-name", null, null, true);
        Assert.assertEquals("C", layout.getRecordType());
        Assert.assertEquals(529, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("140", "I", "test-id", "test-name", null, null, true);
        Assert.assertEquals("I", layout.getRecordType());
        Assert.assertEquals(457, layout.getAllFields().size());
    }

    @Test
    public void testNaaccrXmlLayoutBad() {

        //Null NAACCR version
        boolean exceptionCaught = false;
        try {
            new NaaccrXmlLayout(null, "A", "test-id", "test-name", null, null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Null recordType
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", null, "test-id", "test-name", null, null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Null ID
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "A", null, "test-name", null, null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Null name
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "A", "test-id", null, null, null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Bad version number
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("1160", "A", "test-id", "test-name", null, null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Bad recordType
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "Bad", "test-id", "test-name", null, null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Bad user dictionary - Missing Specification version
        NaaccrDictionary badDictionary = new NaaccrDictionary();
        badDictionary.setDictionaryUri("http://mycompany.org/my-very-own-naaccr-dictionary.xml");
        NaaccrDictionaryItem duplicateItem = new NaaccrDictionaryItem();
        duplicateItem.setNaaccrId("itemId");
        duplicateItem.setNaaccrName("Item Name");
        duplicateItem.setNaaccrNum(10001);
        duplicateItem.setLength(1);
        duplicateItem.setParentXmlElement("Tumor");
        badDictionary.setItems(Collections.singletonList(duplicateItem));
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, Collections.singletonList(badDictionary), true);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Bad user dictionary - duplicate ID
        badDictionary.setSpecificationVersion("1.3");
        duplicateItem.setNaaccrId("recordType");
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, Collections.singletonList(badDictionary), true);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Bad user dictionary - duplicate Name
        duplicateItem.setNaaccrId("itemId");
        duplicateItem.setNaaccrName("Record Type");
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, Collections.singletonList(badDictionary), true);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Bad user dictionary - duplicate NAACCR Number
        duplicateItem.setNaaccrName("Item name");
        duplicateItem.setNaaccrNum(10);
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, Collections.singletonList(badDictionary), true);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);
    }

    @Test
    public void testDefaultConstructor() {
        NaaccrXmlLayout layout = new NaaccrXmlLayout();
        layout.setLayoutId("test");
        layout.setLayoutName("Test");
        layout.setLayoutDescription("For testing...");
        layout.setNaaccrVersion("180");
        layout.setRecordType("I");
        layout.setBaseDictionary(NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion("180"));
        layout.setUserDictionaries(Collections.singletonList(NaaccrXmlDictionaryUtils.getDefaultUserDictionaryByVersion("180")));
        layout.setFields(Collections.singleton(new NaaccrXmlField(NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion("180").getItemByNaaccrId("primarySite"))));
        layout.verify();
    }

    @Test
    public void testLoadFieldsMechanism() {
        //There are 478 items in the base dictionary and 18 items in the default user dictionary that apply to the "Incidence" type,
        NaaccrXmlLayout layout = new NaaccrXmlLayout("160", "I", "test-id", "test-name", null, null, true);
        Assert.assertEquals(496, layout.getAllFields().size());

        //User dictionary for testing
        NaaccrDictionary userDictionary = new NaaccrDictionary();
        userDictionary.setDictionaryUri("http://mycompany.org/my-very-own-naaccr-dictionary.xml");
        userDictionary.setSpecificationVersion("1.3");

        List<NaaccrDictionaryItem> itemList = new ArrayList<>();
        // Item with no record type
        NaaccrDictionaryItem itemNoRecs = new NaaccrDictionaryItem();
        itemNoRecs.setNaaccrId("itemId1");
        itemNoRecs.setNaaccrNum(10001);
        itemNoRecs.setNaaccrName("Item name1");
        itemNoRecs.setLength(1);
        itemNoRecs.setParentXmlElement("Patient");
        itemNoRecs.setRecordTypes("A,M,C,I");
        itemList.add(itemNoRecs);

        // Item with empty record types
        NaaccrDictionaryItem itemEmptyRecs = new NaaccrDictionaryItem();
        itemEmptyRecs.setNaaccrId("itemId2");
        itemEmptyRecs.setNaaccrNum(10002);
        itemEmptyRecs.setNaaccrName("Item name2");
        itemEmptyRecs.setLength(1);
        itemEmptyRecs.setParentXmlElement("Patient");
        itemNoRecs.setRecordTypes("A,M,C,I");
        itemList.add(itemEmptyRecs);

        //Item with all record types
        NaaccrDictionaryItem itemAllRecs = new NaaccrDictionaryItem();
        itemAllRecs.setNaaccrId("itemId3");
        itemAllRecs.setNaaccrNum(10003);
        itemAllRecs.setNaaccrName("Item name3");
        itemAllRecs.setLength(1);
        itemAllRecs.setParentXmlElement("Patient");
        itemAllRecs.setRecordTypes("A,M,C,I");
        itemList.add(itemAllRecs);

        //Item with only the single target record type
        NaaccrDictionaryItem itemOnlyTargetRec = new NaaccrDictionaryItem();
        itemOnlyTargetRec.setNaaccrId("itemId4");
        itemOnlyTargetRec.setNaaccrNum(10004);
        itemOnlyTargetRec.setNaaccrName("Item name4");
        itemOnlyTargetRec.setLength(1);
        itemOnlyTargetRec.setParentXmlElement("Patient");
        itemOnlyTargetRec.setRecordTypes("I");
        itemList.add(itemOnlyTargetRec);

        //Item with all record types except for target type
        NaaccrDictionaryItem itemAllExceptRec = new NaaccrDictionaryItem();
        itemAllExceptRec.setNaaccrId("itemId5");
        itemAllExceptRec.setNaaccrNum(10005);
        itemAllExceptRec.setNaaccrName("Item name5");
        itemAllExceptRec.setLength(1);
        itemAllExceptRec.setParentXmlElement("Patient");
        itemAllExceptRec.setRecordTypes("A,M,C");
        itemList.add(itemAllExceptRec);

        userDictionary.setItems(itemList);

        //There are 478 fields in the base dictionary and 4 fields in the custom user dictionary that apply to the "Incidence" type.
        layout = new NaaccrXmlLayout("160", "I", "test-id", "test-name", null, Collections.singletonList(userDictionary), true);
        Assert.assertEquals(482, layout.getAllFields().size());
        Assert.assertEquals(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_INCIDENCE).getFieldByName("recordType").getShortLabel(), layout.getFieldByName("recordType").getShortLabel());
        Assert.assertEquals("DX Yr", layout.getFieldByName("dateOfDiagnosis").getSubFields().get(0).getShortLabel());
        Assert.assertEquals("DX Mth", layout.getFieldByName("dateOfDiagnosis").getSubFields().get(1).getShortLabel());
        Assert.assertEquals("DX Day", layout.getFieldByName("dateOfDiagnosis").getSubFields().get(2).getShortLabel());
    }

    @Test
    public void testLayoutDocumentation() {
        NaaccrXmlLayout layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, null, true);

        for (NaaccrXmlField field : layout.getAllFields()) {
            Assert.assertNotNull(layout.getFieldDocByName(field.getName()));
            Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum()));
        }

        Assert.assertNull(layout.getFieldDocByNaaccrItemNumber(123456));
        Assert.assertNull(layout.getFieldDocByName("A fake field name"));

        layout = new NaaccrXmlLayout("150", "A", "test-id", "test-name", null, null, true);

        for (NaaccrXmlField field : layout.getAllFields()) {
            //The v15 dictionary contains "reserved" fields, but the v15 layout does not
            if (!field.getNaaccrId().startsWith("reserved")) {
                Assert.assertNotNull(layout.getFieldDocByName(field.getName()));
                Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum()));
            }
        }

        layout = new NaaccrXmlLayout("140", "A", "test-id", "test-name", null, null, true);
        //The v14 dictionary contains "reserved" fields, but the v14 layout does not
        for (NaaccrXmlField field : layout.getAllFields()) {
            if (!field.getNaaccrId().startsWith("reserved")) {
                Assert.assertNotNull(layout.getFieldDocByName(field.getName()));
                Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum()));
            }
        }
    }

    @Test
    public void testReadMethods() throws IOException {
        NaaccrXmlLayout layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, null, false);
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-two-patients.xml");
        NaaccrOptions options = new NaaccrOptions();
        options.setUseStrictNamespaces(false);
        List<Patient> patients;

        //Test read single patient
        try (PatientXmlReader reader = new PatientXmlReader(new FileReader(file), options)) {
            Patient patient = layout.readNextPatient(reader);
            Assert.assertEquals("00000001", patient.getItemValue("patientIdNumber"));
            patient = layout.readNextPatient(reader);
            Assert.assertEquals("00000002", patient.getItemValue("patientIdNumber"));
            Assert.assertNull(layout.readNextPatient(reader));
        }

        //Test read all from file - calls all versions of readAllPatients, so all versions of this method are tested with this test
        patients = layout.readAllPatients(file, StandardCharsets.UTF_8.name(), options);
        Assert.assertEquals("00000001", patients.get(0).getItemValue("patientIdNumber"));
        Assert.assertEquals("00000002", patients.get(1).getItemValue("patientIdNumber"));
    }

    @Test
    public void testWriteMethods() throws IOException {
        NaaccrXmlLayout layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, null, false);
        File file = new File(TestingUtils.getBuildDirectory(), "test-xml-writer.xml");

        NaaccrOptions options = new NaaccrOptions();
        options.setUseStrictNamespaces(false);

        NaaccrData data = new NaaccrData();
        data.setBaseDictionaryUri("http://naaccr.org/naaccrxml/user-defined-naaccr-dictionary-160.xml");
        data.setRecordType("A");

        Patient patient = new Patient();
        patient.addItem(new Item("patientIdNumber", "00000001"));
        Tumor tumor = new Tumor();
        tumor.addItem(new Item("primarySite", "C123"));
        patient.addTumor(tumor);

        //Write single patients
        try (PatientXmlWriter writer = new PatientXmlWriter(new FileWriter(file), data)) {
            layout.writeNextPatient(writer, null);
            layout.writeNextPatient(writer, new Patient());
            layout.writeNextPatient(writer, patient);
        }
        List<Patient> patients = layout.readAllPatients(file, StandardCharsets.UTF_8.name(), options);
        Assert.assertEquals(2, patients.size());
        Assert.assertEquals(0, patients.get(0).getTumors().size());
        Assert.assertEquals(1, patients.get(1).getTumors().size());
        Assert.assertEquals("A", NaaccrXmlUtils.getAttributesFromXmlFile(file).get("recordType"));

        //Write all patients - method using "File" parameter calls all other versions of this method, so all versions are tested with this test
        //Null data - nothing should be written to the file
        layout.writeAllPatients(file, null, null, null);
        Assert.assertTrue(file.exists());
        Assert.assertEquals(0, file.length());

        //Null patient list - root data should still be written
        layout.writeAllPatients(file, null, data, options);
        Assert.assertEquals(0, layout.readAllPatients(file, StandardCharsets.UTF_8.name(), options).size());
        Assert.assertEquals("A", NaaccrXmlUtils.getAttributesFromXmlFile(file).get("recordType"));

        //Empty patient list - root data should still be written
        layout.writeAllPatients(file, new ArrayList<>(), data, options);
        Assert.assertEquals(0, layout.readAllPatients(file, StandardCharsets.UTF_8.name(), options).size());
        Assert.assertEquals("A", NaaccrXmlUtils.getAttributesFromXmlFile(file).get("recordType"));

        //Test empty and valid patients
        List<Patient> allPatients = new ArrayList<>();
        allPatients.add(new Patient());
        allPatients.add(patient);
        layout.writeAllPatients(file, allPatients, data, options);
        patients = layout.readAllPatients(file, StandardCharsets.UTF_8.name(), options);
        Assert.assertEquals(2, patients.size());
        Assert.assertEquals(0, patients.get(0).getTumors().size());
        Assert.assertEquals(1, patients.get(1).getTumors().size());
        Assert.assertEquals("A", NaaccrXmlUtils.getAttributesFromXmlFile(file).get("recordType"));
    }

    @Test
    public void testLayoutRegistering() {
        LayoutFactory.registerLayout(new NaaccrXmlLayout("160", "A", "test-id", "Test Name", null, null, true));

        NaaccrXmlLayout layout = (NaaccrXmlLayout)LayoutFactory.getLayout("test-id");

        Assert.assertEquals("test-id", layout.getLayoutId());
        Assert.assertEquals("Test Name", layout.getLayoutName());
        Assert.assertEquals("No description available", layout.getLayoutDescription());
        Assert.assertEquals("160", layout.getLayoutVersion());
        Assert.assertEquals(587, layout.getAllFields().size());
        Assert.assertNull(layout.getFieldDocByName("field1"));
        Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(10));
        Assert.assertNotNull(layout.getFieldDocDefaultCssStyle());
    }

    @Test
    public void testBuildFileInfo() throws IOException {
        String defaultUserDictionary = "http://naaccr.org/naaccrxml/user-defined-naaccr-dictionary-160.xml";

        //base layout - uses a base dictionary, no user dictionaries
        NaaccrXmlLayout layout = (NaaccrXmlLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT);

        // this exhaustive test was written with data files that don't use the correct XML namespaces; I don't feel like adding it to all the testing files
        // and so I am just turning that option OFF...
        LayoutInfoDiscoveryOptions options = new LayoutInfoDiscoveryOptions();
        options.setNaaccrXmlUseStrictNamespaces(false);

        //create user dictionaries and layouts with 1 or 2 user dictionaries
        NaaccrDictionary userDictionary1 = NaaccrXmlDictionaryUtils.readDictionary(new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/user-dictionary-1.xml"));
        NaaccrDictionary userDictionary2 = NaaccrXmlDictionaryUtils.readDictionary(new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/user-dictionary-2.xml"));
        NaaccrXmlLayout userLayoutOneDictionary = new NaaccrXmlLayout("160", "A", "testLayoutId", "testLayoutName", null, Collections.singletonList(userDictionary1), true);
        LayoutFactory.registerLayout(userLayoutOneDictionary);
        NaaccrXmlLayout userLayoutTwoDictionaries = new NaaccrXmlLayout("160", "A", "testLayoutId2", "testLayoutName2", null, Arrays.asList(userDictionary1, userDictionary2), true);
        LayoutFactory.registerLayout(userLayoutTwoDictionaries);

        //Null file returns null
        Assert.assertNull(layout.buildFileInfo(null, null, null));

        //Bad file path
        Assert.assertNull(layout.buildFileInfo(new File("Bad file path"), null, null));

        //Non-matching layout
        File file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-two-patients.xml");
        Assert.assertNull(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_INCIDENCE).buildFileInfo(file, null, options));

        //layout only uses base dictionary, file uses only the base dictionary
        LayoutInfo info = layout.buildFileInfo(file, null, options);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT, info.getLayoutId());
        Assert.assertEquals("NAACCR XML 16 Abstract", info.getLayoutName());
        Assert.assertNull(info.getLineLength());
        Assert.assertEquals(Collections.singletonList(defaultUserDictionary), info.getAvailableUserDictionaries());
        Assert.assertEquals(new ArrayList<>(), info.getRequestedUserDictionaries());
        Assert.assertNotNull(info.getRootNaaccrXmlData());
        Assert.assertNull(info.getErrorMessage());

        // namespace is missing
        options.setNaaccrXmlUseStrictNamespaces(true);
        info = layout.buildFileInfo(file, null, options);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT, info.getLayoutId());
        Assert.assertNull(info.getRootNaaccrXmlData());
        Assert.assertTrue(info.getErrorMessage().contains("namespace"));
        options.setNaaccrXmlUseStrictNamespaces(false);

        //layout only uses base dictionary, file uses same base dictionary and a user dictionary 
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-1.xml");
        info = layout.buildFileInfo(file, null, options);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT, info.getLayoutId());
        Assert.assertEquals("NAACCR XML 16 Abstract", info.getLayoutName());
        Assert.assertEquals(Collections.singletonList(defaultUserDictionary), info.getAvailableUserDictionaries());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getRequestedUserDictionaries());

        //layout uses one user dictionary, file uses same base dictionary and no user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-two-patients.xml");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertEquals(1, info.getAvailableUserDictionaries().size());
        Assert.assertEquals(new ArrayList<>(), info.getRequestedUserDictionaries());

        //layout uses one user dictionary, file uses same base dictionary and same user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-1.xml");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getAvailableUserDictionaries());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getRequestedUserDictionaries());

        //layout uses one user dictionary, file uses same base dictionary, same user dictionary and one other user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-2.xml");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getAvailableUserDictionaries());
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getRequestedUserDictionaries().size());

        //layout uses one user dictionary, file uses same base dictionary and a different user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-3.xml");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getAvailableUserDictionaries());
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(1, info.getRequestedUserDictionaries().size());

        //layout uses base dictionary and one user dictionary, file uses same user dictionary but different base ditionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-4.xml");
        Assert.assertNull(userLayoutOneDictionary.buildFileInfo(file, null, options));

        //layout uses two user dictionaries, file uses same base dictionary and no user dictionaries
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-two-patients.xml");
        info = userLayoutTwoDictionaries.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutTwoDictionaries.getLayoutId(), info.getLayoutId());
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getAvailableUserDictionaries().size());
        Assert.assertEquals(new ArrayList<>(), info.getRequestedUserDictionaries());

        //layout uses two user dictionaries, file uses same base dictionary and one user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-1.xml");
        info = userLayoutTwoDictionaries.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutTwoDictionaries.getLayoutId(), info.getLayoutId());
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getAvailableUserDictionaries().size());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getRequestedUserDictionaries());

        //layout uses two user dictionaries, file uses same base dictionary and both user dictionaries
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-2.xml");
        info = userLayoutTwoDictionaries.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutTwoDictionaries.getLayoutId(), info.getLayoutId());
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getAvailableUserDictionaries().size());
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getRequestedUserDictionaries().size());

        //layout uses two user dictionaries, file uses both user dictionaries but different base dictionary        
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-5.xml");
        Assert.assertNull(userLayoutTwoDictionaries.buildFileInfo(file, null, options));

        //Test a GZ file - layout uses only base dictionary, file uses only base dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-two-patients.xml.gz");
        info = layout.buildFileInfo(file, null, options);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT, info.getLayoutId());
        Assert.assertEquals("NAACCR XML 16 Abstract", info.getLayoutName());
        Assert.assertEquals(Collections.singletonList(defaultUserDictionary), info.getAvailableUserDictionaries());
        Assert.assertEquals(new ArrayList<>(), info.getRequestedUserDictionaries());

        //Test a GZ file - layout uses only base dictionary, file uses one user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-1.xml.gz");
        info = layout.buildFileInfo(file, null, options);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT, info.getLayoutId());
        Assert.assertEquals("NAACCR XML 16 Abstract", info.getLayoutName());
        Assert.assertEquals(Collections.singletonList(defaultUserDictionary), info.getAvailableUserDictionaries());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getRequestedUserDictionaries());

        //layout uses one user dictionaries, GZ file uses same base dictionary and same user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-1.xml.gz");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getAvailableUserDictionaries());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getRequestedUserDictionaries());

        //layout uses one user dictionaries, GZ file uses same base dictionary and no user dictionaries
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-two-patients.xml.gz");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getAvailableUserDictionaries());
        Assert.assertEquals(new ArrayList<>(), info.getRequestedUserDictionaries());

        //layout uses one user dictionaries, GZ file uses same base dictionary and different user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-3.xml.gz");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getAvailableUserDictionaries());
        Assert.assertEquals(Collections.singletonList(userDictionary2.getDictionaryUri()), info.getRequestedUserDictionaries());

        //layout uses one user dictionaries, GZ file uses same base dictionary, same user dictionary and one other user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-2.xml.gz");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getAvailableUserDictionaries());
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getRequestedUserDictionaries().size());

        //layout uses two user dictionaries, GZ file uses same base dictionary and both dictionaries
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-2.xml.gz");
        info = userLayoutTwoDictionaries.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutTwoDictionaries.getLayoutId(), info.getLayoutId());
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getAvailableUserDictionaries().size());
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getRequestedUserDictionaries().size());

        //layout uses two user dictionaries, GZ file uses same base dictionary and one user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-1.xml.gz");
        info = userLayoutTwoDictionaries.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutTwoDictionaries.getLayoutId(), info.getLayoutId());
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getAvailableUserDictionaries().size());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getRequestedUserDictionaries());

        //Test a zip file, one entry. zipEntryName null - layout should still be detected
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-two-patients.zip");
        info = layout.buildFileInfo(file, null, options);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT, info.getLayoutId());
        Assert.assertEquals("NAACCR XML 16 Abstract", info.getLayoutName());

        //Zip file, one entry. zipEntryName provided - layout should be detected
        info = layout.buildFileInfo(file, "xml-reader-two-patients.xml", options);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT, info.getLayoutId());
        Assert.assertEquals("NAACCR XML 16 Abstract", info.getLayoutName());

        //Zip file, two entries
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/multiple-xml-files.zip");
        info = layout.buildFileInfo(file, "fake-naaccr-16-abstract.xml", options);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT, info.getLayoutId());
        Assert.assertEquals("NAACCR XML 16 Abstract", info.getLayoutName());

        //Zip file, two entries, no zipEntryName
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/multiple-xml-files.zip");
        Assert.assertNull(layout.buildFileInfo(file, null, options));

        //layout only uses base dictionary, zip file uses user user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-1.zip");
        info = layout.buildFileInfo(file, null, options);
        Assert.assertEquals(layout.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(defaultUserDictionary), info.getAvailableUserDictionaries());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getRequestedUserDictionaries());

        //layout uses one user dictionary, zip file uses only base dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-two-patients.zip");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getAvailableUserDictionaries());
        Assert.assertEquals(new ArrayList<>(), info.getRequestedUserDictionaries());

        //layout uses one user dictionary, zip file uses same user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-1.zip");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getAvailableUserDictionaries());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getRequestedUserDictionaries());

        //layout uses one user dictionary, zip file uses different user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-3.zip");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getAvailableUserDictionaries());
        Assert.assertEquals(Collections.singletonList(userDictionary2.getDictionaryUri()), info.getRequestedUserDictionaries());

        //layout uses one user dictionary, zip file uses same user dictionary and another user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-2.zip");
        info = userLayoutOneDictionary.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutOneDictionary.getLayoutId(), info.getLayoutId());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getAvailableUserDictionaries());
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getRequestedUserDictionaries().size());

        //layout uses two user dictionaries, zip files uses only base dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-two-patients.zip");
        info = userLayoutTwoDictionaries.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutTwoDictionaries.getLayoutId(), info.getLayoutId());
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getAvailableUserDictionaries().size());
        Assert.assertEquals(new ArrayList<>(), info.getRequestedUserDictionaries());

        //layout uses two user dictionaries, zip files uses one user dictionary
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-1.zip");
        info = userLayoutTwoDictionaries.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutTwoDictionaries.getLayoutId(), info.getLayoutId());
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getAvailableUserDictionaries().size());
        Assert.assertEquals(Collections.singletonList(userDictionary1.getDictionaryUri()), info.getRequestedUserDictionaries());

        //layout uses two user dictionaries, zip files uses both user dictionaries
        file = new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/xml/xml-reader-user-dict-2.zip");
        info = userLayoutTwoDictionaries.buildFileInfo(file, null, options);
        Assert.assertEquals(userLayoutTwoDictionaries.getLayoutId(), info.getLayoutId());
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getAvailableUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getAvailableUserDictionaries().size());
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary1.getDictionaryUri()));
        Assert.assertTrue(info.getRequestedUserDictionaries().contains(userDictionary2.getDictionaryUri()));
        Assert.assertEquals(2, info.getRequestedUserDictionaries().size());
    }
}
