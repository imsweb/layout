/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

public class Hl7SubComponent {

    // the parent component
    private Hl7Component _component;

    // the sub-component index
    private Integer _index;

    // the sub-component value
    private String _value;

    /**
     * Constructor.
     * @param component parent component (can be null)
     * @param index the sub-component index (cannot be null)
     * @param value the sub-component value (can be null)
     */
    public Hl7SubComponent(Hl7Component component, Integer index, String value) {
        if (index == null)
            throw new RuntimeException("Index is required");
        if (index < 1 || index > 999)
            throw new RuntimeException("Index must be between 1 and 999");
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
        if (index < 1 || index > 999)
            throw new RuntimeException("Index must be between 1 and 999");
        _index = index;
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }
}
