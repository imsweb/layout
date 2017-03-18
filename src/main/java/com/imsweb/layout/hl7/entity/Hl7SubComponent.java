/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

public class Hl7SubComponent {

    private Hl7Component _component;

    private Integer _index;

    private String _value;

    public Hl7SubComponent(Hl7Component component, Integer index, String value) {
        if (index == null)
            throw new RuntimeException("Index is required");
        if (index < 1 || index > 99)
            throw new RuntimeException("Index must be between 1 and 99");
        _component = component;
        _index = index;
        _value = value;

        if (component != null)
            component.addSubComponent(this);
    }

    public Hl7Component getComponent() {
        return _component;
    }

    public void setComponent(Hl7Component component) {
        if (component == null)
            throw new RuntimeException("Parent component cannot be null");
        _component = component;
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

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }
}
