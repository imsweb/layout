/*
 * Copyright (C) 2012 Information Management Services, Inc.
 */
package com.imsweb.layout;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.layout.record.RecordLayout;
import com.imsweb.layout.record.csv.CommaSeparatedField;
import com.imsweb.layout.record.csv.CommaSeparatedLayout;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.FixedColumnsLayout;

public class LayoutFactoryTest {

    @Test
    public void testInternalLayouts() {
        Assert.assertFalse(LayoutFactory.getAvailableInternalLayouts().isEmpty());

        // test NAACCR 16
        Assert.assertTrue(LayoutFactory.getAvailableInternalLayouts().containsKey(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT));
        Assert.assertNotNull(LayoutFactory.getAvailableInternalLayouts().get(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT));

        // test NAACCR 15
        Assert.assertTrue(LayoutFactory.getAvailableInternalLayouts().containsKey(LayoutFactory.LAYOUT_ID_NAACCR_15_ABSTRACT));
        Assert.assertNotNull(LayoutFactory.getAvailableInternalLayouts().get(LayoutFactory.LAYOUT_ID_NAACCR_15_ABSTRACT));

        // test NAACCR 14
        Assert.assertTrue(LayoutFactory.getAvailableInternalLayouts().containsKey(LayoutFactory.LAYOUT_ID_NAACCR_14_ABSTRACT));
        Assert.assertNotNull(LayoutFactory.getAvailableInternalLayouts().get(LayoutFactory.LAYOUT_ID_NAACCR_14_ABSTRACT));

        // test NAACCR 13
        Assert.assertTrue(LayoutFactory.getAvailableInternalLayouts().containsKey(LayoutFactory.LAYOUT_ID_NAACCR_13_ABSTRACT));
        Assert.assertNotNull(LayoutFactory.getAvailableInternalLayouts().get(LayoutFactory.LAYOUT_ID_NAACCR_13_ABSTRACT));

        // test NAACCR 12
        Assert.assertTrue(LayoutFactory.getAvailableInternalLayouts().containsKey(LayoutFactory.LAYOUT_ID_NAACCR_12_ABSTRACT));
        Assert.assertNotNull(LayoutFactory.getAvailableInternalLayouts().get(LayoutFactory.LAYOUT_ID_NAACCR_12_ABSTRACT));

        // but "alias" layouts shouldn't be returned (because they are deprecated)
        Assert.assertFalse(LayoutFactory.getAvailableInternalLayouts().containsKey(LayoutFactory.LAYOUT_ID_NAACCR_14));
        Assert.assertFalse(LayoutFactory.getAvailableInternalLayouts().containsKey(LayoutFactory.LAYOUT_ID_NAACCR_13));
        Assert.assertFalse(LayoutFactory.getAvailableInternalLayouts().containsKey(LayoutFactory.LAYOUT_ID_NAACCR_12));

        // make sure the properties defined in 12 kept the same name in 13
        Layout l12 = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_12_ABSTRACT);
        Map<Integer, String> m12 = new HashMap<>();
        for (Field f : l12.getAllFields())
            m12.put(f.getNaaccrItemNum(), f.getName());
        Layout l13 = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_13_ABSTRACT);
        Map<Integer, String> m13 = new HashMap<>();
        for (Field f : l13.getAllFields())
            m13.put(f.getNaaccrItemNum(), f.getName());
        for (Entry<Integer, String> entry : m12.entrySet())
            if (m13.containsKey(entry.getKey()) && !m13.get(entry.getKey()).equals(entry.getValue()))
                Assert.fail(entry.getKey() + " - " + m12.get(entry.getKey()) + " - " + m13.get(entry.getKey()));

        // make sure the properties defined in 13 kept the same name in 14
        Layout l14 = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_14_ABSTRACT);
        Map<Integer, String> m14 = new HashMap<>();
        for (Field f : l14.getAllFields())
            m14.put(f.getNaaccrItemNum(), f.getName());
        for (Entry<Integer, String> entry : m13.entrySet())
            if (m14.containsKey(entry.getKey()) && !m14.get(entry.getKey()).equals(entry.getValue()))
                Assert.fail(entry.getKey() + " - " + m13.get(entry.getKey()) + " - " + m14.get(entry.getKey()));

        // make sure the properties defined in 14 kept the same name in 15
        Layout l15 = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_15_ABSTRACT);
        Map<Integer, String> m15 = new HashMap<>();
        for (Field f : l15.getAllFields())
            m15.put(f.getNaaccrItemNum(), f.getName());
        for (Entry<Integer, String> entry : m14.entrySet())
            if (m15.containsKey(entry.getKey()) && !m15.get(entry.getKey()).equals(entry.getValue()))
                Assert.fail(entry.getKey() + " - " + m14.get(entry.getKey()) + " - " + m15.get(entry.getKey()));

        // make sure the properties defined in 15 kept the same name in 16
        Layout l16 = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT);
        Map<Integer, String> m16 = new HashMap<>();
        for (Field f : l16.getAllFields())
            m16.put(f.getNaaccrItemNum(), f.getName());
        for (Entry<Integer, String> entry : m15.entrySet())
            if (m16.containsKey(entry.getKey()) && !m16.get(entry.getKey()).equals(entry.getValue()))
                Assert.fail(entry.getKey() + " - " + m15.get(entry.getKey()) + " - " + m16.get(entry.getKey()));
    }

    @Test
    public void testRegisterLayout() throws Exception {

        // trying to get a layout before it's registered
        LayoutFactory.unregisterAllLayouts();
        Assert.assertFalse(LayoutFactory.getAvailableLayouts().containsKey("test"));
        Assert.assertFalse(LayoutFactory.getAvailableLayouts().containsKey("test-csv"));
        Assert.assertNull(getLayout("test"));
        Assert.assertNull(getLayout("test-csv"));

        // register fixed from XML
        LayoutFactory.unregisterAllLayouts();
        LayoutFactory.registerLayout(new FixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-fixed-columns.xml")));
        Assert.assertTrue(LayoutFactory.getAvailableLayouts().containsKey("test"));
        Assert.assertNotNull(getLayout("test"));

        // unregister that particular layout
        LayoutFactory.unregisterLayout("test");
        Assert.assertFalse(LayoutFactory.getAvailableLayouts().containsKey("test"));
        Assert.assertNull(getLayout("test"));

        // register fixed programmatically
        LayoutFactory.unregisterAllLayouts();
        LayoutFactory.registerLayout(createFixedColumnTestingLayout());
        Assert.assertTrue(LayoutFactory.getAvailableLayouts().containsKey("test"));
        Assert.assertNotNull(getLayout("test"));

        // register csv from XML
        LayoutFactory.unregisterAllLayouts();
        LayoutFactory.registerLayout(new CommaSeparatedLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-comma-separated.xml")));
        Assert.assertTrue(LayoutFactory.getAvailableLayouts().containsKey("test-csv"));
        Assert.assertNotNull(getLayout("test-csv"));

        // register csv programmatically
        LayoutFactory.unregisterAllLayouts();
        LayoutFactory.registerLayout(createCommaSeparatedTestingLayout());
        Assert.assertTrue(LayoutFactory.getAvailableLayouts().containsKey("test-csv"));
        Assert.assertNotNull(getLayout("test-csv"));
    }

    @Test(expected = IOException.class)
    public void testRegisterLayoutBadFixedFromXml() throws IOException {
        LayoutFactory.registerLayout(new FixedColumnsLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-fixed-columns-bad.xml")));
    }

    @Test(expected = RuntimeException.class)
    public void testRegisterLayoutBadFixed() {
        FixedColumnsLayout layout = createFixedColumnTestingLayout();
        layout.setLayoutName(null);
        LayoutFactory.registerLayout(layout);
    }

    @Test(expected = IOException.class)
    public void testRegisterLayoutBadCsvFromXml() throws IOException {
        LayoutFactory.registerLayout(new CommaSeparatedLayout(Thread.currentThread().getContextClassLoader().getResource("testing-layout-comma-separated-bad.xml")));
    }

    @Test(expected = RuntimeException.class)
    public void testRegisterLayoutBadCsv() {
        CommaSeparatedLayout layout = createCommaSeparatedTestingLayout();
        layout.setLayoutName(null);
        LayoutFactory.registerLayout(layout);
    }

    private Layout getLayout(String layout) {
        try {
            return LayoutFactory.getLayout(layout);
        }
        catch (Exception e) {
            return null;
        }
    }

    private FixedColumnsLayout createFixedColumnTestingLayout() {
        FixedColumnsLayout layout = new FixedColumnsLayout();
        layout.setLayoutId("test");
        layout.setLayoutName("Test");
        layout.setLayoutLineLength(10);
        FixedColumnsField f1 = new FixedColumnsField();
        f1.setName("f1");
        f1.setStart(1);
        f1.setEnd(10);
        layout.setFields(Collections.singleton(f1));
        return layout;
    }

    private CommaSeparatedLayout createCommaSeparatedTestingLayout() {
        CommaSeparatedLayout layout = new CommaSeparatedLayout();
        layout.setLayoutId("test-csv");
        layout.setLayoutName("Test CSV");
        layout.setLayoutNumberOfFields(10);
        layout.setSeparator('|');
        layout.setIgnoreFirstLine(true);
        CommaSeparatedField f1 = new CommaSeparatedField();
        f1.setName("f1");
        f1.setIndex(1);
        f1.setMaxLength(10);
        layout.setFields(Collections.singleton(f1));
        return layout;
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testGetLayoutInfo() throws IOException {
        LayoutFactory.unregisterAllLayouts();

        LayoutInfoDiscoveryOptions options = new LayoutInfoDiscoveryOptions();

        // regular case, well formatted NAACCR lines
        Assert.assertFalse(LayoutFactory.discoverFormat(createNaaccrLine(22824, "A", "120"), options).isEmpty());
        Assert.assertFalse(LayoutFactory.discoverFormat(createNaaccrLine(22824, "M", "121"), options).isEmpty());
        Assert.assertFalse(LayoutFactory.discoverFormat(createNaaccrLine(5564, "C", "122"), options).isEmpty());
        Assert.assertFalse(LayoutFactory.discoverFormat(createNaaccrLine(3339, "I", "123"), options).isEmpty());
        LayoutInfo info = LayoutFactory.discoverFormat(createNaaccrLine(22824, null, null), options).get(0); // test the actual values
        Assert.assertEquals("NAACCR 16 Abstract", info.getLayoutName());
        Assert.assertEquals(22824, info.getLineLength().intValue());
        Assert.assertEquals("NAACCR 16 Abstract [22,824 char]", info.toString());

        // bad line numbers
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(25, "A", "120")).isEmpty());
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(25, "M", "121")).isEmpty());
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(25, "C", "122")).isEmpty());
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(25, "I", "123")).isEmpty());

        // bad record types
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(22824, "?", "120")).isEmpty());
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(22824, "?", "121")).isEmpty());
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(5564, "?", "122")).isEmpty());
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(3339, "?", "123")).isEmpty());

        // bad NAACCR version
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(22824, "A", "???")).isEmpty());
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(22824, "M", "???")).isEmpty());
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(5564, "C", "???")).isEmpty());
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(3339, "I", "???")).isEmpty());

        // no NAACCR version and not record type available, but we don't enforce the strict format, so the line length should be used
        Assert.assertFalse(LayoutFactory.discoverFormat(createNaaccrLine(22824, null, null)).isEmpty());
        Assert.assertFalse(LayoutFactory.discoverFormat(createNaaccrLine(5564, null, null)).isEmpty());
        Assert.assertFalse(LayoutFactory.discoverFormat(createNaaccrLine(3339, null, null)).isEmpty());
        info = LayoutFactory.discoverFormat(createNaaccrLine(22824, null, null)).get(0); // make sure it defaults to an abstract
        Assert.assertEquals("NAACCR 16 Abstract", info.getLayoutName());
        Assert.assertEquals(22824, info.getLineLength().intValue());

        // if we enforced the strict format, the line length won't be used anymore
        options.setNaaccrAllowBlankVersion(false);
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(22824, null, null), options).isEmpty());
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(5564, null, null), options).isEmpty());
        Assert.assertTrue(LayoutFactory.discoverFormat(createNaaccrLine(3339, null, null), options).isEmpty());

        // test the other flavors of the method (no need to be exhaustive here since they all end up calling the string one)
        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-naaccr-1-rec.txt");
        Assert.assertFalse(LayoutFactory.discoverFormat(new File(url.getPath().replace("%20", " "))).isEmpty()); // file
        url = Thread.currentThread().getContextClassLoader().getResource("fake-naaccr-1-rec.zip");
        Assert.assertFalse(LayoutFactory.discoverFormat(new File(url.getPath().replace("%20", " ")), "fake-naaccr-1-rec").isEmpty()); // file
        url = Thread.currentThread().getContextClassLoader().getResource("fake-naaccr-1000-recs.txt.gz");
        Assert.assertFalse(LayoutFactory.discoverFormat(new File(url.getPath().replace("%20", " "))).isEmpty()); // file
        url = Thread.currentThread().getContextClassLoader().getResource("empty-file.data");
        Assert.assertTrue(LayoutFactory.discoverFormat(new File(url.getPath().replace("%20", " "))).isEmpty()); // file
        Assert.assertTrue(LayoutFactory.discoverFormat(new File(url.getPath().replace("%20", " ")), null, options).isEmpty()); // file

        // test loading a layout without registering it
        LayoutFactory.unregisterAllLayouts();
        Assert.assertTrue(LayoutFactory.getRegisterLayouts().isEmpty());
        Assert.assertFalse(LayoutFactory.discoverFormat(createNaaccrLine(22824, "A", "140")).isEmpty());
        Assert.assertTrue(LayoutFactory.getRegisterLayouts().isEmpty());
    }

    private File createNaaccrLine(int length, String recType, String naaccrVersion) throws IOException {
        StringBuilder line = new StringBuilder(getTestingString(length));

        if (recType == null)
            line.setCharAt(0, ' ');
        else
            line.setCharAt(0, recType.charAt(0));

        if (naaccrVersion == null) {
            line.setCharAt(16, ' ');
            line.setCharAt(17, ' ');
            line.setCharAt(18, ' ');
        }
        else {
            line.setCharAt(16, naaccrVersion.charAt(0));
            line.setCharAt(17, naaccrVersion.charAt(1));
            if (naaccrVersion.length() > 2)
                line.setCharAt(18, naaccrVersion.charAt(2));
        }

        File file = new File(System.getProperty("user.dir") + File.pathSeparator + "naaccr-test.xml");
        FileWriter writer = new FileWriter(file);
        writer.write(line.toString());
        writer.close();

        return file;
    }

    private String getTestingString(int length) {
        return getTestingString(length, null);
    }

    private String getTestingString(int length, String c) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < length; i++)
            buf.append(c != null ? c : (char)((i % 26) + 65));

        return buf.toString();
    }

    @Test
    public void testGetLayoutInfoNoNaaccr() throws IOException {
        List<LayoutInfo> result;

        // with some information available, only one layout should be found
        result = LayoutFactory.discoverFormat(createFileFromTestingResource("fake-naaccr14-1-rec.txt"));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_14_INCIDENCE, result.get(0).getLayoutId());
        result = LayoutFactory.discoverFormat(createFileFromTestingResource("fake-naaccr15-1-rec.txt"));
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_15_INCIDENCE, result.get(0).getLayoutId());

        // with no information available, several layouts should be found if the options allow it
        LayoutInfoDiscoveryOptions options = new LayoutInfoDiscoveryOptions();
        options.setNaaccrAllowBlankVersion(true);
        options.setNaaccrAllowBlankRecordType(true);
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        result = LayoutFactory.discoverFormat(createFileFromTestingResource("fake-naaccr15-1-rec-no-version.txt"), options);
        Assert.assertEquals(5, result.size());
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_INCIDENCE, result.get(0).getLayoutId());
        Assert.assertEquals("C400", ((RecordLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_INCIDENCE)).readAllRecords(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(
                        "fake-naaccr15-1-rec-no-version.txt"), "UTF-8").get(0).get("primarySite"));

        // turning blank record type off...
        options.setNaaccrAllowBlankVersion(false);
        options.setNaaccrAllowBlankRecordType(true);
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        result = LayoutFactory.discoverFormat(createFileFromTestingResource("fake-naaccr15-1-rec-no-version.txt"), options);
        Assert.assertTrue(result.isEmpty());

        // turning blank version off...
        options.setNaaccrAllowBlankVersion(true);
        options.setNaaccrAllowBlankRecordType(false);
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        result = LayoutFactory.discoverFormat(createFileFromTestingResource("fake-naaccr15-1-rec-no-version.txt"), options);
        Assert.assertTrue(result.isEmpty());

        // turning using line length off...
        options.setNaaccrAllowBlankVersion(true);
        options.setNaaccrAllowBlankRecordType(true);
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        result = LayoutFactory.discoverFormat(createFileFromTestingResource("fake-naaccr15-1-rec-no-version.txt"), options);
        Assert.assertTrue(result.isEmpty());
    }

    private File createFileFromTestingResource(String path) {
        return new File(Thread.currentThread().getContextClassLoader().getResource(path).getPath());

    }
}
