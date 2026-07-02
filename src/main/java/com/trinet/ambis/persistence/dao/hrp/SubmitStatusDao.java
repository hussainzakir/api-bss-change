package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.SubmitStatus;

@Repository
@Transactional(readOnly = true)
public interface SubmitStatusDao extends JpaRepository<SubmitStatus, Long> {

	/**
	 * Find SubmitStatus for given companyCode and confirmationNumber
	 * 
	 * @param companyCode
	 * @param confirmationNumber
	 * @return SubmitStatus
	 */
	SubmitStatus findByCompanyAndConfirmationNumber(String companyCode, String confirmationNumber);

	/**
	 * Find latest submitted entry for given companyCode
	 * 
	 * @param companyCode
	 * @return SubmitStatus
	 */
	@Query("from SubmitStatus ss where ss.company=?1 and ss.id = (select MAX(ss1.id) from SubmitStatus ss1 where ss1.company=?1)")
	SubmitStatus findLatestSubmitStatusBy(String companyCode);

	/**
	 * Find SubmitStatus for given confirmationNumber and status
	 * 
	 * @param confirmationNumber
	 * @param statuses
	 * 
	 * @return SubmitStatus
	 */
	SubmitStatus findByConfirmationNumberAndStatusIn(String confirmationNumber, Set<String> statuses);

	/**
	 * Returns the list of SubmitStatus for given company, plan year id and statuses
	 * 
	 * @param companyCode
	 * @param realmPlYrId
	 * @param statuses
	 * @return
	 */
	List<SubmitStatus> findByCompanyAndRealmYrIdAndStatusIn(String companyCode, long realmPlYrId, Set<String> statuses);

	/**
	 * This method updates submit status for given confirmationNumber and
	 * companyCode.
	 * 
	 * @param status
	 * @param confirmationNumber
	 * @param companyCode
	 * @return
	 */
	@Modifying
	@Query("update SubmitStatus ss set ss.status = :status where ss.confirmationNumber = :confirmationNumber and ss.company = :companyCode")
	Integer update(@Param("status") String status, @Param("confirmationNumber") String confirmationNumber,
			@Param("companyCode") String companyCode);

	/**
	 * This method updates statementUploadStatus and emailSent for given
	 * confirmationNumber and companyCode.
	 * 
	 * @param statementUploadStatus
	 * @param emailSentStatus
	 * @param confirmationNumber
	 * @param companyCode
	 * @return
	 */
	@Modifying
	@Query("update SubmitStatus ss set ss.statementUploadStatus = :statementUploadStatus, ss.emailSentStatus = :emailSent  where ss.confirmationNumber = :confirmationNumber and ss.company = :companyCode")
	Integer update(@Param("statementUploadStatus") String statementUploadStatus, @Param("emailSent") boolean emailSentStatus,
			@Param("confirmationNumber") String confirmationNumber, @Param("companyCode") String companyCode);

	/**
	 * This method updates status and updateTime for given
	 * confirmationNumber and companyCode.
	 * 
	 * @param status
	 * @param updateTime
	 * @param confirmationNumber
	 * @param companyCode
	 * @return
	 */
	@Modifying
	@Query("update SubmitStatus ss set ss.status = :status, ss.updateTime = :updateTime where ss.confirmationNumber = :confirmationNumber and ss.company = :companyCode")
	Integer update(@Param("status") String status,
			@Param("updateTime") Date updateTime, @Param("confirmationNumber") String confirmationNumber,
			@Param("companyCode") String companyCode);
}
