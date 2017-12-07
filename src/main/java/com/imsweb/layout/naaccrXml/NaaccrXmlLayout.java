/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.naaccrXml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import com.imsweb.layout.LayoutUtils;
import com.imsweb.naaccrxml.NaaccrIOException;
import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.PatientXmlReader;
import com.imsweb.naaccrxml.PatientXmlWriter;
import com.imsweb.naaccrxml.entity.NaaccrData;
import com.imsweb.naaccrxml.entity.Patient;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;

public class NaaccrXmlLayout implements Layout {

    private static final StringBuilder _CSS_STYLE_SUMMARY_TABLE = new StringBuilder();

    static {
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-summary-table { width: 100%; }\n");
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-summary-header { text-align: center; padding: 2px; background-color: #E0E0E0; }\n");
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-summary-cell { vertical-align:top; padding: 2px; }\n");
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-summary-centered { text-align: center; }\n");
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-borders { border: 1px solid gray; border-collapse: collapse; }\n");
        _CSS_STYLE_SUMMARY_TABLE.append(".naaccr-underline { text-decoration:underline; }\n");
    }

    // following styles are used only in version 14 and later (those come straight from the online NAACCR HTML page)
    private static final StringBuilder _DEFAULT_CSS_14_AND_LATER = new StringBuilder();

    static {
        _DEFAULT_CSS_14_AND_LATER.append("#readerWrapper { margin-top: 20px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#pnlSearch { width: 100%;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#lblSearch { margin-right: 20px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".wrapper { margin-bottom: 50px; width: 775px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(
                ".col { border-bottom: 1px solid black; border-left: 1px solid black; border-top: 1px solid black; float: left; height: 1100px; margin-bottom: 50px; padding: 0 5px; width: 180px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(
                ".colnoborder { border-bottom: 1px solid black; border-top: 1px solid black; float: left; height: 1100px; margin-bottom: 50px; padding: 0 5px; width: 180px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColHead td:first-child { text-align: left;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColBody { text-align: center; vertical-align: top;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColTitle { background-color: #165185; color: #FABA44;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColDataStripe td:first-child { border-left: 0 none;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColDataStripe table tr td { padding: 0; text-align: center;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColData td:first-child { border-left: 0 none;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColData table tr td { padding: 0; text-align: center;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColData td.HighlightToolTip,\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableColDataStripe td.HighlightToolTip { border-bottom: 1px dashed black; background-color: #a7d9f2;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".tableAppendixBody { border-left: 0 none; display: table-row; padding-bottom: 1px; text-align: left;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".code-nbr { padding-right: 20px; vertical-align: top;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#stateList { padding-bottom: 30px; text-align: center; width: 720px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#chapter { width: 950px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder { display: none; left: 0; position: absolute; top: 0; z-index: 99999;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_top { padding-bottom: 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_bottom { padding-top: 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_right { padding-left: 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_left { padding-right: 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tblSearch { margin-top: 20px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".dataDictionaryHeader { padding-right: 10px; text-align: right;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#menuWrapper { height: 200px; margin-top: 20px; width: 950px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chapterColumn { border-right: 2px solid white; float: left;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("a .chapter { color: #FFFFFF; padding: 0; text-decoration: none;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chapter:hover { background: none repeat scroll 0 0 #FF9900; color: #FFFFFF; text-decoration: none;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_arrow, #tiptip_arrow_inner { border-color: transparent; border-style: solid; border-width: 6px; height: 0; position: absolute; width: 0;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_top #tiptip_arrow { border-top-color: #FFFFCE;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_bottom #tiptip_arrow { border-bottom-color: #FFFFCE;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_right #tiptip_arrow { border-right-color: #FFFFCE;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_left #tiptip_arrow { border-left-color: #FFFFCE;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_top #tiptip_arrow_inner { border-top-color: #FFFFCE; margin-left: -6px; margin-top: -7px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_bottom #tiptip_arrow_inner { border-bottom-color: #FFFFCE; margin-left: -6px; margin-top: -5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_right #tiptip_arrow_inner { border-right-color: #FFFFCE; margin-left: -5px; margin-top: -6px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_left #tiptip_arrow_inner { border-left-color: #FFFFCE; margin-left: -7px; margin-top: -6px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".GeoCtrAlpha { float: left; width: 300px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".threeColSubTitle { float: left; font-style: italic; margin-bottom: 12px; width: 300px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".colRight { border-bottom: 1px solid black; border-left: 1px solid black; float: right; height: 1150px; padding-left: 20px; width: 454px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".colLeft { border-bottom: 1px solid black; float: left; height: 1150px; padding-left: 20px; width: 455px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".GeoCtr { float: left; width: 460px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".threeCol { border-bottom: 1px solid black; float: left; height: 1135px; padding-left: 10px; width: 305px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".threeColTitle { float: left; font-weight: bold; width: 300px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".CountryContinentName { float: left; font-weight: bold; margin-bottom: 15px; margin-top: 15px; width: 400px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(
                ".threeColMid { -moz-border-bottom-colors: none; -moz-border-image: none; -moz-border-left-colors: none; -moz-border-right-colors: none; -moz-border-top-colors: none; border-color: -moz-use-text-color black black; border-style: none solid solid; border-width: medium 1px 1px; float: left; height: 1135px; padding-left: 10px; width: 305px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_content { background-color: #FFFFCE; padding: 4px 8px 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_bottom #tiptip_arrow_inner { border-bottom-color: #FFFFCE;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#tiptip_holder.tip_top #tiptip_arrow_inner { border-top-color: rgba(20, 20, 20, 0.92);}\n");
        _DEFAULT_CSS_14_AND_LATER.append("h1 { margin: 1em 0;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("h2, h3 { margin: 0 0 1.33em 0;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("h4 { margin: 0;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("ul, ol { margin: 0 0 1.33em 35px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("ul li, ol li { margin-bottom: 1.33em;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("table.padded td, table.padded th { padding: 5px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("ul.nobullets { list-style: none;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("ul.notspaced li, ol.notspaced li { margin-bottom: 0;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".c8cell1 { width: 70px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#pnlSearch { width: 100%;}\n");
        _DEFAULT_CSS_14_AND_LATER.append("#lblSearch { margin-right: 20px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chap10-head-table { margin-top: 15px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chap10-para-head { font-weight: bold; padding-top: 10px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chap10-para { padding-bottom: 10px;}\n");
        _DEFAULT_CSS_14_AND_LATER.append(".chap10-para ul li { margin-bottom: 0;}\n");
    }

    private String _layoutId;

    private String _layoutName;

    private String _naaccrVersion;

    private String _layoutDesc;

    private String _recordType;

    private List<NaaccrXmlField> _allFields;

    private Map<String, NaaccrXmlField> _fieldsCachedByName;
    private Map<Integer, NaaccrXmlField> _fieldsCachedByNaaccrNumber;

    private NaaccrDictionary _baseDictionary;
    private List<NaaccrDictionary> _userDictionaries;

    //Constructors
    public NaaccrXmlLayout() {
        super();
    }

    public NaaccrXmlLayout(String naaccrVersion, String layoutId, String layoutName, String recordType, List<NaaccrDictionary> dictionaries) {
        _baseDictionary = NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion(naaccrVersion);
        _naaccrVersion = naaccrVersion;
        _layoutName = layoutName;
        _layoutId = layoutId;
        _recordType = recordType;
        _userDictionaries = dictionaries != null ? dictionaries : Collections.singletonList(NaaccrXmlDictionaryUtils.getDefaultUserDictionaryByVersion(naaccrVersion));

        StringBuilder description = new StringBuilder("This layout uses the base dictionary: " + _baseDictionary.getDescription());
        description.append(" This layout also uses the user dictionaries: ");
        for (NaaccrDictionary dictionary : _userDictionaries)
            description.append(dictionary.getDescription()).append(", ");
        description.setLength(description.length() - 2);
        _layoutDesc = description.toString();

        _allFields = new ArrayList<>();
        _fieldsCachedByName = new HashMap<>();
        _fieldsCachedByNaaccrNumber = new HashMap<>();

        //Get all item and grouped item definitions and add to layout's field list
        NaaccrDictionary allItemsDictionary = NaaccrXmlDictionaryUtils.mergeDictionaries(_baseDictionary, _userDictionaries.toArray(new NaaccrDictionary[_userDictionaries.size()]));
        List<NaaccrDictionaryItem> allItems = new ArrayList<>(allItemsDictionary.getItems());
        allItems.addAll(allItemsDictionary.getGroupedItems());
        for (NaaccrDictionaryItem item : allItems) {
            if (item.getRecordTypes().isEmpty() || item.getRecordTypes().contains(_recordType)) {
                NaaccrXmlField field = new NaaccrXmlField(item);
                if (item.getNaaccrName() != null) {
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
                throw new RuntimeException("Invalid user dictionary. Errors: " + errors + " found on dictionary at URI: " + userDictionary.getDictionaryUri());
        }

        //validate the fields
        if (!_allFields.isEmpty()) {
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
                if (field.getIsGroupedItem() && field.getContains() == null)
                    throw new RuntimeException("Fields corresponding to grouped item definitions must define contained items, missing definition for" + field.getName());
            }
        }
    }

    //Writers and readers
    public void writeNextPatient(PatientXmlWriter writer, Patient patient) throws NaaccrIOException {
        writer.writePatient(patient);
    }

    public void writeAllPatients(OutputStream outputStream, List<Patient> patients, NaaccrData data) throws NaaccrIOException {
        PatientXmlWriter writer = null;
        try {
            writer = new PatientXmlWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), data);
            for (Patient patient : patients)
                writer.writePatient(patient);
        }
        finally {
            //Keep parameter stream alive - we don't know what the other side is doing with it.
            if (writer != null)
                writer.closeAndKeepAlive();
        }
    }

    public void writeAllPatients(PatientXmlWriter writer, List<Patient> patients) throws NaaccrIOException {
        for (Patient patient : patients)
            writer.writePatient(patient);
    }

    public void writeAllPatients(File file, List<Patient> allPatients, NaaccrData data) throws IOException {
        try (PatientXmlWriter writer = new PatientXmlWriter(new FileWriter(file), data)) {
            for (Patient patient : allPatients)
                writer.writePatient(patient);
        }
    }

    public Patient readNextPatient(PatientXmlReader reader) throws NaaccrIOException {
        return reader.readPatient();
    }

    public List<Patient> readAllPatients(InputStream inputStream) throws NaaccrIOException {
        List<Patient> allPatients = new ArrayList<>();
        PatientXmlReader reader = null;
        try {
            reader = new PatientXmlReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            Patient patient;
            while ((patient = reader.readPatient()) != null)
                allPatients.add(patient);
        }
        finally {
            //Keep parameter stream alive - we don't know what the other side is doing with it.
            if (reader != null)
                reader.closeAndKeepAlive();
        }
        return allPatients;
    }

    public List<Patient> readAllPatients(PatientXmlReader reader) throws NaaccrIOException {
        List<Patient> allPatients = new ArrayList<>();
        Patient patient;
        while ((patient = reader.readPatient()) != null)
            allPatients.add(patient);

        return allPatients;
    }

    public List<Patient> readAllPatients(File file) throws NaaccrIOException, FileNotFoundException {
        List<Patient> allPatients = new ArrayList<>();
        try (PatientXmlReader reader = new PatientXmlReader(new FileReader(file))) {
            Patient patient;
            while ((patient = reader.readPatient()) != null)
                allPatients.add(patient);
        }
        return allPatients;
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

        return LayoutFactory.getLayout(_layoutId.replace("-xml", "")).getFieldDocByNaaccrItemNumber(field.getNaaccrItemNum());
    }

    @Override
    public String getFieldDocDefaultCssStyle() {
        return _CSS_STYLE_SUMMARY_TABLE.toString() + _DEFAULT_CSS_14_AND_LATER;
    }

    @Override
    public LayoutInfo buildFileInfo(File file, String zipEntryName, LayoutInfoDiscoveryOptions options) {
        LayoutInfo info = new LayoutInfo();
        NaaccrData data;
        try (PatientXmlReader reader = new PatientXmlReader(new InputStreamReader(LayoutUtils.createInputStream(file, zipEntryName)))) {
            data = reader.getRootData();
        }
        catch (IOException e) {
            return null;
        }

        if (!_baseDictionary.getDictionaryUri().equals(data.getBaseDictionaryUri()))
            return null;

        // Checking to see if the file uses custom dictionaries, and if this layout can access those dictionaries
        List<String> layoutDictionaryUris = new ArrayList<>();
        for (NaaccrDictionary dictionary : _userDictionaries)
            layoutDictionaryUris.add(dictionary.getDictionaryUri());
        if (!layoutDictionaryUris.containsAll(data.getUserDictionaryUri()))
            return null;

        info.setLayoutId(_layoutId);
        info.setLayoutName(_layoutName);
        return info;
    }
}
