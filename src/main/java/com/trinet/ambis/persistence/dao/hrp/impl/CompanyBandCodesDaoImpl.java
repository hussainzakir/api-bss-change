/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp.impl;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.CompanyBandCodesDao;
import com.trinet.ambis.service.model.CompanyBandCodes;
import com.trinet.ambis.util.DaoUtils;
import java.util.Date;

/**
 * @author rvutukuri
 *
 */
public class CompanyBandCodesDaoImpl implements CompanyBandCodesDao {
	private static final String PROSPECT_BAND_EFFDT = "PROSPECT_BAND_EFFDT";

	@PersistenceContext(unitName = "bis-hrp")
	EntityManager em;

	@Override
	public List<CompanyBandCodes> getBandCodesByCompanyId(Long companyId) {
		Query query = em.createNamedQuery("BANDCODES_BY_COMPANY_ID");
		query.setParameter(BSSQueryConstants.COMPANY_ID, companyId);
		List<Object[]> results = DaoUtils.getResultList(query, "BANDCODES_BY_COMPANY_ID");
		List<CompanyBandCodes> companyBandcodes = new ArrayList<>();

		for (Object[] r : results) {
			CompanyBandCodes cbc = new CompanyBandCodes();
			cbc.setCompanyId(companyId);
			cbc.setBandCodeType((String) r[1]);
			cbc.setBandCodeValue((String) r[2]);
			companyBandcodes.add(cbc);
		}
		return companyBandcodes;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public int insertUpdateCompanyBandCodes(Long companyId, List<CompanyBandCodes> companyBandCodes) {
		// deleting the band codes before inserting.
		Query dquery = em.createNamedQuery("DELETE_COMPANY_BAND_CODES");
		dquery.setParameter(BSSQueryConstants.COMPANY_ID, companyId);
		DaoUtils.executeUpdate(dquery, "DELETE_COMPANY_BAND_CODES");

		Query query = em.createNamedQuery("INSERT_COMPANY_BAND_CODES");
		int result = 0;
		for (CompanyBandCodes cbc : companyBandCodes) {
			query.setParameter(BSSQueryConstants.COMPANY_ID, cbc.getCompanyId());
			query.setParameter("bandCodeType", cbc.getBandCodeType());
			query.setParameter("bandCodeValue", cbc.getBandCodeValue());
			int num = DaoUtils.executeUpdate(query, "INSERT_COMPANY_BAND_CODES");
			result = result + num;
		}
		return result;
	}
	
	// this setter is required for junit test.
	public void setEm(EntityManager em) {
		this.em = em;
	}

	@Override
	public List<CompanyBandCodes> getBandCodesByCompanyIdAndEffDate(long companyId, String benStartDt) {
		Query query = em.createNamedQuery("BANDCODES_BY_COMPANY_ID_AND_EFFDT");
		query.setParameter(BSSQueryConstants.COMPANY_ID, companyId);
		query.setParameter(BSSQueryConstants.EFF_DATE, benStartDt);
		List<Object[]> results = DaoUtils.getResultList(query, "BANDCODES_BY_COMPANY_ID_AND_EFFDT");
		List<CompanyBandCodes> companyBandcodes = new ArrayList<>();

		for (Object[] r : results) {
			CompanyBandCodes cbc = new CompanyBandCodes();
			cbc.setCompanyId(companyId);
			cbc.setBandCodeType((String) r[1]);
			cbc.setBandCodeValue((String) r[2]);
			companyBandcodes.add(cbc);
		}
		return companyBandcodes;
	}
	
	@Override
	public Date getProspectBandEffectiveDate(String prospectId, long realmId) {
		Query query = em.createNamedQuery(PROSPECT_BAND_EFFDT);
		query.setParameter(BSSQueryConstants.COMPANY_CODE, prospectId);
		query.setParameter(BSSQueryConstants.REALM_ID, realmId);
		Object result = DaoUtils.getSingleResult(query, PROSPECT_BAND_EFFDT);
		return (Date) result;
	}

	
	@Override
	public Date getProspectBandEffDate(String prospectId, long realmId) {
		try {
			Query query = em.createNamedQuery(PROSPECT_BAND_EFFDT);
			query.setParameter(BSSQueryConstants.COMPANY_CODE, prospectId);
			query.setParameter(BSSQueryConstants.REALM_ID, realmId);
			Object result = DaoUtils.getSingleResult(query, PROSPECT_BAND_EFFDT);
			return (Date) result;
		} catch (NoResultException nre) {
			return null;
		}
	}
}
