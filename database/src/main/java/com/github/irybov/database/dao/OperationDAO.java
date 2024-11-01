package com.github.irybov.database.dao;

import java.time.OffsetDateTime;
import java.util.List;

import javax.persistence.EntityManager;
//import javax.persistence.TypedQuery;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.github.irybov.database.common.QPredicate;
import com.github.irybov.database.entity.Operation;
import com.github.irybov.database.entity.QOperation;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;

@Repository
public class OperationDAO {
	
	@Autowired
	private EntityManager entityManager;
	
	public void save(Operation operation) {
		entityManager.persist(operation);
	}
	
	public Operation getById(long id) {
		return entityManager.find(Operation.class, id);
	}
	
	public List<Operation> getAll(int id) {
		return entityManager.createQuery
				("SELECT o FROM Operation o WHERE o.sender=:id OR o.recipient=:id ORDER BY id DESC",
				Operation.class)
				.setParameter("id", id)					
				.getResultList();
	}
	
/*	public Page<Operation> getPage(int id, Pageable pageable){
		
		long count = (long) entityManager.createQuery("SELECT COUNT(o.id) FROM Operation o "
				+ "WHERE o.sender=:id OR o.recipient=:id")
				.setParameter("id", id)
				.getSingleResult();
		
		List<Operation> operations = entityManager.createQuery
				("SELECT o FROM Operation o WHERE o.sender=:id OR o.recipient=:id ORDER BY id DESC",
				Operation.class)
				.setParameter("id", id)
				.setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
				.setMaxResults(pageable.getPageSize())
				.getResultList();
		
		return new PageImpl<>(operations, pageable, count);
	}*/
	
	public Page<Operation> getPage(int id, String action, Double minval, Double maxval, 
			OffsetDateTime mindate, OffsetDateTime maxdate, Pageable pageable){
/*		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Operation> result = builder.createQuery(Operation.class);
		Root<Operation> root = result.from(Operation.class);
		
		Predicate hasAction = builder.like(root.get(Operation_.ACTION), "%"+action+"%");
		Predicate hasOwner = builder.or(builder.equal(root.get(Operation_.SENDER), id),
							 			builder.equal(root.get(Operation_.RECIPIENT), id));
		Predicate amountBetween = builder.between(root.get(Operation_.AMOUNT), minval, maxval);
		Predicate dateBetween = builder.between(root.get(Operation_.CREATED_AT), mindate, maxdate);
		Predicate query = builder.and(hasAction, hasOwner, amountBetween, dateBetween);
		
		result.where(query);
		result.orderBy(builder.desc(root.get(Operation_.ID)));		
		result.select(root);
		List<Operation> operations = entityManager.createQuery(result).getResultList();
		
//		TypedQuery<Operation> typed = entityManager.createQuery(result);
//		List<Operation> operations = typed.getResultList();
//        typed.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
//        typed.setMaxResults(pageable.getPageSize());
		
		CriteriaQuery<Long> howMuch = builder.createQuery(Long.class);
		Root<Operation> quantity = howMuch.from(Operation.class);
		howMuch.select(builder.count(quantity));
		long count = entityManager.createQuery(howMuch).getSingleResult();
*/		
		Predicate or = QPredicate.builder()
				.add(id, QOperation.operation.sender::eq)
				.add(id, QOperation.operation.recipient::eq)
				.buildOr();
		Predicate and = QPredicate.builder()
				.add(action, QOperation.operation.action::like)
				.add(minval, maxval, QOperation.operation.amount::between)
				.add(mindate, maxdate, QOperation.operation.createdAt::between)
				.buildAnd();
		Predicate where = ExpressionUtils.allOf(or, and);
//		Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
//		Sort.Direction.DESC, new OperationPage().getSortBy());	
		
		JPAQuery<Operation> query = new JPAQuery<>(entityManager);
		query.from(QOperation.operation)
			.where(where)
			.limit(pageable.getPageSize())
			.offset(pageable.getOffset());
		PathBuilder<Operation> entityPath = new PathBuilder<>(Operation.class, "operation");
        for(Sort.Order order : pageable.getSort()) {
            PathBuilder<Object> path = entityPath.get(order.getProperty());
            query.orderBy(new OrderSpecifier(Order.valueOf(order.getDirection().name()), path));
        }
        List<Operation> operations = query.createQuery().getResultList();
/*		
		List<Operation> operations = new JPAQuery<Operation>(entityManager)
//									.select(QOperation.operation)
									.from(QOperation.operation)
									.where(where)
									.where(QOperation.operation.action.like("%"+action+"%"), 
										QOperation.operation.sender.eq(id).or
										(QOperation.operation.recipient.eq(id)), 
										QOperation.operation.amount.between(minval, maxval), 
										QOperation.operation.createdAt.between(mindate, maxdate))
//									.orderBy(QOperation.operation.id.desc())
									.limit(pageable.getPageSize())
									.offset(pageable.getOffset())
									.fetch();
*/		
		long count = new JPAQuery<Operation>(entityManager)
//					.select(QOperation.operation)
					.from(QOperation.operation)
					.fetchCount();
		
		return new PageImpl<>(operations, pageable, count);
	}
	
}
