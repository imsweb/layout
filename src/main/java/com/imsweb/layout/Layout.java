/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.layout;

import java.io.File;
import java.util.List;

/**
 * A layout represents a data format. With a layout instance, a caller will have access to the fields (variables) information and will be able
 * to read/write data files of that format (although the read/write features are not part of this interface and so technically, a layout implementation
 * is not required to provide them).
 * <br/><br/>
 * This interface provides three types of methods:
 * <ul>
 *     <li>Getters for the basic layout information (ID, name, description, etc...)</li>
 *     <li>Fields-related methods</li>
 *     <li>The method used by the factory to evaluate if the layout can be used to handle a given data file (format discovery)</li>
 * </ul>
 * <p/>
 * Created on Jul 14, 2011 by depryf
 * @author depryf
 */
public interface Layout {

    /**
     * Returns the ID for this layout. This is how the framework uniquely identifies layouts.
     * <p/>
     * Created on Aug 16, 2011 by depryf
     * @return the ID for this layout
     */
    String getLayoutId();

    /**
     * Returns the name for this layout.
     * <p/>
     * Created on Aug 16, 2011 by depryf
     * @return the name for this layout
     */
    String getLayoutName();

    /**
     * Returns the version for this layout. The framework makes no assumption on the format of the version or what it represents.
     * <p/>
     * Created on Feb 9, 2012 by depryf
     * @return the version for this layout
     */
    String getLayoutVersion();

    /**
     * Returns the description for this layout. It should be kept short and on one line if possible.
     * <p/>
     * Created on Aug 16, 2011 by depryf
     * @return the description for this layout
     */
    String getLayoutDescription();

    /**
     * Returns the field associated with the field name, null if none is found
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param name field name
     * @return the <code>Field</code> associated with the field name, null if none is found
     */
    Field getFieldByName(String name);

    /**
     * Returns thefield associated with the NAACCR Item Number, null if none is found.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param num NAACCR Item Number
     * @return the <code>Field</code> associated with the NAACCR Item Number, null if none is found
     */
    Field getFieldByNaaccrItemNumber(Integer num);

    /**
     * Returns all the fields in this layout, orderd by start column.
     * <p/>
     * If a field is a group (for example the morpholgy field containing the histology and behavior fields), ONLY THE PARENT FIELD WILL BE RETURNED.<br>
     * It is the caller's responsability to iterate through the sub-fields for any parent fields that has some.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @return List of all the <code>Field</code> objects
     */
    List<? extends Field> getAllFields();

    /**
     * Gives the documentation in HTML format for the field that corresponds to the field name passed in.
     * <p/>
     * Returns null if the documentation is not found.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param name Field name
     * @return null if doc is not found, the documentation otherwise.
     */
    String getFieldDocByName(String name);

    /**
     * Gives the documentation in HTML format for the field that corresponds to the Naaccr Item Number passed in.
     * <p/>
     * Returns null if the documentation is not found.
     * <p/>
     * Created on Jul 14, 2011 by murphyr
     * @param num NAACCR item number
     * @return null if doc is not found, the documentation otherwise.
     */
    String getFieldDocByNaaccrItemNumber(Integer num);

    /**
     * Returns the default CSS style that can be injected into the NAACCR documentation.
     * <p/>
     * If a particular layout doesn't support a default CSS style, an empty string will be returned.
     * <p/>
     * Created on Apr 10, 2012 by depryf
     * @return the default CSS style that goes with the fields documentation, maybe empty string but never null
     */
    String getFieldDocDefaultCssStyle();

    /**
     * Returns a file info object from the provided file, returns null if this layout cannot handle the data contained in the file.
     * <br/><br/>
     * The factory uses this method to know if a particular instance of a layout can handle a given data file (format discovery).
     * <p/>
     * Created on Jun 23, 2012 by depryf
     * @param file data file
     * @param zipEntryName optional zip entry to use if the file is a zip file, not used if the file is not a zip file
     * @param options discovery options
     * @return a <code>FileInfo</code> object, maybe null
     */
    LayoutInfo buildFileInfo(File file, String zipEntryName, LayoutInfoDiscoveryOptions options);
}
