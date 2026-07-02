package com.trinet.ambis.persistence.dao.hrp;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.trinet.ambis.persistence.model.RlRegionPlan1;
import com.trinet.ambis.persistence.model.embeddable.RlRegionPlan1UK;
import com.trinet.ambis.persistence.projections.RlRegionPlan1View;

public interface RlRegionPlan1Repository extends JpaRepository<RlRegionPlan1, RlRegionPlan1UK> {

	@Query(value = "select new com.trinet.ambis.persistence.projections.RlRegionPlan1View(plan.benefitPlan as benefitPlan, nvl(plan1.subRegion ,plan1.rlRegionPlan1UK.region) as region) from RlRegionPlan1 plan1 left join XbssRealmPlyrPlan plan on plan1.rlRegionPlan1UK.realmPlyrPlanId = plan.id where plan.realmYearId = :realmPlanYearId")
	public List<RlRegionPlan1View> findByRealPlanYearId(@Param("realmPlanYearId") BigDecimal realmPlanYearId);

}
