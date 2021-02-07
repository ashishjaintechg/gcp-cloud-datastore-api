package com.db.cloud.constant;

/**
 * 
 * This interface ClassType defines different java wrapper types for conversion
 * at run time.
 * 
 * @author Ashish Jain
 * @version 1.0
 * @date 13-Dec-2018
 *
 */
public interface JavaClassType {

	String STRING_TYPE = "java.lang.String";
	String INTEGER_TYPE = "java.lang.Integer";
	String LONG_TYPE = "java.lang.Long";
	String BOOLEAN_TYPE = "java.lang.Boolean";
	String SQL_TIMESTAMP_TYPE = "java.sql.Timestamp";
	String COLLECTION_TYPE = "java.util.Collection";
	String LIST_TYPE = "java.util.List";
	String MAP_TYPE = "java.util.Map";
	String JAVA_UTIL_DATE = "java.util.Date";
	String DOUBLE_TYPE = "java.lang.Double";
}
