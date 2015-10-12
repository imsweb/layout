/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.layout.record.fixed;

import java.util.List;

import com.imsweb.layout.Field;

public class FixedColumnsField extends Field {

    /**
     * Start column (required for fixed length layouts)
     */
    protected Integer _start;

    /**
     * End column (required for fixed length layout)
     */
    protected Integer _end;

    /**
     * Sub-group fields (will be null if this field is not a group; comma separated cannot have groups)
     */
    protected List<FixedColumnsField> _subFields;

    /**
     * @return Returns the start.
     */
    public Integer getStart() {
        return _start;
    }

    /**
     * @param start The start to set.
     */
    public void setStart(Integer start) {
        this._start = start;
    }

    /**
     * @return Returns the end.
     */
    public Integer getEnd() {
        return _end;
    }

    /**
     * @param end The end to set.
     */
    public void setEnd(Integer end) {
        this._end = end;
    }

    /**
     * @return Returns the subFields.
     */
    public List<FixedColumnsField> getSubFields() {
        return _subFields;
    }

    /**
     * @param subFields The subFields to set.
     */
    public void setSubFields(List<FixedColumnsField> subFields) {
        this._subFields = subFields;
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
