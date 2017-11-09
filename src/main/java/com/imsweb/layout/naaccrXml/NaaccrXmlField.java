/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.naaccrXml;

import java.util.List;

import com.imsweb.layout.Field;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryGroupedItem;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;

public class NaaccrXmlField extends Field {

    private NaaccrDictionaryItem _item;

    //TODO probably need to store default values somewhere? Probably in layout, not individual field.
    public NaaccrXmlField() {
        super();
    }

    public NaaccrXmlField(NaaccrDictionaryItem item) {
        super();
        _item = item;
        _naaccrItemNum = item.getNaaccrNum();
        _name = item.getNaaccrId();
    }

    private void validate() {

    }

    public String getNaaccrId() {
        return _item.getNaaccrId();
    }

    public Integer getStartColumn() {
        return _item.getStartColumn();
    }

    public Integer getLength() {
        return _item.getLength();
    }

    public String getParentXmlElement() {
        return _item.getParentXmlElement();
    }

    public String getNaaccrName() {
        return _item.getNaaccrName();
    }

    public boolean isAllowUnlimitedText() {
        return _item.getAllowUnlimitedText();
    }

    public String getRecordTypes() {
        return _item.getRecordTypes();
    }

    public String getDataType() {
        return _item.getDataType();
    }

    public String getPadding() {
        return _item.getPadding();
    }

    public String getTrimType() {
        return _item.getTrim();
    }

    //methods for Grouped Items
    public String getContains() {
        return _item instanceof NaaccrDictionaryGroupedItem ? ((NaaccrDictionaryGroupedItem)_item).getContains() : null;
    }

    public List<String> getContainedItemIds() {
        return _item instanceof NaaccrDictionaryGroupedItem ? ((NaaccrDictionaryGroupedItem)_item).getContainedItemId() : null;
    }

}
