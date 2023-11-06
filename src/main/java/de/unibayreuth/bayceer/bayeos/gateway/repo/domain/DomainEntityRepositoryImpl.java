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

	protected Boolean defaultDomainReadable = false;

	private static final Sort sortDefault = Sort.by(Direction.ASC, "domain.name");

	private Specification<T> domain(Long id) {
		return (root, query, cb) -> {
			return cb.equal(root.get("domain").get("id"), id);
		};
	};

	private Specification<T> domainOrDefault(Long id) {
		return (root, query, cb) -> {
			Path<Long> p = root.get("domain").get("id");
			return cb.or(cb.equal(p, id), cb.equal(p,1));
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
		defaultDomainReadable = entityInformation.getEntityName().matches(DomainEntityRepository.defaultDomainReadable);		
	}

	@Override
	public List<T> findAllSorted(User user, DomainFilter d, Sort sort) {
		if (user.inDefaultDomain()) {
			// Filter
			if (d == null || d.getId() == null) {
				return super.findAll(sort);
			} else {
				return super.findAll(domain(d.getId()), sort);
			}
		} else {
			// Domain User
			if (defaultDomainReadable) {
				return findAll(domainOrDefault(user.getDomain().getId()), sort);
			} else {
				return findAll(domain(user.getDomain().getId()), sort);
			}
		}
	}

	@Override
	public List<T> findAll(User user, DomainFilter d) {
		return findAllSorted(user, d, sortDefault);

	}

	@Override
	public Page<T> findAll(User user, DomainFilter d, Pageable pageable) {
		if (user.inDefaultDomain()) {
			// Filter
			if (d == null || d.getId() == null) {
				return super.findAll(pageable);
			} else {
				return super.findAll(domain(d.getId()), pageable);
			}
		} else {
			// Domain User
			if (defaultDomainReadable) {
				return super.findAll(domainOrDefault(user.getDomain().getId()), pageable);
			} else {
				return super.findAll(domain(user.getDomain().getId()), pageable);
			}
		}
	}

	@Override
	public T findOne(User user, Long id) {
		if (user.inDefaultDomain()) { // all domain matches
			return super.getById(id);
		} else if (defaultDomainReadable) {// coalesce user domain, default domain
			return super.findOne(where(domainOrDefault(user.getDomain().getId())).and(id(id)))
					.orElseThrow(() -> new EntityNotFoundException());
		} else { // user domain only
			return super.findOne(where(domain(user.getDomain().getId())).and(id(id)))
					.orElseThrow(() -> new EntityNotFoundException());
		}
	}

	@Override
	public T findOneByName(User user, String name) {
		if (user.inDefaultDomain()) {
			return super.findOne(where(name(name))).orElseThrow(() -> new EntityNotFoundException());
		} else if (defaultDomainReadable) {
			return super.findOne(where(name(name)).and(domainOrDefault(user.getDomain().getId())))
					.orElseThrow(() -> new EntityNotFoundException());
		} else {
			return super.findOne(where(name(name)).and(domain(user.getDomain().getId())))
					.orElseThrow(() -> new EntityNotFoundException());
		}
	}

	@Override
	public void delete(User user, Long id) {
		T e = findOne(user, id);
		if (e != null) {
			if (user.inDefaultDomain() || user.getDomain().equals(e.getDomain())) {
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
		if (user.inDefaultDomain() || user.getDomain().equals(entity.getDomain())) {
			return super.save(entity);
		} else {
			throw new AccessDeniedException("Missing rights to save domain object.");
		}
	}

	@Override
	public DataTablesOutput<T> findAll(User user, DomainFilter d, DataTablesInput input) {
		if (user.inDefaultDomain()) {
			// Filter
			if (d == null || d.getId() == null) {
				return findDataTable(input, null);
			} else {
				return findDataTable(input, domain(d.getId()));
			}
		} else {
			// Domain User
			if (defaultDomainReadable) {
				return findDataTable(input, domainOrDefault(user.getDomain().getId()));
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
