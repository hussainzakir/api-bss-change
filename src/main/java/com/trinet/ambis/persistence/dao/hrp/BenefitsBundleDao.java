package com.trinet.ambis.persistence.dao.hrp;

import com.trinet.ambis.persistence.model.Bundle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface BenefitsBundleDao extends JpaRepository<Bundle, Long> {

    @Query(value = "SELECT b.ID as bundleId, b.NAME as bundleName, b.TYPE  AS bundleType," +
            "regional.REGIONAL_PLAN_ID as regionalPlanId " +
            "FROM XBSS_BUNDLE b, XBSS_BUNDLE_PLANS bp, XBSS_REGIONAL_BASE_PLAN_MAPPING regional " +
            "WHERE b.ID = bp.BUNDLE_ID " +
            "AND bp.BASE_BENEFIT_PLAN = regional.BASE_PLAN_ID " +
            "AND :effectiveDt BETWEEN regional.EFFDT AND regional.ENDDT " +
            "AND :effectiveDt BETWEEN b.EFFDT AND b.ENDDT " +
            "AND :effectiveDt BETWEEN bp.EFFDT AND bp.ENDDT " +
            "AND bp.OE_QUARTER = regional.OE_QUARTER "+
            "AND bp.OE_QUARTER = :quarter",
            nativeQuery = true)
    List<Object[]> findByEffectiveDateAndQuarter(@Param("effectiveDt") LocalDate effectiveDt,
                                                 @Param("quarter") String quarter);

    @Query(value = "SELECT b.ID as bundleId, b.NAME as bundleName, b.TYPE AS bundleType, " +
            " regional.REGIONAL_PLAN_ID as regionalPlanId " +
            " FROM XBSS_BUNDLE b " +
            " JOIN XBSS_BUNDLE_QUARTER bq " +
            " ON b.ID = bq.BUNDLE_ID " +
            " LEFT JOIN XBSS_BUNDLE_QUARTER_PLAN bqp " +
            " ON bq.ID = bqp.BUNDLE_QUARTER_ID " +
            " AND :effectiveDt BETWEEN bqp.EFFDT AND bqp.ENDDT " +
            " LEFT JOIN XBSS_REGIONAL_BASE_PLAN_MAPPING regional " +
            " ON bqp.BASE_BENEFIT_PLAN = regional.BASE_PLAN_ID " +
            " AND bq.OE_QUARTER = regional.OE_QUARTER " +
            " AND :effectiveDt BETWEEN regional.EFFDT AND regional.ENDDT " +
            " WHERE :effectiveDt BETWEEN bq.EFFDT AND bq.ENDDT " +
            " AND :effectiveDt BETWEEN b.EFFDT AND b.ENDDT " +
            " AND bq.OE_QUARTER = :quarter",
            nativeQuery = true)
    List<Object[]> findByEffectiveDateAndQuarterV2(@Param("effectiveDt") LocalDate effectiveDt,
                                                   @Param("quarter") String quarter);

    @Query("SELECT DISTINCT b FROM Bundle b " +
            "JOIN b.bundlePlans bp " +
            "WHERE bp.id.oeQuarter = :oeQuarter " +
            "AND b.effectiveDate = :effectiveDate")
    List<Bundle> findAllByOeQuarterAndEffectiveDate(@Param("oeQuarter") String oeQuarter,
                                                    @Param("effectiveDate") LocalDate effectiveDate);

    @Query("SELECT DISTINCT b FROM Bundle b " +
            "JOIN b.bundleQuarters bq " +
            "WHERE bq.oeQuarter = :oeQuarter and  :effectiveDate between bq.effdt and bq.enddt " +
            "AND :effectiveDate between b.effectiveDate and b.endDate")
    List<Bundle> findAllByOeQuarterAndEffectiveDateV2(@Param("oeQuarter") String oeQuarter,
                                                      @Param("effectiveDate") LocalDate effectiveDate);

    Bundle findById(long id);

    Bundle findByCompanyCodeAndType(String companyCode, String type);

    Bundle findByCompanyCode(String companyCode);
}