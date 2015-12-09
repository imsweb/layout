/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.layout;

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

    /**
     * Field name (required)
     */
    protected String _name;

    /**
     * Short label (required)
     */
    protected String _shortLabel;

    /**
     * Long label (required)
     */
    protected String _longLabel;

    /**
     * NAACCR Item Number (optional)
     */
    protected Integer _naaccrItemNum;

    /**
     * Alignment (default to left)
     */
    protected FieldAlignment _align;

    /**
     * Padding character (optional)
     */
    protected String _padChar;

    /**
     * Default Value (default to null)
     */
    protected String _defaultValue;

    /**
     * Trimming (defaults to true)
     */
    protected Boolean _trim;

    /**
     * Section of field (optional)
     */
    protected String _section;

    /**
     * Constructor.
     */
    public Field() {
        _trim = Boolean.TRUE;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return _name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * @return Returns the shortLabel.
     */
    public String getShortLabel() {
        return _shortLabel;
    }

    /**
     * @param shortLabel The shortLabel to set.
     */
    public void setShortLabel(String shortLabel) {
        this._shortLabel = shortLabel;
    }

    /**
     * @return Returns the longLabel.
     */
    public String getLongLabel() {
        return _longLabel;
    }

    /**
     * @param longLabel The longLabel to set.
     */
    public void setLongLabel(String longLabel) {
        this._longLabel = longLabel;
    }

    /**
     * @return Returns the naaccrItemNum.
     */
    public Integer getNaaccrItemNum() {
        return _naaccrItemNum;
    }

    /**
     * @param naaccrItemNum The naaccrItemNum to set.
     */
    public void setNaaccrItemNum(Integer naaccrItemNum) {
        this._naaccrItemNum = naaccrItemNum;
    }

    /**
     * @return Returns the align.
     */
    public FieldAlignment getAlign() {
        return _align;
    }

    /**
     * @param align The align to set.
     */
    public void setAlign(FieldAlignment align) {
        this._align = align;
    }

    /**
     * @return Returns the default value.
     */
    public String getDefaultValue() {
        return _defaultValue;
    }

    /**
     * @param defaultValue The default value to set.
     */
    public void setDefaultValue(String defaultValue) {
        this._defaultValue = defaultValue;
    }

    /**
     * @return Returns the padChar.
     */
    public String getPadChar() {
        return _padChar;
    }

    /**
     * @param padChar The padChar to set.
     */
    public void setPadChar(String padChar) {
        this._padChar = padChar;
    }

    /**
     * @return Returns the trim value.
     */
    public Boolean getTrim() {
        return _trim;
    }

    /**
     * @param trim The trim value to set.
     */
    public void setTrim(Boolean trim) {
        this._trim = trim;
    }

    /**
     * @return Returns the section
     */
    public String getSection() {
        return _section;
    }

    /**
     * @param section The section value to set
     */
    public void setSection(String section) {
        _section = section;
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
            if (other._name != null)
                return false;
        }
        else if (!_name.equals(other._name))
            return false;
        return true;
    }
}
