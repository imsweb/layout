/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.layout.record.fixed;

import java.io.IOException;
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

import com.imsweb.layout.Field;
import com.imsweb.layout.Field.FieldAlignment;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.LayoutUtils;
import com.imsweb.layout.record.RecordLayout;
import com.imsweb.layout.record.fixed.xml.FixedColumnLayoutFieldXmlDto;
import com.imsweb.layout.record.fixed.xml.FixedColumnLayoutXmlDto;

/**
 * This class contains the logic related to fixed-columns layouts.
 * <p/>
 * Created on Jul 28, 2011 by depryf
 * @author depryf
 */
public class FixedColumnsLayout extends RecordLayout {

    /**
     * Line length
     */
    protected Integer _layoutLineLength;

    /**
     * The fields for this layout
     */
    protected List<FixedColumnsField> _fields = new ArrayList<>();

    /**
     * Cached fields by name
     */
    protected Map<String, FixedColumnsField> _cachedByName = new HashMap<>();

    /**
     * Cached fields by NAACCR Item Number
     */
    protected Map<Integer, FixedColumnsField> _cachedByNaaccrItemNumber = new HashMap<>();

    public FixedColumnsLayout() {
        super();
    }

    public FixedColumnsLayout(FixedColumnLayoutXmlDto layoutXmlDto) throws IOException {
        this();
        init(layoutXmlDto);
    }

    public FixedColumnsLayout(URL layoutUrl) throws IOException {
        this();
        if (layoutUrl == null)
            throw new IOException("Unable to read from provided URL, URL is null");
        init(LayoutUtils.readFixedColumnsLayout(layoutUrl.openStream()));
    }

    protected void init(FixedColumnLayoutXmlDto layoutXmlDto) throws IOException {

        _layoutId = layoutXmlDto.getId();
        _layoutName = layoutXmlDto.getName();
        _layoutVersion = layoutXmlDto.getVersion();
        _layoutDesc = layoutXmlDto.getDescription();
        _layoutLineLength = layoutXmlDto.getLength();

        // are we extending another layout?
        FixedColumnsLayout parentLayout = null;
        if (layoutXmlDto.getExtendLayout() != null) {
            if (!LayoutFactory.getAvailableLayouts().containsKey(layoutXmlDto.getExtendLayout()))
                throw new IOException("Unable to find referenced layout ID '" + layoutXmlDto.getExtendLayout() + "'");
            try {
                parentLayout = (FixedColumnsLayout)LayoutFactory.getLayout(layoutXmlDto.getExtendLayout());
            }
            catch (ClassCastException e) {
                throw new IOException("A fixed-columns layout must extend another fixed-columns layout");
            }
            _parentLayoutId = parentLayout.getLayoutId();
        }

        _fields.clear();
        if (layoutXmlDto.getField() != null)
            for (FixedColumnLayoutFieldXmlDto fieldXmlDto : layoutXmlDto.getField())
                addField(createFieldFromXmlField(fieldXmlDto));
        if (parentLayout != null)
            for (FixedColumnsField field : parentLayout.getAllFields())
                if (!_cachedByName.containsKey(field.getName()))
                    addField(field);

        // sort the fields by start column
        Collections.sort(_fields, new Comparator<FixedColumnsField>() {
            @Override
            public int compare(FixedColumnsField f1, FixedColumnsField f2) {
                return f1.getStart().compareTo(f2.getStart());
            }
        });

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
     * @param dto <code>FixedColumnLayoutFieldXmlDto</code> to translate
     * @return the translated <code>Field</code>
     * @throws IOException
     */
    protected FixedColumnsField createFieldFromXmlField(FixedColumnLayoutFieldXmlDto dto) throws IOException {
        FixedColumnsField field = new FixedColumnsField();

        field.setName(dto.getName());
        field.setLongLabel(dto.getLongLabel());
        field.setShortLabel(dto.getShortLabel() != null ? dto.getShortLabel() : dto.getLongLabel());
        field.setNaaccrItemNum(dto.getNaaccrItemNum());
        field.setStart(dto.getStart());
        field.setEnd(dto.getEnd());
        if (dto.getStart() > dto.getEnd())
            throw new IOException("Field " + field.getName() + " defines a end column that is greater than its start column");
        if (dto.getAlign() != null) {
            try {
                field.setAlign(FieldAlignment.fromValue(dto.getAlign()));
            }
            catch (IllegalArgumentException e) {
                throw new IOException("Field " + field.getName() + " defines an invalid align option: " + dto.getAlign());
            }
        }
        else
            field.setAlign(FieldAlignment.LEFT);
        field.setPadChar(dto.getPadChar() == null ? " " : dto.getPadChar());
        field.setDefaultValue(dto.getDefaultValue());
        field.setTrim(dto.getTrim() == null ? Boolean.TRUE : dto.getTrim());
        if (dto.getField() != null && !dto.getField().isEmpty()) {
            List<FixedColumnsField> subFields = new ArrayList<>();
            for (FixedColumnLayoutFieldXmlDto childDto : dto.getField())
                subFields.add(createFieldFromXmlField(childDto));
            field.setSubFields(subFields);
        }
        field.setSection(dto.getSection());

        return field;
    }

    private void addField(FixedColumnsField field) {
        // update collection of fields
        _fields.add(field);
        // update name cache
        _cachedByName.put(field.getName(), field);
        if (field.getSubFields() != null)
            for (Field f : field.getSubFields())
                _cachedByName.put(f.getName(), (FixedColumnsField)f);
        // update NAACCR cache
        if (field.getNaaccrItemNum() != null)
            _cachedByNaaccrItemNumber.put(field.getNaaccrItemNum(), field);
        if (field.getSubFields() != null)
            for (Field f : field.getSubFields())
                if (f.getNaaccrItemNum() != null)
                    _cachedByNaaccrItemNumber.put(f.getNaaccrItemNum(), (FixedColumnsField)f);
    }

    public void setFields(Collection<FixedColumnsField> fields) {
        _fields.clear();
        _fields.addAll(fields);

        // sort the fields by start column
        Collections.sort(_fields, new Comparator<FixedColumnsField>() {
            @Override
            public int compare(FixedColumnsField f1, FixedColumnsField f2) {
                return f1.getStart().compareTo(f2.getStart());
            }
        });

        // verify they make sense
        verify();
    }

    /**
     * Getter for layout line length.
     * <p/>
     * Created on Jun 25, 2012 by depryf
     * @return the layout line length
     */
    public Integer getLayoutLineLength() {
        return _layoutLineLength;
    }

    /**
     * Setter for layout line length.
     * <p/>
     * Created on Jun 25, 2012 by depryf
     * @param lineLength length
     */
    public void setLayoutLineLength(Integer lineLength) {
        _layoutLineLength = lineLength;
    }

    @Override
    public FixedColumnsField getFieldByName(String name) {
        return _cachedByName.get(name);
    }

    @Override
    public FixedColumnsField getFieldByNaaccrItemNumber(Integer num) {
        return _cachedByNaaccrItemNumber.get(num);
    }

    @Override
    public List<FixedColumnsField> getAllFields() {
        return Collections.unmodifiableList(_fields);
    }

    /**
     * Returns the expected line length for the given data line.
     * <p/>
     * Created on Sep 16, 2011 by depryf
     * @param line data line
     * @return the expected line length for the given data line
     * @throws IOException
     */
    protected Integer getLengthFromLine(String line) throws IOException {
        return _layoutLineLength;
    }

    /**
     * Returns the expected line length for the given record.
     * <p/>
     * Created on Sep 16, 2011 by depryf
     * @param record record
     * @return the expected line length for the given record
     * @throws IOException
     */
    protected Integer getLengthFromRecord(Map<String, String> record) throws IOException {
        return _layoutLineLength;
    }

    @Override
    public String validateLine(String line, Integer lineNumber) {

        // if this layout extends another one, delegate to the other one...
        if (_parentLayoutId != null)
            return ((FixedColumnsLayout)LayoutFactory.getLayout(_parentLayoutId)).validateLine(line, lineNumber);

        StringBuilder msg = new StringBuilder();

        if (line == null || line.isEmpty())
            msg.append("line ").append(lineNumber).append(": line is empty");
        else if (line.length() != _layoutLineLength)
            msg.append("line ").append(lineNumber).append(": wrong length, expected ").append(_layoutLineLength).append(" but got ").append(line.length());

        return msg.length() == 0 ? null : msg.toString();
    }

    @Override
    public String createLineFromRecord(Map<String, String> record) throws IOException {
        StringBuilder result = new StringBuilder();

        if (record == null)
            record = new HashMap<>();

        Integer formatLength = getLengthFromRecord(record);

        int currentIndex = 1;
        for (FixedColumnsField field : _fields) {
            int start = field.getStart();
            int end = field.getEnd();

            // adjust for the "leading" gap
            if (start > currentIndex)
                for (int i = 0; i < start - currentIndex; i++)
                    result.append(' ');
            currentIndex = start;

            // get value; if the field defines subfields, always use the subfields (#162)
            if (field.getSubFields() != null && !field.getSubFields().isEmpty()) {
                for (Field child : field.getSubFields()) {
                    int subStart = ((FixedColumnsField)child).getStart();
                    int subEnd = ((FixedColumnsField)child).getEnd();

                    // adjust for the "leading" gap within the subfields
                    if (subStart > currentIndex)
                        for (int i = 0; i < subStart - currentIndex; i++)
                            result.append(' ');
                    currentIndex = subStart;

                    if (subEnd <= end) { // do not write the current subfield out if it can potentially go out of the field
                        String value = record.get(child.getName());
                        if (value == null)
                            value = child.getDefaultValue() != null ? child.getDefaultValue() : "";
                        int length = subEnd - subStart + 1;
                        if (value.length() > length)
                            throw new IOException("value too long for field '" + child.getName() + "'");
                        if (child.getAlign() == FieldAlignment.RIGHT)
                            value = LayoutUtils.pad(value, length, value.isEmpty() ? " " : child.getPadChar(), true);
                        else
                            value = LayoutUtils.pad(value, length, value.isEmpty() ? " " : child.getPadChar(), false);
                        result.append(cleanValue(value, child));
                        currentIndex = subEnd + 1;
                    }
                }

                // adjust for the "trailing" gap within the subfields
                if (currentIndex <= end)
                    for (int i = 0; i < end - currentIndex + 1; i++)
                        result.append(' ');
                currentIndex = end + 1;
            }
            else if (end <= formatLength) { // do not write the current field out if it can potentially go out of the line
                String value = record.get(field.getName());
                if (value == null)
                    value = field.getDefaultValue() != null ? field.getDefaultValue() : "";
                int length = end - start + 1;
                if (value.length() > length)
                    throw new IOException("value too long for field '" + field.getName() + "'");
                if (field.getAlign() == FieldAlignment.RIGHT)
                    value = LayoutUtils.pad(value, length, value.isEmpty() ? " " : field.getPadChar(), true);
                else
                    value = LayoutUtils.pad(value, length, value.isEmpty() ? " " : field.getPadChar(), false);
                result.append(cleanValue(value, field));
                currentIndex = end + 1;
            }
        }

        // adjust for the "leading" gap
        if (currentIndex <= formatLength)
            for (int i = 0; i < formatLength - currentIndex + 1; i++)
                result.append(' ');

        return result.toString();
    }

    /**
     * Clean the value for the given field; this default implementation does nothing but it delegates to the parent if this is an extending layout.
     */
    protected String cleanValue(String value, Field field) {

        // if this layout extends another one, delegate to the other one...
        if (_parentLayoutId != null)
            return ((FixedColumnsLayout)LayoutFactory.getLayout(_parentLayoutId)).cleanValue(value, field);

        return value;
    }

    @Override
    @SuppressWarnings("RedundantStringConstructorCall")
    public Map<String, String> createRecordFromLine(String line, Integer lineNumber, boolean trimValues, boolean enforceStrictFormat) throws IOException {
        Map<String, String> result = new HashMap<>();

        Integer lineNumberSafe = lineNumber == null ? Integer.valueOf(1) : lineNumber;

        // handle special case
        if (line == null || line.isEmpty()) {
            if (enforceStrictFormat)
                throw new IOException("line " + lineNumberSafe + ": got en empty line");
            else
                return result;
        }

        // if we need to enforce the format, get the expected line length; otherwise read until the EOL
        int formatLength = line.length();
        if (enforceStrictFormat) {
            String validationMsg = validateLine(line, lineNumberSafe);
            if (validationMsg != null)
                throw new IOException(validationMsg);
            formatLength = getLengthFromLine(line);
        }

        for (FixedColumnsField field : _fields) {
            int start = field.getStart();
            int end = field.getEnd();

            if (end > formatLength)
                break;

            // why are we instanciating a new String? http://blog.mikemccandless.com/2010/06/beware-stringsubstrings-memory-usage.html
            String value = new String(line.substring(start - 1, end));
            String trimmedValue = value.trim();
            if (trimValues && field.getTrim()) {
                // never trim a group field unless it's completely empty (or we would lose the info of which child value is which)
                if (field.getSubFields() == null || trimmedValue.isEmpty())
                    value = trimmedValue;
            }

            if (!value.isEmpty()) {
                result.put(field.getName(), value);

                // handle children fields if any
                if (field.getSubFields() != null) {
                    for (Field child : field.getSubFields()) {
                        start = ((FixedColumnsField)child).getStart();
                        end = ((FixedColumnsField)child).getEnd();

                        value = new String(line.substring(start - 1, end));
                        if (trimValues && child.getTrim())
                            value = value.trim();

                        if (!value.isEmpty()) {
                            result.put(child.getName(), value);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public LayoutInfo buildFileInfo(String firstRecord, LayoutInfoDiscoveryOptions options) {
        LayoutInfo result = null;

        // this default implementation is based only on the line length, only if we don't enforce the strict format
        if (firstRecord != null && options.isFixedColumnAllowDiscoveryFromLineLength() && firstRecord.length() == _layoutLineLength) {
            result = new LayoutInfo();
            result.setLayoutId(getLayoutId());
            result.setLayoutName(getLayoutName());
            result.setLineLength(_layoutLineLength);
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

        // line length is required
        if (_layoutLineLength == null)
            throw new RuntimeException("Line length is required");

        if (!_fields.isEmpty()) {

            // verify first field starts at 1 or greater
            if (_fields.get(0).getStart() <= 0)
                throw new RuntimeException("First field start column must be greater or equals to 1");

            // verify last field ends at line length or smaller
            if (_fields.get(_fields.size() - 1).getEnd() > _layoutLineLength)
                throw new RuntimeException("Last field end column must be smaller or equals to defined line length");

            // verify there is no overlapping
            for (int i = 0; i < _fields.size() - 1; i++) {
                FixedColumnsField f1 = _fields.get(i);
                FixedColumnsField f2 = _fields.get(i + 1);
                if (f1.getEnd() >= f2.getStart())
                    throw new RuntimeException("Fields " + f1.getName() + " and " + f2.getName() + " are overlapping");

                // also verify the subfields, only need to do this on f1
                if (f1.getSubFields() != null) {
                    List<FixedColumnsField> list = f1.getSubFields();
                    int size = list.size();
                    for (int j = 0; j < size; j++) {
                        FixedColumnsField ff = list.get(j);
                        if (j == 0 && ff.getStart() < f1.getStart())
                            throw new RuntimeException("Field " + f1.getName() + " defines a sub-field that starts before it");
                        if (j == size - 1 && ff.getEnd() > f1.getEnd())
                            throw new RuntimeException("Field " + f1.getName() + " defines a sub-field that ends after it");
                        if (j < size - 1 && ff.getEnd() >= list.get(j + 1).getStart())
                            throw new RuntimeException("Field " + f1.getName() + " defines overlapping subfields");
                    }
                }
            }

            Set<String> names = new HashSet<>(), naaccrItemNums = new HashSet<>(), shortLabels = new HashSet<>(), longLabels = new HashSet<>();
            for (FixedColumnsField field : _fields) {
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

                if (field.getSubFields() != null) {
                    for (Field f : field.getSubFields()) {
                        if (f.getName() == null)
                            throw new RuntimeException("Field name is required");
                        if (names.contains(f.getName()))
                            throw new RuntimeException("Field name must be unique, found duplicate name for '" + f.getName() + "'");
                        names.add(f.getName());
                        if (shortLabels.contains(f.getShortLabel()))
                            throw new RuntimeException("Field short labels must be unique, found duplicate name for '" + f.getShortLabel() + "'");
                        shortLabels.add(f.getShortLabel());
                        if (longLabels.contains(f.getLongLabel()))
                            throw new RuntimeException("Field long labels must be unique, found duplicate name for '" + f.getLongLabel() + "'");
                        longLabels.add(f.getLongLabel());
                        if (f.getNaaccrItemNum() != null) {
                            if (naaccrItemNums.contains(f.getNaaccrItemNum().toString()))
                                throw new RuntimeException("Field NAACCR item number must be unique, found duplicate number for '" + f.getNaaccrItemNum() + "'");
                            naaccrItemNums.add(f.getNaaccrItemNum().toString());
                        }
                    }
                }
            }
        }
    }
}
