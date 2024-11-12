/*
 * Copyright (C) 2023 Information Management Services, Inc.
 */
package lab;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.math.NumberUtils;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import de.siegmar.fastcsv.writer.CsvWriter;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.naaccrxml.NaaccrXmlLayout;

public class DescriptionFillerLab {

    public static void main(String[] args) throws IOException {
        NaaccrXmlLayout layout = LayoutFactory.getNaaccrXmlLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_23);

        File inputFile = new File("C:\\Users\\depryf\\Desktop\\SEER Data Layout - HV Version - 20230414.csv");
        File outputFile = new File("C:\\Users\\depryf\\Desktop\\SEER Data Layout - HV Version - 20230414 - TMP.csv");

        try (CsvReader<NamedCsvRecord> reader = CsvReader.builder().ofNamedCsvRecord(new FileReader(inputFile));
             CsvWriter writer = CsvWriter.builder().build(new FileWriter(outputFile))) {
            reader.stream().forEach(line -> {
                if (NumberUtils.isDigits(line.getField((2)))) {
                    String doc = layout.getFieldDocByNaaccrItemNumber(Integer.valueOf(line.getField(2)));
                    if (doc != null) {
                        doc = doc.substring(doc.indexOf("<div class='content"));
                        writer.writeRecord(line.getField(2), doc);
                    }
                    else
                        writer.writeRecord(line.getField(2), line.getField(5));
                }
                else
                    writer.writeRecord(line.getField(2), line.getField(5));
            });
        }
    }
}
