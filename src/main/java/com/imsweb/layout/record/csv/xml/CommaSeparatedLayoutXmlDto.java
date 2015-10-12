
package com.imsweb.layout.record.csv.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("comma-separated-layout")
public class CommaSeparatedLayoutXmlDto {

    @XStreamAsAttribute
    private String id;

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private String version;

    @XStreamAsAttribute
    private String description;

    @XStreamAsAttribute
    @XStreamAlias("num-fields")
    private Integer numFields;

    @XStreamAsAttribute
    private String separator;

    @XStreamAsAttribute
    @XStreamAlias("ignore-first-line")
    private Boolean ignoreFirstLine;

    @XStreamAsAttribute
    @XStreamAlias("extend-layout")
    private String extendLayout;

    @XStreamImplicit
    private List<CommaSeparatedLayoutFieldXmlDto> field;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getNumFields() {
        return numFields;
    }

    public void setNumFields(Integer numFields) {
        this.numFields = numFields;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public Boolean getIgnoreFirstLine() {
        return ignoreFirstLine;
    }

    public void setIgnoreFirstLine(Boolean ignoreFirstLine) {
        this.ignoreFirstLine = ignoreFirstLine;
    }

    public String getExtendLayout() {
        return extendLayout;
    }

    public void setExtendLayout(String extendLayout) {
        this.extendLayout = extendLayout;
    }

    public List<CommaSeparatedLayoutFieldXmlDto> getField() {
        return field;
    }

    public void setField(List<CommaSeparatedLayoutFieldXmlDto> field) {
        this.field = field;
    }
}
