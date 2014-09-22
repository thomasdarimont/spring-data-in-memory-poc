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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.inmemory.ConcurrentHashMapDataStore;
import org.springframework.data.inmemory.repository.sample.TestObject;
import org.springframework.data.inmemory.repository.support.InMemoryRepositoryFactory;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Thomas Darimont
 * @param <T>
 * @param <ID>
 */
public class SimpleInMemoryRepositoryTests {

	TestObjectRepository repository;

	@Before
	public void setup() {

		// repository = new SimpleInMemoryRepository<TestObject, String>(new ReflectionEntityInformation<TestObject,
		// String>(
		// TestObject.class));

		ConcurrentHashMapDataStore<String, TestObject> dataStore = new ConcurrentHashMapDataStore<String, TestObject>();
		this.repository = new InMemoryRepositoryFactory<String, TestObject>(dataStore)
				.getRepository(TestObjectRepository.class);
	}

	@Test
	public void foo() {

		String id = "4711";

		TestObject o = repository.save(new TestObject(id).withStringProperty("test123"));

		TestObject found = repository.findOne(id);

		assertThat(found, is((Object) o));
	}

	@Test
	public void queryDerivationWithSingleAttribute() {

		String id = "4711";
		String stringPropertyValue = "test123";

		TestObject o = repository.save(new TestObject(id).withStringProperty(stringPropertyValue));

		List<TestObject> found = repository.findByStringProperty(stringPropertyValue);

		assertThat(found, hasSize(1));
		assertThat(found.get(0), is(o));
	}

	@Test
	public void queryDerivationWithMultipleAttributesWithConjunction() {

		String id = "4711";
		String stringPropertyValue = "test123";
		int intPropertyValue = 42;

		TestObject o = repository.save(new TestObject(id).withStringProperty(stringPropertyValue).withIntProperty(
				intPropertyValue));

		List<TestObject> found = repository.findByStringPropertyAndIntProperty(stringPropertyValue, intPropertyValue);

		assertThat(found, hasSize(1));
		assertThat(found.get(0), is(o));
	}

	@Test
	public void queryDerivationWithMultipleAttributesWithDisjunction() {

		String id = "4711";
		String stringPropertyValue = "test123";
		int intPropertyValue = 42;

		TestObject o = repository.save(new TestObject(id).withStringProperty(stringPropertyValue).withIntProperty(
				intPropertyValue));

		List<TestObject> found = repository.findByStringPropertyOrIntProperty("NO", intPropertyValue);

		assertThat(found, hasSize(1));
		assertThat(found.get(0), is(o));
	}

	static interface TestObjectRepository extends CrudRepository<TestObject, String>,
			InMemoryRepository<TestObject, String> {

		List<TestObject> findByStringProperty(String stringPropertyValue);

		List<TestObject> findByStringPropertyAndIntProperty(String stringPropertyValue, int intPropertyValue);

		List<TestObject> findByStringPropertyOrIntProperty(String stringPropertyValue, int intPropertyValue);
	}
}
