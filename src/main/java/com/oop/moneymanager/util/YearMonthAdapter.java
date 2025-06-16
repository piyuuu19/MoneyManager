package com.oop.moneymanager.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

class YearMonthAdapter implements JsonSerializer<YearMonth>, JsonDeserializer<YearMonth> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
    @Override
    public JsonElement serialize(YearMonth src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(formatter.format(src));
    }
    @Override
    public YearMonth deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return YearMonth.parse(json.getAsString(), formatter);
    }
}
