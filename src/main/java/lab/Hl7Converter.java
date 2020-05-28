/*
 * Copyright (C) 2020 Information Management Services, Inc.
 */
package lab;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.hl7.NaaccrHl7Layout;
import com.imsweb.layout.hl7.entity.Hl7Message;
import com.imsweb.layout.hl7.entity.Hl7Segment;
import com.imsweb.layout.naaccrxml.NaaccrXmlLayout;
import com.imsweb.naaccrxml.NaaccrFormat;
import com.imsweb.naaccrxml.NaaccrOptions;
import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.entity.Item;
import com.imsweb.naaccrxml.entity.NaaccrData;
import com.imsweb.naaccrxml.entity.Patient;
import com.imsweb.naaccrxml.entity.Tumor;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * I added a "shadowJar" Gradle task to build one JAR with this lab and all the dependencies...
 */
public class Hl7Converter {

    public static void main(String[] args) throws IOException {

        if (args.length != 2)
            throw new RuntimeException("Expected 2 arguments (input directory and output directory); got " + args.length);

        File inputDir = new File(args[0]);
        if (!inputDir.exists())
            throw new RuntimeException("Input directory doesn't exist: " + inputDir.getPath());
        File outputDir = new File(args[1]);
        if (!outputDir.exists())
            throw new RuntimeException("Output directory doesn't exist: " + outputDir.getPath());

        NaaccrDictionary dictionary;
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("path-text-dictionary.xml")), UTF_8)) {
            dictionary = NaaccrXmlDictionaryUtils.readDictionary(reader);
        }

        NaaccrHl7Layout inputLayout = (NaaccrHl7Layout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_HL7_2_5_1);
        NaaccrXmlLayout outputLayout = new NaaccrXmlLayout("180", "A", "path-reports", "Path Reports", null, Collections.singletonList(dictionary), true);

        NaaccrData rootData = new NaaccrData();
        rootData.setBaseDictionaryUri(NaaccrXmlDictionaryUtils.getBaseDictionaryByVersion(NaaccrFormat.NAACCR_VERSION_180).getDictionaryUri());
        rootData.getUserDictionaryUri().add(dictionary.getDictionaryUri());
        rootData.setRecordType(NaaccrFormat.NAACCR_REC_TYPE_ABSTRACT);

        // registryId: not supported (there are several available for California and so it's better to leave this blank)
        rootData.addItem(new Item("registryId", null));

        NaaccrOptions options = NaaccrOptions.getDefault();

        List<File> files = Files.list(inputDir.toPath())
                .map(Path::toFile)
                .filter(File::isFile)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .collect(Collectors.toList());

        System.out.println("Found " + files.size() + " files to process; converting them...");

        int total = 0;
        for (File file : files)
            total += convertSingleFile(file, outputDir, inputLayout, outputLayout, rootData, options);
        System.out.println("Done converting " + total + " HL7 messages into NAACCR XML files...");
        System.out.println("Goodbye");
    }

    private static int convertSingleFile(File inputFile, File outputDir, NaaccrHl7Layout inputLayout, NaaccrXmlLayout outputLayout, NaaccrData rootData, NaaccrOptions options) {
        int count = 0;
        try {
            for (Hl7Message message : inputLayout.readAllMessages(inputFile)) {

                // get a unique ID from the message so we can use it in the output file name
                String messageId = message.getSegment("MSH").getField(10).getValue();
                if (StringUtils.isBlank(messageId))
                    messageId = message.getSegment("MSH").getField(7).getValue();
                if (StringUtils.isBlank(messageId)) {
                    System.err.println("  > Unable to consume message in " + inputFile.getName() + "; both MSH-10 and MSH-7 were blank...");
                    continue;
                }

                Patient patient = convertSingleMessage(message);

                File outputFile = new File(outputDir, "CA." + messageId + ".XML");
                try {
                    outputLayout.writeAllPatients(outputFile, Collections.singletonList(patient), rootData, options);
                }
                catch (IOException e) {
                    System.err.println("  > Error writing messages from " + inputFile.getName() + ": " + e.getMessage());
                }

                count++;
            }
        }
        catch (IOException e) {
            System.err.println("  > Error reading messages from " + inputFile.getName() + ": " + e.getMessage());
        }

        return count;
    }

    private static Patient convertSingleMessage(Hl7Message message) {
        Patient patient = new Patient();

        // patientIdNumber: not supported
        patient.addItem(new Item("patientIdNumber", null));

        Tumor tumor = new Tumor();

        // tumorRecordNumber: not supported
        tumor.addItem(new Item("tumorRecordNumber", null));

        // documentRecordId: not supported
        tumor.addItem(new Item("documentRecordId", null));

        // pathDateSpecCollect1: OBR-7 or SPM-17
        String value = message.getSegment("OBR") == null ? null : message.getSegment("OBR").getField(7).getValue();
        if (StringUtils.isBlank(value))
            value = message.getSegment("SMP") == null ? null : message.getSegment("SPM").getField(17).getComponent(1).getValue();
        if (value != null && value.length() > 8)
            value = value.substring(0, 8);
        if (value != null && (value.length() < 8 || !NumberUtils.isDigits(value)))
            value = null;
        tumor.addItem(new Item("pathDateSpecCollect1", value));

        // dateEpathMessage: MSH-7
        value = message.getSegment("MSH").getField(7).getValue();
        if (value != null && value.length() > 8)
            value = value.substring(0, 8);
        if (value != null && (value.length() < 8 || !NumberUtils.isDigits(value)))
            value = null;
        tumor.addItem(new Item("dateEpathMessage", value));

        StringBuilder textPathClinicalHistory = new StringBuilder();
        StringBuilder textPathComments = new StringBuilder();
        StringBuilder textPathFormalDx = new StringBuilder();
        StringBuilder textPathFullText = new StringBuilder();
        StringBuilder textPathGrossPathology = new StringBuilder();
        StringBuilder textPathMicroscopicDesc = new StringBuilder();
        StringBuilder textPathSuppReportsAddenda = new StringBuilder();
        StringBuilder textPathNatureOfSpecimens = new StringBuilder();
        StringBuilder textStagingParams = new StringBuilder();

        // text fields: OBX-3 and OBX-5
        for (Hl7Segment segment : message.getSegments()) {
            if ("OBX".equals(segment.getId())) {

                String type = segment.getField(3).getValue();
                String codedType = segment.getField(3).getComponent(1).getValue();
                String labelType = segment.getField(3).getComponent(2).getValue();
                String text = segment.getField(5).getValue();

                // textPathClinicalHistory: OBX-5 (if OBX-3 is 'CH' or OBX-3.1 is '22636-5' or OBX-3.2 is 'Clinical History')
                if ("CH".equals(type) || "22636-5".equals(codedType) || "Clinical History".equalsIgnoreCase(labelType))
                    appendText(text, textPathClinicalHistory);

                // textPathComments:OBX-5 (if OBX-3 is 'CM' or OBX-3.1 is '22638-1' or '22049-1' or OBX-3.2 is 'Comment Section' or 'MISC')
                if ("CM".equals(type) || "22638-1".equals(codedType) || "22049-1".equals(codedType) || "Comment Section".equalsIgnoreCase(labelType) || "MISC".equalsIgnoreCase(labelType))
                    appendText(text, textPathComments);

                // textPathFormalDx: OBX-5 (if OBX-3 is 'FD' or OBX-3.1 is '22637-3' or OBX-3.2 is 'Final Diagnosis')
                if ("FD".equals(type) || "22637-3".equals(codedType) || "Final Diagnosis".equalsIgnoreCase(labelType))
                    appendText(text, textPathFormalDx);

                // textPathFullText: OBX-5 (if OBX-3 is 'FP' or OBX-3 is 'FT' or OBX-3.1 is '33746-9' or OBX-3.2 is 'Text Diagnosis' or (OBX-3.1 is 'TI' and OBX-3.2 is 'Block') or OBX-3.1 is '60568-3' or OBX-3.1 is '21865-1')
                if ("FP".equals(type) || "FT".equals(type) || "33746-9".equals(codedType) || "60568-3".equals(codedType) || "21865-1".equals(codedType) || "Text Diagnosis".equalsIgnoreCase(labelType)
                        || ("TI".equals(codedType) && "Block".equalsIgnoreCase(labelType)))
                    appendText(text, textPathFullText);

                // textPathGrossPathology: OBX-5 (if OBX-3 is 'GP' or OBX-3.1 is '22634-0' or OBX-3.2 is 'Gross Pathology')
                if ("GP".equals(type) || "22634-0".equals(codedType) || "Gross Pathology".equalsIgnoreCase(labelType))
                    appendText(text, textPathGrossPathology);

                // textPathMicroscopicDesc: OBX-5 (if OBX-3 is 'MP' or OBX-3.1 is '22635-7' or OBX-3.2 is 'Micro Pathology')
                if ("MP".equals(type) || "22635-7".equals(codedType) || "Micro Pathology".equalsIgnoreCase(labelType))
                    appendText(text, textPathMicroscopicDesc);

                // textPathSuppReportsAddenda: OBX-5 (if OBX-3 is 'SR' or OBX-3.1 is '22639-9' or OBX-3.2 is 'Suppl Reports')
                if ("SR".equals(type) || "22639-9".equals(codedType) || "Suppl Reports".equalsIgnoreCase(labelType))
                    appendText(text, textPathSuppReportsAddenda);

                // textPathNatureOfSpecimens: OBX-5 (if OBX-3 is 'NS' or OBX-3.1 is '22633-2' or OBX-3.2 is 'Nature of Specimen')
                if ("NS".equals(type) || "22633-2".equals(codedType) || "Nature of Specimen".equalsIgnoreCase(labelType))
                    appendText(text, textPathNatureOfSpecimens);

                // textStagingParams: OBX-5 (if OBX-3 is 'PR')
                if ("PR".equals(type))
                    appendText(text, textStagingParams);
            }
        }

        tumor.addItem(new Item("textPathClinicalHistory", StringUtils.stripToNull(textPathClinicalHistory.toString())));
        tumor.addItem(new Item("textPathComments", StringUtils.stripToNull(textPathComments.toString())));
        tumor.addItem(new Item("textPathFormalDx", StringUtils.stripToNull(textPathFormalDx.toString())));
        tumor.addItem(new Item("textPathFullText", StringUtils.stripToNull(textPathFullText.toString())));
        tumor.addItem(new Item("textPathGrossPathology", StringUtils.stripToNull(textPathGrossPathology.toString())));
        tumor.addItem(new Item("textPathMicroscopicDesc", StringUtils.stripToNull(textPathMicroscopicDesc.toString())));
        tumor.addItem(new Item("textPathSuppReportsAddenda", StringUtils.stripToNull(textPathSuppReportsAddenda.toString())));
        tumor.addItem(new Item("textPathNatureOfSpecimens", StringUtils.stripToNull(textPathNatureOfSpecimens.toString())));
        tumor.addItem(new Item("textStagingParams", StringUtils.stripToNull(textStagingParams.toString())));

        patient.addTumor(tumor);

        return patient;
    }

    private static void appendText(String text, StringBuilder buf) {
        if (buf.length() > 0)
            buf.append("\r\n\r\n");
        buf.append(text);
    }
}
