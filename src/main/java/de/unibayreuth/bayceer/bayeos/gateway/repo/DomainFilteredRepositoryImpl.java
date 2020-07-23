package de.unibayreuth.bayceer.bayeos.gateway.repo;

import static org.springframework.data.jpa.domain.Specification.where;

import java.util.List;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Path;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.datatables.SpecificationBuilder;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.security.access.AccessDeniedException;

import de.unibayreuth.bayceer.bayeos.gateway.DomainFilter;
import de.unibayreuth.bayceer.bayeos.gateway.model.DomainEntity;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;

public class DomainFilteredRepositoryImpl<T extends DomainEntity> extends SimpleJpaRepository<T, Long> implements DomainFilteredRepository<T> {
				
	private Boolean nullDomainReadAble;
	
	private static final Sort domainAsc = Sort.by(Direction.ASC,"domain.id");
	private static final Sort domainDesc = Sort.by(Direction.DESC,"domain.id");
			
	DomainFilteredRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
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
		return findAllSorted(user,domainFilter, Sort.by("name","domain.name"));
	}
	
	
	
	
	@Override
	public List<T> findAll(User user, DomainFilter d, Sort sort) {
		return findAllSorted(user, d, sort);
	}	
			
	private List<T> findAllSorted(User user, DomainFilter domainFilter, Sort sort) {
		sort = (sort == null)?Sort.unsorted():sort;
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
		if (user.inNullDomain()) {	 	// all domain matches		
			return super.findById(id).orElseThrow();		  
		} else if (nullDomainReadAble) {// coalesce user domain, null domain  			
			return super.findOne(where(domainOrNull(user.getDomainId())).and(id(id))).orElseThrow();
		} else { 						// user domain only  			
			return super.findOne(where(domain(user.getDomainId())).and(id(id))).orElseThrow();
		}
	}
	
	
	@Override
	public T findOneByName(User user, String name) {
		if (user.inNullDomain()) { 	// coalesce null domain, any domain  			
			List<T> l = super.findAll(name(name), domainDesc);
			if (l != null && l.size()>0) {
				return l.get(0);
			} else {
				return null;
			}
	
		} else if (nullDomainReadAble) { // coalesce user domain, null domain
			List<T> l = super.findAll(where(name(name)).and(domainOrNull(user.getDomainId())), domainAsc);
			if (l != null && l.size()>0) {
				return l.get(0);
			} else {
				return null;
			}			
	
		} else { // user domain only 
			return super.findOne(where(domain(user.getDomainId())).and(name(name))).orElseThrow();
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
		// Fixes transient instance problem
		if (entity.getDomainId()==null) {
			entity.setDomain(null);
		}
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
				return findAll(input);
			} else {
				return findAll(input,null, domain(domainFilter.getId()));
			}
		} else {
			// Domain User
			if (nullDomainReadAble) {
				return findAll(input, null,domainOrNull(user.getDomain().getId()));
			} else {
				return findAll(input, null,domain(user.getDomain().getId()));
			}
		}

	}


	@Override
	  public DataTablesOutput<T> findAll(DataTablesInput input) {
	    return findAll(input, null, null, null);
	  }

	  @Override
	  public DataTablesOutput<T> findAll(DataTablesInput input,
	      Specification<T> additionalSpecification) {
	    return findAll(input, additionalSpecification, null, null);
	  }

	  @Override
	  public DataTablesOutput<T> findAll(DataTablesInput input,
	      Specification<T> additionalSpecification, Specification<T> preFilteringSpecification) {
	    return findAll(input, additionalSpecification, preFilteringSpecification, null);
	  }

	  @Override
	  public <R> DataTablesOutput<R> findAll(DataTablesInput input, Function<T, R> converter) {
	    return findAll(input, null, null, converter);
	  }

	  @Override
	  public <R> DataTablesOutput<R> findAll(DataTablesInput input,
	      Specification<T> additionalSpecification, Specification<T> preFilteringSpecification,
	      Function<T, R> converter) {
	    DataTablesOutput<R> output = new DataTablesOutput<>();
	    output.setDraw(input.getDraw());
	    if (input.getLength() == 0) {
	      return output;
	    }

	    try {
	      long recordsTotal =
	          preFilteringSpecification == null ? count() : count(preFilteringSpecification);
	      if (recordsTotal == 0) {
	        return output;
	      }
	      output.setRecordsTotal(recordsTotal);

	      SpecificationBuilder<T> specificationBuilder = new SpecificationBuilder<>(input);
	      Page<T> data = findAll(
	              Specification.where(specificationBuilder.build())
	                      .and(additionalSpecification)
	                      .and(preFilteringSpecification),
	              specificationBuilder.createPageable());

	      @SuppressWarnings("unchecked")
	      List<R> content =
	          converter == null ? (List<R>) data.getContent() : data.map(converter).getContent();
	      output.setData(content);
	      output.setRecordsFiltered(data.getTotalElements());

	    } catch (Exception e) {
	      output.setError(e.toString());
	    }

	    return output;
	  }

	



	

}
