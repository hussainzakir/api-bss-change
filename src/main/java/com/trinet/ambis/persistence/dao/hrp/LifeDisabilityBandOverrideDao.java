package com.trinet.ambis.persistence.dao.hrp;

import com.trinet.ambis.persistence.model.LifeDisabilityBandOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;


@Repository
public interface LifeDisabilityBandOverrideDao extends JpaRepository<LifeDisabilityBandOverride, Long> {

    /**
     * Fetches life/disability band overrides for a company by active status.
     *
     * @param company company code
     * @param active  active flag
     * @return matching override records
     */
    Set<LifeDisabilityBandOverride> findByCompanyCodeAndActive(String company, boolean active);
}
