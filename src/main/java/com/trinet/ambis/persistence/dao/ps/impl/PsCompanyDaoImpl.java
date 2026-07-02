package com.trinet.ambis.persistence.dao.ps.impl;

import com.trinet.ambis.aop.BSSCacheable;
import com.trinet.ambis.aop.CacheKey;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.enums.CacheObjectTypeEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.RealmTypeService;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.DaoUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;


public class PsCompanyDaoImpl implements PsCompanyDao {

	public static final String COMPANY_CENSUS_CREATED_DT = "COMPANY_CENSUS_CREATED_DT";

	private static final String IS_TERMED_COMPANY = "IS_TERMED_COMPANY";
	
	private static final String IS_TEXAS_SITUS = "IS_TEXAS_SITUS";
	
    @PersistenceContext(unitName = "bis-sysadm")
    private EntityManager entityManager;

    @PersistenceContext(unitName = "bis-hrp")
    private EntityManager hrpEntityManager;
    
    @Autowired 
    private RealmTypeService realmTypeService;


    private static final Logger logger = LoggerFactory.getLogger(PsCompanyDaoImpl.class);

    public void setEntityManager(EntityManager em) {
        this.entityManager = em;
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public EntityManager getHrpEntityManager() {
        return this.hrpEntityManager;
    }

    public void setHrpEntityManager(EntityManager em) {
        this.hrpEntityManager = em;
    }
    // This setter is used for junit test.
    public void setRealmTypeService(RealmTypeService realmTypeService) {
		this.realmTypeService = realmTypeService;
	}

	@Override
	@Transactional(value = "bisSysadmTransactionManager", propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	public void refreshCompanyCensus( String companyCode, long realmYearId ) {
		Query q = entityManager.createNativeQuery("BEGIN SEEKER.EE_BEN_CENSUS.refreshCompanyMidYear( :realmYearId , :company ); COMMIT; END; ");
		q.setParameter( BSSQueryConstants.REALM_YEAR_ID, realmYearId );
		q.setParameter( BSSQueryConstants.COMPANY, companyCode );
		DaoUtils.executeUpdate( q, "EE_BEN_CENSUS.refreshCompanyMidYear" );
	}

	@Override
	public Timestamp getCompanyCensusCreateDt( String companyCode, long realmYearId ) {
		Query q = hrpEntityManager.createNamedQuery( COMPANY_CENSUS_CREATED_DT );
		q.setParameter( BSSQueryConstants.COMPANY, companyCode );
		q.setParameter( BSSQueryConstants.REALM_YEAR_ID, realmYearId );
		return (Timestamp) DaoUtils.getSingleResult( q, COMPANY_CENSUS_CREATED_DT );
	}


    @Override
	@BSSCacheable(objectType = CacheObjectTypeEnum.BASIC_COMPANY_DETAILS, ttl = BSSApplicationConstants.TTL_FOR_STRATEGY_CACHE)
	public Company getBasicCompanyDetails(@CacheKey String code) {
		Company company = null;
		String sqlName = "BASIC_COMPANY_DETAILS";
		Query query = entityManager.createNamedQuery(sqlName);
		query.setParameter(BSSQueryConstants.COMPANY, code);
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(query);
		List<Object[]> list = DaoUtils.getResultList(query, sqlName);

		if (CollectionUtils.isEmpty(list)) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_PS_COMPANY_NOT_FOUND,
					BSSHttpStatusConstants.NOT_FOUND, PsCompanyDaoImpl.class.getName(),
					"No results found.", sqlName, queryMap));

		} else {
			for (Object[] tempValue : list) {
				if (ArrayUtils.isNotEmpty(tempValue)) {
					company = new Company();
					company.setCode(code);
					company.setPfClient((String) tempValue[0]);
					company.setLiveDate((String) tempValue[1]);
					company.setPlanStartDate((String) tempValue[2]);
					company.setQuater((String) tempValue[3]);
					String oeQuarterId = ((String) tempValue[3]);
					if (oeQuarterId != null) {
						company.setRealm(realmTypeService.findByQuarter(oeQuarterId));
					} else {
						throw new BSSApplicationException(new BSSApplicationError(
								BSSErrorResponseCodes.BSS_COMPANY_NOT_VAL_PEO, BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
								PsCompanyDaoImpl.class.getName(), "Invalid Quarter", sqlName, queryMap));
					}
					company.setPayrollProcessed(isPayConfirm(company.getCode()));
					String k1Value = (String) tempValue[5];
					company.setK1Company(k1Value != null && k1Value.equalsIgnoreCase(Constants.K1VALUE));
					if(null != tempValue[6]) {
						company.setBenefitProgram((String) tempValue[6]);
					}
				}
			}
		}
		return company;
	}
    
    @Override
	public Company getCompanyDetailsByEffdt(Company company, Date effdt) {
		String sqlName = "COMPANY_DETAILS_EFFDT";
		Query query = entityManager.createNamedQuery(sqlName);
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		query.setParameter("effdt", effdt);
		Map<String, Object> queryMap = DaoUtils.generateQueryMap(query);
		List<Object[]> list = DaoUtils.getResultList(query, sqlName);
		if (CollectionUtils.isEmpty(list)) {
			throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_COMPANY_NOT_FOUND,
					BSSHttpStatusConstants.NOT_FOUND, PsCompanyDaoImpl.class.getName(),
					"No results found.", sqlName, queryMap));
		} else {
			for (Object[] tempValue : list) {
				if (tempValue != null && tempValue.length > 0) {
					company.setPfClient((String) tempValue[0]);
					company.setLiveDate((String) tempValue[1]);
					company.setPlanStartDate((String) tempValue[2]);
					company.setBenefitStartDate((String) tempValue[2]);
					company.setQuater((String) tempValue[3]);
					logger.info("COMPANY QUARTER : {}", company.getQuater());
					try {
						company.setNaicsCode(Integer.parseInt((String) tempValue[4]));
					} catch (NumberFormatException e) {
						throw new BSSApplicationException(e,
								new BSSApplicationError(BSSErrorResponseCodes.BSS_COMPANY_NOT_VAL_NAICS,
										BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, PsCompanyDaoImpl.class.getName(),
										"Invalid NAICS CODE", sqlName, queryMap));
					}
					String oeQuarterId = ((String) tempValue[3]);
					if (oeQuarterId != null) {
						company.setRealm(realmTypeService.findByQuarter(oeQuarterId));
					}
					company.setBenefitProgram((String) tempValue[5]);
					company.setHeadQuatersState((String) tempValue[6]);
					BandCodes bandCodes = new BandCodes();
					bandCodes.setKaiserBandCode((String) tempValue[7]);
					bandCodes.setAetnaBandCode((String) tempValue[8]);
					bandCodes.setUhcBandCode((String) tempValue[9]);
					bandCodes.setBcbsBandCode((String) tempValue[10]);
					bandCodes.setBcbsNcBandCode((String) tempValue[11]);
					bandCodes.setKaisCoBandCode((String) tempValue[12]);
					bandCodes.setBsOfCaBandCode((String) tempValue[13]);
					bandCodes.setTuftsBandCode((String) tempValue[14]);
					bandCodes.setLifeBandCode((String) tempValue[15]);
					bandCodes.setDisBandCode((String) tempValue[16]);
					bandCodes.setBcOfIdBandCode((String) tempValue[22]);
					bandCodes.setKaisNwBandCode( (String) tempValue[23]);
					bandCodes.setBcbsMNBandCode( (String) tempValue[24]);
					bandCodes.setKaiMidAtlBandCode( (String) tempValue[25] );
					bandCodes.setKaiHawaiiBandCode( (String) tempValue[26] );
					bandCodes.setAetnaHmoBandCode( (String) tempValue[27] );
					bandCodes.setAetnaPpoBandCode( (String) tempValue[28] );
					bandCodes.setEmpireNYBand( (String) tempValue[29] );
					bandCodes.setHarvardBandCode( (String) tempValue[34] );
					bandCodes.setHighmarkBandCode((String) tempValue[27]);
					company.setBandCodes(bandCodes);
					company.setName((String) tempValue[17]);
					company.setDescription((String) tempValue[18]);
					company.setHeadQuatersCity((String) tempValue[19]);
					String aleQueryColumnValue = (String) tempValue[20];
					boolean isEligAle = aleQueryColumnValue != null && "Y".equalsIgnoreCase(aleQueryColumnValue);
					company.setEligAle(isEligAle);
					if (isEligAle) {
						company.setAleAmount(Constants.ALEAMOUNTHIGH);
					} else {
						company.setAleAmount(Constants.ALEAMOUNTLOW);
					}
					company.setCompanySetupDate((String) tempValue[21]);
					company.setPayrollProcessed(isPayConfirm(company.getCode()));
					company.setExclusiveMedPlan((String) tempValue[30]);
					String zipCode = (String) tempValue[31];
					if (null != zipCode && zipCode.length() > 5) {
						zipCode = zipCode.substring(0, 5);
					}
					company.setZipCode(zipCode);
					String texasSitusValue = (String) tempValue[32];
					company.setTexasSitus(Constants.TEXAS_SITUS_VALUE.equalsIgnoreCase(texasSitusValue));
					String k1Value = (String) tempValue[33];
					company.setK1Company(k1Value != null && k1Value.equalsIgnoreCase(Constants.K1VALUE));
				}
			}
		}
		return company;
	}

    @Override
    public boolean isBDMUser(String personid, String company) {
		try {
			Query query = hrpEntityManager.createNamedQuery("CHECK_IS_BDM_USER");
			query.setParameter(BSSQueryConstants.PERSON_ID, personid);
			query.setParameter(BSSQueryConstants.COMPANY, company);

			BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, "CHECK_IS_BDM_USER");
			logger.debug("NUMBER OF RECORDS : {}", count);

			return (count.intValue() > 0);

		} catch (NoResultException ex) {
            return false;
       }
    }

    @Override
	public boolean isCSAUser(String emplId, long realmId) {
		try {
			Query query = entityManager.createNamedQuery("CHECK_IS_CSA_USER");
			query.setParameter(BSSQueryConstants.PERSON_ID, emplId);
            query.setParameter(BSSQueryConstants.REALM_YEAR_ID, realmId);

			BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, "CHECK_IS_CSA_USER");
			logger.debug("NUMBER OF RECORDS : {}", count);

			return (count.intValue() > 0);

		} catch (NoResultException ex) {
            return false;
       }
	}


	@Override
	public boolean isActiveColleague( String emplid ) {
		return this.isActiveWithCompany( emplid, "001" );
	}

	@Override
	public boolean isActiveWithCompany( String emplid, String companyCode  ) {
		try {
			Query query = entityManager.createNamedQuery( "CHECK_IS_ACTIVE_WITH_COMPANY" );
			query.setParameter( "emplid", emplid );
			query.setParameter( BSSQueryConstants.COMPANY, companyCode );
			BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, "CHECK_IS_ACTIVE_WITH_COMPANY");

			return (count.intValue() > 0);
		} catch (NoResultException ex) {
			return false;
		}
	}



    @Override
    public boolean isBMGUser(String personid) {
        try {
            Query query = hrpEntityManager.createNamedQuery("CHECK_IS_BMG_USER");
            query.setParameter(BSSQueryConstants.PERSON_ID, personid);

            BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, "CHECK_IS_BMG_USER");
            logger.debug("NUMBER OF RECORDS : {}", count);

            return (count.intValue() > 0);

        } catch (NoResultException ex) {
            return false;
        }
    }
    
	@Override
	public boolean isTMTUser(String personid) {
		try {
			Query query = hrpEntityManager.createNamedQuery("CHECK_IS_TMT_USER");
			query.setParameter(BSSQueryConstants.PERSON_ID, personid);
			BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, "CHECK_IS_TMT_USER");
			logger.debug("NUMBER OF RECORDS : {}", count);
			return (count.intValue() > 0);
		} catch (NoResultException ex) {
			return false;
		}
	}
	
	@Override
	public boolean isBenCorpAdUser(String personid) {
		try {
			Query query = hrpEntityManager.createNamedQuery("CHECK_IS_BEN_CORP_AD_USER");
			query.setParameter("personid", personid);
			BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, "CHECK_IS_BEN_CORP_AD_USER");
			return count.intValue() > 0;
		} catch (NoResultException ex) {
			return false;
		}
	}
	
	@Override
	public boolean isBenAdvisorUser(String personid, String company) {
		try {
			Query query = hrpEntityManager.createNamedQuery("CHECK_IS_BENADVISOR_USER");
			query.setParameter("company", company);
			query.setParameter("personid", personid);
			BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, "CHECK_IS_BENADVISOR_USER");
			return count.intValue() > 0;
		} catch (NoResultException ex) {
			return false;
		}
	}
    
    @Override
    public int getCompanyActualHeadCount(String company) {
        try {
            Query query = entityManager.createNamedQuery("GET_ACTUAL_HEADCOUNT");
            query.setParameter(BSSQueryConstants.COMPANY, company);
            BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, "GET_ACTUAL_HEADCOUNT");
            logger.debug("NUMBER OF RECORDS : {}", count.intValue());
            return count.intValue();
        } catch (NoResultException ex) {
            return 0;
        }
    }
    
	@Override
	public boolean isPayConfirm(String company) {
		Query query = entityManager.createNamedQuery("IS_PAY_CONFIRMED");
		query.setParameter(BSSQueryConstants.COMPANY, company);
		String confirm = null;
		boolean returnValue = false;
		try {
			confirm = (String) DaoUtils.getSingleResult(query, "IS_PAY_CONFIRMED");
			returnValue = "YES".equalsIgnoreCase(confirm);
		} catch (NoResultException e) {
			logger.debug("No result forund for the company {}", confirm);
			returnValue = false;
		}
		return returnValue;
	}

	@Override
	public boolean isNewBandsAvailable(Company company, Date effDate) {
		Query query = entityManager.createNamedQuery("NEW_BANDS_EXIST");
		query.setParameter("effdt", effDate);
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		boolean isBandsExist = false;
		try {
			String result = (String) DaoUtils.getSingleResult(query, "NEW_BANDS_EXIST");
			isBandsExist = "TRUE".equals(result);
		} catch (NoResultException e) {
		}
		return isBandsExist;
	}
	
	@Override
	public List<CarrierMinimumFunding> getLowestCostPlanPerPlanCarrier(Company company) {
		Date benEffdt = CommonUtils.formatStringToDate(company.getPlanStartDate(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
		List<CarrierMinimumFunding> minFundings = new ArrayList<>();
		Query q = entityManager.createNamedQuery("MINIMUM_FUNDING_BASELINE");
		q.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		q.setParameter("hqState", company.getHeadQuatersState());
		q.setParameter("ben_effdt", benEffdt);
		q.setParameter("realmYrId", company.getRealmPlanYearId());
		q.setParameter("situs", CommonServiceHelper.getSitusValue(company.isTexasSitus()));
		List<Object[]> results = DaoUtils.getResultList(q, "MINIMUM_FUNDING_BASELINE");
		for (Object[] r : results) {
			String planType = (String) r[0];
			long carrierId = ((BigDecimal) r[1]).longValue();
			BigDecimal minFundingAmt = (BigDecimal) r[3];
			CarrierMinimumFunding minFunding = new CarrierMinimumFunding(carrierId, planType, minFundingAmt);
			minFundings.add(minFunding);
		}
		return minFundings;
	}

	@Override
	public Map<String, String> findCompaniesNames(Set<String> companyCodes) {
		Query q = entityManager.createNamedQuery("GET_PS_COMPANIES_NAMES");
		q.setParameter("companyCodes", companyCodes);

		@SuppressWarnings("unchecked")
		List<Object[]> results = q.getResultList();

		Map<String, String> compNames = new HashMap<>();
		for (Object[] result : results) {
			String code = (String) result[0];
			String name = (String) result[1];
			compNames.put(code, name);
		}
		return compNames;
	}
	
	/**
	 * Checks if given company is termed company or not
	 *
	 * @return true if termed company<br>
	 *         false if not termed company
	 */
	@Override
	public boolean isTermedCompany(String companyCode) {
		Query q = entityManager.createNamedQuery(IS_TERMED_COMPANY);
		q.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		return Boolean.parseBoolean((String) DaoUtils.getSingleResult(q, IS_TERMED_COMPANY));
	}

	@Override
	public boolean isTexasSitus(String companyCode, Date effDate) {
		Query q = entityManager.createNamedQuery(IS_TEXAS_SITUS);
		q.setParameter(BSSQueryConstants.COMPANY_CODE, companyCode);
		q.setParameter(BSSQueryConstants.EFF_DATE, effDate);
		return Boolean.parseBoolean((String) DaoUtils.getSingleResult(q, IS_TEXAS_SITUS));
	}
	
}
