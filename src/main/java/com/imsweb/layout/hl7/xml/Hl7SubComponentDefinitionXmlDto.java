/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("subcomponent")
public class Hl7SubComponentDefinitionXmlDto {

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
}
