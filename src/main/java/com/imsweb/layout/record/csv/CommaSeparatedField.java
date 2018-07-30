/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.layout.record.csv;

import com.imsweb.layout.Field;

public class CommaSeparatedField extends Field {

    /**
     * Field index (required for comma separated layouts)
     */
    protected Integer _index;

    /**
     * Field max-length (optional, applicable for comma separated layouts only)
     */
    protected Integer _maxLength;

    /**
     * Constructor.
     */
    public CommaSeparatedField() {
        super();
    }

    /**
     * @return Returns the index.
     */
    public Integer getIndex() {
        return _index;
    }

    /**
     * @param index The index to set.
     */
    public void setIndex(Integer index) {
        this._index = index;
    }

    /**
     * @return Returns the max length.
     */
    public Integer getMaxLength() {
        return _maxLength;
    }

    /**
     * @param maxLength The max length to set.
     */
    public void setMaxLength(Integer maxLength) {
        this._maxLength = maxLength;
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
        if (_name == null)
            return other.getName() == null;
        return _name.equals(other.getName());
    }
}
