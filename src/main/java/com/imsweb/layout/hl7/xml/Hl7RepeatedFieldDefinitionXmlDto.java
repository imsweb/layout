/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

import com.imsweb.layout.hl7.entity.Hl7Component;

@XStreamAlias("hl7-repeated-field")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"_value"})
public class Hl7RepeatedFieldDefinitionXmlDto {

    private String _value;

    @XStreamImplicit
    private List<Hl7Component> _hl7Components;

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    public List<Hl7Component> getHl7Components() {
        if (_hl7Components == null)
            _hl7Components = new ArrayList<>();
        return _hl7Components;
    }
}
