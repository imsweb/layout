/*
 * Copyright (C) 2023 Information Management Services, Inc.
 */
package lab;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.RFC4180Parser;
import com.opencsv.exceptions.CsvValidationException;

public class NaaccrLookupsToSasFormats {

    public static void main(String[] args) throws IOException, CsvValidationException {
        String version = "230";

        File inFile = new File(System.getProperty("user.dir") + "\\docs\\naaccr-lookups\\naaccr-lookups-" + version + ".zip");
        File outFile = new File(System.getProperty("user.dir") + "\\docs\\naaccr-lookups\\naaccr-lookups-" + version + "-sas-formats.sas.gz");

        //try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(outFile.toPath()), StandardCharsets.UTF_8))) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(outFile.toPath())), StandardCharsets.UTF_8))) {

            writer.write("/************************************************************************************************************;\r\n");
            writer.write("SAS formats for the NAACCR data items that have a 'standard' lookup.\r\n");
            writer.write("\r\n");
            writer.write("Format names are the XML IDs with a trailing 'F' character; with the following exceptions\r\n");
            writer.write("for IDs that are too long:\r\n");
            writer.write(" - phase[1-3]RadiationTreatmentModality uses phase[1-3]RadTreatmentModalityF\r\n");
            writer.write(" - phase[1-3]RadiationExternalBeamTech uses phase[1-3]RadExternalBeamTechF\r\n");
            writer.write(" - neoadjuvTherapyClinicalResponse uses neoadjuvTherapyClinResponseF\r\n");
            writer.write(" - reportingFacilityRestrictionFlag uses reportingFacRestrictionFlagF\r\n");
            writer.write("\r\n");
            writer.write("The histologicTypeIcdO3 data item is a bit different: the codes are the concatenation of both the\r\n");
            writer.write("histology and behavior with a slash between them (so 8000/3).\r\n");
            writer.write("************************************************************************************************************/;\r\n\r\n");

            writer.write("proc format;\r\n\r\n");

            try (ZipFile zFile = new ZipFile(inFile)) {
                Enumeration<? extends ZipEntry> entries = zFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.isDirectory())
                        continue;

                    String field = entry.getName().replace("naaccr-lookups-" + version + "/", "").replace(".csv", "");
                    if ("histologicTypeIcdO3_preferredTermsOnly".equals(field))
                        field = "histologicTypeIcdO3";

                    if ("histologicTypeIcdO3_allTerms".equals(field))
                        continue;

                    // we are adding a trailing "f" but variable names cannot be longer than 32!
                    if (field.endsWith("RadiationTreatmentModality"))
                        field = field.replace("RadiationTreatmentModality", "RadTreatmentModality");
                    if (field.endsWith("RadiationExternalBeamTech"))
                        field = field.replace("RadiationExternalBeamTech", "RadExternalBeamTech");
                    if ("reportingFacilityRestrictionFlag".equals(field))
                        field = "reportingFacRestrictionFlag";
                    if ("neoadjuvTherapyClinicalResponse".equals(field))
                        field = "neoadjuvTherapyClinResponse";
                    if (field.length() >= 31)
                        throw new IllegalStateException("Field needs to be truncated: " + field);

                    writer.write("    value $" + field + "F\r\n");

                    try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(zFile.getInputStream(entry), StandardCharsets.UTF_8));
                         CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(new RFC4180Parser()).build()) {
                        String[] line = csvReader.readNext();
                        while (line != null) {
                            String code = line[0].replace("'", "''");
                            String label = line[1].replace("'", "''");
                            int idx = label.indexOf("\n");
                            if (idx != -1)
                                label = label.substring(0, idx);
                            writer.write("        '" + code + "'='" + label + "'\r\n");
                            line = csvReader.readNext();
                        }
                    }

                    writer.write("        ;\r\n\r\n");
                }
            }
        }
    }
}
