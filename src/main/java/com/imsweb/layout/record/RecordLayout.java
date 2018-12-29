/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.layout.record;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.LayoutUtils;
import com.imsweb.layout.record.csv.CommaSeparatedLayout;

/**
 * Abstract implementation of a layout, contains the logic/fields that is common to all record layouts.
 * <p/>
 * Created on Jun 26, 2012 by Fabian
 * @author Fabian
 */
public abstract class RecordLayout implements Layout {

    // layout ID
    protected String _layoutId;

    // layout Name
    protected String _layoutName;

    // layout version
    protected String _layoutVersion;

    // layout Description
    protected String _layoutDesc;

    // parent layout ID
    protected String _parentLayoutId;

    /**
     * Constructor.
     */
    public RecordLayout() {
    }

    @Override
    public String getLayoutId() {
        return _layoutId;
    }

    public void setLayoutId(String id) {
        _layoutId = id;
    }

    @Override
    public String getLayoutName() {
        return _layoutName;
    }

    public void setLayoutName(String name) {
        _layoutName = name;
    }

    @Override
    public String getLayoutVersion() {
        return _layoutVersion;
    }

    public void setLayoutVersion(String version) {
        _layoutVersion = version;
    }

    @Override
    public String getLayoutDescription() {
        return _layoutDesc;
    }

    public void setLayoutDescription(String description) {
        _layoutDesc = description;
    }

    public void setLayoutNumberOfFields(Integer numFields) {
        throw new RuntimeException("Number of fields is not supported for this layout");
    }

    public String getParentLayoutId() {
        return _parentLayoutId;
    }

    @Override
    public String getFieldDocByName(String name) {
        if (_parentLayoutId != null)
            return LayoutFactory.getLayout(_parentLayoutId).getFieldDocByName(name);
        return null;
    }

    @Override
    public String getFieldDocByNaaccrItemNumber(Integer num) {
        if (_parentLayoutId != null)
            return LayoutFactory.getLayout(_parentLayoutId).getFieldDocByNaaccrItemNumber(num);
        return null;
    }

    @Override
    public String getFieldDocDefaultCssStyle() {
        if (_parentLayoutId != null)
            return LayoutFactory.getLayout(_parentLayoutId).getFieldDocDefaultCssStyle();
        return "";
    }

    protected boolean trimValues(RecordLayoutOptions options) {
        return options == null || options.trimValues();
    }

    protected boolean enforceStrictFormat(RecordLayoutOptions options) {
        return options != null && options.enforceStrictFormat();
    }

    protected boolean applyAlignment(RecordLayoutOptions options) {
        return options == null || options.applyAlignment();
    }

    protected boolean applyPadding(RecordLayoutOptions options) {
        return options == null || options.applyPadding();
    }

    protected String getLineSeparator(RecordLayoutOptions options) {
        return options == null ? System.lineSeparator() : options.getLineSeparator();
    }

    protected Charset getEncoding(RecordLayoutOptions options) {
        return options == null ? StandardCharsets.UTF_8 : options.getEncoding();
    }

    /**
     * Writes the record to the output stream, followed be a line separator. Does not close or open output stream.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param outputStream outputStream to write to
     * @param record record to be written
     */
    public void writeRecord(OutputStream outputStream, Map<String, String> record) throws IOException {
        writeRecord(outputStream, record, null);
    }

    /**
     * Writes the record to the output stream, followed be a line separator. Does not close or open output stream.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param outputStream outputStream to write to
     * @param record record to be written
     * @param options options to use (null means default option values will be used)
     */
    public void writeRecord(OutputStream outputStream, Map<String, String> record, RecordLayoutOptions options) throws IOException {
        outputStream.write(createLineFromRecord(record, options).getBytes(getEncoding(options)));
        outputStream.write(getLineSeparator(options).getBytes(getEncoding(options)));
    }

    /**
     * Writes the records to the outputstream, each followed be a line separator. Does not close or open output stream.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param outputStream OutputStream to write to
     * @param records Records to be written to the OuputStream
     */
    public void writeRecords(OutputStream outputStream, List<Map<String, String>> records) throws IOException {
        writeRecords(outputStream, records, null);
    }

    /**
     * Writes the records to the outputstream, each followed be a line separator. Does not close or open output stream.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param outputStream OutputStream to write to
     * @param records Records to be written to the OuputStream
     * @param options options to use (null means default option values will be used)
     */
    public void writeRecords(OutputStream outputStream, List<Map<String, String>> records, RecordLayoutOptions options) throws IOException {
        for (Map<String, String> record : records) {
            outputStream.write(createLineFromRecord(record, options).getBytes(getEncoding(options)));
            outputStream.write(getLineSeparator(options).getBytes(getEncoding(options)));
        }
    }

    /**
     * Writes the record to the writer, followed be a line separator. Does not close or open writer.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param writer Writer to write to
     * @param record Record to be written to the Writer
     */
    public void writeRecord(Writer writer, Map<String, String> record) throws IOException {
        writeRecord(writer, record, null);
    }

    /**
     * Writes the record to the writer, followed be a line separator. Does not close or open writer.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param writer Writer to write to
     * @param record Record to be written to the Writer
     * @param options options to use (null means default option values will be used)
     */
    public void writeRecord(Writer writer, Map<String, String> record, RecordLayoutOptions options) throws IOException {
        writer.write(createLineFromRecord(record, options));
        writer.write(getLineSeparator(options));
    }

    /**
     * Writes the records to the writer, each followed be a line separator. Does not close or open writer.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param writer Writer to write to
     * @param records Records to be written to the Writer
     */
    public void writeRecords(Writer writer, List<Map<String, String>> records) throws IOException {
        writeRecords(writer, records, null);
    }

    /**
     * Writes the records to the writer, each followed be a line separator. Does not close or open writer.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param writer Writer to write to
     * @param records Records to be written to the Writer
     * @param options options to use (null means default option values will be used)
     */
    public void writeRecords(Writer writer, List<Map<String, String>> records, RecordLayoutOptions options) throws IOException {
        for (Map<String, String> record : records) {
            writer.write(createLineFromRecord(record, options));
            writer.write(getLineSeparator(options));
        }
    }

    /**
     * Writes the record to the file, followed be a line separator.
     * <p/>
     * The line separator will be the default one for the current system.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param file File to write to
     * @param record Record to be written to the File
     */
    public void writeRecord(File file, Map<String, String> record) throws IOException {
        writeRecords(file, Collections.singletonList(record));
    }

    /**
     * Writes the record to the file, followed be a line separator.
     * <p/>
     * The line separator will be the default one for the current system.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param file File to write to
     * @param record Record to be written to the File
     * @param options options to use (null means default option values will be used)
     */
    public void writeRecord(File file, Map<String, String> record, RecordLayoutOptions options) throws IOException {
        writeRecords(file, Collections.singletonList(record), options);
    }

    /**
     * Writes the records to the file, each followed be a line separator.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param file File to write to
     * @param records Records to be written to the File
     */
    public void writeRecords(File file, List<Map<String, String>> records) throws IOException {
        writeRecords(file, records, null);
    }

    /**
     * Writes the records to the file, each followed be a line separator.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param file File to write to
     * @param records Records to be written to the File
     * @param options options to use (null means default option values will be used)
     */
    public void writeRecords(File file, List<Map<String, String>> records, RecordLayoutOptions options) throws IOException {
        try (BufferedOutputStream out = new BufferedOutputStream(LayoutUtils.createOutputStream(file))) {
            for (Map<String, String> record : records) {
                out.write(createLineFromRecord(record, options).getBytes(getEncoding(options)));
                out.write(getLineSeparator(options).getBytes(getEncoding(options)));
            }
        }
    }

    /**
     * Returns a record that is created from the String returned when readLine() is called on the LineReader passed in.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param lineReader Used to get the next line from a file; Cannot be null
     * @return A map of the data from the line read by the LineNumberReader
     */
    public Map<String, String> readNextRecord(LineNumberReader lineReader) throws IOException {
        return readNextRecord(lineReader, null);
    }

    /**
     * Returns a record that is created from the String returned when readLine() is called on the LineReader passed in.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param lineReader Used to get the next line from a file; Cannot be null
     * @param options options to use (null means default option values will be used)
     * @return A map of the data from the line read by the LineNumberReader
     */
    public Map<String, String> readNextRecord(LineNumberReader lineReader, RecordLayoutOptions options) throws IOException {
        String line = lineReader.readLine();
        if (line == null)
            return null;
        return createRecordFromLine(line, lineReader.getLineNumber(), options);
    }

    /**
     * Returns records that are created from the data in the input stream passed in.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param inputStream Stream to the data
     * @param options options to use (null means default option values will be used)
     * @return A list of the records created from the data in the InputStream
     */
    public List<Map<String, String>> readAllRecords(InputStream inputStream, RecordLayoutOptions options) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();

        LineNumberReader r = new LineNumberReader(new InputStreamReader(inputStream, getEncoding(options)));
        String line;
        while ((line = r.readLine()) != null)
            result.add(createRecordFromLine(line, r.getLineNumber(), options));

        return result;
    }

    /**
     * Returns records that are created from the data given from the reader passed in.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param reader Reader containing the data
     * @return A list of the records created from the data read from the Reader
     */
    public List<Map<String, String>> readAllRecords(Reader reader) throws IOException {
        return readAllRecords(reader, null);
    }

    /**
     * Returns records that are created from the data given from the reader passed in.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param reader Reader containing the data
     * @param options options to use (null means default option values will be used)
     * @return A list of the records created from the data read from the Reader
     */
    public List<Map<String, String>> readAllRecords(Reader reader, RecordLayoutOptions options) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();

        LineNumberReader r = new LineNumberReader(reader);
        String line;
        while ((line = r.readLine()) != null)
            result.add(createRecordFromLine(line, r.getLineNumber(), options));

        return result;
    }

    /**
     * Returns records that are created from the data in the file passed in.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param file File containing data
     * @return A list of the records created from the data in the File
     */
    public List<Map<String, String>> readAllRecords(File file) throws IOException {
        return readAllRecords(file, null, null);
    }

    /**
     * Returns records that are created from the data in the file passed in.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param file File containing data
     * @param options options to use (null means default option values will be used)
     * @return A list of the records created from the data in the File
     */
    public List<Map<String, String>> readAllRecords(File file, RecordLayoutOptions options) throws IOException {
        return readAllRecords(file, null, options);
    }

    /**
     * Returns records that are created from the data in the file passed in.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param file File containing data
     * @param zipEntry the zip entry to use in the file if it's a zip file (if none are provided and the file contains several entries, an exception will be thrown)
     * @param options options to use (null means default option values will be used)
     * @return A list of the records created from the data in the File
     */
    public List<Map<String, String>> readAllRecords(File file, String zipEntry, RecordLayoutOptions options) throws IOException {
        List<Map<String, String>> result = new ArrayList<>();

        try (LineNumberReader r = new LineNumberReader(new InputStreamReader(LayoutUtils.createInputStream(file, zipEntry), getEncoding(options)))) {

            // some CSV layout need to ignore the first line (see issue #2)
            if (this instanceof CommaSeparatedLayout && ((CommaSeparatedLayout)this).ignoreFirstLine())
                r.readLine();

            String line;
            while ((line = r.readLine()) != null)
                result.add(createRecordFromLine(line, r.getLineNumber(), options));
        }

        return result;
    }

    /**
     * Returns null if the provided line is valid for this layout, otherwise returns an error message.
     * <p/>
     * The layouts included in SEER*Utils (like NAACCR12) only check the line length; to add a more robust checking (like data validity),
     * one can override this method to include more complex logic.
     * <p/>
     * Created on Jul 28, 2011 by murphyr
     * @param line line to validate
     * @param lineNumber line number (optional)
     * @return null if the provided line is valid for this layout, otherwise returns the reason why it is not valid
     */
    public abstract String validateLine(String line, Integer lineNumber);

    /**
     * Converts the given data line into a map representing a record.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param line data line
     * @param lineNumber line number (use null if no line number available)
     * @param options the options to use to create the line (pass null to use all default options)
     * @return a map representing a record
     */
    public abstract Map<String, String> createRecordFromLine(String line, Integer lineNumber, RecordLayoutOptions options) throws IOException;

    /**
     * Converts the provided record into a data line.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param record record to convert
     * @param options the options to use to create the line (pass null to use all default options)
     * @return a data line
     */
    public abstract String createLineFromRecord(Map<String, String> record, RecordLayoutOptions options) throws IOException;

    /**
     * Returns a layout info object if this instance of a layout can handle the provided data line, returns null otherwise.
     * @param firstRecord first record data line
     * @param options discovery options
     * @return a layout info, maybe null
     */
    public abstract LayoutInfo buildFileInfo(String firstRecord, LayoutInfoDiscoveryOptions options);

    @Override
    public LayoutInfo buildFileInfo(File file, String zipEntryName, LayoutInfoDiscoveryOptions options) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(LayoutUtils.createInputStream(file, zipEntryName), getEncoding(null)))) {
            String firstLine = reader.readLine();
            if (firstLine == null)
                return null;
            return buildFileInfo(firstLine, options);
        }
        catch (IOException e) {
            return null;
        }
    }
}
