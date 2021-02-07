package com.db.cloud.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.db.cloud.constant.JavaClassType;
import com.db.cloud.converter.CloudDatastoreTypeConverter;
import com.db.cloud.custom.annotation.CloudInsertIgnore;
import com.db.cloud.custom.annotation.JsonStringToObject;
import com.db.cloud.custom.annotation.JsonToListCustomObject;
import com.db.cloud.custom.annotation.JsonToListObject;
import com.db.cloud.custom.annotation.JsonToMapObject;
import com.db.cloud.custom.annotation.ListCustomObjectToJson;
import com.db.cloud.custom.annotation.ListObjectToJson;
import com.db.cloud.custom.annotation.MapObjectToJson;
import com.db.cloud.custom.annotation.ObjectToJsonString;
import com.db.cloud.marker.MarkerModel;
import com.google.cloud.datastore.BaseEntity;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Entity.Builder;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.NullValue;
import com.google.cloud.datastore.Value;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
/**
 * This utility CloudReflectionUtil class works as an intermediary between model
 * objects and cloud data store entity. It converts model objects in to cloud
 * data store entity and vice versa and set the instance states. It uses
 * CloudTypeConverterUtil class for converting different types like List<Value>
 * & Timestamp .
 *
 * @author Ashish Jain
 * @version 1.0
 * @date 13-DEC-2018
 *
 * 
 */
public class CloudDatastoreRefUtil {

	/**
	 * This converterUtil instance used for converting different java and cloud data
	 * store data types
	 */
	private CloudDatastoreTypeConverter converterUtil;

	/**
	 * This converts java objects in json and vice versa.
	 */
	Gson gson = new Gson();

	/**
	 * This CloudReflectionUtil constructor initialize instance variable.
	 */
	public CloudDatastoreRefUtil() {
		converterUtil = new CloudDatastoreTypeConverter();
	}

	/**
	 * This getKindId method gives cloud data store table key.
	 * 
	 * @param Object obj - This will retrieve key from object getId method.
	 * @return Long - returns data store key value.
	 */
	public Long getKindId(Object obj) {
		Long kindId = null;
		try {
			Method kindIdGetterMethod = obj.getClass().getMethod("getId");
			if (kindIdGetterMethod == null)
				return null;
			kindId = (Long) kindIdGetterMethod.invoke(obj);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		return kindId;
	}

	/**
	 * This setEntityToModel method converts cloud data store entity to model
	 * object.
	 * 
	 * @param        Class<?> modelClassType - Generic class type for all Model
	 *               Classes .
	 * @param Entity entity - Cloud data store Entity for CRUD operation purpose.
	 * @return Object - returns model after filling Entity values .
	 */
	public Object setEntityToModel(Class<?> modelClassType, Entity entity) {
		Object model = null;
		try {
			BeanInfo info = null;
			model = modelClassType.newInstance();
			info = Introspector.getBeanInfo(model.getClass(), Object.class);
			PropertyDescriptor[] props = info.getPropertyDescriptors();
			for (PropertyDescriptor pd : props) {
				if (pd.getName().equals("class"))
					continue;
				String name = pd.getName();
				Method setter = pd.getWriteMethod();
				if (setter == null)
					continue;
				if (setter.getAnnotationsByType(CloudInsertIgnore.class) != null && setter.getAnnotationsByType(CloudInsertIgnore.class).length > 0) 
					continue;
				Class<?> type = pd.getPropertyType();
				Object value = retrieveEntityColumnValue(entity, name, type.getName(), setter);
				if (value == null) {
					continue;

				}
				//System.out.println(name + " = " + value + ", type = " + type + ", name = " + type.getName());
				setter.invoke(model, value);
			}
		} catch (ClassNotFoundException | IllegalArgumentException | InvocationTargetException | IntrospectionException
				| InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}

		return model;

	}

	/**
	 * This retrieveEntityColumnValue method gets cloud data store entity data by
	 * column name.
	 * 
	 * @param        Class<?> modelClassType - Generic class type for all Model
	 *               Classes .
	 * @param String dataStoreColumName - Cloud data store column name.
	 * @param String dataStoreColumnTypeShouldBe - Cloud data store column type.
	 * @return Object - returns model property value.
	 * @throws ClassNotFoundException
	 */

	private Object retrieveEntityColumnValue(Entity entity, String dataStoreColumName,
			String dataStoreColumnTypeShouldBe, Method setter) throws ClassNotFoundException {
		if (dataStoreColumName.equals("id")) {
			Object result = entity.getKey().getId();
			return result;
		}
		if (!entity.contains(dataStoreColumName))
			return null;
		Value<?> dataStoreValue = entity.getValue(dataStoreColumName);
		if (dataStoreValue instanceof NullValue)
			return null;
		Object result = null;
		switch (dataStoreValue.getType()) {
		case LONG:
			if (JavaClassType.LONG_TYPE.equals(dataStoreColumnTypeShouldBe))
				result = dataStoreValue.get();
			else if(JavaClassType.INTEGER_TYPE.equals(dataStoreColumnTypeShouldBe))
				result = converterUtil.longToIntegerObject((Long)dataStoreValue.get());
			break;
		case DOUBLE:
			if (JavaClassType.DOUBLE_TYPE.equals(dataStoreColumnTypeShouldBe))
				result = dataStoreValue.get();
			break;
		case STRING:
			if (JavaClassType.STRING_TYPE.equals(dataStoreColumnTypeShouldBe))
				result = dataStoreValue.get();
			else if (setter.getAnnotationsByType(JsonToListObject.class) != null
					&& setter.getAnnotationsByType(JsonToListObject.class).length > 0) {
				//System.out.println("JsonToListObject : "+dataStoreColumnTypeShouldBe);
				result = getListObject(setter, dataStoreValue, dataStoreColumnTypeShouldBe);
			}else if (setter.getAnnotationsByType(JsonToListCustomObject.class) != null && setter.getAnnotationsByType(JsonToListCustomObject.class).length > 0) 
			{	
				//System.out.println("JsonToListCustomObject : "+dataStoreColumnTypeShouldBe);
				result = getListCustomObject(setter, dataStoreValue,dataStoreColumnTypeShouldBe);
			} 
			else if (setter.getAnnotationsByType(JsonStringToObject.class) != null
					&& setter.getAnnotationsByType(JsonStringToObject.class).length > 0) {
				//System.out.println("JsonStringToObject : "+dataStoreColumnTypeShouldBe);
				result = getCustomClassObject(setter, dataStoreValue, dataStoreColumnTypeShouldBe);
			} 
			else if (setter.getAnnotationsByType(JsonToMapObject.class) != null && setter.getAnnotationsByType(JsonToMapObject.class).length > 0) 
			{	
				//System.out.println("JsonToMapObject : "+dataStoreColumnTypeShouldBe);
				result = getMapObject(setter, dataStoreValue,dataStoreColumnTypeShouldBe);
			}
			else
				System.err.println("STRING Error : New Type " + dataStoreColumnTypeShouldBe);
			break;
		case LIST:
			if (JavaClassType.LIST_TYPE.equals(dataStoreColumnTypeShouldBe)
					|| JavaClassType.COLLECTION_TYPE.equals(dataStoreColumnTypeShouldBe))
				result = setGenericModelList(entity, dataStoreColumName, setter, result);
			break;
		case NULL:
			result = null;
			break;
		case BOOLEAN:
			if (JavaClassType.BOOLEAN_TYPE.equals(dataStoreColumnTypeShouldBe))
				result = dataStoreValue.get();
			break;
		case TIMESTAMP:
			if (JavaClassType.JAVA_UTIL_DATE.equals(dataStoreColumnTypeShouldBe)) 
				result = converterUtil.cloudTimestampToJavaDate(entity.getTimestamp(dataStoreColumName));
			break;
		default:
			System.out.println("IN SWITCH DEFAULT Error : New Type " + dataStoreColumnTypeShouldBe +", "+dataStoreValue.getType());
		}
		return result;
	}

	/**
	 * This getListObject method retrieves Json data from data store and sets in
	 * list in Model object by annotation through reflection.
	 * 
	 * @param Method setter - Model object setter method used for setting values
	 *               using reflection.
	 * @param        Value<?> dataStoreValue - Entity data store object having
	 *               values for setting in Model's custom class object.
	 * @param String dataStoreColumnTypeShouldBe - It is for matching property type
	 *               and value retrieved by annotation.
	 * @return Object - returns Custom class object in Model object after filling
	 *         Entity values .
	 */
	private Object getListObject(Method setter, Value<?> dataStoreValue, String dataStoreColumnTypeShouldBe)
			throws ClassNotFoundException {
		Object result = null;
		Type[] genericParameterTypes = setter.getGenericParameterTypes();
		for (Type genericParameterType : genericParameterTypes) {
			if (genericParameterType instanceof ParameterizedType) {
				ParameterizedType aType = (ParameterizedType) genericParameterType;
				Type[] parameterArgTypes = aType.getActualTypeArguments();
				for (Type parameterArgType : parameterArgTypes) {
					Class parameterArgClass = (Class) parameterArgType;
					//System.out.println("parameterArgClass = " + parameterArgClass.getSimpleName() + "--" + parameterArgClass.getName());
					Class<?> cls = Class.forName(parameterArgClass.getName());
					result = gson.fromJson((String) dataStoreValue.get(), getType(List.class, cls));
				}
			}
		}
		return result;
	}

	/**
	 * This getListCustomObject method retrieves Json data from data store and sets in list in Model object by annotation through reflection.
	 * 
	 * @param Method setter - Model object setter method used for setting values using reflection.
	 * @param Value<?> dataStoreValue - Entity data store object having values for setting in Model's custom class object.
	 * @param String dataStoreColumnTypeShouldBe - It is for matching property type and value retrieved by annotation.
	 * @return Object - returns Custom class object in Model object after filling Entity values .
	 */
	private Object getListCustomObject(Method setter, Value<?> dataStoreValue, String dataStoreColumnTypeShouldBe)
			throws ClassNotFoundException {
		Object result=null;
		Type[] genericParameterTypes = setter.getGenericParameterTypes();
		for(Type genericParameterType : genericParameterTypes){
		    if(genericParameterType instanceof ParameterizedType){
		        ParameterizedType aType = (ParameterizedType) genericParameterType;
		        Type[] parameterArgTypes = aType.getActualTypeArguments();
		        for(Type parameterArgType : parameterArgTypes){
		            Class parameterArgClass = (Class) parameterArgType;
		            //System.out.println("parameterArgClass = " + parameterArgClass.getSimpleName() +"--"+parameterArgClass.getName());
		            Class<?> cls = Class.forName(parameterArgClass.getName());
					result =gson.fromJson((String)dataStoreValue.get(),  getType(List.class, cls));
		        }
		    }
		}
		return result;
	}
	/**
	 * 
	 * @param Entity entity -
	 * @param String dataStoreColumName -
	 * @param Method setter -
	 * @param Object result -
	 * @return
	 */
	private Object setGenericModelList(Entity entity, String dataStoreColumName, Method setter, Object result) {
		Type[] genericParameterTypes = setter.getGenericParameterTypes();
		for (Type genericParameterType : genericParameterTypes) {
			if (genericParameterType instanceof ParameterizedType) {
				ParameterizedType aType = (ParameterizedType) genericParameterType;
				Type[] parameterArgTypes = aType.getActualTypeArguments();
				for (Type parameterArgType : parameterArgTypes) {
					Class parameterArgClass = (Class) parameterArgType;
					//System.out.println("parameterArgClass = " + parameterArgClass.getSimpleName() + "--"	+ parameterArgClass.getName());
					if (parameterArgClass.getName().equals(JavaClassType.STRING_TYPE))
						result = converterUtil.<String>cloudListToJavaList(entity.getList(dataStoreColumName));
					else if (parameterArgClass.getName().equals(JavaClassType.LONG_TYPE))
						result = converterUtil.<Long>cloudListToJavaList(entity.getList(dataStoreColumName));
				}
			}
		}
		return result;
	}
	
	/**
	 * This getMapObject method retrieves Json data from data store and sets in Map in Model object by annotation through reflection.
	 * 
	 * @param Method setter - Model object setter method used for setting values using reflection.
	 * @param Value<?> dataStoreValue - Entity data store object having values for setting in Model's custom class object.
	 * @param String dataStoreColumnTypeShouldBe - It is for matching property type and value retrieved by annotation.
	 * @return Object - returns Custom class object in Model object after filling Entity values .
	 */
	private Object getMapObject(Method setter, Value<?> dataStoreValue, String dataStoreColumnTypeShouldBe)
			throws ClassNotFoundException {
		Object result=null;
		Type[] genericParameterTypes = setter.getGenericParameterTypes();
		for(Type genericParameterType : genericParameterTypes){
		    if(genericParameterType instanceof ParameterizedType){
		        ParameterizedType aType = (ParameterizedType) genericParameterType;
		        Type[] parameterArgTypes = aType.getActualTypeArguments();
		        
		        String [] classParameterGSON=new String[parameterArgTypes.length];
		        int index=0;
		        for(Type parameterArgType : parameterArgTypes){
		            Class parameterArgClass = (Class) parameterArgType;
		            //System.out.println("parameterArgClass = " + parameterArgClass.getSimpleName() +"--"+parameterArgClass.getName());
		            classParameterGSON[index++]=parameterArgClass.getName();
		        }
		        if(classParameterGSON.length>1)
		        {
		        	if(classParameterGSON[0].equals(JavaClassType.STRING_TYPE) && classParameterGSON[1].equals(JavaClassType.STRING_TYPE))
		        	{
		        		result =gson.fromJson((String)dataStoreValue.get(),  new TypeToken<Map<String, String>>() { }.getType());
		        	}
		        	else if(classParameterGSON[0].equals(JavaClassType.LONG_TYPE) && classParameterGSON[1].equals(JavaClassType.LONG_TYPE))
		        	{
		        		result =gson.fromJson((String)dataStoreValue.get(),  new TypeToken<Map<Long, Long>>() { }.getType());
		        	}
		        }
		        
		    }
		}
		return result;
	}

	/**
	 * This getCustomClassObject method retrieves Custom class object in Model
	 * object by annotation through reflection and sets the value retrieved from
	 * Entity data store.
	 * 
	 * @param Method setter - Model object setter method used for setting values
	 *               using reflection.
	 * @param        Value<?> dataStoreValue - Entity data store object having
	 *               values for setting in Model's custom class object.
	 * @param String dataStoreColumnTypeShouldBe - It is for matching property type
	 *               and value retrieved by annotation.
	 * @return Object - returns Custom class object in Model object after filling
	 *         Entity values .
	 */
	private Object getCustomClassObject(Method setter, Value<?> dataStoreValue, String dataStoreColumnTypeShouldBe)
			throws ClassNotFoundException {
		Class<?> cls = Class.forName(dataStoreColumnTypeShouldBe);
		return gson.fromJson((String) dataStoreValue.get(), cls);
	}

	/**
	 * This setModelToEntityBuilder method creates data store Entity builder by
	 * model for Add and update operation.
	 * 
	 * @param KeyFactory keyFactory - Data store class used for creating builder.
	 * @param Object     bean - Generic Model object.
	 * @param            boolean isAddOperation - This flag describes operation for
	 *                   Add and update.
	 * @return Object - returns model after filling Entity values .
	 */
	@SuppressWarnings("rawtypes")
	public BaseEntity.Builder setModelToEntityBuilder(KeyFactory keyFactory, Object bean, boolean isAddOperation) {
		BeanInfo info = null;
		BaseEntity.Builder builder = null;
		boolean isUserGenKey = false;
		try {

			Long kindId = getKindId(bean);
			if (null != kindId)
				isUserGenKey = true;

			if (isAddOperation && !isUserGenKey) {
				IncompleteKey key = keyFactory.newKey(); // Key will be assigned once written
				com.google.cloud.datastore.FullEntity.Builder<IncompleteKey> addBuilder = Entity.newBuilder(key);
				builder = addBuilder;
			} else {
				Builder updateBuilder = Entity.newBuilder(keyFactory.newKey(kindId));
				builder = updateBuilder;
			}
			info = Introspector.getBeanInfo(bean.getClass(), Object.class);
			PropertyDescriptor[] props = info.getPropertyDescriptors();

			for (PropertyDescriptor pd : props) {
				if (pd.getName().equals("class"))
					continue;
				String name = pd.getName();
				Method getter = pd.getReadMethod();

				if (getter == null)
					continue;
				Class<?> type = pd.getPropertyType();
				Object value = null;
				value = getter.invoke(bean);
				//System.out.println(name + " = " + value + ", type = " + type + ", name = " + type.getName());

				if ((name.equals("id") && !isUserGenKey) || (!isAddOperation && name.equals("id")))
					continue;

				if (getter.getAnnotationsByType(CloudInsertIgnore.class) != null && getter.getAnnotationsByType(CloudInsertIgnore.class).length > 0)
					continue;	
				if (value == null) {
					builder.setNull(name);
				} else {
					setEntityBuilderProperties(builder, name, getter, type, value);
				}
			}
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| IntrospectionException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
		return builder;
	}

	/**
	 * 
	 * /** This setEntityBuilderProperties method sets cloud data store entity
	 * builder data by model properties.
	 * 
	 * @param        BaseEntity.Builder builder - This Entity builder is used to set
	 *               values in data store.
	 * @param String name - Name of the data store table column name.
	 * @param Method getter - Getter for retrieving runtime Model values for setting
	 *               in Entity builder.
	 * @param        Class<?> type - Entity Builder data store column type.
	 * @param Object value - Sets value in Entity buider.
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	private void setEntityBuilderProperties(BaseEntity.Builder builder, String name, Method getter, Class<?> type,
			Object value) throws ClassNotFoundException {
		switch (type.getName()) {
		case JavaClassType.STRING_TYPE:
			builder.set(name, (java.lang.String) value);
			break;
		case JavaClassType.JAVA_UTIL_DATE:
			builder.set(name, converterUtil.javaDateToCloudTimestamp((java.util.Date) value));
			break;
		case JavaClassType.LONG_TYPE:
			builder.set(name, (java.lang.Long) value);
			break;
		case JavaClassType.INTEGER_TYPE:
			builder.set(name, converterUtil.integerToLongObject((java.lang.Integer) value));
			break;
		case JavaClassType.BOOLEAN_TYPE:
			builder.set(name, (java.lang.Boolean) value);
			break;
		case JavaClassType.DOUBLE_TYPE:
			builder.set(name, (java.lang.Double) value);
			break;
		case JavaClassType.COLLECTION_TYPE:
		case JavaClassType.LIST_TYPE:
			Annotation cus1[] = getter.getAnnotationsByType(ListObjectToJson.class);
			if (cus1 != null && cus1.length > 0) {
				builder.set(name, gson.toJson(value));
			}
			else if (getter.getAnnotationsByType(ListCustomObjectToJson.class) != null && getter.getAnnotationsByType(ListCustomObjectToJson.class).length > 0) {
				Type contentInfoType = new TypeToken<List<MarkerModel>>(){}.getType();
				String str=gson.toJson(value,contentInfoType);
				builder.set(name, str);
			}
			else {
				setGenericBuilderList(builder, name, getter, value);
			}
			break;
		case JavaClassType.MAP_TYPE:
			if (getter.getAnnotationsByType(MapObjectToJson.class) != null && getter.getAnnotationsByType(MapObjectToJson.class).length > 0) {
				builder.set(name, gson.toJson(value));
			} 
		default:
			if (value instanceof MarkerModel) {
				if (getter.getAnnotationsByType(ObjectToJsonString.class) != null
						&& getter.getAnnotationsByType(ObjectToJsonString.class).length > 0) {
					builder.set(name, (java.lang.String) gson.toJson(value));
				}
			}
		}
	}

	/**
	 * This setGenericBuilderList generic method converts different java type list
	 * provided by getter method and make compatible as per datastore entity and
	 * sets in builder.
	 * 
	 * @param        BaseEntity.Builder builder - Datastore entity builder objects
	 *               for setting values in entity.
	 * @param String name - Datastore entity column name which needs to be set.
	 * @param Method getter - Method though which we get the generic list type.
	 * @param Object value - Value which needs to be set in datastore entity column.
	 */

	private void setGenericBuilderList(BaseEntity.Builder builder, String name, Method getter, Object value) {
		Type returnType = getter.getGenericReturnType();
		if (returnType instanceof ParameterizedType) {
			ParameterizedType type1 = (ParameterizedType) returnType;
			Type[] typeArguments = type1.getActualTypeArguments();
			for (Type typeArgument : typeArguments) {
				Class typeArgClass = (Class) typeArgument;
				//System.out.println("typeArgClass = " + typeArgClass.getSimpleName() + "--" + typeArgClass.getName());
				if (typeArgClass.getName().equals(JavaClassType.STRING_TYPE)) {
					List<Value<String>> javaCollectionToCloudList = converterUtil
							.<String>javaCollectionToCloudList((Collection<String>) value);
					if (javaCollectionToCloudList == null)
						builder.setNull(name);
					else
						builder.set(name, javaCollectionToCloudList);
				} else if (typeArgClass.getName().equals(JavaClassType.LONG_TYPE)) {
					List<Value<Long>> javaCollectionToCloudList = converterUtil
							.<Long>javaCollectionToCloudList((Collection<Long>) value);
					if (javaCollectionToCloudList == null)
						builder.setNull(name);
					else
						builder.set(name, javaCollectionToCloudList);
				}
			}
		}
	}

	/**
	 * This getType returns Type for converting gson to Object and Object to gson.
	 * 
	 * @param rawClass
	 * @param parameter
	 * @return
	 */
	private Type getType(Class<?> rawClass, Class<?> parameter) {
		return new ParameterizedType() {

			@Override
			public Type getRawType() {
				// TODO Auto-generated method stub
				return rawClass;
			}

			@Override
			public Type getOwnerType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Type[] getActualTypeArguments() {
				// TODO Auto-generated method stub
				return new Type[] { parameter };
			}

		};
	}

}
