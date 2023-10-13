package lab;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

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

        //        String test = "This is a   \n  \ntest...";
        //        System.out.println(test);
        //        System.out.println(renderer.render(parser.parse(test)));
        //        System.out.println(cleanHtml(renderer.render(parser.parse(test))));

        File outputDir = new File(TestingUtils.getWorkingDirectory() + "\\src\\main\\resources\\layout\\fixed\\naaccr\\doc\\naaccr24");

        for (String id : List.of("race1", "nameLast", "primarySite", "classOfCase")) {
            try (FileReader reader = new FileReader(System.getProperty("user.dir") + "/src/test/resources/doc/naaccr-doc-template.txt");
                 FileWriter writer = new FileWriter(new File(outputDir, id + ".html"))) { // TODO FD deal with retired items

                NaaccrDataItem item = client.getDataItem("24", id);

                Template template = new Template(item.getXmlNaaccrId(), reader, null);

                Map<String, Object> data = new HashMap<>();
                data.put("ITEM_LENGTH", item.getItemLength());
                data.put("ITEM_NUMBER", item.getItemNumber());
                data.put("SOURCE_OF_STANDARD", Objects.toString(item.getSourceOfStandard(), ""));
                data.put("YEAR_IMPLEMENTED", Objects.toString(item.getYearImplemented(), ""));
                data.put("VERSION_IMPLEMENTED", Objects.toString(item.getVersionImplemented(), ""));
                data.put("YEAR_RETIRED", Objects.toString(item.getYearRetired(), ""));
                data.put("VERSION_RETIRED", Objects.toString(item.getVersionRetired(), ""));
                data.put("DATA_LEVEL", item.getXmlParentId());
                data.put("XML_ID", item.getXmlNaaccrId());
                data.put("ALTERNATE_NAMES", item.getAlternateNames());
                data.put("DESCRIPTION", cleanHtml(renderer.render(parser.parse(item.getDescription()))));
                if (item.getRationale() != null)
                    data.put("RATIONALE", cleanHtml(renderer.render(parser.parse(item.getRationale()))));
                if (item.getAllowedCodes() != null) {
                    for (NaaccrAllowedCode code : item.getAllowedCodes()) {
                        if (code.getCode() == null)
                            code.setCode("");
                        if (code.getDescription() == null)
                            code.setDescription("");
                    }
                    data.put("ALLOWED_CODES", item.getAllowedCodes());
                }

                template.process(data, writer);
            }
        }

        //        // TODO FD deal with replacing special characters in HTML... (see race1 for an example of a quote)

        //        StringBuilder buf = new StringBuilder();
        //        buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
        //        buf.append("\n");
        //        buf.append("<html>\n");
        //        buf.append("\n");
        //        buf.append("<head>\n");
        //        buf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n");
        //        buf.append("<title>").append(item.getItemName().replace("&", "&amp;")).append("</title>\n");
        //        buf.append("<style>\n");
        //        buf.append("body { padding:5px; font-family:Tahoma; font-size: 14px; }\n");
        //        buf.append("h1 { font-size:14px; margin-top:0px; }\n");
        //        buf.append(style);
        //        buf.append("</style>\n");
        //        buf.append("</head>\n");
        //        buf.append("\n");
        //        buf.append("<body>\n");
        //        buf.append("\n");
        //        buf.append("<h1>").append(item.getItemName().replace("&", "&amp;")).append("</h1>\n");
        //        buf.append("\n");
        //        buf.append(content);
        //        buf.append("</body>\n");
        //        buf.append("</html>\n");
        //
        //        SeerUtils.writeFile(buf.toString(), new File(System.getProperty("user.dir") + "/build/test.html"));
    }

    private static String cleanHtml(String html) {
        //html = Pattern.compile("^<p>").matcher(html).replaceAll("");
        //html = Pattern.compile("</p>$").matcher(html).replaceAll("");

        html = Pattern.compile("\\^(\\d+)\\^").matcher(html).replaceAll("<sup>$1</sup>");

        return html;
    }

}
