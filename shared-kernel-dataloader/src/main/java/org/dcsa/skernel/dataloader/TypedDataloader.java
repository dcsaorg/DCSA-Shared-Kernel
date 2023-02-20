package org.dcsa.skernel.dataloader;

import org.dcsa.skernel.dataloader.DataloaderConfig.DataloaderSource;

public interface TypedDataloader {
  public void loadData(DataloaderSource source);
}
