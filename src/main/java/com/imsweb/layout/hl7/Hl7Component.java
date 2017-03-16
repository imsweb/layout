/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.util.ArrayList;
import java.util.List;

public class Hl7Component {

    private Hl7FieldValue _fieldValue;

    private Integer _index;

    private List<Hl7SubComponent> _subComponents;

    public Hl7Component() {
        _subComponents = new ArrayList<>();
    }
    
    public Hl7FieldValue getFieldValue() {
        return _fieldValue;
    }

    public void setFieldValue(Hl7FieldValue fieldValue) {
        _fieldValue = fieldValue;
    }

    public Integer getIndex() {
        return _index;
    }

    public void setIndex(Integer index) {
        _index = index;
    }

    public List<Hl7SubComponent> getSubComponents() {
        return _subComponents;
    }

    public void setSubComponents(List<Hl7SubComponent> subComponents) {
        _subComponents = subComponents;
    }

    public Hl7SubComponent withSubComponent(Integer index) {
        Hl7SubComponent subComponent = new Hl7SubComponent();
        subComponent.setComponent(this);
        subComponent.setIndex(index);
        _subComponents.add(subComponent);
        return subComponent;
    }

    public Hl7SubComponent withSubComponent(Integer index, String value) {
        Hl7SubComponent subComponent = new Hl7SubComponent();
        subComponent.setComponent(this);
        subComponent.setIndex(index);
        subComponent.setValue(value);
        _subComponents.add(subComponent);
        return subComponent;
    }

    public Hl7Component withComponent(Integer index, String... values) {
        Hl7Component component = new Hl7Component();
        component.setFieldValue(_fieldValue);
        component.setIndex(index);
        _fieldValue.getComponents().add(component);

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

    public Hl7FieldValue withRepeatedValue() {
        Hl7FieldValue fieldValue = new Hl7FieldValue();
        fieldValue.setField(getFieldValue().getField());
        getFieldValue().getField().getValues().add(fieldValue);
        return fieldValue;
    }

    public String getValue(Integer subComponentIdx) {
        if (_subComponents.isEmpty() || subComponentIdx == null || subComponentIdx < 0 || subComponentIdx >= _subComponents.size())
            return null;
        return _subComponents.get(subComponentIdx).getValue();
    }
    
    public String getValue() {
       return getValue(0);
    }
    
    public Hl7Message build() {
        return getFieldValue().getField().getSegment().getMessage();
    }
}
