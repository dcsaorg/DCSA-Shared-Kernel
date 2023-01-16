package org.dcsa.skernel.infrastructure.validation;

import jakarta.validation.Payload;

public @interface ISO6346EquipmentReference {

  String message() default "must be a valid ISO-6346 equipment reference";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
