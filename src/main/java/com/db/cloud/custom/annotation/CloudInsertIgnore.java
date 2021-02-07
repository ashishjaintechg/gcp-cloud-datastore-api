package com.db.cloud.custom.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * This CloudInsertIgnore custom annotation helps identifying properties which don't participate in cloud datastore operations.
 *
 * @author Ashish Jain
 * @version 1.0 
 * @date 02-FEB-2019
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})  
public @interface CloudInsertIgnore {
}
