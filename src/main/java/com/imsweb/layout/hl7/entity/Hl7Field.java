/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.util.ArrayList;
import java.util.List;

public class Hl7Field {

    private Hl7Segment _segment;

    private Integer _index;

    private List<Hl7RepeatedField> _repeatedFields;

    public Hl7Field(Hl7Segment segment, Integer index) {
        if (index == null)
            throw new RuntimeException("Index is required");
        if (index < 1 || index > 99)
            throw new RuntimeException("Index must be between 1 and 99");
        _segment = segment;
        _index = index;
        _repeatedFields = new ArrayList<>();

        if (segment != null)
            segment.addField(this);
    }

    public Hl7Field(Hl7Segment segment, Integer index, String value) {
        this(segment, index);
        addRepeatedField(new Hl7RepeatedField(this, value));
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

    public Hl7RepeatedField addRepeatedField(Hl7RepeatedField repeatedField) {
        _repeatedFields.add(repeatedField);
        return repeatedField;
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
        Hl7Component result = _repeatedFields.get(0).getComponent(componentIdx);
        return result == null ? new Hl7Component(null, componentIdx) : result;
    }

    public String getValue() {
        String value = Hl7Utils.fieldToString(this);
        return value.isEmpty() ? null : value;
    }

    //    public List<Hl7Component> getComponents() {
    //        if (_repeatedFields.isEmpty())
    //            return Collections.emptyList();
    //        return _repeatedFields.get(0).getComponents();
    //    }
    //
    //    public Hl7Component getComponent(Integer index) {
    //        List<Hl7Component> components = getComponents();
    //        if (index == null || index < 0 || index >= components.size())
    //            return null;
    //        return components.get(index);
    //    }

    //    public String getValue(Integer repeatedFieldIdx) {
    //        if (_repeatedFields.isEmpty() || repeatedFieldIdx == null || repeatedFieldIdx < 0 || repeatedFieldIdx >= _repeatedFields.size())
    //            return null;
    //        return _repeatedFields.get(repeatedFieldIdx).getValue();
    //    }
    //
    //    public String getValue() {
    //        return getValue(0);
    //    }
}
