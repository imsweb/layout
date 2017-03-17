/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.util.HashMap;
import java.util.Map;

public class Hl7Segment {

    private Hl7Message _message;

    private String _id;

    private Map<Integer, Hl7Field> _fields;

    public Hl7Segment() {
        _fields = new HashMap<>();
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

    public Map<Integer, Hl7Field> getFields() {
        return _fields;
    }

    public void setFields(Map<Integer, Hl7Field> fields) {
        _fields = fields;
    }

    public void addField(Hl7Field field) {
        _fields.put(field.getIndex(), field);
    }

    public Hl7Field getField(int fieldIdx) {
        Hl7Field field = _fields.get(fieldIdx);
        return field == null ? new Hl7Field() : field;
    }
}
