package org.pdfgen.Renderring;

import org.pdfgen.Facades.DateStringFacade;
import org.pdfgen.Utils.*;
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
    private final String defaultFontDirectory = "defaultFonts";

    private String activeFontDirectory = defaultFontDirectory;
    private Map<String, String> settingsMap;

    private static final Map<String, String> defaultConfig = new HashMap<>() {{
        put(SettingsMapKeys.fileName, "Unnamed file");
        put(SettingsMapKeys.customFontDirectory, "");
        put(SettingsMapKeys.fontSize, "14");
        put(SettingsMapKeys.fontNameNormal, "arial.ttf");
        put(SettingsMapKeys.fontNameHeader, "arialbd.ttf");
        put(SettingsMapKeys.fieldYPadding, "35");
        put(SettingsMapKeys.attachSignature, "false");
        put(SettingsMapKeys.appendDateToFilename, "true");
        put(SettingsMapKeys.dateFormatting, "MM-yyyy");
        put(SettingsMapKeys.dateSpelledOut, "false");
        put(SettingsMapKeys.dateGrammaCase, "m");
        put(SettingsMapKeys.languageLocale, "PL");
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
        String customFontDirectory = this.settingsMap.get(SettingsMapKeys.customFontDirectory);

        if (!customFontDirectory.isBlank()) {
            if (!isAbsolutePath(customFontDirectory)) {
                System.out.printf("Path '%s' isnt absolute! Using defaults... %n", customFontDirectory);
                return;
            }
            this.activeFontDirectory = customFontDirectory;
        } else if (!Files.exists(Paths.get(defaultFontDirectory))) {
            throw new IllegalStateException(String.format("Default or custom font directory doesnt exist! Folder '%s' missing from project path?", defaultFontDirectory));
        }
    }
    /**
     * Creates the file name per provided settings file
     */
    private void setFileName() {
        var settingsMap = this.settingsMap;
        String fileName = settingsMap.get(SettingsMapKeys.fileName);

        if (Boolean.parseBoolean(settingsMap.get(SettingsMapKeys.appendDateToFilename))) {

            LocalDateOptions dateConfig = new LocalDateOptions(
                    settingsMap.get(SettingsMapKeys.dateFormatting),
                    settingsMap.get(SettingsMapKeys.dateSpelledOut),
                    settingsMap.get(SettingsMapKeys.dateGrammaCase)
            );

            String appendedDate = DateStringFacade.create(dateConfig, this.getProperty(SettingsMapKeys.languageLocale));

            fileName = fileName + " "+ appendedDate;
        }

        this.fileName = fileName + ".pdf";
    }

    /**
     * Sanitizes and implements user configuration
     *
     * @param settingsMap User provided map of settings from the project JSON file.
     */
    public void setUserConfig(Map<String, String> settingsMap) {
        // Provided settingsMap contain a setting unlisted in defaults above.
        Set<String> KeySet = settingsMap.keySet();
        Set<String> unsupportedSettings = KeySet.stream()
                .filter(Setting -> !defaultConfig.containsKey(Setting))
                .collect(Collectors.toSet());

        KeySet.removeAll(unsupportedSettings);
        unsupportedSettings.forEach(Setting -> System.out.printf("Unsupported setting in userConfig.json: '%s' Ignoring.. %n", Setting));

        // Provided settingsMap doesn't contain a setting listed in the defaults, or provides a blank string while default isn't blank.
        Set<String> missingSettings = defaultConfig.keySet().stream()
                .filter(Setting -> isDefaultMissing(settingsMap.get(Setting), defaultConfig.get(Setting)))
                .collect(Collectors.toSet());

        // Supply missing settings to the settingsMap local.
        missingSettings.forEach(setting -> {
            settingsMap.put(setting, defaultConfig.get(setting));
            System.out.printf("Missing setting in userConfig.json: '%s' Using defaults.. %n", setting);
        });

        this.settingsMap = settingsMap;
        this.useFontOfName(settingsMap.get(SettingsMapKeys.fontNameHeader), settingsMap.get(SettingsMapKeys.fontNameNormal));
        this.setFileName();
        this.setActiveFontDirectory();
    }

    /**
     * Null check & empty string check
     *
     * @param userProvided value provided by the user
     * @param defaultValue reference hardcoded value
     */
    private boolean isDefaultMissing(String userProvided, String defaultValue) {
        return (userProvided == null) || (userProvided.isBlank() && !defaultValue.isBlank());
    }

    /**
     * Checks whether the path is absolute (starts with root/drive)
     *
     * @param userProvidedPath Path provided by the user
     * @throws InvalidPathException String cannot be converted to class Path
     */
    private boolean isAbsolutePath(String userProvidedPath) throws InvalidPathException {
        return Paths.get(userProvidedPath).isAbsolute();
    }

    /**
     * Fetches a setting in the internal settingsMap
     *
     * @param setting key representing the setting of the configuration
     * @throws NullPointerException key cannot be found in the settingsMap
     */
    public String getProperty(String setting) {
        return this.settingsMap.get(setting);
    }

    /**
     * Retrieves the PDType0Font with previously provided file name and directory path.
     *
     * @param document     PDDocument by pdfBox
     * @param fontFileName .ttf file (requries extension suffix)
     * @throws IOException           exception during file reading
     * @throws IllegalStateException file unreachable/empty in size
     */
    public PDType0Font getFontByDocument(PDDocument document, String fontFileName) throws IOException {
        String activeFontDirectory = this.activeFontDirectory;
        Path activePath = Paths.get(activeFontDirectory + "/" + fontFileName);

        if (!Files.exists(activePath) || Files.size(activePath) == 0) {
            throw new IllegalStateException(String.format("File '%s' doesnt exist in directory '%s' or the file is empty.", fontFileName, activeFontDirectory));
        }

        return PDType0Font.load(document, new File(activeFontDirectory + "/" + fontFileName));
    }
}
