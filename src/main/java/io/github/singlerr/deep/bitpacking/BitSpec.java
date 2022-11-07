package io.github.singlerr.deep.bitpacking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * Attached to a method that will be return value from packed bits.
 * Set bit spec for packing value to unsigned 32 / 64 integer
 * @author Singlerr
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface BitSpec {

    /***
     * Range of bits that the value will take up in unsigned 32 / 64 integer
     * @return range of bits
     */
    int bitSize();

    /***
     * Set which variable will be packed
     * Exists in need of grouping getter and setter together
     * @return name that groups getter and setter together
     */
    String variableName();
}
