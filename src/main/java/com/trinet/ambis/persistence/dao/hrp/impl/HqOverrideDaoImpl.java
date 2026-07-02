package com.trinet.ambis.persistence.dao.hrp.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.persistence.dao.hrp.HqOverrideDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.service.model.CompanyHQData;
import com.trinet.ambis.service.model.HqOverridesDto;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.DaoUtils;

public class HqOverrideDaoImpl implements HqOverrideDao {
	
	@PersistenceContext(unitName="bis-hrp")
	EntityManager em;

	@Autowired
	PsCompanyDao psCompanyDao;

	@Override
	public List<HqOverridesDto> getHqOverridesDetails(String companyCode, String quarter) {
		List<HqOverridesDto> hqOverridesList = new ArrayList<>();
		Query q = em.createNamedQuery("COMPANY_HQ_OVERRIDES");
		q.setParameter("filter_quarter", quarter);
		q.setParameter(BSSQueryConstants.COMPANY, companyCode);
		List<Object[]> results = DaoUtils.getResultList(q, "COMPANY_HQ_OVERRIDES");
		HqOverridesDto hqOverridesDto = null;
		if (results != null) {
			for (Object[] result : results) {
				hqOverridesDto = new  HqOverridesDto();
				hqOverridesDto.setCompanyName(setResultValue(result[1]));
				hqOverridesDto.setCompanyCode(setResultValue(result[0]));
				hqOverridesDto.setTermDate(setDateValue(result[2]));
				hqOverridesDto.setOeQuarter(setResultValue(result[3]));
				hqOverridesDto.setOverrideHqState(setResultValue(result[9]));
				hqOverridesDto.setOverrideHqZip(setResultValue(result[10]));
				hqOverridesDto.setPlanYearStart(setDateValue(result[5]));
				hqOverridesDto.setRealmYearId(Long.valueOf(result[4].toString()));
				hqOverridesDto.setPlanYearEnd(setDateValue(result[6]  ));
				hqOverridesDto.setState(setResultValue(result[7]));
				hqOverridesDto.setZip(setResultValue(result[8]));
				hqOverridesDto.setCanCopy(getBoolean(result[13]) );
				hqOverridesDto.setCanDelete(getBoolean(result[12]));
				hqOverridesDto.setCanEdit(getBoolean(result[11])); 
				hqOverridesDto.setHasStrategies(getBoolean(result[14]));
				hqOverridesDto.setNextRealmYearId(Long.valueOf(setResultIntValue(result[15])));
				hqOverridesList.add(hqOverridesDto);
			}
		}
		return hqOverridesList;
	}
	private boolean getBoolean(Object obj) {
		return "1".equals(setResultIntValue(obj).toString());
	}

	private String setResultValue(Object obj) {
		return obj == null ? "" : obj.toString();
	}
	private Integer setResultIntValue(Object obj) {
		return obj == null ? 0 : Integer.valueOf(obj.toString()) ;
	}
	private String setDateValue(Object obj) {
		return obj == null ? "" : CommonUtils.formatDateToString((Date)obj, BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY);
	}


	@Override
	public List<Map<String, Object>> getOverridesPlanYears(String companyCode) {
		Query query = em.createNamedQuery("GET_PLAN_YEAR_NEW_OVERRIDE");
		query.setParameter(BSSQueryConstants.COMPANY, companyCode);
		List<Object[]> resultList = DaoUtils.getResultList(query, "GET_PLAN_YEAR_NEW_OVERRIDE");
		List<Map<String, Object>> listOfObj = new ArrayList<>();
		if (resultList != null) {
			for (Object[] result : resultList) {
				Map<String, Object> obj = new HashMap<>();
				obj.put("planYearStart", result[1]);
				obj.put("planYearEnd", result[2]);
				obj.put("realmYearId", result[0]);
				listOfObj.add(obj);
			}
		}
		return listOfObj;
	}
    private Long convertToLong(Object o){
        String stringToConvert = String.valueOf(o);
        return Long.parseLong(stringToConvert);
        
    }
	@Override
	public List<CompanyHQData> getHqPlanYearData(String code) {
		List<CompanyHQData> returnList = new ArrayList<>();
		Query query = em.createNamedQuery("GET_COMPANY_HQ_FOR_PLAN_YEAR");
		query.setParameter(BSSQueryConstants.COMPANY, code);
		List<Object[]> results = DaoUtils.getResultList(query, "GET_COMPANY_HQ_FOR_PLAN_YEAR");
		for (Object[] result : results) {
			if (result[0] != null) {
				CompanyHQData companyRealmData = new CompanyHQData();
				companyRealmData.setCode(code);
				companyRealmData.setCompanyName((String) result[0]);
				companyRealmData.setRealmYearId(convertToLong(result[1]));
				companyRealmData.setOeQuarter((String) result[2]);
				companyRealmData.setPlanYearStartDate((Date) result[3]);
				companyRealmData.setPlanYearEndDate((Date) result[4]);
				companyRealmData.setState((String) result[5]);
				companyRealmData.setZip((String) result[6]); 
				companyRealmData.setHasStrategies(getBoolean(result[7])); 
				returnList.add(companyRealmData);
			}
		}
		return returnList;
	}
	
	/**
	 * @param em the em to set
	 */
	public void setEm(EntityManager em) {
		this.em = em;
	}


	/**
	 * @param psCompanyDao the psCompanyDao to set
	 */
	public void setPsCompanyDao(PsCompanyDao psCompanyDao) {
		this.psCompanyDao = psCompanyDao;
	}

}
