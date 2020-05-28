/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.imsweb.layout.hl7.entity.Hl7Component;
import com.imsweb.layout.hl7.entity.Hl7Field;
import com.imsweb.layout.hl7.entity.Hl7Message;
import com.imsweb.layout.hl7.entity.Hl7RepeatedField;
import com.imsweb.layout.hl7.entity.Hl7Segment;
import com.imsweb.layout.hl7.entity.Hl7SubComponent;

public final class Hl7Utils {

    private static final Pattern _ESCAPED_SEQUENCES = Pattern.compile("\\\\([A-Z])([^\\\\]*)\\\\");

    private static final Pattern _SEGMENT_ID_PATTERN = Pattern.compile("[A-Z0-9]{3}");

    /**
     * Private constructor, no instanciation!
     * <p/>
     * Created on Aug 16, 2011 by depryf
     */
    private Hl7Utils() {
    }

    /**
     * Reads a segment for the given line and adds it to the given message. Can return null for invalid lines depending on the options.
     * @param msg parent message, required
     * @param line line to parse, required
     * @return the created segment, possibly null
     */
    public static Hl7Segment segmentFromString(Hl7Message msg, String line) {
        return segmentFromString(msg, line, new Hl7LayoutOptions());
    }

    /**
     * Reads a segment for the given line and adds it to the given message. Can return null for invalid lines depending on the options.
     * @param msg parent message, required
     * @param line line to parse, required
     * @param options options, required
     * @return the created segment, possibly null
     */
    public static Hl7Segment segmentFromString(Hl7Message msg, String line, Hl7LayoutOptions options) {

        String id = line.length() < 3 ? line : line.substring(0, 3);
        if (!_SEGMENT_ID_PATTERN.matcher(id).matches()) {
            if (options.skipInvalidSegmentIds())
                return null;
            throw new RuntimeException("Index must be a mix of 3 uppercase letters and/or digits");
        }

        Hl7Segment segment = new Hl7Segment(msg, id);

        if ("MSH".equals(segment.getId()) && line.length() > 3) {
            String fieldSeparator = String.valueOf(line.charAt(3));
            segment.getField(2).getRepeatedField(1).getComponent(1).getSubComponent(1).setValue(fieldSeparator);
        }

        String[] fieldValues = StringUtils.splitPreserveAllTokens(line, msg.getFieldSeparator());
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
                Hl7Field field = new Hl7Field(segment, "MSH".equals(segment.getId()) ? (fieldIdx + 1) : fieldIdx);

                String[] repeatedFieldValues = StringUtils.splitPreserveAllTokens(fieldValue, msg.getRepetitionSeparator());
                //noinspection ForLoopReplaceableByForEach
                for (int repeatedFieldIdx = 0; repeatedFieldIdx < repeatedFieldValues.length; repeatedFieldIdx++) {
                    String repeatedFieldValue = repeatedFieldValues[repeatedFieldIdx];
                    if (!repeatedFieldValue.isEmpty()) {
                        Hl7RepeatedField repeatedField = new Hl7RepeatedField(field);
                        String[] compValues = StringUtils.splitPreserveAllTokens(repeatedFieldValue, msg.getComponentSeparator());
                        for (int compIdx = 0; compIdx < compValues.length; compIdx++) {
                            String compValue = compValues[compIdx];
                            if (!compValue.isEmpty()) {
                                Hl7Component component = new Hl7Component(repeatedField, compIdx + 1);
                                String[] subCompValues = StringUtils.splitPreserveAllTokens(compValue, msg.getSubComponentSeparator());
                                for (int subCompIdx = 0; subCompIdx < subCompValues.length; subCompIdx++) {
                                    String escapeChar = msg.getEscapeCharacter();
                                    String fieldChar = msg.getFieldSeparator();
                                    String repeatingChar = msg.getRepetitionSeparator();
                                    String compChar = msg.getComponentSeparator();
                                    String subCompChar = msg.getSubComponentSeparator();
                                    String subCompValue = decodeEscapedSequences(subCompValues[subCompIdx], escapeChar, fieldChar, repeatingChar, compChar, subCompChar);
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

    /**
     * Writes the given message as a string. Will use the default platform line separator between the segments.
     * @param message message to write, required
     * @return the created string
     */
    public static String messageToString(Hl7Message message) {
        return messageToString(message, new Hl7LayoutOptions());
    }

    /**
     * Writes the given message as a string.
     * @param message message to write, required
     * @param options options, required
     * @return the created string
     */
    public static String messageToString(Hl7Message message, Hl7LayoutOptions options) {

        // make sure input is not null/empty
        if (message == null || message.getSegments().isEmpty())
            return "";

        // write each element with a separator between them
        return message.getSegments().stream().map(Hl7Utils::segmentToString).filter(Objects::nonNull).collect(Collectors.joining(options.getLineSeparator()));
    }

    /**
     * Writes the given segment as a string.
     * @param segment segment to write, required
     * @return the created string
     */
    public static String segmentToString(Hl7Segment segment) {
        return segmentToString(segment, true);
    }

    /**
     * Writes the given segment as a string, returns null if the input segment is null or doesn't have any fields.
     * @param segment segment to write, required
     * @param encode if true, the values will be properly encoded
     * @return the created string, possibly null
     */
    public static String segmentToString(Hl7Segment segment, boolean encode) {

        // make sure input is not null/empty
        if (segment == null || segment.getFields().isEmpty())
            return null;

        // get maximum index
        int max = segment.getFields().keySet().stream().max(Integer::compareTo).orElse(0);

        // we need to adjust a few things for MSH because the first field is the separator but we don't want to output it
        boolean msh = "MSH".equals(segment.getId());

        // create a list that takes into account the gaps
        List<Hl7Field> list = new ArrayList<>(Collections.nCopies(max, null));
        segment.getFields().values().stream().filter(f -> !msh || f.getIndex() != 1).forEach(f -> list.set(msh ? (f.getIndex() - 2) : f.getIndex() - 1, f));

        // write each element with a separator between them
        String separator = segment.getMessage().getFieldSeparator();
        return segment.getId() + separator + list.stream().map(f -> fieldToString(f, encode && !msh)).collect(Collectors.joining(separator));
    }

    /**
     * Writes the given field as a string. Will use the field separator defined in the parent message of the field.
     * @param field field to write, required
     * @return the created string
     */
    public static String fieldToString(Hl7Field field) {
        return fieldToString(field, true);
    }

    /**
     * Writes the given field as a string. Will use the field separator defined in the parent message of the field.
     * @param field field to write, required
     * @param encode if true, the values will be properly escaped
     * @return the created string
     */
    public static String fieldToString(Hl7Field field, boolean encode) {

        // make sure input is not null/empty
        if (field == null || field.getRepeatedFields().isEmpty())
            return "";

        // write each element with a separator between them
        String separator = field.getSegment().getMessage().getRepetitionSeparator();
        return field.getRepeatedFields().stream().map(r -> repeatedFieldToString(r, encode)).collect(Collectors.joining(separator));
    }

    /**
     * Writes the given repeated field as a string. Will use the repetition separator defined in the parent message of the repeating field.
     * @param repeatedField repeating field to write, required
     * @return the created string
     */
    public static String repeatedFieldToString(Hl7RepeatedField repeatedField) {
        return repeatedFieldToString(repeatedField, true);
    }

    /**
     * Writes the given repeated field as a string. Will use the repetition separator defined in the parent message of the repeating field.
     * @param repeatedField repeating field to write, required
     * @param encode if true, the values will be properly encoded
     * @return the created string
     */
    public static String repeatedFieldToString(Hl7RepeatedField repeatedField, boolean encode) {

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
        return list.stream().map(c -> componentToString(c, encode)).collect(Collectors.joining(separator));
    }

    /**
     * Writes the given component as a string. Will use the component separator defined in the parent message of the component.
     * @param component component to write, required
     * @return the created string
     */
    public static String componentToString(Hl7Component component) {
        return componentToString(component, true);
    }

    /**
     * Writes the given component as a string. Will use the component separator defined in the parent message of the component.
     * @param component component to write, required
     * @param encode if true, the values will be properly escaped
     * @return the created string
     */
    public static String componentToString(Hl7Component component, boolean encode) {

        // make sure input is not null/empty
        if (component == null || component.getSubComponents().isEmpty())
            return "";

        // get maximum index
        int max = component.getSubComponents().keySet().stream().max(Integer::compareTo).orElse(0);

        // create a list that takes into account the gaps
        List<Hl7SubComponent> list = new ArrayList<>(Collections.nCopies(max, null));
        component.getSubComponents().values().forEach(c -> list.set(c.getIndex() - 1, c));

        // write each element with a separator between them
        Hl7Message msg = component.getRepeatedField().getField().getSegment().getMessage();
        String escapeChar = msg.getEscapeCharacter();
        String fieldChar = msg.getFieldSeparator();
        String repeatingChar = msg.getRepetitionSeparator();
        String compChar = msg.getComponentSeparator();
        String subCompChar = msg.getSubComponentSeparator();
        return list.stream()
                .map(c -> c == null ? "" : encode ? encodeEscapedSequences(c.getValue(), escapeChar, fieldChar, repeatingChar, compChar, subCompChar) : c.getValue())
                .collect(Collectors.joining(subCompChar));
    }

    /**
     * Replaces escaped sequences by their value, using standard separators.
     * @param value value in which the sequences need to be replaced
     * @return the same value, with the escaped sequences replaced
     */
    public static String decodeEscapedSequences(String value) {
        return decodeEscapedSequences(value, "\\", "|", "~", "^", "&");
    }

    /**
     * Replaces escaped sequences by their value.
     * @param value value in which the sequences need to be replaced
     * @return the same value, with the escaped sequences replaced
     */
    public static String decodeEscapedSequences(String value, String escapeChar, String fieldChar, String repeatChar, String compChar, String subComChar) {
        if (value == null)
            return null;

        // optimization - since the escape character is probably the backslash, let's try to use a pre-compiled pattern if we can...s
        Matcher matcher = ("\\".equals(escapeChar) ? _ESCAPED_SEQUENCES : Pattern.compile(Pattern.quote(escapeChar) + "([A-Z])([^" + escapeChar + "]*)" + Pattern.quote(escapeChar))).matcher(value);

        // ugh - the replaceAll method on the matcher does all this, but it's a Java 9 feature and this project is still on Java 8 :-(
        int currentIdx = 0;
        StringBuilder buf = new StringBuilder();
        while (matcher.find()) {

            // add the part of the original string that wasn't process yet
            buf.append(value, currentIdx, matcher.start());

            // we are going to use this as a default value in several of the cases
            String fullOriginalGroup = escapeChar + matcher.group(1) + matcher.group(2) + escapeChar;

            switch (matcher.group(1)) {
                case "F":
                    buf.append(fieldChar);
                    break;
                case "R":
                    buf.append(repeatChar);
                    break;
                case "S":
                    buf.append(compChar);
                    break;
                case "T":
                    buf.append(subComChar);
                    break;
                case "E":
                    buf.append(escapeChar);
                    break;
                case "X":
                    String codeValue = matcher.group(2);

                    // that would be weird, but if there is an X without any hex value, don't report an error, don't copy the escaped X
                    if (codeValue.isEmpty())
                        break;

                    // the codes are supposed to be pairs of hex values, if that's not the case, copy back original value
                    if (codeValue.length() % 2 != 0) {
                        buf.append(fullOriginalGroup);
                        break;
                    }

                    // if anything goes wrong, copy back the original value (so this is an all or nothing operation)
                    try {
                        StringBuilder tmpBuf = new StringBuilder();
                        for (int i = 0; i < matcher.group(2).length(); i += 2)
                            tmpBuf.append((char)(Integer.parseInt(matcher.group(2).substring(i, i + 2), 16)));
                        buf.append(tmpBuf);
                    }
                    catch (NumberFormatException e) {
                        buf.append(fullOriginalGroup);
                    }
                    break;
                case "C":
                case "H":
                case "M":
                case "N":
                case "Z":
                    // these are legit HL7 escaped sequence but they are not supported, so don't replace them
                    break;
                default:
                    // anything else is invalid, replace it as-is so the caller can see what the original value was
                    buf.append(fullOriginalGroup);
            }
            currentIdx = matcher.end();
        }
        buf.append(value.substring(currentIdx));

        return buf.toString();
    }

    /**
     * Replaces special characters by their escaped sequences, using standard separators.
     * @param value value in which the sequences need to be set
     * @return the same value, with the escaped sequences set
     */
    public static String encodeEscapedSequences(String value) {
        return encodeEscapedSequences(value, "\\", "|", "~", "^", "&");
    }

    /**
     * Replaces special characters by their escaped sequences.
     * @param value value in which the sequences need to be set
     * @return the same value, with the escaped sequences set
     */
    public static String encodeEscapedSequences(String value, String escapeChar, String fieldChar, String repeatChar, String compChar, String subComChar) {
        if (value == null)
            return "";

        value = value.replace(escapeChar, escapeChar + "E" + escapeChar);
        value = value.replace(fieldChar, escapeChar + "F" + escapeChar);
        value = value.replace(repeatChar, escapeChar + "R" + escapeChar);
        value = value.replace(compChar, escapeChar + "S" + escapeChar);
        value = value.replace(subComChar, escapeChar + "T" + escapeChar);
        value = value.replace("\r", escapeChar + "X0D" + escapeChar);
        value = value.replace("\n", escapeChar + "X0A" + escapeChar);

        return value;
    }
}
