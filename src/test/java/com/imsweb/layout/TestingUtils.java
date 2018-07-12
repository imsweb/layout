/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package com.imsweb.layout;

public class TestingUtils {

    public static String getWorkingDirectory() {
        return System.getProperty("user.dir").replace(".idea\\modules", ""); // this will make it work in IntelliJ and outside of it...
    }
}
