/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.record.fixed.naaccr;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.LayoutInfo;
import com.imsweb.layout.LayoutInfoDiscoveryOptions;
import com.imsweb.layout.TestingUtils;
import com.imsweb.layout.record.RecordLayout;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.imsweb.layout.LayoutFactory.LAYOUT_ID_NAACCR_16_INCIDENCE;

public class NaaccrLayoutTest {

    @Test
    public void testBuildFileInfo() throws IOException {
        RecordLayout layout = (RecordLayout)LayoutFactory.getLayout(LAYOUT_ID_NAACCR_16_INCIDENCE);

        Map<String, String> record = layout.readAllRecords(new File(TestingUtils.getWorkingDirectory() + "/src/test/resources/fake-naaccr16-1-rec.txt")).get(0);
        StringBuilder buf = new StringBuilder(layout.createLineFromRecord(record, null));
        String line = buf.toString();
        String lineShort = line.substring(0, 25);

        buf.replace(0, 1, " "); // blanking out record type
        String lineBlankType = buf.toString();
        String lineBlankTypeShort = lineBlankType.substring(0, 25);

        buf.replace(0, 1, "I");
        buf.replace(16, 19, "   "); // blanking out naaccr version
        String lineBlankVersion = buf.toString();
        String lineBlankVersionShort = lineBlankVersion.substring(0, 25);

        buf.replace(0, 1, " ");
        String lineBlankInfo = buf.toString();
        String lineBlankInfoShort = lineBlankInfo.substring(0, 25);

        // null/empty input
        Assert.assertNull(layout.buildFileInfo(null, null));
        Assert.assertNull(layout.buildFileInfo("", null));

        // default options (allow blank type/version as long as the line length is correct)
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(line, null).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineShort, null).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankType, null).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankTypeShort, null));
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankVersion, null).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankVersionShort, null));
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankInfo, null).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankInfoShort, null));

        LayoutInfoDiscoveryOptions options = new LayoutInfoDiscoveryOptions();

        // change line length option
        options.setFixedColumnAllowDiscoveryFromLineLength(true);
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(line, options).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineShort, options).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankType, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankTypeShort, options));
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankVersion, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankVersionShort, options));
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankInfo, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankInfoShort, options));
        options.setFixedColumnAllowDiscoveryFromLineLength(false);
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(line, options).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineShort, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankType, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankTypeShort, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankVersion, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankVersionShort, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankInfo, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankInfoShort, options));
        options.setFixedColumnAllowDiscoveryFromLineLength(true);

        // change allow blank version option
        options.setNaaccrAllowBlankVersion(true);
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(line, options).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineShort, options).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankType, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankTypeShort, options));
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankVersion, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankVersionShort, options));
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankInfo, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankInfoShort, options));
        options.setNaaccrAllowBlankVersion(false);
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(line, options).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineShort, options).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankType, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankTypeShort, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankVersion, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankVersionShort, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankInfo, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankInfoShort, options));
        options.setNaaccrAllowBlankVersion(true);

        // change allow blank type option
        options.setNaaccrAllowBlankRecordType(true);
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(line, options).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineShort, options).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankType, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankTypeShort, options));
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankVersion, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankVersionShort, options));
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankInfo, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankInfoShort, options));
        options.setNaaccrAllowBlankRecordType(false);
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(line, options).getLayoutId());
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineShort, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankType, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankTypeShort, options));
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, layout.buildFileInfo(lineBlankVersion, options).getLayoutId());
        Assert.assertNull(layout.buildFileInfo(lineBlankVersionShort, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankInfo, options));
        Assert.assertNull(layout.buildFileInfo(lineBlankInfoShort, options));
        options.setNaaccrAllowBlankRecordType(true);

        // if the line length is not correct, there should be an error
        LayoutInfo info = layout.buildFileInfo(line, options);
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, info.getLayoutId());
        Assert.assertNull(info.getErrorMessage());
        info = layout.buildFileInfo(lineShort, options);
        Assert.assertEquals(LAYOUT_ID_NAACCR_16_INCIDENCE, info.getLayoutId());
        Assert.assertNotNull(info.getErrorMessage());

        // wrong type
        buf.replace(0, 1, "A");
        buf.replace(16, 19, "160");
        Assert.assertNull(layout.buildFileInfo(buf.toString(), options));
        Assert.assertNull(layout.buildFileInfo(buf.toString().substring(0, 25), options));

        // wrong version
        buf.replace(0, 1, "I");
        buf.replace(16, 19, "150");
        Assert.assertNull(layout.buildFileInfo(buf.toString(), options));
        Assert.assertNull(layout.buildFileInfo(buf.toString().substring(0, 25), options));
    }
}
