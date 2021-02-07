package com.db.cloud.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.db.cloud.util.CloudDatastoreRefUtil;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.BooleanValue;
import com.google.cloud.datastore.LongValue;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Value;

/**
 * This utility class CloudTypeConverterUtil provides a way to convert java
 * built in types to cloud data store types and vice versa .
 *
 * @author Ashish Jain
 * @version 1.0
 * @date 13-DEC-2018
 *
 */
public class CloudDatastoreTypeConverter {
	/**
	 * This cloudListToJavaList method converts data store List of Value<String> in
	 * to java List<String>.
	 * 
	 * @param List<Value<String>> dataStoreList - List of Value<String> type.
	 * @return List<String> - Returns data in to Java List<String> type
	 */
	public <T> List<T> cloudListToJavaList(List<Value<T>> dataStoreList) {
		if (dataStoreList == null || dataStoreList.isEmpty())
			return null;
		List<T> result = new ArrayList<T>();
		for (Value<T> s : dataStoreList) {
			result.add(s.get());
		}
		return result;
	}

	/**
	 * This javaCollectionToCloudList method converts java Collection<String> in to
	 * data store List of Value<String> .
	 * 
	 * @param Collection<String> domainList - Java Collection of <String> type.
	 * @return List<Value<String>> - Returns data in to cloud data store
	 *         List<Value<String>> type
	 */
	public <T> List<Value<T>> javaCollectionToCloudList(Collection<T> domainList) {
		if (domainList == null || domainList.isEmpty())
			return null;
		List<Value<T>> result = new ArrayList<>();
		for (T s : domainList) {
			result.add(getValueTypeOf(s));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <T> Value<T> getValueTypeOf(T t) {
		Value<T> result = null;
		if (t instanceof String)
			result = (Value<T>) StringValue.of((String) t);
		else if (t instanceof Long)
			result = (Value<T>) LongValue.of((Long) t);
		else if (t instanceof Boolean)
			result = (Value<T>) BooleanValue.of((Boolean) t);
		else
			System.err.println("Error : New Type-" + t);

		return result;

	}

	/**
	 * This javaTimestampToCloudTimestamp method converts java sql Timestamp type
	 * object in to cloud data store Timestamp type.
	 * 
	 * @param java.sql.Timestamp sqlTimestamp - Java sql Timestamp type.
	 * @return Timestamp - Cloud data store Timestamp type.
	 */
	public Timestamp javaTimestampToCloudTimestamp(java.sql.Timestamp sqlTimestamp) {
		return sqlTimestamp != null ? Timestamp.ofTimeMicroseconds((sqlTimestamp).getTime()) : null;
	}

	/**
	 * This cloudTimestampToJavaTimestamp method converts cloud data store Timestamp
	 * type object in to java sql Timestamp type.
	 * 
	 * @param Timestamp cloudTimestamp- Cloud data store Timestamp type.
	 * @return java.sql.Timestamp - Java sql Timestamp type.
	 */
	public java.sql.Timestamp cloudTimestampToJavaTimestamp(Timestamp cloudTimestamp) {
		return cloudTimestamp != null ? cloudTimestamp.toSqlTimestamp() : null;
	}

	/**
	 * This javaDateToCloudTimestamp method converts java util Date type object in
	 * to cloud data store Timestamp type.
	 * 
	 * @param java.util.Date javaDate - Java util Date type.
	 * @return Timestamp - Cloud data store Timestamp type.
	 */
	public Timestamp javaDateToCloudTimestamp(Date javaDate) {
		return javaDate != null ? Timestamp.of(javaDate) : null ;
	}

	/**
	 * This cloudTimestampToJavaTimestamp method converts cloud data store Timestamp
	 * type object in to java util Date type.
	 * 
	 * @param Timestamp cloudTimestamp- Cloud data store Timestamp type.
	 * @return java.util.Date - Java util Date type.
	 */
	public Date cloudTimestampToJavaDate(Timestamp cloudTimestamp) {
		return cloudTimestamp != null ? cloudTimestamp.toDate() : null;
	}

	/**
	 * This integerToLongObject method converts Integer wrapper in Long wrapper type.
	 * @param Integer object- Cloud data store Integer type.
	 * @return Long - Java Long data type after converting.
	 */

	public Long integerToLongObject(Integer object) {
		return object != null ? Long.valueOf(object) : null;
	}
	
	/**
	 * This longToIntegerObject method converts Long wrapper in Integer wrapper type.
	 * @param Long object- Cloud data store Long type.
	 * @return Integer - Java Integer data type after converting.
	 */

	public Integer longToIntegerObject(Long object) {
		return object!=null ? object.intValue() : null;
	}

	
	
}
