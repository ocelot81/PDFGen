package org.pdfgen.Renderring;

import lombok.Setter;
import org.pdfgen.Facades.DateStringFacade;
import org.pdfgen.Utils.LocalDateOptions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.pdfgen.Utils.SettingsMapKeys;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DocumentRenderer {

    private PDPageContentStream contentStream;
    private PDDocument document;
    private final String fileName;
    private final PDType0Font fontNormal;
    private final PDType0Font fontHeader;
    private final int fontSize;
    private final int OYPadding;
    private final String languageLocale;

    @Setter private boolean logStateToConsole = false;
    @Setter private LinkedHashMap<String, Object> symbolReaderMap;

    private final HashMap<String, LocalDateOptions> datePreference = new HashMap<>() {};

    /**
     * Sets the preference of formatting of specific fields.
     * The first occurence of symbol %s will be formatted into the desired date.
     *
     * @param headerName Distinct header by which the text body is found
     * @param options Date creation options. (Arrangement, written_out, grammatical case)
     */
    public void setDatePreference(String headerName, LocalDateOptions options) {
        this.datePreference.put(headerName, options);
    }

    /**
     * Constructor of the class. Creates the required byte stream objects & initializes fonts and fileName via config.
     *
     * @param config user provided renderer and file configuration
     * @throws RuntimeException represents thrown IOExceptions that occured during renderring/IO operations.
     */
    public DocumentRenderer(DocumentRendererConfig config) {
        try {
            createContentStreamAndPage();
            this.fileName = config.fileName;
            this.fontHeader = config.getFontByDocument(this.document, config.fontNameHeader);
            this.fontNormal = config.getFontByDocument(this.document, config.fontNameNormal);
            this.fontSize = Integer.parseInt(config.getProperty(SettingsMapKeys.fontSize));
            this.OYPadding = Integer.parseInt(config.getProperty(SettingsMapKeys.fieldYPadding));
            this.languageLocale = config.getProperty("languageLocale");
        } catch (IOException e) {
            throw new RuntimeException("Exception during stream creation/font import: " + e);
        }
    }

    /**
     * Renders the symbol map of the class
     *
     * @apiNote Benchmark may be toggled with DocumentRenderer.setLogStateToConsole(true)
     * @throws IOException thrown IOException that occured during the stream reading/writing.
     */
    public void renderAll() throws IOException {
        if (this.logStateToConsole) { System.out.println("Opening stream & renderring.."); }
        Instant startingTime = Instant.now();

        float fieldShift = 0;
        this.contentStream.beginText();
        this.contentStream.newLineAtOffset(50, 750);

        for (Map.Entry<String, Object> fieldKv : this.symbolReaderMap.entrySet()) {
            String header = fieldKv.getKey();
            String body = fieldKv.getValue().toString();

            if (this.datePreference.containsKey(header)) {
                body = String.format(body, DateStringFacade.create(datePreference.get(header), this.languageLocale));
            }

            if (header.startsWith("__") && header.endsWith("__")) {
                fieldShift = renderField(this.contentStream, body, "", fieldShift);
                this.contentStream.newLineAtOffset(0, -5);
                continue;
            }
            fieldShift = renderField(this.contentStream, header + ": ", body, fieldShift);
        }

        this.contentStream.endText();
        this.contentStream.close();

        if (this.logStateToConsole) {
            System.out.printf("Successfully closed stream. Took: %s ms. %n", Duration.between(startingTime,Instant.now()).toMillis());
        }
    }

    /**
     * Saves the closed content stream to a PDF file
     *
     * @apiNote Log may be toggled with DocumentRenderer.setLogStateToConsole(true);
     * @throws IOException thrown IOException that occured during the stream reading/writing.
     */
    public void saveFile() throws IOException {
        this.document.save(this.fileName);
        if (this.logStateToConsole) { System.out.printf("Successfully saved file '%s'! %n", this.fileName); }
    }

    /**
     * Creates the required byte stream objects
     *
     * @throws IOException thrown IOException that occured during the stream reading/writing.
     */
    private void createContentStreamAndPage() throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        this.document = document;
        this.contentStream = contentStream;
    }
    /**
     * Writes a (header: body) field onto the page.
     *
     * @param contentStream the content stream.
     * @param header Distinct header part of the field, using its distinct font.
     * @param body Body of the field, using its set font.
     * @param fieldShift Current X-Axis shift to the right on the page during writing.
     * @throws IOException thrown IOException that occured during the stream reading/writing.
     */
    private float renderField(PDPageContentStream contentStream, String header, String body, float fieldShift) throws IOException {
        contentStream.newLineAtOffset(-fieldShift, -this.OYPadding);
        contentStream.setFont(this.fontHeader, this.fontSize);
        contentStream.showText(header);
        fieldShift = this.fontHeader.getStringWidth(header) / 1000 * this.fontSize;

        contentStream.newLineAtOffset(fieldShift, 0);
        contentStream.setFont(this.fontNormal, this.fontSize);
        contentStream.showText(body);
        return fieldShift;
    }

}
