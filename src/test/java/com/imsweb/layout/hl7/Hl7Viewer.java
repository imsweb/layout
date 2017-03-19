/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import com.imsweb.layout.hl7.entity.Hl7Component;
import com.imsweb.layout.hl7.entity.Hl7Field;
import com.imsweb.layout.hl7.entity.Hl7Message;
import com.imsweb.layout.hl7.entity.Hl7Segment;
import com.imsweb.layout.hl7.entity.Hl7SubComponent;

public class Hl7Viewer extends JFrame {

    public Hl7Viewer() {
        this.setTitle("NAACCR HL7 Utility 0.1");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(new BorderLayout());

        JPanel centerPnl = new JPanel();
        centerPnl.setBorder(null);
        centerPnl.setLayout(new BorderLayout());
        this.getContentPane().add(centerPnl, BorderLayout.CENTER);

        Hl7Message message = Hl7MessageBuilder.createMessage()
                .withSegment("PID")
                .withField(3)
                .withRepeatedField()
                .withComponent(1, "010203040")
                .withComponent(5, "MR")
                .withComponent(6, "STJ", "03D1234567", "AHA")
                .withRepeatedField()
                .withComponent(1, "111223333")
                .withComponent(5, "SS")
                .withRepeatedField()
                .withComponent(1, "97 810430")
                .withComponent(5, "PI")
                .withComponent(6, "HITECK PATH LAB-ATL", "3D932840", "CLIA")
                .withField(5)
                .withComponent(1, "DEPRY")
                .withComponent(2, "FABIAN")
                .withComponent(3, "P")
                .build();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Message");
        message.getSegments().forEach(s -> root.add(createNodeForSegment(s)));
        JTree tree = new JTree(root);
        centerPnl.add(tree, BorderLayout.CENTER);

        for (int i = 0; i < tree.getRowCount(); ++i)
            tree.expandRow(i);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> SwingUtilities.invokeLater(() -> {
            String msg = "An unexpected error happened, it is recommended to close the application.\n\n   Error: " + (e.getMessage() == null ? "null access" : e.getMessage());
            JOptionPane.showMessageDialog(Hl7Viewer.this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        }));
    }

    private static DefaultMutableTreeNode createNodeForSegment(Hl7Segment segment) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(segment.getId());
        segment.getFields().values().forEach(f -> createNodeForField(f).forEach(node::add));
        return node;
    }

    private static List<DefaultMutableTreeNode> createNodeForField(Hl7Field field) {
        Hl7Segment segment = field.getSegment();
        List<DefaultMutableTreeNode> nodes = new ArrayList<>();
        if (field.getRepeatedFields().size() == 1) {
            // TODO simplify this if single component?
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(segment.getId() + "-" + field.getIndex());
            field.getRepeatedField(1).getComponents().values().forEach(c -> node.add(createNodeForComponent(c)));
            nodes.add(node);
        }
        else if (field.getRepeatedFields().size() > 1) {
            for (int i = 1; i <= field.getRepeatedFields().size(); i++) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(segment.getId() + "-" + field.getIndex() + " (Repetition " + i + " of " + field.getRepeatedFields().size() + ")");
                field.getRepeatedField(i).getComponents().values().forEach(c -> node.add(createNodeForComponent(c)));
                nodes.add(node);
            }
        }
        return nodes;
    }

    private static DefaultMutableTreeNode createNodeForComponent(Hl7Component component) {
        Hl7Field field = component.getRepeatedField().getField();
        Hl7Segment segment = field.getSegment();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(segment.getId() + "-" + field.getIndex() + "." + component.getIndex());
        // TODO FD this simplification should check the fields definition instead of checking the size/index...
        if (component.getSubComponents().size() == 1 && component.getSubComponents().keySet().contains(1))
            node.add(createNodeForValue(component.getSubComponent(1).getValue()));
        else
            component.getSubComponents().values().forEach(s -> node.add(createNodeForSubComponent(s)));
        return node;
    }

    private static DefaultMutableTreeNode createNodeForSubComponent(Hl7SubComponent subComponent) {
        Hl7Component component = subComponent.getComponent();
        Hl7Field field = component.getRepeatedField().getField();
        Hl7Segment segment = field.getSegment();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(segment.getId() + "-" + field.getIndex() + "." + component.getIndex() + "." + subComponent.getIndex());
        node.add(createNodeForValue(subComponent.getValue()));
        return node;
    }

    private static DefaultMutableTreeNode createNodeForValue(String value) {
        return new DefaultMutableTreeNode(value == null ? "<blank>" : value);
    }

    public static void main(String[] args) {

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
                // ignored, the look and feel will be the default Java one...
            }
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
            Insets insets = UIManager.getInsets("TabbedPane.tabAreaInsets");
            insets.bottom = 0;
            UIManager.put("TabbedPane.tabAreaInsets", insets);
        }

        final JFrame frame = new Hl7Viewer();
        frame.pack();

        // start in the middle of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point center = new Point(screenSize.width / 2, screenSize.height / 2);
        frame.setLocation(center.x - frame.getWidth() / 2, center.y - frame.getHeight() / 2);

        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}
