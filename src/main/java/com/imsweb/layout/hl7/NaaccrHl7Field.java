/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.util.List;
import java.util.Objects;

import com.imsweb.layout.Field;

public class NaaccrHl7Field extends Field {

    private String _identifier;

    private String _type;

    private Integer _minOccurrence;

    private Integer _maxOccurrence;

    private List<NaaccrHl7Field> _subFields;

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

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
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

    public List<NaaccrHl7Field> getSubFields() {
        return _subFields;
    }

    public void setSubFields(List<NaaccrHl7Field> subFields) {
        _subFields = subFields;
    }

    @Override
    public String toString() {
        return _identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NaaccrHl7Field)) return false;
        if (!super.equals(o)) return false;
        NaaccrHl7Field that = (NaaccrHl7Field)o;
        return Objects.equals(_identifier, that._identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _identifier);
    }
}
