package org.dcsa.skernel.dataloader;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class DataloaderController {
  private final Dataloader dataloader;

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("${dcsa.dataloader.endpoint}/{groups}")
  public void loadReferenceData(@PathVariable("groups") String groups) {
    dataloader.loadData(Arrays.stream(groups.split(",")).collect(Collectors.toSet()));
  }
}
