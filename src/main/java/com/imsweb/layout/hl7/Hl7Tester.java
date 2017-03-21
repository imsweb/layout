/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import com.imsweb.layout.hl7.entity.Hl7Message;

public class Hl7Tester {

    public static void main(String[] args) throws Exception {

        // @formatter:off
        Hl7Message msg = Hl7MessageBuilder.createMessage()
            .withSegment("PID")
                .withField(3)
                    .withRepeatedField()
                        .withComponent(1, "010203040")
                        .withComponent(5, "MR")
                        .withComponent(6, "STJ", "03D1234567", "AHA")
                    .withRepeatedField()
                        .withComponent(1, "111223333")
                        .withComponent(5, "SS")
                    .withRepeatedField()
                        .withComponent(1, "97 810430")
                        .withComponent(5, "PI")
                        .withComponent(6, "HITECK PATH LAB-ATL", "3D932840", "CLIA")
                .withField(5)
                    .withComponent(1, "DEPRY")
                    .withComponent(2, "FABIAN")
                    .withComponent(3, "P")
            .build();
        // @formatter:on 

        // TODO add methods to retrieve the data more gracefully...
        System.out.println(msg.getField("PID-5").getComponent(1).getValue());
        //System.out.println(msg.getComponent("PID-5.1").getValue()); // TODO not sure I like this one, it's weird to call getComponent from a message!

//        File file = new File("C:\\dev\\projects\\seerdms\\app\\src\\test\\resources\\importer\\hl7_naaccr_good1.txt");
//
//        NaaccrHl7Layout layout = new NaaccrHl7Layout();
//
//        try (StringWriter writer = new StringWriter()) {
//            layout.writeMessage(writer, msg);
//            System.out.println(writer.toString());
//        }
//
//        List<Hl7Message> messages = layout.readAllMessages(file);
//        System.out.println("read " + messages.size() + " messages from " + file.getPath());
//
//        File newFile = Paths.get("build/test.txt").toFile();
//        layout.writeMessages(newFile, messages);
//        System.out.println("Wrote messages to " + newFile.getPath());

        // TODO there is a bug for ^ and & starting a field. I am not outputting the correct number of separators. Maybe it's time for unit tests...
    }

}
