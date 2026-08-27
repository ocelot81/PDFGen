
package org.pdfgen;
import java.util.HashMap;
import java.util.LinkedHashMap;
import com.fasterxml.jackson.core.type.TypeReference;
import org.pdfgen.Renderring.DocumentRenderer;
import org.pdfgen.Renderring.DocumentRendererConfig;
import org.pdfgen.Utils.JsonUtils;
import org.pdfgen.Utils.LocalDateOptions;
import static org.pdfgen.Utils.LocalDateOptions.DATE_FORMAT_DAY_MONTH_YEAR;
import static org.pdfgen.Utils.LocalDateOptions.DATE_FORMAT_MONTH_YEAR;

public class Main {

    static void main() throws Exception {

        DocumentRendererConfig config = new DocumentRendererConfig();
        var ConfigTypeReference = new TypeReference<HashMap<String, String>>() {};
        config.setUserConfig(JsonUtils.ImportJSONAsType("src/main/resources/userConfig.json", ConfigTypeReference));

        DocumentRenderer renderer = new DocumentRenderer(config);
        var RendererTypeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        renderer.setSymbolReaderMap(JsonUtils.ImportJSONAsType("src/main/resources/defaultData.json", RendererTypeReference));

        renderer.setDatePreference("__Nagłówek__", new LocalDateOptions(DATE_FORMAT_DAY_MONTH_YEAR, "true", "d"));
        renderer.setDatePreference("Podstawa prawna", new LocalDateOptions(DATE_FORMAT_DAY_MONTH_YEAR, "false", "m"));
        renderer.setDatePreference("Tytułem", new LocalDateOptions(DATE_FORMAT_MONTH_YEAR, "true", "m"));
        renderer.setDatePreference("Termin płatności", new LocalDateOptions(DATE_FORMAT_MONTH_YEAR, "true", "d"));

        renderer.setLogStateToConsole(true);
        renderer.renderAll();
        renderer.saveFile();

    }
}

