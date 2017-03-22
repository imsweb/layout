/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XStreamAlias("hl7-sub-component")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"_value"})
public class Hl7SubComponentDefinitionXmlDto {

    private String _value;

    @XStreamAsAttribute
    private Integer _index;

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
}
