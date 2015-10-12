package com.imsweb.layout;

public class LayoutInfoDiscoveryOptions {

    // applies to fixed-column only, if set to false, then the line length won't be used (defaults to true)
    private boolean _fixedColumnAllowDiscoveryFromLineLength;

    // applies to comma-separated only, if set to false, then the number of fields won't be used (defaults to true)
    private boolean _commaSeparatedAllowDiscoveryFromNumFields;

    // if true, a blank version will be accepted; the latest NAACCR version available will be the best choice, but other versions might work as well (defaults to false)
    private boolean _naaccrAllowBlankVersion;

    // if true, a blank record type will be accepted; the line length will be used to determine it (defaults to false)
    private boolean _naaccrAllowBlankRecordType;

    /**
     * Consturctor
     */
    public LayoutInfoDiscoveryOptions() {
        _fixedColumnAllowDiscoveryFromLineLength = true;
        _commaSeparatedAllowDiscoveryFromNumFields = true;

        // I would love to set those two default to false, but the reality is that many software produce records without this besic information available!
        _naaccrAllowBlankVersion = true;
        _naaccrAllowBlankRecordType = true;
    }

    public boolean isFixedColumnAllowDiscoveryFromLineLength() {
        return _fixedColumnAllowDiscoveryFromLineLength;
    }

    public void setFixedColumnAllowDiscoveryFromLineLength(boolean fixedColumnAllowDiscoveryFromLineLength) {
        _fixedColumnAllowDiscoveryFromLineLength = fixedColumnAllowDiscoveryFromLineLength;
    }

    public boolean isCommaSeparatedAllowDiscoveryFromNumFields() {
        return _commaSeparatedAllowDiscoveryFromNumFields;
    }

    public void setCommaSeparatedAllowDiscoveryFromNumFields(boolean commaSeparatedAllowDiscoveryFromNumFields) {
        _commaSeparatedAllowDiscoveryFromNumFields = commaSeparatedAllowDiscoveryFromNumFields;
    }

    public boolean isNaaccrAllowBlankVersion() {
        return _naaccrAllowBlankVersion;
    }

    public void setNaaccrAllowBlankVersion(boolean naaccrAllowBlankVersion) {
        _naaccrAllowBlankVersion = naaccrAllowBlankVersion;
    }

    public boolean isNaaccrAllowBlankRecordType() {
        return _naaccrAllowBlankRecordType;
    }

    public void setNaaccrAllowBlankRecordType(boolean naaccrAllowBlankRecordType) {
        _naaccrAllowBlankRecordType = naaccrAllowBlankRecordType;
    }
}
