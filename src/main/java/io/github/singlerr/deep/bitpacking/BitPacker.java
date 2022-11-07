package io.github.singlerr.deep.bitpacking;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * Attached to a class that its members needed to be packed
 * Notify annotation processor to handle it
 * Note that a class with {@link BitPacker} must be abstract
 * @author Singlerr
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface BitPacker {
}
