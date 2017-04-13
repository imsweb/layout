/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.imsweb.layout.Field;
import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.hl7.entity.Hl7Message;
import com.imsweb.layout.hl7.xml.Hl7FieldDefinitionXmlDto;
import com.imsweb.layout.hl7.xml.Hl7LayoutDefinitionXmlDto;
import com.imsweb.layout.hl7.xml.Hl7SegmentDefinitionXmlDto;

public class NaaccrHl7Layout implements Layout {

    /**
     * Layout ID
     */
    protected String _layoutId;

    /**
     * Layout name
     */
    protected String _layoutName;

    /**
     * Layout version
     */
    protected String _layoutVersion;

    /**
     * Layout description
     */
    protected String _layoutDesc;

    /**
     * The fields for this layout
     */
    protected List<NaaccrHl7Field> _fields = new ArrayList<>();

    /**
     * Cached fields by name
     */
    protected Map<String, NaaccrHl7Field> _cachedByName = new HashMap<>();

    @Override
    public String getLayoutId() {
        return _layoutId;
    }

    @Override
    public String getLayoutName() {
        return _layoutName;
    }

    @Override
    public String getLayoutVersion() {
        return _layoutVersion;
    }

    @Override
    public String getLayoutDescription() {
        return _layoutDesc;
    }

    @Override
    public Field getFieldByName(String name) {
        return _cachedByName.get(name);
    }

    @Override
    public Field getFieldByNaaccrItemNumber(Integer num) {
        return null;
    }

    @Override
    public List<? extends Field> getAllFields() {
        return Collections.unmodifiableList(_fields);
    }

    @Override
    public String getFieldDocByName(String name) {
        return null;
    }

    @Override
    public String getFieldDocByNaaccrItemNumber(Integer num) {
        return null;
    }

    @Override
    public String getFieldDocDefaultCssStyle() {
        return null;
    }

    @Override
    public LayoutInfo buildFileInfo(File file, String zipEntryName, LayoutInfoDiscoveryOptions options) {
        LayoutInfo result = null;

        return result;
    }

    public NaaccrHl7Layout(URL hl7LayoutUrl) throws IOException {
        if (hl7LayoutUrl == null)
            throw new NullPointerException("Unable to create HL7-layout from null URL");

        try (InputStream is = hl7LayoutUrl.openStream()) {
            init(Hl7Utils.readFixedColumnsLayout(is));
        }
    }

    public NaaccrHl7Layout(File hl7LayoutFile) throws IOException {
        if (hl7LayoutFile == null)
            throw new NullPointerException("Unable to create HL7-layout from null file");
        if (!hl7LayoutFile.exists())
            throw new IOException("Unable to read from " + hl7LayoutFile.getPath());

        try (InputStream is = new FileInputStream(hl7LayoutFile)) {
            init(Hl7Utils.readFixedColumnsLayout(is));
        }
    }

    public NaaccrHl7Layout(Hl7LayoutDefinitionXmlDto hl7LayoutXmlDto) throws IOException {
        if (hl7LayoutXmlDto == null)
            throw new NullPointerException("Unable to create HL7-layout from null XML object");

        init(hl7LayoutXmlDto);
    }

    protected void init(Hl7LayoutDefinitionXmlDto layoutXmlDto) throws IOException {

        _layoutId = layoutXmlDto.getId();
        _layoutName = layoutXmlDto.getName();
        _layoutVersion = layoutXmlDto.getVersion();
        _layoutDesc = layoutXmlDto.getDescription();

        _fields.clear();
        if (layoutXmlDto.getHl7Segments() != null)
            for (Hl7SegmentDefinitionXmlDto hl7SegmentXmlDto : layoutXmlDto.getHl7Segments())
                for (Hl7FieldDefinitionXmlDto hl7FieldXmlDto : hl7SegmentXmlDto.getHl7Fields())
                    addField(createFieldFromXmlField(hl7FieldXmlDto));
    }

    protected NaaccrHl7Field createFieldFromXmlField(Hl7FieldDefinitionXmlDto hl7FieldXmlDto) throws IOException {
        NaaccrHl7Field hl7Field = new NaaccrHl7Field();

        hl7Field.setName(hl7FieldXmlDto.getName());
        hl7Field.setIdentifier(hl7FieldXmlDto.getIdentifier());
        hl7Field.setLongLabel(hl7FieldXmlDto.getLongLabel());
        hl7Field.setType(hl7FieldXmlDto.getType());
        hl7Field.setMinOccurrence(hl7FieldXmlDto.getMinOccurrence());
        hl7Field.setMaxOccurrence(hl7FieldXmlDto.getMaxOccurrence());

        return hl7Field;
    }

    private void addField(NaaccrHl7Field field) {
        // update collection of fields
        _fields.add(field);

        // update name cache
        _cachedByName.put(field.getName(), field);
    }

    public Hl7Message readNextMessage(LineNumberReader reader) throws IOException {
        return fetchNextMessage(reader);
    }

    public List<Hl7Message> readAllMessages(File file) throws IOException {
        List<Hl7Message> result = new ArrayList<>();
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            Hl7Message message = readNextMessage(reader);
            while (message != null) {
                result.add(message);
                message = readNextMessage(reader);
            }
        }
        return result;
    }

    public void writeMessage(Writer writer, Hl7Message message) throws IOException {
        writer.write(Hl7Utils.messageToString(message));
        writer.write(System.getProperty("line.separator"));
    }

    public void writeMessages(File file, List<Hl7Message> messages) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
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
                if (line.length() > 3)
                    Hl7Utils.segmentFromString(msg, line);

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
