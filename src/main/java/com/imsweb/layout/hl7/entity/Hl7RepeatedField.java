/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.util.HashMap;
import java.util.Map;

import com.imsweb.layout.hl7.Hl7Utils;

public class Hl7RepeatedField {

    // the parent field
    private Hl7Field _field;

    // the components has they appear in the repeated field
    private Map<Integer, Hl7Component> _components;

    /**
     * Constructor.
     * @param field parent field (can be null)
     * @param values optional values to set on the repeated field
     */
    public Hl7RepeatedField(Hl7Field field, String... values) {
        _field = field;
        _components = new HashMap<>();

        if (field != null)
            field.addRepeatedField(this);

        if (values != null && values.length != 0)
            for (String value : values)
                new Hl7Component(this, 1, value);
    }

    public Hl7Field getField() {
        return _field;
    }

    public void setField(Hl7Field field) {
        if (field == null)
            throw new RuntimeException("Parent field cannot be null");
        _field = field;
    }

    public Map<Integer, Hl7Component> getComponents() {
        return _components;
    }

    public void setComponents(Map<Integer, Hl7Component> components) {
        _components = components == null ? new HashMap<>() : components;
    }

    public void addComponent(Hl7Component component) {
        _components.put(component.getIndex(), component);
    }

    public Hl7Component getComponent(int componentIdx) {
        Hl7Component result = _components.get(componentIdx);
        return result == null ? new Hl7Component(null, componentIdx) : result;
    }

    public String getValue() {
        String value = Hl7Utils.repeatedFieldToString(this);
        return value.isEmpty() ? null : value;
    }

    public String getValue(int componentIdx) {
        return getComponent(componentIdx).getValue();
    }

    public String getValue(int componentIdx, int subComponentIdx) {
        return getComponent(componentIdx).getSubComponent(subComponentIdx).getValue();
    }
}
