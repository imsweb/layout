/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("field")
public class Hl7FieldDefinitionXmlDto {

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

    @XStreamAlias("min-occurrence")
    @XStreamAsAttribute
    private String _minOccurrence;

    @XStreamAlias("max-occurrence")
    @XStreamAsAttribute
    private String _maxOccurrence;

    @XStreamImplicit
    private List<Hl7ComponentDefinitionXmlDto> _hl7Components;

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

    public String getMinOccurrence() {
        return _minOccurrence;
    }

    public void setMinOccurrence(String minOccurrence) {
        _minOccurrence = minOccurrence;
    }

    public String getMaxOccurrence() {
        return _maxOccurrence;
    }

    public void setMaxOccurrence(String maxOccurrence) {
        _maxOccurrence = maxOccurrence;
    }

    public List<Hl7ComponentDefinitionXmlDto> getHl7Components() {
        if (_hl7Components == null)
            _hl7Components = new ArrayList<>();
        return _hl7Components;
    }
}
