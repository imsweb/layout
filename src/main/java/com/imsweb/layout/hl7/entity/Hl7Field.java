/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.util.ArrayList;
import java.util.List;

import com.imsweb.layout.hl7.Hl7Utils;

public class Hl7Field {

    // the parent segment
    private Hl7Segment _segment;

    // the field index
    private Integer _index;

    // the repeated fields in the order they appear in this field
    private List<Hl7RepeatedField> _repeatedFields;

    /**
     * Constructor
     * @param segment parent segment (can be null)
     * @param index field index (cannot be null)
     * @param values optional values to set on the field
     */
    public Hl7Field(Hl7Segment segment, Integer index, String... values) {
        if (index == null)
            throw new RuntimeException("Index is required");
        if (index < 1 || index > 99)
            throw new RuntimeException("Index must be between 1 and 99");
        _segment = segment;
        _index = index;
        _repeatedFields = new ArrayList<>();

        if (segment != null)
            segment.addField(this);

        if (values != null && values.length != 0) {
            Hl7RepeatedField repeatedField = new Hl7RepeatedField(this);
            for (int i = 0; i < values.length; i++)
                new Hl7Component(repeatedField, i + 1, values[i]);
        }
    }

    public Hl7Segment getSegment() {
        return _segment;
    }

    public void setSegment(Hl7Segment segment) {
        if (segment == null)
            throw new RuntimeException("Parent segment cannot be null");
        _segment = segment;
    }

    public Integer getIndex() {
        return _index;
    }

    public void setIndex(Integer index) {
        if (index == null)
            throw new RuntimeException("Index is required");
        if (index < 1 || index > 99)
            throw new RuntimeException("Index must be between 1 and 99");
        _index = index;
    }

    public List<Hl7RepeatedField> getRepeatedFields() {
        return _repeatedFields;
    }

    public void setRepeatedFields(List<Hl7RepeatedField> repeatedFields) {
        _repeatedFields = repeatedFields == null ? new ArrayList<>() : repeatedFields;
    }

    public void addRepeatedField(Hl7RepeatedField repeatedField) {
        _repeatedFields.add(repeatedField);
    }

    public Hl7RepeatedField getRepeatedField(int repeatedFieldIdx) {
        repeatedFieldIdx = repeatedFieldIdx - 1;
        if (repeatedFieldIdx < 0 || repeatedFieldIdx >= _repeatedFields.size())
            return new Hl7RepeatedField(null);
        Hl7RepeatedField result = _repeatedFields.get(repeatedFieldIdx);
        return result == null ? new Hl7RepeatedField(null) : result;
    }

    public Hl7Component getComponent(int componentIdx) {
        if (_repeatedFields.isEmpty())
            return new Hl7Component(null, componentIdx);
        return _repeatedFields.get(0).getComponent(componentIdx);
    }

    public String getValue() {
        String value = Hl7Utils.fieldToString(this, false);
        return value.isEmpty() ? null : value;
    }

    public String getValue(int repeatedFieldIdx) {
        return getRepeatedField(repeatedFieldIdx).getValue();
    }

    public String getValue(int repeatedFieldIdx, int componentIdx, int subComponentIdx) {
        return getRepeatedField(repeatedFieldIdx).getComponent(componentIdx).getSubComponent(subComponentIdx).getValue();
    }
}
