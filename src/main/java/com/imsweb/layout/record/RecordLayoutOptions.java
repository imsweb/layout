/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.layout.record;

public class RecordLayoutOptions {

    /**
     * When reading values, do we need to trim them (defaults to true).
     */
    protected boolean _trimValues;

    /**
     * When reading lines, do we need to check the correct number of fields are available or just use what we have (defaults to false).
     */
    protected boolean _enforceStrictFormat;

    /**
     * When writing values, do we need to apply the alignment field rules, or left-align all the time (defaults to true).
     */
    protected boolean _applyAlignment;

    /**
     * When writing values, do we need to apply the padding field rules, or use spaces all the time (defaults to true).
     */
    protected boolean _applyPadding;

    /**
     * Default Constructor.
     */
    public RecordLayoutOptions() {
        _trimValues = true;
        _enforceStrictFormat = false;
        _applyAlignment = true;
        _applyPadding = true;
    }

    /**
     * Copy Constructor.
     * @param options other options
     */
    public RecordLayoutOptions(RecordLayoutOptions options) {
        _trimValues = options.trimValues();
        _enforceStrictFormat = options.enforceStrictFormat();
        _applyAlignment = options.applyAlignment();
        _applyPadding = options.applyPadding();
    }

    public boolean trimValues() {
        return _trimValues;
    }

    public void setTrimValues(boolean trimValues) {
        _trimValues = trimValues;
    }

    public boolean enforceStrictFormat() {
        return _enforceStrictFormat;
    }

    public void setEnforceStrictFormat(boolean enforceStrictFormat) {
        _enforceStrictFormat = enforceStrictFormat;
    }

    public boolean applyAlignment() {
        return _applyAlignment;
    }

    public void setApplyAlignment(boolean applyAlignment) {
        _applyAlignment = applyAlignment;
    }

    public boolean applyPadding() {
        return _applyPadding;
    }

    public void setApplyPadding(boolean applyPadding) {
        _applyPadding = applyPadding;
    }
}
