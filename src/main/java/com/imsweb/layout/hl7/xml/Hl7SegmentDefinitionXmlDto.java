/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("segment")
public class Hl7SegmentDefinitionXmlDto {

    @XStreamAlias("identifier")
    @XStreamAsAttribute
    private String _identifier;

    @XStreamImplicit
    private List<Hl7FieldDefinitionXmlDto> _hl7Fields;

    public String getIdentifier() {
        return _identifier;
    }

    public void setIdentifier(String identifier) {
        _identifier = identifier;
    }

    public List<Hl7FieldDefinitionXmlDto> getHl7Fields() {
        if (_hl7Fields == null)
            _hl7Fields = new ArrayList<>();
        return _hl7Fields;
    }
}
