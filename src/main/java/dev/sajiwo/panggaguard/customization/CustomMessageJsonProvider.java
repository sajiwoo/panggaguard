package dev.sajiwo.panggaguard.customization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.JsonWritingUtils;

/**
 * <p>
 * This class is an custom message json provider for logback-logstash-encoder to
 * support pretty-print JSON message output.
 * </p>
 * 
 * The usage (enable/disable) is configured via logback.xml file or you
 * respective logging framework configuration.
 * 
 */
@Slf4j
public class CustomMessageJsonProvider extends AbstractFieldJsonProvider<ILoggingEvent> {

  private static ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule())
      .enable(SerializationFeature.INDENT_OUTPUT)
      .build();

  @Override
  public void writeTo(JsonGenerator jsonGenerator, ILoggingEvent event) throws IOException {
    if (!StringUtils.hasText(event.getFormattedMessage()) || !event.getFormattedMessage().trim().startsWith("{")) {
      try {
        JsonWritingUtils.writeStringField(jsonGenerator, "message", event.getFormattedMessage());
      } catch (IOException e) {
        log.error("Failed to write logging due the IOException, Message {}", e.getMessage());
      } catch (Exception e) {
        log.error("Failed to write logging due the Exception, Message {}", e.getMessage());
      }
      return;
    }

    Map<String, Object> result = new HashMap<>();
    Map<String, Object> warnings = new HashMap<>();

    try {
      TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() {
      };

      Map<String, Object> message = objectMapper.readValue(event.getFormattedMessage(), typeReference);

      for (var field : message.entrySet()) {
        Object value = field.getValue();
        if (value != null && value instanceof String && value.toString().trim().startsWith("{")) {
          try {
            message.put(field.getKey(), objectMapper.readValue(value.toString(), typeReference));
          } catch (JacksonException e) {
          }
        }
      }

      result.put("http_data", message);

    } catch (JsonProcessingException e) {
      result.put("http_data", event.getFormattedMessage());
    } catch (Exception e) {
      warnings.put(e.getClass().getSimpleName(), e.getMessage());
      result.put("http_data", event.getFormattedMessage());
    } finally {
      result.put("warning", warnings);
      JsonWritingUtils.writeMapEntries(jsonGenerator, result);
    }
  }

}
