
package com.imsweb.layout.record.csv.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("field")
public class CommaSeparatedLayoutFieldXmlDto {

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    @XStreamAlias("long-label")
    private String longLabel;

    @XStreamAsAttribute
    @XStreamAlias("short-label")
    private String shortLabel;

    @XStreamAsAttribute
    @XStreamAlias("naaccr-item-num")
    private Integer naaccrItemNum;

    @XStreamAsAttribute
    private Integer index;

    @XStreamAsAttribute
    @XStreamAlias("max-length")
    private Integer maxLength;

    @XStreamAsAttribute
    @XStreamAlias("default-value")
    private String defaultValue;

    @XStreamAsAttribute
    private Boolean trim;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLongLabel() {
        return longLabel;
    }

    public void setLongLabel(String longLabel) {
        this.longLabel = longLabel;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public void setShortLabel(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    public Integer getNaaccrItemNum() {
        return naaccrItemNum;
    }

    public void setNaaccrItemNum(Integer naaccrItemNum) {
        this.naaccrItemNum = naaccrItemNum;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getTrim() {
        return trim;
    }

    public void setTrim(Boolean trim) {
        this.trim = trim;
    }
}
