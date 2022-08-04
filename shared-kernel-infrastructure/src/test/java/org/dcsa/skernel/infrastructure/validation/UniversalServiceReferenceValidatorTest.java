package org.dcsa.skernel.infrastructure.validation;

import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

@ExtendWith(MockitoExtension.class)
public class UniversalServiceReferenceValidatorTest {

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Test
    public void testValidInput() {
      Assertions.assertTrue(validate(TestEntity.of("SR00001D")));
      Assertions.assertTrue(validate(TestEntity.of("SR00002B")));
      Assertions.assertTrue(validate(TestEntity.of("SR00003H")));
      Assertions.assertTrue(validate(TestEntity.of("SR14501A")));

      // null is also valid (combine with @NonNull/@NotBlank to avoid that)
      Assertions.assertTrue(validate(TestEntity.of(null)));
    }

    @Test
    public void testValidInvalidInput() {
        // Invalid (checksum is off)
      Assertions.assertFalse(validate(TestEntity.of("SR00001E")));
      Assertions.assertFalse(validate(TestEntity.of("SR00002A")));
      Assertions.assertFalse(validate(TestEntity.of("SR40501A")));
      // Length issue
      Assertions.assertFalse(validate(TestEntity.of("SR000002B")));
      // Not quite a number (but would fool Integer.parseInt)
      Assertions.assertFalse(validate(TestEntity.of("SR-0001D")));
    }

  private boolean validate(TestEntity container) {
    UniversalServiceReferenceValidator imoValidator = new UniversalServiceReferenceValidator();
    String value = null;
    if (container != null) {
      UniversalServiceReference anno = container.getClass().getAnnotation(UniversalServiceReference.class);
      imoValidator.initialize(anno);
      value = container.getUniversalServiceReference();
    }
    return imoValidator.isValid(value, constraintValidatorContext);
  }

    // Put the annotation on the container (it will apply to everything, and it is easier to get the annotation)
    @UniversalServiceReference
    @Data(staticConstructor = "of")
    private static class TestEntity {
        private final String universalServiceReference;
    }

}
