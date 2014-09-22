package org.springframework.data.inmemory.repository.support;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.data.inmemory.DataStore;
import org.springframework.data.inmemory.IdEntityPair;
import org.springframework.data.inmemory.repository.SimpleInMemoryRepository;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.ReflectionEntityInformation;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.repository.query.parser.PartTree.OrPart;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class InMemoryRepositoryFactory<T, ID extends Serializable> extends RepositoryFactorySupport {

	private final DataStore<T, ID> dataStore;

	public InMemoryRepositoryFactory(DataStore<T, ID> dataStore) {
		this.dataStore = dataStore;
	}

	@Override
	public <ET, IT extends Serializable> EntityInformation<ET, IT> getEntityInformation(Class<ET> domainClass) {
		return new ReflectionEntityInformation<ET, IT>(domainClass);
	}

	@Override
	protected Object getTargetRepository(RepositoryMetadata metadata) {

		EntityInformation<T, ID> ei = this.<T, ID> getEntityInformation((Class<T>) metadata.getDomainType());
		return new SimpleInMemoryRepository<T, ID>(dataStore, ei);
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		return SimpleInMemoryRepository.class;
	}

	@Override
	protected QueryLookupStrategy getQueryLookupStrategy(Key key, EvaluationContextProvider evaluationContextProvider) {
		return new InMemoryQueryLookupStrategy<T, ID>(key, evaluationContextProvider, dataStore);
	}

	/**
	 * {@link QueryLookupStrategy} to create {@link PartTreeMongoQuery} instances.
	 * 
	 * @author Oliver Gierke
	 */
	public static class InMemoryQueryLookupStrategy<T, ID extends Serializable> implements QueryLookupStrategy {

		private Key key;
		private EvaluationContextProvider evaluationContextProvider;
		private DataStore<T, ID> dataStore;

		public InMemoryQueryLookupStrategy(Key key, EvaluationContextProvider evaluationContextProvider,
				DataStore<T, ID> dataStore) {
			this.key = key;
			this.evaluationContextProvider = evaluationContextProvider;
			this.dataStore = dataStore;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.QueryLookupStrategy#resolveQuery(java.lang.reflect.Method, org.springframework.data.repository.core.RepositoryMetadata, org.springframework.data.repository.core.NamedQueries)
		 */
		public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries) {

			QueryMethod queryMethod = new QueryMethod(method, metadata);
			return new InMemoryPartTreeQuery<T, ID>(queryMethod, evaluationContextProvider, dataStore);
		}
	}

	public static class InMemoryPartTreeQuery<T, ID extends Serializable> implements RepositoryQuery {

		private QueryMethod queryMethod;
		private Expression expression;
		private EvaluationContextProvider evaluationContextProvider;
		private DataStore<T, ID> dataStore;

		public InMemoryPartTreeQuery(QueryMethod queryMethod, EvaluationContextProvider evalContextProvider,
				DataStore<T, ID> dataStore) {
			this.queryMethod = queryMethod;
			this.evaluationContextProvider = evalContextProvider;
			this.expression = toPredicateExpression(queryMethod);
			this.dataStore = dataStore;
		}

		protected Expression toPredicateExpression(QueryMethod queryMethod) {

			PartTree tree = new PartTree(queryMethod.getName(), queryMethod.getEntityInformation().getJavaType());

			int parameterIndex = 0;
			StringBuilder sb = new StringBuilder();

			for (Iterator<OrPart> orPartIter = tree.iterator(); orPartIter.hasNext();) {

				OrPart orPart = orPartIter.next();

				int partCnt = 0;
				StringBuilder partBuilder = new StringBuilder();
				for (Iterator<Part> partIter = orPart.iterator(); partIter.hasNext();) {

					Part part = partIter.next();
					partBuilder.append("#it?.");
					partBuilder.append(part.getProperty().toDotPath().replace(".", ".?")).append("?.equals(").append("[")
							.append(parameterIndex++).append("])");
					if (partIter.hasNext()) {
						partBuilder.append("&&");
					}

					partCnt++;
				}

				if (partCnt > 1) {
					sb.append("(").append(partBuilder).append(")");
				} else {
					sb.append(partBuilder);
				}

				if (orPartIter.hasNext()) {
					sb.append("||");
				}
			}

			return new SpelExpressionParser().parseExpression(sb.toString());
		}

		@Override
		public Object execute(Object[] parameters) {

			List<Object> resultList = new ArrayList<Object>();

			// EvaluationContext ec = evaluationContextProvider.getEvaluationContext(null, parameters);
			EvaluationContext ec = new StandardEvaluationContext(parameters);
			for (IdEntityPair<T, ID> pair : this.dataStore) {

				ec.setVariable("it", pair.getEntity());
				Object test = expression.getValue(ec);

				if (Boolean.TRUE.equals(test)) {
					resultList.add(pair.getEntity());
				}
			}

			return resultList;
		}

		@Override
		public QueryMethod getQueryMethod() {
			return queryMethod;
		}
	}
}
