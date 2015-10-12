
package com.imsweb.layout.record.fixed.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("field")
public class FixedColumnLayoutFieldXmlDto {

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
    private Integer start;

    @XStreamAsAttribute
    private Integer end;

    @XStreamAsAttribute
    private String align;

    @XStreamAsAttribute
    @XStreamAlias("pad-char")
    private String padChar;

    @XStreamAsAttribute
    @XStreamAlias("default-value")
    private String defaultValue;

    @XStreamAsAttribute
    private Boolean trim;

    @XStreamAsAttribute
    private String section;

    @XStreamImplicit
    private List<FixedColumnLayoutFieldXmlDto> field;

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

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public String getPadChar() {
        return padChar;
    }

    public void setPadChar(String padChar) {
        this.padChar = padChar;
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

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public List<FixedColumnLayoutFieldXmlDto> getField() {
        return field;
    }

    public void setField(List<FixedColumnLayoutFieldXmlDto> field) {
        this.field = field;
    }
}
