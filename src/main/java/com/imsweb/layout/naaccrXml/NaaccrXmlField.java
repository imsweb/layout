/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.naaccrXml;

import java.util.List;
import java.util.Objects;

import com.imsweb.layout.Field;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryGroupedItem;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;

public class NaaccrXmlField extends Field {

    private NaaccrDictionaryItem _item;

    // TODO remove everything about "groups"
    private boolean _isGroupedItem;

    public NaaccrXmlField() {
        super();
    }

    public NaaccrXmlField(NaaccrDictionaryItem item) {
        super();
        _item = item;
        _name = item.getNaaccrId();
        // TODO this is not needed
        if (item.getNaaccrName() != null) {
            _shortLabel = item.getNaaccrName();
            _longLabel = item.getNaaccrName();
        }
        _naaccrItemNum = item.getNaaccrNum();
        _isGroupedItem = item instanceof NaaccrDictionaryGroupedItem;
    }

    public NaaccrDictionaryItem getItem() {
        return _item;
    }

    public boolean getIsGroupedItem() {
        return _isGroupedItem;
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

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NaaccrXmlField that = (NaaccrXmlField)o;
        return Objects.equals(_item, that._item);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), _item);
    }
}
