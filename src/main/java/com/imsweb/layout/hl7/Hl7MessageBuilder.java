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

/**
 * This class can be used to build HL7 message programmatically.
 */
public class Hl7MessageBuilder {

    /**
     * Creates a new HL7 message wrapped into a builder.
     * @return a new HL7 message wrapped into a builder
     */
    public static Hl7MessageBuilder createMessage() {
        return createMessage(null);
    }

    /**
     * Creates a new HL7 message wrapped into a builder, sets the starting line number for the message
     * @return a new HL7 message wrapped into a builder
     */
    public static Hl7MessageBuilder createMessage(Integer startingLineNumber) {
        Hl7Message message = new Hl7Message();
        message.setLineNumber(startingLineNumber);
        return new Hl7MessageBuilder(message);
    }

    // current message
    private Hl7Message _message;

    // current segment
    private Hl7Segment _currentSegment;

    // current field
    private Hl7Field _currentField;

    // current repeating field
    private Hl7RepeatedField _currentRepeatedField;

    // current component
    private Hl7Component _currentComponent;

    // current sub-component
    private Hl7SubComponent _currentSubComponent;

    /**
     * Constructor.
     * @param message current message
     */
    public Hl7MessageBuilder(Hl7Message message) {
        _message = message;
    }

    /**
     * Builds a segment for the provided ID.
     * @param id segment ID
     * @return builder instance
     */
    public Hl7MessageBuilder withSegment(String id) {
        _currentSegment = new Hl7Segment(_message, id);
        _currentField = null;
        _currentRepeatedField = null;
        _currentComponent = null;
        _currentSubComponent = null;
        return this;
    }

    /**
     * Builds a field for the provided index and assign the provided values to its repeated fields.
     * @param index field index
     * @param values repeated field values
     * @return builder instance
     */
    public Hl7MessageBuilder withField(Integer index, String... values) {
        if (_currentSegment == null)
            throw new RuntimeException("no segment has been created yet");
        _currentField = new Hl7Field(_currentSegment, index, values);
        _currentRepeatedField = null;
        _currentComponent = null;
        _currentSubComponent = null;
        return this;
    }

    /**
     * Builds a repeated field.
     * @return builder instance
     */
    public Hl7MessageBuilder withRepeatedField() {
        if (_currentField == null)
            throw new RuntimeException("no field has been created yet");
        _currentRepeatedField = new Hl7RepeatedField(_currentField);
        _currentComponent = null;
        _currentSubComponent = null;
        return this;
    }

    /**
     * Builds a field for the provided index and assign the provided values to its sub-components.
     * @param index component index
     * @param values sub-component values
     * @return builder instance
     */
    public Hl7MessageBuilder withComponent(Integer index, String... values) {
        if (_currentRepeatedField == null)
            withRepeatedField();
        _currentComponent = new Hl7Component(_currentRepeatedField, index, values);
        _currentSubComponent = null;
        return this;
    }

    /**
     * Builds a sub-component for the provided index.
     * @param index sub-component index
     * @return builder instance
     */
    public Hl7MessageBuilder withSubComponent(Integer index) {
        return withSubComponent(index, null);
    }

    /**
     * Builds a sub-component for the provided index and sets its value.
     * @param index sub-component index
     * @param value sub-component value
     * @return builder instance
     */
    public Hl7MessageBuilder withSubComponent(Integer index, String value) {
        if (_currentComponent == null)
            throw new RuntimeException("no component has been created yet");
        _currentSubComponent = new Hl7SubComponent(_currentComponent, index, value);
        return this;
    }

    /**
     * Sets the value on the current sub-component.
     * @param value value to set
     * @return builder instance
     */
    public Hl7MessageBuilder withValue(String value) {
        if (_currentSubComponent == null)
            throw new RuntimeException("no sub-component has been created yet");
        _currentSubComponent.setValue(value);
        _currentSubComponent = null;
        return this;
    }

    /**
     * Triggers the final build, putting all the provided information together to actually build the HL7 message.
     * @return the built HL7 message
     */
    public Hl7Message build() {
        return _message;
    }
}
