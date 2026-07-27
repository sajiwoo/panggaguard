package dev.sajiwo.panggaguard.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.sajiwo.panggaguard.enumeration.Messages;
import dev.sajiwo.panggaguard.utilities.TraceContextAccessor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataResponse<T> {

  @JsonProperty("request_id")
  private String traceId;

  private boolean status;

  private Integer code;

  private ResponseMessage message;

  private T data;

  public DataResponse(T data) {
    this.data = data;
    this.traceId = TraceContextAccessor.getCurrentTraceId();
  }

  public String getTraceId() {
    if (this.traceId == null) {
      this.traceId = TraceContextAccessor.getCurrentTraceId();
    }
    return this.traceId;
  }

  @Data
  public static class ResponseMessage {

    private String title;

    @Setter(value = AccessLevel.NONE)
    private String description;

    public void setDescription(Messages message) {
      setDescription(message.toString());
    }

    public void setDescription(String description) {
      this.description = description;
    }

  }

}
