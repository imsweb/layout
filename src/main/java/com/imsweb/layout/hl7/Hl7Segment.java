/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.util.ArrayList;
import java.util.List;

public class Hl7Segment {

    private Hl7Message _message;
    
    private String _id;
    
    private List<Hl7Field> _fields;
    
    public Hl7Segment() {
        _fields = new ArrayList<>();
    }

    public Hl7Message getMessage() {
        return _message;
    }

    public void setMessage(Hl7Message message) {
        _message = message;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        _id = id;
    }

    public List<Hl7Field> getFields() {
        return _fields;
    }

    public void setFields(List<Hl7Field> fields) {
        _fields = fields;
    }

    public Hl7Field withField(Integer index) {
        Hl7Field field = new Hl7Field();
        field.setSegment(this);
        field.setIndex(index);
        _fields.add(field);
        return field;
    }
}
