
package com.imsweb.layout.record.fixed.xml;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("fixed-column-layout")
public class FixedColumnLayoutXmlDto {

    @XStreamAsAttribute
    private String id;

    @XStreamAsAttribute
    private String name;

    @XStreamAsAttribute
    private String version;

    @XStreamAsAttribute
    private String description;

    @XStreamAsAttribute
    private Integer length;

    @XStreamAsAttribute
    @XStreamAlias("extend-layout")
    private String extendLayout;

    @XStreamImplicit
    protected List<FixedColumnLayoutFieldXmlDto> field;

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

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getExtendLayout() {
        return extendLayout;
    }

    public void setExtendLayout(String extendLayout) {
        this.extendLayout = extendLayout;
    }

    public List<FixedColumnLayoutFieldXmlDto> getField() {
        return field;
    }

    public void setField(List<FixedColumnLayoutFieldXmlDto> field) {
        this.field = field;
    }
}
