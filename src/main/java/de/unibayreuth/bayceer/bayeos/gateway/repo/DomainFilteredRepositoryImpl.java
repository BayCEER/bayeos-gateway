package de.unibayreuth.bayceer.bayeos.gateway.repo;

import static org.springframework.data.jpa.domain.Specifications.where;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Path;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.security.access.AccessDeniedException;

import de.unibayreuth.bayceer.bayeos.gateway.DomainFilter;
import de.unibayreuth.bayceer.bayeos.gateway.model.DomainEntity;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public class DomainFilteredRepositoryImpl<T extends DomainEntity> 
extends DataTablesRepositoryImpl<T, Long>
implements DomainFilteredRepository<T> {
				
	Boolean nullDomainReadAble;
			
	public DomainFilteredRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);				 
	
		// This represents hard coded rights !
		if (entityInformation.getEntityName().matches(DomainFilteredRepository.nullDomainReadAble)){
			nullDomainReadAble = true;			
		} else {
			nullDomainReadAble = false;
		}				
	}
	
	
		
		

	private Specification<T> domain(Long id) {
		return (root, query, cb) -> {
			return cb.equal(root.get("domain").get("id"), id);
		};
	};

	private Specification<T> domainOrNull(Long id) {
		return (root, query, cb) -> {
			Path<Long> p = root.get("domain").get("id");
			return cb.or(cb.equal(p, id), cb.isNull(p));
		};
	};

	private Specification<T> id(Long id) {
		return (root, query, cb) -> {
			return cb.equal(root.get("id"), id);
		};
	};
	
	private Specification<T> name(String value) {
		return (root, query, cb) -> {
			return cb.equal(root.get("name"), value);
		};
	};
		

	@Override
	public List<T> findAll(User user, DomainFilter domainFilter) {
		return findAllSorted(user,domainFilter,null);
	}
	
	@Override
	public List<T> findAllSortedByName(User user, DomainFilter domainFilter) {
		return findAllSorted(user,domainFilter,new Sort("name","domain.name"));
	}
	
	
	
	
	@Override
	public List<T> findAll(User user, DomainFilter d, Sort sort) {
		return findAllSorted(user, d, sort);
	}	
			
	private List<T> findAllSorted(User user, DomainFilter domainFilter, Sort sort) {
		if (user.inNullDomain()) {
			// Filter
			if (domainFilter == null || domainFilter.getId() == null) {
				return super.findAll(sort);
			} else {
				return super.findAll(domain(domainFilter.getId()),sort);
			}
		} else {
			// Domain User
			if (nullDomainReadAble) {
				return findAll(domainOrNull(user.getDomainId()),sort);
			} else {
				return findAll(domain(user.getDomainId()),sort);
			}
		}

	}

	@Override
	public T findOne(User user, Long id) {
		if (user.inNullDomain() || nullDomainReadAble) {
			return super.findOne(id);
		} else {
			return super.findOne(where(domain(user.getDomainId())).and(id(id)));
		}
	}
	
	
	@Override
	public T findOneByName(User user, String name) {
		if (user.inNullDomain() || nullDomainReadAble) {
			return super.findOne(name(name));
		} else {
			return super.findOne(where(domain(user.getDomainId())).and(name(name)));
		}
	}


	@Override
	public void delete(User user, Long id) {
		T e = findOne(user, id);
		if (e != null) {
			if (user.inNullDomain() || user.getDomainId().equals(e.getDomainId()) ) {
				super.delete(e);
			} else {
				throw new AccessDeniedException("Missing rights to delete foreign domain objects.");
			}
		} else {
			throw new EntityNotFoundException();
		}

	}

	@Override
	public <S extends T> S save(User user, S entity) {
		if (user.inNullDomain() || user.getDomainId().equals(entity.getDomainId())) {
			return super.save(entity);
		} else {
			throw new AccessDeniedException("Missing rights to save domain object.");
		}
	}

	@Override
	public Page<T> findAll(User user, DomainFilter domainFilter, Pageable pageable) {
		if (user.inNullDomain()) {
			// Filter
			if (domainFilter == null || domainFilter.getId() == null) {
				return super.findAll(pageable);
			} else {
				return super.findAll(domain(domainFilter.getId()), pageable);
			}
		} else {
			// Domain User
			if (nullDomainReadAble) {
				return super.findAll(domainOrNull(user.getDomainId()), pageable);
			} else {
				return super.findAll(domain(user.getDomainId()), pageable);
			}
		}
	}

	@Override
	public DataTablesOutput<T> findAll(User user, DomainFilter domainFilter, DataTablesInput input) {
		if (user.inNullDomain()) {
			// Filter
			if (domainFilter == null || domainFilter.getId() == null) {
				return super.findAll(input);
			} else {
				return super.findAll(input, domain(domainFilter.getId()));
			}
		} else {
			// Domain User
			if (nullDomainReadAble) {
				return super.findAll(input, domainOrNull(user.getDomain().getId()));
			} else {
				return super.findAll(input, domain(user.getDomain().getId()));
			}
		}

	}





	

}
