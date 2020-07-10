/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.LayoutUtils;
import com.imsweb.layout.hl7.entity.Hl7Message;
import com.imsweb.layout.hl7.entity.Hl7Segment;
import com.imsweb.layout.hl7.xml.Hl7ComponentXmlDto;
import com.imsweb.layout.hl7.xml.Hl7FieldXmlDto;
import com.imsweb.layout.hl7.xml.Hl7LayoutXmlDto;
import com.imsweb.layout.hl7.xml.Hl7SegmentXmlDto;
import com.imsweb.layout.hl7.xml.Hl7SubComponentXmlDto;

/**
 * Provides the functionality for reading/writing NAACCR HL7 messages.
 */
public class NaaccrHl7Layout implements Layout {

    // layout ID
    protected String _layoutId;

    // layout name
    protected String _layoutName;

    // layout version
    protected String _layoutVersion;

    // layout description
    protected String _layoutDesc;

    // the HL7 specifications version
    protected String _hl7Specifications;

    // the fields for this layout
    protected List<NaaccrHl7Field> _fields = new ArrayList<>();

    // cached fields by name
    protected Map<String, NaaccrHl7Field> _cachedByName = new HashMap<>();

    /**
     * Constructor.
     * @param layoutId layout ID, cannot be null
     * @param layoutVersion layout version, cannot be null
     * @param loadFields if true, then load the fields
     */
    public NaaccrHl7Layout(String layoutId, String layoutVersion, boolean loadFields) {

        // for now only one HL7 version is supported, so I am hard-coding it...
        _hl7Specifications = "2.5.1";

        // optimization - if we don't need to load the fields, then don't load the XML at all!
        Hl7LayoutXmlDto layoutXmlDto = new Hl7LayoutXmlDto();
        layoutXmlDto.setId(layoutId);
        layoutXmlDto.setName(LayoutFactory.getAvailableInternalLayouts().get(layoutId));
        layoutXmlDto.setVersion(layoutVersion);
        layoutXmlDto.setDescription("NAACCR HL7 version " + layoutVersion);

        try {
            if (loadFields) {
                String xmlFilename = layoutId + "-layout.xml";
                Hl7LayoutXmlDto tmpXmlLayout = LayoutUtils.readHl7Layout(Thread.currentThread().getContextClassLoader().getResourceAsStream("layout/hl7/naaccr/" + xmlFilename));
                layoutXmlDto.setHl7Segments(tmpXmlLayout.getHl7Segments());
            }

            init(layoutXmlDto);
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to instantiate NAACCR HL7 layout", e);
        }
    }

    /**
     * Constructor.
     * @param layoutUrl URL to the XML definition, cannot be null
     * @throws IOException if the XML definition is not valid
     */
    public NaaccrHl7Layout(URL layoutUrl) throws IOException {
        if (layoutUrl == null)
            throw new NullPointerException("Unable to create HL7 layout from null URL");

        try (InputStream is = layoutUrl.openStream()) {
            init(LayoutUtils.readHl7Layout(is));
        }
    }

    /**
     * Constructor.
     * @param layoutFile XML definition, cannot be null, must exist
     * @throws IOException if the XML definition is not valid
     */
    public NaaccrHl7Layout(File layoutFile) throws IOException {
        if (layoutFile == null)
            throw new NullPointerException("Unable to create HL7-layout from null file");
        if (!layoutFile.exists())
            throw new IOException("Unable to read from " + layoutFile.getPath());

        try (InputStream is = new FileInputStream(layoutFile)) {
            init(LayoutUtils.readHl7Layout(is));
        }
    }

    /**
     * Constructor.
     * @param layoutXmlDto java representation of the XML definition, cannot be null
     * @throws IOException if the XML definition is not valid
     */
    public NaaccrHl7Layout(Hl7LayoutXmlDto layoutXmlDto) throws IOException {
        if (layoutXmlDto == null)
            throw new NullPointerException("Unable to create HL7-layout from null XML object");

        init(layoutXmlDto);
    }

    // helper
    protected void init(Hl7LayoutXmlDto layoutXmlDto) throws IOException {

        _layoutId = layoutXmlDto.getId();
        _layoutName = layoutXmlDto.getName();
        _layoutVersion = layoutXmlDto.getVersion();
        _layoutDesc = layoutXmlDto.getDescription();

        _fields.clear();
        for (Hl7SegmentXmlDto segmentXmlDto : layoutXmlDto.getHl7Segments()) {
            for (Hl7FieldXmlDto fieldXmlDto : segmentXmlDto.getHl7Fields()) {
                NaaccrHl7Field field = createFieldFromXmlField(fieldXmlDto);
                List<NaaccrHl7Field> subFields = new ArrayList<>();
                for (Hl7ComponentXmlDto componentXmlDto : fieldXmlDto.getHl7Components()) {
                    NaaccrHl7Field subField = createFieldFromXmlComponent(componentXmlDto);
                    List<NaaccrHl7Field> subSubFields = new ArrayList<>();
                    for (Hl7SubComponentXmlDto subComponentXmlDto : componentXmlDto.getHl7SubComponents())
                        subSubFields.add(createFieldFromXmlSubComponent(subComponentXmlDto));
                    if (!subSubFields.isEmpty())
                        subField.setSubFields(subSubFields);
                    subFields.add(subField);
                }
                if (!subFields.isEmpty())
                    field.setSubFields(subFields);
                _fields.add(field);
                _cachedByName.put(field.getName(), field);
            }
        }
    }

    // helper
    protected NaaccrHl7Field createFieldFromXmlField(Hl7FieldXmlDto hl7FieldXmlDto) throws IOException {
        NaaccrHl7Field field = new NaaccrHl7Field();

        field.setName(hl7FieldXmlDto.getName());
        field.setIdentifier(hl7FieldXmlDto.getIdentifier());
        field.setLongLabel(hl7FieldXmlDto.getLongLabel());
        field.setType(hl7FieldXmlDto.getType());
        field.setMinOccurrence(hl7FieldXmlDto.getMinOccurrence());
        field.setMaxOccurrence(hl7FieldXmlDto.getMaxOccurrence());
        if (hl7FieldXmlDto.getMinOccurrence() > hl7FieldXmlDto.getMaxOccurrence())
            throw new IOException("Field " + field.getName() + " defines a minimum occurrence that is greater than the maximum occurrence");

        return field;
    }

    // helper
    protected NaaccrHl7Field createFieldFromXmlComponent(Hl7ComponentXmlDto hl7ComponentXmlDto) {
        NaaccrHl7Field field = new NaaccrHl7Field();

        field.setName(hl7ComponentXmlDto.getName());
        field.setIdentifier(hl7ComponentXmlDto.getIdentifier());
        field.setLongLabel(hl7ComponentXmlDto.getLongLabel());
        field.setType(hl7ComponentXmlDto.getType());

        return field;
    }

    // helper
    protected NaaccrHl7Field createFieldFromXmlSubComponent(Hl7SubComponentXmlDto hl7FSubComponentXmlDto) {
        NaaccrHl7Field field = new NaaccrHl7Field();

        field.setName(hl7FSubComponentXmlDto.getName());
        field.setIdentifier(hl7FSubComponentXmlDto.getIdentifier());
        field.setLongLabel(hl7FSubComponentXmlDto.getLongLabel());
        field.setType(hl7FSubComponentXmlDto.getType());

        return field;
    }

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

    public String getHl7Specifications() {
        return _hl7Specifications;
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

        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(LayoutUtils.createInputStream(file, zipEntryName), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while (line != null && reader.getLineNumber() < 25 && result == null) {
                if (line.startsWith("MSH")) {
                    Hl7Segment segment = Hl7Utils.segmentFromString(new Hl7Message(), line);

                    String hl7Version = segment.getField(12).getComponent(1).getValue();
                    if (_hl7Specifications.equals(hl7Version)) {
                        String expectedProfileIdentifier = "VOL_V_" + _layoutVersion.replace(".", "") + "_ORU_R01";
                        String profileIdentifier = segment.getField(21).getComponent(1).getValue();
                        if (expectedProfileIdentifier.equals(profileIdentifier)) {
                            result = new LayoutInfo();
                            result.setLayoutId(getLayoutId());
                            result.setLayoutName(getLayoutName());
                        }
                    }
                    break;
                }
                else if (line.startsWith("PID"))
                    break;
                line = reader.readLine();
            }
        }
        catch (IOException e) {
            // ignored, result will be null
        }

        return result;
    }

    /**
     * Reads the next message from the given reader; this method won't close the reader.
     */
    public Hl7Message readNextMessage(LineNumberReader reader) throws IOException {
        return readNextMessage(reader, new Hl7LayoutOptions());
    }

    /**
     * Reads the next message from the given reader; this method won't close the reader.
     */
    public Hl7Message readNextMessage(LineNumberReader reader, Hl7LayoutOptions options) throws IOException {
        return fetchNextMessage(reader, options);
    }

    /**
     * Reads all the messages from the given file.
     */
    public List<Hl7Message> readAllMessages(File file) throws IOException {
        return readAllMessages(file, new Hl7LayoutOptions());
    }

    /**
     * Reads all the messages from the given file.
     */
    public List<Hl7Message> readAllMessages(File file, Hl7LayoutOptions options) throws IOException {
        List<Hl7Message> result = new ArrayList<>();
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(LayoutUtils.createInputStream(file), options.getEncoding()))) {
            Hl7Message message = readNextMessage(reader, options);
            while (message != null) {
                result.add(message);
                message = readNextMessage(reader, options);
            }
        }
        return result;
    }

    /**
     * Writes the given message on the given writer; this method won't close the writer.
     */
    public void writeMessage(Writer writer, Hl7Message message) throws IOException {
        writeMessage(writer, message, new Hl7LayoutOptions());
    }

    /**
     * Writes the given message on the given writer; this method won't close the writer.
     */
    public void writeMessage(Writer writer, Hl7Message message, Hl7LayoutOptions options) throws IOException {
        writer.write(Hl7Utils.messageToString(message, options));
        writer.write(options.getLineSeparator());
    }

    /**
     * Writes the given messages in the given file, they will be separated by a blank line.
     */
    public void writeMessages(File file, List<Hl7Message> messages) throws IOException {
        writeMessages(file, messages, new Hl7LayoutOptions());
    }

    /**
     * Writes the given messages in the given file, they will be separated by a blank line.
     */
    public void writeMessages(File file, List<Hl7Message> messages, Hl7LayoutOptions options) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(LayoutUtils.createOutputStream(file), options.getEncoding()))) {
            for (Hl7Message message : messages) {
                writeMessage(writer, message);
                writer.write(options.getLineSeparator());
            }
        }
    }

    // helper
    protected Hl7Message fetchNextMessage(LineNumberReader reader, Hl7LayoutOptions options) throws IOException {
        Hl7Message msg = null;

        String line = reader.readLine();

        // ignore the FHS and BHS line (those are transmission segments that we don't use)
        while (line != null && (line.startsWith("FHS") || line.startsWith("BHS")))
            line = reader.readLine();

        // MSH should immediately follow FHS or BHS otherwise it's bad format
        while (line != null && !line.startsWith("MSH"))
            line = reader.readLine();

        // if we found the header, create the message
        if (line != null) {
            msg = new Hl7Message();
            msg.setLineNumber(reader.getLineNumber());

            // then read the block of text
            while (line != null && !line.trim().isEmpty()) {
                if (line.length() > 3)
                    Hl7Utils.segmentFromString(msg, line, options);

                // peek for the next three bytes to determine if next line is a message starter
                reader.mark(8192); // this is the default buffer size for the BufferedReader
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
