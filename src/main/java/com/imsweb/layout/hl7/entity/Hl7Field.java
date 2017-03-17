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

    public Hl7Field() {
        _repeatedFields = new ArrayList<>();
    }
    
    public Hl7Segment getSegment() {
        return _segment;
    }

    public void setSegment(Hl7Segment segment) {
        _segment = segment;
    }

    public Integer getIndex() {
        return _index;
    }

    public void setIndex(Integer index) {
        _index = index;
    }

    public List<Hl7RepeatedField> getRepeatedFields() {
        return _repeatedFields;
    }

    public void setRepeatedFields(List<Hl7RepeatedField> repeatedFields) {
        _repeatedFields = repeatedFields;
    }
    
    public void addRepeatedField(Hl7RepeatedField repeatedField) {
        _repeatedFields.add(repeatedField);
    }
    
    public Hl7RepeatedField getRepeatedField(int repeatedFieldIdx) {
        repeatedFieldIdx = repeatedFieldIdx - 1;
        if (repeatedFieldIdx < 0 || repeatedFieldIdx >= _repeatedFields.size())
            return null;
        return _repeatedFields.get(repeatedFieldIdx);
    }

    public Hl7Component getComponent(int componentIdx) {
        if (_repeatedFields.isEmpty())
            return new Hl7Component();
        return _repeatedFields.get(0).getComponent(componentIdx);
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
//
//    public String getValue(Integer repeatedValueIdx) {
//        if (_repeatedFields.isEmpty() || repeatedValueIdx == null || repeatedValueIdx < 0 || repeatedValueIdx >= _repeatedFields.size())
//            return null;
//        return _repeatedFields.get(repeatedValueIdx).getValue();
//    }
//    
//    public String getValue() {
//        return getValue(0);
//    }
}
