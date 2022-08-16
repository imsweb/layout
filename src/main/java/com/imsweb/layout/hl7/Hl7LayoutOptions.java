/*
 * Copyright (C) 2020 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class Hl7LayoutOptions {

    // the different options for new lines
    public static final String NEW_LINE_OS = System.lineSeparator();
    public static final String NEW_LINE_LF = "\n";
    public static final String NEW_LINE_CRLF = "\r\n";

    // when reading messages, whether segments with an invalid ID should be skipped instead of raising an exception (defaults to true)
    protected boolean _skipInvalidSegmentIds;

    // when writing messages, the flavor of new lines to use (defaults to the OS one)
    protected String _lineSeparator;

    // when reading and writing messages, the encoding to use (defaults to UTF-8)
    protected Charset _encoding;

    /**
     * Default Constructor.
     */
    public Hl7LayoutOptions() {
        _skipInvalidSegmentIds = true;
        _lineSeparator = NEW_LINE_OS;
        _encoding = StandardCharsets.UTF_8;
    }

    public String getLineSeparator() {
        return _lineSeparator;
    }

    public void setLineSeparator(String lineSeparator) {
        if (!NEW_LINE_OS.equals(lineSeparator) && !NEW_LINE_LF.equals(lineSeparator) && !NEW_LINE_CRLF.equals(lineSeparator))
            throw new IllegalStateException("Invalid new line separator option: " + lineSeparator);
        _lineSeparator = lineSeparator;
    }

    public boolean skipInvalidSegmentIds() {
        return _skipInvalidSegmentIds;
    }

    public void setSkipInvalidSegmentIds(boolean skipInvalidSegmentIds) {
        _skipInvalidSegmentIds = skipInvalidSegmentIds;
    }

    public Charset getEncoding() {
        return _encoding;
    }

    public void setEncoding(Charset encoding) {
        _encoding = encoding;
    }
}
