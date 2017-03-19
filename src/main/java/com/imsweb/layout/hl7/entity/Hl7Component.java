/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.util.HashMap;
import java.util.Map;

import com.imsweb.layout.hl7.Hl7Utils;

public class Hl7Component {

    private Hl7RepeatedField _repeatedField;

    private Integer _index;

    private Map<Integer, Hl7SubComponent> _subComponents;

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

        if (values != null)
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

    public Hl7SubComponent addSubComponent(Hl7SubComponent subComponent) {
        _subComponents.put(subComponent.getIndex(), subComponent);
        return subComponent;
    }

    public Hl7SubComponent getSubComponent(int subComponentIdx) {
        Hl7SubComponent result = _subComponents.get(subComponentIdx);
        return result == null ? new Hl7SubComponent(null, subComponentIdx, null) : result;
    }

    public String getValue() {
        String value = Hl7Utils.componentToString(this);
        return value.isEmpty() ? null : value;
    }
}
