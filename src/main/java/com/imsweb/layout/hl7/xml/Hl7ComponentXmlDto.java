/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("component")
public class Hl7ComponentXmlDto {

    @XStreamAlias("name")
    @XStreamAsAttribute
    private String _name;

    @XStreamAlias("identifier")
    @XStreamAsAttribute
    private String _identifier;

    @XStreamAlias("long-label")
    @XStreamAsAttribute
    private String _longLabel;

    @XStreamAlias("type")
    @XStreamAsAttribute
    private String _type;

    @XStreamImplicit
    private List<Hl7SubComponentXmlDto> _hl7SubComponents;

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getIdentifier() {
        return _identifier;
    }

    public void setIdentifier(String identifier) {
        _identifier = identifier;
    }

    public String getLongLabel() {
        return _longLabel;
    }

    public void setLongLabel(String longLabel) {
        _longLabel = longLabel;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public List<Hl7SubComponentXmlDto> getHl7SubComponents() {
        if (_hl7SubComponents == null)
            _hl7SubComponents = new ArrayList<>();
        return _hl7SubComponents;
    }

    public void setHl7SubComponents(List<Hl7SubComponentXmlDto> hl7SubComponents) {
        _hl7SubComponents = hl7SubComponents;
    }
}
