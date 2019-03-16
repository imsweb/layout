/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.naaccrxml;

import java.util.List;
import java.util.Objects;

import com.imsweb.layout.Field;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;

public class NaaccrXmlField extends Field {

    private NaaccrDictionaryItem _item;

    private List<NaaccrXmlField> _subFields;

    public NaaccrXmlField(NaaccrDictionaryItem item) {
        super();
        _item = item;
        _name = item.getNaaccrId();
        _longLabel = item.getNaaccrName();
        _naaccrItemNum = item.getNaaccrNum();
    }

    public NaaccrDictionaryItem getItem() {
        return _item;
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

    public List<NaaccrXmlField> getSubFields() {
        return _subFields;
    }

    public void setSubFields(List<NaaccrXmlField> subFields) {
        _subFields = subFields;
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
