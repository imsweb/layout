package lab;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import com.imsweb.layout.TestingUtils;
import com.imsweb.naaccr.api.client.NaaccrApiClient;
import com.imsweb.naaccr.api.client.entity.NaaccrAllowedCode;
import com.imsweb.naaccr.api.client.entity.NaaccrDataItem;
import com.imsweb.seerutils.SeerUtils;

public class NaaccrDocGenerator {

    public static void main(String[] args) throws IOException, TemplateException {
        NaaccrApiClient client = NaaccrApiClient.getInstance();

        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        File outputDir = new File(TestingUtils.getWorkingDirectory() + "\\src\\main\\resources\\layout\\fixed\\naaccr\\doc\\naaccr25");

        for (NaaccrDataItem item : client.getDataItems("25")) {
            try (FileReader reader = new FileReader(System.getProperty("user.dir") + "/src/test/resources/doc/naaccr-doc-template.txt")) {

                Template template = new Template(item.getXmlNaaccrId(), reader, null);

                Map<String, Object> data = new HashMap<>();
                data.put("ITEM_LENGTH", Objects.toString(item.getItemLength(), ""));
                data.put("ITEM_NUMBER", Objects.toString(item.getItemNumber(), ""));
                data.put("SOURCE_OF_STANDARD", Objects.toString(item.getSourceOfStandard(), ""));
                data.put("YEAR_IMPLEMENTED", Objects.toString(item.getYearImplemented(), ""));
                data.put("VERSION_IMPLEMENTED", Objects.toString(item.getVersionImplemented(), ""));
                data.put("YEAR_RETIRED", Objects.toString(item.getYearRetired(), ""));
                data.put("VERSION_RETIRED", Objects.toString(item.getVersionRetired(), ""));
                data.put("DATA_LEVEL", Objects.toString(item.getXmlParentId(), ""));
                data.put("XML_ID", Objects.toString(item.getXmlNaaccrId(), ""));
                if (item.getAlternateNames() != null) {
                    List<String> cleanedNames = new ArrayList<>();
                    for (String name : item.getAlternateNames())
                        cleanedNames.add(cleanHtml(name));
                    data.put("ALTERNATE_NAMES", cleanedNames);
                }
                if (item.getDescription() != null)
                    data.put("DESCRIPTION", cleanHtml(renderer.render(parser.parse(item.getDescription()))));
                if (item.getRationale() != null)
                    data.put("RATIONALE", cleanHtml(renderer.render(parser.parse(item.getRationale()))));
                if (item.getClarification() != null)
                    data.put("CLARIFICATION", cleanHtml(renderer.render(parser.parse(item.getClarification()))));
                if (item.getGeneralNotes() != null)
                    data.put("GENERAL_NOTES", cleanHtml(renderer.render(parser.parse(item.getGeneralNotes()))));
                if (item.getCodeHeading() != null)
                    data.put("CODE_HEADING", cleanHtml(renderer.render(parser.parse(item.getCodeHeading()))));
                else
                    data.put("CODE_HEADING", "<strong>Codes</strong>");
                if (item.getAllowedCodes() != null) {
                    for (NaaccrAllowedCode code : item.getAllowedCodes()) {
                        if (code.getCode() != null)
                            code.setCode(cleanHtml(code.getCode()));
                        else
                            code.setCode("");
                        if (code.getDescription() != null)
                            code.setDescription(cleanHtml(code.getDescription()));
                    }
                    data.put("ALLOWED_CODES", item.getAllowedCodes());
                }
                if (item.getCodeNote() != null)
                    data.put("CODE_NOTE", cleanHtml(renderer.render(parser.parse(item.getCodeNote()))));

                String html;
                try (StringWriter writer = new StringWriter()) {
                    template.process(data, writer);
                    html = writer.toString();
                }

                // special cases
                switch (item.getItemNumber().toString()) {
                    case "145":
                        html = html.replace("<5%", "&lt;5%").replace("<10%", "&lt;10%").replace("<20%", "&lt;20%");
                        break;
                    case "170":
                    case "180":
                        html = html.replace("SEER < 1988", "SEER &lt; 1988");
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
                    case "2156":
                        html = html.replace(
                                "<span style=\"background-color: #ffffff;\">of</span><span style=\"background-color: #ffffff;\"> 08.XX.XX</span><span style=\"background-color: #ffffff;\"> d</span>uring",
                                "of 08.XX.XX during");
                        break;
                    case "2120":
                    case "2130":
                        html = html.replace("</>", "</i>");
                        break;
                    case "87":
                        // sigh, there shouldn't be any complex HTML table in the documentation, but there is still this one...
                        html = html.replace(
                                        "<table style=\"border-color:hsl(0, 0%, 0%);border-style:solid;\"><tbody><tr><td style=\"border:1.0pt solid windowtext;padding:0in 5.4pt;vertical-align:top;width:40.25pt;\">",
                                        "")
                                .replace(
                                        "</td><td style=\"border-bottom-style:solid;border-color:windowtext;border-left-style:none;border-right-style:solid;border-top-style:solid;border-width:1.0pt;padding:0in 5.4pt;vertical-align:top;width:355.25pt;\">",
                                        "<br/>")
                                .replace(
                                        "</td></tr><tr><td style=\"border-bottom-style:solid;border-color:windowtext;border-left-style:solid;border-right-style:solid;border-top-style:none;border-width:1.0pt;padding:0in 5.4pt;vertical-align:top;width:40.25pt;\">",
                                        "<br/><br/>\r\n")
                                .replace(
                                        "</td><td style=\"border-bottom:1.0pt solid windowtext;border-left-style:none;border-right:1.0pt solid windowtext;border-top-style:none;padding:0in 5.4pt;vertical-align:top;width:355.25pt;\">",
                                        "<br/>")
                                .replace("</td></tr></tbody></table>", "");
                        break;
                    default:
                        // ignored, nothing to do
                }

                // deal with tables
                int startTableIdx = html.indexOf("<p>|");
                while (startTableIdx >= 0) {
                    int endTableIdx = html.indexOf("|</p>") + 5;
                    String mdTable = html.substring(startTableIdx, endTableIdx);
                    html = new StringBuilder(html).replace(startTableIdx, endTableIdx, convertTable(mdTable)).toString();
                    startTableIdx = html.indexOf("<p>|", endTableIdx);
                }

                String filename = item.getXmlNaaccrId() != null ? item.getXmlNaaccrId() : item.getItemNumber().toString();
                SeerUtils.writeFile(html, new File(outputDir, filename + ".html"), StandardCharsets.UTF_8.toString());
            }
        }
    }

    private static String cleanHtml(String html) {

        html = html.replace("<br>", "<br/>");

        html = html.replaceAll("<script.+?</script>", "");
        html = html.replaceAll("</em>\\^(\\d+)\\^", "</em>&nbsp;&nbsp;<sup>$1</sup>");
        html = html.replaceAll("\\^(\\d+)\\^", "&nbsp;<sup>$1</sup>");

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

    private static String convertTable(String html) {
        html = Pattern.compile("^<p>|</p>$").matcher(html).replaceAll("");

        StringBuilder buf = new StringBuilder("<table>\r\n");
        for (String line : html.split("\r?\n")) {
            if (line.matches("^[|\\- ]+$"))
                continue;
            buf.append("  <tr>");
            for (String part : StringUtils.split(line, "|"))
                buf.append("    <td>").append(part).append("</td>");
            buf.append("  </tr>\r\n");
        }
        buf.append("</table>\r\n");

        return buf.toString();
    }

}
