/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.layout.record.csv;

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
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
