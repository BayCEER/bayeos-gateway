package de.unibayreuth.bayceer.bayeos.gateway.repo.domain;

import static org.springframework.data.jpa.domain.Specification.where;

import java.util.List;

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

public class DomainEntityRepositoryImpl<T extends DomainEntity> extends SimpleJpaRepository<T, Long>
		implements DomainEntityRepository<T> {

	protected Boolean nullDomainReadable = false;

	private static final Sort sortDefault = Sort.by(Direction.ASC, "domain.name");

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

	public DomainEntityRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);

		// This represents hard coded rights !
		if (entityInformation.getEntityName().matches(DomainEntityRepository.nullDomainReadable)) {
			nullDomainReadable = true;
		} else {
			nullDomainReadable = false;
		}
	}

	@Override
	public List<T> findAllSorted(User user, DomainFilter d, Sort sort) {
		if (user.inNullDomain()) {
			// Filter
			if (d == null || d.getId() == null) {
				return super.findAll(sort);
			} else {
				return super.findAll(domain(d.getId()), sort);
			}
		} else {
			// Domain User
			if (nullDomainReadable) {
				return findAll(domainOrNull(user.getDomainId()), sort);
			} else {
				return findAll(domain(user.getDomainId()), sort);
			}
		}
	}

	@Override
	public List<T> findAll(User user, DomainFilter d) {
		return findAllSorted(user, d, sortDefault);

	}

	@Override
	public Page<T> findAll(User user, DomainFilter d, Pageable pageable) {
		if (user.inNullDomain()) {
			// Filter
			if (d == null || d.getId() == null) {
				return super.findAll(pageable);
			} else {
				return super.findAll(domain(d.getId()), pageable);
			}
		} else {
			// Domain User
			if (nullDomainReadable) {
				return super.findAll(domainOrNull(user.getDomainId()), pageable);
			} else {
				return super.findAll(domain(user.getDomainId()), pageable);
			}
		}
	}

	@Override
	public T findOne(User user, Long id) {
		if (user.inNullDomain()) { // all domain matches
			return super.getById(id);
		} else if (nullDomainReadable) {// coalesce user domain, null domain
			return super.findOne(where(domainOrNull(user.getDomainId())).and(id(id)))
					.orElseThrow(() -> new EntityNotFoundException());
		} else { // user domain only
			return super.findOne(where(domain(user.getDomainId())).and(id(id)))
					.orElseThrow(() -> new EntityNotFoundException());
		}
	}

	@Override
	public T findOneByName(User user, String name) {
		if (user.inNullDomain()) {
			return super.findOne(where(name(name))).orElseThrow(() -> new EntityNotFoundException());
		} else if (nullDomainReadable) {
			return super.findOne(where(name(name)).and(domainOrNull(user.getDomainId())))
					.orElseThrow(() -> new EntityNotFoundException());
		} else {
			return super.findOne(where(name(name)).and(domain(user.getDomainId())))
					.orElseThrow(() -> new EntityNotFoundException());
		}
	}

	@Override
	public void delete(User user, Long id) {
		T e = findOne(user, id);
		if (e != null) {
			if (user.inNullDomain() || user.getDomainId().equals(e.getDomainId())) {
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
	public DataTablesOutput<T> findAll(User user, DomainFilter d, DataTablesInput input) {
		if (user.inNullDomain()) {
			// Filter
			if (d == null || d.getId() == null) {
				return findDataTable(input, null);
			} else {
				return findDataTable(input, domain(d.getId()));
			}
		} else {
			// Domain User
			if (nullDomainReadable) {
				return findDataTable(input, domainOrNull(user.getDomain().getId()));
			} else {
				return findDataTable(input, domain(user.getDomain().getId()));
			}
		}
	}

	private DataTablesOutput<T> findDataTable(DataTablesInput input, Specification<T> filteringSpec) {
		DataTablesOutput<T> output = new DataTablesOutput<>();
		output.setDraw(input.getDraw());
		if (input.getLength() == 0) {
			return output;
		}
		try {
			long recordsTotal = filteringSpec == null ? count() : count(filteringSpec);
			if (recordsTotal == 0) {
				return output;
			}
			output.setRecordsTotal(recordsTotal);

			SpecificationBuilder<T> specificationBuilder = new SpecificationBuilder<>(input);
			Specification<T> specification = Specification.where(specificationBuilder.build())
					.and(filteringSpec);
			Page<T> data = findAll(specification, specificationBuilder.createPageable());
				
			output.setData(data.getContent());
			output.setRecordsFiltered(data.getTotalElements());
			
		} catch (Exception e) {
			output.setError(e.toString());
		}

		return output;

	}

}
