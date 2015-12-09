package com.moozvine.detox;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Declares that the tagged member forms part of the identity of the object.
 * Two instances are considered equal if they are the same instance or if all
 * @Id members are equal.
 */
@Target(ElementType.METHOD)
public @interface Id {
}
