package com.db.cloud.custom.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This JsonToListObject custom annotation helps converting data store string of
 * Json in to List of Java Wrapper types while retrieving from data store
 * entity.
 *
 * @author Ashish Jain
 * @version 1.0
 * @date 28-Jan-2018
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface JsonToListObject {

}
