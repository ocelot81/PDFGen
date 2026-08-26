package org.main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;


public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Imports the contents of a JSON file to the desired type (such as a HashMap)
     *
     * @param fileName Filename of the JSON to be imported (with .json suffix)
     * @param typeReference The type to which the JSON will be represented as.
     * @throws RuntimeException Represents exceptions that may have occured during the reading of the file.
     */
    public static <T> T ImportJSONAsType(String fileName, TypeReference<T> typeReference) throws RuntimeException {
        try {
             return mapper.readValue(
                 new File(fileName), typeReference
             );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data from " + fileName + ": " + e);
        }
    }
}
