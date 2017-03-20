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

    @Override
    public String toString() {
        return "Field [name=" + _name + "]";
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((_name == null) ? 0 : _name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Field other = (Field)obj;
        if (_name == null) {
            if (other.getName() != null)
                return false;
        }
        else if (!_name.equals(other.getName()))
            return false;
        return true;
    }
}
