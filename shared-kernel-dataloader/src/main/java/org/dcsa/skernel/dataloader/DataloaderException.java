package org.dcsa.skernel.dataloader;

public class DataloaderException extends RuntimeException {
  public DataloaderException(String message) {
    super(message);
  }
  public DataloaderException(String message, Exception cause) {
    super(message, cause);
  }
}
