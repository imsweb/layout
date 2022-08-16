/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package com.imsweb.layout;

import java.io.File;

public class TestingUtils {

    public static String getWorkingDirectory() {
        return System.getProperty("user.dir");
    }

    public static File getBuildDirectory() {
        File dir = new File(TestingUtils.getWorkingDirectory(), "build");
        if (!dir.exists() && !dir.mkdir())
            throw new IllegalStateException("Unable to create build directory!");
        return dir;
    }

    public static File getArchivedNaaccrDoc() {
        return new File(TestingUtils.getWorkingDirectory() + File.separator + "docs" + File.separator + "naaccr-documentation");
    }
}
