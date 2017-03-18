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

import com.imsweb.layout.Field;
import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.hl7.entity.Hl7Message;

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
        writer.write(Hl7Utils.messageToString(message));
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
