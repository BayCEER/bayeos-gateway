package de.unibayreuth.bayceer.bayeos.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.unibayreuth.bayceer.bayeos.gateway.UserSession;
import de.unibayreuth.bayceer.bayeos.gateway.model.DomainEntity;
import de.unibayreuth.bayceer.bayeos.gateway.repo.domain.DomainEntityRepository;

@Service
public class AccessService {

    @Autowired
    UserSession userSession;

    public boolean isWriteable(DomainEntity e) {               
        return (userSession.getUser().inDefaultDomain() || userSession.getDomain().getId().equals(e.getDomain().getId()));
    }

    public boolean isReadable(DomainEntity e) {
        return e.getDomain().getName().matches(DomainEntityRepository.defaultDomainReadable)
                || userSession.getUser().inDefaultDomain()
                || userSession.getDomain().getId().equals(e.getDomain().getId());
    }

}
