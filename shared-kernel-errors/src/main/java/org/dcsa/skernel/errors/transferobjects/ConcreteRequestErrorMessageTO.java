package org.dcsa.skernel.errors.transferobjects;

import lombok.Value;

@Value
public class ConcreteRequestErrorMessageTO {
    private final String reason;
    private final String message;
}
