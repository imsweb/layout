/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.imsweb.layout.hl7.entity.Hl7Component;
import com.imsweb.layout.hl7.entity.Hl7Field;
import com.imsweb.layout.hl7.entity.Hl7Message;
import com.imsweb.layout.hl7.entity.Hl7RepeatedField;
import com.imsweb.layout.hl7.entity.Hl7Segment;
import com.imsweb.layout.hl7.entity.Hl7SubComponent;

public final class Hl7Utils {

    private Hl7Utils() {
    }

    public static Hl7Segment segmentFromString(Hl7Message msg, String line) {
        Hl7Segment segment = new Hl7Segment(msg, line.substring(0, 3));

        if ("MSH".equals(segment.getId()) && line.length() > 3) {
            String fieldSeparator = String.valueOf(line.charAt(3));
            segment.getField(2).getRepeatedField(1).getComponent(1).getSubComponent(1).setValue(fieldSeparator);
        }

        String[] fieldValues = line.split(Pattern.quote(msg.getFieldSeparator()));
        for (int fieldIdx = 1; fieldIdx < fieldValues.length; fieldIdx++) {
            if ("MSH".equals(segment.getId()) && fieldIdx == 1) {
                if (fieldValues[1].length() > 3) {
                    segment.getField(2).getRepeatedField(1).getComponent(1).getSubComponent(1).setValue(String.valueOf(fieldValues[1].charAt(0)));
                    segment.getField(2).getRepeatedField(1).getComponent(1).getSubComponent(2).setValue(String.valueOf(fieldValues[1].charAt(1)));
                    segment.getField(2).getRepeatedField(1).getComponent(1).getSubComponent(3).setValue(String.valueOf(fieldValues[1].charAt(2)));
                    segment.getField(2).getRepeatedField(1).getComponent(1).getSubComponent(4).setValue(String.valueOf(fieldValues[1].charAt(3)));
                }
                segment.addField(new Hl7Field(segment, 2, fieldValues[1]));
                continue;
            }
            String fieldValue = fieldValues[fieldIdx];
            if (!fieldValue.isEmpty()) {
                Hl7Field field = new Hl7Field(segment, fieldIdx);

                String[] repeatedFieldValues = fieldValue.split(Pattern.quote(msg.getRepetitionSeparator()));
                for (int repeatedFieldIdx = 0; repeatedFieldIdx < repeatedFieldValues.length; repeatedFieldIdx++) {
                    String repeatedFieldValue = repeatedFieldValues[repeatedFieldIdx];
                    if (!repeatedFieldValue.isEmpty()) {
                        Hl7RepeatedField repeatedField = new Hl7RepeatedField(field);
                        String[] compValues = repeatedFieldValue.split(Pattern.quote(msg.getComponentSeparator()));
                        for (int compIdx = 0; compIdx < compValues.length; compIdx++) {
                            String compValue = compValues[compIdx];
                            if (!compValue.isEmpty()) {
                                Hl7Component component = new Hl7Component(repeatedField, compIdx + 1);
                                String[] subCompValues = compValue.split(Pattern.quote(msg.getSubComponentSeparator()));
                                for (int subCompIdx = 0; subCompIdx < subCompValues.length; subCompIdx++) {
                                    String subCompValue = subCompValues[subCompIdx];
                                    if (!subCompValue.isEmpty())
                                        new Hl7SubComponent(component, subCompIdx + 1, subCompValue);
                                }
                            }
                        }
                    }
                }
            }
        }

        return segment;
    }

    public static String messageToString(Hl7Message message) {

        // make sure input is not null/empty
        if (message == null || message.getSegments().isEmpty())
            return "";

        // write each element with a separator between them
        return message.getSegments().stream().map(Hl7Utils::segmentToString).collect(Collectors.joining(System.getProperty("line.separator")));
    }

    public static String segmentToString(Hl7Segment segment) {

        // make sure input is not null/empty
        if (segment == null || segment.getFields().isEmpty())
            return "";

        // get maximum index
        int max = segment.getFields().keySet().stream().max(Integer::compareTo).orElse(0);

        // create a list that takes into account the gaps
        List<Hl7Field> list = new ArrayList<>(Collections.nCopies(max, null));
        segment.getFields().values().forEach(f -> list.set("MSH".equals(segment.getId()) ? f.getIndex() : f.getIndex() - 1, f));

        // write each element with a separator between them
        String separator = segment.getMessage().getFieldSeparator();
        return list.stream().map(Hl7Utils::fieldToString).collect(Collectors.joining(separator));
    }

    public static String fieldToString(Hl7Field field) {

        // make sure input is not null/empty
        if (field == null || field.getRepeatedFields().isEmpty())
            return "";

        // write each element with a separator between them
        String separator = field.getSegment().getMessage().getRepetitionSeparator();
        return field.getRepeatedFields().stream().map(Hl7Utils::repeatedFieldToString).collect(Collectors.joining(separator));
    }

    public static String repeatedFieldToString(Hl7RepeatedField repeatedField) {

        // make sure input is not null/empty
        if (repeatedField == null || repeatedField.getComponents().isEmpty())
            return "";

        // get maximum index
        int max = repeatedField.getComponents().keySet().stream().max(Integer::compareTo).orElse(0);

        // create a list that takes into account the gaps
        List<Hl7Component> list = new ArrayList<>(Collections.nCopies(max, null));
        repeatedField.getComponents().values().forEach(c -> list.set(c.getIndex() - 1, c));

        // write each element with a separator between them
        String separator = repeatedField.getField().getSegment().getMessage().getComponentSeparator();
        return list.stream().map(Hl7Utils::componentToString).collect(Collectors.joining(separator));
    }

    public static String componentToString(Hl7Component component) {

        // make sure input is not null/empty
        if (component == null || component.getSubComponents().isEmpty())
            return "";

        // get maximum index
        int max = component.getSubComponents().keySet().stream().max(Integer::compareTo).orElse(0);

        // create a list that takes into account the gaps
        List<Hl7SubComponent> list = new ArrayList<>(Collections.nCopies(max, null));
        component.getSubComponents().values().forEach(c -> list.set(c.getIndex() - 1, c));

        // write each element with a separator between them
        String separator = component.getRepeatedField().getField().getSegment().getMessage().getSubComponentSeparator();
        return list.stream().map(c -> c == null || c.getValue() == null ? "" : c.getValue()).collect(Collectors.joining(separator));
    }
}
