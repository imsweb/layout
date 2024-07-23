/*
 * Copyright (C) 2021 Information Management Services, Inc.
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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneUI;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import com.imsweb.seerutilsgui.SeerGuiUtils;
import com.imsweb.seerutilsgui.SeerList;

/**
 * Lookups need to be generated from the "ExtractLookupsLab" in SEER*DMS.
 */
public class NaaccrLookupsViewer extends JFrame {

    private static final File _DIR = new File("C:\\dev\\temp\\naaccr-lookups\\naaccr-lookups-240");

    private JTextArea _textArea = null;

    @SuppressWarnings("DataFlowIssue")
    public NaaccrLookupsViewer() {
        this.setTitle("NAACCR Lookups Viewer");
        this.setPreferredSize(new Dimension(1200, 800));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        List<String> names = new ArrayList<>();
        for (File file : _DIR.listFiles())
            names.add(file.getName());
        Collections.sort(names);

        JPanel contentPnl = new JPanel();
        contentPnl.setOpaque(true);
        contentPnl.setLayout(new BorderLayout());
        contentPnl.setBackground(new Color(180, 191, 211));
        contentPnl.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(contentPnl, BorderLayout.CENTER);

        // LEFT - list of fieds
        JPanel leftPnl = SeerGuiUtils.createPanel();
        leftPnl.setOpaque(true);
        leftPnl.setBackground(new Color(180, 191, 211));

        // WEST/CENTER - list
        final SeerList<String> list = new SeerList<>(names, SeerList.DISPLAY_MODE_DOTTED_LINES, SeerList.FILTERING_MODE_CONTAINED);
        list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        list.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            String name = (String)list.getSelectedValue();
            if (name != null) {
                try (CSVReader reader = new CSVReader(new FileReader(new File(_DIR, name)))) {
                    StringBuilder buf = new StringBuilder();
                    for (String[] row : reader.readAll())
                        buf.append(row[0]).append(": ").append(row[1]).append("\n");
                    _textArea.setText(buf.toString());
                    _textArea.setCaretPosition(0);

                }
                catch (IOException | CsvException ex) {
                    _textArea.setText(ex.getMessage());
                }
            }

        });
        leftPnl.add(new JScrollPane(list), BorderLayout.CENTER);

        // LEFT/NORTH - filter
        JPanel filterPnl = SeerGuiUtils.createPanel();
        filterPnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        final JTextField filterFld = new JTextField(12);
        filterFld.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(2, 2, 2, 1)));
        filterFld.addFocusListener(createDefaultFocusListener());
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
        leftPnl.add(filterPnl, BorderLayout.NORTH);

        // RIGHT - doc
        JPanel rightPnl = SeerGuiUtils.createPanel();
        rightPnl.setOpaque(true);
        rightPnl.setBackground(new Color(180, 191, 211));
        _textArea = new JTextArea();
        JScrollPane pane = new JScrollPane(_textArea);
        rightPnl.add(pane, BorderLayout.CENTER);

        // CENTER - split pane
        JSplitPane splitPane = new JSplitPane();
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        if (splitPane.getUI() instanceof BasicSplitPaneUI) {
            ((BasicSplitPaneUI)splitPane.getUI()).getDivider().setBorder(null);
            ((BasicSplitPaneUI)splitPane.getUI()).getDivider().setBackground(new Color(180, 191, 211));
        }
        splitPane.setLeftComponent(leftPnl);
        splitPane.setRightComponent(rightPnl);
        contentPnl.add(splitPane, BorderLayout.CENTER);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                list.setSelectedIndex(0);
                list.requestFocusInWindow();
                NaaccrLookupsViewer.this.removeComponentListener(this);
            }
        });
    }

    private FocusListener createDefaultFocusListener() {
        return new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                setFocus(e, BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(2, 2, 2, 1)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                setFocus(e, BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(2, 2, 2, 1)));
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

    public static void main(String[] args) throws Exception {

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        Insets insets = UIManager.getInsets("TabbedPane.tabAreaInsets");
        insets.bottom = 0;
        UIManager.put("TabbedPane.tabAreaInsets", insets);

        final NaaccrLookupsViewer viewer = new NaaccrLookupsViewer();
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
