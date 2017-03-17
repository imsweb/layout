/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.util.HashMap;
import java.util.Map;

public class Hl7Component {

    private Hl7RepeatedField _repeatedField;

    private Integer _index;

    private Map<Integer, Hl7SubComponent> _subComponents;

    public Hl7Component() {
        _subComponents = new HashMap<>();
    }

    public Hl7RepeatedField getRepeatedField() {
        return _repeatedField;
    }

    public void setRepeatedField(Hl7RepeatedField repeatedField) {
        _repeatedField = repeatedField;
    }

    public Integer getIndex() {
        return _index;
    }

    public void setIndex(Integer index) {
        _index = index;
    }

    public Map<Integer, Hl7SubComponent> getSubComponents() {
        return _subComponents;
    }

    public void setSubComponents(Map<Integer, Hl7SubComponent> subComponents) {
        _subComponents = subComponents;
    }

    public void addSubComponent(Hl7SubComponent subComponent) {
        _subComponents.put(subComponent.getIndex(), subComponent);
    }

    public Hl7SubComponent getSubComponent(int subComponentIdx) {
        Hl7SubComponent subComponent = _subComponents.get(subComponentIdx);
        return subComponent == null ? new Hl7SubComponent() : subComponent;
    }

    public String getValue() {
        return getValue(1);
    }

    public String getValue(Integer subComponentIdx) {
        return getSubComponent(subComponentIdx).getValue();
    }
}
