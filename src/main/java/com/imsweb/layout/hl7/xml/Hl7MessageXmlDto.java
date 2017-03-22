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

import com.imsweb.layout.hl7.entity.Hl7Segment;

@XStreamAlias("hl7-message")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"_value"})
public class Hl7MessageXmlDto {

    private String _value;

    @XStreamImplicit
    private List<Hl7Segment> _hl7Segments;

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    public List<Hl7Segment> getHl7Segments() {
        if (_hl7Segments == null)
            _hl7Segments = new ArrayList<>();
        return _hl7Segments;
    }
}
