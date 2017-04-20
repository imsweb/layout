/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.util.HashMap;
import java.util.Map;

import com.imsweb.layout.hl7.Hl7Utils;

public class Hl7Component {

    // the parent repeated field
    private Hl7RepeatedField _repeatedField;

    // the component index
    private Integer _index;

    // the list of sub-components in the order they appear in this component
    private Map<Integer, Hl7SubComponent> _subComponents;

    /**
     * Constructor.
     * @param repeatedField parent repeated field (can be null)
     * @param index component index (cannot be null)
     * @param values optional values to set on this component
     */
    public Hl7Component(Hl7RepeatedField repeatedField, Integer index, String... values) {
        if (index == null)
            throw new RuntimeException("Index is required");
        if (index < 1 || index > 99)
            throw new RuntimeException("Index must be between 1 and 99");
        _repeatedField = repeatedField;
        _index = index;
        _subComponents = new HashMap<>();

        if (repeatedField != null)
            repeatedField.addComponent(this);

        if (values != null && values.length != 0)
            for (int i = 0; i < values.length; i++)
                new Hl7SubComponent(this, i + 1, values[i]);
    }

    public Hl7RepeatedField getRepeatedField() {
        return _repeatedField;
    }

    public void setRepeatedField(Hl7RepeatedField repeatedField) {
        if (repeatedField == null)
            throw new RuntimeException("Parent repeated field cannot be null");
        _repeatedField = repeatedField;
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

    public Map<Integer, Hl7SubComponent> getSubComponents() {
        return _subComponents;
    }

    public void setSubComponents(Map<Integer, Hl7SubComponent> subComponents) {
        _subComponents = subComponents == null ? new HashMap<>() : subComponents;
    }

    public void addSubComponent(Hl7SubComponent subComponent) {
        _subComponents.put(subComponent.getIndex(), subComponent);
    }

    public Hl7SubComponent getSubComponent(int subComponentIdx) {
        Hl7SubComponent result = _subComponents.get(subComponentIdx);
        return result == null ? new Hl7SubComponent(null, subComponentIdx, null) : result;
    }

    public String getValue() {
        String value = Hl7Utils.componentToString(this);
        return value.isEmpty() ? null : value;
    }

    public String getValue(int subComponentIdx) {
        return getSubComponent(subComponentIdx).getValue();
    }
}
