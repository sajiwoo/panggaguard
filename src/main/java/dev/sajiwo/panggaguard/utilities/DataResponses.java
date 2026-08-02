package dev.sajiwo.panggaguard.utilities;

import org.springframework.http.HttpStatus;

import dev.sajiwo.panggaguard.dto.response.DataResponse;
import dev.sajiwo.panggaguard.dto.response.DataResponse.ResponseMessage;
import dev.sajiwo.panggaguard.enumeration.Messages;

public abstract class DataResponses {

  public static <T> DataResponse<T> created(T data) {
    return success(HttpStatus.CREATED, data);
  }

  public static DataResponse<Void> ok() {
    return success(HttpStatus.OK, null);
  }

  public static <T> DataResponse<T> ok(T data) {
    return success(HttpStatus.OK, data);
  }

  public static <T> DataResponse<T> badRequest(String messsage) {
    return error(HttpStatus.BAD_REQUEST, Messages.FAILED, messsage);
  }

  public static <T> DataResponse<T> internalServerError() {
    return error(HttpStatus.INTERNAL_SERVER_ERROR, Messages.FAILED, Messages.INTERNAL_SERVER_ERROR);
  }

  public static <T> DataResponse<T> success(HttpStatus code, T data) {
    ResponseMessage message = new ResponseMessage();
    message.setTitle(Messages.SUCCESS);
    message.setDescription(Messages.SUCCESS);

    DataResponse<T> result = new DataResponse<>();
    result.setTraceId(TraceContextAccessor.getCurrentTraceId());
    result.setStatus(true);
    result.setCode(code.value());
    result.setMessage(message);
    result.setData(data);

    return result;
  }

  public static <T> DataResponse<T> error(HttpStatus code, String title, String description) {
    ResponseMessage message = new ResponseMessage();
    message.setTitle(title);
    message.setDescription(description);

    DataResponse<T> result = new DataResponse<>();
    result.setTraceId(TraceContextAccessor.getCurrentTraceId());
    result.setStatus(false);
    result.setCode(code.value());
    result.setMessage(message);

    return result;
  }
}
