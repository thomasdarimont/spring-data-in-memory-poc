/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.inmemory.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.inmemory.DataStore;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.util.Assert;

/**
 * @author Thomas Darimont
 * @param <T>
 * @param <ID>
 */
public class SimpleInMemoryRepository<T, ID extends Serializable> implements InMemoryRepository<T, ID>,
		CrudRepository<T, ID> {

	private final EntityInformation<T, ?> entityInformation;
	private final DataStore<T, ID> dataStore;

	public SimpleInMemoryRepository(DataStore<T, ID> dataStore, EntityInformation<T, ?> entityInformation) {
		this.dataStore = dataStore;
		this.entityInformation = entityInformation;

	}

	@Override
	public <S extends T> S save(S entity) {

		ID id = extractId(entity);

		Assert.notNull(id, "Id for given entity must not be null!");

		getStore().put(id, entity);

		return entity;
	}

	@Override
	public <S extends T> Iterable<S> save(Iterable<S> entities) {

		for (S entity : entities) {
			save(entity);
		}

		return entities;
	}

	@Override
	public T findOne(ID id) {
		return getStore().get(id);
	}

	@Override
	public boolean exists(ID id) {
		return getStore().containsKey(id);
	}

	@Override
	public Iterable<T> findAll() {
		return getStore().values();
	}

	@Override
	public Iterable<T> findAll(Iterable<ID> ids) {

		List<T> values = new ArrayList<T>();

		Map<ID, T> map = getStore();
		for (ID id : ids) {
			T value = map.get(id);
			if (value != null) {
				values.add(value);
			}
		}

		return values;
	}

	@Override
	public long count() {
		return getStore().size();
	}

	@Override
	public void delete(ID id) {
		getStore().remove(id);
	}

	@Override
	public void delete(T entity) {

		ID id = extractId(entity);
		if (id != null) {
			this.delete(id);
		}
	}

	@Override
	public void delete(Iterable<? extends T> entities) {

		for (T value : entities) {
			delete(value);
		}
	}

	@Override
	public void deleteAll() {
		getStore().clear();
	}

	private Map<ID, T> getStore() {
		return (Map<ID, T>) dataStore.getDelegate();
	}

	@SuppressWarnings("unchecked")
	private ID extractId(T entity) {
		return (ID) entityInformation.getId(entity);
	}
}
