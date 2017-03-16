/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

public class Hl7Tester {

    public static void main(String[] args) throws Exception {

        // @formatter:off
        Hl7Message msg = new Hl7MessageBuilder()
            .withSegment("PID")
                .withField(3)
                    .withRepeatedValue()
                        .withComponent(1, "010203040")
                        .withComponent(5, "MR")
                        .withComponent(6, "STJ", "03D1234567", "AHA")
                    .withRepeatedValue()
                        .withComponent(1, "111223333")
                        .withComponent(5, "SS")
                    .withRepeatedValue()
                        .withComponent(1, "97 810430")
                        .withComponent(5, "PI")
                        .withComponent(6, "HITECK PATH LAB-ATL", "3D932840", "CLIA")
            .build();
        // @formatter:on 
        
        File file = new File("C:\\dev\\projects\\seerdms\\app\\src\\test\\resources\\importer\\hl7_naaccr_good1.txt");
        
        NaaccrHl7Layout layout = new NaaccrHl7Layout();
        
        // TODO add methods to retrieve the data more gracefully...
        // TODO there is an issue of contention between just adding an entity and it's index. I need to dynamically fix the lists...
        
        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            Hl7Message message = layout.fetchNextMessage(reader);
            while (message != null) {
                //System.out.println(message.getSegment("PID"));
                
                //List<Hl7Component> c = message.getField("PID-3").getValues().get(0).getComponents();
                //System.out.println(c.get(0).getSubComponents().get(0).getValue() + " " + c.get(1).getSubComponents().get(0).getValue());
                
                message.getField("PID-3").getComponent(0).getValue();
                
                message = layout.fetchNextMessage(reader);
            }
        }
        
    }

}
