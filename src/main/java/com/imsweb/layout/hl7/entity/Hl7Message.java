/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.util.ArrayList;
import java.util.List;

public class Hl7Message {

    private List<Hl7Segment> _segments;

    public Hl7Message() {
        _segments = new ArrayList<>();
    }

    public List<Hl7Segment> getSegments() {
        return _segments;
    }

    public void setSegments(List<Hl7Segment> segments) {
        _segments = segments;
    }

    public Hl7Segment addSegment(Hl7Segment segment) {
        _segments.add(segment);
        return segment;
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
        for (Hl7Segment segment : _segments)
            if (id.equals(segment.getId()))
                return segment;
        return null;
    }

    public Hl7Field getField(String id) {
        String[] parts = id.split("-");
        String segmentId = parts[0];
        Integer fieldIdx = Integer.valueOf(parts[1]);
        Hl7Segment segment = getSegment(segmentId);
        if (segment != null)
            return segment.getField(fieldIdx);
        return null;
        //return new Hl7Field(segment, 1);
    }
}
