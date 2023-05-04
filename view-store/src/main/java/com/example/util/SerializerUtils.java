package com.example.util;

import com.example.events.Event;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import java.io.IOException;
import java.util.HashMap;

public class SerializerUtils {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new ParameterNamesModule())
            .addModule(new Jdk8Module())
            .addModule(new JavaTimeModule())
            .build();

    private SerializerUtils() {
    }

    public static <T> T deserializeFromJsonBytes(final byte[] jsonBytes, final Class<T> valueType) {
        try {
            return objectMapper.readValue(jsonBytes, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Event[] deserializeEventsFromJsonBytes(final byte[] jsonBytes) {
        try {
            return objectMapper.readValue(jsonBytes, Event[].class);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static HashMap<String, byte[]> deserializeEventsMetadata(final byte[] metaData) {
        final var tr = new TypeReference<HashMap<String, byte[]>>() {
        };
        try {
            return objectMapper.readValue(metaData, tr);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
