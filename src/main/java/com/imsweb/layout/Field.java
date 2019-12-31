/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.layout;

import java.util.Objects;

/**
 * A single field from the layout.
 * <p/>
 * Created on Aug 16, 2011 by depryf
 * @author depryf
 */
public class Field {

    /**
     * Possible values for the alignment attribute of the field.
     * <p/>
     * Created on Aug 16, 2011 by depryf
     * @author depryf
     */
    public enum FieldAlignment {

        // left alignment
        LEFT("left"),

        // right alignment
        RIGHT("right");

        private final String _value;

        FieldAlignment(String v) {
            _value = v;
        }

        /**
         * Returns the value.
         * <p/>
         * Created on Jul 29, 2011 by depryf
         * @return the value
         */
        public String value() {
            return _value;
        }

        /**
         * Returns the type.
         * <p/>
         * Created on Jul 29, 2011 by depryf
         * @param v string
         * @return the type
         */
        public static FieldAlignment fromValue(String v) {
            for (FieldAlignment c : FieldAlignment.values()) {
                if (c._value.equals(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }
    }

    // field name (required)
    protected String _name;

    // short label (required)
    protected String _shortLabel;

    // long label (required)
    protected String _longLabel;

    // field length (optional, depends on the type of layout)
    protected Integer _length;

    // NAACCR Item Number (optional)
    protected Integer _naaccrItemNum;

    // alignment (default to left)
    protected FieldAlignment _align;

    // padding character (optional)
    protected String _padChar;

    // default Value (default to null)
    protected String _defaultValue;

    // trimming (defaults to true)
    protected Boolean _trim;

    // section of field (optional)
    protected String _section;

    /**
     * Constructor.
     */
    public Field() {
        _trim = Boolean.TRUE;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getShortLabel() {
        return _shortLabel;
    }

    public void setShortLabel(String shortLabel) {
        this._shortLabel = shortLabel;
    }

    public String getLongLabel() {
        return _longLabel;
    }

    public void setLongLabel(String longLabel) {
        this._longLabel = longLabel;
    }

    public Integer getLength() {
        return _length;
    }

    public void setLength(Integer length) {
        _length = length;
    }

    public Integer getNaaccrItemNum() {
        return _naaccrItemNum;
    }

    public void setNaaccrItemNum(Integer naaccrItemNum) {
        this._naaccrItemNum = naaccrItemNum;
    }

    public FieldAlignment getAlign() {
        return _align;
    }

    public void setAlign(FieldAlignment align) {
        this._align = align;
    }

    public String getDefaultValue() {
        return _defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this._defaultValue = defaultValue;
    }

    public String getPadChar() {
        return _padChar;
    }

    public void setPadChar(String padChar) {
        this._padChar = padChar;
    }

    public Boolean getTrim() {
        return _trim;
    }

    public void setTrim(Boolean trim) {
        this._trim = trim;
    }

    public String getSection() {
        return _section;
    }

    public void setSection(String section) {
        _section = section;
    }

    @Override
    public String toString() {
        return "Field [name=" + _name + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Field that = (Field)o;
        return Objects.equals(_name, that._name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _name);
    }
}
