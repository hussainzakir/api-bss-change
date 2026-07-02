package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.BandCodes;
import com.trinet.ambis.persistence.model.embeddable.BandCodesUK;

public interface BandCodesRepository extends JpaRepository<BandCodes, BandCodesUK> {

	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	@Modifying
	@Query(value =
			"DELETE "
		+  "  FROM XBSS_BAND_CODES "
		+  " WHERE COMPANY_ID IN (:companyIds)", nativeQuery = true)
	public void deleteWhereCompanyIdIn(@Param("companyIds") List<Long> companyIds);

}
