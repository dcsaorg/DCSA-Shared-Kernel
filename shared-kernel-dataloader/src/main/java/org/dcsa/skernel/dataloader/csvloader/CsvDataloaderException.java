package org.dcsa.skernel.dataloader.csvloader;

import org.dcsa.skernel.dataloader.DataloaderException;

public class CsvDataloaderException extends DataloaderException {
  public CsvDataloaderException(String message, Exception cause) {
    super(message, cause);
  }
}
