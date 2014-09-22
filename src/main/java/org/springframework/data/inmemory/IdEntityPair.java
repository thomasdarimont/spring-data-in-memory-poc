package org.springframework.data.inmemory;

public interface IdEntityPair<ID, ENTITY> {

	ID getId();

	ENTITY getEntity();

}
