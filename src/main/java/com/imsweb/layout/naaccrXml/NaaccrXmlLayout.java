/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.naaccrXml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.imsweb.layout.Field;
import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.naaccrxml.NaaccrIOException;
import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.PatientXmlReader;
import com.imsweb.naaccrxml.PatientXmlWriter;
import com.imsweb.naaccrxml.entity.NaaccrData;
import com.imsweb.naaccrxml.entity.Patient;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryGroupedItem;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;

public class NaaccrXmlLayout implements Layout {

    private static String _RECORD_TYPE_ABSTRACT = "A";
    private static String _RECORD_TYPE_MODIFIED = "M";
    private static String _RECORD_TYPE_CONFIDENTIAL = "C";
    private static String _RECORD_TYPE_INCIDENCE = "I";

    //the layout ID is the Base Dictionary URI associated with this layout
    private String _layoutId;

    private String _layoutName;

    private String _layoutVersion;

    private String _layoutDesc;

    private List<NaaccrXmlField> _allFields;

    private Map<String, NaaccrXmlField> _fieldsCachedByName;
    private Map<Integer, NaaccrXmlField> _fieldsCachedByNaaccrNumber;

    //TODO how to handle a layout that utilizes multiple/custom user dictionaries? (This and DataViewer issue)
    //TODO DV is gonna need a dictionaries folder for sure. But we don't want to persist layouts that are based on these - will require Xml Dtos.
    //TODO DV needs to be able to take in a file, read it and say "this uses the 160 base dictionary, but it also uses some other ones that I can't access"
    //TODO then the user gives it access somehow, and then DV creates an internal layout. But needs to persist this info sometimes
    //TODO Creating files - won't be 160 type bc it also uses other dictionaries. so it's 160-2 dictionaries/160-3 dictionaries, etc.
    //TODO how to indicate that to the user? how to handle that with the layout?
    //TODO if a user dictionary is added/this is initialized with multiple user dictionaries, it needs to generate its own name.
    //TODO how to keep track of that though? If a user registers
    //TODO what about - user inputs file, file is detected as Naaccr 160 xml or whatever. Then DV says hey, I also detected these other libraries. Do you want to use these

    private String _baseDefaultDictionaryUri;
    private String _userDefaultDictionaryUri;
    private List<String> _userDictionaries; //TODO Does this need to actually hold the user dictionaries? Will it cause problems if those are too big?

    //TODO Runtime vs IOException for errors
    //TODO 2 string constructors - 1 from Naaccr version and one from base dictionary Uri. Do we need both? Would version be more useful?
    //TODO for name/Id inputs, if they are blank/null is this an error or should there be a default?
    //TODO check other layouts - how much of this needs to be logged?
    //TODO incorporate abstract/incidence/etc. In Name/Id/etc.

    //Constructors
    public NaaccrXmlLayout() {
        super();
    }

    public NaaccrXmlLayout(String naaccrVersion, String recordType) {
        NaaccrDictionary dictionary = NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion(naaccrVersion);
        if (dictionary == null)
            throw new RuntimeException("Could not find base dictionary from version: " + naaccrVersion);
        _baseDefaultDictionaryUri = dictionary.getDictionaryUri();

        dictionary = NaaccrXmlDictionaryUtils.getDefaultUserDictionaryByVersion(naaccrVersion);
        if (dictionary == null)
            throw new RuntimeException("Could not find default user dictionary from version: " + naaccrVersion);
        _userDefaultDictionaryUri = dictionary.getDictionaryUri();
        init();
    }

    public NaaccrXmlLayout(NaaccrDictionary baseDictionary) {
        if (NaaccrXmlDictionaryUtils.validateBaseDictionary(baseDictionary) != null)
            throw new RuntimeException("Base dictionary invalid: " + baseDictionary.getDictionaryUri());
        _baseDefaultDictionaryUri = baseDictionary.getDictionaryUri();
        init();
    }

    public NaaccrXmlLayout(String baseDictionaryUri) {
        NaaccrDictionary dictionary = NaaccrXmlDictionaryUtils.getBaseDictionaryByUri(baseDictionaryUri);
        if (dictionary == null) {
            throw new RuntimeException("Dictionary could not be found from URI.");
        }
        _baseDefaultDictionaryUri = dictionary.getDictionaryUri();
        init();
    }

    public NaaccrXmlLayout(List<NaaccrDictionary> dictionaries, String layoutId, String layoutName) {
        _userDictionaries = new ArrayList<>();
        for (NaaccrDictionary dictionary : dictionaries) {
            if (NaaccrXmlDictionaryUtils.validateBaseDictionary(dictionary) == null) {
                if (_baseDefaultDictionaryUri != null)
                    throw new RuntimeException("Layout cannot be created from multiple base dictionaries: " + _baseDefaultDictionaryUri + " " + dictionary.getDictionaryUri());

                //TODO this may need adjustment, depending on how we handle the format id if there are user dictionaries involved.
                _baseDefaultDictionaryUri = dictionary.getDictionaryUri();
            }
            else {
                String errorMessage = NaaccrXmlDictionaryUtils.validateUserDictionary(dictionary);
                if (errorMessage != null)
                    throw new RuntimeException("Invalid user dictionary: " + errorMessage);

                _userDictionaries.add(dictionary.getDictionaryUri());
            }
        }
        if (_baseDefaultDictionaryUri == null)
            throw new RuntimeException("No base dictionary found");

        if (layoutId == null || layoutId.isEmpty())
            throw new RuntimeException("Layout ID is required");
        _layoutId = layoutId;

        if (layoutName == null || layoutName.isEmpty())
            throw new RuntimeException("Layout name is required");
        _layoutName = layoutName;

        init();
    }

    private void init() {
        _allFields = new ArrayList<>();
        _fieldsCachedByName = new HashMap<>();
        _fieldsCachedByNaaccrNumber = new HashMap<>();

        //_baseDefaultDictionaryUri will always have a value at this point. //TODO this may need adjustment depending on if there are non default user dictionaries.
        NaaccrDictionary baseDictionary = NaaccrXmlDictionaryUtils.getBaseDictionaryByUri(_baseDefaultDictionaryUri);
        _layoutVersion = baseDictionary.getNaaccrVersion();
        _layoutId = _layoutId == null ? baseDictionary.getDictionaryUri() : _layoutId;
        _layoutDesc = baseDictionary.getDescription();
        _layoutName = _layoutName == null ? "NAACCR XML " + _layoutVersion : _layoutName;

        //Loading items from Base dictionary
        for (NaaccrDictionaryItem item : baseDictionary.getItems()) {
            NaaccrXmlField field = new NaaccrXmlField(item);
            _allFields.add(field);
            _fieldsCachedByNaaccrNumber.put(field.getNaaccrItemNum(), field);
            _fieldsCachedByName.put(field.getName(), field);
        }

        for (NaaccrDictionaryGroupedItem item : baseDictionary.getGroupedItems()) {
            NaaccrXmlField field = new NaaccrXmlField(item);
            _allFields.add(field);
            _fieldsCachedByNaaccrNumber.put(field.getNaaccrItemNum(), field);
            _fieldsCachedByName.put(field.getName(), field);
        }

        //Loading items from User dictionaries (default or custom)
        if (_userDictionaries == null) {
            NaaccrDictionary userDefaultDictionary = NaaccrXmlDictionaryUtils.getDefaultUserDictionaryByVersion(_layoutVersion);
            _userDefaultDictionaryUri = userDefaultDictionary.getDictionaryUri();
            for (NaaccrDictionaryItem item : userDefaultDictionary.getItems()) {
                NaaccrXmlField field = new NaaccrXmlField(item);
                _allFields.add(field);
                _fieldsCachedByNaaccrNumber.put(field.getNaaccrItemNum(), field);
                _fieldsCachedByName.put(field.getName(), field);
            }
        }
        else {
            //Change name and ID to reflect format changes
            for (String uri : _userDictionaries) {
                //TODO will need to pass in user dictionaries at some point - can't get from just URI.
                //                for (NaaccrDictionaryItem item : NaaccrXmlDictionaryUtils.(uri).getItems()) {
                //                    NaaccrXmlField field = new NaaccrXmlField(item);
                //                    _allFields.add(field);
                //                    _fieldsCachedByNaaccrNumber.put(field.getNaaccrItemNum(), field);
                //                    _fieldsCachedByName.put(field.getName(), field);
                //                }
            }
        }
    }

    //Writers and readers.
    public Patient readNextPatient(PatientXmlReader reader) {
        Patient patient = null;
        try {
            patient = reader.readPatient();
        }
        catch (NaaccrIOException e) {
            //TODO throw an error?
        }

        if (patient != null && !patient.getAllValidationErrors().isEmpty()) {
            //TODO throw an error? Report the validation errors?
        }
        return patient;
    }

    public List<Patient> readAllPatients(PatientXmlReader reader) {
        List<Patient> allPatients = new ArrayList<>();
        try {
            Patient patient = reader.readPatient();
            while (patient != null) {
                if (!patient.getAllValidationErrors().isEmpty()) {
                    //TODO throw an error? Log it? Make a list of patients with errors?
                }
                allPatients.add(patient);
                patient = reader.readPatient();
            }
        }
        catch (NaaccrIOException e) {
            //TODO throw an error? Just log it?
        }
        return allPatients;
    }

    public void writeNextPatient(PatientXmlWriter writer, Patient patient) {
        try {
            writer.writePatient(patient);
        }
        catch (NaaccrIOException e) {
            //TODO throw an error
        }
    }

    public void writeAllPatients(PatientXmlWriter writer, List<Patient> allPatients) {
        for (Patient patient : allPatients) {
            try {
                writer.writePatient(patient);
            }
            catch (NaaccrIOException e) {
                //TODO throw an error
            }
        }
    }

    public void addUserDictionaries(List<NaaccrDictionary> dictionaries, String layoutName, String layoutId) {
        for (NaaccrDictionary dictionary : dictionaries) {
            if (dictionary != null && dictionary.getItems() != null && !dictionary.getItems().isEmpty()) {
                String errors = NaaccrXmlDictionaryUtils.validateUserDictionary(dictionary);
                if (errors == null) {
                    _userDictionaries.add(dictionary.getDictionaryUri());
                    for (NaaccrDictionaryItem item : dictionary.getItems()) {
                        NaaccrXmlField field = new NaaccrXmlField(item);
                        _allFields.add(field);
                        _fieldsCachedByNaaccrNumber.put(field.getNaaccrItemNum(), field);
                        _fieldsCachedByName.put(field.getName(), field);
                    }
                }
                else {
                    throw new RuntimeException("Invalid user dictionary: " + errors);
                }
            }
        }
        if (layoutId == null || layoutId.isEmpty())
            throw new RuntimeException("Layout ID is required");
        _layoutId = layoutId;
        if (layoutName == null || layoutName.isEmpty())
            throw new RuntimeException("Layout name is required");
        _layoutName = layoutName;
    }

    public List<NaaccrXmlField> getFieldsForRecordType(String recordType) {
        List<NaaccrXmlField> fieldsList = null;
        if (_RECORD_TYPE_ABSTRACT.equals(recordType) || _RECORD_TYPE_MODIFIED.equals(recordType))
            fieldsList = _allFields;
        else if (_RECORD_TYPE_CONFIDENTIAL.equals(recordType) || _RECORD_TYPE_INCIDENCE.equals(recordType)) {
            fieldsList = new ArrayList<>();
            for (NaaccrXmlField field : _allFields)
                if (field.getRecordTypes().contains(recordType))
                    fieldsList.add(field);
        }
        return fieldsList;
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
        return _layoutVersion;
    }

    @Override
    public String getLayoutDescription() {
        return _layoutDesc;
    }

    @Override
    public Field getFieldByName(String name) {
        return _fieldsCachedByName.get(name);
    }

    @Override
    public Field getFieldByNaaccrItemNumber(Integer num) {
        return _fieldsCachedByNaaccrNumber.get(num);
    }

    @Override
    public List<? extends Field> getAllFields() {
        return _allFields;
    }

    //XML Fields don't have documentation //TODO should these throw errors if they are called?
    @Override
    public String getFieldDocByName(String name) {
        return null;
    }

    //XML Fields don't have documentation
    @Override
    public String getFieldDocByNaaccrItemNumber(Integer num) {
        return null;
    }

    //XML Fields don't have documentation
    @Override
    public String getFieldDocDefaultCssStyle() {
        return null;
    }

    //Todo should the name
    @Override
    public LayoutInfo buildFileInfo(File file, String zipEntryName, LayoutInfoDiscoveryOptions options) {
        LayoutInfo info = new LayoutInfo();
        try {
            PatientXmlReader reader = new PatientXmlReader(new FileReader(file));
            NaaccrData data = reader.getRootData();
            //TODO may need to be adjusted depending on how we handle adding user dictionaries
            String baseUri = data.getBaseDictionaryUri();
            info.setLayoutId(baseUri);
            info.setLayoutName("NAACCR XML " + NaaccrXmlDictionaryUtils.getBaseDictionaryByUri(baseUri).getNaaccrVersion() + " " + data.getRecordType());
        }
        catch (FileNotFoundException | NaaccrIOException e) {
            //TODO throw an error
        }
        return info;
    }
}
