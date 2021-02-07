package com.db.cloud.custom.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * This ListObjectToJson custom annotation helps converting List of Java Wrapper types in to Json string  while storing in data store entity.
 *
 * @author Ashish Jain
 * @version 1.0 
 * @date 31-Dec-2018
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})  
public @interface JsonToMapObject {

}
