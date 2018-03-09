/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package lab;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.naaccr.NaaccrLayout;
import com.imsweb.naaccrxml.NaaccrFormat;
import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;

public class LayoutVsDictionary {

    public static void main(String[] args) {

        // get the layout fields
        NaaccrLayout layout = (NaaccrLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18);
        Map<Integer, FixedColumnsField> layoutFields = new HashMap<>();
        for (FixedColumnsField field : layout.getAllFields()) {
            if (field.getNaaccrItemNum() != null)
                layoutFields.put(field.getNaaccrItemNum(), field);
            if (field.getSubFields() != null) {
                for (FixedColumnsField subField : field.getSubFields())
                    if (subField.getNaaccrItemNum() != null)
                        layoutFields.put(subField.getNaaccrItemNum(), subField);
            }
        }

        // get the NAACCR XML fields
        NaaccrDictionary dictionary = NaaccrXmlDictionaryUtils.getMergedDictionaries(NaaccrFormat.NAACCR_VERSION_180);
        Map<Integer, NaaccrDictionaryItem> xmlFields = new TreeMap<>();
        for (NaaccrDictionaryItem item : dictionary.getItems())
            xmlFields.put(item.getNaaccrNum(), item);
        for (NaaccrDictionaryItem item : dictionary.getGroupedItems())
            xmlFields.put(item.getNaaccrNum(), item);

        // compare them
        System.out.println("In layout but not in XML:");
        layoutFields.values().forEach(f -> {
            if (!xmlFields.containsKey(f.getNaaccrItemNum()))
                System.out.println("  " + f.getNaaccrItemNum() + " - " + f.getName());
        });
        System.out.println("In XML but not in layout:");
        xmlFields.values().forEach(f -> {
            if (!layoutFields.containsKey(f.getNaaccrNum()))
                System.out.println("  " + f.getNaaccrNum() + " - " + f.getNaaccrId());
        });

        System.out.println("Both in layout and XML but with different names:");
        xmlFields.values().forEach(xmlField -> {
            FixedColumnsField layoutField = layoutFields.get(xmlField.getNaaccrNum());
            if (layoutField != null && !layoutField.getName().equals(xmlField.getNaaccrId()))
                System.out.println("  " + xmlField.getNaaccrNum() + " - " + xmlField.getNaaccrId() + " vs " + layoutField.getName());
        });
    }

}
