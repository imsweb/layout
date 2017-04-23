/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import com.imsweb.layout.hl7.entity.Hl7Component;
import com.imsweb.layout.hl7.entity.Hl7Field;
import com.imsweb.layout.hl7.entity.Hl7Message;
import com.imsweb.layout.hl7.entity.Hl7RepeatedField;
import com.imsweb.layout.hl7.entity.Hl7Segment;
import com.imsweb.layout.hl7.entity.Hl7SubComponent;

public class Hl7MessageBuilder {

    public static Hl7MessageBuilder createMessage() {
        return new Hl7MessageBuilder(new Hl7Message());
    }

    private Hl7Message _message;

    private Hl7Segment _currentSegment;

    private Hl7Field _currentField;

    private Hl7RepeatedField _currentRepeatedField;

    private Hl7Component _currentComponent;

    private Hl7SubComponent _currentSubComponent;

    public Hl7MessageBuilder(Hl7Message message) {
        _message = message;
    }

    public Hl7MessageBuilder withSegment(String id) {
        _currentSegment = new Hl7Segment(_message, id);
        _currentField = null;
        _currentRepeatedField = null;
        _currentComponent = null;
        _currentSubComponent = null;
        return this;
    }

    public Hl7MessageBuilder withField(Integer index) {
        if (_currentSegment == null)
            throw new RuntimeException("no segment has been created yet");
        _currentField = new Hl7Field(_currentSegment, index);
        _currentRepeatedField = null;
        _currentComponent = null;
        _currentSubComponent = null;
        return this;
    }

    public Hl7MessageBuilder withField(Integer index, String... values) {
        if (_currentSegment == null)
            throw new RuntimeException("no segment has been created yet");
        _currentField = new Hl7Field(_currentSegment, index, values);
        _currentRepeatedField = null;
        _currentComponent = null;
        _currentSubComponent = null;
        return this;
    }

    public Hl7MessageBuilder withRepeatedField() {
        if (_currentField == null)
            throw new RuntimeException("no field has been created yet");
        _currentRepeatedField = new Hl7RepeatedField(_currentField);
        _currentComponent = null;
        _currentSubComponent = null;
        return this;
    }

    public Hl7MessageBuilder withComponent(Integer index, String... values) {
        if (_currentRepeatedField == null)
            withRepeatedField();
        _currentComponent = new Hl7Component(_currentRepeatedField, index, values);
        _currentSubComponent = null;
        return this;
    }

    public Hl7MessageBuilder withSubComponent(Integer index) {
        return withSubComponent(index, null);
    }

    public Hl7MessageBuilder withSubComponent(Integer index, String value) {
        if (_currentComponent == null)
            throw new RuntimeException("no component has been created yet");
        _currentSubComponent = new Hl7SubComponent(_currentComponent, index, value);
        return this;
    }

    public Hl7MessageBuilder withValue(String value) {
        if (_currentSubComponent == null)
            throw new RuntimeException("no sub-component has been created yet");
        _currentSubComponent.setValue(value);
        _currentSubComponent = null;
        return this;
    }

    public Hl7Message build() {
        return _message;
    }
}
