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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.imsweb.layout.Field;
import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.LayoutUtils;
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

import static com.imsweb.naaccrxml.NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_BASE_DICT;
import static com.imsweb.naaccrxml.NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_REC_TYPE;
import static com.imsweb.naaccrxml.NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_USER_DICT;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class contains the logic related to all NAACCR XML layouts
 */
public class NaaccrXmlLayout implements Layout {

    private NaaccrLayout _flatLayout;

    // layout ID
    protected String _layoutId;

    // layout Name
    protected String _layoutName;

    // layout Description
    protected String _layoutDesc;

    // layout version (for this layout type, it's the NAACCR version)
    protected String _naaccrVersion;

    // record type supported by this layout (A, M, C or I)
    private String _recordType;

    // the base dictionary for this layout
    private NaaccrDictionary _baseDictionary;

    // the user-defined dictionaries for this layout
    private List<NaaccrDictionary> _userDictionaries;

    // the fields for this layout
    private List<NaaccrXmlField> _allFields = new ArrayList<>();

    // fields cache for quick access by name (which is NAACCR ID for this layout)
    private Map<String, NaaccrXmlField> _fieldsCachedByName = new HashMap<>();

    // fields cache for quick access by NAACCR number
    private Map<Integer, NaaccrXmlField> _fieldsCachedByNaaccrNumber = new HashMap<>();

    /**
     * Default constructor.
     */
    public NaaccrXmlLayout() {
    }

    /**
     * Constructor.
     * @param naaccrVersion String of three digit NAACCR version
     * @param recordType String - allowed values are "A", "M", "C", and "I"
     * @param layoutId String used as layout ID - must be unique from those registered in Layout Factory
     * @param layoutName String used as layout name
     * @param dictionaries List (of type NaaccrDictionary) of custom user dictionaries - used for making custom layouts
     * @param loadFields determines whether to load all the fields from all dictionaries into the allFields variable
     */
    public NaaccrXmlLayout(String naaccrVersion, String recordType, String layoutId, String layoutName, String description, List<NaaccrDictionary> dictionaries, boolean loadFields) {
        _naaccrVersion = naaccrVersion;
        _layoutName = layoutName;
        _layoutId = layoutId;
        _recordType = recordType;
        _baseDictionary = NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion(naaccrVersion);
        _userDictionaries = dictionaries;
        if (_userDictionaries == null || _userDictionaries.isEmpty())
            _userDictionaries = Collections.singletonList(NaaccrXmlDictionaryUtils.getDefaultUserDictionaryByVersion(naaccrVersion));
        _layoutDesc = StringUtils.isBlank(description) ? "No description available" : description;

        //Only load dictionaries/fields if specified, otherwise avoid expensive operations
        if (loadFields) {

            //Load the flat file layout corresponding to the base dictionary and record type for this layout
            StringBuilder flatFileId = new StringBuilder("naaccr-");
            flatFileId.append(_naaccrVersion, 0, 2).append("-");
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
                throw new RuntimeException("Could not find corresponding flat layout '" + flatFileId + "'");

            //Get all item definitions, create fields and add to layout's field list
            for (NaaccrDictionaryItem item : NaaccrXmlDictionaryUtils.mergeDictionaries(_baseDictionary, _userDictionaries.toArray(new NaaccrDictionary[0])).getItems()) {
                if (item.getRecordTypes() == null || item.getRecordTypes().isEmpty() || item.getRecordTypes().contains(_recordType)) {
                    NaaccrXmlField field = new NaaccrXmlField(item);
                    Field flatField = _flatLayout.getFieldByNaaccrItemNumber(item.getNaaccrNum());
                    field.setShortLabel(flatField == null ? item.getNaaccrName() : flatField.getShortLabel());

                    _allFields.add(field);
                    _fieldsCachedByNaaccrNumber.put(field.getNaaccrItemNum(), field);
                    _fieldsCachedByName.put(field.getNaaccrId(), field);

                    //If the field is a date, add child fields for the year, month and day parts
                    if (NaaccrXmlDictionaryUtils.NAACCR_DATA_TYPE_DATE.equals(item.getDataType())) {
                        String shortLbl = field.getShortLabel();
                        if (shortLbl.endsWith(" Dt"))
                            shortLbl = shortLbl.substring(0, shortLbl.length() - 3);

                        NaaccrDictionaryItem yearItem = new NaaccrDictionaryItem();
                        yearItem.setNaaccrId(item.getNaaccrId() + "Year");
                        yearItem.setNaaccrName(field.getNaaccrName() + " (Year)");
                        yearItem.setParentXmlElement(field.getParentXmlElement());
                        yearItem.setLength(4);
                        yearItem.setDataType(NaaccrXmlDictionaryUtils.NAACCR_DATA_TYPE_DIGITS);
                        NaaccrXmlField yearFld = new NaaccrXmlField(yearItem);
                        yearFld.setShortLabel(shortLbl + " Yr");
                        _fieldsCachedByNaaccrNumber.put(yearFld.getNaaccrItemNum(), yearFld);
                        _fieldsCachedByName.put(yearFld.getNaaccrId(), yearFld);

                        NaaccrDictionaryItem monthItem = new NaaccrDictionaryItem();
                        monthItem.setNaaccrId(item.getNaaccrId() + "Month");
                        monthItem.setNaaccrName(field.getNaaccrName() + " (Month)");
                        monthItem.setParentXmlElement(field.getParentXmlElement());
                        monthItem.setLength(2);
                        monthItem.setDataType(NaaccrXmlDictionaryUtils.NAACCR_DATA_TYPE_DIGITS);
                        NaaccrXmlField monthFld = new NaaccrXmlField(monthItem);
                        monthFld.setShortLabel(shortLbl + " Mth");
                        _fieldsCachedByNaaccrNumber.put(monthFld.getNaaccrItemNum(), monthFld);
                        _fieldsCachedByName.put(monthFld.getNaaccrId(), monthFld);

                        NaaccrDictionaryItem dayItem = new NaaccrDictionaryItem();
                        dayItem.setNaaccrId(item.getNaaccrId() + "Day");
                        dayItem.setNaaccrName(field.getNaaccrName() + " (Day)");
                        dayItem.setParentXmlElement(field.getParentXmlElement());
                        dayItem.setLength(2);
                        dayItem.setDataType(NaaccrXmlDictionaryUtils.NAACCR_DATA_TYPE_DIGITS);
                        NaaccrXmlField dayFld = new NaaccrXmlField(dayItem);
                        dayFld.setShortLabel(shortLbl + " Day");
                        _fieldsCachedByNaaccrNumber.put(dayFld.getNaaccrItemNum(), dayFld);
                        _fieldsCachedByName.put(dayFld.getNaaccrId(), dayFld);

                        field.setSubFields(Arrays.asList(yearFld, monthFld, dayFld));
                    }
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

        //NAACCR Version is required and must be a version that is currently supported
        if (_naaccrVersion == null || !NaaccrFormat.isVersionSupported(_naaccrVersion))
            throw new RuntimeException("Unsupported NAACCR version: " + _naaccrVersion);

        //Base dictionary is required
        if (_baseDictionary == null)
            throw new RuntimeException("Base Dictionary is required");

        //Validate the user dictionaries
        for (NaaccrDictionary userDictionary : _userDictionaries) {
            List<String> errors = NaaccrXmlDictionaryUtils.validateUserDictionary(userDictionary);
            if (!errors.isEmpty())
                throw new RuntimeException("Error found on user dictionary - " + errors.get(0));
        }

        //If fields/dictionaries were supposed to be loaded, check validity of fields and dictionaries. Otherwise, this is the end of validation.
        if (!_allFields.isEmpty()) {

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
                if (field.getNaaccrItemNum() != null) {
                    if (naaccrItemNums.contains(field.getNaaccrItemNum().toString()))
                        throw new RuntimeException("Field NAACCR item number must be unique, found duplicate number for '" + field.getNaaccrItemNum() + "'");
                    naaccrItemNums.add(field.getNaaccrItemNum().toString());
                }
                if (field.getLength() == null)
                    throw new RuntimeException("Field length is required, missing for field " + field.getName());
                if (field.getParentXmlElement() == null)
                    throw new RuntimeException("Field parent XML element is required, missing for field " + field.getName());
            }
        }
    }

    //Getters/Setters

    @Override
    public String getLayoutId() {
        return _layoutId;
    }

    public void setLayoutId(String layoutId) {
        _layoutId = layoutId;
    }

    @Override
    public String getLayoutName() {
        return _layoutName;
    }

    public void setLayoutName(String layoutName) {
        _layoutName = layoutName;
    }

    @Override
    public String getLayoutVersion() {
        return _naaccrVersion;
    }

    public void setLayoutVersion(String version) {
        _naaccrVersion = version;
    }

    @Override
    public String getLayoutDescription() {
        return _layoutDesc;
    }

    public void setLayoutDescription(String description) {
        _layoutDesc = description;
    }

    public String getNaaccrVersion() {
        return _naaccrVersion;
    }

    public void setNaaccrVersion(String naaccrVersion) {
        _naaccrVersion = naaccrVersion;
    }

    public String getRecordType() {
        return _recordType;
    }

    public void setRecordType(String recordType) {
        _recordType = recordType;
    }

    @Override
    public List<NaaccrXmlField> getAllFields() {
        return _allFields;
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
    public String getFieldDocByName(String name) {
        NaaccrXmlField field = getFieldByName(name);
        if (field == null)
            return null;

        return getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum());
    }

    @Override
    public String getFieldDocByNaaccrItemNumber(Integer num) {
        return _flatLayout.getFieldDocByNaaccrItemNumber(num);
    }

    @Override
    public String getFieldDocDefaultCssStyle() {
        return _flatLayout.getFieldDocDefaultCssStyle();
    }

    public void setFields(Collection<NaaccrXmlField> fields) {
        _allFields.clear();
        _allFields.addAll(fields);
        verify();
    }

    public NaaccrDictionary getBaseDictionary() {
        return _baseDictionary;
    }

    public void setBaseDictionary(NaaccrDictionary dictionary) {
        _baseDictionary = dictionary;
    }

    public List<NaaccrDictionary> getUserDictionaries() {
        return _userDictionaries;
    }

    public void setUserDictionaries(List<NaaccrDictionary> dictionaries) {
        _userDictionaries = dictionaries;
    }

    //Writers and readers

    /**
     * Writes a single patient using a provided writer. Does not open or close the writer.
     * @param writer The PatientXmlWriter used to write Patients to an XML file
     * @param patient The patient that is to be written
     * @throws NaaccrIOException if patients cannot be written
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
     * @throws NaaccrIOException if patients cannot be written
     */
    public void writeAllPatients(File file, List<Patient> allPatients, NaaccrData data, NaaccrOptions options) throws NaaccrIOException {
        try (FileOutputStream os = new FileOutputStream(file)) {
            writeAllPatients(os, allPatients, data, options);
        }
        catch (IOException e) {
            throw new NaaccrIOException(e.getMessage());
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
     * @throws NaaccrIOException if patients cannot be written
     */
    public void writeAllPatients(OutputStream outputStream, List<Patient> patients, NaaccrData data, NaaccrOptions options) throws NaaccrIOException {
        if (data == null)
            return;

        PatientXmlWriter writer = null;
        try {
            writer = new PatientXmlWriter(new OutputStreamWriter(outputStream, UTF_8), data, options);
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
     * @throws NaaccrIOException if patients cannot be written
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
     * @throws NaaccrIOException if patients cannot be read
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
     * @throws NaaccrIOException if patients cannot be read
     */
    public List<Patient> readAllPatients(File file, String encoding, NaaccrOptions options) throws NaaccrIOException {
        List<Patient> patients;

        try (FileInputStream is = new FileInputStream(file)) {
            patients = readAllPatients(is, encoding, options);
        }
        catch (IOException e) {
            throw new NaaccrIOException(e.getMessage());
        }

        return patients;
    }

    /**
     * Creates a PatientXmlReader from the InputStream parameter to read all Patients from an XML file. Passes the writer to the method below.
     * This method opens and closes the reader.
     * This method does not open or close the InputStream.
     * @param inputStream the InputStream used to read the patients (required)
     * @param encoding Stream encoding (required)
     * @param options (can be null)
     * @return List of all Patients read from the stream.
     * @throws NaaccrIOException if patients cannot be read
     */
    public List<Patient> readAllPatients(InputStream inputStream, String encoding, NaaccrOptions options) throws NaaccrIOException {
        List<Patient> patients;

        PatientXmlReader reader = null;
        try {
            reader = new PatientXmlReader(new InputStreamReader(inputStream, encoding), options);
            patients = readAllPatients(reader);
        }
        catch (IOException e) {
            throw new NaaccrIOException(e.getMessage());
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
     * * @throws NaaccrIOException if patients cannot be read
     */
    public List<Patient> readAllPatients(PatientXmlReader reader) throws NaaccrIOException {
        List<Patient> allPatients = new ArrayList<>();
        Patient patient;
        while ((patient = reader.readPatient()) != null)
            allPatients.add(patient);

        return allPatients;
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

        Map<String, String> attr;
        try (InputStreamReader is = new InputStreamReader(LayoutUtils.createInputStream(file, zipEntryName), UTF_8)) {
            attr = NaaccrXmlUtils.getAttributesFromXmlReader(is);
        }
        catch (IOException e) {
            return null;
        }

        String recordType = attr.get(NAACCR_XML_ROOT_ATT_REC_TYPE);
        if (recordType == null || recordType.isEmpty() || !_recordType.equals(recordType))
            return null;

        String baseUri = NaaccrXmlDictionaryUtils.createUriFromVersion(_naaccrVersion, true);
        if (!baseUri.equals(attr.get(NAACCR_XML_ROOT_ATT_BASE_DICT)))
            return null;

        LayoutInfo info = new LayoutInfo();
        info.setLayoutId(_layoutId);
        info.setLayoutName(_layoutName);
        info.setAvailableUserDictionaries(_userDictionaries == null ? Collections.emptyList() : _userDictionaries.stream().map(NaaccrDictionary::getDictionaryUri).collect(Collectors.toList()));
        info.setRequestedUserDictionaries(attr.get(NAACCR_XML_ROOT_ATT_USER_DICT) == null ? Collections.emptyList() : Arrays.asList(StringUtils.split(attr.get(NAACCR_XML_ROOT_ATT_USER_DICT), " ")));

        // at this point we know that this layout can be used to read the data file; let's try to get the root data and if anything goes wrong,
        // let's return the info object, without the root data and with the error
        NaaccrOptions xmlOptions = NaaccrOptions.getDefault();
        xmlOptions.setUseStrictNamespaces(options == null || options.isNaaccrXmlUseStrictNamespaces());
        xmlOptions.setIgnoreExtensions(true);
        try (InputStreamReader is = new InputStreamReader(LayoutUtils.createInputStream(file, zipEntryName), UTF_8); PatientXmlReader reader = new PatientXmlReader(is, xmlOptions)) {
            info.setRootNaaccrXmlData(reader.getRootData());
        }
        catch (IOException e) {
            info.setMissingRootNaaccrXmlDataReason(e.getMessage());
        }

        return info;
    }
}
