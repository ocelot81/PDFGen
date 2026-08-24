package org.main;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;


public class DocumentRendererConfig {

    public String fileName;
    public String fontNameNormal;
    public String fontNameHeader;

    private String activeFontDirectory = "defaultFonts";
    private HashMap<String, String> settingsMap;

    private static final Map<String, String> defaultConfig = new HashMap<>() {{
        put("fileName", "Unnamed file");
        put("appendDateToFileName", "true");
        put("customFontDirectory", "");
        put("appendedDateFormatting", "MM-yyyy");
        put("appendedDateWrittenOut", "false");
        put("appendedDateGrammaCase", "m");
    }};

    /**
     * Supplements the name of the .ttf file queried to the default/custom directory
     *
     * @param headerFont Bold/Highlighted font for the header of the field.
     * @param normalFont Normal font for the body of the field
     */
    public void useFontOfName(String headerFont, String normalFont) {
        this.fontNameHeader = headerFont;
        this.fontNameNormal = normalFont;
    }

    /**
     * Sets the directory through which font files are queried
     *
     * @throws IllegalStateException Font directory unreachable (default 'defaultFonts' and user provided one)
     */
    private void setActiveFontDirectory() {
        String customFontDirectory = this.settingsMap.get("customFontDirectory");

        if (!customFontDirectory.isBlank()) {
            if (!Paths.get(customFontDirectory).isAbsolute()) {
                Console.Warning("Path '" + customFontDirectory + "' isnt absolute! Using defaults...");
                return;
            }
            this.activeFontDirectory = customFontDirectory;
        } else if (!Files.exists(Paths.get("defaultFonts"))) {
            throw new IllegalStateException("Default or custom font directory doesnt exist! Folder 'defaultFonts' missing from project path?");
        }
    }

    /**
     * Creates the file name per provided settings file
     */
    private void setFileName() {
        var settingsMap = this.settingsMap;
        String fileName = settingsMap.get("fileName");

        if (JsonUtils.booleanizeString(settingsMap.get("appendDateToFileName"))) {
            fileName = fileName + " " +
                    LocalDateString.create(new LocalDateOptions(
                            settingsMap.get("appendedDateFormatting"),
                            settingsMap.get("appendedDateWrittenOut"),
                            settingsMap.get("appendedDateGrammaCase")));
        }
        this.fileName = fileName + ".pdf";
    }

    /**
     * Sanitizes and implements user configuration
     *
     * @param settingsMap User provided map of settings from the project JSON file.
     */
    public void setUserConfig(HashMap<String, String> settingsMap) {
        // Provided settingsMap contain a setting unlisted in defaults above.
        Set<String> KeySet = settingsMap.keySet();
        Set<String> unsupportedSettings = KeySet.stream()
                .filter(Setting -> !defaultConfig.containsKey(Setting))
                .collect(Collectors.toSet());

        KeySet.removeAll(unsupportedSettings);
        unsupportedSettings.forEach(Setting -> Console.Warning("Unsupported setting in userConfig.json: '" + Setting + "' Ignoring.."));

        // Provided settingsMap doesn't contain a setting listed in the defaults, or provides a blank string while default isn't blank.
        Set<String> missingSettings = defaultConfig.keySet().stream()
                .filter(Setting -> isMissing(settingsMap.get(Setting), defaultConfig.get(Setting)))
                .collect(Collectors.toSet());

        // Supply missing settings to the settingsMap local.
        missingSettings.forEach(setting -> {
            settingsMap.put(setting, defaultConfig.get(setting));
            Console.Warning("Missing setting in userConfig.json: '" + setting + "' Using defaults..");
        });

        this.settingsMap = settingsMap;

        setFileName();
        setActiveFontDirectory();
    }

    /**
     * Null check & empty string check
     *
     * @param userProvided value provided by the user
     * @param defaultValue reference hardcoded value
     */
    private boolean isMissing(String userProvided, String defaultValue) {
        return userProvided == null || (userProvided.isBlank() && !defaultValue.isBlank());
    }

    /**
     * Retrieves the PDType0Font with previously provided file name and directory path.
     *
     * @param document PDDocument by pdfBox
     * @param fontFileName .ttf file (requries extension suffix)
     * @throws IOException exception during file reading
     * @throws IllegalStateException file unreachable/empty in size
     */
    public PDType0Font getFontByDocument(PDDocument document, String fontFileName) throws IOException {
        String activeFontDirectory = this.activeFontDirectory;
        Path activePath = Paths.get(activeFontDirectory + "/" + fontFileName);

        if (!Files.exists(activePath)) {
            throw new IllegalStateException("File '" + fontFileName + "' doesnt exist in directory '" + activeFontDirectory);
        }
        if (Files.size(activePath) == 0) {
            throw new IllegalStateException("File '" + fontFileName + "' exists in directory'" + activeFontDirectory + "but is empty.");
        }

        return PDType0Font.load(document, new File(activeFontDirectory + "/" + fontFileName));
    }
}
