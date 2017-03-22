/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

import com.imsweb.layout.hl7.entity.Hl7SubComponent;

@XStreamAlias("hl7-component")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"_value"})
public class Hl7ComponentDefinitionXmlDto {

    private String _value;

    @XStreamAsAttribute
    private Integer _index;

    @XStreamImplicit
    private List<Hl7SubComponent> _subComponents;

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    public Integer getIndex() {
        return _index;
    }

    public void setIndex(Integer index) {
        _index = index;
    }

    public List<Hl7SubComponent> getSubComponents() {
        if (_subComponents == null)
            _subComponents = new ArrayList<>();
        return _subComponents;
    }
}
