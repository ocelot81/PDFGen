
package org.main;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import com.fasterxml.jackson.core.type.TypeReference;

//ctrl alt 0L - intellij format

public class Main {
    static void main() throws IOException {

        DocumentRendererConfig config = new DocumentRendererConfig();
        var ConfigTypeReference = new TypeReference<HashMap<String, String>>() {};

        config.setUserConfig(JsonUtils.ImportJSONAsType("userConfig.json", ConfigTypeReference));
        config.useFontOfName("arialbd.ttf", "arial.ttf");

        DocumentRenderer renderer = new DocumentRenderer(config);
        var RendererTypeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        renderer.setSymbolReaderMap(JsonUtils.ImportJSONAsType("defaultData.json", RendererTypeReference));


        renderer.setDatePreference("__Nagłówek__", new LocalDateOptions("MM-yyyy", "true", "d"));
        renderer.setDatePreference("Podstawa prawna", new LocalDateOptions("dd-MM-yyyy", "false", "m"));
        renderer.setDatePreference("Tytułem", new LocalDateOptions("MM-yyyy", "true", "m"));
        renderer.setDatePreference("Termin płatności", new LocalDateOptions("MM-yyyy", "true", "d"));

        renderer.setLogStateToConsole(true);
        renderer.renderAll();
        renderer.saveFile();

    }
}

