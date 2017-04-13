/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import org.junit.Test;

public class NaaccrHl7LayoutTest {

    @Test
    public void readNextMessageTest() throws Exception {
        NaaccrHl7Layout layout = new NaaccrHl7Layout(Thread.currentThread().getContextClassLoader().getResource(""));
    }
}
