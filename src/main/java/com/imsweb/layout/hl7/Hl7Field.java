/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hl7Field {

    private Hl7Segment _segment;

    private Integer _index;

    private List<Hl7FieldValue> _values;

    public Hl7Field() {
        _values = new ArrayList<>();
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

    public List<Hl7FieldValue> getValues() {
        return _values;
    }

    public void setValues(List<Hl7FieldValue> values) {
        _values = values;
    }

    public Hl7FieldValue withRepeatedValue() {
        Hl7FieldValue fieldValue = new Hl7FieldValue();
        fieldValue.setField(this);
        _values.add(fieldValue);
        return fieldValue;
    }

    public Hl7Component withComponent(Integer index, String... values) {
        Hl7FieldValue fieldValue = new Hl7FieldValue();
        fieldValue.setField(this);
        _values.add(fieldValue);

        Hl7Component component = new Hl7Component();
        component.setFieldValue(fieldValue);
        component.setIndex(index);
        fieldValue.getComponents().add(component);

        if (values != null) {
            for (int subCompIdx = 0; subCompIdx < values.length; subCompIdx++) {
                Hl7SubComponent subComponent = new Hl7SubComponent();
                subComponent.setComponent(component);
                subComponent.setIndex(subCompIdx);
                subComponent.setValue(values[subCompIdx]);
                component.getSubComponents().add(subComponent);
            }
        }

        return component;
    }
    
    public List<Hl7Component> getComponents() {
        if (_values.isEmpty())
            return Collections.emptyList();
        return _values.get(0).getComponents();
    }
    
    public Hl7Component getComponent(Integer index) {
        List<Hl7Component> components = getComponents();
        if (index == null || index < 0 || index >= components.size())
            return null;
        return components.get(index);
    }

    public String getValue(Integer repeatedValueIdx) {
        if (_values.isEmpty() || repeatedValueIdx == null || repeatedValueIdx < 0 || repeatedValueIdx >= _values.size())
            return null;
        return _values.get(repeatedValueIdx).getValue();
    }
    
    public String getValue() {
        return getValue(0);
    }
}
