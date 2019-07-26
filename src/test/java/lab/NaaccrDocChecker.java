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
import com.imsweb.layout.Layout;
import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.TestingUtils;
import com.imsweb.seerutils.SeerUtils;

@SuppressWarnings({"ConstantConditions"})
public class NaaccrDocChecker extends JFrame {

    private static final File _DIR = new File(TestingUtils.getWorkingDirectory() + "\\src\\main\\resources\\layout\\fixed\\naaccr\\doc\\naaccr18");

    private static Layout _LAYOUT = LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18);

    public static void main(String[] args) throws Exception {

        for (File f : _DIR.listFiles()) {
            if (f.getName().endsWith(".html")) {
                if (!SeerUtils.isPureAscii(SeerUtils.readFile(f)))
                    System.err.println("!!! NON-ASCII CHARACTERS: " + f.getName());

                Field field = _LAYOUT.getFieldByName(f.getName().replace(".html", ""));
                if (field != null)
                    displayDoc(field);
                else
                    System.err.println("Unknown field: " + f.getName());
            }
        }
    }

    private static void displayDoc(Field field) {

        String fileContent;
        try {
            fileContent = SeerUtils.readFile(new File(_DIR, field.getName() + ".html"));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        String longName = field.getLongLabel();

        StringBuilder buf = new StringBuilder();
        buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        buf.append("\n");
        buf.append("<html>\n");
        buf.append("\n");
        buf.append("<head>\n");
        buf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n");
        buf.append("<title>").append(longName.replace("&", "&amp;")).append("</title>\n");
        buf.append("<style>\n");
        buf.append("body { padding:5px; font-family:Tahoma; font-size: 14px; }\n");
        buf.append("h1 { font-size:14px; margin-top:0px; }\n");
        buf.append(_LAYOUT.getFieldDocDefaultCssStyle());
        buf.append("</style>\n");
        buf.append("</head>\n");
        buf.append("\n");
        buf.append("<body>\n");
        buf.append("\n");
        buf.append("<h1>").append(longName.toUpperCase().replace("&", "&amp;")).append("</h1>\n");
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
            System.err.println(field.getName() + ": " + ex.getMessage());
        }
    }
}