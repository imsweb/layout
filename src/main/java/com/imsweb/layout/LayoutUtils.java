/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.layout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import com.imsweb.layout.hl7.xml.Hl7LayoutDefinitionXmlDto;
import com.imsweb.layout.record.csv.xml.CommaSeparatedLayoutXmlDto;
import com.imsweb.layout.record.fixed.xml.FixedColumnLayoutXmlDto;

/**
 * Generic utility class.
 * <p/>
 * Created on Aug 16, 2011 by depryf
 * @author depryf
 */
public final class LayoutUtils {

    /**
     * Private constructor, no instanciation!
     * <p/>
     * Created on Aug 16, 2011 by depryf
     */
    private LayoutUtils() {
    }

    private static XStream createFixedColumnsXStream() {
        XStream xstream = new XStream(new StaxDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new PrettyPrintWriter(out, "    ");
            }
        });
        xstream.autodetectAnnotations(true);
        xstream.alias("fixed-column-layout", FixedColumnLayoutXmlDto.class);
        return xstream;
    }

    /**
     * Reads the layout from the provided URL, expects XML format.
     * <p/>
     * The provided stream will be closed when this method returns
     * <p/>
     * Created on Dec 21, 2010 by depryf
     * @param stream <code>InputStream</code> to the data file, cannot be null
     * @return a <code>FixedColumnLayoutXmlDto</code>, never null
     * @throws IOException
     */
    public static FixedColumnLayoutXmlDto readFixedColumnsLayout(InputStream stream) throws IOException {
        if (stream == null)
            throw new IOException("Unable to read layout, target input stream is null");

        try (InputStream is = stream) {
            return (FixedColumnLayoutXmlDto)createFixedColumnsXStream().fromXML(is);
        }
        catch (RuntimeException e) {
            throw new IOException("Unable to read XML layout: " + e.getMessage(), e);
        }
    }

    /**
     * Writes the layout to the provided output stream, using XML format.
     * <p/>
     * Created on Dec 21, 2010 by depryf
     * @param stream <code>OutputStream</code> to the data file, cannot be null
     * @param layout the <code>FixedColumnLayoutXmlDto</code> to write, cannot be null
     * @throws IOException
     */
    public static void writeFixedColumnsLayout(OutputStream stream, FixedColumnLayoutXmlDto layout) throws IOException {
        if (layout == null)
            throw new IOException("Unable to write NULL layout");
        if (stream == null)
            throw new IOException("Unable to write layout for '" + layout.getId() + "', target output stream is null");

        try (Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write(System.lineSeparator());
            createFixedColumnsXStream().toXML(layout, writer);
        }
        catch (RuntimeException e) {
            throw new IOException("Unable to write XML layout: " + e.getMessage(), e);
        }
    }

    private static XStream createCommaSeparatedXStream() {
        XStream xstream = new XStream(new StaxDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new PrettyPrintWriter(out, "    ");
            }
        });
        xstream.autodetectAnnotations(true);
        xstream.alias("comma-separated-layout", CommaSeparatedLayoutXmlDto.class);
        return xstream;
    }

    /**
     * Reads the layout from the provided URL, expects XML format.
     * <p/>
     * The provided stream will be closed when this method returns
     * <p/>
     * Created on Dec 21, 2010 by depryf
     * @param stream <code>InputStream</code> to the data file, cannot be null
     * @return a <code>CommaSeparatedLayoutXmlDto</code>, never null
     * @throws IOException
     */
    public static CommaSeparatedLayoutXmlDto readCommaSeparatedLayout(InputStream stream) throws IOException {
        if (stream == null)
            throw new IOException("Unable to read layout, target input stream is null");
        try (InputStream is = stream) {
            return (CommaSeparatedLayoutXmlDto)createCommaSeparatedXStream().fromXML(is);
        }
        catch (RuntimeException e) {
            throw new IOException("Unable to read XML layout: " + e.getMessage(), e);
        }
    }

    /**
     * Writes the layout to the provided output stream, using XML format.
     * <p/>
     * Created on Dec 21, 2010 by depryf
     * @param stream <code>OutputStream</code> to the data file, cannot be null
     * @param layout the <code>CommaSeparatedLayoutXmlDto</code> to write, cannot be null
     * @throws IOException
     */
    public static void writeCommaSeparatedLayout(OutputStream stream, CommaSeparatedLayoutXmlDto layout) throws IOException {
        if (layout == null)
            throw new IOException("Unable to write NULL layout");
        if (stream == null)
            throw new IOException("Unable to write layout for '" + layout.getId() + "', target output stream is null");

        try (Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write(System.lineSeparator());
            createCommaSeparatedXStream().toXML(layout, writer);
        }
        catch (RuntimeException e) {
            throw new IOException("Unable to write XML layout: " + e.getMessage(), e);
        }
    }

    private static XStream createNaaccrHl7XStream() {
        XStream xstream = new XStream(new StaxDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new PrettyPrintWriter(out, "    ");
            }
        });
        xstream.autodetectAnnotations(true);
        xstream.alias("hl7-layout", Hl7LayoutDefinitionXmlDto.class);
        return xstream;
    }

    /**
     * Reads the layout from the provided URL, expects XML format.
     * <p/>
     * The provided stream will be closed when this method returns
     * <p/>
     * Created on Mar 20, 2017 by depryf
     * @param stream <code>InputStream</code> to the data file, cannot be null
     * @return a <code>Hl7LayoutDefinitionXmlDto</code>, never null
     * @throws IOException
     */
    public static Hl7LayoutDefinitionXmlDto readHl7Layout(InputStream stream) throws IOException {
        if (stream == null)
            throw new IOException("Unable to read layout, target input stream is null");

        try (InputStream is = stream) {
            return (Hl7LayoutDefinitionXmlDto)createNaaccrHl7XStream().fromXML(is);
        }
        catch (RuntimeException e) {
            throw new IOException("Unable to read XML layout: " + e.getMessage(), e);
        }
    }

    /**
     * Writes the layout to the provided output stream, using XML format.
     * <p/>
     * Created on Mar 20, 2017 by depryf
     * @param stream <code>OutputStream</code> to the data file, cannot be null
     * @param layout the <code>Hl7LayoutDefinitionXmlDto</code> to write, cannot be null
     * @throws IOException
     */
    public static void writeHl7Layout(OutputStream stream, Hl7LayoutDefinitionXmlDto layout) throws IOException {
        if (layout == null)
            throw new IOException("Unable to write NULL layout");
        if (stream == null)
            throw new IOException("Unable to write layout for '" + layout.getId() + "', target output stream is null");

        try (Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.write(System.lineSeparator());
            createNaaccrHl7XStream().toXML(layout, writer);
        }
        catch (RuntimeException e) {
            throw new IOException("Unable to write XML layout: " + e.getMessage(), e);
        }
    }

    /**
     * Creates an <code>InputStream</code> for the provided file based on its extension:
     * <ul>
     * <li>if it ends with '.gz' or '.gzip', it will be considered as a GZipped file</li>
     * <li>if it ends with '.zip', it will be considered as a Zipped file but an error will be generated if it contains more than one entry</li>
     * <li>otherwise it is considered as a non-compressed file</li>
     * </ul>
     * <p/>
     * Created on Sep 19, 2011 by depryf
     * @param file <code>File</code>, cannot be null (an exception will be thrown if it does not exist)
     * @return an <code>InputStream</code>, never null
     * @throws IOException
     */
    public static InputStream createInputStream(File file) throws IOException {
        return createInputStream(file, null);
    }

    /**
     * Creates an <code>InputStream</code> for the provided file based on its extension:
     * <ul>
     * <li>if it ends with '.gz' or '.gzip', it will be considered as a GZipped file</li>
     * <li>if it ends with '.zip', it will be considered as a Zipped file:
     * <ul>
     * <li>If the file contains no entry, an exception is generated</li>
     * <li>If the file contains a single entry, a stream to that entry will be returned</li>
     * <li>If the file contains more than one entry and zipEntryToUse was provided, a stream to that entry will be returned</li>
     * <li>Otherwise an IOException will be generated</li>
     * </ul>
     * </li>
     * <li>otherwise it is considered as a non-compressed file</li>
     * </ul>
     * <p/>
     * Created on Sep 19, 2011 by depryf
     * @param file <code>File</code>, cannot be null (an exception will be thrown if it does not exist)
     * @param zipEntryToUse if the zip file contains more than one entry
     * @return an <code>InputStream</code>, never null
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static InputStream createInputStream(File file, String zipEntryToUse) throws IOException {
        if (file == null || !file.exists())
            throw new IOException("File does not exist.");

        String name = file.getName().toLowerCase();

        InputStream is;
        if (name.endsWith(".gz") || name.endsWith(".gzip"))
            is = new GZIPInputStream(new FileInputStream(file));
        else if (name.endsWith(".zip")) {
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            // count the number of entries
            List<String> list = new ArrayList<>();
            while (entries.hasMoreElements())
                list.add(entries.nextElement().getName());
            // can't be empty
            if (list.isEmpty())
                throw new IOException("Zip file is empty.");
            InputStream tmp;
            // if only one, just take that one...
            if (list.size() == 1)
                zipEntryToUse = list.get(0);

            if (list.contains(zipEntryToUse))
                tmp = zipFile.getInputStream(zipFile.getEntry(zipEntryToUse));
            else
                throw new IOException("Zip file contains more than one file.");

            // zip file could contain another compressed file; we are only supporting gzip or uncompressed!
            if ((zipEntryToUse.endsWith(".gz") || zipEntryToUse.endsWith(".gzip")))
                is = new GZIPInputStream(tmp);
            else if (zipEntryToUse.endsWith(".zip"))
                throw new IOException("Zip files inside zip files is not supported.");
            else
                is = tmp;
        }
        else
            is = new FileInputStream(file);

        return is;
    }

    /**
     * Creates an <code>OutputStream</code> for the provided file based on its extension:
     * <ul>
     * <li>if it ends with '.gz' or '.gzip', it will be considered as a GZipped file</li>
     * <li>if it ends with '.zip', it will be considered as a Zipped file (the caller is responsible for adding the entries)</li>
     * <li>otherwise it is considered as a non-compressed file</li>
     * </ul>
     * <p/>
     * Created on Sep 19, 2011 by depryf
     * @param file <code>File</code>, cannot be null (an exception will be thrown if it does not exist)
     * @return an <code>OutputStream</code>, never null
     * @throws IOException
     */
    public static OutputStream createOutputStream(File file) throws IOException {
        OutputStream os;

        String name = file.getName().toLowerCase();

        if (name.endsWith(".gz") || name.endsWith(".gzip"))
            os = new GZIPOutputStream(new FileOutputStream(file));
        else if (name.endsWith(".zip"))
            os = new ZipOutputStream(new FileOutputStream(file));
        else
            os = new FileOutputStream(file);

        return os;
    }

    /**
     * Format the passed number, added commas for the decimal parts.
     * <p/>
     * Created on Dec 3, 2008 by depryf
     * @param num number to format
     * @return formatted number
     */
    public static String formatNumber(int num) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalSeparatorAlwaysShown(false);
        return format.format(num);
    }

    /**
     * Takes a string with a byte count and converts it into a "nice" representation of size.
     * <p/>
     * 124 b <br>
     * 34 KB <br>
     * 12 MB <br>
     * 2 GB
     * <p/>
     * Created on May 281, 2004 by Chuck May
     * @param size size to format
     * @return <code>String</code> with the formatted size
     */
    public static String formatFileSize(long size) {
        if (size < 1024)
            return size + " B";
        else if (size < 1024 * 1024)
            return new DecimalFormat("#.# KB").format((double)size / 1024);
        else if (size < 1024 * 1024 * 1024)
            return new DecimalFormat("#.# MB").format((double)size / 1024 / 1024);

        return new DecimalFormat("#.# GB").format((double)size / 1024 / 1024 / 1024);
    }

    /**
     * Formats a time given in millisecond. The output will be "X hours Y min Z sec", unless X, Y or Z is 0 in which
     * case that part of the string will be omitted.
     * <p/>
     * Created on May 3, 2004 by Fabian Depry
     * @param timeInMilli time in milli-seconds
     * @return a <code>String</code> representing the formatted time...
     */
    public static String formatTime(long timeInMilli) {
        long hourBasis = 60;

        StringBuilder formattedTime = new StringBuilder();

        long secTmp = timeInMilli / 1000;
        long sec = secTmp % hourBasis;
        long minTmp = secTmp / hourBasis;
        long min = minTmp % hourBasis;
        long hour = minTmp / hourBasis;

        if (hour > 0) {
            formattedTime.append(hour).append(" hour");
            if (hour > 1)
                formattedTime.append("s");
        }

        if (min > 0) {
            if (formattedTime.length() > 0)
                formattedTime.append(", ");
            formattedTime.append(min).append(" minute");
            if (min > 1)
                formattedTime.append("s");
        }

        if (sec > 0) {
            if (formattedTime.length() > 0)
                formattedTime.append(", ");
            formattedTime.append(sec).append(" second");
            if (sec > 1)
                formattedTime.append("s");
        }

        if (formattedTime.length() > 0)
            return formattedTime.toString();

        return "< 1 second";
    }

    /**
     * Pad the passed value up to the passed length using the passed string
     * <p/>
     * Created on Dec 3, 2008 by depryf
     * @param value value to pad
     * @param length length of the result
     * @param with character to pad with
     * @param leftPad if true value will be left padded, otherwise it will be right padded
     * @return padded value, maybe null
     */
    public static String pad(String value, int length, String with, boolean leftPad) {
        if (value == null || value.length() >= length)
            return value;

        StringBuilder builder = new StringBuilder(value);
        while (builder.length() < length)
            if (leftPad)
                builder.insert(0, with);
            else
                builder.append(with);

        return builder.toString();
    }
}
