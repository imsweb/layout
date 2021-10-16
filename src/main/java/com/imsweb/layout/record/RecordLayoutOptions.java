/*
 * Copyright (C) 2016 Information Management Services, Inc.
 */
package com.imsweb.layout.record;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RecordLayoutOptions {

    // the different options for new lines
    public static final String NEW_LINE_OS = System.lineSeparator();
    public static final String NEW_LINE_LF = "\n";
    public static final String NEW_LINE_CRLF = "\r\n";

    // the different options for dealing with values too long
    public static final String VAL_TOO_LONG_EXCEPTION = "exception";
    public static final String VAL_TOO_LONG_NULLIFY = "nullify";
    public static final String VAL_TOO_LONG_CUTOFF = "cutoff";

    // when reading values, do we need to trim them (defaults to true)
    protected boolean _trimValues;

    // when reading records, do we need to check that the correct number of fields are available or just use what we have (defaults to false)
    protected boolean _enforceStrictFormat;

    // when writing records, do we need to apply the alignment field rules, or left-align all the time (defaults to true)
    protected boolean _applyAlignment;

    // when writing records, do we need to apply the padding field rules, or use spaces all the time (defaults to true)
    protected boolean _applyPadding;

    // when writing records, the flavor of new lines to use (defaults to the OS one)
    protected String _lineSeparator;

    // when reading and writing records, the encoding to use (defaults to UTF-8)
    protected Charset _encoding;

    // when writing records, do we need to force every value to be quoted; applicable to CSV layouts only (defaults to false meaning we quote when needed)
    protected boolean _quoteAllValues;

    // when writing flat records, how to deal with a value too long (defaults to throwing an exception)
    protected String _valueTooLongHandling;

    /**
     * Default Constructor.
     */
    public RecordLayoutOptions() {
        _trimValues = true;
        _enforceStrictFormat = false;
        _applyAlignment = true;
        _applyPadding = true;
        _lineSeparator = NEW_LINE_OS;
        _encoding = StandardCharsets.UTF_8;
        _quoteAllValues = false;
        _valueTooLongHandling = VAL_TOO_LONG_EXCEPTION;
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

    public String getLineSeparator() {
        return _lineSeparator;
    }

    public void setLineSeparator(String lineSeparator) {
        if (!NEW_LINE_OS.equals(lineSeparator) && !NEW_LINE_LF.equals(lineSeparator) && !NEW_LINE_CRLF.equals(lineSeparator))
            throw new RuntimeException("Invalid new line separator option: " + lineSeparator);
        _lineSeparator = lineSeparator;
    }

    public Charset getEncoding() {
        return _encoding;
    }

    public void setEncoding(Charset encoding) {
        _encoding = encoding;
    }

    public boolean quoteAllValues() {
        return _quoteAllValues;
    }

    public void setQuoteAllValues(boolean quoteAllValues) {
        _quoteAllValues = quoteAllValues;
    }

    public String getValueTooLongHandling() {
        return _valueTooLongHandling;
    }

    public void setValueTooLongHandling(String valueTooLongHandling) {
        _valueTooLongHandling = valueTooLongHandling;
    }
}
