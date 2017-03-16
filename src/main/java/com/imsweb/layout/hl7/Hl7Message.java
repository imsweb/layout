/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

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
    
    public Hl7Segment withSegment(String id) {
        Hl7Segment segment = new Hl7Segment();
        segment.setMessage(this);
        segment.setId(id);
        segment.setFields(new ArrayList<>());
        _segments.add(segment);
        return segment;
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
            return segment.getFields().get(fieldIdx);
        return null;
    }
}
