/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.xml;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("hl7-layout")
public class Hl7LayoutXmlDto {

    @XStreamAlias("id")
    @XStreamAsAttribute
    private String _id;

    @XStreamAlias("name")
    @XStreamAsAttribute
    private String _name;

    @XStreamAlias("version")
    @XStreamAsAttribute
    private String _version;

    @XStreamAlias("description")
    @XStreamAsAttribute
    private String _description;

    @XStreamImplicit
    private List<Hl7SegmentXmlDto> _hl7Segments;

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getVersion() {
        return _version;
    }

    public void setVersion(String version) {
        _version = version;
    }

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public List<Hl7SegmentXmlDto> getHl7Segments() {
        if (_hl7Segments == null)
            _hl7Segments = new ArrayList<>();
        return _hl7Segments;
    }

    public void setHl7Segments(List<Hl7SegmentXmlDto> hl7Segments) {
        _hl7Segments = hl7Segments;
    }
}
