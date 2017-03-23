/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.layout.hl7.entity.Hl7Component;
import com.imsweb.layout.hl7.entity.Hl7Field;
import com.imsweb.layout.hl7.entity.Hl7Message;
import com.imsweb.layout.hl7.entity.Hl7RepeatedField;
import com.imsweb.layout.hl7.entity.Hl7Segment;
import com.imsweb.layout.hl7.entity.Hl7SubComponent;

public class Hl7UtilsTest {

    private static final String _LINE_SEPARATOR = System.getProperty("line.separator");

    @Test
    public void testReadFixedColumnsLayout() {
        // TODO - make sure this test uses only fake XML resources
        Assert.assertTrue(true);
    }

    @Test
    public void testWriteFixedColumnsLayout() {
        // TODO - make sure this test uses the build folder to write the testing files
        Assert.assertTrue(true);
    }

    @Test
    public void testSegmentFromString() {

        Hl7Segment segment = Hl7Utils.segmentFromString(new Hl7Message(), "TST|VAL");
        Assert.assertEquals("VAL", segment.getField(1).getValue());

        segment = Hl7Utils.segmentFromString(new Hl7Message(), "TST|VAL1^^VAL2");
        Assert.assertEquals("VAL1^^VAL2", segment.getField(1).getValue());
        Assert.assertEquals("VAL1", segment.getField(1).getComponent(1).getValue());
        Assert.assertNull(segment.getField(1).getComponent(2).getValue());
        Assert.assertEquals("VAL2", segment.getField(1).getComponent(3).getValue());

        segment = Hl7Utils.segmentFromString(new Hl7Message(), "TST||VAL1^VAL2&VAL3&&&");
        Assert.assertNull(segment.getField(1).getValue());
        Assert.assertEquals("VAL1^VAL2&VAL3", segment.getField(2).getValue());
        Assert.assertEquals("VAL1", segment.getField(2).getComponent(1).getValue());
        Assert.assertEquals("VAL2&VAL3", segment.getField(2).getComponent(2).getValue());
        Assert.assertEquals("VAL2", segment.getField(2).getComponent(2).getSubComponent(1).getValue());
        Assert.assertEquals("VAL3", segment.getField(2).getComponent(2).getSubComponent(2).getValue());
    }

    @Test
    public void testMessageToString() {
        // no component
        Hl7Message message = new Hl7Message();
        Assert.assertEquals("", Hl7Utils.messageToString(message));

        // invalid id
        message = new Hl7Message();
        assertInvalidSegmentId(message, null);
        assertInvalidSegmentId(message, "");
        assertInvalidSegmentId(message, "   ");
        assertInvalidSegmentId(message, "VAL1");

        // one id
        message = new Hl7Message();
        addSegment(message, "VL1", "VAL1|VAL2");
        Assert.assertEquals("VL1|VAL1|VAL2", Hl7Utils.messageToString(message));

        // three ids
        message = new Hl7Message();
        addSegment(message, "VL1", "VAL1");
        addSegment(message, "VL2", "VAL2|VAL3");
        addSegment(message, "VL3", "VAL4|VAL5|VAL6");
        Assert.assertEquals("VL1|VAL1" + _LINE_SEPARATOR + "VL2|VAL2|VAL3" + _LINE_SEPARATOR + "VL3|VAL4|VAL5|VAL6", Hl7Utils.messageToString(message));

        // four ids with some null values
        message = new Hl7Message();
        addSegment(message, "VL1", null);
        addSegment(message, "VL2", " ");
        addSegment(message, "VL3", "VAL1");
        addSegment(message, "VL4", "");
        Assert.assertEquals("VL1|" + _LINE_SEPARATOR + "VL2| " + _LINE_SEPARATOR + "VL3|VAL1" + _LINE_SEPARATOR + "VL4|", Hl7Utils.messageToString(message));
    }

    @Test
    public void testSegmentToString() {
        Hl7Message message = new Hl7Message();

        // no component
        Hl7Segment segment = new Hl7Segment(message, "TST");
        Assert.assertEquals("", Hl7Utils.segmentToString(segment));

        // null value (TST|)
        segment = new Hl7Segment(message, "TST");
        addField(segment, 1, null);
        Assert.assertEquals("TST|", Hl7Utils.segmentToString(segment));

        // blank value (TST|)
        segment = new Hl7Segment(message, "TST");
        addField(segment, 1, "");
        Assert.assertEquals("TST|", Hl7Utils.segmentToString(segment));

        // spaces (TST|   )
        segment = new Hl7Segment(message, "TST");
        addField(segment, 1, "   ");
        Assert.assertEquals("TST|   ", Hl7Utils.segmentToString(segment));

        // TST|VAL1
        segment = new Hl7Segment(message, "TST");
        addField(segment, 1, "VAL1");
        Assert.assertEquals("TST|VAL1", Hl7Utils.segmentToString(segment));

        // TST||VAL1 (with intermediate fields provided)
        segment = new Hl7Segment(message, "TST");
        addField(segment, 1, null);
        addField(segment, 2, "VAL1");
        Assert.assertEquals("TST||VAL1", Hl7Utils.segmentToString(segment));

        // TST||VAL1 (with intermediate fields NOT provided)
        segment = new Hl7Segment(message, "TST");
        addField(segment, 2, "VAL1");
        Assert.assertEquals("TST||VAL1", Hl7Utils.segmentToString(segment));

        // TST|VAL1|
        segment = new Hl7Segment(message, "TST");
        addField(segment, 1, "VAL1");
        addField(segment, 2, null);
        Assert.assertEquals("TST|VAL1|", Hl7Utils.segmentToString(segment));

        // TST|| ||VAL1 (with intermediate fields provided)
        segment = new Hl7Segment(message, "TST");
        addField(segment, 1, null);
        addField(segment, 2, " ");
        addField(segment, 3, "");
        addField(segment, 4, "VAL1");
        Assert.assertEquals("TST|| ||VAL1", Hl7Utils.segmentToString(segment));

        // TST||||VAL1 (with intermediate fields NOT provided)
        segment = new Hl7Segment(message, "TST");
        addField(segment, 4, "VAL1");
        Assert.assertEquals("TST||||VAL1", Hl7Utils.segmentToString(segment));

        // TST|VAL1|||
        segment = new Hl7Segment(message, "TST");
        addField(segment, 1, "VAL1");
        addField(segment, 4, null);
        Assert.assertEquals("TST|VAL1|||", Hl7Utils.segmentToString(segment));

        // TST|VAL1|VAL2
        segment = new Hl7Segment(message, "TST");
        addField(segment, 1, "VAL1");
        addField(segment, 2, "VAL2");
        Assert.assertEquals("TST|VAL1|VAL2", Hl7Utils.segmentToString(segment));

        // TST|VAL1||VAL2
        segment = new Hl7Segment(message, "TST");
        addField(segment, 1, "VAL1");
        addField(segment, 3, "VAL2");
        Assert.assertEquals("TST|VAL1||VAL2", Hl7Utils.segmentToString(segment));

        // TST||VAL1||VAL2|||VAL3 (with intermediate fields provided)
        segment = new Hl7Segment(message, "TST");
        addField(segment, 1, null);
        addField(segment, 2, "VAL1");
        addField(segment, 3, null);
        addField(segment, 4, "VAL2");
        addField(segment, 5, null);
        addField(segment, 6, null);
        addField(segment, 7, "VAL3");
        Assert.assertEquals("TST||VAL1||VAL2|||VAL3", Hl7Utils.segmentToString(segment));

        // TST||VAL1||VAL2|||VAL3 (with intermediate fields NOT provided)
        segment = new Hl7Segment(message, "TST");
        addField(segment, 2, "VAL1");
        addField(segment, 4, "VAL2");
        addField(segment, 7, "VAL3");
        Assert.assertEquals("TST||VAL1||VAL2|||VAL3", Hl7Utils.segmentToString(segment));
    }

    @Test
    public void testFieldToString() {
        Hl7Segment segment = new Hl7Segment(new Hl7Message(), "TST");

        // no component
        Hl7Field field = new Hl7Field(segment, 1);
        Assert.assertEquals("", Hl7Utils.fieldToString(field));

        // null value
        field = new Hl7Field(segment, 1);
        addRepeatedField(field, null);
        Assert.assertEquals("", Hl7Utils.fieldToString(field));

        // blank value
        field = new Hl7Field(segment, 1);
        addRepeatedField(field, "");
        Assert.assertEquals("", Hl7Utils.fieldToString(field));

        // spaces
        field = new Hl7Field(segment, 1);
        addRepeatedField(field, "   ");
        Assert.assertEquals("   ", Hl7Utils.fieldToString(field));

        // one value
        field = new Hl7Field(segment, 1);
        addRepeatedField(field, "VAL1");
        Assert.assertEquals("VAL1", Hl7Utils.fieldToString(field));

        // three values
        field = new Hl7Field(segment, 1);
        addRepeatedField(field, "VAL1");
        addRepeatedField(field, "VAL2");
        addRepeatedField(field, "VAL3");
        Assert.assertEquals("VAL1~VAL2~VAL3", Hl7Utils.fieldToString(field));

        // three values with some null
        field = new Hl7Field(segment, 1);
        addRepeatedField(field, null);
        addRepeatedField(field, " ");
        addRepeatedField(field, "VAL2");
        addRepeatedField(field, "");
        Assert.assertEquals("~ ~VAL2~", Hl7Utils.fieldToString(field));
    }

    @Test
    public void testRepeatedFieldToString() {
        Hl7Field field = new Hl7Field(new Hl7Segment(new Hl7Message(), "TST"), 1);

        // no component
        Hl7RepeatedField repField = new Hl7RepeatedField(field);
        Assert.assertEquals("", Hl7Utils.repeatedFieldToString(repField));

        // null value
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 1, null);
        Assert.assertEquals("", Hl7Utils.repeatedFieldToString(repField));

        // blank value
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 1, "");
        Assert.assertEquals("", Hl7Utils.repeatedFieldToString(repField));

        // spaces
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 1, "   ");
        Assert.assertEquals("   ", Hl7Utils.repeatedFieldToString(repField));

        // VAL1
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 1, "VAL1");
        Assert.assertEquals("VAL1", Hl7Utils.repeatedFieldToString(repField));

        // ^VAL1 (with intermediate subcomponents provided)
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 1, null);
        addComponent(repField, 2, "VAL1");
        Assert.assertEquals("^VAL1", Hl7Utils.repeatedFieldToString(repField));

        // ^VAL1 (with intermediate subcomponents NOT provided)
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 2, "VAL1");
        Assert.assertEquals("^VAL1", Hl7Utils.repeatedFieldToString(repField));

        // VAL^
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 1, "VAL1");
        addComponent(repField, 2, null);
        Assert.assertEquals("VAL1^", Hl7Utils.repeatedFieldToString(repField));

        // ^ ^^VAL1 (with intermediate subcomponents provided)
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 1, null);
        addComponent(repField, 2, " ");
        addComponent(repField, 3, "");
        addComponent(repField, 4, "VAL1");
        Assert.assertEquals("^ ^^VAL1", Hl7Utils.repeatedFieldToString(repField));

        // ^^^VAL1 (with intermediate subcomponents NOT provided)
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 4, "VAL1");
        Assert.assertEquals("^^^VAL1", Hl7Utils.repeatedFieldToString(repField));

        // VAL1^^^
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 1, "VAL1");
        addComponent(repField, 4, null);
        Assert.assertEquals("VAL1^^^", Hl7Utils.repeatedFieldToString(repField));

        // VAL1^VAL2
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 1, "VAL1");
        addComponent(repField, 2, "VAL2");
        Assert.assertEquals("VAL1^VAL2", Hl7Utils.repeatedFieldToString(repField));

        // VAL1^^VAL2
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 1, "VAL1");
        addComponent(repField, 3, "VAL2");
        Assert.assertEquals("VAL1^^VAL2", Hl7Utils.repeatedFieldToString(repField));

        // ^VAL1^VAL2^^VAL3 (with intermediate subcomponents provided)
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 1, null);
        addComponent(repField, 2, "VAL1");
        addComponent(repField, 3, null);
        addComponent(repField, 4, "VAL2");
        addComponent(repField, 5, null);
        addComponent(repField, 6, null);
        addComponent(repField, 7, "VAL3");
        Assert.assertEquals("^VAL1^^VAL2^^^VAL3", Hl7Utils.repeatedFieldToString(repField));

        // ^VAL1^VAL2^^VAL3 (with intermediate subcomponents NOT provided)
        repField = new Hl7RepeatedField(field);
        addComponent(repField, 2, "VAL1");
        addComponent(repField, 4, "VAL2");
        addComponent(repField, 7, "VAL3");
        Assert.assertEquals("^VAL1^^VAL2^^^VAL3", Hl7Utils.repeatedFieldToString(repField));
    }

    @Test
    public void testComponentToString() {
        Hl7RepeatedField repField = new Hl7RepeatedField(new Hl7Field(new Hl7Segment(new Hl7Message(), "TST"), 1));

        // no subcomponent
        Hl7Component comp = new Hl7Component(repField, 1);
        Assert.assertEquals("", Hl7Utils.componentToString(comp));

        // null value
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 1, null);
        Assert.assertEquals("", Hl7Utils.componentToString(comp));

        // blank value
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 1, "");
        Assert.assertEquals("", Hl7Utils.componentToString(comp));

        // spaces
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 1, "   ");
        Assert.assertEquals("   ", Hl7Utils.componentToString(comp));

        // VAL1
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 1, "VAL1");
        Assert.assertEquals("VAL1", Hl7Utils.componentToString(comp));

        // &VAL1 (with intermediate subcomponents provided)
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 1, null);
        addSubComponent(comp, 2, "VAL1");
        Assert.assertEquals("&VAL1", Hl7Utils.componentToString(comp));

        // &VAL1 (with intermediate subcomponents NOT provided)
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 2, "VAL1");
        Assert.assertEquals("&VAL1", Hl7Utils.componentToString(comp));

        // VAL&
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 1, "VAL1");
        addSubComponent(comp, 2, null);
        Assert.assertEquals("VAL1&", Hl7Utils.componentToString(comp));

        // & &&VAL1 (with intermediate subcomponents provided)
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 1, null);
        addSubComponent(comp, 2, " ");
        addSubComponent(comp, 3, "");
        addSubComponent(comp, 4, "VAL1");
        Assert.assertEquals("& &&VAL1", Hl7Utils.componentToString(comp));

        // &&&VAL1 (with intermediate subcomponents NOT provided)
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 4, "VAL1");
        Assert.assertEquals("&&&VAL1", Hl7Utils.componentToString(comp));

        // VAL1&&&
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 1, "VAL1");
        addSubComponent(comp, 4, null);
        Assert.assertEquals("VAL1&&&", Hl7Utils.componentToString(comp));

        // VAL1&VAL2
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 1, "VAL1");
        addSubComponent(comp, 2, "VAL2");
        Assert.assertEquals("VAL1&VAL2", Hl7Utils.componentToString(comp));

        // VAL1&&VAL2
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 1, "VAL1");
        addSubComponent(comp, 3, "VAL2");
        Assert.assertEquals("VAL1&&VAL2", Hl7Utils.componentToString(comp));

        // &VAL1&VAL2&&VAL3 (with intermediate subcomponents provided)
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 1, null);
        addSubComponent(comp, 2, "VAL1");
        addSubComponent(comp, 3, null);
        addSubComponent(comp, 4, "VAL2");
        addSubComponent(comp, 5, null);
        addSubComponent(comp, 6, null);
        addSubComponent(comp, 7, "VAL3");
        Assert.assertEquals("&VAL1&&VAL2&&&VAL3", Hl7Utils.componentToString(comp));

        // &VAL1&VAL2&&VAL3 (with intermediate subcomponents NOT provided)
        comp = new Hl7Component(repField, 1);
        addSubComponent(comp, 2, "VAL1");
        addSubComponent(comp, 4, "VAL2");
        addSubComponent(comp, 7, "VAL3");
        Assert.assertEquals("&VAL1&&VAL2&&&VAL3", Hl7Utils.componentToString(comp));
    }

    // helper
    private void assertInvalidSegmentId(Hl7Message message, String id) {
        boolean invalidId = false;
        try {
            addSegment(message, id, null);
        }
        catch (RuntimeException e) {
            invalidId = true;
        }
        Assert.assertTrue(invalidId);
    }

    // helper
    private void addSegment(Hl7Message message, String id, String value) {
        addField(new Hl7Segment(message, id), 1, value);
    }

    // helper
    private void addField(Hl7Segment segment, Integer index, String value) {
        addRepeatedField(new Hl7Field(segment, index), value);
    }

    // helper
    private void addRepeatedField(Hl7Field field, String value) {
        addComponent(new Hl7RepeatedField(field), 1, value);
    }

    // helper
    private void addComponent(Hl7RepeatedField repField, Integer index, String value) {
        addSubComponent(new Hl7Component(repField, index), 1, value);
    }

    // helper
    private void addSubComponent(Hl7Component component, Integer index, String value) {
        new Hl7SubComponent(component, index, value);
    }
}
