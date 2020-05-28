/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.imsweb.layout.hl7.Hl7Utils;

public class Hl7Segment {

    // the parent message
    private Hl7Message _message;

    // the segment ID
    private String _id;

    // the list of fields, in the order they appear in the segment
    private Map<Integer, Hl7Field> _fields;

    /**
     * Constructor.
     * @param message parent message (can be null)
     * @param id message ID (cannot be null)
     */
    public Hl7Segment(Hl7Message message, String id) {
        if (id == null)
            throw new RuntimeException("ID is required");

        _message = message;
        _id = id;
        _fields = new HashMap<>();

        if ("MSH".equals(id)) {
            new Hl7Field(this, 1, "|");
            new Hl7Field(this, 2, "^~\\&");
            new Hl7Field(this, 7, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSS")));
            new Hl7Field(this, 9, "ORU", "R01", "ORU_R01");
            new Hl7Field(this, 11, "P");
            new Hl7Field(this, 12, "2.5.1");
            new Hl7Field(this, 21, "VOL_V_40_ORU_R01", "NAACCR_CP");
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
        _id = id;
    }

    public Map<Integer, Hl7Field> getFields() {
        return _fields;
    }

    public void setFields(Map<Integer, Hl7Field> fields) {
        _fields = fields == null ? new HashMap<>() : fields;
    }

    public void addField(Hl7Field field) {
        _fields.put(field.getIndex(), field);
    }

    public Hl7Field getField(int fieldIdx) {
        Hl7Field result = _fields.get(fieldIdx);
        return result == null ? new Hl7Field(null, fieldIdx) : result;
    }

    public String getValue() {
        return Hl7Utils.segmentToString(this, false);
    }

    public String getValue(int fieldIdx) {
        return getField(fieldIdx).getValue();
    }

    public String getValue(int fieldIdx, int repeatedFieldIdx, int componentIdx, int subComponentIdx) {
        return getField(fieldIdx).getRepeatedField(repeatedFieldIdx).getComponent(componentIdx).getSubComponent(subComponentIdx).getValue();
    }
}
