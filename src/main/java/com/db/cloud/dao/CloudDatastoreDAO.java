package com.db.cloud.dao;

import java.util.Iterator;
import java.util.List;

import com.db.cloud.model.Result;
import com.google.cloud.datastore.StructuredQuery.Filter;

/**
 * This interface CloudDatastoreDAO have all cloud data store common operations.
 *
 * @author Ashish Jain
 * @version 1.0
 * @date 13-DEC-2018
 *
 * @param <T> This parameter will have different model objects at run time.
 */
public interface CloudDatastoreDAO<T> {

	T findById(Long id);

	Long add(T t);

	void update(T t);

	void delete(Long id);

	List<T> getByParam(String column, String value);

	List<T> getByParam(String column, Long value);

	T findUniqueBy(Filter filter);

	List<T> findAll(Filter filter);

	List<T> getByFields(Filter firstFilter, Filter... remainingFilter);

	void delete(Long[] ids);

	Long[] add(T[] t);

	void update(T[] t);

	Result<T> queryPage(Integer pageSize, String orderBy, boolean isAscending, String cursor, Filter firstFilter,
			Filter... remainingFilters);

	Iterator<T> queryIterable(Integer pageSize, String orderBy, boolean isAscending, String cursor, Filter firstFilter,
			Filter... remainingFilters);

	int countEntities();
}
