package org.dcsa.skernel.transferobjects;

import lombok.Data;

@Data
public class ConcreteRequestErrorMessageTO {

    private final String reason;
    private final String message;
}
