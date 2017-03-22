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

import com.imsweb.layout.hl7.entity.Hl7Field;

@XStreamAlias("hl7-segment")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"_value"})
public class Hl7SegmentDefinitionXmlDto {

    private String _value;

    @XStreamAsAttribute
    private String _id;

    @XStreamImplicit
    private List<Hl7Field> _hl7Fields;

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public List<Hl7Field> getHl7Fields() {
        if (_hl7Fields == null)
            _hl7Fields = new ArrayList<>();
        return _hl7Fields;
    }
}
