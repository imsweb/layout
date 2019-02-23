/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package com.imsweb.layout;

import java.io.File;

public class TestingUtils {

    public static String getWorkingDirectory() {
        return System.getProperty("user.dir").replace(".idea\\modules", ""); // this will make it work in IntelliJ and outside of it...
    }

    public static File getBuildDirectory() {
        File dir = new File(TestingUtils.getWorkingDirectory(), "build");
        if (!dir.exists() && !dir.mkdir())
            throw new RuntimeException("Unable to create build directory!");
        return dir;
    }
}
