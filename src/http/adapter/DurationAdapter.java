package http.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Duration;

// Адаптер для преобразования Duration в JSON и обратно.
// В JSON будет храниться продолжительность как количество минут.
public class DurationAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {

    // Преобразование объекта Duration в JSON
    @Override
    public JsonElement serialize(Duration duration, Type typeOfSrc, JsonSerializationContext context) {
        if (duration == null) {
            return null;
        }

        return new JsonPrimitive(duration.toMinutes());
    }

    // Преобразование JSON обратно в объект Duration
    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        return Duration.ofMinutes(json.getAsLong());
    }
}