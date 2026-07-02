package com.trinet.ambis.persistence.dao.hrp.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.SchedTblDataDao;
import com.trinet.ambis.service.model.SchedTblAdminDto;
import com.trinet.ambis.util.DaoUtils;

public class SchedTblDataDaoImpl implements SchedTblDataDao {

	@PersistenceContext(unitName = "bis-hrp")
	private EntityManager em;

	@Override
	public List<SchedTblAdminDto> getSchedTblAdminDates(String companyCode, String quarter) {
		List<SchedTblAdminDto> returnList = new ArrayList<>();
		Query query = em.createNamedQuery("GET_COMPANY_SCHEDULE_ADMIN_DATES");
		query.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		query.setParameter(BSSQueryConstants.OE_QUARTER, quarter);
		List<Object[]> results = DaoUtils.getResultList(query, "GET_COMPANY_SCHEDULE_ADMIN_DATES");
		for (Object[] r : results) {
			SchedTblAdminDto schedTblAdminDto = new SchedTblAdminDto();
			schedTblAdminDto.setCompany(companyCode);
			schedTblAdminDto.setRealmYearId(((BigDecimal) r[1]).longValue());
			schedTblAdminDto.setPlanYearType((String) r[0]);
			schedTblAdminDto.setOeQuarter(quarter);
			schedTblAdminDto.setPlanYearStartDate((Date) r[2]);
			schedTblAdminDto.setPlanYearEndDate((Date) r[3]);
			schedTblAdminDto.setOpenDate((Date) r[4]);
			schedTblAdminDto.setCloseDate((Date) r[5]);
			schedTblAdminDto.setInternalOpenDate((Date) r[6]);
			schedTblAdminDto.setInternalCloseDate((Date) r[7]);
			schedTblAdminDto.setExtensionEndDate((Date) r[8]);
			returnList.add(schedTblAdminDto);
		}
		return returnList;
	}

}