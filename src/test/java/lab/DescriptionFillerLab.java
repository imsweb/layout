/*
 * Copyright (C) 2023 Information Management Services, Inc.
 */
package lab;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.math.NumberUtils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.naaccrxml.NaaccrXmlLayout;

public class DescriptionFillerLab {

    public static void main(String[] args) throws IOException, CsvException {
        NaaccrXmlLayout layout = LayoutFactory.getNaaccrXmlLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_23);

        File inputFile = new File("C:\\Users\\depryf\\Desktop\\SEER Data Layout - HV Version - 20230414.csv");
        File outputFile = new File("C:\\Users\\depryf\\Desktop\\SEER Data Layout - HV Version - 20230414 - TMP.csv");

        try (CSVReader reader = new CSVReader(new FileReader(inputFile)); CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {
            for (String[] row : reader.readAll()) {
                if (NumberUtils.isDigits(row[2])) {
                    String doc = layout.getFieldDocByNaaccrItemNumber(Integer.valueOf(row[2]));
                    if (doc != null) {
                        doc = doc.substring(doc.indexOf("<div class='content"));
                        writer.writeNext(new String[] {row[2], doc});
                    }
                    else
                        writer.writeNext(new String[] {row[2], row[5]});
                }
                else
                    writer.writeNext(new String[] {row[2], row[5]});
            }
        }
    }
    
}
