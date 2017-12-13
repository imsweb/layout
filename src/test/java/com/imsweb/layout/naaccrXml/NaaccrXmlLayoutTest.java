/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.naaccrXml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.naaccrxml.NaaccrIOException;
import com.imsweb.naaccrxml.NaaccrOptions;
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
    public void testNaaccrXmlLayout() {
        NaaccrXmlLayout layout;

        //Null NAACCR version
        boolean exceptionCaught = false;
        try {
            new NaaccrXmlLayout(null, "A", "test-id", "test-name", null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Null recordType
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", null, "test-id", "test-name", null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Null ID
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "A", null, "test-name", null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Null name
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "A", "test-id", null, null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Bad version number
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("1160", "A", "test-id", "test-name", null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Bad recordType
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "Bad", "test-id", "test-name", null, false);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Bad user dictionary - duplicate ID
        NaaccrDictionary badDictionary = new NaaccrDictionary();
        badDictionary.setDictionaryUri("http://mycompany.org/my-very-own-naaccr-dictionary.xml");
        badDictionary.setSpecificationVersion("1.3");
        NaaccrDictionaryItem duplicateItem = new NaaccrDictionaryItem();
        duplicateItem.setNaaccrId("recordType");
        duplicateItem.setNaaccrName("Record Type");
        duplicateItem.setNaaccrNum(10);
        duplicateItem.setLength(1);
        duplicateItem.setParentXmlElement("Tumor");
        badDictionary.setItems(Collections.singletonList(duplicateItem));
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "A", "test-id", "test-name", Collections.singletonList(badDictionary), true);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Bad user dictionary - duplicate Name
        duplicateItem.setNaaccrId("itemId");
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "A", "test-id", "test-name", Collections.singletonList(badDictionary), true);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Bad user dictionary - duplicate NAACCR Number
        duplicateItem.setNaaccrName("Item name");
        exceptionCaught = false;
        try {
            new NaaccrXmlLayout("160", "A", "test-id", "test-name", Collections.singletonList(badDictionary), true);
        }
        catch (RuntimeException e) {
            exceptionCaught = true;
        }
        Assert.assertTrue(exceptionCaught);

        //Test Version 160
        layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, false);
        Assert.assertEquals(new ArrayList<>(), layout.getAllFields());
        Assert.assertEquals("160", layout.getLayoutVersion());
        Assert.assertEquals("test-id", layout.getLayoutId());
        Assert.assertEquals("test-name", layout.getLayoutName());
        Assert.assertEquals("A", layout.getRecordType());
        Assert.assertEquals("160", layout.getBaseDictionary().getNaaccrVersion());
        Assert.assertEquals(1, layout.getUserDictionaries().size());
        Assert.assertEquals("160", layout.getUserDictionaries().get(0).getNaaccrVersion());
        Assert.assertNull(layout.getFieldByNaaccrItemNumber(10));
        Assert.assertNull(layout.getFieldByName("recordType"));
        layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, true);
        Assert.assertEquals(587, layout.getAllFields().size());
        Assert.assertNotNull(layout.getFieldByNaaccrItemNumber(10));
        Assert.assertNotNull(layout.getFieldByName("recordType"));
        Assert.assertNotNull(layout.getFieldByNaaccrItemNumber(37));
        Assert.assertNotNull(layout.getFieldByName("reserved00"));

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
        userDictionary.setItems(Collections.singletonList(item));

        layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", Collections.singletonList(userDictionary), true);
        Assert.assertEquals(565, layout.getAllFields().size());
        Assert.assertNotNull(layout.getFieldByNaaccrItemNumber(10003));
        Assert.assertNotNull(layout.getFieldByName("itemId"));
        Assert.assertEquals(1, layout.getUserDictionaries().size());

        layout = new NaaccrXmlLayout("160", "M", "test-id", "test-name", null, true);
        Assert.assertEquals("M", layout.getRecordType());
        Assert.assertEquals(587, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("160", "C", "test-id", "test-name", null, true);
        Assert.assertEquals("C", layout.getRecordType());
        Assert.assertEquals(568, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("160", "I", "test-id", "test-name", null, true);
        Assert.assertEquals("I", layout.getRecordType());
        Assert.assertEquals(496, layout.getAllFields().size());

        //Test version 150
        layout = new NaaccrXmlLayout("150", "A", "test-id", "test-name", null, true);
        Assert.assertEquals("150", layout.getLayoutVersion());
        Assert.assertEquals("150", layout.getBaseDictionary().getNaaccrVersion());
        Assert.assertEquals("A", layout.getRecordType());
        Assert.assertEquals(1, layout.getUserDictionaries().size());
        Assert.assertEquals("150", layout.getUserDictionaries().get(0).getNaaccrVersion());
        Assert.assertEquals(555, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("150", "M", "test-id", "test-name", null, true);
        Assert.assertEquals("M", layout.getRecordType());
        Assert.assertEquals(555, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("150", "C", "test-id", "test-name", null, true);
        Assert.assertEquals("C", layout.getRecordType());
        Assert.assertEquals(536, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("150", "I", "test-id", "test-name", null, true);
        Assert.assertEquals("I", layout.getRecordType());
        Assert.assertEquals(464, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("140", "A", "test-id", "test-name", null, true);
        Assert.assertEquals("140", layout.getLayoutVersion());
        Assert.assertEquals("140", layout.getBaseDictionary().getNaaccrVersion());
        Assert.assertEquals("A", layout.getRecordType());
        Assert.assertEquals(1, layout.getUserDictionaries().size());
        Assert.assertEquals("140", layout.getUserDictionaries().get(0).getNaaccrVersion());
        Assert.assertEquals(548, layout.getAllFields().size());
        Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(10));

        layout = new NaaccrXmlLayout("140", "M", "test-id", "test-name", null, true);
        Assert.assertEquals("M", layout.getRecordType());
        Assert.assertEquals(548, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("140", "C", "test-id", "test-name", null, true);
        Assert.assertEquals("C", layout.getRecordType());
        Assert.assertEquals(529, layout.getAllFields().size());

        layout = new NaaccrXmlLayout("140", "I", "test-id", "test-name", null, true);
        Assert.assertEquals("I", layout.getRecordType());
        Assert.assertEquals(457, layout.getAllFields().size());
    }

    @Test
    public void testLayoutDocumentation() {
        NaaccrXmlLayout layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, true);

        for (NaaccrXmlField field : layout.getAllFields()) {
            Assert.assertNotNull(layout.getFieldDocByName(field.getName()));
            Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum()));
        }

        Assert.assertNull(layout.getFieldDocByNaaccrItemNumber(123456));
        Assert.assertNull(layout.getFieldDocByName("A fake field name"));

        layout = new NaaccrXmlLayout("150", "A", "test-id", "test-name", null, true);

        for (NaaccrXmlField field : layout.getAllFields()) {
            //The v15 dictionary contains "reserved" fields, but the v15 layout does not
            if (!field.getNaaccrId().startsWith("reserved")) {
                Assert.assertNotNull(layout.getFieldDocByName(field.getName()));
                Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum()));
            }
        }

        layout = new NaaccrXmlLayout("140", "A", "test-id", "test-name", null, true);
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
        NaaccrXmlLayout layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, false);
        File file = new File(System.getProperty("user.dir") + "/src/test/resources/xml-reader-two-patients.xml");
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
        patients = layout.readAllPatients(file, null);
        Assert.assertEquals("00000001", patients.get(0).getItemValue("patientIdNumber"));
        Assert.assertEquals("00000002", patients.get(1).getItemValue("patientIdNumber"));
    }

    @Test
    public void testWriteMethods() throws IOException {
        NaaccrXmlLayout layout = new NaaccrXmlLayout("160", "A", "test-id", "test-name", null, false);
        File file = new File(System.getProperty("user.dir") + "/build/test-xml-writer.xml");

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
        List<Patient> patients = layout.readAllPatients(file, null);
        Assert.assertEquals(2, patients.size());
        Assert.assertEquals(0, patients.get(0).getTumors().size());
        Assert.assertEquals(1, patients.get(1).getTumors().size());
        Assert.assertEquals("A", NaaccrXmlUtils.getAttributesFromXmlFile(file).get("recordType"));

        //Write all patients - method using "File" parameter calls all other versions of this method, so all versions are tested with this test
        //Null data - nothing should be written to the file
        layout.writeAllPatients(file, null, null);
        Assert.assertTrue(file.exists());
        Assert.assertEquals(0, file.length());

        //Null patient list - root data should still be written
        layout.writeAllPatients(file, null, data);
        Assert.assertEquals(0, layout.readAllPatients(file, null).size());
        Assert.assertEquals("A", NaaccrXmlUtils.getAttributesFromXmlFile(file).get("recordType"));

        //Empty patient list - root data should still be written
        layout.writeAllPatients(file, new ArrayList<>(), data);
        Assert.assertEquals(0, layout.readAllPatients(file, null).size());
        Assert.assertEquals("A", NaaccrXmlUtils.getAttributesFromXmlFile(file).get("recordType"));

        //Test empty and valid patients
        List<Patient> allPatients = new ArrayList<>();
        allPatients.add(new Patient());
        allPatients.add(patient);
        layout.writeAllPatients(file, allPatients, data);
        patients = layout.readAllPatients(file, null);
        Assert.assertEquals(2, patients.size());
        Assert.assertEquals(0, patients.get(0).getTumors().size());
        Assert.assertEquals(1, patients.get(1).getTumors().size());
        Assert.assertEquals("A", NaaccrXmlUtils.getAttributesFromXmlFile(file).get("recordType"));
    }

    @Test
    public void testLayoutRegistering() {
        LayoutFactory.registerLayout(new NaaccrXmlLayout("160", "A", "test-id", "Test Name", null, true));

        NaaccrXmlLayout layout = (NaaccrXmlLayout)LayoutFactory.getLayout("test-id");

        Assert.assertEquals("test-id", layout.getLayoutId());
        Assert.assertEquals("Test Name", layout.getLayoutName());
        Assert.assertTrue(layout.getLayoutDescription().startsWith("This layout uses the base dictionary:"));
        Assert.assertEquals("160", layout.getLayoutVersion());
        Assert.assertEquals(587, layout.getAllFields().size());
        Assert.assertNull(layout.getFieldDocByName("field1"));
        Assert.assertNotNull(layout.getFieldDocByNaaccrItemNumber(10));
        Assert.assertNotNull(layout.getFieldDocDefaultCssStyle());
    }

    @Test
    public void testBuildFileInfo() throws NaaccrIOException, FileNotFoundException {
        File file = new File(System.getProperty("user.dir") + "/src/test/resources/xml-reader-two-patients.xml");

        NaaccrXmlLayout layout = (NaaccrXmlLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT);

        //Null file returns null
        Assert.assertNull(layout.buildFileInfo(null, null, null));

        //Bad file path
        Assert.assertNull(layout.buildFileInfo(new File("Bad file path"), null, null));

        //Builds file info for an actual file
        LayoutInfo info = layout.buildFileInfo(file, null, new LayoutInfoDiscoveryOptions());
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT, info.getLayoutId());
        Assert.assertEquals("NAACCR XML 16 Abstract", info.getLayoutName());
        Assert.assertNull(info.getLineLength());

        //Test a zip file
        file = new File(System.getProperty("user.dir") + "/src/test/resources/xml-reader-two-patients.xml.gz");
        info = layout.buildFileInfo(file, null, new LayoutInfoDiscoveryOptions());
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_XML_16_ABSTRACT, info.getLayoutId());
        Assert.assertEquals("NAACCR XML 16 Abstract", info.getLayoutName());
    }
}
