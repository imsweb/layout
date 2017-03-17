/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.util.HashMap;
import java.util.Map;

public class Hl7RepeatedField {

    private Hl7Field _field;

    private Map<Integer, Hl7Component> _components;

    public Hl7RepeatedField() {
        _components = new HashMap<>();
    }

    public Hl7Field getField() {
        return _field;
    }

    public void setField(Hl7Field field) {
        _field = field;
    }

    public Map<Integer, Hl7Component> getComponents() {
        return _components;
    }

    public void setComponents(Map<Integer, Hl7Component> components) {
        _components = components;
    }

    public void addComponent(Hl7Component component) {
        _components.put(component.getIndex(), component);
    }

    public Hl7Component getComponent(int componentIdx) {
        Hl7Component component = _components.get(componentIdx);
        return component == null ? new Hl7Component() : component;
    }
}
