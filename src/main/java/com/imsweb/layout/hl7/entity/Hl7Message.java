/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Hl7Message {

    // the line number of the first segment of this message (the MSH one), if applicable
    private Integer _lineNumber;

    // the list of segment, in the order they appear in the message
    private List<Hl7Segment> _segments;

    /**
     * Constructor
     */
    public Hl7Message() {
        _segments = new ArrayList<>();
    }

    public List<Hl7Segment> getSegments() {
        return _segments;
    }

    public void setSegments(List<Hl7Segment> segments) {
        _segments = segments;
    }

    public void addSegment(Hl7Segment segment) {
        _segments.add(segment);
    }

    public Integer getLineNumber() {
        return _lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        _lineNumber = lineNumber;
    }

    public String getFieldSeparator() {
        String value = getMshFieldValue(1);
        return value == null ? "|" : value;
    }

    public String getComponentSeparator() {
        String value = getMshFieldValue(2);
        return value == null || value.length() != 4 ? "^" : String.valueOf(value.charAt(0));
    }

    public String getRepetitionSeparator() {
        String value = getMshFieldValue(2);
        return value == null || value.length() != 4 ? "~" : String.valueOf(value.charAt(1));
    }

    public String getEscapeCharacter() {
        String value = getMshFieldValue(2);
        return value == null || value.length() != 4 ? "\\" : String.valueOf(value.charAt(2));
    }

    public String getSubComponentSeparator() {
        String value = getMshFieldValue(2);
        return value == null || value.length() != 4 ? "&" : String.valueOf(value.charAt(3));
    }

    private String getMshFieldValue(int fieldIdx) {
        Hl7Segment segment = getSegment("MSH");
        if (segment != null) {
            Hl7Field field = segment.getField(fieldIdx);
            if (field != null) {
                Hl7RepeatedField repeatedField = field.getRepeatedField(1);
                if (repeatedField != null) {
                    Hl7Component component = repeatedField.getComponent(1);
                    if (component != null) {
                        Hl7SubComponent subComponent = component.getSubComponent(1);
                        if (subComponent != null)
                            return subComponent.getValue();
                    }
                }
            }
        }
        return null;
    }

    public Hl7Segment getSegment(String id) {
        return getSegment(id, 0);
    }

    public Hl7Segment getSegment(String id, int idx) {
        List<Hl7Segment> filteredSegments = _segments.stream().filter(s -> s.getId().equals(id)).collect(Collectors.toList());
        return idx >= filteredSegments.size() ? null : filteredSegments.get(idx);
    }
}
