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

        File outputDir = new File(TestingUtils.getWorkingDirectory() + "\\src\\main\\resources\\layout\\fixed\\naaccr\\doc\\naaccr24");

        for (String id : List.of("race1", "nameLast", "primarySite", "classOfCase", "ajccCancerSurvApiVersionCurrent")) {
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
                    for (NaaccrAllowedCode code : item.getAllowedCodes())
                        if (code.getCode() == null)
                            code.setCode("");
                    data.put("ALLOWED_CODES", item.getAllowedCodes());
                }
                if (item.getCodeNote() != null)
                    data.put("CODE_NOTE", cleanHtml(renderer.render(parser.parse(item.getCodeNote()))));

                template.process(data, writer);
            }
        }

        // TODO FD deal with replacing special characters in HTML... (see race1 for an example of a quote)
    }

    private static String cleanHtml(String html) {

        html = Pattern.compile("</em>\\^(\\d+)\\^").matcher(html).replaceAll("</em>&nbsp;&nbsp;<sup>$1</sup>");
        html = Pattern.compile("\\^(\\d+)\\^").matcher(html).replaceAll("&nbsp;<sup>$1</sup>");

        return html;
    }

}
