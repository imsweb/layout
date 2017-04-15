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
public class Hl7SegmentXmlDto {

    @XStreamAlias("identifier")
    @XStreamAsAttribute
    private String _identifier;

    @XStreamImplicit
    private List<Hl7FieldXmlDto> _hl7Fields;

    public String getIdentifier() {
        return _identifier;
    }

    public void setIdentifier(String identifier) {
        _identifier = identifier;
    }

    public List<Hl7FieldXmlDto> getHl7Fields() {
        if (_hl7Fields == null)
            _hl7Fields = new ArrayList<>();
        return _hl7Fields;
    }

    public void setHl7Fields(List<Hl7FieldXmlDto> hl7Fields) {
        _hl7Fields = hl7Fields;
    }
}
