package com.moozvine.detox;

/**
 * Our own version of Function so we can not depend or Guava and run on Java < 8.
 * @param <F> from type
 * @param <T> to type
 */
interface Function<F,T> {
  T apply(F from);
}
