package com.imsweb.layout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.imsweb.layout.hl7.NaaccrHl7Layout;
import com.imsweb.layout.naaccrxml.NaaccrXmlLayout;
import com.imsweb.layout.record.csv.CommaSeparatedLayout;
import com.imsweb.layout.record.fixed.FixedColumnsLayout;
import com.imsweb.layout.record.fixed.naaccr.NaaccrLayout;

/**
 * This class is responsible for caching layouts and instanciating the internal ones. It also provides a file format discovery mechanism.
 * <br/><br/>
 * Layouts can be used to read and write formatted files; the NAACCR formats are supported by default (those are the "internal" layouts)
 * but other user-defined layouts can also be registered. Fixed columns and CSV layout types are fully supported.
 * <br/><br/>
 * To read/write a file, the first thing to do is to determine which layout to use. This is accomplished using one of the <b>discoverFormat</b> methods.
 * Once this is done, one of the <code>LayoutInfo</code> objects can be used to call <b>getLayout</b> using the correct layout ID. The returned
 * <code>Layout</code> object can then be used for both reading and writing operations (it might have to be cast into a <code>RecordLayout</code> first).
 * <br/><br/>
 * There are two ways to register a layout:
 * <ol>
 * <li>Build it programmatically (eith er a <code>FixedColumnsLayout</code> or a <code>CommaSeparatedLayout</code> instance) and call the
 * <b>registerLayout()</b> method from this class, passing the built layout as an argument.</li>
 * <li>Build it from an XML file using one of the <code>LayoutUtils</code> utility methods, then create the layout instance by
 * providing the XML object to its constructor; and finally call the <b>registerLayout()</b> method from this class, passing the built layout as an argument.</li>
 * </ol>
 * This class caches the loaded layout, to clear the caches and free some memory, call one fo the <b>unregisterLayout()</b> or <b>unregisterAllLayouts</b>() method.
 * <p/>
 * Created on Jun 23, 2012 by Fabian Depry
 */
public final class LayoutFactory {

    //XML Layouts - constants for the internal layout IDs
    public static final String LAYOUT_ID_NAACCR_XML_16 = "naaccr-xml-16"; // kept for backward compatibility, actually means abstract...
    public static final String LAYOUT_ID_NAACCR_XML_16_ABSTRACT = "naaccr-xml-16-abstract";
    public static final String LAYOUT_ID_NAACCR_XML_16_MODIFIED = "naaccr-xml-16-modified";
    public static final String LAYOUT_ID_NAACCR_XML_16_CONFIDENTIAL = "naaccr-xml-16-confidential";
    public static final String LAYOUT_ID_NAACCR_XML_16_INCIDENCE = "naaccr-xml-16-incidence";
    //Fixed column layouts
    public static final String LAYOUT_ID_NAACCR_16 = "naaccr-16"; // kept for backward compatibility, actually means abstract...
    public static final String LAYOUT_ID_NAACCR_16_ABSTRACT = "naaccr-16-abstract";
    public static final String LAYOUT_ID_NAACCR_16_MODIFIED = "naaccr-16-modified";
    public static final String LAYOUT_ID_NAACCR_16_CONFIDENTIAL = "naaccr-16-confidential";
    public static final String LAYOUT_ID_NAACCR_16_INCIDENCE = "naaccr-16-incidence";
    public static final String LAYOUT_ID_NAACCR_15 = "naaccr-15"; // kept for backward compatibility, actually means abstract...
    public static final String LAYOUT_ID_NAACCR_15_ABSTRACT = "naaccr-15-abstract";
    public static final String LAYOUT_ID_NAACCR_15_MODIFIED = "naaccr-15-modified";
    public static final String LAYOUT_ID_NAACCR_15_CONFIDENTIAL = "naaccr-15-confidential";
    public static final String LAYOUT_ID_NAACCR_15_INCIDENCE = "naaccr-15-incidence";
    public static final String LAYOUT_ID_NAACCR_14 = "naaccr-14"; // kept for backward compatibility, actually means abstract...
    public static final String LAYOUT_ID_NAACCR_14_ABSTRACT = "naaccr-14-abstract";
    public static final String LAYOUT_ID_NAACCR_14_MODIFIED = "naaccr-14-modified";
    public static final String LAYOUT_ID_NAACCR_14_CONFIDENTIAL = "naaccr-14-confidential";
    public static final String LAYOUT_ID_NAACCR_14_INCIDENCE = "naaccr-14-incidence";
    public static final String LAYOUT_ID_NAACCR_13 = "naaccr-13"; // kept for backward compatibility, actually means abstract...
    public static final String LAYOUT_ID_NAACCR_13_ABSTRACT = "naaccr-13-abstract";
    public static final String LAYOUT_ID_NAACCR_13_MODIFIED = "naaccr-13-modified";
    public static final String LAYOUT_ID_NAACCR_13_CONFIDENTIAL = "naaccr-13-confidential";
    public static final String LAYOUT_ID_NAACCR_13_INCIDENCE = "naaccr-13-incidence";
    public static final String LAYOUT_ID_NAACCR_12 = "naaccr-12"; // kept for backward compatibility, actually means abstract...
    public static final String LAYOUT_ID_NAACCR_12_ABSTRACT = "naaccr-12-abstract";
    public static final String LAYOUT_ID_NAACCR_12_MODIFIED = "naaccr-12-modified";
    public static final String LAYOUT_ID_NAACCR_12_CONFIDENTIAL = "naaccr-12-confidential";
    public static final String LAYOUT_ID_NAACCR_12_INCIDENCE = "naaccr-12-incidence";
    //Hl7 layouts
    public static final String LAYOUT_ID_NAACCR_HL7_2_5_1 = "naaccr-hl7-2.5.1";

    // internal alias IDs resolution, any of the keys will be translated into its value...
    private static final Map<String, String> _INTERNAL_LAYOUT_ID_ALIASES = new HashMap<>();

    // make sure to put all the "aliases" in this list...
    static {
        _INTERNAL_LAYOUT_ID_ALIASES.put(LAYOUT_ID_NAACCR_XML_16, LAYOUT_ID_NAACCR_XML_16_ABSTRACT);
        _INTERNAL_LAYOUT_ID_ALIASES.put(LAYOUT_ID_NAACCR_16, LAYOUT_ID_NAACCR_16_ABSTRACT);
        _INTERNAL_LAYOUT_ID_ALIASES.put(LAYOUT_ID_NAACCR_15, LAYOUT_ID_NAACCR_15_ABSTRACT);
        _INTERNAL_LAYOUT_ID_ALIASES.put(LAYOUT_ID_NAACCR_14, LAYOUT_ID_NAACCR_14_ABSTRACT);
        _INTERNAL_LAYOUT_ID_ALIASES.put(LAYOUT_ID_NAACCR_13, LAYOUT_ID_NAACCR_13_ABSTRACT);
        _INTERNAL_LAYOUT_ID_ALIASES.put(LAYOUT_ID_NAACCR_12, LAYOUT_ID_NAACCR_12_ABSTRACT);
    }

    // internal layouts information (IDs and names)
    private static final Map<String, String> _INTERNAL_LAYOUTS = new LinkedHashMap<>();

    // make sure to add the most recent layouts first, they will be "tried" in that order (important for discovery mechanism)
    static {
        //NAACCR XML
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_XML_16, "NAACCR XML 16 Abstract");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_XML_16_ABSTRACT, "NAACCR XML 16 Abstract");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_XML_16_MODIFIED, "NAACCR XML 16 Modified");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_XML_16_CONFIDENTIAL, "NAACCR XML 16 Confidential");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_XML_16_INCIDENCE, "NAACCR XML 16 Incidence");
        // NAACCR FIXED_COLUMNS
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_16, "NAACCR 16 Abstract"); // kept for backward compatibility...
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_16_ABSTRACT, "NAACCR 16 Abstract");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_16_MODIFIED, "NAACCR 16 Modified");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_16_CONFIDENTIAL, "NAACCR 16 Confidential");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_16_INCIDENCE, "NAACCR 16 Incidence");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_15, "NAACCR 15 Abstract"); // kept for backward compatibility...
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_15_ABSTRACT, "NAACCR 15 Abstract");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_15_MODIFIED, "NAACCR 15 Modified");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_15_CONFIDENTIAL, "NAACCR 15 Confidential");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_15_INCIDENCE, "NAACCR 15 Incidence");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_14, "NAACCR 14 Abstract"); // kept for backward compatibility...
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_14_ABSTRACT, "NAACCR 14 Abstract");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_14_MODIFIED, "NAACCR 14 Modified");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_14_CONFIDENTIAL, "NAACCR 14 Confidential");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_14_INCIDENCE, "NAACCR 14 Incidence");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_13, "NAACCR 13 Abstract"); // kept for backward compatibility...
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_13_ABSTRACT, "NAACCR 13 Abstract");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_13_MODIFIED, "NAACCR 13 Modified");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_13_CONFIDENTIAL, "NAACCR 13 Confidential");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_13_INCIDENCE, "NAACCR 13 Incidence");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_12, "NAACCR 12 Abstract"); // kept for backward compatibility...
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_12_ABSTRACT, "NAACCR 12 Abstract");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_12_MODIFIED, "NAACCR 12 Modified");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_12_CONFIDENTIAL, "NAACCR 12 Confidential");
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_12_INCIDENCE, "NAACCR 12 Incidence");
        // NAACCR HL7
        _INTERNAL_LAYOUTS.put(LAYOUT_ID_NAACCR_HL7_2_5_1, "NAACCR HL7");
    }

    // registered layouts (internal and external)
    private static final Map<String, Layout> _LAYOUTS = new ConcurrentHashMap<>();

    /**
     * Helper method to load an internal layout. I am putting this method on the top so it's clear it has to be updated when adding support for new NAACCR versions.
     * <p/>
     * @param layoutId internal layout ID to load
     * @param loadFields if false, the fields won't be loaded
     * @return the loaded layout
     */
    private static Layout loadInternalLayout(String layoutId, boolean loadFields) {
        Layout layout = null;

        // note that this method doesn't deal with ID aliases, and it's on purpose, we want to load only the true layouts...
        if (LAYOUT_ID_NAACCR_XML_16_ABSTRACT.equals(layoutId))
            layout = new NaaccrXmlLayout("160", "A", LAYOUT_ID_NAACCR_XML_16_ABSTRACT, _INTERNAL_LAYOUTS.get(LAYOUT_ID_NAACCR_XML_16_ABSTRACT), null, loadFields);
        else if (LAYOUT_ID_NAACCR_XML_16_MODIFIED.equals(layoutId))
            layout = new NaaccrXmlLayout("160", "M", LAYOUT_ID_NAACCR_XML_16_MODIFIED, _INTERNAL_LAYOUTS.get(LAYOUT_ID_NAACCR_XML_16_MODIFIED), null, loadFields);
        else if (LAYOUT_ID_NAACCR_XML_16_CONFIDENTIAL.equals(layoutId))
            layout = new NaaccrXmlLayout("160", "C", LAYOUT_ID_NAACCR_XML_16_CONFIDENTIAL, _INTERNAL_LAYOUTS.get(LAYOUT_ID_NAACCR_XML_16_CONFIDENTIAL), null, loadFields);
        else if (LAYOUT_ID_NAACCR_XML_16_INCIDENCE.equals(layoutId))
            layout = new NaaccrXmlLayout("160", "I", LAYOUT_ID_NAACCR_XML_16_INCIDENCE, _INTERNAL_LAYOUTS.get(LAYOUT_ID_NAACCR_XML_16_INCIDENCE), null, loadFields);
        else if (LAYOUT_ID_NAACCR_16_ABSTRACT.equals(layoutId))
            layout = new NaaccrLayout("160", "A", 22824, LAYOUT_ID_NAACCR_16_ABSTRACT, loadFields);
        else if (LAYOUT_ID_NAACCR_16_MODIFIED.equals(layoutId))
            layout = new NaaccrLayout("160", "M", 22824, LAYOUT_ID_NAACCR_16_MODIFIED, loadFields);
        else if (LAYOUT_ID_NAACCR_16_CONFIDENTIAL.equals(layoutId))
            layout = new NaaccrLayout("160", "C", 5564, LAYOUT_ID_NAACCR_16_CONFIDENTIAL, loadFields);
        else if (LAYOUT_ID_NAACCR_16_INCIDENCE.equals(layoutId))
            layout = new NaaccrLayout("160", "I", 3339, LAYOUT_ID_NAACCR_16_INCIDENCE, loadFields);
        else if (LAYOUT_ID_NAACCR_15_ABSTRACT.equals(layoutId))
            layout = new NaaccrLayout("150", "A", 22824, LAYOUT_ID_NAACCR_15_ABSTRACT, loadFields);
        else if (LAYOUT_ID_NAACCR_15_MODIFIED.equals(layoutId))
            layout = new NaaccrLayout("150", "M", 22824, LAYOUT_ID_NAACCR_15_MODIFIED, loadFields);
        else if (LAYOUT_ID_NAACCR_15_CONFIDENTIAL.equals(layoutId))
            layout = new NaaccrLayout("150", "C", 5564, LAYOUT_ID_NAACCR_15_CONFIDENTIAL, loadFields);
        else if (LAYOUT_ID_NAACCR_15_INCIDENCE.equals(layoutId))
            layout = new NaaccrLayout("150", "I", 3339, LAYOUT_ID_NAACCR_15_INCIDENCE, loadFields);
        else if (LAYOUT_ID_NAACCR_14_ABSTRACT.equals(layoutId))
            layout = new NaaccrLayout("140", "A", 22824, LAYOUT_ID_NAACCR_14_ABSTRACT, loadFields);
        else if (LAYOUT_ID_NAACCR_14_MODIFIED.equals(layoutId))
            layout = new NaaccrLayout("140", "M", 22824, LAYOUT_ID_NAACCR_14_MODIFIED, loadFields);
        else if (LAYOUT_ID_NAACCR_14_CONFIDENTIAL.equals(layoutId))
            layout = new NaaccrLayout("140", "C", 5564, LAYOUT_ID_NAACCR_14_CONFIDENTIAL, loadFields);
        else if (LAYOUT_ID_NAACCR_14_INCIDENCE.equals(layoutId))
            layout = new NaaccrLayout("140", "I", 3339, LAYOUT_ID_NAACCR_14_INCIDENCE, loadFields);
        else if (LAYOUT_ID_NAACCR_13_ABSTRACT.equals(layoutId))
            layout = new NaaccrLayout("130", "A", 22824, LAYOUT_ID_NAACCR_13_ABSTRACT, loadFields);
        else if (LAYOUT_ID_NAACCR_13_MODIFIED.equals(layoutId))
            layout = new NaaccrLayout("130", "M", 22824, LAYOUT_ID_NAACCR_13_MODIFIED, loadFields);
        else if (LAYOUT_ID_NAACCR_13_CONFIDENTIAL.equals(layoutId))
            layout = new NaaccrLayout("130", "C", 5564, LAYOUT_ID_NAACCR_13_CONFIDENTIAL, loadFields);
        else if (LAYOUT_ID_NAACCR_13_INCIDENCE.equals(layoutId))
            layout = new NaaccrLayout("130", "I", 3339, LAYOUT_ID_NAACCR_13_INCIDENCE, loadFields);
        else if (LAYOUT_ID_NAACCR_12_ABSTRACT.equals(layoutId))
            layout = new NaaccrLayout("122", "A", 22824, LAYOUT_ID_NAACCR_12_ABSTRACT, loadFields);
        else if (LAYOUT_ID_NAACCR_12_MODIFIED.equals(layoutId))
            layout = new NaaccrLayout("122", "M", 22824, LAYOUT_ID_NAACCR_12_MODIFIED, loadFields);
        else if (LAYOUT_ID_NAACCR_12_CONFIDENTIAL.equals(layoutId))
            layout = new NaaccrLayout("122", "C", 5564, LAYOUT_ID_NAACCR_12_CONFIDENTIAL, loadFields);
        else if (LAYOUT_ID_NAACCR_12_INCIDENCE.equals(layoutId))
            layout = new NaaccrLayout("122", "I", 3339, LAYOUT_ID_NAACCR_12_INCIDENCE, loadFields);
        else if (LAYOUT_ID_NAACCR_HL7_2_5_1.equals(layoutId))
            layout = new NaaccrHl7Layout(LAYOUT_ID_NAACCR_HL7_2_5_1, "2.5.1", loadFields);

        if (layout == null)
            throw new RuntimeException("Unknown internal layout: " + layoutId);

        return layout;
    }

    /**
     * Private constructor, no instancication...
     * <p/>
     * Created on Jun 23, 2012 by Fabian
     */
    private LayoutFactory() {
    }

    /**
     * Returns the requested layout.
     * <br/><br/>
     * Throws a <b>RuntimeException</b> if the layout ID doesn't exist; you should check whether the layout exists before calling this method by either
     * <ul>
     * <li>using the <b>getAvailableLayouts()</b> method</li>
     * <li>using one of the <b>discoverFormat()</b> methods</li>
     * </ul>
     * This method will never throw an exception when using one of the layout IDs defined as constants in this class.
     * <p/>
     * @param layoutId requested layout ID, cannot be null
     * @return requested layout, never null
     */
    public static synchronized Layout getLayout(String layoutId) {

        // check if an alias ID was requested (used for backward compatibility)
        if (_INTERNAL_LAYOUT_ID_ALIASES.containsKey(layoutId))
            layoutId = _INTERNAL_LAYOUT_ID_ALIASES.get(layoutId);

        // if the layout is already registered, just return it
        if (_LAYOUTS.containsKey(layoutId))
            return _LAYOUTS.get(layoutId);

        // if no registered layout is found, let's check the internal ones
        if (_INTERNAL_LAYOUTS.containsKey(layoutId)) {
            Layout layout = loadInternalLayout(layoutId, true);
            _LAYOUTS.put(layout.getLayoutId(), layout);
            return layout;
        }

        throw new RuntimeException("Unknown layout ID: " + layoutId);
    }

    /**
     * Registers the provided layout.
     * <br/><br/>
     * If the layout is not valid in any way, a <b>RuntimeException</b> will be thrown (it is an unchecked excpetion because the validation
     * already happens when the layout object is created, so this is a safety net; we don't expect any validation issues in this method.
     * <p/>
     * @param layout layout to register, cannot be null
     */
    public static synchronized void registerLayout(Layout layout) {

        // make sure the layout is valid
        if (layout == null)
            throw new RuntimeException("Provided layout instance is null");
        if (layout instanceof FixedColumnsLayout)
            ((FixedColumnsLayout)layout).verify();
        else if (layout instanceof CommaSeparatedLayout)
            ((CommaSeparatedLayout)layout).verify();

        _LAYOUTS.put(layout.getLayoutId(), layout);
    }

    /**
     * Unregisters the layout corresponding to the provided layout ID.
     * <p/>
     * @param layoutId layout ID to unregister
     */
    public static synchronized void unregisterLayout(String layoutId) {
        _LAYOUTS.remove(layoutId);
    }

    /**
     * Unregisters all the registered layouts.
     */
    public static synchronized void unregisterAllLayouts() {
        _LAYOUTS.clear();
    }

    /**
     * Returns true if the layout ID has been registered, false otherwise.
     * <p/>
     * @param layoutId requested layout ID
     * @return true if the layout ID has been registered, false otherwise
     */
    public static synchronized boolean isLayoutRegister(String layoutId) {
        return _LAYOUTS.containsKey(layoutId);
    }

    /**
     * Returns the IDs of all the layouts currently registered.
     * <p/>
     * @return the IDs of all the layouts currently registered
     */
    public static synchronized Set<String> getRegisterLayouts() {
        return Collections.unmodifiableSet(_LAYOUTS.keySet());
    }

    /**
     * Returns a map ID -&gt; Name of the internal layouts.
     * <p/>
     * @return a map ID -&gt; Name of the internal layouts
     */
    public static Map<String, String> getAvailableInternalLayouts() {
        Map<String, String> result = new HashMap<>(_INTERNAL_LAYOUTS);
        // don't want to expose the aliases...
        for (String alias : _INTERNAL_LAYOUT_ID_ALIASES.keySet())
            result.remove(alias);
        return result;
    }

    /**
     * Returns a map ID -&gt; Name of the available layouts (registered external plus any internal).
     * <p/>
     * @return a map ID -&gt; Name of the registered layouts (registered external plus any internal)
     */
    public static Map<String, String> getAvailableLayouts() {
        Map<String, String> result = new HashMap<>();

        // add registered layouts
        for (Entry<String, Layout> entry : _LAYOUTS.entrySet())
            result.put(entry.getKey(), entry.getValue().getLayoutName());

        // add internal layouts that have not been registered yet
        for (Entry<String, String> entry : _INTERNAL_LAYOUTS.entrySet())
            if (!_INTERNAL_LAYOUT_ID_ALIASES.containsKey(entry.getKey()) && !result.containsKey(entry.getKey()))
                result.put(entry.getKey(), entry.getValue());

        return result;
    }

    /**
     * Discovers the format of the given file.
     * <br/><br/>
     * See other flavors of this method for full explanation.
     * <p/>
     * @param file file to analyze
     * @return the list of layout info representing the layouts that can handle this data file.
     * @throws IOException if there is a problem reading the file
     */
    public static List<LayoutInfo> discoverFormat(File file) throws IOException {
        return discoverFormat(file, null, new LayoutInfoDiscoveryOptions());
    }

    /**
     * Discovers the format of the given file, using the given zip entry if the file is a zip file.
     * <br/><br/>
     * See other flavors of this method for full explanation.
     * <p/>
     * @param file file to analyze
     * @param zipEntryName zip entry to use if the file is a zip file, not used if the file is not a zip file
     * @return the list of layout info representing the layouts that can handle this data file.
     * @throws IOException if there is a problem reading the file
     */
    public static List<LayoutInfo> discoverFormat(File file, String zipEntryName) throws IOException {
        return discoverFormat(file, zipEntryName, null);
    }

    /**
     * Discovers the format of the given file using the provided discovery options.
     * <br/><br/>
     * See other flavors of this method for full explanation.
     * <p/>
     * @param file file to analyze
     * @param options discovery options
     * @return the list of layout info representing the layouts that can handle this data file.
     * @throws IOException if there is a problem reading the file
     */
    public static List<LayoutInfo> discoverFormat(File file, LayoutInfoDiscoveryOptions options) throws IOException {
        return discoverFormat(file, null, options);
    }

    /**
     * Discovers the format of the given file using the provided discovery options.
     * <br/><br/>
     * The layouts are tried in the following order:
     * <ol>
     * <li>the registered external layouts (in alphabetical order on the layout ID, so the the order is deterministic)</li>
     * <li>the registered internal layouts</li>
     * <li>the internal layouts that have not been registered yet</li>
     * </ol>
     * Interal NAACCR layouts are tried from most recent version to oldest one.
     * <br/><br/>
     * Because of that particular order, the returned list of layout info will usually have the "best" layout as first element. If an application doesn't want to
     * present all the possibilities to the user, then it can just use that first element and nothing else.
     * <p/>
     * @param file file to analyze
     * @param zipEntryName optional zip entry to use if the file is a zip file, not used if the file is not a zip file
     * @param options discovery options
     * @return the list of layout info representing the layouts that can handle this data file.
     * @throws IOException if there is a problem reading the file
     */
    public static List<LayoutInfo> discoverFormat(File file, String zipEntryName, LayoutInfoDiscoveryOptions options) throws IOException {
        List<LayoutInfo> result = new ArrayList<>();

        if (options == null)
            options = new LayoutInfoDiscoveryOptions();

        // try the registered external layout
        for (Entry<String, Layout> entry : new TreeMap<>(_LAYOUTS).entrySet()) {
            if (!_INTERNAL_LAYOUTS.containsKey(entry.getKey())) {
                LayoutInfo info = entry.getValue().buildFileInfo(file, zipEntryName, options);
                if (info != null)
                    result.add(info);
            }
        }

        // try the registered internal layout
        for (String layoutId : _INTERNAL_LAYOUTS.keySet()) {
            if (_LAYOUTS.containsKey(layoutId)) {
                LayoutInfo info = _LAYOUTS.get(layoutId).buildFileInfo(file, zipEntryName, options);
                if (info != null)
                    result.add(info);
            }
        }

        // try the internal layout that have not been registered yet
        for (String layoutId : _INTERNAL_LAYOUTS.keySet()) {
            if (!_LAYOUTS.containsKey(layoutId) && !_INTERNAL_LAYOUT_ID_ALIASES.containsKey(layoutId)) {
                Layout layout = loadInternalLayout(layoutId, false);
                LayoutInfo info = layout.buildFileInfo(file, zipEntryName, options);
                if (info != null)
                    result.add(info);
            }
        }

        return result;
    }
}
