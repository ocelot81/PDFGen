package org.pdfgen.renderring;

import lombok.Setter;
import org.pdfgen.factory.DateStringFactory;
import org.pdfgen.utils.LocalDateOptions;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DocumentRenderer {

    private PDPageContentStream contentStream;
    private PDDocument document;
    private final PDType0Font fontNormal;
    private final PDType0Font fontHeader;
    private final DocumentRendererConfig config;
    private final int fontSize;
    private final int fieldYPadding;
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
     * @param userSettings user provided renderer and file configuration
     * @throws RuntimeException represents thrown IOExceptions that occured during renderring/IO operations.
     */
    public DocumentRenderer(DocumentRendererConfig userSettings) {
        try {
            createContentStreamAndPage();
            this.fontHeader = userSettings.getFontByDocument(this.document, userSettings.fontNameHeader);
            this.fontNormal = userSettings.getFontByDocument(this.document, userSettings.fontNameNormal);
            this.fontSize = Integer.parseInt(userSettings.fontSize);
            this.fieldYPadding = Integer.parseInt(userSettings.fieldYPadding);
            this.config = userSettings;
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
                String dateToAdd = DateStringFactory.create(datePreference.get(header), config.languageLocale);
                body = String.format(body, dateToAdd);
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
        this.document.save(config.outputDirectory + File.separator + config.fileName);
        if (this.logStateToConsole) { System.out.printf("Successfully saved file '%s'! %n", config.fileName); }
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
        contentStream.newLineAtOffset(-fieldShift, -this.fieldYPadding);
        contentStream.setFont(this.fontHeader, fontSize);
        contentStream.showText(header);
        fieldShift = this.fontHeader.getStringWidth(header) / 1000 * this.fontSize;
        contentStream.newLineAtOffset(fieldShift, 0);
        contentStream.setFont(this.fontNormal, this.fontSize);
        contentStream.showText(body);
        return fieldShift;
    }

}
