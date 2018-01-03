/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package lab;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.hl7.NaaccrHl7Layout;
import com.imsweb.layout.hl7.entity.Hl7Message;

public class Hl7FolderEvaluator {

    public static void main(String[] args) throws IOException {
        NaaccrHl7Layout layout = (NaaccrHl7Layout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_HL7_2_5_1);
        Files.newDirectoryStream(Paths.get("D:\\Users\\depryf\\Desktop\\HL7 data")).forEach(path -> {
            try (LineNumberReader reader = new LineNumberReader(new FileReader(path.toFile()))) {
                //List<Hl7Message> messages = layout.readAllMessages(path.toFile());
                Hl7Message msg = layout.readNextMessage(reader);
                while (msg != null) {
                    System.out.println(msg.getLineNumber());
                    msg = layout.readNextMessage(reader);
                }

                //System.out.println("Valid: " + path.getFileName() + " (" + messages.size() + ")");
            }
            catch (IOException | RuntimeException e) {
                System.out.println("Invalid: " + path.getFileName() + " - " + e.getMessage());
            }
        });

    }
}
