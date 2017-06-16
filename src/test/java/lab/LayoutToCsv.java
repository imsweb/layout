/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package lab;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.naaccr.NaaccrLayout;

public class LayoutToCsv {

    public static void main(String[] args) {
        NaaccrLayout layout = (NaaccrLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_16_ABSTRACT);

        System.out.println("Item #,Item Name,Item Short Name");
        for (FixedColumnsField f : layout.getAllFields()) {
            if (f.getNaaccrItemNum() != null)
                printField(f);
            if (f.getSubFields() != null)
                for (FixedColumnsField ff : f.getSubFields())
                    if (ff.getNaaccrItemNum() != null)
                        printField(ff);
        }
    }

    private static void printField(FixedColumnsField f) {
        String name = f.getLongLabel().contains(",") ? ("\"" + f.getLongLabel() + "\"") : f.getLongLabel();
        String shortName = f.getShortLabel().contains(",") ? ("\"" + f.getShortLabel() + "\"") : f.getShortLabel();
        System.out.println(f.getNaaccrItemNum() + "," + name + "," + shortName);
    }
}
