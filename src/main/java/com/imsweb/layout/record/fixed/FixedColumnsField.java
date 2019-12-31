/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.layout.record.fixed;

import java.util.List;

import com.imsweb.layout.Field;

public class FixedColumnsField extends Field {

    // start column (required)
    protected Integer _start;

    // end column (required)
    protected Integer _end;

    // sub-group fields (will be null if this field is not a group)
    protected List<FixedColumnsField> _subFields;

    /**
     * Constructor.
     */
    public FixedColumnsField() {
        super();
    }

    public Integer getStart() {
        return _start;
    }

    public void setStart(Integer start) {
        this._start = start;
    }

    public Integer getEnd() {
        return _end;
    }

    public void setEnd(Integer end) {
        this._end = end;
    }

    public List<FixedColumnsField> getSubFields() {
        return _subFields;
    }

    public void setSubFields(List<FixedColumnsField> subFields) {
        this._subFields = subFields;
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
