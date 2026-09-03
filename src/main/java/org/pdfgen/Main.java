package org.pdfgen;

import java.util.HashMap;
import java.util.LinkedHashMap;
import com.fasterxml.jackson.core.type.TypeReference;
import org.pdfgen.renderring.DocumentRenderer;
import org.pdfgen.renderring.DocumentRendererConfig;
import org.pdfgen.signature.DigitalSigner;
import org.pdfgen.utils.JsonUtils;
import org.pdfgen.utils.LocalDateOptions;
import static org.pdfgen.utils.LocalDateOptions.*;

public class Main {

    static void main() throws Exception {

        DocumentRendererConfig config = new DocumentRendererConfig();
        var ConfigTypeReference = new TypeReference<HashMap<String, String>>() {};
        config.setUserConfig(JsonUtils.ImportJSONAsType("src/main/resources/userConfig.json", ConfigTypeReference));

        DocumentRenderer renderer = new DocumentRenderer(config);
        var RendererTypeReference = new TypeReference<LinkedHashMap<String, Object>>() {};
        renderer.setSymbolReaderMap(JsonUtils.ImportJSONAsType("src/main/resources/defaultData.json", RendererTypeReference));

        renderer.setDatePreference("__Nagłówek__", new LocalDateOptions(DATE_FORMAT_DAY_MONTH_YEAR, "true", "d"));
        renderer.setDatePreference("Testing 1", new LocalDateOptions(DATE_FORMAT_DAY_MONTH_YEAR, "false", "m"));
        renderer.setDatePreference("Testing 2", new LocalDateOptions(DATE_FORMAT_MONTH_YEAR, "true", "m"));
        renderer.setDatePreference("Testing 3", new LocalDateOptions(DATE_FORMAT_YEAR, "true", "d"));

        renderer.setLogStateToConsole(true);
        renderer.renderAll();
        renderer.saveFile();

        DigitalSigner signer = new DigitalSigner(config.fullSavePath, "path", "mykey");

        signer.addSignatureToDocument();

    }
}

