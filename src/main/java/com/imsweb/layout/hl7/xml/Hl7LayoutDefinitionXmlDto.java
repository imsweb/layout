/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import com.imsweb.layout.hl7.entity.Hl7Segment;

@XStreamAlias("hl7-layout")
public class Hl7LayoutDefinitionXmlDto {

    @XStreamAsAttribute
    private String id;

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private String version;

    @XStreamAsAttribute
    private String description;

    @XStreamImplicit
    private List<Hl7Segment> _hl7Segments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Hl7Segment> getHl7Segments() {
        if (_hl7Segments == null)
            _hl7Segments = new ArrayList<>();
        return _hl7Segments;
    }
}
