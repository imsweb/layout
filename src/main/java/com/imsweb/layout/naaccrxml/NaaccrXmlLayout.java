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

// TODO clean up Javadocs
public class NaaccrXmlLayout implements Layout {

    private NaaccrLayout _baseFlatFile; // TODO I wouldn't call it "FlatFile"; maybe "_flatLayout"?

    private String _layoutId;

    private String _layoutName;

    private String _naaccrVersion;

    private String _layoutDesc;

    private String _recordType;

    private List<NaaccrXmlField> _allFields = new ArrayList<>();

    private Map<String, NaaccrXmlField> _fieldsCachedByName = new HashMap<>();
    private Map<Integer, NaaccrXmlField> _fieldsCachedByNaaccrNumber = new HashMap<>();

    private NaaccrDictionary _baseDictionary;
    private List<NaaccrDictionary> _userDictionaries;

    //Constructors
    public NaaccrXmlLayout(String naaccrVersion, String recordType, String layoutId, String layoutName, List<NaaccrDictionary> dictionaries, boolean loadFields) {
        _naaccrVersion = naaccrVersion; // TODO use the library to validate the version and the record type
        _layoutName = layoutName;
        _layoutId = layoutId; // TODO this seem wrong (and the name as well); if I create a v18 dictionary with a user-one, it shouldn't be recognized as the same as a pure v18...
        _recordType = recordType;
        _userDictionaries = dictionaries;

        //Load base dictionary from version
        _baseDictionary = NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion(naaccrVersion);

        //If user dictionaries weren't provided, use default user dictionary
        if (_userDictionaries == null)
            _userDictionaries = Collections.singletonList(NaaccrXmlDictionaryUtils.getDefaultUserDictionaryByVersion(naaccrVersion));

        //add description from base and user dictionaries
        // TODO this might be too long, need to see how it looks like with 2 user dictionaries for example...
        StringBuilder description = new StringBuilder("This layout uses the base dictionary: " + _baseDictionary.getDescription());
        description.append(" This layout also uses the user dictionaries: ");
        for (NaaccrDictionary dictionary : _userDictionaries)
            description.append(dictionary.getDescription()).append(", ");
        description.setLength(description.length() - 2);
        _layoutDesc = description.toString();

        if (loadFields) {
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
            _baseFlatFile = (NaaccrLayout)LayoutFactory.getLayout(flatFileId.toString()); // TODO make sure NAACCR layout is not null, raise exception otherwise

            // TODO FD for now we don't handle "groups" (so subfields); should we?
            //Get all item definitions, create fields and add to layout's field list
            NaaccrDictionary allItemsDictionary = NaaccrXmlDictionaryUtils.mergeDictionaries(_baseDictionary, _userDictionaries.toArray(new NaaccrDictionary[_userDictionaries.size()]));
            List<NaaccrDictionaryItem> allItems = new ArrayList<>(allItemsDictionary.getItems()); // TODO there is no reason to create the list variable...
            for (NaaccrDictionaryItem item : allItems) {
                if (item.getRecordTypes() == null || item.getRecordTypes().isEmpty() || item.getRecordTypes().contains(_recordType)) { // TODO are we sure about this logic? Seems suspicious to me
                    NaaccrXmlField field = new NaaccrXmlField(item);
                    _allFields.add(field);
                    _fieldsCachedByNaaccrNumber.put(field.getNaaccrItemNum(), field);
                    _fieldsCachedByName.put(field.getNaaccrId(), field);
                }
            }
        }
        verify();
    }

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

        //Base dictionary is required
        if (_baseDictionary == null)
            throw new RuntimeException("Base Dictionary is required");

        //Validate the user dictionaries
        for (NaaccrDictionary userDictionary : _userDictionaries) {
            String errors = NaaccrXmlDictionaryUtils.validateUserDictionary(userDictionary);
            if (errors != null)
                throw new RuntimeException("Invalid user dictionary. Error '" + errors + "' found on dictionary at URI: " + userDictionary.getDictionaryUri()); // TODO this is too verbose; the error might be very long.
        }

        //validate the fields
        if (_allFields != null && !_allFields.isEmpty()) {
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
    public void writeNextPatient(PatientXmlWriter writer, Patient patient) throws NaaccrIOException {
        if (patient != null)
            writer.writePatient(patient);
    }

    public void writeAllPatients(File file, List<Patient> allPatients, NaaccrData data) throws IOException {
        try (FileOutputStream os = new FileOutputStream(file)) {
            writeAllPatients(os, allPatients, data);
        }
    }

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

    public void writeAllPatients(PatientXmlWriter writer, List<Patient> patients) throws NaaccrIOException {
        if (patients != null)
            for (Patient patient : patients)
                writer.writePatient(patient);
    }

    public Patient readNextPatient(PatientXmlReader reader) throws NaaccrIOException {
        return reader.readPatient();
    }

    public List<Patient> readAllPatients(File file, String encoding) throws IOException {
        List<Patient> patients;

        try (FileInputStream is = new FileInputStream(file)) {
            patients = readAllPatients(is, encoding);
        }
        return patients;
    }

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

    public List<Patient> readAllPatients(PatientXmlReader reader) throws NaaccrIOException {
        List<Patient> allPatients = new ArrayList<>();
        Patient patient;
        while ((patient = reader.readPatient()) != null)
            allPatients.add(patient);

        return allPatients;
    }

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

    @Override
    public String getFieldDocByName(String name) {
        NaaccrXmlField field = getFieldByName(name);
        if (field == null)
            return null;

        return getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum());
    }

    @Override
    public String getFieldDocByNaaccrItemNumber(Integer num) {
        //NAACCR XML ID's don't all align with fixed column ID's/field doc names - need to use number to get doc through fixed column layout
        NaaccrXmlField field = getFieldByNaaccrItemNumber(num);
        if (field == null)
            return null;

        return _baseFlatFile.getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum());
    }

    @Override
    public String getFieldDocDefaultCssStyle() {
        return _baseFlatFile.getFieldDocDefaultCssStyle();
    }

    @Override
    public LayoutInfo buildFileInfo(File file, String zipEntryName, LayoutInfoDiscoveryOptions options) {
        if (file == null)
            return null;

        LayoutInfo info = new LayoutInfo();
        Map<String, String> attributes = NaaccrXmlUtils.getAttributesFromXmlFile(file);

        String recordType = attributes.get(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_REC_TYPE);
        if (recordType == null || recordType.isEmpty() || !_recordType.equals(recordType))
            return null;

        String baseUri = "http://naaccr.org/naaccrxml/naaccr-dictionary-" + _naaccrVersion + ".xml"; // TODO we have the base dictionary, we should be able to use its URI and not build it here
        if (!baseUri.equals(attributes.get(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_BASE_DICT)))
            return null;

        // Checking to see if the file uses custom dictionaries, and if this layout can access those dictionaries
        List<String> layoutDictionaryUris = new ArrayList<>();
        if (_userDictionaries == null) // TODO this is not possible, it's set in the unique constructor...
            layoutDictionaryUris.add("http://naaccr.org/naaccrxml/user-defined-naaccr-dictionary-" + _naaccrVersion + ".xml");
        else
            for (NaaccrDictionary dictionary : _userDictionaries)
                layoutDictionaryUris.add(dictionary.getDictionaryUri());

        // TODO use SeerString.split()
        if (attributes.get(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_USER_DICT) != null &&
                !layoutDictionaryUris.containsAll(Arrays.asList(attributes.get(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_USER_DICT).split(" "))))
            return null;

        info.setLayoutId(_layoutId);
        info.setLayoutName(_layoutName);
        return info;
    }
}
