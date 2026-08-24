package org.main;

import lombok.Setter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

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

    @Setter private boolean logStateToConsole = false;
    @Setter private LinkedHashMap<String, Object> symbolReaderMap;

    private final Map<String, LocalDateOptions> datePreference = new HashMap<>() {};

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
        PDPageContentStream contentStream = this.contentStream;

        contentStream.beginText();
        contentStream.newLineAtOffset(50, 750);

        Map<String, LocalDateOptions> datePreference = this.datePreference;

        for (Map.Entry<String, Object> fieldKv : this.symbolReaderMap.entrySet()) {
            String header = fieldKv.getKey();
            String body = fieldKv.getValue().toString();

            if (datePreference.containsKey(header)) {
                body = String.format(body, LocalDateString.create(datePreference.get(header)));
            }

            if (header.startsWith("__") && header.endsWith("__")) {
                fieldShift = this.renderField(contentStream, body, "", fieldShift);
                contentStream.newLineAtOffset(0, -5);
                continue;
            }
            fieldShift = this.renderField(contentStream, header + ": ", body, fieldShift);
        }

        contentStream.endText();
        contentStream.close();

        if (this.logStateToConsole) {
            System.out.println("Successfully closed stream. Took: " + Duration.between(startingTime, Instant.now()).toMillis() + "ms");
        }
    }

    /**
     * Saves the closed content stream to a PDF file
     *
     * @apiNote Console notice may be toggled with DocumentRenderer.setLogStateToConsole(true);
     * @throws IOException thrown IOException that occured during the stream reading/writing.
     */
    public void saveFile() throws IOException {
        this.document.save(this.fileName);
        if (this.logStateToConsole) { System.out.println("Successfully saved file '" + this.fileName + "'!"); }
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
     * @param header Distinct header part of the field, using its setfont.
     * @param body Body of the field, using its set font.
     * @param fieldShift Current X-Axis shift to the right on the page during writing.
     * @throws IOException thrown IOException that occured during the stream reading/writing.
     */
    private float renderField(PDPageContentStream contentStream, String header, String body, float fieldShift) throws IOException {
        contentStream.newLineAtOffset(-fieldShift, -35);
        contentStream.setFont(this.fontHeader, 13);
        contentStream.showText(header);
        fieldShift = this.fontHeader.getStringWidth(header) / 1000 * 13;
        contentStream.newLineAtOffset(fieldShift, 0);
        contentStream.setFont(this.fontNormal, 13);
        contentStream.showText(body);
        return fieldShift;
    }

}
