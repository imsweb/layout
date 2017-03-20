/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import com.imsweb.layout.Field;

public class NaaccrHl7Field extends Field {

    private String _identifier;

    private Integer _minOccurrence;

    private Integer _maxOccurrence;

    private String _type;

    public NaaccrHl7Field() {
        _minOccurrence = 0;
        _maxOccurrence = 1;
    }

    public String getIdentifier() {
        return _identifier;
    }

    public void setIdentifier(String identifier) {
        _identifier = identifier;
    }

    public Integer getMinOccurrence() {
        return _minOccurrence;
    }

    public void setMinOccurrence(Integer minOccurrence) {
        _minOccurrence = minOccurrence;
    }

    public Integer getMaxOccurrence() {
        return _maxOccurrence;
    }

    public void setMaxOccurrence(Integer maxOccurrence) {
        _maxOccurrence = maxOccurrence;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }
}
