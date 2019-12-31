/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.layout.record.csv;

import java.util.Objects;

import com.imsweb.layout.Field;

public class CommaSeparatedField extends Field {

    // field index (required)
    protected Integer _index;

    /**
     * Constructor.
     */
    public CommaSeparatedField() {
        super();
    }

    public Integer getIndex() {
        return _index;
    }

    public void setIndex(Integer index) {
        this._index = index;
    }

    @Override
    public String toString() {
        return "Field [name=" + _name + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommaSeparatedField that = (CommaSeparatedField)o;
        return Objects.equals(_name, that._name);
    }

    @Override
    public int hashCode() {
        return Objects.hash( _name);
    }
}
