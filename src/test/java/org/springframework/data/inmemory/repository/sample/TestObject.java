package org.springframework.data.inmemory.repository.sample;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

public class TestObject implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id String id;

	String stringProperty;

	int intProperty;

	public TestObject(String id) {
		this.id = id;
	}

	public TestObject withStringProperty(String value) {

		this.stringProperty = value;

		return this;
	}

	public TestObject withIntProperty(int value) {

		this.intProperty = value;

		return this;
	}

	public String getId() {
		return id;
	}

	public String getStringProperty() {
		return stringProperty;
	}

	public int getIntProperty() {
		return intProperty;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TestObject [id=" + id + ", stringProperty=" + stringProperty + ", intProperty=" + intProperty + "]";
	}
}
