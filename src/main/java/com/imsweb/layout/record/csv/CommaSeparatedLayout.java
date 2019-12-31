/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.layout.record.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.opencsv.CSVParserBuilder;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.LayoutUtils;
import com.imsweb.layout.record.RecordLayout;
import com.imsweb.layout.record.RecordLayoutOptions;
import com.imsweb.layout.record.csv.xml.CommaSeparatedLayoutFieldXmlDto;
import com.imsweb.layout.record.csv.xml.CommaSeparatedLayoutXmlDto;

/**
 * This class contains the logic related to a comma-separated-value layout (CSV).
 * <p/>
 * Created on Jul 28, 2011 by depryf
 * @author depryf
 */
public class CommaSeparatedLayout extends RecordLayout {

    /**
     * Number of fields
     */
    protected Integer _numFields;

    /**
     * Separator
     */
    protected char _separator = ',';

    /**
     * Ignore (header) first line (this is purely informational; the read/write methods do not use this field, it is the caller's responsibility to use it)
     */
    protected boolean _ignoreFirstLine;

    /**
     * The fields for this layout
     */
    protected List<CommaSeparatedField> _fields = new ArrayList<>();

    /**
     * Cached fields by name
     */
    protected Map<String, CommaSeparatedField> _cachedByName = new HashMap<>();

    /**
     * Cached fields by NAACCR Item Number
     */
    protected Map<Integer, CommaSeparatedField> _cachedByNaaccrItemNumber = new HashMap<>();

    /**
     * Default constructor.
     */
    public CommaSeparatedLayout() {
        super();

        _ignoreFirstLine = true;
    }

    /**
     * Constructor.
     * @param layoutUrl URL to the XML definition, cannot be null
     * @throws IOException if the XML definition is not valid
     */
    public CommaSeparatedLayout(URL layoutUrl) throws IOException {
        this();

        if (layoutUrl == null)
            throw new NullPointerException("Unable to create layout from null URL");

        try (InputStream is = layoutUrl.openStream()) {
            init(LayoutUtils.readCommaSeparatedLayout(is));
        }
    }

    /**
     * Constructor.
     * @param layoutFile XML definition, cannot be null, must exists
     * @throws IOException if the XML definition is not valid
     */
    public CommaSeparatedLayout(File layoutFile) throws IOException {
        this();

        if (layoutFile == null)
            throw new NullPointerException("Unable to create layout from null file");
        if (!layoutFile.exists())
            throw new IOException("Unable to read from " + layoutFile.getPath());

        try (InputStream is = new FileInputStream(layoutFile)) {
            init(LayoutUtils.readCommaSeparatedLayout(is));
        }
    }

    /**
     * Constructor.
     * @param layoutXmlDto java representation of the XML definition, cannot be null
     * @throws IOException if the XML definition is not valid
     */
    public CommaSeparatedLayout(CommaSeparatedLayoutXmlDto layoutXmlDto) throws IOException {
        this();

        if (layoutXmlDto == null)
            throw new NullPointerException("Unable to create layout from null XML object");

        init(layoutXmlDto);
    }

    protected void init(CommaSeparatedLayoutXmlDto layoutXmlDto) throws IOException {

        _layoutId = layoutXmlDto.getId();
        _layoutName = layoutXmlDto.getName();
        _layoutVersion = layoutXmlDto.getVersion();
        _layoutDesc = layoutXmlDto.getDescription();
        _numFields = layoutXmlDto.getNumFields();
        _separator = layoutXmlDto.getSeparator() == null ? ',' : layoutXmlDto.getSeparator().charAt(0);
        _ignoreFirstLine = layoutXmlDto.getIgnoreFirstLine() == null ? true : layoutXmlDto.getIgnoreFirstLine();

        // are we extending another layout?
        CommaSeparatedLayout parentLayout = null;
        if (layoutXmlDto.getExtendLayout() != null) {
            if (!LayoutFactory.getAvailableLayouts().containsKey(layoutXmlDto.getExtendLayout()))
                throw new IOException("Unable to find referenced layout ID '" + layoutXmlDto.getExtendLayout() + "'");
            try {
                parentLayout = (CommaSeparatedLayout)LayoutFactory.getLayout(layoutXmlDto.getExtendLayout());
            }
            catch (ClassCastException e) {
                throw new IOException("A CSV layout must extend another CSV layout");
            }
            _parentLayoutId = parentLayout.getLayoutId();
        }

        _fields.clear();
        if (layoutXmlDto.getField() != null)
            for (CommaSeparatedLayoutFieldXmlDto fieldXmlDto : layoutXmlDto.getField())
                addField(createFieldFromXmlField(fieldXmlDto));
        if (parentLayout != null)
            parentLayout.getAllFields().stream().filter(field -> !_cachedByName.containsKey(field.getName())).forEach(this::addField);

        // sort the fields by index
        _fields.sort(Comparator.comparing(CommaSeparatedField::getIndex));

        // final verifications
        try {
            verify();
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Helper that translates an XML field into an exposed DTO field.
     * <p/>
     * Created on Aug 16, 2011 by depryf
     * @param dto <code>CommaSeparatedLayoutFieldXmlDto</code> to translate
     * @return the translated <code>Field</code>
     */
    protected CommaSeparatedField createFieldFromXmlField(CommaSeparatedLayoutFieldXmlDto dto) {
        CommaSeparatedField field = new CommaSeparatedField();

        field.setName(dto.getName());
        field.setLongLabel(dto.getLongLabel());
        field.setShortLabel(dto.getShortLabel() != null ? dto.getShortLabel() : dto.getLongLabel());
        field.setNaaccrItemNum(dto.getNaaccrItemNum());
        field.setIndex(dto.getIndex());
        field.setMaxLength(dto.getMaxLength());
        field.setDefaultValue(dto.getDefaultValue());
        field.setTrim(dto.getTrim() == null ? Boolean.TRUE : dto.getTrim());

        return field;
    }

    private void addField(CommaSeparatedField field) {
        // update collection of fields
        _fields.add(field);
        // update name cache
        _cachedByName.put(field.getName(), field);
        // update NAACCR cache
        if (field.getNaaccrItemNum() != null)
            _cachedByNaaccrItemNumber.put(field.getNaaccrItemNum(), field);
    }

    public void setFields(Collection<CommaSeparatedField> fields) {
        _fields.clear();
        fields.forEach(this::addField);

        // sort the fields by index
        _fields.sort(Comparator.comparing(CommaSeparatedField::getIndex));

        // verify they make sense
        verify();
    }

    /**
     * Getter for the number of fieldsr.
     * @return the number of fields
     */
    public Integer getLayoutNumberOfFields() {
        return _numFields;
    }

    @Override
    public void setLayoutNumberOfFields(Integer numFields) {
        _numFields = numFields;
    }

    /**
     * Getter for the fields separator.
     * @return the fields separator
     */
    public char getSeparator() {
        return _separator;
    }

    /**
     * Setter for the fields separator.
     * @param separator fields separator
     */
    public void setSeparator(char separator) {
        _separator = separator;
    }

    /**
     * Getter for the ignore-first-line param.
     * @return the ignore-first-line param
     */
    public boolean ignoreFirstLine() {
        return _ignoreFirstLine;
    }

    /**
     * Setter for the ignore-first-line param.
     * @param ignoreFirstLine ignore-first-line param
     */
    public void setIgnoreFirstLine(boolean ignoreFirstLine) {
        _ignoreFirstLine = ignoreFirstLine;
    }

    @Override
    public CommaSeparatedField getFieldByName(String name) {
        return _cachedByName.get(name);
    }

    @Override
    public CommaSeparatedField getFieldByNaaccrItemNumber(Integer num) {
        return _cachedByNaaccrItemNumber.get(num);
    }

    @Override
    public List<CommaSeparatedField> getAllFields() {
        return Collections.unmodifiableList(_fields);
    }

    @Override
    public String validateLine(String line, Integer lineNumber) {
        StringBuilder msg = new StringBuilder();

        if (line == null || line.isEmpty())
            msg.append("line ").append(lineNumber).append(": line is empty");
        else {
            try {
                int numFields = new CSVParserBuilder().withSeparator(_separator).build().parseLine(line).length;
                if (numFields != _numFields)
                    msg.append("line ").append(lineNumber).append(": wrong number of fields, expected ").append(_numFields).append(" but got ").append(numFields);
            }
            catch (IOException e) {
                msg.append("line ").append(lineNumber).append(": unable to parse line");
            }
        }

        return msg.length() == 0 ? null : msg.toString();
    }

    @Override
    public String createLineFromRecord(Map<String, String> record, RecordLayoutOptions options) throws IOException {
        StringBuilder result = new StringBuilder();

        if (record == null)
            record = new HashMap<>();

        String[] values = new String[_numFields];
        for (CommaSeparatedField field : _fields) {
            String val = record.get(field.getName());
            if (val == null && field.getDefaultValue() != null)
                val = field.getDefaultValue();
            values[field.getIndex() - 1] = val;
        }

        for (String val : values) {
            if (val != null) {
                // this is following the specs from RFC4180 (https://tools.ietf.org/html/rfc4180)
                if (val.indexOf(_separator) > -1 || val.indexOf('\n') > -1 || val.indexOf('"') > -1)
                    result.append("\"").append(StringUtils.replace(val, "\"", "\"\"")).append("\"");
                else
                    result.append(val);
            }
            result.append(_separator);
        }

        // remove last extra comma
        if (result.length() > 1)
            result.setLength(result.length() - 1);

        return result.toString();
    }

    @Override
    public Map<String, String> createRecordFromLine(String line, Integer lineNumber, RecordLayoutOptions options) throws IOException {
        Map<String, String> result = new HashMap<>();

        Integer lineNumberSafe = lineNumber == null ? Integer.valueOf(1) : lineNumber;

        // handle special case
        if (line == null || line.isEmpty()) {
            if (enforceStrictFormat(options))
                throw new IOException("line " + lineNumberSafe + ": got en empty line");
            else
                return result;
        }

        // if we need to enforce the format, validate the line
        if (enforceStrictFormat(options)) {
            String validationMsg = validateLine(line, lineNumberSafe);
            if (validationMsg != null)
                throw new IOException(validationMsg);
        }

        // parse the line
        String[] values = new CSVParserBuilder().withSeparator(_separator).build().parseLine(line);

        for (CommaSeparatedField field : _fields) {
            int index = field.getIndex() - 1;

            if (index >= values.length)
                break;

            String value = values[index];
            String trimmedValue = value.trim();
            if (trimValues(options) && field.getTrim())
                value = trimmedValue;

            if (!value.isEmpty())
                result.put(field.getName(), value);
        }

        return result;
    }

    @Override
    public LayoutInfo buildFileInfo(String firstRecord, LayoutInfoDiscoveryOptions options) {
        LayoutInfo result = null;

        // this default implementation is based only on the number of fields, only if we don't enforce the strict format
        if (firstRecord != null && options.isCommaSeparatedAllowDiscoveryFromNumFields()) {
            try {
                if (new CSVParserBuilder().withSeparator(_separator).build().parseLine(firstRecord).length == _numFields) {
                    result = new LayoutInfo();
                    result.setLayoutId(getLayoutId());
                    result.setLayoutName(getLayoutName());
                    result.setNumFields(getLayoutNumberOfFields());
                }
            }
            catch (IOException e) {
                // ignored
            }
        }

        return result;
    }

    /**
     * Verify the internal fields, throws a runtime exception if something is wrong.
     * <p/>
     * Created on Jun 25, 2012 by depryf
     */
    public void verify() {

        // ID is required
        if (_layoutId == null)
            throw new RuntimeException("Layout ID is required");

        // name is required
        if (_layoutName == null)
            throw new RuntimeException("Layout name is required");

        // number of fields is required
        if (_numFields == null)
            throw new RuntimeException("Number of fields is required");

        // number of fields is required
        if (_separator == '\0')
            throw new RuntimeException("Separator is required");

        if (!_fields.isEmpty()) {

            Set<String> names = new HashSet<>(), naaccrItemNums = new HashSet<>(), shortLabels = new HashSet<>(), longLabels = new HashSet<>();
            Set<Integer> indexes = new HashSet<>();
            for (CommaSeparatedField field : _fields) {
                if (field.getName() == null)
                    throw new RuntimeException("Field name is required");
                if (names.contains(field.getName()))
                    throw new RuntimeException("Field name must be unique, found duplicate name for '" + field.getName() + "'");
                names.add(field.getName());
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
                if (field.getIndex() == null)
                    throw new RuntimeException("Field index is required");
                if (field.getIndex() <= 0)
                    throw new RuntimeException("Field index must be greater than zero");
                if (field.getIndex() > _numFields)
                    throw new RuntimeException("Field index must be smaller or equal to the defined number of fields");
                if (indexes.contains(field.getIndex()))
                    throw new RuntimeException("Field index must be unique");
                indexes.add(field.getIndex());
            }
        }
    }
}
