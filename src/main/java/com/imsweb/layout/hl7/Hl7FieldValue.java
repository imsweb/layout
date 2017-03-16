/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.util.ArrayList;
import java.util.List;

public class Hl7FieldValue {

    private Hl7Field _field;
    
    private List<Hl7Component> _components;

    public Hl7FieldValue() {
        _components = new ArrayList<>();
    }
    
    public Hl7Field getField() {
        return _field;
    }

    public void setField(Hl7Field field) {
        _field = field;
    }

    public List<Hl7Component> getComponents() {
        return _components;
    }

    public void setComponents(List<Hl7Component> components) {
        _components = components;
    }

    public Hl7Component withComponent(Integer index, String... values) {
        Hl7Component component = new Hl7Component();
        component.setFieldValue(this);
        component.setIndex(index);
        _components.add(component);

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
    
    public String getValue(Integer componentIdx) {
        if (_components.isEmpty() || componentIdx == null || componentIdx < 0 || componentIdx >= _components.size())
            return null;
        return _components.get(componentIdx).getValue();
    }

    public String getValue() {
        return getValue(0);
    }
}
