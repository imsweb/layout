/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package lab;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.imsweb.layout.LayoutFactory;
import com.imsweb.layout.TestingUtils;
import com.imsweb.layout.record.fixed.FixedColumnsField;
import com.imsweb.layout.record.fixed.naaccr.NaaccrLayout;

/*********************************************************************************************************
 *     Use this class to generate the NAACCR documentation from the NAACCR website.
 *
 *     !!!!    MAKE SURE TO AUTO-FORMAT THE ENTIRE FOLDER AFTER CREATING THE FILES    !!!!
 *     !!!!    ALSO MAKE SURE YOU RUN THE VIEWER AND CHECKER AFTER AN UPDATE          !!!!
 *
 *     This class also creates the styles that need to be copied into the NaaccrLayout class.
 *
 *     After creating the files with this class, use the "NaaccrDocViewer" and review every created file;
 *     that utility class is in the SEER*Utils GUI project (test/naaccr)
 *
 *     2014/10/25 FD - didn't need to redo the styles for NAACCR 15/16/18, they were the same as NAACCR 14...
 *
 *     2019/07/26 FD - I added support for "retired fields"; those will use their NAACCR number in the HTML
 *                     filename since there is no legit field name for them.  They can only be returned by
 *                     the "getFieldDocByNaaccrItemNumber" method.
 *
 *********************************************************************************************************/
@SuppressWarnings({"MismatchedQueryAndUpdateOfStringBuilder", "ConstantConditions"})
public class NaaccrDocScraper {

    public static void main(String[] args) throws Exception {
        // output directory
        File outputDir = new File(TestingUtils.getWorkingDirectory() + "\\src\\main\\resources\\layout\\fixed\\naaccr\\doc\\naaccr18");

        // the layout to use to gather the fields
        NaaccrLayout layout = (NaaccrLayout)LayoutFactory.getLayout(LayoutFactory.LAYOUT_ID_NAACCR_18);

        // this is the URL to read the full HTML page from
        //URL url = new URL("http://datadictionary.naaccr.org/?c=10");
        URL url = Thread.currentThread().getContextClassLoader().getResource("doc/naaccr-18.html");

        // this is the URL to read the style sheet from
        //URL styleSheetUrl = new URL("http://datadictionary.naaccr.org/Styles/ContentReader.css");
        URL styleSheetUrl = Thread.currentThread().getContextClassLoader().getResource("doc/naaccr-18-style.css");

        // create the stylesheet
        StringBuilder styleBuf = new StringBuilder();
        String styleSheet = new Scanner(styleSheetUrl.openStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();
        for (String line : styleSheet.replace("{\r\n", "{").replace(";\r\n", ";").replace("\r\n\r\n", "\r\n").replace("    ", " ").split("\\r?\\n")) {
            line = line.trim();
            if (!line.isEmpty() && !line.contains("body") && !line.contains("/*") && !line.contains("*/") && !line.startsWith(".mark-changed") && !line.contains("Times New Roman") && !line.contains(
                    "font-size"))
                styleBuf.append("        _DEFAULT_CSS_14_AND_LATER.append(\"").append(line).append("\\r\n\");\r\n");
        }
        // the printed style should be copied into the NaaccrLayout class...
        //System.out.println(styleBuf);

        // read the page
        String fullContent = new Scanner(url.openStream(), StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

        // split by anchors (every item section starts with an anchor with its NAACCR number)
        Map<String, String> items = new HashMap<>();
        Matcher matcher = Pattern.compile("<a\\sname='(\\d+)'></a>").matcher(fullContent);
        String itemNumber = "";
        int start = 0;
        while (matcher.find(start)) {
            if (start != 0 && NumberUtils.isDigits(itemNumber))
                items.put(itemNumber, fullContent.substring(start, matcher.start()));
            itemNumber = matcher.group(1);
            start = matcher.end();
        }
        String lastContent = fullContent.substring(start);
        items.put(itemNumber, lastContent.substring(0, lastContent.lastIndexOf("</div>")));

        // gather the fields so we can use the property names as file names
        Map<String, String> fields = new HashMap<>();
        for (FixedColumnsField field : layout.getAllFields()) {
            if (field.getNaaccrItemNum() != null)
                fields.put(field.getNaaccrItemNum().toString(), field.getName());
            if (field.getSubFields() != null) {
                for (FixedColumnsField subField : field.getSubFields()) {
                    if (subField.getNaaccrItemNum() != null)
                        fields.put(subField.getNaaccrItemNum().toString(), subField.getName());
                }
            }
        }

        // go through each field and create the corresponding file
        if (!outputDir.exists() && !outputDir.mkdirs())
            throw new RuntimeException("Unable to create target folder!");
        for (Entry<String, String> entry : items.entrySet()) {
            String itemNum = entry.getKey();
            String html = entry.getValue();
            String fieldName = fields.getOrDefault(itemNum, itemNum);
            if (html != null) {

                // bad & characters
                html = cleanupHtml(html);

                // special cases
                switch (itemNum) {
                    case "145":
                        html = html.replace("<5%", "&lt;5%").replace("<10%", "&lt;10%").replace("<20%", "&lt;20%");
                        break;
                    case "170":
                    case "180":
                        html = html.replace("SEER < 1988", "SEER &lt; 1988");
                        break;
                    case "2130":
                        html = html.replace("<i>2011 SEER Coding Manual", "<i>2011 SEER Coding Manual</i>");
                        break;
                    case "2085":
                        html = html.replace("<span style=\"font-family:Symbol;\">&#183;<span style=\"Times New Roman&quot;\">&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</span></span>", "");
                        break;
                    case "3811":
                        html = html.replace("Hgb <", "Hgb &lt;");
                        break;
                    case "3930":
                    case "3931":
                        html = html.replace("<3.5", "&lt;3.5").replace("<5.5", "&lt;5.5");
                        break;
                    case "3933":
                        html = html.replace("< 100", "&lt; 100");
                        break;
                    case "2040":
                        html = html.replace(
                                "<leave blank=\"\" and=\"\" correct=\"\" any=\"\" errors=\"\" for=\"\" the=\"\" case=\"\" if=\"\" an=\"\" item=\"\" is=\"\" discovered=\"\" to=\"\" be=\"\"  incorrect.<=\"\" tab=\"\"><tab><tab><tab><tab><tab><tab><tab><tab><tab><tab><tab>Code 1, 2, or 3 as indicated if review of all items in the error or warning message confirms that all are correct.</tab></tab></tab></tab></tab></tab></tab></tab></tab></tab></tab></leave>",
                                "Code 1, 2, or 3 as indicated if review of all items in the error or warning message confirms that all are correct.");
                        break;
                    case "345":
                    case "346":
                        html = html.replace("< 100", "&lt; 100").replace("< 50", "&lt; 50");
                        break;
                    default:
                        // ignored, nothing to do
                }

                html = html.replace("<br>", "<br/>");

                // parse out the table
                int idx = html.indexOf("</table>");
                String summary = cleanupSummaryTable(html.substring(0, idx + 8));

                // parse out the extra section with alternate names, NAACCR XML ID and Parent XML Tag
                int idx2 = html.indexOf("</table>", idx + 8);
                String metaData = cleanupMetaDataTable(html.substring(idx + 8, idx2 + 8));

                // rest is the true content
                String content = html.substring(idx2 + 8);

                // clean up some table borders
                if (Arrays.asList("2330", "2350", "2660", "2640", "2650", "2670", "2620", "2610", "560", "2550", "2560", "2570", "2520", "2540", "2530", "2590", "2580", "2600").contains(itemNum))
                    content = content.replace("<table style=", "<table class=\"naaccr - borders\" style=").replace("<td style=", "<td class=\"naaccr - borders\" style=");

                // write the resulting file
                try {
                    FileUtils.writeStringToFile(new File(outputDir, fieldName + ".html"), summary + "\r\n\r\n" + metaData + "\r\n\r\n" + content, "UTF-8");
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
                System.out.println("Can't find content for item number " + itemNum);
        }
    }

    private static String cleanupHtml(String html) {

        html = html.replaceAll("<script.+?</script>", "");

        // make sure we don't replace codes that are already escaped (there are many other codes, I am handling only the most common ones...)
        html = html.replace("&quot;", "@@@QUOTE@@@");
        html = html.replace("&amp;", "@@@AND@@@");
        html = html.replace("&lt;", "@@@LESS@@@");
        html = html.replace("&gt;", "@@@GREATER@@@");
        html = html.replace("&nbsp;", "@@@SPACE@@@");
        html = html.replace("&#", "@@@NUMBER@@@");

        // then replace the remaining & by the escaped version
        html = html.replace("&", "&amp;");

        // and finally, replace back the original escaped ones
        html = html.replace("@@@QUOTE@@@", "&quot;");
        html = html.replace("@@@AND@@@", "&amp;");
        html = html.replace("@@@LESS@@@", "&lt;");
        html = html.replace("@@@GREATER@@@", "&gt;");
        html = html.replace("@@@SPACE@@@", "&nbsp;");
        html = html.replace("@@@NUMBER@@@", "&#");

        return html;
    }

    // it's easier to re-construct the entire table than trying to replace part of it...
    private static String cleanupSummaryTable(String txt) {
        StringBuilder buf = new StringBuilder();
        buf.append("<table class=\"naaccr-summary-table naaccr-borders\">");

        // split by rows
        Matcher rowMatcher = Pattern.compile("<tr( class=\".+?\")?>(.+?)</tr>", Pattern.MULTILINE | Pattern.DOTALL).matcher(txt);
        int rowStart = 0, rowIdx = 0;
        while (rowMatcher.find(rowStart)) {
            if (rowStart != 0) { // we ignore the first row which is the field name
                String row = rowMatcher.group(2);
                rowIdx++;

                buf.append("\r\n  <tr>");
                Matcher cellMatcher = Pattern.compile("<td(.*?)>(.*?)</td>", Pattern.MULTILINE | Pattern.DOTALL).matcher(row);
                int cellStart = 0;
                while (cellMatcher.find(cellStart)) {
                    String cell = cellMatcher.group(2).replace("<a href='#sources'>", "").replace("</a>", "");

                    if (rowIdx == 1)
                        buf.append("\r\n    <th class=\"naaccr-summary-header naaccr-borders\">").append(cell).append("</th>");
                    else
                        buf.append("\r\n    <td class=\"naaccr-summary-cell naaccr-borders naaccr-summary-centered\">").append(cell).append("</td>");
                    cellStart = cellMatcher.end();
                }
                buf.append("\r\n  </tr>");
            }
            rowStart = rowMatcher.end();
        }

        buf.append("\r\n</table>");

        return cleanupHtml(buf.toString());
    }

    // let's not even bother with a table for this section!
    private static String cleanupMetaDataTable(String txt) {

        // this would be the pattern to extract the NAACCR ID if needed
        //Pattern p = Pattern.compile("<td>XML NAACCR ID:</td><td(.*?)>(.*?)</td>",Pattern.MULTILINE | Pattern.DOTALL);

        // gather the cells
        List<String> cells = new ArrayList<>();
        Matcher cellMatcher = Pattern.compile("(<td>|<td(.*?)\">)(.*?)</td>", Pattern.MULTILINE | Pattern.DOTALL).matcher(txt);
        int cellStart = 0;
        while (cellMatcher.find(cellStart)) {
            cells.add(cellMatcher.group(3));
            cellStart = cellMatcher.end();
        }
        // parse the alternate names
        List<String> altNames = Arrays.stream(cells.get(1).split("<br\\s?/>")).filter(s -> !StringUtils.isBlank(s)).collect(Collectors.toList());

        StringBuilder buf = new StringBuilder("<br/>");
        if (!StringUtils.isBlank(cells.get(5)) && !StringUtils.isBlank(cells.get(3)))
            buf.append("<strong>NAACCR XML</strong>: ").append(cells.get(5)).append(".").append(cells.get(3)).append("<br/>");
        buf.append("<br/>\r\n");
        if (!altNames.isEmpty()) {
            buf.append("<strong>Alternate Names</strong>\r\n");
            for (String altName : altNames)
                buf.append("<br/>&nbsp;&nbsp;&nbsp;").append(altName).append("\r\n");
        }

        return cleanupHtml(buf.toString());
    }
}
