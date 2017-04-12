/*
 * Copyright (C) 2017 Information Management Services, Inc.
 */
package com.imsweb.layout.hl7;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.imsweb.layout.hl7.entity.Hl7Component;
import com.imsweb.layout.hl7.entity.Hl7Field;
import com.imsweb.layout.hl7.entity.Hl7Message;
import com.imsweb.layout.hl7.entity.Hl7RepeatedField;
import com.imsweb.layout.hl7.entity.Hl7Segment;
import com.imsweb.layout.hl7.entity.Hl7SubComponent;

// TODO use the layout to provide a label to the field/components and subcomponents
// Allow every node to be expanded/collapsed, and apply to its children
// handle batch transmission segments
public class Hl7Viewer extends JFrame {

    public Hl7Viewer() {
        this.setTitle("NAACCR HL7 Viewer 0.1");
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.getContentPane().setLayout(new BorderLayout());

        JPanel centerPnl = new JPanel();
        centerPnl.setBorder(null);
        centerPnl.setLayout(new BorderLayout());
        this.getContentPane().add(centerPnl, BorderLayout.CENTER);

        List<Hl7Message> messages = new ArrayList<>();
        messages.add(Hl7MessageBuilder.createMessage()
                .withSegment("MSH")
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
                .withField(5, "DEPRY", "FABIAN", "P")
                .withField(10, "2106-3", "White", "HL70005")
                .withSegment("OBR")
                .withField(1, "1")
                .withField(3, "06-123456-MH")
                .withField(4, "22049-1", "Flow Cytometry Analysis", "LN")
                .withSegment("OBX")
                .withField(1, "1")
                .withField(2, "TX")
                .withField(3, "22633-2", "nature of specimen", "LN")
                .withField(5, "Bone Marrow")
                .build());

        //        try {
        //            messages.addAll(new NaaccrHl7Layout().readAllMessages(new File("C:\\dev\\projects\\seerdms\\app\\src\\test\\resources\\importer\\hl7_naaccr_good1.txt")));
        //        }
        //        catch (IOException e) {
        //            e.printStackTrace();
        //        }

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Messages");
        for (Hl7Message message : messages)
            rootNode.add(createNodeForMessage(message));
        JTree tree = new JTree(rootNode);
        tree.setBorder(new EmptyBorder(2, 5, 2, 5));

        //DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)tree.getCellRenderer();
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (leaf) {
                    comp.setFont(comp.getFont().deriveFont(Font.BOLD));
                    comp.setForeground(Color.BLACK);
                }
                else {
                    comp.setFont(comp.getFont().deriveFont(Font.PLAIN));
                    comp.setForeground(Color.DARK_GRAY);
                }
                return comp;
            }
        };
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);
        tree.setCellRenderer(renderer);

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(null);
        centerPnl.add(scrollPane, BorderLayout.CENTER);

        //for (int i = 0; i < tree.getRowCount(); ++i)
        //    tree.expandRow(i);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> SwingUtilities.invokeLater(() -> {
            String msg = "An unexpected error happened, it is recommended to close the application.\n\n   Error: " + (e.getMessage() == null ? "null access" : e.getMessage());
            JOptionPane.showMessageDialog(Hl7Viewer.this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        }));
    }

    private static DefaultMutableTreeNode createNodeForMessage(Hl7Message message) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EntityWrapper(message));
        message.getSegments().forEach(s -> node.add(createNodeForSegment(s)));
        return node;
    }

    private static DefaultMutableTreeNode createNodeForSegment(Hl7Segment segment) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EntityWrapper(segment));
        segment.getFields().keySet().stream().sorted().forEach(idx -> node.add(createNodeForField(segment.getField(idx))));
        return node;
    }

    private static DefaultMutableTreeNode createNodeForField(Hl7Field field) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EntityWrapper(field));
        if (field.getRepeatedFields().size() == 1) {
            Hl7RepeatedField repeatedField = field.getRepeatedField(1);
            if (repeatedField.getComponents().size() == 1 && repeatedField.getComponents().keySet().contains(1))
                node.add(createNodeForValue(repeatedField.getComponent(1).getValue()));
            else
                repeatedField.getComponents().values().forEach(c -> node.add(createNodeForComponent(c)));
        }
        else if (field.getRepeatedFields().size() > 1)
            field.getRepeatedFields().forEach(f -> node.add(createNodeForRepeatedField(f)));
        return node;
    }

    private static DefaultMutableTreeNode createNodeForRepeatedField(Hl7RepeatedField repeatedField) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EntityWrapper(repeatedField));
        repeatedField.getComponents().keySet().stream().sorted().forEach(idx -> node.add(createNodeForComponent(repeatedField.getComponent(idx))));
        return node;
    }

    private static DefaultMutableTreeNode createNodeForComponent(Hl7Component component) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EntityWrapper(component));
        // TODO FD this simplification should check the fields definition instead of checking the size/index...
        if (component.getSubComponents().size() == 1 && component.getSubComponents().keySet().contains(1))
            node.add(createNodeForValue(component.getSubComponent(1).getValue()));
        else
            component.getSubComponents().keySet().stream().sorted().forEach(idx -> node.add(createNodeForSubComponent(component.getSubComponent(idx))));
        return node;
    }

    private static DefaultMutableTreeNode createNodeForSubComponent(Hl7SubComponent subComponent) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new EntityWrapper(subComponent));
        node.add(createNodeForValue(subComponent.getValue()));
        return node;
    }

    private static DefaultMutableTreeNode createNodeForValue(String value) {
        return new DefaultMutableTreeNode(new EntityWrapper(value == null ? "<blank>" : value));
    }

    private static class EntityWrapper {

        private Object _entity;

        public EntityWrapper(Object entity) {
            _entity = entity;
        }

        @Override
        public String toString() {
            String result;
            switch (_entity.getClass().getSimpleName()) {
                case "String":
                    result = (String)_entity;
                    break;
                case "Hl7SubComponent":
                    result = subComponentToString((Hl7SubComponent)_entity);
                    break;
                case "Hl7Component":
                    result = componentToString((Hl7Component)_entity);
                    break;
                case "Hl7RepeatedField":
                    result = repeatedFieldToString((Hl7RepeatedField)_entity);
                    break;
                case "Hl7Field":
                    result = fieldToString((Hl7Field)_entity);
                    break;
                case "Hl7Segment":
                    result = segmentToString((Hl7Segment)_entity);
                    break;
                case "Hl7Message":
                    result = messageToString((Hl7Message)_entity);
                    break;
                default:
                    result = _entity.getClass().getSimpleName();
            }
            return result;
        }

        private static String messageToString(Hl7Message message) {
            return "Message";
        }

        private static String segmentToString(Hl7Segment segment) {
            return segment.getId();
        }

        private static String fieldToString(Hl7Field field) {
            Hl7Segment segment = field.getSegment();
            return segment.getId() + "-" + field.getIndex();
        }

        private static String repeatedFieldToString(Hl7RepeatedField repeatedField) {
            Hl7Field field = repeatedField.getField();
            return "Repetition #" + (field.getRepeatedFields().indexOf(repeatedField) + 1);
        }

        private static String componentToString(Hl7Component component) {
            Hl7Field field = component.getRepeatedField().getField();
            Hl7Segment segment = field.getSegment();
            return segment.getId() + "-" + field.getIndex() + "." + component.getIndex();
        }

        private static String subComponentToString(Hl7SubComponent subComponent) {
            Hl7Component component = subComponent.getComponent();
            Hl7Field field = component.getRepeatedField().getField();
            Hl7Segment segment = field.getSegment();
            return segment.getId() + "-" + field.getIndex() + "." + component.getIndex() + "." + subComponent.getIndex();
        }
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
