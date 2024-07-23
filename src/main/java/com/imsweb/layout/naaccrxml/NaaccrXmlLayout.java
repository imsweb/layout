/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.naaccrxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.LayoutUtils;
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

    // CSS styles for the fields HTML documentation
    private static final StringBuilder _CSS_STYLE = new StringBuilder();

    static {
        // style for the summary tables
        _CSS_STYLE.append(".naaccr-summary-table { width: 100%; }\n");
        _CSS_STYLE.append(".naaccr-summary-header { text-align: center; padding: 2px; background-color: #E0E0E0; }\n");
        _CSS_STYLE.append(".naaccr-summary-cell { vertical-align:top; padding: 2px; }\n");
        _CSS_STYLE.append(".naaccr-summary-centered { text-align: center; }\n");
        _CSS_STYLE.append(".naaccr-borders { border: 1px solid gray; border-collapse: collapse; }\n");
        _CSS_STYLE.append(".naaccr-underline { text-decoration:underline; }\n");

        // style for everything else
        _CSS_STYLE.append("#readerWrapper { margin-top: 20px;}\n");
        _CSS_STYLE.append("#pnlSearch { width: 100%;}\n");
        _CSS_STYLE.append("#lblSearch { margin-right: 20px;}\n");
        _CSS_STYLE.append(".wrapper { margin-bottom: 50px; width: 775px;}\n");
        _CSS_STYLE.append(
                ".col { border-bottom: 1px solid black; border-left: 1px solid black; border-top: 1px solid black; float: left; height: 1100px; margin-bottom: 50px; padding: 0 5px; width: 180px;}\n");
        _CSS_STYLE.append(
                ".colnoborder { border-bottom: 1px solid black; border-top: 1px solid black; float: left; height: 1100px; margin-bottom: 50px; padding: 0 5px; width: 180px;}\n");
        _CSS_STYLE.append(".tableColHead td:first-child { text-align: left;}\n");
        _CSS_STYLE.append(".tableColBody { text-align: center; vertical-align: top;}\n");
        _CSS_STYLE.append(".tableColTitle { background-color: #165185; color: #FABA44;}\n");
        _CSS_STYLE.append(".tableColDataStripe td:first-child { border-left: 0 none;}\n");
        _CSS_STYLE.append(".tableColDataStripe table tr td { padding: 0; text-align: center;}\n");
        _CSS_STYLE.append(".tableColData td:first-child { border-left: 0 none;}\n");
        _CSS_STYLE.append(".tableColData table tr td { padding: 0; text-align: center;}\n");
        _CSS_STYLE.append(".tableColData td.HighlightToolTip,\n");
        _CSS_STYLE.append(".tableColDataStripe td.HighlightToolTip { border-bottom: 1px dashed black; background-color: #a7d9f2;}\n");
        _CSS_STYLE.append(".tableAppendixBody { border-left: 0 none; display: table-row; padding-bottom: 1px; text-align: left;}\n");
        _CSS_STYLE.append(".code-nbr { padding-right: 20px; vertical-align: top;}\n");
        _CSS_STYLE.append("#stateList { padding-bottom: 30px; text-align: center; width: 720px;}\n");
        _CSS_STYLE.append("#chapter { width: 950px;}\n");
        _CSS_STYLE.append("#tiptip_holder { display: none; left: 0; position: absolute; top: 0; z-index: 99999;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_top { padding-bottom: 5px;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_bottom { padding-top: 5px;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_right { padding-left: 5px;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_left { padding-right: 5px;}\n");
        _CSS_STYLE.append("#tblSearch { margin-top: 20px;}\n");
        _CSS_STYLE.append(".dataDictionaryHeader { padding-right: 10px; text-align: right;}\n");
        _CSS_STYLE.append("#menuWrapper { height: 200px; margin-top: 20px; width: 950px;}\n");
        _CSS_STYLE.append(".chapterColumn { border-right: 2px solid white; float: left;}\n");
        _CSS_STYLE.append("a .chapter { color: #FFFFFF; padding: 0; text-decoration: none;}\n");
        _CSS_STYLE.append(".chapter:hover { background: none repeat scroll 0 0 #FF9900; color: #FFFFFF; text-decoration: none;}\n");
        _CSS_STYLE.append("#tiptip_arrow, #tiptip_arrow_inner { border-color: transparent; border-style: solid; border-width: 6px; height: 0; position: absolute; width: 0;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_top #tiptip_arrow { border-top-color: #FFFFCE;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_bottom #tiptip_arrow { border-bottom-color: #FFFFCE;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_right #tiptip_arrow { border-right-color: #FFFFCE;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_left #tiptip_arrow { border-left-color: #FFFFCE;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_top #tiptip_arrow_inner { border-top-color: #FFFFCE; margin-left: -6px; margin-top: -7px;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_bottom #tiptip_arrow_inner { border-bottom-color: #FFFFCE; margin-left: -6px; margin-top: -5px;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_right #tiptip_arrow_inner { border-right-color: #FFFFCE; margin-left: -5px; margin-top: -6px;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_left #tiptip_arrow_inner { border-left-color: #FFFFCE; margin-left: -7px; margin-top: -6px;}\n");
        _CSS_STYLE.append(".GeoCtrAlpha { float: left; width: 300px;}\n");
        _CSS_STYLE.append(".threeColSubTitle { float: left; font-style: italic; margin-bottom: 12px; width: 300px;}\n");
        _CSS_STYLE.append(".colRight { border-bottom: 1px solid black; border-left: 1px solid black; float: right; height: 1150px; padding-left: 20px; width: 454px;}\n");
        _CSS_STYLE.append(".colLeft { border-bottom: 1px solid black; float: left; height: 1150px; padding-left: 20px; width: 455px;}\n");
        _CSS_STYLE.append(".GeoCtr { float: left; width: 460px;}\n");
        _CSS_STYLE.append(".threeCol { border-bottom: 1px solid black; float: left; height: 1135px; padding-left: 10px; width: 305px;}\n");
        _CSS_STYLE.append(".threeColTitle { float: left; font-weight: bold; width: 300px;}\n");
        _CSS_STYLE.append(".CountryContinentName { float: left; font-weight: bold; margin-bottom: 15px; margin-top: 15px; width: 400px;}\n");
        _CSS_STYLE.append(
                ".threeColMid { -moz-border-bottom-colors: none; -moz-border-image: none; -moz-border-left-colors: none; -moz-border-right-colors: none; -moz-border-top-colors: none; border-color: -moz-use-text-color black black; border-style: none solid solid; border-width: medium 1px 1px; float: left; height: 1135px; padding-left: 10px; width: 305px;}\n");
        _CSS_STYLE.append("#tiptip_content { background-color: #FFFFCE; padding: 4px 8px 5px;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_bottom #tiptip_arrow_inner { border-bottom-color: #FFFFCE;}\n");
        _CSS_STYLE.append("#tiptip_holder.tip_top #tiptip_arrow_inner { border-top-color: rgba(20, 20, 20, 0.92);}\n");
        _CSS_STYLE.append("h1 { margin: 1em 0;}\n");
        _CSS_STYLE.append("h2, h3 { margin: 0 0 1.33em 0;}\n");
        _CSS_STYLE.append("h4 { margin: 0;}\n");
        _CSS_STYLE.append("ul, ol { margin: 0 0 1.33em 35px;}\n");
        _CSS_STYLE.append("ul li, ol li { margin-bottom: 1.33em;}\n");
        _CSS_STYLE.append("table.padded td, table.padded th { padding: 5px;}\n");
        _CSS_STYLE.append("ul.nobullets { list-style: none;}\n");
        _CSS_STYLE.append("ul.notspaced li, ol.notspaced li { margin-bottom: 0;}\n");
        _CSS_STYLE.append(".c8cell1 { width: 70px;}\n");
        _CSS_STYLE.append("#pnlSearch { width: 100%;}\n");
        _CSS_STYLE.append("#lblSearch { margin-right: 20px;}\n");
        _CSS_STYLE.append(".chap10-head-table { margin-top: 15px;}\n");
        _CSS_STYLE.append(".chap10-para-head { font-weight: bold; padding-top: 10px;}\n");
        _CSS_STYLE.append(".chap10-para { padding-bottom: 10px;}\n");
        _CSS_STYLE.append(".chap10-para ul li { margin-bottom: 0;}\n");
    }

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
    private final List<NaaccrXmlField> _allFields = new ArrayList<>();

    // fields cache for quick access by name (which is NAACCR ID for this layout)
    private final Map<String, NaaccrXmlField> _fieldsCachedByName = new HashMap<>();

    // fields cache for quick access by NAACCR number
    private final Map<Integer, NaaccrXmlField> _fieldsCachedByNaaccrNumber = new HashMap<>();

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
    @SuppressWarnings("DataFlowIssue")
    public NaaccrXmlLayout(String naaccrVersion, String recordType, String layoutId, String layoutName, String description, List<NaaccrDictionary> dictionaries, boolean loadFields) {
        _naaccrVersion = naaccrVersion;
        _layoutName = layoutName;
        _layoutId = layoutId;
        _recordType = recordType;
        _baseDictionary = NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion(naaccrVersion);
        _userDictionaries = dictionaries;
        if (_userDictionaries == null || _userDictionaries.isEmpty()) {
            NaaccrDictionary defaultUserDictionary = NaaccrXmlDictionaryUtils.getDefaultUserDictionaryByVersion(naaccrVersion);
            if (defaultUserDictionary != null)
                _userDictionaries = Collections.singletonList(defaultUserDictionary);
            else
                _userDictionaries = Collections.emptyList();
        }
        _layoutDesc = StringUtils.stripToNull(description);

        // only load dictionaries/fields if specified, otherwise avoid expensive operations
        if (loadFields) {

            Map<String, String> shortLabels = new HashMap<>();
            Map<String, String> sections = new HashMap<>();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("layout/fixed/naaccr/items-extra-info.csv"), UTF_8))) {
                in.lines().forEach(line -> {
                    String[] parts = StringUtils.split(line, ',');
                    if (parts.length == 3) {
                        shortLabels.put(parts[0], parts[1]);
                        sections.put(parts[0], parts[2]);
                    }
                });
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }

            // get all item definitions, create fields and add to layout's field list based on record type
            for (NaaccrDictionaryItem item : NaaccrXmlDictionaryUtils.mergeDictionaries(_baseDictionary, _userDictionaries.toArray(new NaaccrDictionary[0])).getItems()) {
                if (item.getRecordTypes() == null || item.getRecordTypes().isEmpty() || item.getRecordTypes().contains(_recordType)) {
                    NaaccrXmlField field = new NaaccrXmlField(item);
                    field.setShortLabel(shortLabels.getOrDefault(item.getNaaccrId(), "?"));
                    field.setSection(sections.getOrDefault(item.getNaaccrId(), "?"));

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
                        _fieldsCachedByName.put(yearFld.getNaaccrId(), yearFld);

                        NaaccrDictionaryItem monthItem = new NaaccrDictionaryItem();
                        monthItem.setNaaccrId(item.getNaaccrId() + "Month");
                        monthItem.setNaaccrName(field.getNaaccrName() + " (Month)");
                        monthItem.setParentXmlElement(field.getParentXmlElement());
                        monthItem.setLength(2);
                        monthItem.setDataType(NaaccrXmlDictionaryUtils.NAACCR_DATA_TYPE_DIGITS);
                        NaaccrXmlField monthFld = new NaaccrXmlField(monthItem);
                        monthFld.setShortLabel(shortLbl + " Mth");
                        _fieldsCachedByName.put(monthFld.getNaaccrId(), monthFld);

                        NaaccrDictionaryItem dayItem = new NaaccrDictionaryItem();
                        dayItem.setNaaccrId(item.getNaaccrId() + "Day");
                        dayItem.setNaaccrName(field.getNaaccrName() + " (Day)");
                        dayItem.setParentXmlElement(field.getParentXmlElement());
                        dayItem.setLength(2);
                        dayItem.setDataType(NaaccrXmlDictionaryUtils.NAACCR_DATA_TYPE_DIGITS);
                        NaaccrXmlField dayFld = new NaaccrXmlField(dayItem);
                        dayFld.setShortLabel(shortLbl + " Day");
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
            throw new IllegalStateException("Layout ID is required");

        // name is required
        if (_layoutName == null || _layoutName.isEmpty())
            throw new IllegalStateException("Layout name is required");

        // Record type is required and must be one of the following: A, M, C, I
        if (_recordType == null || _recordType.isEmpty())
            throw new IllegalStateException("Record type is required");
        else if (!_recordType.equals("A") && !_recordType.equals("M") && !_recordType.equals("C") && !_recordType.equals("I")) {
            throw new IllegalStateException("Record type not recognized: " + _recordType);
        }

        // NAACCR Version is required and must be a version that is currently supported
        if (_naaccrVersion == null || !NaaccrFormat.isVersionSupported(_naaccrVersion))
            throw new IllegalStateException("Unsupported NAACCR version: " + _naaccrVersion);

        // base dictionary is required
        if (_baseDictionary == null)
            throw new IllegalStateException("Base Dictionary is required");

        // validate the user dictionaries
        for (NaaccrDictionary userDictionary : _userDictionaries) {
            List<String> errors = NaaccrXmlDictionaryUtils.validateUserDictionary(userDictionary);
            if (!errors.isEmpty())
                throw new IllegalStateException("Error found on user dictionary - " + errors.get(0));
        }

        // if fields/dictionaries were supposed to be loaded, check validity of fields and dictionaries. Otherwise, this is the end of validation.
        if (!_allFields.isEmpty()) {

            // validate the NaaccrXmlFields
            Set<String> names = new HashSet<>();
            Set<String> naaccrItemNums = new HashSet<>();
            for (NaaccrXmlField field : _allFields) {
                if (field.getName() == null)
                    throw new IllegalStateException("Field name (NAACCR XML ID) is required");
                if (names.contains(field.getName()))
                    throw new IllegalStateException("Field name (NAACCR XML ID) must be unique, found duplicate name for '" + field.getName() + "'");
                names.add(field.getName());
                if (field.getItem() == null)
                    throw new IllegalStateException("Field item definition is required, missing for field " + field.getName());
                if (field.getNaaccrItemNum() != null) {
                    if (naaccrItemNums.contains(field.getNaaccrItemNum().toString()))
                        throw new IllegalStateException("Field NAACCR number must be unique, found duplicate number for '" + field.getNaaccrItemNum() + "'");
                    naaccrItemNums.add(field.getNaaccrItemNum().toString());
                }
                if (field.getLength() == null)
                    throw new IllegalStateException("Field length is required, missing for field " + field.getName());
                if (field.getParentXmlElement() == null)
                    throw new IllegalStateException("Field parent XML element is required, missing for field " + field.getName());
            }
        }
    }

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
        return getFieldDocByNameOrNumber(name, null, null, null);
    }

    public String getFieldDocByName(String name, File archivedDocFile) {
        return getFieldDocByNameOrNumber(name, null, archivedDocFile, null);
    }

    public String getFieldDocByName(String name, ZipInputStream archivedDocStream) {
        return getFieldDocByNameOrNumber(name, null, null, archivedDocStream);
    }

    @Override
    public String getFieldDocByNaaccrItemNumber(Integer num) {
        return getFieldDocByNameOrNumber(null, num, null, null);
    }

    public String getFieldDocByNaaccrItemNumber(Integer num, File archivedDocFile) {
        return getFieldDocByNameOrNumber(null, num, archivedDocFile, null);
    }

    public String getFieldDocByNaaccrItemNumber(Integer num, ZipInputStream archivedDocStream) {
        return getFieldDocByNameOrNumber(null, num, null, archivedDocStream);
    }

    @SuppressWarnings("java:S1075") // hard-coded path separator
    protected String getFieldDocByNameOrNumber(String name, Integer number, File archivedDocFile, ZipInputStream archivedDocStream) {
        NaaccrXmlField field = name != null ? getFieldByName(name) : getFieldByNaaccrItemNumber(number);

        String filename = null;
        if (field != null)
            filename = field.getName();
        else if (number != null)
            filename = number.toString();

        if (filename == null)
            return null;

        String result = null;

        URL docPath = Thread.currentThread().getContextClassLoader().getResource("layout/fixed/naaccr/doc/" + getDocFolder() + "/" + filename + ".html");
        if (docPath != null) {
            try (Reader reader = new InputStreamReader(docPath.openStream(), StandardCharsets.UTF_8); Writer writer = new StringWriter()) {
                IOUtils.copy(reader, writer);
                result = writer.toString();
            }
            catch (IOException e) {
                /* do nothing, result will be null, as per specs */
            }
        }
        else if (archivedDocFile != null && archivedDocFile.exists())
            result = LayoutUtils.readNaaccrDocumentationFromFile(getDocFolder() + "/" + filename + ".html", archivedDocFile);
        else if (archivedDocStream != null)
            result = LayoutUtils.readNaaccrDocumentationFromZipInputStream(getDocFolder() + "/" + filename + ".html", archivedDocStream);

        return result;
    }

    protected String getDocFolder() {
        // there is always a delay before the documentation is released on the NAACCR website...
        if ("250".equals(_naaccrVersion))
            return "naaccr24";
        return "naaccr" + _naaccrVersion.substring(0, 2);
    }

    @Override
    public String getFieldDocDefaultCssStyle() {
        return _CSS_STYLE.toString();
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
    @SuppressWarnings("java:S2093") // use try-with-resources; this method doesn't close the writer!
    public void writeAllPatients(OutputStream outputStream, List<Patient> patients, NaaccrData data, NaaccrOptions options) throws NaaccrIOException {
        if (data == null)
            return;

        PatientXmlWriter writer = null;
        try {
            writer = new PatientXmlWriter(new OutputStreamWriter(outputStream, UTF_8), data, options, _userDictionaries);
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
    @SuppressWarnings("java:S2093") // use try-with-resources; this method doesn't close the writer!
    public List<Patient> readAllPatients(InputStream inputStream, String encoding, NaaccrOptions options) throws NaaccrIOException {
        List<Patient> patients;

        PatientXmlReader reader = null;
        try {
            reader = new PatientXmlReader(new InputStreamReader(inputStream, encoding), options, _userDictionaries);
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
        NaaccrOptions opts = NaaccrOptions.getDefault();
        opts.setUseStrictNamespaces(options == null || options.isNaaccrXmlUseStrictNamespaces());
        opts.setIgnoreExtensions(true);
        try (InputStreamReader is = new InputStreamReader(LayoutUtils.createInputStream(file, zipEntryName), UTF_8); PatientXmlReader reader = new PatientXmlReader(is, opts, _userDictionaries)) {
            info.setRootNaaccrXmlData(reader.getRootData());
        }
        catch (IOException e) {
            info.setErrorMessage(e.getMessage());
        }

        return info;
    }
}
