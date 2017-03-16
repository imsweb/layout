/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.util.ArrayList;

public class Hl7MessageBuilder {

    private Hl7Message _message;
    
    public Hl7MessageBuilder() {
        _message = new Hl7Message();
        _message.setSegments(new ArrayList<>());
    }

    public Hl7Segment withSegment(String id) {
        Hl7Segment segment = new Hl7Segment();
        segment.setMessage(_message);
        segment.setId(id);
        segment.setFields(new ArrayList<>());
        _message.getSegments().add(segment);
        return segment;
    }
    
    public Hl7Message build() {
        return _message;
    }
}
