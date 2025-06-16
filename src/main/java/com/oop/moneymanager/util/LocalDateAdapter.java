package com.oop.moneymanager.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(formatter.format(src));
    }
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return LocalDate.parse(json.getAsString(), formatter);
    }
}