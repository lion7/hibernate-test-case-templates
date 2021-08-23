package org.hibernate.validator.bugs;

import org.hibernate.validator.testutil.TestForIssue;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.*;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import java.util.Arrays;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;

interface A {
}

interface B extends A {
}

interface C {
}

class X {
    @Valid
    @ConvertGroup(from = A.class, to = C.class)
    Y a2c = new Y();
    @Valid
    @ConvertGroup(from = B.class, to = C.class)
    Y b2c = new Y();
    @Valid
    @ConvertGroup.List({
            @ConvertGroup(from = A.class, to = C.class),
            @ConvertGroup(from = B.class, to = C.class),
    })
    Y both2c = new Y();
}

class Y {
    @NotNull(message = "A", groups = A.class)
    String a;
    @NotNull(message = "B", groups = B.class)
    String b;
    @NotNull(message = "C", groups = C.class)
    String c;
}

public class YourTestCase {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @TestForIssue(jiraKey = "HV-1540")
    public void testYourBug() {
        X yourEntity1 = new X();

        Set<ConstraintViolation<X>> constraintViolations = validator.validate(yourEntity1, B.class);
        Object[] actualPaths = constraintViolations
                .stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Path::toString)
                .sorted()
                .toArray();
        Object[] expectedPaths = {"a2c.a", "a2c.b", "b2c.c", "both2c.c"};

        System.out.println("Expected: " + Arrays.toString(expectedPaths));
        System.out.println("Actual: " + Arrays.toString(actualPaths));

        assertArrayEquals(actualPaths, expectedPaths);
    }

}
