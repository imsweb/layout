/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.record.fixed.naaccr;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.record.RecordLayout;

public class NaaccrLayoutTest {

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testBuildFileInfo() throws IOException {
        RecordLayout layout = (RecordLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16);
        URL url = Thread.currentThread().getContextClassLoader().getResource("fake-naaccr16-1-rec.txt");
        Map<String, String> record = layout.readAllRecords(new File(url.getPath().replace("%20", " "))).get(0);

        LayoutInfoDiscoveryOptions options = new LayoutInfoDiscoveryOptions();
        StringBuilder recordString;

        //Null and empty record cases return null
        Assert.assertNull(layout.buildFileInfo(null, null));
        Assert.assertNull(layout.buildFileInfo("", null));

        //Valid record with null options - uses default options, should return info as normal
        recordString = new StringBuilder(layout.createLineFromRecord(record, null));
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT, layout.buildFileInfo(recordString.toString(), null).getLayoutId());
        recordString.replace(16, 19, "   "); //Blanking out Version
        recordString.replace(0, 1, " "); //Blanking out record Type
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT, layout.buildFileInfo(recordString.toString(), null).getLayoutId());
        recordString.replace(16, 19, "160"); //Replace Version
        recordString.replace(0, 1, "I"); //Replace record Type

        //Valid version and record
        recordString = new StringBuilder(layout.createLineFromRecord(record, null));
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT, layout.buildFileInfo(recordString.toString(), options).getLayoutId());
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(false);
        options.setNaaccrAllowBlankVersion(false);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT, layout.buildFileInfo(recordString.toString(), options).getLayoutId());
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(true);
        options.setNaaccrAllowBlankVersion(false);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT, layout.buildFileInfo(recordString.toString(), options).getLayoutId());
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(false);
        options.setNaaccrAllowBlankVersion(true);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT, layout.buildFileInfo(recordString.toString(), options).getLayoutId());
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        //blank version, valid record
        recordString.replace(16, 19, "   "); //Blanking out Version
        options = new LayoutInfoDiscoveryOptions();
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT, layout.buildFileInfo(recordString.toString(), options).getLayoutId());
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(false);
        options.setNaaccrAllowBlankVersion(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(true);
        options.setNaaccrAllowBlankVersion(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(false);
        options.setNaaccrAllowBlankVersion(true);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT, layout.buildFileInfo(recordString.toString(), options).getLayoutId());
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        //both version and record blank
        recordString.replace(0, 1, " "); //Blanking out record Type
        options = new LayoutInfoDiscoveryOptions();
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT, layout.buildFileInfo(recordString.toString(), options).getLayoutId());
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(false);
        options.setNaaccrAllowBlankVersion(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(true);
        options.setNaaccrAllowBlankVersion(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(false);
        options.setNaaccrAllowBlankVersion(true);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        //valid version, blank record
        recordString.replace(16, 19, "160"); //Replacing Version
        options = new LayoutInfoDiscoveryOptions();
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT, layout.buildFileInfo(recordString.toString(), options).getLayoutId());
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(false);
        options.setNaaccrAllowBlankVersion(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(true);
        options.setNaaccrAllowBlankVersion(false);
        Assert.assertEquals(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT, layout.buildFileInfo(recordString.toString(), options).getLayoutId());
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));

        options.setNaaccrAllowBlankRecordType(false);
        options.setNaaccrAllowBlankVersion(true);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertNull(layout.buildFileInfo(recordString.toString(), options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertNull(layout.buildFileInfo(recordString.substring(2, 10), options));
    }

    @Test
    public void testGetXmlIdFromLayoutName() {
        Assert.assertEquals("raceNapiia", NaaccrLayout.getXmlIdFromLayoutName("napiia"));
        Assert.assertEquals("primarySite", NaaccrLayout.getXmlIdFromLayoutName("primarySite"));
        Assert.assertEquals("?", NaaccrLayout.getXmlIdFromLayoutName("?"));
        Assert.assertNull(NaaccrLayout.getXmlIdFromLayoutName(null));
    }

    @Test
    public void testGetLatestFieldDocByName() {
        //System.out.println(NaaccrLayout.getLatestFieldDocByName("firstCourseCalcMethod"));
        Assert.fail("implement me");
    }
}
