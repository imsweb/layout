/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

public class Hl7SubComponent {

    private Hl7Component _component;

    private Integer _index;

    private String _value;

    public Hl7Component getComponent() {
        return _component;
    }

    public void setComponent(Hl7Component component) {
        _component = component;
    }

    public Integer getIndex() {
        return _index;
    }

    public void setIndex(Integer index) {
        _index = index;
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    public void withValue(String value) {
        _value = value;
    }
}
