/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.naaccrxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.record.fixed.naaccr.NaaccrLayout;
import com.imsweb.naaccrxml.NaaccrFormat;
import com.imsweb.naaccrxml.NaaccrIOException;
import com.imsweb.naaccrxml.NaaccrOptions;
import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.NaaccrXmlUtils;
import com.imsweb.naaccrxml.PatientXmlReader;
import com.imsweb.naaccrxml.PatientXmlWriter;
import com.imsweb.naaccrxml.entity.NaaccrData;
import com.imsweb.naaccrxml.entity.Patient;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;

/**
 * This class contains the logic related to all NAACCR XML layouts
 */
public class NaaccrXmlLayout implements Layout {

    private NaaccrLayout _flatLayout;

    private String _layoutId;

    private String _layoutName;

    private String _naaccrVersion;

    private String _layoutDesc;

    private String _recordType;

    private List<NaaccrXmlField> _allFields;

    private Map<String, NaaccrXmlField> _fieldsCachedByName = new HashMap<>();
    private Map<Integer, NaaccrXmlField> _fieldsCachedByNaaccrNumber = new HashMap<>();

    private NaaccrDictionary _baseDictionary;
    private List<NaaccrDictionary> _userDictionaries;

    /**
     * @param naaccrVersion String of three digit NAACCR version
     * @param recordType String - allowed values are "A", "M", "C", and "I"
     * @param layoutId String used as layout ID - must be unique from those registered in Layout Factory
     * @param layoutName String used as layout name
     * @param dictionaries List (of type NaaccrDictionary) of custom user dictionaries - used for making custom layouts
     * @param loadFields determines whether to load all the fields from all dicitonaries into the allFields variable
     */
    public NaaccrXmlLayout(String naaccrVersion, String recordType, String layoutId, String layoutName, List<NaaccrDictionary> dictionaries, boolean loadFields) {
        _naaccrVersion = naaccrVersion;
        _layoutName = layoutName;
        _layoutId = layoutId;
        _recordType = recordType;
        _userDictionaries = dictionaries;

        //Only load dictionaries/fields if specified, otherwise avoid expensive operations
        if (loadFields) {
            //Load base dictionary from version
            _baseDictionary = NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion(naaccrVersion);

            //If user dictionaries weren't provided, use default user dictionary
            if (_userDictionaries == null || _userDictionaries.isEmpty())
                _userDictionaries = Collections.singletonList(NaaccrXmlDictionaryUtils.getDefaultUserDictionaryByVersion(naaccrVersion));

            //add description from base and user dictionaries
            StringBuilder description = new StringBuilder("This layout uses the base dictionary version " + _naaccrVersion);
            description.append(". This layout also uses ").append(_userDictionaries.size()).append(" user dictionaries with URI's ending in: ");
            for (NaaccrDictionary dictionary : _userDictionaries) {
                String uri = dictionary.getDictionaryUri();
                if (uri.length() > 20)
                    uri = uri.substring(uri.length() - 20);
                description.append(uri).append(", ");
            }
            description.setLength(description.length() - 2);
            _layoutDesc = description.toString();

            //Load the flat file layout corresponding to the base dictionary and record type for this layout
            StringBuilder flatFileId = new StringBuilder("naaccr-");
            flatFileId.append(_naaccrVersion.substring(0, 2)).append("-");
            switch (_recordType) {
                case "M":
                    flatFileId.append("modified");
                    break;
                case "C":
                    flatFileId.append("confidential");
                    break;
                case "I":
                    flatFileId.append("incidence");
                    break;
                default:
                    flatFileId.append("abstract");
                    break;
            }
            _flatLayout = (NaaccrLayout)LayoutFactory.getLayout(flatFileId.toString());
            if (_flatLayout == null)
                throw new RuntimeException("Could not find corresponding flat layout.");

            // TODO FD for now we don't handle "groups" (so subfields); should we?
            //Get all item definitions, create fields and add to layout's field list
            NaaccrDictionary allItemsDictionary = NaaccrXmlDictionaryUtils.mergeDictionaries(_baseDictionary, _userDictionaries.toArray(new NaaccrDictionary[_userDictionaries.size()]));
            _allFields = new ArrayList<>();
            for (NaaccrDictionaryItem item : allItemsDictionary.getItems()) {
                if (item.getRecordTypes() == null || item.getRecordTypes().isEmpty() || item.getRecordTypes().contains(_recordType)) {
                    NaaccrXmlField field = new NaaccrXmlField(item);
                    _allFields.add(field);
                    _fieldsCachedByNaaccrNumber.put(field.getNaaccrItemNum(), field);
                    _fieldsCachedByName.put(field.getNaaccrId(), field);
                }
            }
        }
        verify();
    }

    /**
     * Verify the internal fields, throws a runtime exception if something is wrong.
     */
    public void verify() {
        // ID is required
        if (_layoutId == null || _layoutId.isEmpty())
            throw new RuntimeException("Layout ID is required");

        // name is required
        if (_layoutName == null || _layoutName.isEmpty())
            throw new RuntimeException("Layout name is required");

        //Record type is required and must be one of the following: A, M, C, I
        if (_recordType == null || _recordType.isEmpty())
            throw new RuntimeException("Record type is required");
        else if (!_recordType.equals("A") && !_recordType.equals("M") && !_recordType.equals("C") && !_recordType.equals("I")) {
            throw new RuntimeException("Record type not recognized: " + _recordType);
        }

        if (_naaccrVersion == null || !NaaccrFormat.isVersionSupported(_naaccrVersion))
            throw new RuntimeException("Unsupported NAACCR version: " + _naaccrVersion);

        //If fields/dictionaries were loaded, _allFields is not null and these should be checked. If they were not loaded, _allFields is null
        if (_allFields != null) {
            //Base dictionary is required
            if (_baseDictionary == null)
                throw new RuntimeException("Base Dictionary is required");

            //Validate the user dictionaries
            for (NaaccrDictionary userDictionary : _userDictionaries) {
                String error = NaaccrXmlDictionaryUtils.validateUserDictionary(userDictionary);
                if (error != null)
                    throw new RuntimeException("Error found on user dictionary - " + error);
            }

            //validate the NaaccrXmlFields
            Set<String> names = new HashSet<>(), naaccrItemNums = new HashSet<>(), shortLabels = new HashSet<>(), longLabels = new HashSet<>();
            for (NaaccrXmlField field : _allFields) {
                if (field.getName() == null)
                    throw new RuntimeException("Field name is required");
                if (names.contains(field.getName()))
                    throw new RuntimeException("Field name must be unique, found duplicate name for '" + field.getName() + "'");
                names.add(field.getName());
                if (field.getItem() == null)
                    throw new RuntimeException("Field item definition is required, missing for field " + field.getName());
                if (shortLabels.contains(field.getShortLabel()))
                    throw new RuntimeException("Field short labels must be unique, found duplicate name for '" + field.getShortLabel() + "'");
                shortLabels.add(field.getShortLabel());
                if (longLabels.contains(field.getLongLabel()))
                    throw new RuntimeException("Field long labels must be unique, found duplicate name for '" + field.getLongLabel() + "'");
                longLabels.add(field.getLongLabel());
                if (field.getNaaccrItemNum() != null)
                    if (naaccrItemNums.contains(field.getNaaccrItemNum().toString()))
                        throw new RuntimeException("Field NAACCR item number must be unique, found duplicate number for '" + field.getNaaccrItemNum() + "'");
                naaccrItemNums.add(field.getNaaccrItemNum().toString());
                if (field.getLength() == null)
                    throw new RuntimeException("Field length is required, missing for field " + field.getName());
                if (field.getParentXmlElement() == null)
                    throw new RuntimeException("Field parent XML element is required, missing for field " + field.getName());
            }
        }
    }

    //Writers and readers

    /**
     * Writes a single patient using a provided writer. Does not open or close the writer.
     * @param writer The PatientXmlWriter used to write Patients to an XML file
     * @param patient The patient that is to be written
     * @throws NaaccrIOException
     */
    public void writeNextPatient(PatientXmlWriter writer, Patient patient) throws NaaccrIOException {
        if (patient != null)
            writer.writePatient(patient);
    }

    /**
     * Opens a FileOutputStream to write a list of patients to a file. Passes the stream to the method below.
     * The first step of this process is to write the root data to the file, so this method can only be used to write a complete
     * list of patients (it cannot write a "partial" list that can be added to later).
     * The OutputStream is opened and closed in this method.
     * @param file File that the patients will be written to
     * @param allPatients List of all patients that will be written to the file.
     * @param data The root data, to be written at the top of the file.
     * @throws IOException
     */
    public void writeAllPatients(File file, List<Patient> allPatients, NaaccrData data) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file)) {
            writeAllPatients(os, allPatients, data);
        }
    }

    /**
     * Starts a PatientXmlWriter to write a list of patients to an OutputStream. Passes the writer to the method below.
     * The first step of this process is to write the root data to the stream, so this method can only be used to write a complete
     * list of patients (it cannot write a "partial" list that can be added to later).
     * This method does not open or close the OutputStream.
     * The writer is opened and closed in this method.
     * @param outputStream The OutputStream that the writer will write to
     * @param patients List of Patients to be written
     * @param data Root data needed to create a PatientXmlWriter - is written as part of the writer's construction
     * @throws NaaccrIOException
     */
    public void writeAllPatients(OutputStream outputStream, List<Patient> patients, NaaccrData data) throws NaaccrIOException {
        if (data == null)
            return;

        PatientXmlWriter writer = null;
        try {
            writer = new PatientXmlWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), data);
            writeAllPatients(writer, patients);
        }
        finally {
            if (writer != null)
                writer.closeAndKeepAlive();
        }
    }

    /**
     * Used to write a list of patients using a PatientXmlWriter. Can be multiple times to write to the same file.
     * This method does not open or close the writer.
     * @param writer The PatientXmlWriter used to write Patients to an XML file
     * @param patients List of Patients to be written
     * @throws NaaccrIOException
     */
    public void writeAllPatients(PatientXmlWriter writer, List<Patient> patients) throws NaaccrIOException {
        if (patients != null)
            for (Patient patient : patients)
                writer.writePatient(patient);
    }

    /**
     * Used to read single patients from an XML file using a PatientXmlReader
     * @param reader PatientXmlReader used to read Patients from an XML file.
     * @return returns the next Patient found by the reader
     * @throws NaaccrIOException
     */
    public Patient readNextPatient(PatientXmlReader reader) throws NaaccrIOException {
        return reader.readPatient();
    }

    /**
     * Opens a FileInputStream to read all the Patients from an XML file. Passes the stream to the method below.
     * The method opens and closes the InputStream.
     * @param file XML file that Patients will be read from.
     * @param encoding The encoding of the file (null means default OS encoding)
     * @return List of all Patients in the file.
     * @throws IOException
     */
    public List<Patient> readAllPatients(File file, String encoding) throws IOException {
        List<Patient> patients;

        try (FileInputStream is = new FileInputStream(file)) {
            patients = readAllPatients(is, encoding);
        }
        return patients;
    }

    /**
     * Creates a PatientXmlReader from the InputStream parameter to read all Patients from an XML file. Passes the writer to the method below.
     * This method opens and closes the reader.
     * This method does not open or close the InputStream.
     * @param inputStream InputStream used to read Patients
     * @param encoding Stream encoding (null means default OS encoding)
     * @return List of all Patients read from the stream.
     * @throws NaaccrIOException
     * @throws UnsupportedEncodingException
     */
    public List<Patient> readAllPatients(InputStream inputStream, String encoding) throws NaaccrIOException, UnsupportedEncodingException {
        NaaccrOptions options = new NaaccrOptions();
        options.setUseStrictNamespaces(false);

        List<Patient> patients;
        PatientXmlReader reader = null;
        try {
            if (encoding == null)
                reader = new PatientXmlReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), options);
            else
                reader = new PatientXmlReader(new InputStreamReader(inputStream, encoding), options);
            patients = readAllPatients(reader);
        }
        finally {
            if (reader != null)
                reader.closeAndKeepAlive();
        }
        return patients;
    }

    /**
     * Reads all Patients found by the reader.
     * This method does not open or close the reader.
     * @param reader PatientXmlReader used to read Patients from an XML file
     * @return List of all the Patients found by the reader.
     * @throws NaaccrIOException
     */
    public List<Patient> readAllPatients(PatientXmlReader reader) throws NaaccrIOException {
        List<Patient> allPatients = new ArrayList<>();
        Patient patient;
        while ((patient = reader.readPatient()) != null)
            allPatients.add(patient);

        return allPatients;
    }

    //Getters
    public NaaccrDictionary getBaseDictionary() {
        return _baseDictionary;
    }

    public List<NaaccrDictionary> getUserDictionaries() {
        return _userDictionaries;
    }

    public String getRecordType() {
        return _recordType;
    }

    @Override
    public String getLayoutId() {
        return _layoutId;
    }

    @Override
    public String getLayoutName() {
        return _layoutName;
    }

    @Override
    public String getLayoutVersion() {
        return _naaccrVersion;
    }

    @Override
    public String getLayoutDescription() {
        return _layoutDesc;
    }

    @Override
    public NaaccrXmlField getFieldByName(String name) {
        return _fieldsCachedByName.get(name);
    }

    @Override
    public NaaccrXmlField getFieldByNaaccrItemNumber(Integer num) {
        return _fieldsCachedByNaaccrNumber.get(num);
    }

    @Override
    public List<NaaccrXmlField> getAllFields() {
        return _allFields;
    }

    /**
     * Gets the field corresponding to the parameter and uses the NAACCR Item Number to call the method below
     * @param name Field name
     * @return Field doc for provided field. Null if field is not found.
     */
    @Override
    public String getFieldDocByName(String name) {
        NaaccrXmlField field = getFieldByName(name);
        if (field == null)
            return null;

        return getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum());
    }

    /**
     * Calls the same method in the corresponding flat layout
     * NAACCR XML IDs don't align with field doc names, so field docs must be obtained using the Item Numbers and the flat layout.
     * @param num NAACCR item number
     * @return Field Documentation for field corresponding to the given number. Null if field is not found.
     */
    @Override
    public String getFieldDocByNaaccrItemNumber(Integer num) {
        //NAACCR XML ID's don't all align with fixed column ID's/field doc names - need to use number to get doc through fixed column layout
        NaaccrXmlField field = getFieldByNaaccrItemNumber(num);
        if (field == null)
            return null;

        return _flatLayout.getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum());
    }

    /**
     * Uses corresponding flat layout to call the same method
     * @return String of CSS Style for field docs
     */
    @Override
    public String getFieldDocDefaultCssStyle() {
        return _flatLayout.getFieldDocDefaultCssStyle();
    }

    /**
     * Compares the record type and dictionaries to determine if this layout can read the input file
     * @param file data file
     * @param zipEntryName optional zip entry to use if the file is a zip file, not used if the file is not a zip file
     * @param options discovery options
     * @return LayoutInfo containing this layout's ID and name. Null if this layout cannot be used to read this file.
     */
    @Override
    public LayoutInfo buildFileInfo(File file, String zipEntryName, LayoutInfoDiscoveryOptions options) {
        if (file == null)
            return null;

        LayoutInfo info = new LayoutInfo();
        Map<String, String> attributes = NaaccrXmlUtils.getAttributesFromXmlFile(file);

        String recordType = attributes.get(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_REC_TYPE);
        if (recordType == null || recordType.isEmpty() || !_recordType.equals(recordType))
            return null;

        String baseUri = NaaccrXmlDictionaryUtils.createUriFromVersion(_naaccrVersion, true);
        if (!baseUri.equals(attributes.get(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_BASE_DICT)))
            return null;

        // Checking to see if the file uses custom dictionaries, and if this layout can access those dictionaries
        List<String> layoutDictionaryUris = new ArrayList<>();
        for (NaaccrDictionary dictionary : _userDictionaries)
            layoutDictionaryUris.add(dictionary.getDictionaryUri());

        if (attributes.get(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_USER_DICT) != null &&
                !layoutDictionaryUris.containsAll(Arrays.asList(attributes.get(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_USER_DICT).split(" "))))
            return null;

        info.setLayoutId(_layoutId);
        info.setLayoutName(_layoutName);
        return info;
    }
}
