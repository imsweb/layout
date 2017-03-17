/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void addSegment(Hl7Segment segment) {
        _segments.add(segment);
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
        return new Hl7Field();
    }

    public Hl7Component getComponent(String id) {
        Pattern pattern = Pattern.compile("([A-Z][A-Z][A-Z])-(\\d+)\\.(\\d+)");
        Matcher matcher = pattern.matcher(id);
        if (matcher.matches()) {
            Hl7Segment segment = getSegment(matcher.group(1));
            if (segment != null)
                return segment.getField(Integer.parseInt(matcher.group(2))).getComponent(Integer.parseInt(matcher.group(3)));
            else
                return new Hl7Component();
        }
        else
            return new Hl7Component();
    }
}
