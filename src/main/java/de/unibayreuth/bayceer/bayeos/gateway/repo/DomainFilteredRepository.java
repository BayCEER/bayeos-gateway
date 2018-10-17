package de.unibayreuth.bayceer.bayeos.gateway.repo;


import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

import de.unibayreuth.bayceer.bayeos.gateway.DomainFilter;
import de.unibayreuth.bayceer.bayeos.gateway.model.DomainEntity;
import de.unibayreuth.bayceer.bayeos.gateway.model.User;

@NoRepositoryBean
public interface DomainFilteredRepository<T extends DomainEntity> extends DataTablesRepository<T, Long>  {
	
	public static final String nullDomainReadAble = "(BoardTemplate|Function|Interval|Unit|Spline|ChannelFunction)";
	
	
	public List<T> findAll(User user, DomainFilter d);		
	public List<T> findAll(User user, DomainFilter d, Sort sort);	
	public List<T> findAllSortedByName(User user, DomainFilter d);
	public Page<T> findAll(User user, DomainFilter d, Pageable pageable);
	
	
	public DataTablesOutput<T> findAll(User user, DomainFilter d, DataTablesInput input);		
	
	public T findOne(User user, Long id);	
	public T findOneByName(User user, String name);

	@Transactional
	public void delete(User user, Long id);
	@Transactional
	public <S extends T> S save(User user, S entity) ;
		
	
	
}