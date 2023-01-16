package org.dcsa.skernel.infrastructure.validation;

import jakarta.validation.ConstraintValidatorContext;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ISO6346EquipmentReferenceValidatorTest {

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @Test
    void testValidInput() {
        // Actual container numbers
        Assertions.assertTrue(validate(TestEntity.of("GVTU3000389")));
        Assertions.assertTrue(validate(TestEntity.of("CSQU3054383")));
        Assertions.assertTrue(validate(TestEntity.of("TOLU4734787")));
        Assertions.assertTrue(validate(TestEntity.of("APZU4812090")));  // From swagger

        // Special-case
        Assertions.assertTrue(validate(TestEntity.of(null)));
    }

    @Test
    void testValidInvalidInput() {
        // Invalid (checksum is off)
        Assertions.assertFalse(validate(TestEntity.of("GVTU3000388")));
        Assertions.assertFalse(validate(TestEntity.of("GVTU3000380")));

        // Length issues
        Assertions.assertFalse(validate(TestEntity.of("GVTU300038")));
        Assertions.assertFalse(validate(TestEntity.of("GVTU30003891")));

        // Invalid equipment type (the checksum is probably also off)
        Assertions.assertFalse(validate(TestEntity.of("GVTZ3000380")));

        // Invalid equipment type
        Assertions.assertFalse(validate(TestEntity.of("GVTZ3000380")));

    }

    private boolean validate(TestEntity container) {
        var validator = new ISO6346EquipmentReferenceValidator();
        return validator.isValid(container.getEquipmentReference(), constraintValidatorContext);
    }

    // Put the annotation on the container (it will apply to everything, and it is easier to get the annotation)
    @ISO6346EquipmentReference
    @Data(staticConstructor = "of")
    private static class TestEntity {
        private final String equipmentReference;
    }
}
