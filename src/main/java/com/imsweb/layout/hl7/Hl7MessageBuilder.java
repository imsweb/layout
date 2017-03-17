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
        return new Hl7MessageBuilder();
    }

    private Hl7Message _message;

    private Hl7Segment _currentSegment;

    private Hl7Field _currentField;

    private Hl7RepeatedField _currentRepeatedField;

    private Hl7Component _currentComponent;

    private Hl7SubComponent _currentSubComponent;

    public Hl7MessageBuilder() {
        _message = new Hl7Message();
    }

    public Hl7MessageBuilder withSegment(String id) {
        Hl7Segment segment = new Hl7Segment();
        segment.setMessage(_message);
        segment.setId(id);
        _message.addSegment(segment);
        _currentSegment = segment;
        _currentField = null;
        _currentRepeatedField = null;
        _currentComponent = null;
        _currentSubComponent = null;
        return this;
    }

    public Hl7MessageBuilder withField(Integer index) {
        if (_currentSegment == null)
            throw new RuntimeException("no segment has been created yet");
        Hl7Field field = new Hl7Field();
        field.setSegment(_currentSegment);
        field.setIndex(index);
        _currentSegment.addField(field);
        _currentField = field;
        _currentRepeatedField = null;
        _currentComponent = null;
        _currentSubComponent = null;
        return this;
    }

    public Hl7MessageBuilder withRepeatedField() {
        if (_currentField == null)
            throw new RuntimeException("no field has been created yet");
        Hl7RepeatedField repeatedField = new Hl7RepeatedField();
        repeatedField.setField(_currentField);
        _currentField.addRepeatedField(repeatedField);
        _currentRepeatedField = repeatedField;
        _currentComponent = null;
        _currentSubComponent = null;
        return this;
    }

    public Hl7MessageBuilder withComponent(Integer index, String... values) {
        if (_currentRepeatedField == null)
            withRepeatedField(); // TODO need to make sure no more fields are added...
        Hl7Component component = new Hl7Component();
        component.setRepeatedField(_currentRepeatedField);
        component.setIndex(index);
        _currentRepeatedField.addComponent(component);

        if (values != null) {
            for (int subCompIdx = 0; subCompIdx < values.length; subCompIdx++) {
                Hl7SubComponent subComponent = new Hl7SubComponent();
                subComponent.setComponent(component);
                subComponent.setIndex(subCompIdx + 1);
                subComponent.setValue(values[subCompIdx]);
                component.addSubComponent(subComponent);
            }
        }
        _currentComponent = component;
        _currentSubComponent = null;
        return this;
    }

    public Hl7MessageBuilder withSubComponent(Integer index) {
        return withSubComponent(index, null);
    }

    public Hl7MessageBuilder withSubComponent(Integer index, String value) {
        if (_currentComponent == null)
            throw new RuntimeException("no component has been created yet");
        Hl7SubComponent subComponent = new Hl7SubComponent();
        subComponent.setComponent(_currentComponent);
        subComponent.setIndex(index);
        subComponent.setValue(value);
        _currentComponent.addSubComponent(subComponent);
        _currentSubComponent = subComponent;
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
