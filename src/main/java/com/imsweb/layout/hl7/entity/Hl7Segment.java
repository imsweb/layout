/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Hl7Segment {

    private Hl7Message _message;

    private String _id;

    private Map<Integer, Hl7Field> _fields;

    public Hl7Segment(Hl7Message message, String id) {
        if (id == null)
            throw new RuntimeException("ID is required");
        if (!id.matches("[A-Z0-9]{3}"))
            throw new RuntimeException("Index must be a mix of 3 uppercase letters and/or digits");
        _message = message;
        _id = id;
        _fields = new HashMap<>();

        if ("MSH".equals(id)) {
            new Hl7Field(this, 1, "|");
            new Hl7Field(this, 2, "^~\\&");
            new Hl7Field(this, 7, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSS")));
            new Hl7Field(this, 9, "ORU", "R01", "ORU_R01");
            new Hl7Field(this, 11, "P");
            new Hl7Field(this, 12, "2.5.1"); // TODO this should set from the layout
            new Hl7Field(this, 21, "VOL_V_40_ORU_R01", "NAACCR_CP"); // TODO not sure about this one...
        }

        if (message != null)
            message.addSegment(this);
    }

    public Hl7Message getMessage() {
        return _message;
    }

    public void setMessage(Hl7Message message) {
        if (message == null)
            throw new RuntimeException("Parent message cannot be null");
        _message = message;
    }

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        if (id == null)
            throw new RuntimeException("ID is required");
        if (!id.matches("[A-Z]{3}"))
            throw new RuntimeException("Index must be 3 uppercase characters");
        _id = id;
    }

    public Map<Integer, Hl7Field> getFields() {
        return _fields;
    }

    public void setFields(Map<Integer, Hl7Field> fields) {
        _fields = fields == null ? new HashMap<>() : fields;
    }

    public Hl7Field addField(Hl7Field field) {
        _fields.put(field.getIndex(), field);
        return field;
    }

    public Hl7Field getField(int fieldIdx) {
        Hl7Field result = _fields.get(fieldIdx);
        return result == null ? new Hl7Field(null, fieldIdx) : result;
    }
}
