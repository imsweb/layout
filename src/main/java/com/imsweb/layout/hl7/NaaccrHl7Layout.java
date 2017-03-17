/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.imsweb.layout.Field;
import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.hl7.entity.Hl7Component;
import com.imsweb.layout.hl7.entity.Hl7Field;
import com.imsweb.layout.hl7.entity.Hl7Message;
import com.imsweb.layout.hl7.entity.Hl7RepeatedField;
import com.imsweb.layout.hl7.entity.Hl7Segment;
import com.imsweb.layout.hl7.entity.Hl7SubComponent;

public class NaaccrHl7Layout implements Layout {

    private String _fieldSeparator = "|";

    private String _componentSeparator = "^";

    private String _subComponentSeparator = "&";

    private String _repetitionSeparator = "~";

    @Override
    public String getLayoutId() {
        return "naaccr-hl7";
    }

    @Override
    public String getLayoutName() {
        return "NAACCR HL7";
    }

    @Override
    public String getLayoutVersion() {
        return "2.5.1";
    }

    @Override
    public String getLayoutDescription() {
        return "NAACCR HL7";
    }

    @Override
    public Field getFieldByName(String name) {
        return null; // TODO
    }

    @Override
    public Field getFieldByNaaccrItemNumber(Integer num) {
        return null; // TODO
    }

    @Override
    public List<? extends Field> getAllFields() {
        return null; // TODO
    }

    @Override
    public String getFieldDocByName(String name) {
        return null; // TODO
    }

    @Override
    public String getFieldDocByNaaccrItemNumber(Integer num) {
        return null; // TODO
    }

    @Override
    public String getFieldDocDefaultCssStyle() {
        return null; // TODO
    }

    @Override
    public LayoutInfo buildFileInfo(File file, String zipEntryName, LayoutInfoDiscoveryOptions options) {
        return null; // TODO
    }

    public Hl7Message readNextMessage(LineNumberReader reader) throws IOException {
        return fetchNextMessage(reader);
    }

    public List<Hl7Message> readAllMessages(File file) throws IOException {
        List<Hl7Message> result = new ArrayList<>();
        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            Hl7Message message = readNextMessage(reader);
            while (message != null) {
                result.add(message);
                message = readNextMessage(reader);
            }
        }
        return result;
    }

    public void writeMessage(Writer writer, Hl7Message message) throws IOException {
        writer.write(messageToString(message).toString());
        writer.write(System.getProperty("line.separator"));
    }

    public void writeMessages(File file, List<Hl7Message> messages) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Hl7Message message : messages)
                writeMessage(writer, message);
        }
    }

    private Hl7Message fetchNextMessage(LineNumberReader reader) throws IOException {
        Hl7Message msg = null;

        String line = reader.readLine();

        // ignore the FHS and BHS line (those are transmission segments that we don't use)
        while (line != null && (line.startsWith("FHS") || line.startsWith("BHS")))
            line = reader.readLine();

        // MSH should immediately follow FHS or BHS otherwise it's bad format.
        // first get to the message header (MSH) and make sure no other data line is found in between
        while (line != null && !line.startsWith("MSH")) {
            //if (!line.trim().isEmpty())
            //    throw new IOException("File is not properly formatted, found data without a proper MSH segment at line " + reader.getLineNumber());
            line = reader.readLine();
        }

        // if we found the header, create the message
        if (line != null) {
            msg = new Hl7Message(); // TODO reader.getLineNumber()

            // then read the block of text
            while (line != null && !line.trim().isEmpty()) {
                Hl7Segment segment = new Hl7Segment();
                segment.setMessage(msg);
                msg.addSegment(segment);
                segment.setId(line.substring(0, 3));
                
                // TODO set MSH-1 which is the field separator...
                
                String[] fieldValues = line.split("\\|");
                for (int fieldIdx = 1; fieldIdx < fieldValues.length; fieldIdx++) {
                    if ("MSH".equals(segment.getId()) && fieldIdx == 1) {
                        Hl7SubComponent encodingSubComponent = new Hl7SubComponent();
                        encodingSubComponent.setValue(fieldValues[1]);
                        encodingSubComponent.setIndex(1);
                        Hl7Component encodingComponent = new Hl7Component();
                        encodingSubComponent.setComponent(encodingComponent);
                        encodingComponent.setIndex(1);
                        encodingComponent.addSubComponent(encodingSubComponent);
                        Hl7RepeatedField encodingRepeatableField = new Hl7RepeatedField();
                        encodingComponent.setRepeatedField(encodingRepeatableField);
                        encodingRepeatableField.addComponent(encodingComponent);
                        Hl7Field encodingField = new Hl7Field();
                        encodingRepeatableField.setField(encodingField);
                        encodingField.setIndex(2);
                        encodingField.addRepeatedField(encodingRepeatableField);
                        encodingField.setSegment(segment);
                        segment.addField(encodingField);
                        continue;
                    }
                    String fieldValue = fieldValues[fieldIdx];
                    if (!fieldValue.isEmpty()) {
                        Hl7Field field = new Hl7Field();
                        field.setSegment(segment);
                        field.setIndex(fieldIdx);
                        segment.addField(field);
                        String[] repeatedFieldValues = fieldValue.split("~");
                        for (int repeatedFieldIdx = 0; repeatedFieldIdx < repeatedFieldValues.length; repeatedFieldIdx++) {
                            String repeatedFieldValue = repeatedFieldValues[repeatedFieldIdx];
                            if (!repeatedFieldValue.isEmpty()) {
                                Hl7RepeatedField repeatedField = new Hl7RepeatedField();
                                repeatedField.setField(field);
                                field.addRepeatedField(repeatedField);
                                // there is no index on repeated field, maybe there will be one added one day...
                                String[] compValues = repeatedFieldValue.split("\\^");
                                for (int compIdx = 0; compIdx < compValues.length; compIdx++) {
                                    String compValue = compValues[compIdx];
                                    if (!compValue.isEmpty()) {
                                        Hl7Component component = new Hl7Component();
                                        component.setRepeatedField(repeatedField);
                                        component.setIndex(compIdx);
                                        repeatedField.addComponent(component);
                                        String[] subCompValues = compValue.split("&");
                                        for (int subCompIdx = 0; subCompIdx < subCompValues.length; subCompIdx++) {
                                            String subCompValue = subCompValues[subCompIdx];
                                            if (!subCompValue.isEmpty()) {
                                                Hl7SubComponent subComponent = new Hl7SubComponent();
                                                subComponent.setComponent(component);
                                                subComponent.setIndex(subCompIdx);
                                                subComponent.setValue(subCompValue);
                                                component.addSubComponent(subComponent);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // peek for the next three bytes to determine if next line is a message starter
                reader.mark(8192); // this is the default buffer size for the BufferedReader (#67992)
                char[] peek = new char[3];
                int n = reader.read(peek);
                reader.reset();
                if (n != -1 && "MSH".equals(String.valueOf(peek)))
                    break;

                line = reader.readLine();
            }
        }

        return msg;
    }

    private Hl7Message messageFromString(StringBuilder buf) {
        // TODO
        return new Hl7Message();
    }

    private StringBuilder messageToString(Hl7Message message) {
        StringBuilder buf = new StringBuilder();

        for (Hl7Segment segment : message.getSegments()) {
            buf.append(segment.getId());
            int fieldIdx = "MSH".equals(segment.getId()) ? 2: 1;
            for (Hl7Field field : segment.getFields().keySet().stream().sorted().map(segment::getField).collect(Collectors.toList())) {
                for (int i = fieldIdx + 1; i < field.getIndex(); i++)
                    buf.append(_fieldSeparator);
                fieldIdx = field.getIndex();
                buf.append(_fieldSeparator).append(fieldToString(field));
            }
            buf.append(System.getProperty("line.separator"));
        }

        return buf;
    }

    private String fieldToString(Hl7Field field) {
        if (field.getRepeatedFields().isEmpty())
            return "";

        StringBuilder buf = new StringBuilder();

        buf.append(repeatedFieldToString(field.getRepeatedFields().get(0)));
        for (int i = 1; i < field.getRepeatedFields().size(); i++)
            buf.append(_repetitionSeparator).append(repeatedFieldToString(field.getRepeatedFields().get(i)));

        return buf.toString();
    }

    private String repeatedFieldToString(Hl7RepeatedField repeatedField) {
        List<Hl7Component> list = repeatedField.getComponents().keySet().stream().sorted().map(repeatedField::getComponent).collect(Collectors.toList());
        if (list.isEmpty())
            return "";

        StringBuilder buf = new StringBuilder();

        Hl7Component component = list.get(0);

        int componentIdx = 1;
        for (int i = componentIdx + 1; i < component.getIndex(); i++)
            buf.append(_componentSeparator);
        componentIdx = component.getIndex();
        buf.append(componentToString(component));

        for (int j = 1; j < list.size(); j++) {
            component = list.get(j);
            for (int i = componentIdx + 1; i < component.getIndex(); i++)
                buf.append(_componentSeparator);
            componentIdx = component.getIndex();
            buf.append(_componentSeparator).append(componentToString(component));
        }

        return buf.toString();
    }

    private String componentToString(Hl7Component component) {
        List<Hl7SubComponent> list = component.getSubComponents().keySet().stream().sorted().map(component::getSubComponent).collect(Collectors.toList());
        if (list.isEmpty())
            return "";

        StringBuilder buf = new StringBuilder();

        Hl7SubComponent subComponent = list.get(0);

        int subComponentIdx = 1;
        for (int i = subComponentIdx + 1; i < subComponent.getIndex(); i++)
            buf.append(_subComponentSeparator);
        subComponentIdx = subComponent.getIndex();
        buf.append(subComponent.getValue());

        for (int j = 1; j < list.size(); j++) {
            subComponent = list.get(j);
            for (int i = subComponentIdx + 1; i < subComponent.getIndex(); i++)
                buf.append(_subComponentSeparator);
            subComponentIdx = subComponent.getIndex();
            buf.append(_subComponentSeparator).append(subComponent.getValue());
        }

        return buf.toString();
    }
}
