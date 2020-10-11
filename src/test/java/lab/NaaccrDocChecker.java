/*
 * Copyright (C) 2019 Information Management Services, Inc.
 */
package lab;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.ScrollPaneConstants;

import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.swing.ScalableXHTMLPanel;

import com.imsweb.layout.Field;
import com.imsweb.layout.TestingUtils;
import com.imsweb.seerutils.SeerUtils;

@SuppressWarnings({"ConstantConditions"})
public class NaaccrDocChecker extends JFrame {

    private static final File _DIR = new File(TestingUtils.getWorkingDirectory() + "\\src\\main\\resources\\layout\\fixed\\naaccr\\doc\\naaccr21");

    public static void main(String[] args) {

        for (File file : _DIR.listFiles()) {
            if (file.getName().endsWith(".html")) {
                Field field = NaaccrDocViewer.getFieldFromLayout(file.getName().replace(".html", ""));
                String title = field != null ? field.getLongLabel() : "No Name Available";
                displayDoc(file, title);
            }
        }
    }

    private static void displayDoc(File file, String title) {

        String fileContent;
        try {
            fileContent = SeerUtils.readFile(file);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        buf.append(NaaccrDocViewer.getFieldDocDefaultCssStyle());
        buf.append("</style>\n");
        buf.append("</head>\n");
        buf.append("\n");
        buf.append("<body>\n");
        buf.append("\n");
        buf.append("<h1>").append(title.toUpperCase().replace("&", "&amp;")).append("</h1>\n");
        buf.append("\n");
        buf.append(fileContent);
        buf.append("</body>\n");
        buf.append("</html>\n");

        ScalableXHTMLPanel pnl = new ScalableXHTMLPanel();
        pnl.getSharedContext().getTextRenderer().setSmoothingThreshold(-1);
        try {
            pnl.setDocument(new ByteArrayInputStream(buf.toString().getBytes()), "");

            FSScrollPane pane = new FSScrollPane(pnl);
            pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            pane.setBorder(null);
        }
        catch (Exception ex) {
            System.err.println(file.getName() + ": " + ex.getMessage());
        }
    }
}