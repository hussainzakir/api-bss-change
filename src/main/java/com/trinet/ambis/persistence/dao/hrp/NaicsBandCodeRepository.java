package com.trinet.ambis.persistence.dao.hrp;

import com.trinet.ambis.persistence.model.NaicsBandCode;
import com.trinet.ambis.persistence.model.embeddable.NaicsBandCodeUK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface NaicsBandCodeRepository extends JpaRepository<NaicsBandCode, NaicsBandCodeUK> {

    @Query(
            value = "SELECT * FROM XBSS_NAICS_BAND_CODE " +
                    "WHERE NAICS_CODE = :naicsCode " +
                    "AND :date BETWEEN EFF_DT AND END_DT " +
                    "ORDER BY EFF_DT DESC " +
                    "FETCH FIRST 1 ROWS ONLY",
            nativeQuery = true
    )
    Optional<NaicsBandCode> findActiveByNaicsCodeAndDate(@Param("naicsCode") String naicsCode, @Param("date") Date date);
}
