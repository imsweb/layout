/*
 * Copyright (C) 2019 Information Management Services, Inc.
 */
package lab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.swing.ScalableXHTMLPanel;

import com.imsweb.layout.Field;
import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.TestingUtils;
import com.imsweb.layout.naaccrxml.NaaccrXmlField;
import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;
import com.imsweb.seerutils.SeerUtils;
import com.imsweb.seerutilsgui.SeerBoldTitlesTabbedPane;
import com.imsweb.seerutilsgui.SeerBoldTitlesTabbedPanePage;
import com.imsweb.seerutilsgui.SeerGuiUtils;
import com.imsweb.seerutilsgui.SeerList;
import com.imsweb.seerutilsgui.editor.SyntaxKit;

@SuppressWarnings({"ConstantConditions", "FieldCanBeLocal", "SameParameterValue"})
public class NaaccrDocViewer extends JFrame {

    private static final File _DIR = new File(TestingUtils.getWorkingDirectory() + "\\src\\main\\resources\\layout\\fixed\\naaccr\\doc\\naaccr24");

    private static final List<Layout> _LAYOUTS = new ArrayList<>();

    static {
        _LAYOUTS.add(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_24));
        _LAYOUTS.add(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_23));
        _LAYOUTS.add(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_22));
        _LAYOUTS.add(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_21));
        _LAYOUTS.add(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_18));
        _LAYOUTS.add(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_16));
        _LAYOUTS.add(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_15));
        _LAYOUTS.add(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_14));
        //_LAYOUTS.add(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_13));
        //_LAYOUTS.add(LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_12));
    }

    private static final Boolean _SORT_BY_ITEM_NUMBER = false;

    private final JPanel _leftPnl, _rightPnl;
    private final JSplitPane _splitPane;
    private SeerBoldTitlesTabbedPane _pane;

    /**
     * Special borders
     */
    private static final Border _BORDER_FIELD_OUT = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(2, 2, 2, 1));
    private static final Border _BORDER_FIELD_IN = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(2, 2, 2, 1));

    public static Field getFieldFromLayout(String name) {
        for (Layout layout : _LAYOUTS) {
            Field field;
            if (NumberUtils.isDigits(name))
                field = layout.getFieldByNaaccrItemNumber(Integer.valueOf(name));
            else
                field = layout.getFieldByName(name);
            if (field == null && layout.getLayoutVersion().compareTo("140") >= 0) {
                NaaccrDictionary dictionary = NaaccrXmlDictionaryUtils.getMergedDictionaries(layout.getLayoutVersion());
                NaaccrDictionaryItem item = NumberUtils.isDigits(name) ? dictionary.getGroupedItemByNaaccrNum(Integer.valueOf(name)) : dictionary.getGroupedItemByNaaccrId(name);
                if (item != null)
                    field = new NaaccrXmlField(item);
            }
            if (field != null)
                return field;
        }
        return null;
    }

    public static String getFieldDocDefaultCssStyle() {
        return _LAYOUTS.get(0).getFieldDocDefaultCssStyle();
    }

    /**
     * Created on Oct 26, 2011 by depryf
     */
    public NaaccrDocViewer() {
        this.setTitle("NAACCR Documentation Viewer");
        this.setPreferredSize(new Dimension(1200, 800));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final Map<String, String> labelMappings = new HashMap<>();
        List<String> names = new ArrayList<>();
        for (File file : _DIR.listFiles()) {
            if (file.getName().endsWith(".html")) {
                String filenameNotExtension = file.getName().replace(".html", "");
                boolean retired = NumberUtils.isDigits(filenameNotExtension);

                Field field = getFieldFromLayout(filenameNotExtension);
                if (field != null) {
                    String label = (retired ? "[RETIRED] " : "") + field.getLongLabel() + " (#" + field.getNaaccrItemNum() + ")";
                    labelMappings.put(label, filenameNotExtension);
                    names.add(label);
                }
                else if (retired) {
                    String label = "[RETIRED] _ No Name Available (#" + filenameNotExtension + ")";
                    labelMappings.put(label, filenameNotExtension);
                    names.add(label);
                }
                else {
                    String label = "[UNKNOWN] (" + file.getName() + " )";
                    labelMappings.put(label, filenameNotExtension);
                    names.add(label);
                }
            }
        }

        //        Pattern p = Pattern.compile("[^_]blank", Pattern.CASE_INSENSITIVE);
        //        System.out.println("NAACCR XML ID,NAACCR Number,NAACCR Name,Description Sample");
        //        names.stream().forEach(i -> {
        //            String doc = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_21).getFieldDocByName(labelMappings.get(i));
        //            if (doc == null)
        //                return;
        //
        //            Field f = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_XML_21).getFieldByName(labelMappings.get(i));
        //            if (f == null)
        //                return;
        //
        //            for (String line : doc.split("\r?\n")) {
        //                line = line.trim();
        //                Matcher m = p.matcher(line);
        //                if (m.find()) {
        //                    String s = line.substring(Math.max(0, m.start() - 50), Math.min(line.length(), m.end() + 50));
        //                    if (!line.startsWith(s))
        //                        s = "..." + s;
        //                    if (!line.endsWith(s))
        //                        s = s + "...";
        //                    System.out.println(f.getName() + "," + f.getNaaccrItemNum() + ",\"" + f.getLongLabel() + "\",\"" + s + "\"");
        //                }
        //            }
        //        });

        names.sort((o1, o2) -> {
            if (_SORT_BY_ITEM_NUMBER) {
                if (o1.contains("#") && o2.contains("#") && o1.endsWith(")") && o2.endsWith(")")) {
                    String num1 = o1.substring(o1.lastIndexOf('#') + 1, o1.length() - 1);
                    String num2 = o2.substring(o2.lastIndexOf('#') + 1, o2.length() - 1);
                    return Integer.valueOf(num1).compareTo(Integer.valueOf(num2));
                }
                else
                    throw new IllegalStateException("Bad format: " + o1 + "; " + o2);
            }
            return o1.compareToIgnoreCase(o2);
        });

        JPanel contentPnl = new JPanel();
        contentPnl.setOpaque(true);
        contentPnl.setLayout(new BorderLayout());
        contentPnl.setBackground(new Color(180, 191, 211));
        contentPnl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(contentPnl, BorderLayout.CENTER);

        // LEFT - list of fieds
        _leftPnl = SeerGuiUtils.createPanel();
        _leftPnl.setOpaque(true);
        _leftPnl.setBackground(new Color(180, 191, 211));

        // WEST/CENTER - list
        final SeerList<String> list = new SeerList<>(names, SeerList.DISPLAY_MODE_DOTTED_LINES, SeerList.FILTERING_MODE_CONTAINED);
        list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        list.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            String name = (String)list.getSelectedValue();
            if (name != null)
                displayDoc(labelMappings.get(name));
        });
        _leftPnl.add(new JScrollPane(list), BorderLayout.CENTER);

        // LEFT/NORTH - filter
        JPanel filterPnl = SeerGuiUtils.createPanel();
        filterPnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        final JTextField filterFld = new JTextField(12);
        filterFld.setBorder(_BORDER_FIELD_OUT);
        filterFld.addFocusListener(createDefaultFocusListener(_BORDER_FIELD_IN, _BORDER_FIELD_OUT));
        filterFld.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN)
                    list.requestFocusInWindow();
                else {
                    list.filter(filterFld.getText());
                    if (list.getModel().getSize() > 0)
                        list.setSelectedIndex(0);
                }
            }
        });
        filterPnl.add(filterFld, BorderLayout.CENTER);
        _leftPnl.add(filterPnl, BorderLayout.NORTH);

        // RIGHT - doc
        _rightPnl = SeerGuiUtils.createPanel();
        _rightPnl.setOpaque(true);
        _rightPnl.setBackground(new Color(180, 191, 211));

        // CENTER - split pane
        _splitPane = new JSplitPane();
        _splitPane.setBorder(null);
        _splitPane.setDividerSize(5);
        if (_splitPane.getUI() instanceof BasicSplitPaneUI) {
            ((BasicSplitPaneUI)_splitPane.getUI()).getDivider().setBorder(null);
            ((BasicSplitPaneUI)_splitPane.getUI()).getDivider().setBackground(new Color(180, 191, 211));
        }
        _splitPane.setLeftComponent(_leftPnl);
        _splitPane.setRightComponent(_rightPnl);
        contentPnl.add(_splitPane, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                list.setSelectedIndex(0);
                list.requestFocusInWindow();
                NaaccrDocViewer.this.removeComponentListener(this);
            }
        });
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public static String addStyleAndBody(String title, String content) {

        StringBuilder buf = new StringBuilder();
        buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        buf.append("\n");
        buf.append("<html>\n");
        buf.append("\n");
        buf.append("<head>\n");
        buf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n");
        buf.append("<title>").append(title.replace("&", "&amp;")).append("</title>\n");
        buf.append("<style>\n");
        buf.append("body { padding:5px; font-family:Tahoma; font-size: 14px; }\n");
        buf.append("h1 { font-size:14px; margin-top:0px; }\n");
        buf.append(getFieldDocDefaultCssStyle());
        buf.append("</style>\n");
        buf.append("</head>\n");
        buf.append("\n");
        buf.append("<body>\n");
        buf.append("\n");
        buf.append("<h1>").append(title.toUpperCase().replace("&", "&amp;")).append("</h1>\n");
        buf.append("\n");
        buf.append(content);
        buf.append("</body>\n");
        buf.append("</html>\n");

        return buf.toString();
    }

    private void displayDoc(String name) {
        _rightPnl.removeAll();

        Field field = getFieldFromLayout(name);
        String fileTitle = field == null ? "Retired Field - No Name Available (pre NAACCR 12 layout)" : field.getLongLabel();

        String fileContent;
        try {
            fileContent = SeerUtils.readFile(new File(_DIR, name + ".html"), "UTF-8");
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
        String content = addStyleAndBody(fileTitle, fileContent);

        String title = _pane == null ? null : _pane.getCurrentPageTitle();
        _pane = new SeerBoldTitlesTabbedPane();
        _pane.setSynchronizeHeaderWidths(true);
        _pane.setCenterTitles(true);
        _pane.setTabPlacement(JTabbedPane.BOTTOM);

        ScalableXHTMLPanel pnl = new ScalableXHTMLPanel();
        pnl.getSharedContext().getTextRenderer().setSmoothingThreshold(-1);
        try {
            pnl.setDocument(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), "");

            FSScrollPane pane = new FSScrollPane(pnl);
            pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            pane.setBorder(null);

            JPanel p1 = new SeerBoldTitlesTabbedPanePage(_pane);
            p1.add(pane, BorderLayout.CENTER);
            _pane.addPage("Display", p1);

        }
        catch (Exception ex) {
            JPanel p = new SeerBoldTitlesTabbedPanePage(_pane);
            JEditorPane e = new JEditorPane();
            e.setFont(e.getFont().deriveFont((float)10));
            e.setEditable(false);
            e.setForeground(Color.RED);
            e.setText(ExceptionUtils.getStackTrace(ex));
            JScrollPane s = new JScrollPane(e);
            s.setBorder(null);
            p.add(s, BorderLayout.CENTER);
            _pane.addPage("Display", p);
        }

        JPanel p2 = new SeerBoldTitlesTabbedPanePage(_pane);
        JEditorPane e2 = new JEditorPane();
        e2.setEditable(false);
        e2.setEditorKit(new SyntaxKit(SyntaxKit.SYNTAX_TYPE_XML, null));
        e2.setText(fileContent);
        JScrollPane s2 = new JScrollPane(e2);
        s2.setBorder(null);
        p2.add(s2, BorderLayout.CENTER);
        _pane.addPage("Raw XML", p2);

        JPanel p3 = new SeerBoldTitlesTabbedPanePage(_pane);
        JEditorPane e3 = new JEditorPane();
        e3.setEditable(false);
        e3.setEditorKit(new SyntaxKit(SyntaxKit.SYNTAX_TYPE_XML, null));
        e3.setText(content);
        JScrollPane s3 = new JScrollPane(e3);
        s3.setBorder(null);
        p3.add(s3, BorderLayout.CENTER);
        _pane.addPage("Full XML", p3);

        _rightPnl.add(_pane, BorderLayout.CENTER);
        if (title != null)
            _pane.displayPage(title);
        _rightPnl.revalidate();
    }

    private static FocusListener createDefaultFocusListener(final Border in, final Border out) {
        return new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                setFocus(e, in);
            }

            @Override
            public void focusLost(FocusEvent e) {
                setFocus(e, out);
            }

            private void setFocus(FocusEvent e, Border b) {
                JComponent father = (JComponent)e.getComponent().getParent();
                if (father instanceof JComboBox)
                    father.setBorder(b);
                else {
                    JComponent grandfather = (JComponent)father.getParent();
                    if (grandfather instanceof JScrollPane)
                        grandfather.setBorder(b);
                    else
                        ((JComponent)e.getComponent()).setBorder(b);
                }
            }
        };
    }

    /**
     * Created on Oct 26, 2011 by depryf
     * @param args arguments
     */
    public static void main(String[] args) throws Exception {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        Insets insets = UIManager.getInsets("TabbedPane.tabAreaInsets");
        insets.bottom = 0;
        UIManager.put("TabbedPane.tabAreaInsets", insets);

        final NaaccrDocViewer viewer = new NaaccrDocViewer();
        viewer.pack();
        SeerGuiUtils.centerWindow(viewer, null);

        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));

        // show login dialog
        SwingUtilities.invokeLater(() -> viewer.setVisible(true));
    }
}
