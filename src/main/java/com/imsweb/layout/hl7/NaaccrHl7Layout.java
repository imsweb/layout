/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;

import com.imsweb.layout.Field;
import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;

public class NaaccrHl7Layout implements Layout {

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

    public Hl7Message fetchNextMessage(LineNumberReader reader) throws IOException {
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
                msg.getSegments().add(segment);
                segment.setId(line.substring(0, 3));
                String[] fieldValues = line.split("\\|");
                for (int fieldIdx = 0; fieldIdx < fieldValues.length; fieldIdx++) {
                    String fieldValue = fieldValues[fieldIdx];
                    if (!fieldValue.isEmpty()) {
                        Hl7Field field = new Hl7Field();
                        field.setSegment(segment);
                        segment.getFields().add(field);
                        field.setIndex(fieldIdx);
                        String[] repeatedFieldValues = fieldValue.split("~");
                        for (int repeatedFieldIdx = 0; repeatedFieldIdx < repeatedFieldValues.length; repeatedFieldIdx++) {
                            String repeatedFieldValue = repeatedFieldValues[repeatedFieldIdx];
                            if (!repeatedFieldValue.isEmpty()) {
                                Hl7FieldValue value = new Hl7FieldValue();
                                value.setField(field);
                                field.getValues().add(value);
                                // there is no index on repeated field, maybe there will be one added one day...
                                String[] compValues = repeatedFieldValue.split("\\^");
                                for (int compIdx = 0; compIdx < compValues.length; compIdx++) {
                                    String compValue = compValues[compIdx];
                                    if (!compValue.isEmpty()) {
                                        Hl7Component component = new Hl7Component();
                                        component.setFieldValue(value);
                                        value.getComponents().add(component);
                                        component.setIndex(compIdx);
                                        String[] subCompValues = compValue.split("&");
                                        for (int subCompIdx = 0; subCompIdx < subCompValues.length; subCompIdx++) {
                                            String subCompValue = subCompValues[subCompIdx];
                                            if (!subCompValue.isEmpty()) {
                                                Hl7SubComponent subComponent = new Hl7SubComponent();
                                                subComponent.setComponent(component);
                                                component.getSubComponents().add(subComponent);
                                                subComponent.setIndex(subCompIdx);
                                                subComponent.setValue(subCompValue);
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
}
