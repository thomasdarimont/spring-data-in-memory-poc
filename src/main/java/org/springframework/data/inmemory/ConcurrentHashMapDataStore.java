package org.springframework.data.inmemory;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentHashMapDataStore<ID, ENTITY> implements DataStore<ID, ENTITY> {

	private final ConcurrentMap<ID, ENTITY> map = new ConcurrentHashMap<ID, ENTITY>();

	@Override
	public Object getDelegate() {
		return map;
	}

	@Override
	public Iterator<IdEntityPair<ID, ENTITY>> iterator() {

		final Iterator<Entry<ID, ENTITY>> iter = map.entrySet().iterator();
		return new Iterator<IdEntityPair<ID, ENTITY>>() {

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public IdEntityPair<ID, ENTITY> next() {
				return new MapEntryIdEntityPairAdapter<ID, ENTITY>(iter.next());
			}

			@Override
			public void remove() {
				iter.remove();
			}

		};
	}

	static class MapEntryIdEntityPairAdapter<ID, ENTITY> implements IdEntityPair<ID, ENTITY> {

		private final Map.Entry<ID, ENTITY> entry;

		public MapEntryIdEntityPairAdapter(Entry<ID, ENTITY> entry) {
			this.entry = entry;
		}

		@Override
		public ID getId() {
			return entry.getKey();
		}

		@Override
		public ENTITY getEntity() {
			return entry.getValue();
		}
	}
}
