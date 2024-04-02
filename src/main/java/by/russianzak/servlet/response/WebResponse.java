package by.russianzak.servlet.response;

import java.util.Date;

public class WebResponse {
  private final int status;
  private final String message;
  private final Date timestamp;

  public WebResponse(int status, String message) {
    this.status = status;
    this.message = message;
    this.timestamp = new Date();
  }
}