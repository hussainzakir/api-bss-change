package com.trinet.ambis.persistence.dao.ps.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.RollbackException;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.exception.BSSBadDataException;
import com.trinet.ambis.service.BandCodesService;
import com.trinet.ambis.service.BenefitPlanService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.common.BSSQueryConstants;
import com.trinet.ambis.common.BSSRateType;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.ExcessOptionEnum;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.helper.CommonServiceHelper;
import com.trinet.ambis.helper.RenewalServiceHelper;
import com.trinet.ambis.helper.SubmitServiceHelper;
import com.trinet.ambis.persistence.dao.hrp.BenefitPlanDao;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyGroupDataDao;
import com.trinet.ambis.persistence.dao.ps.BenEligRules;
import com.trinet.ambis.persistence.dao.ps.HSAPlansDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsSubmitDataDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.GroupRate;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.SubmitPayload;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.persistence.sp.GetNextBenefitPlan;
import com.trinet.ambis.persistence.sp.NextRateTblID;
import com.trinet.ambis.persistence.template.model.XbssRealmPlyrPlan;
import com.trinet.ambis.service.BenefitOfferExceptionService;
import com.trinet.ambis.service.BenefitOptionsService;
import com.trinet.ambis.service.BenefitProgramService;
import com.trinet.ambis.service.FlexRateService;
import com.trinet.ambis.service.model.FlexRateResponse;
import com.trinet.ambis.service.model.FlexRateResponseMapper;
import com.trinet.ambis.service.model.PayInRateInfo;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.HSAPlanService;
import com.trinet.ambis.service.PlanRatesService;
import com.trinet.ambis.service.RealmPlanYearService;
import com.trinet.ambis.service.RealmPlyrPlanService;
import com.trinet.ambis.service.StrategyGroupService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.StrategySyncService;
import com.trinet.ambis.service.SubmitStatusService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.email.dto.CompanyAndConfNumberDto;
import com.trinet.ambis.service.email.dto.SubmissionEmailDto;
import com.trinet.ambis.service.impl.BenefitOptionsServiceImpl;
import com.trinet.ambis.service.impl.BenefitProgramServiceImpl;
import com.trinet.ambis.service.impl.HSAPlanMapping;
import com.trinet.ambis.service.model.AdditionalBenefitOffer;
import com.trinet.ambis.service.model.AdditionalBenefitPlan;
import com.trinet.ambis.service.model.BandCodes;
import com.trinet.ambis.service.model.BenefitGroupRateMapper;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitOfferSummary;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.BenefitPlanRate;
import com.trinet.ambis.service.model.BenefitPlanRatesData;
import com.trinet.ambis.service.model.BnRateData;
import com.trinet.ambis.service.model.CarrierMinimumFunding;
import com.trinet.ambis.service.model.CoverageLevel;
import com.trinet.ambis.service.model.DisabilityBenefitOptionPlans;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.PlanRateData;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.service.model.StrategySummary;
import com.trinet.ambis.service.model.SubmissionInfo;
import com.trinet.ambis.service.prospect.enums.ProcessStatusEnum;
import com.trinet.ambis.service.submit.SubmitService;
import com.trinet.ambis.util.AppRulesAndConfigsUtils;
import com.trinet.ambis.util.ApplicationContextProvider;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.DaoUtils;
import com.trinet.ambis.util.DateUtils;
import com.trinet.ambis.util.RulesAndConfigsUtils;
import com.trinet.ambis.util.StrategyUtils;
import com.trinet.ambis.util.SubmitUtil;
import com.trinet.ambis.util.Utils;

public class PsSubmitDataDaoImpl implements PsSubmitDataDao {

    private static final String DEFAULT_PLAN_START_MONTH = "01";
    private static final String DEFAULT_PLAN_START_DAY = "01" ;
    private List<BnRateDataForInsert> batchOfBenPlanRateData;
	private List<ChangeCostRateForInsert> batchOfChangeCostRate;
	private List<BnRateTblForInsert> batchOfBnRateTbl;
	private List<BnRateData> batchOfSimpleBnRateData;
	private EntityManager psEntityManager;
	private EntityManager bssEM;
	private EmailGenService emailGenService;
	private CompanyService companyService;
	private StrategyService strategyService;
	private StrategySyncService strategySyncService;
	private RealmDataDao realmDataDao;
	private RealmPlyrPlanService realmPlyrPlanService;
	private BenefitPlanDao benefitPlanDao;
	private StrategyGroupService strategyGroupService;
	private SubmitStatusService submitStatusService;
	private PsCompanyDao psCompanyDao;
	private BenEligRules psBenEligRules;
	private String companyPlanStartDate;
	private BenefitOptionsService benefitOptionsCreator;
	private BenefitProgramService benefitProgramCreator;
	private HSAPlanService hsaPlanService;
	private HSAPlansDao hsaPlansDao;
	private StrategyDao strategyDao;
	private StrategyDataDao strategyDataDao;
	private StrategyGroupDataDao strategyGroupDataDao;
	private NextRateTblID nextRateTblId;
	private GetNextBenefitPlan nextBenefitPlan;
	private HrpDao hrpDao;
	private PlanRatesService planRatesService;
	private HSAPlanMapping hsaPlanMapping;
	private BenefitOfferExceptionService benOfferExceptionService;
	private RealmPlanYearService realmPlanYearService;
	private SubmitService submitService;
	private BenefitPlanService benefitPlanService;
	private BandCodesService bandCodesService;
	private FlexRateService flexRateService;
	private static final Logger logger = LoggerFactory.getLogger(PsSubmitDataDaoImpl.class);
	

	// SQL parameters, names and display constants
	public static final String CHANGE_COST_RATE = "CHANGE_COST_RATE";
	public static final String CHECK_A3_PLAN_EXISTS = "CHECK_A3_PLAN_EXISTS";
	public static final String CHECK_PS_BEN_DEFN_OPTN_RECORD_EXISTS = "CHECK_PS_BEN_DEFN_OPTN_RECORD_EXISTS";
	public static final String DELETE_BENEFIT_DEFN_COST_OF_TYPE_A3 = "DELETE_BENEFIT_DEFN_COST_OF_TYPE_A3";
	public static final String DELETE_BENEFIT_DEFN_OPTN_OF_TYPE_A3 = "DELETE_BENEFIT_DEFN_OPTN_OF_TYPE_A3";
	public static final String DELETE_BENEFIT_DEFN_OPTN_OF_TYPE_W = "DELETE_BENEFIT_DEFN_OPTN_OF_TYPE_W";
	public static final String DELETE_BENEFIT_DEFN_PLAN_OF_TYPE_A3 = "DELETE_BENEFIT_DEFN_PLAN_OF_TYPE_A3";
	public static final String DELETE_BENEFIT_PROGRAM = "DELETE_BENEFIT_PROGRAM";
	public static final String DELETE_FUTURE_PRCL_EFDT = "DELETE_FUTURE_PRCL_EFDT";
	public static final String DELETE_FUTURE_PRCL_PGM = "DELETE_FUTURE_PRCL_PGM";
	public static final String DELETE_FUTURE_RATE_DATA = "DELETE_FUTURE_RATE_DATA";
	public static final String DELETE_FUTURE_RATE_TBL = "DELETE_FUTURE_RATE_TBL";
	public static final String DELETE_PS_BN_RATE_DATA = "DELETE_PS_BN_RATE_DATA";
	public static final String DELETE_PS_EXC_CR_CALC_INC = "DELETE_PS_EXC_CR_CALC_INC";
	public static final String DELETE_PS_EXC_CR_CALC_TBL = "DELETE_PS_EXC_CR_CALC_TBL";
	public static final String DELETE_PS_T2_EE_BPG = "DELETE_PS_T2_EE_BPG";
	public static final String FIX_ELIG_NOT_OFFERED_PLAN_TYPE = "FIX_ELIG_NOT_OFFERED_PLAN_TYPE";
	public static final String GET_AMB_HDHP_HSA_PLANS = "GET_AMB_HDHP_HSA_PLANS";
	public static final String GET_DP_BENEFIT_PLANS = "GET_DP_BENEFIT_PLANS";
	public static final String GET_HDHP_HSA_PLANS = "GET_HDHP_HSA_PLANS";
	public static final String GET_LIFE_BENEFIT_PLANS = "GET_LIFE_BENEFIT_PLANS";
	public static final String GET_LIFE_DISB_COST_FOR_UPDATE = "GET_LIFE_DISB_COST_FOR_UPDATE";
	public static final String GET_LIFE_DISB_RATE_ID = "GET_LIFE_DISB_RATE_ID";
	public static final String GET_WAIVE_ROW_KEYS = "GET_WAIVE_ROW_KEYS";
	public static final String GET_INACTIVE_BENEFIT_PROGRAMS = "getInactiveBenefitPrograms";
	public static final String GET_SPECIFIC_GROUP_RATE = "getSpecificGroupRate";
	public static final String INSERT_BEN_DEFN_PGM = "INSERT_BEN_DEFN_PGM";
	public static final String INSERT_BEN_PLAN_RATE_DATA = "INSERT_BEN_PLAN_RATE_DATA";
	public static final String INSERT_BEN_PLAN_RATE_DATA_WITH_PAY_IN_RATE = "INSERT_BEN_PLAN_RATE_DATA_WITH_PAY_IN_RATE";
	public static final String INSERT_BENEFIT_DEFN_COST_OF_TYPE_A3 = "INSERT_BENEFIT_DEFN_COST_OF_TYPE_A3";
	public static final String INSERT_BENEFIT_DEFN_OPTN_OF_TYPE_A3 = "INSERT_BENEFIT_DEFN_OPTN_OF_TYPE_A3";
	public static final String INSERT_BENEFIT_DEFN_PLAN_OF_TYPE_A3 = "INSERT_BENEFIT_DEFN_PLAN_OF_TYPE_A3";
	public static final String INSERT_BN_RATE_TBL = "INSERT_BN_RATE_TBL";
	public static final String INSERT_COMPANY_BENEFIT_PLAN_YEAR = "INSERT_COMPANY_BENEFIT_PLAN_YEAR";
	public static final String INSERT_PRCL_BN_PGM = "INSERT_PRCL_BN_PGM";
	public static final String INSERT_PS_BEN_DEFN_OPTN = "INSERT_PS_BEN_DEFN_OPTN";
	public static final String INSERT_PS_EXC_CR_CALC_INC = "INSERT_PS_EXC_CR_CALC_INC";
	public static final String INSERT_PS_EXC_CR_CALC_TBL = "INSERT_PS_EXC_CR_CALC_TBL";
	public static final String INSERT_PS_T2_EE_BPG = "INSERT_PS_T2_EE_BPG";
	public static final String INSERT_SETUP_BEN_DEFN_OPTN = "INSERT_SETUP_BEN_DEFN_OPTN";
	public static final String INSERT_SETUP_BEN_DEFN_PLAN = "INSERT_SETUP_BEN_DEFN_PLAN";
	public static final String INSERT_SIMPLE_BN_RATE_DATA = "INSERT_SIMPLE_BN_RATE_DATA";
	public static final String INSERT_NEW_GROUP_RATE = "insertNewGroupRate";
	public static final String SELECT_XBSS_EMPLOYEE = "SELECT_XBSS_EMPLOYEE";
	public static final String SET_WAIVE_ALLOW_COST = "SET_WAIVE_ALLOW_COST";
	public static final String SET_WAIVE_ALLOW_RATE_DATA = "SET_WAIVE_ALLOW_RATE_DATA";
	public static final String SET_WAIVE_ALLOW_RATE_TBL = "SET_WAIVE_ALLOW_RATE_TBL";
	public static final String TURN_OFF_FSA_NON_SELECTED_BENEFIT_PLANS = "TURN_OFF_FSA_NON_SELECTED_BENEFIT_PLANS";
	public static final String TURN_OFF_LTD_NON_SELECTED_BENEFIT_PLANS = "TURN_OFF_LTD_NON_SELECTED_BENEFIT_PLANS";
	public static final String TURN_OFF_NON_SELECTED_BENEFIT_PLANS = "TURN_OFF_NON_SELECTED_BENEFIT_PLANS";
	public static final String TURN_OFF_STD_NON_SELECTED_BENEFIT_PLANS = "TURN_OFF_STD_NON_SELECTED_BENEFIT_PLANS";
	public static final String UPDATE_BEN_DEFN_OPTN_COST = "UPDATE_BEN_DEFN_OPTN_COST";
	public static final String UPDATE_BEN_SUPPLEMENT_COST = "UPDATE_BEN_SUPPLEMENT_COST";
	public static final String UPDATE_BENEFIT_DEFN_PLAN = "UPDATE_BENEFIT_DEFN_PLAN";
	public static final String UPDATE_CLIENT_OPTIONS = "UPDATE_CLIENT_OPTIONS";
	public static final String UPDATE_ELIG_RULE_FOR_WAIT_PER = "UPDATE_ELIG_RULE_FOR_WAIT_PER";
	public static final String UPDATE_LIFE_DISB_RATE_ID = "UPDATE_LIFE_DISB_RATE_ID";
	public static final String UPDATE_OPT2A = "UPDATE_OPT2A";
	public static final String UPDATE_OPTN2 = "UPDATE_OPTN2";
	public static final String UPDATE_PAYGROUP = "UPDATE_PAYGROUP";
	public static final String UPDATE_PLAN_EVENT_RULE = "UPDATE_PLAN_EVENT_RULE";
	public static final String UPDATE_WAIVE_ROW = "UPDATE_WAIVE_ROW";
	public static final String UPDATE_WAIVE_ROW_FOR_ADDITIONAL_BENEFITS = "UPDATE_WAIVE_ROW_FOR_ADDITIONAL_BENEFITS";
	public static final String WAIT_PER_EVENT_RULE = "WAIT_PER_EVENT_RULE";

	private static final String BEN_PROG = "benProg";
	private static final String BEN_PROG_FLAG = "benProgFlag";
	private static final String BENEFIT_PLAN = "benefitPlan";
	private static final String BATCH_ERR = "BSS_BATCH_ERR";
	private static final String BATCH_ERR_MSG = "Error executing batch SQL";
	private static final String CLIENT_RATE_ID = "clientRateID";
	private static final String EFF_DT_LOWER = "effdt";
	private static final String EXCL_PLAN = "exclPlan";
	private static final String GROUP_DESCR = "groupDescr";
	private static final String OE_QUARTER = "oeQuarter";
	private static final String PRODUCT_CODE = "productCode";
	private static final String QUARTER = "quarter";
	private static final String BUNDLE_NAME = "bundleName";
	private static final String RISK_TYPE = "riskType";
	private static final String START_DATE = "startDate";
	public static final String CHK_T2_BEN_PROG_GROUP = "hasMultipleBenefitPrograms";
	public static final String DELETE_PS_T2_MEAS_STAB = "DELETE_PS_T2_MEAS_STAB";
	public static final String INSERT_PS_T2_MEAS_STAB = "INSERT_PS_T2_MEAS_STAB";
	public static final String T2_EMPL_CLASS_TYPE = "ALL";
	public static final String T2_EMPL_CLASS_CODE = "ALL";
	public static final String T2_CLASS_NAME_DEFAULT = "DEFAULT";
	public static final String T2_STABILITY_12 = "12";
	public static final String T2_MEASUREMENT_12 = "12";
	public static final int STATUS_30 = 30;
	public static final String UPDATE_CLOPT_EFFDT = "UPDATE_CLOPT_EFFDT";
	public static final String UPDATE_BD_COMPANIES = "UPDATE_BD_COMPANIES";
	public static final String UPDATE_CLOPT_EFFDT_ALE_STATUS = "UPDATE_CLOPT_EFFDT_ALE_STATUS";

	public void createEntityManager() {
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		if (context != null) {
			logger.info("Context is not null");
			EntityManagerFactory ef = (EntityManagerFactory) context.getBean("bisSysadmEntityManagerFactory");
			if (ef != null) {
				psEntityManager = ef.createEntityManager();
			}
			ef = (EntityManagerFactory) context.getBean("bisHrpEntityManagerFactory");
			if (ef != null) {
				bssEM = ef.createEntityManager();
			}
			emailGenService = (EmailGenService) context.getBean("emailGenService");
			submitStatusService = (SubmitStatusService) context.getBean("submitStatusService");
			psCompanyDao = (PsCompanyDao) context.getBean("psCompanyDao");
			companyService = (CompanyService) context.getBean("companyService");
			strategyService = (StrategyService) context.getBean("strategyService");
			realmDataDao = (RealmDataDao) context.getBean("realmDataDao");
			realmPlyrPlanService = (RealmPlyrPlanService) context.getBean( "realmPlyrPlanService" );
			benefitPlanDao = (BenefitPlanDao) context.getBean("benefitPlanDao");
			benOfferExceptionService = (BenefitOfferExceptionService) context.getBean("benOfferExceptionService");
			strategyGroupService = (StrategyGroupService) context.getBean("strategyGroupService");
			strategyDao = (StrategyDao) context.getBean("strategyDao");
			strategyDataDao = (StrategyDataDao) context.getBean("strategyDataDao");
			strategyGroupDataDao = (StrategyGroupDataDao) context.getBean("strategyGroupDataDao");
			hrpDao = (HrpDao) context.getBean("hrpDao");
			planRatesService = (PlanRatesService) context.getBean("planRatesService");
			strategySyncService = (StrategySyncService) context.getBean("strategySyncService");
			nextRateTblId = (NextRateTblID) context.getBean("nextRateTblID");
			nextBenefitPlan = (GetNextBenefitPlan) context.getBean("spGetNextBenefitPlan");
			realmPlanYearService = (RealmPlanYearService) context.getBean("realmPlanYearService");
			hsaPlanService = (HSAPlanService) context.getBean("hsaPlanService");
			hsaPlansDao = (HSAPlansDao) context.getBean( "hsaPlansDao" );
			submitService = (SubmitService) context.getBean("submitService");
			benefitPlanService = (BenefitPlanService) context.getBean("benefitPlanService");
			bandCodesService = (BandCodesService) context.getBean("bandCodesService");
			flexRateService = (FlexRateService)  context.getBean("flexRateService");
		}
	}


	public void setEntityManager(EntityManager em) {
		this.psEntityManager = em;
	}

	public EntityManager getEntityManager() {
		return this.psEntityManager;
	}


	public void setBssEntityManager(EntityManager em) {
		this.bssEM = em;
	}

	public EntityManager getBssEntityManager() {
		return this.bssEM;
	}

	// below setters are required for junit test.
	public void setRealmDataDao(RealmDataDao realmDataDao) {
		this.realmDataDao = realmDataDao;
	}

	public void setCompanyService(CompanyService companyService) {
		this.companyService = companyService;
	}

	public void setBenefitPlanService(BenefitPlanService benefitPlanService) {
		this.benefitPlanService = benefitPlanService;
	}
	public void setRealmPlanYearService(RealmPlanYearService realmPlanYearService) {
		this.realmPlanYearService = realmPlanYearService;
	}
	public void setBandCodesService(BandCodesService bandCodesService) {
		this.bandCodesService = bandCodesService;
	}

	public void setFlexRateService(FlexRateService flexRateService) {
		this.flexRateService = flexRateService;
	}

	@Override
	public int insertBenefitSelectionEffectiveDate(Company company, StrategySummary strategySummary ) {
		Query query = psEntityManager.createNamedQuery(INSERT_COMPANY_BENEFIT_PLAN_YEAR);
		query.setParameter(PRODUCT_CODE, company.getRealm().getPeoid());
		query.setParameter(BSSQueryConstants.COMPANY_ID, company.getCode());
		query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		query.setParameter(START_DATE, companyPlanStartDate);
		query.setParameter(QUARTER, company.getQuater());
		query.setParameter(BSSQueryConstants.BUNDLE_ID,
				null != company.getBundleId() ? company.getBundleId() : BSSApplicationConstants.EMPTY_SPACE);
		query.setParameter(BUNDLE_NAME,
				null != company.getBundleName() ? company.getBundleName() : BSSApplicationConstants.EMPTY_SPACE);
		query.setParameter(RISK_TYPE,
				null != company.getRiskType() ? company.getRiskType().name() : BSSApplicationConstants.EMPTY_SPACE);

		String strategyName = strategySummary.getName();
		if (strategyName.length() > 254) {
			query.setParameter("strategyName", strategyName.substring(0, 254));
		}
		else {
			query.setParameter("strategyName", strategyName);
		}
		// "opted" in this context means "opt-in" and since the flag in PeopleSoft represents
		// "opt-out" the value will be reversed here.
		query.setParameter("acaFplOutOut", (( strategySummary.isAcaFplOpted() ) ? "N" : "Y" ) );
		
		int num = DaoUtils.executeUpdate(query, INSERT_COMPANY_BENEFIT_PLAN_YEAR);
		logger.info("NUMBER OF ITEMS INSERTED INTO PS_T2_PRCL_BN_EFDT : {}", num);
		return num;
	}
	

	private void deleteFuturePrclBn( Company company, BenefitGroup group ) {

		Query deletePgm = psEntityManager.createNamedQuery(DELETE_FUTURE_PRCL_PGM);
		deletePgm.setParameter( "peoId", company.getRealm().getPeoid() );
		deletePgm.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		deletePgm.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		deletePgm.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		deletePgm.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		int num = DaoUtils.executeUpdate(deletePgm, DELETE_FUTURE_PRCL_PGM);
		logger.info( "Number of records deleted from T2_PRCL_BN_PGM: {}", num );

		Query deleteEfdt = psEntityManager.createNamedQuery(DELETE_FUTURE_PRCL_EFDT);
		deleteEfdt.setParameter( "peoId", company.getRealm().getPeoid() );
		deleteEfdt.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		deleteEfdt.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		deleteEfdt.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		num = DaoUtils.executeUpdate(deleteEfdt, DELETE_FUTURE_PRCL_EFDT);
		logger.info("Number of records deleted from T2_PRCL_BN_EFDT: {}", num);
	}
	
	
	private void deleteAcaMeasurementAndStabilityPeriods(Company company) {
		Query query = psEntityManager.createNamedQuery(DELETE_PS_T2_MEAS_STAB);
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		int num = DaoUtils.executeUpdate(query, DELETE_PS_T2_MEAS_STAB);
		logger.info("Number of records deleted from PS_T2_MEAS_STAB: {}", num);
	}

	private void insertAceMeasurementAndStabilityPeriods(Company company, String aleTaxYear) {
		// Delete current employees for the company
		deleteAcaMeasurementAndStabilityPeriods(company);
        if(company.isEligAle()){
            // Prepare query to fetch employee data
            Query query = psEntityManager.createNamedQuery(INSERT_PS_T2_MEAS_STAB);
            query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
            query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
            query.setParameter(BSSQueryConstants.OE_QTR, company.getQuater());
            query.setParameter(BSSQueryConstants.PLAN_START_DATE, DateUtils.createDate(aleTaxYear + DEFAULT_PLAN_START_MONTH + DEFAULT_PLAN_START_DAY));
            query.setParameter(BSSQueryConstants.EFF_DT, DateUtils.getSystemDate());
            query.setParameter(BSSQueryConstants.EMPLOYEE_CLASS_TYPE, T2_EMPL_CLASS_TYPE);
            query.setParameter(BSSQueryConstants.EMPLOYEE_CLASS_CODE, T2_EMPL_CLASS_CODE);
            query.setParameter(BSSQueryConstants.EMPLOYEE_CLASS_NAME, T2_CLASS_NAME_DEFAULT);
            query.setParameter(BSSQueryConstants.MEASUREMENT, T2_MEASUREMENT_12);
            query.setParameter(BSSQueryConstants.STABILITY, T2_STABILITY_12);
            query.setParameter(BSSQueryConstants.ADMIN_PERIOD, STATUS_30);
            // Execute query and retrieve results
            DaoUtils.executeUpdate(query, INSERT_PS_T2_MEAS_STAB);
        }
	}
    
    /**
     *
     * @param company
     * @param match
     * @param strategyId
     */
	private void insertEmpBenefitGroup(Company company, boolean match, long strategyId) {
	    // Delete current employees for the company
	    deleteCurrentEmployees(company);

	    // Prepare query to fetch employee data
	    Query query = bssEM.createNamedQuery(SELECT_XBSS_EMPLOYEE);
	    query.setParameter("STRATEGY_ID", strategyId);
	    query.setParameter("IS_MATCH", Boolean.toString(match));

	    // Execute query and retrieve results
	    List<Object[]> results = DaoUtils.getResultList(query, SELECT_XBSS_EMPLOYEE);

	    // Insert each employee's benefit group
	    for (Object[] row : results) {
	        Query insertQuery = psEntityManager.createNamedQuery(INSERT_PS_T2_EE_BPG);

	        insertQuery.setParameter("COMPANY", row[0]);
	        insertQuery.setParameter("EMPLID", row[1]);
	        insertQuery.setParameter("EMPLRCD", row[2]);
	        insertQuery.setParameter("EFFDT", company.getPlanStartDate());
	        insertQuery.setParameter("BENEFITPROGRAM", row[6]);
	        insertQuery.setParameter("ELIGCONFIG1", row[5]);

	        DaoUtils.executeUpdate(insertQuery, INSERT_PS_T2_EE_BPG);
	    }
	}

	
	/**
	 * This method is for deleting the employees in the PS staging table for
	 * resubmits for the same effective date.
	 * 
	 * @param company
	 */
	private void deleteCurrentEmployees(Company company) {
		Query query = psEntityManager.createNamedQuery(DELETE_PS_T2_EE_BPG);
		query.setParameter("COMPANY", company.getCode());
		query.setParameter("EFFDT", company.getPlanStartDate());
		int results = DaoUtils.executeUpdate(query, DELETE_PS_T2_EE_BPG);
		logger.info("No of rows deleted :{}", results);
	}
	
	@Override
	public int insertBenefitProgramFunding(Company company, BenefitGroup group) {
		this.deleteBenefitProgram(company, group);
		Query query = psEntityManager.createNamedQuery(INSERT_PRCL_BN_PGM);
		query.setParameter(PRODUCT_CODE, company.getRealm().getPeoid());
		query.setParameter(BSSQueryConstants.COMPANY_ID, company.getCode());
		query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		query.setParameter(START_DATE, companyPlanStartDate);
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
		query.setParameter(BSSQueryConstants.GROUP_TYPE, group.getType());
		query.setParameter(BSSQueryConstants.GROUP_STATE, CommonUtils.validateParameter(group.getState()));
		
		int num = 0;
		boolean dnNotOffered = true;
		boolean vsNotOffered = true;
		boolean mdNotOffered = true;
		boolean addNotOffered = true;

		List<BenefitOffer> benefitOffers = group.getBenefitOffers();
		for (BenefitOffer benefitOffer : benefitOffers) {
			BenefitOfferSummary summary = benefitOffer.getSummary();
			PlanPackage planPkg = benefitOffer.getPlanPackage();

			if (BSSApplicationConstants.MEDICAL.equals(summary.getType())) {
				mdNotOffered = false;
				SubmitServiceHelper.updateMedicalFunding(query, planPkg);
			} else if (BSSApplicationConstants.DENTAL.equals(summary.getType())) {
				dnNotOffered = false;
				boolean employeePaid = SubmitUtil.isEmployeePaid(benefitOffer);
				SubmitServiceHelper.updateDentalFunding(query, planPkg, employeePaid);
			} else if (BSSApplicationConstants.VISION.equals(summary.getType())) {
				vsNotOffered = false;
				boolean employeePaid = SubmitUtil.isEmployeePaid(benefitOffer);
				SubmitServiceHelper.updateVisionFunding(query, planPkg, employeePaid);
			} else if (BSSApplicationConstants.ADDITIONAL.equals(summary.getType())) {
				addNotOffered = false;
				SubmitServiceHelper.updateAdditionalFunding(query, benefitOffer);
			}
		}

		if (mdNotOffered) {
			SubmitServiceHelper.updateEmptyMedicalFunding(query);
		}

		if (dnNotOffered) {
			SubmitServiceHelper.updateEmptyDentalFunding(query, BSSApplicationConstants.Y, BSSApplicationConstants.Y);
		}
		if (vsNotOffered) {
			SubmitServiceHelper.updateEmptyVisionFunding(query, BSSApplicationConstants.Y, BSSApplicationConstants.Y);
		}

		if (addNotOffered) {
			SubmitServiceHelper.updateAdditionalFunding(query, null);
		}

		num = DaoUtils.executeUpdate(query, INSERT_PRCL_BN_PGM);
		return num;

	}

	@Override
	public void updateBenefitStartDateAndQuarterForPlanYearSync(Company company, String newPlanStartDate) {
		try {
			createEntityManager();
			psEntityManager.getTransaction().begin();
			updateBenefitsStartDateForPlanYearSync(company, newPlanStartDate);
			updateBdCompanies(company, newPlanStartDate);
			psEntityManager.getTransaction().commit();
		} catch (Exception ex) {
			logger.error("Exception occured while updating benefit start date and quarter values");
			if (psEntityManager.getTransaction().isActive())
				psEntityManager.getTransaction().rollback();

		} finally {
			closeEntityManagers();
		}
	}
	
	private void updateBenefitsStartDateForPlanYearSync(Company company, String newPlanStartDate) {
		Query query = psEntityManager.createNamedQuery(UPDATE_CLOPT_EFFDT);
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		query.setParameter("oldPlanStartDate", company.getPlanStartDate());
		query.setParameter(BSSQueryConstants.PLAN_START_DATE, newPlanStartDate);
		query.setParameter(BSSQueryConstants.OE_QTR, company.getQuater());
		query.setParameter(BSSQueryConstants.OE_QTR_EXCE,
				Boolean.TRUE.equals(company.isBenefitsQuarterException()) ? Constants.YES : Constants.NO);
		query.setParameter(BSSQueryConstants.CMN_OWNER_COMP,
				company.getCommonOwnerCompanyCode() != null ? company.getCommonOwnerCompanyCode()
						: BSSQueryConstants.ORACLE_NULL);
		query.setParameter(BSSQueryConstants.PF_CLIENT,
				company.getPfClient() != null ? company.getPfClient() : BSSQueryConstants.ORACLE_NULL);
		DaoUtils.executeUpdate(query, UPDATE_CLOPT_EFFDT);
	}

	private int updateBdCompanies(Company company, String newPlanStartDate) {
		Query query = psEntityManager.createNamedQuery(UPDATE_BD_COMPANIES);
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		query.setParameter("oldPlanStartDate", company.getPlanStartDate());
		query.setParameter(BSSQueryConstants.PLAN_START_DATE, newPlanStartDate);
		return DaoUtils.executeUpdate(query, UPDATE_BD_COMPANIES);
	}
	
	/**
	 * Perform these inserts to create the company setup rows in the benefit program and
	 * return the effective date used for the setup.
	 * @param company
	 * @param group
	 * @return
	 */
	private String insertCompanySetupDate(Company company, BenefitGroup group) {
		logger.info( "Begin insertCompanySetupDate" );
		Date setupDate = Utils.convertStringToDate( company.getCompanySetupDate(), Constants.DATE_FORMAT );
		Date liveDate = Utils.convertStringToDate( company.getLiveDate(), Constants.DATE_FORMAT );
		String insertEffdt;
		
		if( setupDate.getTime() < liveDate.getTime() ) {
			insertEffdt = company.getCompanySetupDate();
		} else {
			insertEffdt = company.getLiveDate();
		}

		Query query2 = psEntityManager.createNamedQuery(INSERT_BEN_DEFN_PGM);
		query2.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query2.setParameter( BSSQueryConstants.EFF_DATE_STR, insertEffdt );
		query2.setParameter( GROUP_DESCR, group.getName() );
		query2.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		
		// set short descr to company name for Std groups
		// set short descr to CMP K1 for K1 groups
		if( "K1".equals( group.getType() )) {
			query2.setParameter( BSSQueryConstants.SHORT_DESCR, company.getCode().trim() + " K1" );
		} else {
			query2.setParameter( BSSQueryConstants.SHORT_DESCR, company.getCode().trim() + " " + group.getName().trim() );
		}

		query2.setParameter( BSSQueryConstants.CLONE_BEN_PROGRAM, company.getRealmPlanYear().getCloneProgram() );
		query2.setParameter( BSSQueryConstants.PLAN_START_DATE, this.companyPlanStartDate );

		// set a default value for excess credit option, in case there are no benefit offers
		query2.setParameter( BSSQueryConstants.EXCESS_CREDIT_OPTION, ExcessOptionEnum.CASH.getCode());

		for (BenefitOffer benefitOffer : group.getBenefitOffers()) {
			if (benefitOffer.getSummary().getType().equals(BSSApplicationConstants.MEDICAL)
					&& null != benefitOffer.getPlanPackage()
					&& BSSApplicationConstants.BSUPP.equals(benefitOffer.getPlanPackage().getFundingType())) {
				query2.setParameter( BSSQueryConstants.EXCESS_CREDIT_OPTION,
						ExcessOptionEnum.getCode(benefitOffer.getPlanPackage().getBsuppExcessOption().intValue()));
				break;   // exit for..loop once this flag is found
			}
		}
		DaoUtils.executeUpdate(query2, INSERT_BEN_DEFN_PGM);

		Query query4 = psEntityManager.createNamedQuery(INSERT_SETUP_BEN_DEFN_PLAN);
		query4.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query4.setParameter( BSSQueryConstants.EFF_DATE_STR, insertEffdt );
		query4.setParameter( BSSQueryConstants.CLONE_BEN_PROGRAM, company.getRealmPlanYear().getCloneProgram() );
		query4.setParameter( BSSQueryConstants.PLAN_START_DATE, this.companyPlanStartDate );
		DaoUtils.executeUpdate(query4, INSERT_SETUP_BEN_DEFN_PLAN);

		Query query6 = psEntityManager.createNamedQuery(INSERT_SETUP_BEN_DEFN_OPTN);
		query6.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		query6.setParameter( "companySetupDate", insertEffdt );
		query6.setParameter( BSSQueryConstants.ELIG_RULES_ID, group.getEligRuleId() );
		query6.setParameter( BSSQueryConstants.CLONE_BEN_PROGRAM, company.getRealmPlanYear().getCloneProgram() );
		query6.setParameter( BSSQueryConstants.PLAN_START_DATE, this.companyPlanStartDate );
		DaoUtils.executeUpdate(query6, INSERT_SETUP_BEN_DEFN_OPTN);

		return insertEffdt;
	}

	private Query prepareClientOptionsUpdate(Company company) {
		Query query = psEntityManager.createNamedQuery(UPDATE_CLIENT_OPTIONS);
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		if(null != companyPlanStartDate) {
			query.setParameter(BSSQueryConstants.EFF_DATE_STR, companyPlanStartDate);
		}else {
			query.setParameter(BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate());
		}
		
		// The following parameters are initialized to null. Set parameter
		// to a value to update that column in the table.
		query.setParameter(BSSQueryConstants.PLAN_START_DATE, BSSQueryConstants.ORACLE_NULL);
		query.setParameter(BSSQueryConstants.PRODUCT_LINE, BSSQueryConstants.ORACLE_NULL);
		query.setParameter(BSSQueryConstants.ALE_STATUS, BSSQueryConstants.ORACLE_NULL);
		query.setParameter(BSSQueryConstants.ALE_TAX_YEAR, BSSQueryConstants.ORACLE_NULL);
		query.setParameter(BSSQueryConstants.CMN_OWNER_COMP, BSSQueryConstants.ORACLE_NULL);
		query.setParameter(BSSQueryConstants.OE_QTR_EXCE, BSSQueryConstants.ORACLE_NULL);
		query.setParameter(BSSQueryConstants.OE_QTR, BSSQueryConstants.ORACLE_NULL);
		return query;
	}
	
	private void updateBenefitsStartDate( Company company ) {
		Query query = this.prepareClientOptionsUpdate( company );
		query.setParameter( BSSQueryConstants.PLAN_START_DATE, companyPlanStartDate );
		DaoUtils.executeUpdate(query, UPDATE_CLIENT_OPTIONS);
	}
	
	private void updateProductLine( Company company, String productLine ) {
		Query query = this.prepareClientOptionsUpdate( company );
		query.setParameter( BSSQueryConstants.PRODUCT_LINE, productLine );
		DaoUtils.executeUpdate(query, UPDATE_CLIENT_OPTIONS);
	}
	
	private void updateTaxYear(Company company, String taxYear) {
		Query query = this.prepareClientOptionsUpdate(company);
		query.setParameter(BSSQueryConstants.ALE_TAX_YEAR, taxYear);
		DaoUtils.executeUpdate(query, UPDATE_CLIENT_OPTIONS);
	}

	
	private void updateAleStatus(Company company) {
		Query query = psEntityManager.createNamedQuery(UPDATE_CLOPT_EFFDT_ALE_STATUS);
		String aleStatus = company.isEligAle() ? BSSApplicationConstants.Y : BSSApplicationConstants.N;
		query.setParameter(BSSQueryConstants.ALE_STATUS, aleStatus);
		query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		DaoUtils.executeUpdate(query, UPDATE_CLOPT_EFFDT_ALE_STATUS);
	}


	private String updatePaygroupNativeSQL() {
		StringBuilder sql = new StringBuilder();

		// NOTE: the space after each named parameter (e.g. dfltBenProgram) is intentional.
		// If the semi-colon follows without a space, the interpreter does not recognize the parameter.
		sql.append( "DECLARE" );
		sql.append( "   paygroupRec   PS_PAYGROUP_TBL%ROWTYPE;" );
		sql.append( "   vCompany      PS_PAYGROUP_TBL.COMPANY%TYPE           \\:= :company ;" );
		sql.append( "   effectDate    PS_PAYGROUP_TBL.EFFDT%TYPE             \\:= TO_DATE( :effdtStr, 'DD-MON-YYYY' );" );
		sql.append( "   vExclPayGrp   PS_PAYGROUP_TBL.PAYGROUP%TYPE          \\:= :exclPayGrp ;" );
		sql.append( "   vBenProgram   PS_PAYGROUP_TBL.DFLT_BEN_PROGRAM%TYPE  \\:= :dfltBenProgram ;" );

		sql.append( "   TYPE PaygrpEmpltypeTbl IS TABLE OF PS_PAYGRP_EMPLTYPE%ROWTYPE" );
		sql.append( "      INDEX BY BINARY_INTEGER;" );
		sql.append( "   paygrpEmpltypeRec   PaygrpEmpltypeTbl;" );

		sql.append( "   CURSOR cPaygroups IS" );
		sql.append( "      SELECT PAYGROUP" );
		sql.append( "        FROM PS_PAYGROUP_TBL T2A" );
		sql.append( "       WHERE T2A.COMPANY = vCompany" );
		sql.append( "         AND T2A.EFF_STATUS = 'A'" );
		sql.append( "         AND T2A.PAYGROUP <> vExclPayGrp" );
		sql.append( "         AND T2A.EFFDT = (" );
		sql.append( "             SELECT MAX(B.EFFDT)" );
		sql.append( "               FROM PS_PAYGROUP_TBL B" );
		sql.append( "              WHERE B.COMPANY = T2A.COMPANY" );
		sql.append( "                AND B.PAYGROUP = T2A.PAYGROUP" );
		sql.append( "                AND B.EFFDT <= effectDate ); " );

		sql.append( "BEGIN" );

		sql.append( "   FOR this IN cPaygroups LOOP" );
		sql.append( "      SELECT *" );
		sql.append( "        INTO paygroupRec" );
		sql.append( "        FROM PS_PAYGROUP_TBL PGP" );
		sql.append( "       WHERE PGP.COMPANY = vCompany" );
		sql.append( "         AND PGP.PAYGROUP = this.PAYGROUP" );
		sql.append( "         AND PGP.EFFDT = (" );
		sql.append( "             SELECT MAX(EFFDT)" );
		sql.append( "               FROM PS_PAYGROUP_TBL P1" );
		sql.append( "              WHERE P1.COMPANY = PGP.COMPANY" );
		sql.append( "                AND P1.PAYGROUP = PGP.PAYGROUP" );
		sql.append( "                AND P1.EFFDT <= effectDate );" );

		sql.append( "      IF paygroupRec.DFLT_BEN_PROGRAM = vBenProgram THEN" );
		sql.append( "         NULL;" );
		sql.append( "      ELSE" );
		sql.append( "         IF paygroupRec.EFFDT = effectDate THEN" );
		sql.append( "            UPDATE PS_PAYGROUP_TBL" );
		sql.append( "               SET DFLT_BEN_PROGRAM = vBenProgram" );
		sql.append( "             WHERE COMPANY = vCompany" );
		sql.append( "               AND PAYGROUP = this.PAYGROUP" );
		sql.append( "               AND EFFDT = effectDate;" );
		sql.append( "         ELSE" );
		sql.append( "            SELECT *" );
		sql.append( "              BULK COLLECT INTO paygrpEmpltypeRec" );
		sql.append( "              FROM PS_PAYGRP_EMPLTYPE PGE" );
		sql.append( "             WHERE PGE.COMPANY = vCompany" );
		sql.append( "               AND PGE.PAYGROUP = this.PAYGROUP" );
		sql.append( "               AND PGE.EFFDT = paygroupRec.EFFDT;" );

		sql.append( "            paygroupRec.EFFDT \\:= effectDate;" );
		sql.append( "            paygroupRec.DFLT_BEN_PROGRAM \\:= vBenProgram;" );
		sql.append( "            INSERT INTO PS_PAYGROUP_TBL VALUES paygroupRec;" );

		sql.append( "            FOR i IN 1 .. paygrpEmpltypeRec.COUNT  LOOP" );
		sql.append( "               paygrpEmpltypeRec( i ).EFFDT \\:= effectDate;" );
		sql.append( "               INSERT INTO PS_PAYGRP_EMPLTYPE VALUES paygrpEmpltypeRec( i );" );
		sql.append( "            END LOOP;" );
		sql.append( "         END IF;" );
		sql.append( "      END IF;" );
		sql.append( "   END LOOP; " );
		sql.append( "END;" );

		return sql.toString();
	}
	/**
	 * Sets the default benefit program on PS_PAYGROUP_TBL
	 * @param company
	 * @param excludePyGrp Expected usage: send " " to update all paygroups for new clients
	 *  and send "NP" to exclude the no-pay paygroup for renewals
	 */
	private void updatePaygroup( Company company, String excludePyGrp ) {

		Query query = psEntityManager.createNativeQuery( this.updatePaygroupNativeSQL() );
		query.setParameter( "dfltBenProgram", company.getBenefitProgram() );
		query.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		query.setParameter( "exclPayGrp", excludePyGrp );
		query.setParameter( BSSQueryConstants.EFF_DATE_STR, companyPlanStartDate );
		DaoUtils.executeUpdate(query, UPDATE_PAYGROUP);
	}
	
	private int updateBenDefnOptn(Company company, BenefitGroup group) {
		int num = 0;
		Set<String> list = null;
		Set<String> dpPlans = null;
		Set<String> autoSelectedPlans = new HashSet<>();
		Set<String> autoSelectedDp = null;
		Map<String, Object> queryMap = null;
		try {
			Query query = psEntityManager.createNamedQuery(TURN_OFF_NON_SELECTED_BENEFIT_PLANS);
			query.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram());
			query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
			list = this.getBenefitPlans(company, group);
			dpPlans = this.getDPPlansForUISelectedPlans(company, group, list);
			// getting auto selected plans
			boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
			if (isPickChoose) {
				Map<String, Set<String>> planTypeSelectedPlans = SubmitServiceHelper.getSelectedBenefitPlans(group);
				if (MapUtils.isNotEmpty(planTypeSelectedPlans)) {
					Set<String> allSelectedPlans = new HashSet<>();
					for (Map.Entry<String, Set<String>> planTypeEntry : planTypeSelectedPlans.entrySet()) {
						allSelectedPlans.addAll(planTypeEntry.getValue());
					}
					Map<String, Map<String, List<String>>> planTypeAutoSelectPlans = realmDataDao
							.getAutoSelectedPlansByRegion(allSelectedPlans, company.getRealmPlanYear().getId());
					for (Map.Entry<String, Map<String, List<String>>> planTypeEntry : planTypeAutoSelectPlans
							.entrySet()) {
						for (String region : planTypeEntry.getValue().keySet()) {
							autoSelectedPlans.addAll(planTypeEntry.getValue().get(region));
						}
					}
				}
			}

			if (CollectionUtils.isNotEmpty(autoSelectedPlans)) {
				autoSelectedDp = this.getDPPlansForUISelectedPlans(company, group, autoSelectedPlans);
			}
		
			if( CollectionUtils.isNotEmpty( list ) ) {
				query.setParameter( "list", list );
			} else {
				query.setParameter( "list", BSSApplicationConstants.EMPTY_SPACE );
			}
			if (CollectionUtils.isNotEmpty(autoSelectedPlans)) {
				query.setParameter("autoSelectedPlans", autoSelectedPlans);
			} else {
				query.setParameter("autoSelectedPlans", BSSApplicationConstants.EMPTY_SPACE);
			}
			if (dpPlans != null && !dpPlans.isEmpty()) {
				query.setParameter("dpPlans", dpPlans);
			} else {
				query.setParameter("dpPlans", BSSApplicationConstants.EMPTY_SPACE);
			}
			if (CollectionUtils.isNotEmpty(autoSelectedDp)) {
				query.setParameter("autoSelectedDp", autoSelectedDp);
			} else {
				query.setParameter("autoSelectedDp", BSSApplicationConstants.EMPTY_SPACE);
			}
			queryMap = DaoUtils.generateQueryMap(query);

			num = DaoUtils.executeUpdate(query, TURN_OFF_NON_SELECTED_BENEFIT_PLANS);
			logger.info("NUMBER OF SELECTED PLANS UPDATED : {}", num);
		} catch (Exception e) {
			throw new BSSApplicationException(e, new BSSApplicationError("", BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
					PsSubmitDataDaoImpl.class.getName(), "", TURN_OFF_NON_SELECTED_BENEFIT_PLANS, queryMap));
		}

		fixDentalVisionEligRules( company, group );
		return num;
	}

	private void fixDentalVisionEligRules( Company company, BenefitGroup group ) {
		final String OPTIONAL = "optional";
		final String GROUP = "group";
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();
		String dentalOffered = null;
		String visionOffered = null;
		for( BenefitOffer benefitOffer : benefitOffers ) {
			switch( benefitOffer.getSummary().getType() ) {
			case BSSApplicationConstants.DENTAL:
				dentalOffered = SubmitUtil.isEmployeePaid( benefitOffer ) ? OPTIONAL : GROUP;
				break;
			case BSSApplicationConstants.VISION:
				visionOffered = SubmitUtil.isEmployeePaid( benefitOffer ) ? OPTIONAL : GROUP;
				break;
			default:
				break;
			}
		}

		final String TURN_OFF = "1";
		final String LEAVE = "0";
		String groupDentalSwitch = LEAVE;
		String optionalDentalSwitch = LEAVE;
		String groupVisionSwitch = LEAVE;
		String optionalVisionSwitch = LEAVE;

		if( dentalOffered == null ) {
			groupDentalSwitch = optionalDentalSwitch = TURN_OFF;
		} else {
			switch( dentalOffered ) {
			case OPTIONAL:
				groupDentalSwitch = TURN_OFF;
				break;
			case GROUP:
				optionalDentalSwitch = TURN_OFF;
				break;
			default:
				break;
			}
		}

		if( visionOffered == null ) {
			groupVisionSwitch = optionalVisionSwitch = TURN_OFF;
		} else {
			switch( visionOffered ) {
			case OPTIONAL:
				groupVisionSwitch = TURN_OFF;
				break;
			case GROUP:
				optionalVisionSwitch = TURN_OFF;
				break;
			default:
				break;
			}
		}

		this.updateNotOfferedEligRules( group.getBenefitProgram(), companyPlanStartDate,
				groupDentalSwitch, optionalDentalSwitch, groupVisionSwitch, optionalVisionSwitch );
	}

	public int updateNotOfferedEligRules( String benefitProgram, String effdtStr, 
			String groupDental, String optionalDental, String groupVision, String optionalVision ) {
		Query updateElig = psEntityManager.createNamedQuery( FIX_ELIG_NOT_OFFERED_PLAN_TYPE );
		updateElig.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram );
		updateElig.setParameter( BSSQueryConstants.EFF_DATE_STR, effdtStr );
		updateElig.setParameter( BSSQueryConstants.GROUP_DENTAL, groupDental );
		updateElig.setParameter( BSSQueryConstants.OPTIONAL_DENTAL, optionalDental );
		updateElig.setParameter( BSSQueryConstants.GROUP_VISION, groupVision );
		updateElig.setParameter( BSSQueryConstants.OPTIONAL_VISION, optionalVision );
		int num = DaoUtils.executeUpdate( updateElig, FIX_ELIG_NOT_OFFERED_PLAN_TYPE );
		return num;
	}


	@Override
	public int updateSTDBenefitDefitionOption(Company company, BenefitGroup group) {
		Set<String> list = this.getStdBenefitPlans(company, group);
		logger.info("NUMBER OF SELECTED STD BENEFIT PLANS : {}", list);
		// if STD is not selected, then we have to change elig rule for the previous year STD plans, to satisfy this scenario 
		// we added dummy entry here to make sure list is not empty
		list.add(" ");
		if (!list.isEmpty()) {
		   Query query = psEntityManager.createNamedQuery(TURN_OFF_STD_NON_SELECTED_BENEFIT_PLANS);
		   query.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram());
		   query.setParameter(EFF_DT_LOWER, companyPlanStartDate);		   		
		   query.setParameter("list", list);
		             
		   int num = DaoUtils.executeUpdate(query, TURN_OFF_STD_NON_SELECTED_BENEFIT_PLANS);
		   logger.info("NUMBER OF ROWS UPDATED IN PS_BEN_DEFN_OPTN : {} FOR STD", num);
		   return num;
		}
		else {
			logger.info("STD is not offered");
			return 0;
		}
	}
	
	
	public int updateLTDBenefitDefitionOption(BenefitGroup group) {
		Set<String> list = this.getLtdBenefitPlans(group);
		// if LTD is not selected, then we have to change elig rule for the previous year LTD plans, to satisfy this scenario 
		// we added dummy entry here to make sure list is not empty
		list.add(" ");
		logger.info("NUMBER OF SELECTED LTD BENEFIT PLANS : {}", list);
		if (!list.isEmpty()) {
		   Query query = psEntityManager.createNamedQuery(TURN_OFF_LTD_NON_SELECTED_BENEFIT_PLANS);
		   query.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram());
		   query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
		   query.setParameter("list", list);
		   int num = DaoUtils.executeUpdate(query, TURN_OFF_LTD_NON_SELECTED_BENEFIT_PLANS);
		   logger.info("NUMBER OF ROWS UPDATED IN PS_BEN_DEFN_OPTN : {} FOR LTD", num);
		   return num;
		}
		else {
			logger.info("LTD is not offered");
			return 0;
		}
	}
	
	private void updateFSABenefitDefitionOption(Company company, BenefitGroup group, List<String> strategyPortfolios) {
		if (CollectionUtils.isNotEmpty(strategyPortfolios)) {
			List<String> fsaPlans = strategyGroupDataDao.getPortfolioFsaPlans(company, strategyPortfolios);
			if (CollectionUtils.isNotEmpty(fsaPlans)) {
				Query query = psEntityManager.createNamedQuery(TURN_OFF_FSA_NON_SELECTED_BENEFIT_PLANS);
				query.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram());
				query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
				query.setParameter("list", fsaPlans);
				DaoUtils.executeUpdate(query, TURN_OFF_FSA_NON_SELECTED_BENEFIT_PLANS);
			}
		}
	}

	private int updateEligRuleForWaitPeriod(Company company, BenefitGroup group) {

		Query eligRuleUpdate = psEntityManager.createNamedQuery( UPDATE_ELIG_RULE_FOR_WAIT_PER );
		eligRuleUpdate.setParameter( BSSQueryConstants.COMPANY, company.getCode() );
		eligRuleUpdate.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		eligRuleUpdate.setParameter( BSSQueryConstants.EFF_DATE_STR, companyPlanStartDate );
		eligRuleUpdate.setParameter( BEN_PROG, group.getBenefitProgram() );
		eligRuleUpdate.setParameter( OE_QUARTER, company.getQuater() );
		eligRuleUpdate.setParameter( "newWaitPeriod", group.getWaitingPeriod() );
		int updateRowCount = DaoUtils.executeUpdate(eligRuleUpdate, UPDATE_ELIG_RULE_FOR_WAIT_PER);
		logger.info("<<<< updateEligRuleForWaitPeriod - returning {}", updateRowCount );
		return updateRowCount;
	}


	private int updateEventRuleForWaitPeriod(Company company, BenefitGroup group) {
		Map<String, String[]> waitPlanEventMap = new HashMap<>();
		HashSet<String> planTypeSet = new HashSet<>();
		planTypeSet.add( "  " );  //ensure list contains at least one valid String

		// later, this method will need to know whether disability is employee paid or not
		// determine that now by examining the benefit offers
		boolean employeePaidSTD = false;
		boolean employeePaidLTD = false;

		// loop through collection of benefit offers...
		for( BenefitOffer offer : group.getBenefitOffers() ) {
			// ...to identify the sub-collection of additional benefits...
			for( AdditionalBenefitOffer addlOffer : offer.getAdditionalBenefitOffers() ) {
				// ...and finally determine whether disability plans are employee paid
				if (addlOffer.getSummary().getType().equals("DISABILITY")) {
					// Get option plans
					List<AdditionalBenefitPlan> planOptions = addlOffer.getAdditionalBenefitPlans();
					for(AdditionalBenefitPlan option : planOptions) {
						List<DisabilityBenefitOptionPlans> plans = option.getOptionPlans();
						for(DisabilityBenefitOptionPlans plan : plans) {
							if (plan.isEmployeePaid()) {
								if (plan.getPlanType().equals(Constants.STD_CODE) ) {
									employeePaidSTD = true;
								} else if (plan.getPlanType().equals(Constants.LTD_CODE)) {
									employeePaidLTD = true;
								}
							}
						}
					}
				}
			}
		}


		logger.info( "employeePaidSTD: {}", employeePaidSTD );
		logger.info( "employeePaidLTD: {}", employeePaidLTD );


		// run a query to get all the event rules for the group waiting period...
		Query eventRuleQuery = psEntityManager.createNamedQuery(WAIT_PER_EVENT_RULE);
		eventRuleQuery.setParameter(OE_QUARTER, company.getQuater());
		eventRuleQuery.setParameter(EFF_DT_LOWER, companyPlanStartDate);

		List<Object[]> results = DaoUtils.getResultList(eventRuleQuery, WAIT_PER_EVENT_RULE);

		// ...and save the results in the map
		for( Object[] row : results ) {
			String planType = (String) row[0];
			planTypeSet.add( planType );
			String waitPer = (String) row[1];
			//for disability: first value is employee-paid, second is company-paid
			String[] eventRules = { (String) row[2], (String) row[3] };
			waitPlanEventMap.put( planType + waitPer, eventRules );
		}

		int updateRowCount = 0;

		// lookup map for plan type and wait period to find the event rule
		for( String planType : planTypeSet ) {
			
			// try to find an event rule using the current waiting period code
			String[] newEventRules = waitPlanEventMap.get( planType + group.getWaitingPeriod() );
			if( newEventRules == null ) {
				// if no event rule was found, substitute "NONE" and "OTHR" for the current code
				String waitPerCode = StringUtils.upperCase( group.getWaitingPeriod() );
				if( " ".equals( waitPerCode ) || "NONE".equals( waitPerCode) || waitPerCode == null) {
					waitPerCode = "NONE";
				} else {
					waitPerCode = "OTHR";
				}
				newEventRules = waitPlanEventMap.get( planType + waitPerCode );
			}
			
			// if an event rule array was found, go update the PLAN record for this plan type
			if( newEventRules != null ) {
				Query updateEvent = psEntityManager.createNamedQuery(UPDATE_PLAN_EVENT_RULE);
				updateEvent.setParameter( BEN_PROG, group.getBenefitProgram() );
				updateEvent.setParameter( EFF_DT_LOWER, companyPlanStartDate );
				updateEvent.setParameter( BSSQueryConstants.PLAN_TYPE, planType );

				// the first event rule is the new value, but also test...
				String newEventRule = newEventRules[0];

				// ...whether employee-paid or company-paid event should be used for disability
				if( BSSApplicationConstants.STD_CODE.equals( planType ) ) {
					if( employeePaidSTD ) {
						newEventRule = newEventRules[0];
					} else {
						newEventRule = newEventRules[1];
					}
				}
				if( BSSApplicationConstants.LTD_CODE.equals( planType ) ) {
					if( employeePaidLTD ) {
						newEventRule = newEventRules[0];
					} else {
						newEventRule = newEventRules[1];
					}
				}

				updateEvent.setParameter( "eventRule", newEventRule );
				updateRowCount += DaoUtils.executeUpdate(updateEvent, UPDATE_PLAN_EVENT_RULE);

			} else {
				logger.info( "Could not find valid event rules for {} and {}", planType, group.getWaitingPeriod() );
			}
		}

		return updateRowCount;
	}


	private int setClientBenefitOptionFlags(Company company, BenefitGroup group, Long strategyId) {
		// update client benefit option flags; set status and the old funding
		// option
		Query qryOptn2 = psEntityManager.createNamedQuery(UPDATE_OPTN2);
		qryOptn2.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		qryOptn2.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		qryOptn2.setParameter(BSSQueryConstants.EFF_DATE_STR, companyPlanStartDate);
		qryOptn2.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
		qryOptn2.setParameter(BSSQueryConstants.EFF_STATUS, "A");
		qryOptn2.setParameter(BSSQueryConstants.T2_FUNDING_OPTN, "SPC");
		qryOptn2.setParameter(BSSQueryConstants.T2_FORFEIT_EXC_CR, "N");
		qryOptn2.setParameter(BSSQueryConstants.T2_BENSUPP_NB, BigDecimal.ZERO);
		qryOptn2.setParameter(BSSQueryConstants.T2_BENSUPP_EE, BigDecimal.ZERO);
		qryOptn2.setParameter(BSSQueryConstants.T2_BENSUPP_SP, BigDecimal.ZERO);
		qryOptn2.setParameter(BSSQueryConstants.T2_BENSUPP_DEP, BigDecimal.ZERO);
		qryOptn2.setParameter(BSSQueryConstants.T2_BENSUPP_FAM, BigDecimal.ZERO);


		// find benefit supplement and set the parameters if found
		for (BenefitOffer benefitOffer : group.getBenefitOffers()) {
			if (benefitOffer.getSummary().getType().equals("medical") && null != benefitOffer.getPlanPackage()
					&& BSSApplicationConstants.BSUPP.equals(benefitOffer.getPlanPackage().getFundingType())) {
				// set funding type to BS
				qryOptn2.setParameter(BSSQueryConstants.T2_FUNDING_OPTN, "BS");
				// set excess credit flag
				qryOptn2.setParameter(BSSQueryConstants.T2_FORFEIT_EXC_CR, this.getForfeitExcFlag( benefitOffer ) );
				// set benefit supplement amounts
				BigDecimal wvrAllow = checkNotNull( benefitOffer.getPlanPackage().getWaiverAllowance() );
				qryOptn2.setParameter(BSSQueryConstants.T2_BENSUPP_NB, wvrAllow );
				BigDecimal bs1 = checkNotNull( benefitOffer.getPlanPackage().getCoverageLevelFunding().get( CoverageCodesEnums.COV_EMPLOYEE.getId() ));
				BigDecimal bs2 = checkNotNull( benefitOffer.getPlanPackage().getCoverageLevelFunding().get( CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId() ));
				BigDecimal bsC = checkNotNull( benefitOffer.getPlanPackage().getCoverageLevelFunding().get( CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId() ));
				BigDecimal bs4 = checkNotNull( benefitOffer.getPlanPackage().getCoverageLevelFunding().get( CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId() ));
				qryOptn2.setParameter(BSSQueryConstants.T2_BENSUPP_EE, bs1 );
				qryOptn2.setParameter(BSSQueryConstants.T2_BENSUPP_SP, bs2 );
				qryOptn2.setParameter(BSSQueryConstants.T2_BENSUPP_DEP, bsC );
				qryOptn2.setParameter(BSSQueryConstants.T2_BENSUPP_FAM, bs4 );

				break;
			}
		}

		int num = DaoUtils.executeUpdate(qryOptn2, UPDATE_OPTN2);

		// get exclusive med plan code values
		List<String> exclMedPlans = strategyGroupDataDao.getExclMedPlanPortfolio( strategyId, company.getRealmPlanYear().getId() );

		Query qryOpt2a = psEntityManager.createNamedQuery(UPDATE_OPT2A);
		qryOpt2a.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		qryOpt2a.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		qryOpt2a.setParameter(BSSQueryConstants.EFF_DATE_STR, companyPlanStartDate);
		qryOpt2a.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());

		if( !exclMedPlans.isEmpty()) {
			String exclMedPlan = exclMedPlans.get( 0 );
			if( "UHC".equals( exclMedPlan ) && "CA".equals( company.getHeadQuatersState() )) {
				exclMedPlan = "UHCA";
			}
			qryOpt2a.setParameter( EXCL_PLAN, exclMedPlan );
		} else {
			if ("FL".equals(company.getHeadQuatersState())) {
				qryOpt2a.setParameter(EXCL_PLAN, "OTHR");
			} else {
				qryOpt2a.setParameter(EXCL_PLAN, "DFLT");
			}
		}
		num += DaoUtils.executeUpdate(qryOpt2a, UPDATE_OPT2A);
		return num;
	}


	private String getForfeitExcFlag( BenefitOffer offer ) {
		String forfeit = ExcessOptionEnum.getCode( offer.getPlanPackage().getBsuppExcessOption().intValue() );
		return "F".equals( forfeit ) ? "Y" : "N";
	}


	private BigDecimal checkNotNull( BigDecimal bigD ) {
		if( bigD == null ) {
			return BigDecimal.ZERO;
		} else {
			return bigD;
		}
	}


	@Override
	public int deleteBenefitDefinitionPlanOfTypeA3(BenefitGroup group) {
		Query query = psEntityManager
				.createNamedQuery(DELETE_BENEFIT_DEFN_PLAN_OF_TYPE_A3);
		query.setParameter(BEN_PROG, group.getBenefitProgram());
		query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
		return DaoUtils.executeUpdate(query, DELETE_BENEFIT_DEFN_PLAN_OF_TYPE_A3);

	}

	@Override
	public int deleteBenefitDefinitionOptionOfTypeA3(BenefitGroup group) {
		Query query = psEntityManager
				.createNamedQuery(DELETE_BENEFIT_DEFN_OPTN_OF_TYPE_A3);
		query.setParameter(BEN_PROG, group.getBenefitProgram());
		query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
		
		return DaoUtils.executeUpdate(query, DELETE_BENEFIT_DEFN_OPTN_OF_TYPE_A3);
	}

	@Override
	public int deleteBenefitDefinitionCostOfTypeA3(BenefitGroup group) {
		Query query = psEntityManager
				.createNamedQuery(DELETE_BENEFIT_DEFN_COST_OF_TYPE_A3);
		query.setParameter(BEN_PROG, group.getBenefitProgram());
		query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
		
		return DaoUtils.executeUpdate(query, DELETE_BENEFIT_DEFN_COST_OF_TYPE_A3);
	}

	@Override
	public int updateBenefitDefitionPlan(Company company, List<String> planTypes, BenefitGroup group) {
		Query query = psEntityManager.createNamedQuery(UPDATE_BENEFIT_DEFN_PLAN);
		query.setParameter(BEN_PROG, group.getBenefitProgram());
		query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
		query.setParameter(BSSQueryConstants.PLAN_TYPES, planTypes);
		int num = DaoUtils.executeUpdate(query, UPDATE_BENEFIT_DEFN_PLAN);
		logger.info("NUMBER OF ROWS UPDATED IN PS_BEN_DEFN_PLAN : {}", num);
		return num;
	}

	@Override
	public int deleteBenefitDefinitionOptionOfTypeW(Company company, List<String> planTypes, BenefitGroup group) {
		Query query = psEntityManager.createNamedQuery(DELETE_BENEFIT_DEFN_OPTN_OF_TYPE_W);
		query.setParameter(BEN_PROG, group.getBenefitProgram());
		query.setParameter(EFF_DT_LOWER, companyPlanStartDate);		
		query.setParameter(BSSQueryConstants.PLAN_TYPES, planTypes);

		int num = DaoUtils.executeUpdate(query, DELETE_BENEFIT_DEFN_OPTN_OF_TYPE_W);
		logger.info("NUMBER OF WAVE ROWS DELETED FROM PS_BEN_DEFN_OPTN : {}", num);
		return num;
	}

	public Set<String> getBenefitPlans(Company company, BenefitGroup group) {
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();
		Set<String> benefitPlans = new TreeSet<>();
		boolean lifeOffered = false;		

		for (BenefitOffer benefitOffer : benefitOffers) {
			BenefitOfferSummary summary = benefitOffer.getSummary();
			// Exclude 1G, iH, A1, 60, 61, 4% (401K)
			// include medical, dental, vision, ID, 1V, A3, LTD, LIFE
			if (summary.getType().equals(BSSApplicationConstants.MEDICAL)
					|| summary.getType().equals(BSSApplicationConstants.DENTAL)
					|| summary.getType().equals(BSSApplicationConstants.VISION))
			{
				benefitPlans.addAll(SubmitUtil.getSelectedBenefitPlans(benefitOffer));
			}
			else if (summary.getType().equals(BSSApplicationConstants.ADDITIONAL)) {
				List<AdditionalBenefitOffer> additionalBenefits = 
						new ArrayList<>(benefitOffer.getAdditionalBenefitOffers());
				
				for (AdditionalBenefitOffer abDTOImpl : additionalBenefits) {
					BenefitOfferSummary summary1 = abDTOImpl.getSummary();
					List<AdditionalBenefitPlan> planOptions = abDTOImpl.getAdditionalBenefitPlans();
					if (summary1.getType().equals(BSSApplicationConstants.DISABILITY)) {
						// Get option plans						
						for(AdditionalBenefitPlan option : planOptions) {
						   List<DisabilityBenefitOptionPlans> plans = option.getOptionPlans();
						   for(DisabilityBenefitOptionPlans plan : plans) {
						       if (plan.getPlanType().equals(BSSApplicationConstants.STD_CODE) || plan.getPlanType().equals(BSSApplicationConstants.LTD_CODE)) {
						    	   benefitPlans.add(plan.getId());
						      }
						   }
					    }
					}			
					else if( abDTOImpl.getSummary().getType().equals(BSSApplicationConstants.CMTR) ) {
				    	  if (abDTOImpl.getAdditionalBenefitPlans() != null) {
				    	      for(AdditionalBenefitPlan plan : abDTOImpl.getAdditionalBenefitPlans()) {
				    	          benefitPlans.add(plan.getId());
				    	      }
				    	  }
				    }
				    else if (abDTOImpl.getSummary().getType().equals(BSSApplicationConstants.LIFE) ) {
				    	   lifeOffered = true;
				    	   benefitPlans.add(abDTOImpl.getAdditionalBenefitPlans().get(0).getId());
				       }
				   }			       
		       }
			
		}
		
		// Get Life optional benefit plans
		List<String> lifePlanTypes = realmDataDao.getLifeSupplementalPlanTypes(company.getRealmPlanYear().getId());
		if (lifeOffered) {
		   benefitPlans.addAll(getLifeBenefitPlans(company, group, lifePlanTypes));
		}
		
		if (!lifeOffered) {			
			boolean flag = false;
			for(String type : lifePlanTypes) {
				if (type.equals("27")) {
					flag = true;
					break;
				}
			}
			if (flag) {
		       benefitPlans.addAll(getLifeBenefitPlans(company, group, lifePlanTypes));
			}
		}
			
		return benefitPlans;
	}
	
	
	private Set<String> getDPPlansForUISelectedPlans(Company company, BenefitGroup group, Set<String> plans) {
		Map<String, String> map = getDPPlanMapping( company, group );
		Set<String> dpPlans = new TreeSet<>();
		
		for(String plan : plans) {
			String dpPlan = map.get(plan);
			if (dpPlan != null) {
				dpPlans.add(dpPlan);
			}			
		}
		return dpPlans;
		
	}
	

	public Set<String> getStdBenefitPlans(Company company, BenefitGroup group) {
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();
		
		Set<String> list = getDisabilityOptionPlans(benefitOffers, Constants.STD_CODE);
		logger.info("LIST OF STD PLANS : {}", list);
		
		return list;
	}
	
	
	public Set<String> getLtdBenefitPlans(BenefitGroup group) {
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();
		Set<String> list = getDisabilityOptionPlans(benefitOffers, Constants.LTD_CODE);
		logger.info("LIST OF LTD PLANS : {}", list);
		return list;
	}


	private Set<String> getDisabilityOptionPlans(List<BenefitOffer> benefitOffers, String planType) {
		Set<String> list = new TreeSet<>();
		for (BenefitOffer benefitOffer : benefitOffers) {
			BenefitOfferSummary summary = benefitOffer.getSummary();
			if (summary.getType().equals(BSSApplicationConstants.ADDITIONAL)) {
				List<AdditionalBenefitOffer> additionalBenefits = new ArrayList<>(
						benefitOffer.getAdditionalBenefitOffers());
				
				for (AdditionalBenefitOffer abDTOImpl : additionalBenefits) {
					BenefitOfferSummary summary1 = abDTOImpl.getSummary();
					List<AdditionalBenefitPlan> planOptions = abDTOImpl.getAdditionalBenefitPlans();
					if (summary1.getType().equals(BSSApplicationConstants.DISABILITY)) {
						// Get option plans						
						for(AdditionalBenefitPlan option : planOptions) {
						   List<DisabilityBenefitOptionPlans> plans = option.getOptionPlans();
						   for(DisabilityBenefitOptionPlans plan : plans) {
						       if (plan.getPlanType().equals(planType) ) {
						    	   list.add(plan.getId());
						      }
						   }
					    }
					}			
				}
			}
		}
		return list;
	}

	@Override
	public int updateBenefitDefinitionOptionCost(Company company, BenefitGroup group) {
		Map<String,String> rateIdMap = BenefitGroupRateMapper.convertGroupRateToMap( group.getGroupRate() );
		String clientRateId = rateIdMap.get( BSSRateType.MEDICAL.rateIdType() );
		logger.info("Updating COST rows for MEDICAL rate table id : {}", clientRateId);
		Query query = psEntityManager.createNamedQuery(UPDATE_BEN_DEFN_OPTN_COST);
		query.setParameter(BSSQueryConstants.PLAN_TYPES, Arrays.asList(BSSApplicationConstants.MEDICAL_PLAN_TYPE) );
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
		query.setParameter(EFF_DT_LOWER, company.getPlanStartDate() );
		query.setParameter(CLIENT_RATE_ID, clientRateId );
		int num = DaoUtils.executeUpdate(query, UPDATE_BEN_DEFN_OPTN_COST);

		clientRateId = rateIdMap.get( BSSRateType.DP_MEDICAL.rateIdType() );
		logger.info("Updating COST rows for DP MEDICAL rate table id : {}", clientRateId);
		query.setParameter(BSSQueryConstants.PLAN_TYPES, Arrays.asList("15") );
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
		query.setParameter(EFF_DT_LOWER, company.getPlanStartDate() );
		query.setParameter(CLIENT_RATE_ID, clientRateId);
		num += DaoUtils.executeUpdate(query, UPDATE_BEN_DEFN_OPTN_COST);

		clientRateId = rateIdMap.get( BSSRateType.OTHER.rateIdType() );
		logger.info("Updating COST rows for OTHER rate table id : {}", clientRateId);
		query.setParameter(BSSQueryConstants.PLAN_TYPES, Arrays.asList("11", "16", "14", "17", "1D", "1E", "1U", "1V") );
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
		query.setParameter(EFF_DT_LOWER, company.getPlanStartDate() );
		query.setParameter(CLIENT_RATE_ID, clientRateId);
		num += DaoUtils.executeUpdate(query, UPDATE_BEN_DEFN_OPTN_COST);

		logger.info("NUMBER OF ROWS UPDATED in PS_BEN_DEFN_COST TBL : {}", num);
		return num;
	}

	/**
	 * 
	 * @param company
	 * @param group
	 * @param benefitPlanContributions
	 * @return
	 */
	public int insertAutoSelectedBenefitPlanRateData(Company company, BenefitGroup group,
			Map<String, List<Contribution>> benefitPlanContributions, Map<String, XbssRealmPlyrPlan> plyrPlanMap,
			Map<String, String> autoSelectPlanTypes, Map<String, List<PayInRateInfo>> payInRatesMap) {
		int total = 0;
		this.batchOfBenPlanRateData = new ArrayList<>();
		this.batchOfChangeCostRate = new ArrayList<>();
		this.batchOfBnRateTbl = new ArrayList<>();
		this.batchOfSimpleBnRateData = new ArrayList<>();

		Map<String, String> rateIdMap = BenefitGroupRateMapper.convertGroupRateToMap(group.getGroupRate());

		BenefitOffer medicalBenefitOffer = null;
		for (BenefitOffer offer : group.getBenefitOffers()) {
			if (BSSApplicationConstants.MEDICAL.equals(offer.getSummary().getType())) {
				medicalBenefitOffer = offer;
				break;
			}
		}

		// go build the expanded supplement map only if the funding type is benefit
		// supplement
		Map<String, BigDecimal> benSupplementMap;
		Map<BSSRateType, String> benSuppRateIdMap;
		if (null != medicalBenefitOffer && BSSApplicationConstants.BSUPP.equals(medicalBenefitOffer.getPlanPackage().getFundingType())) {
			benSupplementMap = this
					.expandBenSuppAmounts(medicalBenefitOffer.getPlanPackage().getCoverageLevelFunding());
			// allocate complete set of rate IDs for benefit supplement
			benSuppRateIdMap = this.getBenSuppRateIds(group, rateIdMap);
		} else {
			benSupplementMap = null;
			benSuppRateIdMap = null;
		}


		Map<String, Map<String, BenefitPlanRatesData>> planRatesMap = new HashMap<>();
		PlanRateData planRateData = new PlanRateData();

		for( Map.Entry<String,List<Contribution>> benefitPlanEntry : benefitPlanContributions.entrySet() ) {
			String bandCode = StrategyUtils.findBandCode(company, benefitPlanEntry.getKey(), plyrPlanMap);
			Map<String, BenefitPlanRatesData> planRatesList = new HashMap<>();
			List<Contribution> contributions = benefitPlanEntry.getValue();
			for (Contribution contribution : contributions) {
				BnRateDataForInsert rateRow = new BnRateDataForInsert();
				rateRow.setRateTblId( rateIdMap.get( Constants.MEDICAL_CODE ) );
				rateRow.setEffdt( companyPlanStartDate );
				rateRow.setBnRateKey01( benefitPlanEntry.getKey() );
				rateRow.setBnRateKey02( contribution.getCoverageLevel() );
				rateRow.setQuarter( company.getQuater() );

				BigDecimal erRate = BigDecimal.ZERO;
				BigDecimal eeRate = BigDecimal.ZERO;
				if (contribution.getEmployerContribution() != null) {
					erRate = contribution.getEmployerContribution().setScale(2, RoundingMode.HALF_UP);
				}
				if (contribution.getEmployeeContribution() != null) {
					eeRate = contribution.getEmployeeContribution().setScale(2, RoundingMode.HALF_UP);
				}

				// seems this Contribution object doesn't have a plan cost property, so derive
				// it
				if (contribution.getPlanCost() == null) {
					contribution.setPlanCost(erRate.add(eeRate));
				}

				// back-out the ben supplement amounts
				// everything that comes through here is medical so just figure out whether it's
				// BSUPP
				if (medicalBenefitOffer != null && BSSApplicationConstants.BSUPP
						.equals(medicalBenefitOffer.getPlanPackage().getFundingType())) {
					BigDecimal benSuppAmount = benSupplementMap.get(contribution.getCoverageLevel());
					if (benSuppAmount == null) {
						benSuppAmount = BigDecimal.ZERO;
					}
					// quick hack to create BenefitPlan object to send to bumpBenefitSupplement
					BenefitPlan benPlan = new BenefitPlan();
					benPlan.setId( benefitPlanEntry.getKey() );
					benPlan.setPlanType(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
					erRate = this.bumpBenefitSupplement(company, group, benPlan, contribution.getCoverageLevel(),
							erRate, benSuppAmount, benSuppRateIdMap);
					eeRate = contribution.getPlanCost().subtract(erRate);
				}

				rateRow.setBnEmplRate( eeRate );
				rateRow.setBnEmplrRate( erRate );
				rateRow.setPfClient( company.getPfClient() );
				rateRow.setPlanType( autoSelectPlanTypes.get( benefitPlanEntry.getKey() ));


				if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(autoSelectPlanTypes.get( benefitPlanEntry.getKey() ))) {
					rateRow.setRateTblId( rateIdMap.get( BSSRateType.MEDICAL.rateIdType() ) );
				} else {
					rateRow.setRateTblId( rateIdMap.get( BSSRateType.OTHER.rateIdType() ) );
				}

				String planTypeCd = null;
				if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(autoSelectPlanTypes.get( benefitPlanEntry.getKey() ))) {
					planTypeCd = BSSApplicationConstants.MEDICAL_PLAN_TYPE_DP;
				} else if (BSSApplicationConstants.DENTAL_PLAN_TYPE.equals(autoSelectPlanTypes.get( benefitPlanEntry.getKey() ))) {
					planTypeCd = BSSApplicationConstants.DENTAL_PLAN_TYPE_DP;
				} else if (BSSApplicationConstants.VISION_PLAN_TYPE.equals(autoSelectPlanTypes.get( benefitPlanEntry.getKey() ))) {
					planTypeCd = BSSApplicationConstants.VISION_PLAN_TYPE_DP;
				} else if (BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE
						.equals(autoSelectPlanTypes.get( benefitPlanEntry.getKey() ))) {
					planTypeCd = BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE_DP;
				} else if (BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE
						.equals(autoSelectPlanTypes.get( benefitPlanEntry.getKey() ))) {
					planTypeCd = BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE_DP;
				}

				rateRow.setBandCode( bandCode );
				
				// For DIFFERENTIALS clients, set pay-in rate
				if (company.getRiskType() == RiskTypeEnum.DIFFERENTIALS) {
					BigDecimal payInRate = lookupPayInRate(payInRatesMap, benefitPlanEntry.getKey(), contribution.getCoverageLevel());
					rateRow.setT2ProvCovrgRate(payInRate);
				}
				batchOfBenPlanRateData.add( rateRow );
				total++;
				BenefitPlanRatesData planRates = new BenefitPlanRatesData(eeRate, erRate, planTypeCd, bandCode);
				planRatesList.put(contribution.getCoverageLevel(), planRates);
			}
			planRatesMap.put( benefitPlanEntry.getKey(), planRatesList);
		}
		
		// go insert the batched data
		this.batchInsertRateData(company.getRiskType());
		this.batchInsertChangeCostRate();
		this.batchInsertBnRateTbl();
		this.batchInsertSimpleBnRateData();

		planRateData.setMapRates(planRatesMap);
		if (planRateData != null && planRateData.getMapRates() != null) {
			logger.info("NUMBER OF ITEMS IN planRateData : {}", planRateData.getMapRates().size());
			insertDPBenefitPlanRateData(company, planRateData, rateIdMap, group, payInRatesMap);
		}
		return total;
	}
	
	
	@Override
	public int insertBenefitPlanRateData(Company company, BenefitGroup group, Map<String, XbssRealmPlyrPlan> plyrPlanMap, Map<String, List<PayInRateInfo>> payInRatesMap) {
		int total = 0;
		this.batchOfBenPlanRateData = new ArrayList<>();
		this.batchOfChangeCostRate = new ArrayList<>();
		this.batchOfBnRateTbl = new ArrayList<>();
		this.batchOfSimpleBnRateData = new ArrayList<>();

		Map<String,String> rateIdMap = BenefitGroupRateMapper.convertGroupRateToMap( group.getGroupRate() );
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();
		PlanRateData planRateData = new PlanRateData();
		Map<String, Map<String, BenefitPlanRatesData>> planRatesMap = new HashMap<>();

		/* These maps are needed for benefit supplement.  They will remain null if ben supp is not used
		 * The first is keyed by COVRG_CD and will be used to back the supplement amount out of contributions.
		 * The second is keyed by BSSRateType and will be used to lookup PeopleSoft rate IDs when needed
		 * to update the PeopleSoft database.
		 */
		Map<String,BigDecimal> benSuppMap = null;
		Map<BSSRateType,String> benSuppRateIdMap = null;

		for (BenefitOffer benefitOffer : benefitOffers) {
			BenefitOfferSummary summary = benefitOffer.getSummary();
			String planTypeDesc = summary.getType();

			// Group and Optional plan types
			// DP plans for Group and Optional, we will not get contributions, how do we insert data into 
			// this table for these plans
			if (planTypeDesc.equals(BSSApplicationConstants.MEDICAL) || planTypeDesc.equals(BSSApplicationConstants.DENTAL) || planTypeDesc.equals(BSSApplicationConstants.VISION)  ) {
				List<BenefitPlan> benefitPlans = benefitOffer.getBenefitPlans();

				if( planTypeDesc.equals(BSSApplicationConstants.MEDICAL) &&
						benefitOffer.getPlanPackage().getWaiverAllowance() != null &&
						benefitOffer.getPlanPackage().getWaiverAllowance().compareTo( BigDecimal.ZERO ) > 0 ) {
					// go setup benefit cost and rate data for waiver allowance
					this.generateWaiverAllowance( company, group, rateIdMap, benefitOffer.getPlanPackage().getWaiverAllowance() );
				}

				if (planTypeDesc.equals(BSSApplicationConstants.MEDICAL)
						&& BSSApplicationConstants.BSUPP.equals(benefitOffer.getPlanPackage().getFundingType())) {
					// allocate complete set of rate IDs for benefit supplement
					benSuppRateIdMap = this.getBenSuppRateIds( group, rateIdMap );
					// go setup rate data for benefit supplement
					benSuppMap = this.generateBenefitSupplement(company, group, benefitOffer.getPlanPackage().getCoverageLevelFunding(), benSuppRateIdMap );
				}

				for (BenefitPlan benefitPlan : benefitPlans) {
					String bandCode  = StrategyUtils.findBandCode(company, benefitPlan.getId(), plyrPlanMap);
					List<PlanContribution> planContributions = benefitPlan.getContributions();
					Map<String, BenefitPlanRatesData> planRatesList = new HashMap<>();

					for (PlanContribution contribution : planContributions ) {
						BnRateDataForInsert rateRow = new BnRateDataForInsert();
						rateRow.setRateTblId( rateIdMap.get( Constants.MEDICAL_CODE ) );
						rateRow.setEffdt( companyPlanStartDate );
						rateRow.setBnRateKey01( benefitPlan.getId() );
						rateRow.setBnRateKey02( SubmitUtil.getCoverageCode( contribution.getType() ) );
						rateRow.setQuarter( company.getQuater() );

						BigDecimal planCost = contribution.getPlanCost();
						BigDecimal erRate;
						BigDecimal eeRate;

						BigDecimal employerPercent = contribution.getEmployerPercent();
						if( BigDecimal.ZERO.equals( employerPercent ) ) {
							erRate = BigDecimal.ZERO;
							eeRate = planCost;
						} else {
							if (contribution.getEmployerContribution() != null) {
								erRate = contribution.getEmployerContribution().setScale(2, RoundingMode.HALF_UP);
							} else {
								employerPercent = employerPercent.divide(new BigDecimal(100));
								erRate = planCost.multiply(employerPercent).setScale(2, RoundingMode.HALF_UP);
							}
							eeRate = planCost.subtract( erRate );
						}
						
						// if funding method was "benefit supplement" the bsupp amount must be backed-out of the contribution for medical
						if (planTypeDesc.equals(BSSApplicationConstants.MEDICAL)
								&& BSSApplicationConstants.BSUPP.equals(benefitOffer.getPlanPackage().getFundingType())) {
							// get benefit supplement for this coverage code
							BigDecimal benSuppAmount = benSuppMap.get( SubmitUtil.getCoverageCode(contribution.getType()) );
							if( benSuppAmount == null ) {
								benSuppAmount = BigDecimal.ZERO;
							}
							erRate = this.bumpBenefitSupplement( company, group, benefitPlan, SubmitUtil.getCoverageCode(contribution.getType()),
									erRate, benSuppAmount, benSuppRateIdMap );
							eeRate = planCost.subtract( erRate );
						}

						rateRow.setBnEmplRate( eeRate );
						rateRow.setBnEmplrRate( erRate );
						rateRow.setBandCode( bandCode );

						// For DIFFERENTIALS clients, set pay-in rate
						if (company.getRiskType() == RiskTypeEnum.DIFFERENTIALS) {
							String coverageCode = SubmitUtil.getCoverageCode(contribution.getType());
							BigDecimal payInRate = lookupPayInRate(payInRatesMap, benefitPlan.getId(), coverageCode);
							rateRow.setT2ProvCovrgRate(payInRate);
						}

						String rateTblId = null;
						String planType = null;
						String dpPlanType = null;
						if (planTypeDesc.equals(BSSApplicationConstants.MEDICAL)) {
							// set the rate table ID to the medical rate
							rateTblId = rateIdMap.get(BSSRateType.MEDICAL.rateIdType());
							planType = BSSApplicationConstants.MEDICAL_PLAN_TYPE;
							dpPlanType = BSSApplicationConstants.MEDICAL_PLAN_TYPE_DP;
						} else {
							// set the rate table ID to the OTHER rate
							rateTblId = rateIdMap.get(BSSRateType.OTHER.rateIdType());
							if (planTypeDesc.equals(BSSApplicationConstants.VISION)) {
								if (benefitPlan.isEmployeePaid()) {
									planType = BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE;
									dpPlanType = "1U";
								} else {
									planType = BSSApplicationConstants.VISION_PLAN_TYPE;
									dpPlanType = "17";
								}
							} else if (planTypeDesc.equals(BSSApplicationConstants.DENTAL)) {
								if (benefitPlan.isEmployeePaid()) {
									planType = BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE;
									dpPlanType = "1E";
								} else {
									planType = BSSApplicationConstants.DENTAL_PLAN_TYPE;
									dpPlanType = "16";
								}
							}
						}

						rateRow.setPfClient( company.getPfClient() );
						rateRow.setRateTblId( rateTblId );
						rateRow.setPlanType( planType );
						batchOfBenPlanRateData.add( rateRow );

						total++;
						BenefitPlanRatesData planRates = new BenefitPlanRatesData(eeRate, erRate, dpPlanType, bandCode);
						planRatesList.put(SubmitUtil.getCoverageCode(contribution.getType()), planRates);
					}
					logger.info("BENEFIT PLAN : {}\t Rate SIZE : {}", benefitPlan.getId(), planRatesList.size());
					planRatesMap.put(benefitPlan.getId(), planRatesList);
	
				}
				planRateData.setMapRates(planRatesMap);
			}
		}

		// go insert the batched data
		this.batchInsertRateData(company.getRiskType());
		this.batchInsertChangeCostRate();
		this.batchInsertBnRateTbl();
		this.batchInsertSimpleBnRateData();

		if(planRateData!=null && planRateData.getMapRates()!=null){
			logger.info("NUMBER OF ITEMS IN planRateData : {}", planRateData.getMapRates().size());
			insertDPBenefitPlanRateData(company, planRateData, rateIdMap, group, payInRatesMap);
		}

		logger.info("NUMBER OF ITEMS ENTERED INTO BEN_PLAN_RATE_DATA : {}", total);
		return total;
	}


	private void batchInsertRateData(RiskTypeEnum riskType) {
		if (riskType == RiskTypeEnum.DIFFERENTIALS) {
			batchInsertRateDataForDifferentials();
		} else {
			batchInsertRateDataForBands();
		}
	}

	private void batchInsertRateDataForBands() {
		if (this.batchOfBenPlanRateData.isEmpty()) {
			return;
		}

		String sqlString = psEntityManager.createNamedQuery(INSERT_BEN_PLAN_RATE_DATA)
				.unwrap(org.hibernate.query.Query.class).getQueryString();

		Session session = psEntityManager.unwrap( Session.class );
		session.doWork( ( Connection cn ) -> {
			PreparedStatement stmt = cn.prepareStatement( sqlString );

			for( BnRateDataForInsert brd : this.batchOfBenPlanRateData ) {
				stmt.setString( 1, brd.getRateTblId() );
				stmt.setString( 2, brd.getEffdt() );
				stmt.setString( 3, brd.getBnRateKey01() );
				stmt.setString( 4, brd.getBnRateKey02() );
				stmt.setBigDecimal( 5, brd.getBnEmplRate() );
				stmt.setBigDecimal( 6, brd.getBnEmplrRate() );
				stmt.setString( 7, brd.getBnRateKey02() );
				stmt.setString( 8, brd.getPlanType() );
				stmt.setString( 9, brd.getBnRateKey01() );
				stmt.setString( 10, brd.getQuarter() );
				stmt.setString( 11, brd.getEffdt() );
				stmt.setString( 12, brd.getBandCode() );
				stmt.setString( 13, brd.getPfClient() );
				stmt.addBatch();
			}
			try {
				stmt.executeBatch();
			} catch( SQLException sqlEx ) {
				throw new BSSApplicationException( sqlEx, new BSSApplicationError( BATCH_ERR,
						BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, PsSubmitDataDaoImpl.class.getName(),
						BATCH_ERR_MSG, INSERT_BEN_PLAN_RATE_DATA, null ) );
			}
		});
		this.batchOfBenPlanRateData = null;
	}


	private void batchInsertRateDataForDifferentials() {
		if (this.batchOfBenPlanRateData.isEmpty()) {
			return;
		}

		String sqlString = psEntityManager.createNamedQuery(INSERT_BEN_PLAN_RATE_DATA_WITH_PAY_IN_RATE)
				.unwrap(org.hibernate.query.Query.class).getQueryString();
		Session session = psEntityManager.unwrap(Session.class);

		try {
			session.doWork((Connection cn) -> {
				try (PreparedStatement stmt = cn.prepareStatement(sqlString)) {
					for (BnRateDataForInsert brd : this.batchOfBenPlanRateData) {
						if (brd.getRateTblId() == null || brd.getEffdt() == null || brd.getBnRateKey01() == null
								|| brd.getBnRateKey02() == null || brd.getBnEmplRate() == null || brd.getBnEmplrRate() == null
								|| brd.getT2ProvCovrgRate() == null || brd.getPfClient() == null) {
							String message = "Error attempting to insert null values into PS_BN_RATE_DATA";
							throw new BSSApplicationException(new IllegalArgumentException(message),
									new BSSApplicationError(BATCH_ERR,
											BSSHttpStatusConstants.INTERNAL_SERVER_ERROR,
											PsSubmitDataDaoImpl.class.getName(), message,
											INSERT_BEN_PLAN_RATE_DATA_WITH_PAY_IN_RATE, null));
						}
						stmt.setString(1, brd.getRateTblId());
						stmt.setString(2, brd.getEffdt());
						stmt.setString(3, brd.getBnRateKey01());
						stmt.setString(4, brd.getBnRateKey02());
						stmt.setBigDecimal(5, brd.getBnEmplRate());
						stmt.setBigDecimal(6, brd.getBnEmplrRate());
						stmt.setBigDecimal(7, brd.getT2ProvCovrgRate());
						stmt.setString(8, brd.getPfClient());
						stmt.addBatch();
					}
					try {
						stmt.executeBatch();
					} catch (SQLException sqlEx) {
						throw new BSSApplicationException(sqlEx, new BSSApplicationError(BATCH_ERR,
								BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, PsSubmitDataDaoImpl.class.getName(),
								BATCH_ERR_MSG, INSERT_BEN_PLAN_RATE_DATA_WITH_PAY_IN_RATE, null));
					}
				}
			});
		} finally {
			this.batchOfBenPlanRateData = null;
		}
	}

	private void batchInsertChangeCostRate() {
		String sqlString = psEntityManager.createNamedQuery(CHANGE_COST_RATE)
			.unwrap(org.hibernate.query.Query.class).getQueryString();

		Session session = psEntityManager.unwrap( Session.class );
		session.doWork( ( Connection cn ) -> {
			PreparedStatement stmt = cn.prepareStatement( sqlString );

			for( ChangeCostRateForInsert costRate : this.batchOfChangeCostRate ) {
				stmt.setString( 1, costRate.getRateType() );
				stmt.setString( 2, costRate.getRateTblId() );
				stmt.setString( 3, costRate.getErncd() );
				stmt.setString( 4, costRate.getBenefitProgram() );
				stmt.setString( 5, costRate.getEffdtStr() );
				stmt.setString( 6, costRate.getPlanType() );
				stmt.setString( 7, costRate.getBenefitPlan() );
				stmt.setString( 8, costRate.getCovrgCd() );
				stmt.setString( 9, costRate.getErncd() );
				stmt.setString( 10, costRate.getRateType() );
				stmt.setString( 11, costRate.getRateTblId() );
				stmt.addBatch();
			}
			try {
				stmt.executeBatch();
			} catch( SQLException sqlEx ) {
				throw new BSSApplicationException( sqlEx, new BSSApplicationError( BATCH_ERR,
						BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, PsSubmitDataDaoImpl.class.getName(),
						BATCH_ERR_MSG, CHANGE_COST_RATE, null ) );
			}
		});
		this.batchOfChangeCostRate = null;
	}

	private void batchInsertBnRateTbl() {		

		String sqlString = psEntityManager.createNamedQuery(INSERT_BN_RATE_TBL)
				.unwrap(org.hibernate.query.Query.class).getQueryString();

		Session session = psEntityManager.unwrap( Session.class );
		session.doWork( ( Connection cn ) -> {
			PreparedStatement stmt = cn.prepareStatement( sqlString );

			for( BnRateTblForInsert brt : this.batchOfBnRateTbl ) {
				stmt.setString( 1, brt.getRateTblId() );
				stmt.setString( 2, brt.getEffdtStr() );
				stmt.setString( 3, brt.getRateType() );
				stmt.setString( 4, brt.getGroupDescr() );
				stmt.setString( 5, brt.getDescrShort() );
				stmt.setString( 6, brt.getPfClient() );
				stmt.addBatch();
			}
			try {
				stmt.executeBatch();
			} catch( SQLException sqlEx ) {
				throw new BSSApplicationException( sqlEx, new BSSApplicationError( BATCH_ERR,
						BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, PsSubmitDataDaoImpl.class.getName(),
						BATCH_ERR_MSG, INSERT_BN_RATE_TBL, null ) );
			}
		});
		this.batchOfBnRateTbl = null;		
	}

	private void batchInsertSimpleBnRateData() {
		String sqlString = psEntityManager.createNamedQuery(INSERT_SIMPLE_BN_RATE_DATA)
				.unwrap(org.hibernate.query.Query.class).getQueryString();

		Session session = psEntityManager.unwrap( Session.class );
		session.doWork( ( Connection cn ) -> {
			PreparedStatement stmt = cn.prepareStatement( sqlString );

			for( BnRateData brd : this.batchOfSimpleBnRateData ) {
				stmt.setString( 1, brd.getRateTblId() );
				stmt.setString( 2, brd.getEffdt() );
				stmt.setString( 3, brd.getBnRateKey01() );
				stmt.setString( 4, brd.getBnRateKey02() );
				stmt.setBigDecimal( 5, brd.getBnEmplRate() );
				stmt.setBigDecimal( 6, brd.getBnEmplrRate() );
				stmt.setBigDecimal( 7, brd.getT2ProvCovrgRate() );
				stmt.setString( 8, brd.getPfClient() );
				stmt.addBatch();
			}
			try {
				stmt.executeBatch();
			} catch( SQLException sqlEx ) {
				throw new BSSApplicationException( sqlEx, new BSSApplicationError( BATCH_ERR,
						BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, PsSubmitDataDaoImpl.class.getName(),
						BATCH_ERR_MSG, INSERT_SIMPLE_BN_RATE_DATA, null ) );
			}
		});
		this.batchOfSimpleBnRateData = null;
	}




	private int generateWaiverAllowance( Company company, BenefitGroup group, Map<String,String> rateIdMap,
			BigDecimal waiverAllowanceAmount ) {
		
		// do SELECT statement to get option_id and cost_id
		BigDecimal optionId = null;
		BigDecimal costId = null;
		Query benDefnKeys = psEntityManager.createNamedQuery( GET_WAIVE_ROW_KEYS );
		benDefnKeys.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		benDefnKeys.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		benDefnKeys.setParameter( BSSQueryConstants.PLAN_TYPE, BSSApplicationConstants.MEDICAL_PLAN_TYPE );
		List<Object[]> list = DaoUtils.getResultList(benDefnKeys, GET_WAIVE_ROW_KEYS);
		for( Object[] l : list ) {
			optionId = (BigDecimal) l[0];
			costId = (BigDecimal) l[1];
		}

		// get WAIVE rate tbl id from map or database or create and save
		String rateId = this.getWaiveRateId( group, rateIdMap );

		// do MERGE statement to create COST row
		Query setupCostRow = psEntityManager.createNamedQuery( SET_WAIVE_ALLOW_COST );
		setupCostRow.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		setupCostRow.setParameter( BSSQueryConstants.PLAN_TYPE, BSSApplicationConstants.MEDICAL_PLAN_TYPE );
		setupCostRow.setParameter( "costId", costId );
		setupCostRow.setParameter( BSSQueryConstants.OPTION_ID, optionId );
		setupCostRow.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		setupCostRow.setParameter( BSSQueryConstants.RATE_TBL_ID, rateId );
		int numRows = DaoUtils.executeUpdate(setupCostRow, SET_WAIVE_ALLOW_COST);

		// do MERGE statement to create RATE_TBL row
		Query setupRateTblRow = psEntityManager.createNamedQuery( SET_WAIVE_ALLOW_RATE_TBL );
		setupRateTblRow.setParameter( BSSQueryConstants.BENEFIT_PROGRAM, group.getBenefitProgram() );
		setupRateTblRow.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		setupRateTblRow.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		setupRateTblRow.setParameter( BSSQueryConstants.RATE_TBL_ID, rateId );
		numRows += DaoUtils.executeUpdate(setupRateTblRow, SET_WAIVE_ALLOW_RATE_TBL);

		// do MERGE statement to create RATE_DATA row
		Query setupRateDataRow = psEntityManager.createNamedQuery( SET_WAIVE_ALLOW_RATE_DATA );
		setupRateDataRow.setParameter( "bnEmplRate", waiverAllowanceAmount );
		setupRateDataRow.setParameter( BSSQueryConstants.PF_CLIENT, company.getPfClient() );
		setupRateDataRow.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		setupRateDataRow.setParameter( BSSQueryConstants.RATE_TBL_ID, rateId );
		numRows += DaoUtils.executeUpdate(setupRateDataRow, SET_WAIVE_ALLOW_RATE_DATA);
		
		return numRows;
	}

	private String getWaiveRateId( BenefitGroup group, Map<String,String> rateIdMap ) {
		String rateId = null;
		if( rateIdMap.containsKey( BSSRateType.WAIVER_ALLOWANCE.rateIdType() )) {
			rateId = rateIdMap.get( BSSRateType.WAIVER_ALLOWANCE.rateIdType() );
		} else if( null != ( rateId = this.isRateTypeInDB( group, BSSRateType.WAIVER_ALLOWANCE )) ) {
			rateIdMap.put( BSSRateType.WAIVER_ALLOWANCE.rateIdType(), rateId );
		} else {
			rateId = nextRateTblId.execute();
			this.insertNewRateId( group.getId(), BSSRateType.WAIVER_ALLOWANCE, rateId );
			rateIdMap.put( BSSRateType.WAIVER_ALLOWANCE.rateIdType(), rateId );
		}

		return rateId;
	}

	/**
	 * Generate the benefit supplement amounts for Domestic Partner coverages and update the PeopleSoft rate tables with the
	 * benefit supplement rates and the benefit program structure with the BS credit earnings rows
	 * @param company
	 * @param group
	 * @param benSuppCvrgLvlFunding
	 * @return the expanded map of coverage codes to benefit supplement amounts
	 */
	private Map<String,BigDecimal> generateBenefitSupplement( Company company, BenefitGroup group,
			Map<String,BigDecimal> benSuppCvrgLvlFunding, Map<BSSRateType,String> bSuppRateIdMap ) {

		// expand the coverage level-ben supplement map
		Map<String,BigDecimal> expandedCovrgCdFunding = this.expandBenSuppAmounts( benSuppCvrgLvlFunding );


		// Finally, a single loop through all the eight rates will handle the inserts.
		for( Map.Entry<BSSRateType,String> entry : bSuppRateIdMap.entrySet() ) {
			// INSERT/MERGE PS_BN_RATE_TBL
			BnRateTblForInsert bnRateTblRow = new BnRateTblForInsert();
			bnRateTblRow.setRateTblId( entry.getValue() );
			bnRateTblRow.setEffdtStr( company.getPlanStartDate() );
			bnRateTblRow.setGroupDescr( group.getBenefitProgram() + "_" + entry.getKey().rateDescr() );
			bnRateTblRow.setDescrShort( entry.getKey().rateDescr() );
			bnRateTblRow.setRateType( "2" );
			bnRateTblRow.setPfClient( company.getPfClient() );
			batchOfBnRateTbl.add( bnRateTblRow );


			// insert ben rate data row
			BnRateData brdRow = new BnRateData();
			brdRow.setRateTblId( entry.getValue() );
			brdRow.setEffdt( company.getPlanStartDate() );
			brdRow.setBnRateKey01( BSSApplicationConstants.EMPTY_SPACE );
			brdRow.setBnRateKey02( BSSApplicationConstants.EMPTY_SPACE );
			brdRow.setBnEmplRate( expandedCovrgCdFunding.get( entry.getKey().covrgCd() ) );
			brdRow.setBnEmplrRate( BigDecimal.ZERO );
			brdRow.setT2ProvCovrgRate( expandedCovrgCdFunding.get( entry.getKey().covrgCd() ) );
			brdRow.setPfClient( company.getPfClient() );
			this.batchOfSimpleBnRateData.add( brdRow );
		}

		// insert new COST rows with the benefit supplement rate IDs
		Query insertBenSuppCost = psEntityManager.createNamedQuery( UPDATE_BEN_SUPPLEMENT_COST );
		insertBenSuppCost.setParameter( "benefitProgram", group.getBenefitProgram() );
		insertBenSuppCost.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
		insertBenSuppCost.setParameter( "erncd", "BS" );
		insertBenSuppCost.setParameter( "rateId1", pickRateTblId( BSSRateType.BEN_SUPP_EE, bSuppRateIdMap, expandedCovrgCdFunding ));
		insertBenSuppCost.setParameter( "rateId2", pickRateTblId( BSSRateType.BEN_SUPP_SP, bSuppRateIdMap, expandedCovrgCdFunding ));
		insertBenSuppCost.setParameter( "rateIdC", pickRateTblId( BSSRateType.BEN_SUPP_DEP, bSuppRateIdMap, expandedCovrgCdFunding ));
		insertBenSuppCost.setParameter( "rateId4", pickRateTblId( BSSRateType.BEN_SUPP_FAM, bSuppRateIdMap, expandedCovrgCdFunding ));
		insertBenSuppCost.setParameter( "rateId5", pickRateTblId( BSSRateType.BEN_SUPP_NQ_ADULT, bSuppRateIdMap, expandedCovrgCdFunding ));
		insertBenSuppCost.setParameter( "rateId6", pickRateTblId( BSSRateType.BEN_SUPP_NQ_CHILD, bSuppRateIdMap, expandedCovrgCdFunding ));
		insertBenSuppCost.setParameter( "rateId7", pickRateTblId( BSSRateType.BEN_SUPP_NQ_ADULT_CHILD, bSuppRateIdMap, expandedCovrgCdFunding ));
		insertBenSuppCost.setParameter( "rateId8", pickRateTblId( BSSRateType.BEN_SUPP_FAM_NQ_ADULT, bSuppRateIdMap, expandedCovrgCdFunding ));
		DaoUtils.executeUpdate(insertBenSuppCost, UPDATE_BEN_SUPPLEMENT_COST);

		return expandedCovrgCdFunding;
	}


	/**
	 * Test to see whether an adjustment of the benefit supplement amount is required.  This may be required if the 
	 * company contribution amount was determined to be higher than the benefit supplement amount.  We should not end
	 * up with a benefit supplement and a separate company contribution.  The benefit supplement amount must contain
	 * the entire company cost of the benefit plan.<P>
	 * Whenever an adjustment is required, PeopleSoft rate data must be created and the benefit program COST data must
	 * be changed to the BS_OTHER rate ID.
	 * 
	 * @param company
	 * @param group
	 * @param benPlan
	 * @param erRate
	 * @param benefitSupplement
	 * @return the adjusted employer cost of the benefit
	 */
	private BigDecimal bumpBenefitSupplement( Company company, BenefitGroup group, BenefitPlan benPlan,
			String covrgCd, BigDecimal erRate, 
			BigDecimal benefitSupplement, Map<BSSRateType,String> typeRateIdMap ) {

		if( erRate.compareTo( benefitSupplement ) > 0 ) {

			benefitSupplement = erRate;
			erRate = BigDecimal.ZERO;

			// go setup the override data for this ben supplement
			String rateTblId = this.getOneBenSuppRateId( group, BSSRateType.BEN_SUPP_OTHER, typeRateIdMap );

			batchOfChangeCostRate.add( storeChangeCostRateParms( company, group, benPlan.getPlanType(), benPlan.getId(), covrgCd, rateTblId ));

			this.overrideBenefitSupplement( company, group, benPlan.getId(), covrgCd, benefitSupplement, rateTblId );
		} else {
			erRate = erRate.subtract( benefitSupplement ).max( BigDecimal.ZERO ).setScale(2, RoundingMode.HALF_UP);
		}

		return erRate;
	}


	private ChangeCostRateForInsert storeChangeCostRateParms( Company company, BenefitGroup group, String planType, String benefitPlan,
			String coverageCode, String rateTableId ) {
		ChangeCostRateForInsert ccrRow = new ChangeCostRateForInsert();
		ccrRow.setBenefitProgram( group.getBenefitProgram() );
		ccrRow.setEffdtStr( company.getPlanStartDate() );
		ccrRow.setPlanType( planType );
		ccrRow.setBenefitPlan( benefitPlan );
		ccrRow.setCovrgCd( coverageCode );
		ccrRow.setRateType( "7" );
		ccrRow.setRateTblId( rateTableId );
		ccrRow.setErncd( "BS" );
		return ccrRow;
	}

	/**
	 * Update the rate tables.  This would be used for benefit supplement amounts
	 * that are different from the standard benefit supplement amount, such as when local regulations require
	 * a 100% contribution.
	 * @param company
	 * @param group
	 * @param benefitPlan
	 * @param covrgCd
	 * @param benefitSupplement
	 * @param rateTableId
	 */
	private void overrideBenefitSupplement( Company company, BenefitGroup group, String benefitPlan,
			String covrgCd, BigDecimal benefitSupplement, String rateTableId ) {

		// INSERT/MERGE PS_BN_RATE_TBL
		BnRateTblForInsert bnRateTblRow = new BnRateTblForInsert();
		bnRateTblRow.setRateTblId( rateTableId );
		bnRateTblRow.setEffdtStr( company.getPlanStartDate() );
		bnRateTblRow.setGroupDescr( group.getBenefitProgram() + "_BS_OTHER" );
		bnRateTblRow.setDescrShort( "BS_OTHER" );
		bnRateTblRow.setRateType( "7" );
		bnRateTblRow.setPfClient( company.getPfClient() );
		batchOfBnRateTbl.add( bnRateTblRow );

		// insert ben rate data row
		BnRateData brdRow = new BnRateData();
		brdRow.setRateTblId( rateTableId );
		brdRow.setEffdt( company.getPlanStartDate() );
		brdRow.setBnRateKey01( benefitPlan );
		brdRow.setBnRateKey02( covrgCd );
		brdRow.setBnEmplRate( benefitSupplement );
		brdRow.setBnEmplrRate( BigDecimal.ZERO );
		brdRow.setT2ProvCovrgRate( benefitSupplement );
		brdRow.setPfClient( company.getPfClient() );
		this.batchOfSimpleBnRateData.add( brdRow );
	}




	/**
	 * Process the benefit supplement amount map and change the coverage descriptions to the correct codes
	 * @param covrgLevelFunding
	 * @return
	 */
	private Map<String,BigDecimal> translateCoverageDescrToCovrgCd( Map<String,BigDecimal> covrgLevelFunding ) {
		Map<String,BigDecimal> covrgCdMap = new HashMap<>();
		for( Map.Entry<String,BigDecimal> entry : covrgLevelFunding.entrySet() ) {
			covrgCdMap.put( CoverageCodesEnums.codeFromId( entry.getKey() ), entry.getValue() );
		}
		return covrgCdMap;
	}

	/**
	 * Expands the map of coverage code to benefit supplement amount by calculating the amounts for domestic partner coverage
	 * @param benSuppFunding the coverage level funding for benefit supplement, from the plan package
	 * @return the expanded map keyed by COVRG_CD values
	 */
	private Map<String,BigDecimal> expandBenSuppAmounts( Map<String,BigDecimal> benSuppFunding ) {
		// translate the coverage descriptions to coverage codes
		// create a new map, first adding all the rows from the provided map, then add the calculated DP amounts
		Map<String,BigDecimal> expandedBenSuppMap;
		if( benSuppFunding == null ) {
			expandedBenSuppMap = new HashMap<>();
		} else {
			expandedBenSuppMap = this.translateCoverageDescrToCovrgCd( benSuppFunding );
		}

		// Next, add new entries for the DP coverage codes and calculate the amounts.
		// max( BigDecimal.ZERO ) at the end of each calculation ensures the amount does not fall below zero
		expandedBenSuppMap.put( BSSRateType.BEN_SUPP_NQ_ADULT.covrgCd(),
				expandedBenSuppMap.get( BSSRateType.BEN_SUPP_SP.covrgCd() ).subtract( expandedBenSuppMap.get( BSSRateType.BEN_SUPP_EE.covrgCd() )).max( BigDecimal.ZERO ) );
		expandedBenSuppMap.put( BSSRateType.BEN_SUPP_NQ_CHILD.covrgCd(),
				expandedBenSuppMap.get( BSSRateType.BEN_SUPP_DEP.covrgCd() ).subtract( expandedBenSuppMap.get( BSSRateType.BEN_SUPP_EE.covrgCd() )).max( BigDecimal.ZERO ) );
		expandedBenSuppMap.put( BSSRateType.BEN_SUPP_NQ_ADULT_CHILD.covrgCd(),
				expandedBenSuppMap.get( BSSRateType.BEN_SUPP_FAM.covrgCd() ).subtract( expandedBenSuppMap.get( BSSRateType.BEN_SUPP_EE.covrgCd() )).max( BigDecimal.ZERO ) );
		expandedBenSuppMap.put( BSSRateType.BEN_SUPP_FAM_NQ_ADULT.covrgCd(),
				expandedBenSuppMap.get( BSSRateType.BEN_SUPP_FAM.covrgCd() ).subtract( expandedBenSuppMap.get( BSSRateType.BEN_SUPP_DEP.covrgCd() )).max( BigDecimal.ZERO ) );
		
		return expandedBenSuppMap;
	}
	
	/**
	 * Examine the benefit supplement amount map and return the corresponding RATE_TBL_ID only if the benefit supplement
	 * amount is greater than zero.  Intended use is for updating the benefit program structure COST rows.  If there
	 * is no benefit supplement amount, then no COST credit row should be inserted.  Returning the zero-length string tells
	 * the SQL statement to insert nothing for that rate type.
	 * @param rateType
	 * @param rateIdMap
	 * @param benSuppMap
	 * @return a value for RATE_TBL_ID or a zero-length String if there was no ben supplement amount found
	 */
	private String pickRateTblId( BSSRateType rateType, Map<BSSRateType,String> rateIdMap, Map<String,BigDecimal> benSuppMap ) {
		BigDecimal benSuppAmt = benSuppMap.get( rateType.covrgCd() );
		if( benSuppAmt == null ) {
			benSuppAmt = BigDecimal.ZERO;
		}
		if( benSuppAmt.compareTo( BigDecimal.ZERO ) <= 0 ) {
			return "";
		} else {
			return rateIdMap.get( rateType );
		}

	}

	/**
	 * This ensures the presence of all eight benefit supplement RATE_TBL_ID values, generating new
	 * codes on-the-fly if required.  The rateIdMap arg is updated as a side-effect.
	 * @param group
	 * @param rateIdMap updated with any new rate ID values that were generated
	 * @return a map with a BSSRateType key and a RATE_TBL_ID value
	 */
	private Map<BSSRateType,String> getBenSuppRateIds( BenefitGroup group, Map<String,String> rateIdMap ) {
		Map<BSSRateType,String> typeToRateIdMap = new EnumMap<>( BSSRateType.class );
		for( BSSRateType tp : BSSRateType.getBenefitSupplementSet()) {
			String rateId = null;
			if( rateIdMap.containsKey( tp.rateIdType() )) {
				rateId = rateIdMap.get( tp.rateIdType() );
			} else if( null != ( rateId = this.isRateTypeInDB( group, tp )) ) {
				rateIdMap.put( tp.rateIdType(), rateId );
			} else {
				rateId = nextRateTblId.execute();
				this.insertNewRateId( group.getId(), tp, rateId );
				rateIdMap.put( tp.rateIdType(), rateId );
			}
			typeToRateIdMap.put( tp, rateId );
		}

		return typeToRateIdMap;
	}

	/**
	 * Get one rate ID for a given rate type.  The type-benSupp map will be used first.  If no rate ID
	 * is found there, the GROUP_RATE table is searched.  If no rate ID is still found, a new one is created.
	 * @param group the benefit group which owns this rate ID (the one you are searching for)
	 * @param rateType the BSSRateType that you want a rate ID for
	 * @param typeBenSuppRateIdMap the existing mapping.  Any new value created here will be added to this map
	 * @return the discovered rate table ID
	 */
	private String getOneBenSuppRateId( BenefitGroup group, BSSRateType rateType, Map<BSSRateType,String> typeBenSuppRateIdMap ) {
		String rateId = null;
		if( typeBenSuppRateIdMap.containsKey( rateType )) {
			rateId = typeBenSuppRateIdMap.get( rateType );
		} else if( null != ( rateId = this.isRateTypeInDB( group, rateType )) ) {
			typeBenSuppRateIdMap.put( rateType, rateId );
		} else {
			rateId = nextRateTblId.execute();
			this.insertNewRateId( group.getId(), rateType, rateId );
			typeBenSuppRateIdMap.put( rateType, rateId );
		}
		return rateId;
	}

	private String isRateTypeInDB( BenefitGroup group, BSSRateType type ) {
		String rateTblId = null;
		Query selectRateId = bssEM.createNamedQuery(GET_SPECIFIC_GROUP_RATE);
		selectRateId.setParameter( "groupID", group.getId() );
		selectRateId.setParameter( "rateIdType", type.rateIdType() );

		try {
			Object result = DaoUtils.getSingleResult(selectRateId, GET_SPECIFIC_GROUP_RATE);
			// return the discovered RATE_TBL_ID value
			rateTblId = (String) result;
		} catch( Exception e ) {
			/* No result was found */
			/* Suppress this exception, returning null */
		}
		return rateTblId;
	}


	private void insertNewRateId( long groupId, BSSRateType type, String rateTblId ) {
		bssEM.getTransaction().begin();
		Query insertWaive = bssEM.createNamedQuery( INSERT_NEW_GROUP_RATE );
		insertWaive.setParameter( BSSQueryConstants.GROUP_ID, groupId );
		insertWaive.setParameter( BSSQueryConstants.RATE_TBL_ID, rateTblId );
		insertWaive.setParameter( "rateIdType", type.rateIdType() );
		DaoUtils.executeUpdate(insertWaive, INSERT_NEW_GROUP_RATE);
		bssEM.getTransaction().commit();
	}

	@Override
	public Map<String, String> getDPPlanMapping( Company company, BenefitGroup group ) {
		Query query = psEntityManager.createNamedQuery(GET_DP_BENEFIT_PLANS);
		query.setParameter(BEN_PROG_FLAG, group.getType() );     // "K1" to use K1 pgm, otherwise for standard pgm
		query.setParameter(BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYearId());		
		query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
		List<Object[]> list = DaoUtils.getResultList(query, GET_DP_BENEFIT_PLANS);
		Map<String, String> map = new HashMap<>();

		for (Object[] tempValue : list) {
			if (tempValue != null && tempValue.length > 0) {
				map.put((String) tempValue[0], (String) tempValue[1]);
			}
		}
		return map;
	}
	
	
	
	private Map<String, String> getHDAndHSA( Company company, BenefitGroup group ) {
		Query query;
		String sqlName;
		if( BenExchngEnums.TRINET_IV.getBenExchng().equals( company.getRealm().getBenExchange() ) || "AMB".equals( company.getRealm().getPeoid() )) {
			/* we need a separate version for Ambrose selects all plans because Ambrose
			 * did not use levels in the OPTION_CD like everyone else */
			sqlName = GET_AMB_HDHP_HSA_PLANS;
		} else {
			sqlName = GET_HDHP_HSA_PLANS;
		}

		query = psEntityManager.createNamedQuery(sqlName);
		query.setParameter(BEN_PROG_FLAG, group.getType() );     // "K1" to use K1 pgm otherwise Standard pgm will be used
		query.setParameter(BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYearId());		
		query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
		List<Object[]> list = DaoUtils.getResultList(query, sqlName);
		Map<String, String> map = new HashMap<>();

		for (Object[] tempValue : list) {
			if (tempValue != null && tempValue.length > 0) {
				map.put((String) tempValue[0], (String) tempValue[1]);
			}
		}
		return map;
	}
	
	private List<String> getSelectedHDPlans(Company company, BenefitGroup group) {
		List<String> selectedMedicalPlans = getSelectedMedicalBenefitPlans(group);
		Map<String, String> map = getHDAndHSA(company, group);
		List<String> hdPlans = new ArrayList<>();
		for(String plan : selectedMedicalPlans) {
			if (map.containsKey(plan)) {
				hdPlans.add(plan);
			}
		}	
		logger.info("*** HD PLANS : {}\t For benefit program : {}", hdPlans, group.getBenefitProgram());
		return hdPlans;		
	}
	
	private List<String> getSelectedMedicalBenefitPlans( BenefitGroup group) {	
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();
		List<BenefitPlan> list = new ArrayList<>();
		for (BenefitOffer benefitOffer : benefitOffers) {
			if (benefitOffer.getSummary().getType().equals(Constants.MEDICAL)) {
				list.addAll(benefitOffer.getBenefitPlans());
				break;
			}
		}
		
		List<String> benefitsPlansList = new ArrayList<>();
		for (BenefitPlan plan : list) {
			benefitsPlansList.add(plan.getId());
		}	
		logger.info("*** UI SELECTED MEDICAL PLANS : {}\t For benefit program : {}", benefitsPlansList,
				group.getBenefitProgram());
		return benefitsPlansList;
		
	}
	
	private int insertDPBenefitPlanRateData(Company company, PlanRateData planRateData,
			Map<String, String> rateTblIdMap, BenefitGroup group, Map<String, List<PayInRateInfo>> payInRatesMap) {
		int total = 0;
		Map<String, String> dpMapping = getDPPlanMapping(company, group);
		Map<String, Map<String, BenefitPlanRatesData>> map = planRateData.getMapRates();

		this.batchOfBenPlanRateData = new ArrayList<>();

		for (Map.Entry<String, Map<String, BenefitPlanRatesData>> entry : map.entrySet()) {
			String dpPlan = dpMapping.get(entry.getKey());
			if (dpPlan != null) {
				logger.info("SELECTED PLAN : {}\t DP PLAN : {}", entry.getKey(), dpPlan);
				Map<String, BenefitPlanRatesData> rateMap = entry.getValue();
				BenefitPlanRatesData rateData1 = rateMap.get(CoverageCodesEnums.COV_EMPLOYEE.getCode());
				BenefitPlanRatesData rateData2 = rateMap.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getCode());
				BenefitPlanRatesData rateDatac = rateMap.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getCode());
				BenefitPlanRatesData rateData4 = rateMap.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getCode());
				String bandCode = rateData1.getBandCode();
				String planType = rateData1.getPlanType();

				BigDecimal covrg1Total = rateData1.getEeRate().add(rateData1.getErRate());
				BigDecimal covrg2Total = rateData2.getEeRate().add(rateData2.getErRate());
				BigDecimal covrgcTotal = rateDatac.getEeRate().add(rateDatac.getErRate());
				BigDecimal covrg4Total = rateData4.getEeRate().add(rateData4.getErRate());

				String rateTblId = Constants.DP_MEDICAL_CODE.equals(rateData1.getPlanType())
						? rateTblIdMap.get(Constants.DP_MEDICAL_CODE)
						: rateTblIdMap.get(BSSRateType.OTHER.rateIdType());

				for (String dpCoverageCode : CoverageCodesEnums.dpCoverageLevels()) {
					BigDecimal erRate = BigDecimal.ZERO;
					BigDecimal eeRate = BigDecimal.ZERO;
					BigDecimal covrgTotal = BigDecimal.ZERO;
					if (CoverageCodesEnums.COV_EMPLOYEE_PLUS_DP.getCode().equals(dpCoverageCode)) {
						covrgTotal = covrg2Total.subtract(covrg1Total).setScale(2, RoundingMode.HALF_UP);
						covrgTotal = covrgTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : covrgTotal;
						erRate = rateData2.getErRate().subtract(rateData1.getErRate()).setScale(2,
								RoundingMode.CEILING);
						eeRate = covrgTotal.subtract(erRate);
					} else if (CoverageCodesEnums.COV_EMPLOYEE_PLUS_DP_CHILD.getCode().equals(dpCoverageCode)) {
						covrgTotal = covrgcTotal.subtract(covrg1Total).setScale(2, RoundingMode.HALF_UP);
						covrgTotal = covrgTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : covrgTotal;
						erRate = rateDatac.getErRate().subtract(rateData1.getErRate()).setScale(2,
								RoundingMode.CEILING);
						eeRate = covrgTotal.subtract(erRate);
					} else if (CoverageCodesEnums.COV_EMPLOYEE_PLUS_DP_ADULT_CHILD.getCode().equals(dpCoverageCode)) {
						covrgTotal = covrg4Total.subtract(covrg1Total).setScale(2, RoundingMode.HALF_UP);
						covrgTotal = covrgTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : covrgTotal;
						erRate = rateData4.getErRate().subtract(rateData1.getErRate()).setScale(2,
								RoundingMode.CEILING);
						eeRate = covrgTotal.subtract(erRate);
					} else if (CoverageCodesEnums.COV_EMPLOYEE_PLUS_TWO_DP_ADULT.getCode().equals(dpCoverageCode)) {
						covrgTotal = covrg4Total.subtract(covrgcTotal).setScale(2, RoundingMode.HALF_UP);
						covrgTotal = covrgTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : covrgTotal;
						erRate = rateData4.getErRate().subtract(rateDatac.getErRate()).setScale(2,
								RoundingMode.CEILING);
						eeRate = covrgTotal.subtract(erRate);
					}
					if (erRate.compareTo(BigDecimal.ZERO) < 0) {
						erRate = BigDecimal.ZERO;
						eeRate = covrgTotal;
					} else if (eeRate.compareTo(BigDecimal.ZERO) < 0) {
						eeRate = BigDecimal.ZERO;
						erRate = covrgTotal;
					}

					BnRateDataForInsert rateRow = new BnRateDataForInsert();
					rateRow.setRateTblId( rateTblId );
					rateRow.setEffdt( companyPlanStartDate );
					rateRow.setQuarter( company.getQuater() );
					rateRow.setBnRateKey01( dpPlan );
					rateRow.setBandCode( bandCode );
					rateRow.setPfClient( company.getPfClient() );
					rateRow.setPlanType( planType );
					rateRow.setBnEmplRate( eeRate );
					rateRow.setBnEmplrRate( erRate );
					rateRow.setBnRateKey02( dpCoverageCode );
					
					// For DIFFERENTIALS clients, set pay-in rate for DP plan
					if (company.getRiskType() == RiskTypeEnum.DIFFERENTIALS) {
						BigDecimal payInRate = lookupPayInRate(payInRatesMap, dpPlan, dpCoverageCode);
						rateRow.setT2ProvCovrgRate(payInRate);
					}
					batchOfBenPlanRateData.add( rateRow );
					total++;
				}
			}
		}
		// go insert a batch of rows
		batchInsertRateData(company.getRiskType());
		
		logger.info("TOTAL NUMBER OF ROWS FOR DP PLAN RATES : {}", total);
		return total;
	}
	
	@Override
	public String submitData(Company company, StrategyData strategy, String userId, boolean sendClientEmail,
			String confirmationId, boolean isResubmit, boolean isDefaultSubmit, Map<String, Integer> adminCounts) {
		Exception exc = null;
		Long strategyId = null;
		String status = BSSApplicationConstants.ERROR;
		
		try {
			Date targetDate = CommonUtils.formatStringToDate(company.getPlanStartDate(),
					BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);

			Map<String, XbssRealmPlyrPlan> plyrPlanMap = realmPlyrPlanService
					.getMapForRealmPlanYear(company.getRealmPlanYear().getId());

			// Retrieve pay-in rates for DIFFERENTIALS clients
			Map<String, List<PayInRateInfo>> payInRatesMap = Collections.emptyMap();
			if (company.getRiskType() == RiskTypeEnum.DIFFERENTIALS) {
				String effectiveDate = CommonUtils.formatDate(company.getPlanStartDate(),
						BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY, BSSApplicationConstants.DATE_PATTERN_YYYY_MM_DD);
				FlexRateResponse flexRateResponse = flexRateService.getPlanRatesFromCache(company, effectiveDate);
				payInRatesMap = FlexRateResponseMapper.getPayInRatesByBenefitPlanId(flexRateResponse, company.getCode());
				logger.info("Retrieved pay-in rates for {} plans for DIFFERENTIALS company: {}",
						payInRatesMap.size(), company.getCode());
			}
			strategyId = strategy.getStrategySummary().getId();
			
			List<String> strategyPortfolios = strategyGroupDataDao.getMedStrategyPortfolios(strategyId);

			List<BenefitGroup> benefitGroups = SubmitServiceHelper.updateBenefitGroupData(strategyGroupService, strategy);
			StrategyHsaFundingDto hsaStrategy = strategy.getStrategyHsaFunding();
			SubmitServiceHelper.updateCompaniesBenefitProgram(company, benefitGroups);

			psBenEligRules = new BenEligRulesImpl();

			psEntityManager.getTransaction().begin();
			psBenEligRules.setEntityManager(psEntityManager);
			benefitProgramCreator = new BenefitProgramServiceImpl(psEntityManager);
			benefitOptionsCreator = new BenefitOptionsServiceImpl(psEntityManager);
			hsaPlanMapping = new HSAPlanMapping( company.getCode() );

			// HSA service.  set Dao provider and entity manager
			hsaPlansDao.setEntityManager( psEntityManager );
			hsaPlanService.setHSAPlansDao( hsaPlansDao );
			hsaPlanService.setNextBenPlanSP( nextBenefitPlan );
			hsaPlanService.setHSAPlanMapping( hsaPlanMapping );
	
			companyPlanStartDate = company.getPlanStartDate();

			if (company.isRenewalCompany()) {
				boolean match = !hasMultipleBenefitPrograms(company) && benefitGroups.size() > 1;
				this.insertEmpBenefitGroup(company, match, strategyId);
			}

			this.insertBenefitSelectionEffectiveDate(company, strategy.getStrategySummary());

			// get the onboarding status for this company
			int olpStatus = hrpDao.getOLPStatus(company);

			// this is a temp copy of the company object. This will use
			// COMPANY_DETAILS_EFFDT to get the company
			// properties in-effect prior to this submit transaction
			// (specifically, the prior default ben program)
			Company companyBefore = new Company();
			companyBefore.setCode(company.getCode());
			companyBefore = psCompanyDao.getCompanyDetailsByEffdt(companyBefore, targetDate);

			logger.info("Default benefit program before submit : {}", companyBefore.getBenefitProgram());

			// save leave and savings plans from the default benefit program
			SavedPlanOptns savedPlans = new SavedPlanOptns(companyBefore, company.getPlanStartDate(),
					psEntityManager);
			savedPlans.saveCurrentPlans();

			/*
			 * for each group in groups -- PASS #1 first pass through the loop,
			 * setup the benefit program structure for each benefit group. This
			 * establishes the foundation that the rest of the submit
			 * transaction will build upon.
			 */
			for (BenefitGroup group : benefitGroups) {
				if (group.isDefaultGroup()) {
					createBenefitProgram(company, group, hsaStrategy);
					break;
				}
			}

			for (BenefitGroup group : benefitGroups) {
				if (!group.isDefaultGroup()) {
					createBenefitProgram(company, group, hsaStrategy);
				}
			}

			Map<String, Boolean> benOfferExceptions = benOfferExceptionService.findApplicableBy(company);
			/*
			 * for each group in groups -- PASS #2 Second pass through the loop
			 * 1) if leave plans or savings plans were saved earlier, insert
			 * them now 2) create the elig rules tables for each benefit group.
			 */

			for (BenefitGroup group : benefitGroups) {
				if (company.isRenewalCompany()) {
					// calling restoreLeavePlans unconditionally will cleanup
					// any dummy rows from the clone
					// and then insert saved rows if they exist
					savedPlans.restoreLeavePlans(group.getBenefitProgram());
				} else {
					// during submit of a new client, only delete/restore leave
					// plans
					// if something was previously saved or if COWEB2 has been
					// run (OLPstatus >= 35).
					// When this is called for olpStatus, even when nothing has
					// been saved, this
					// will make sure the dummy plan rows are deleted
					if (savedPlans.areLeavePlansSaved() || olpStatus >= 35) {
						savedPlans.restoreLeavePlans(group.getBenefitProgram());
					}
				}

				// insert Savings plan rows if they exist
				if (savedPlans.areSavingsPlansSaved()) {
					savedPlans.restoreSavingsPlans(group.getBenefitProgram());
				}

				/*
				 * Creating the elig rules nows ensures that all the tables
				 * exist and the criteria can be updated later. This solves a
				 * problem (AMBIS-5090) where sometimes the order that tables
				 * were created or updated deleted what had been created before.
				 */
				psBenEligRules.createBasEligRules(group, company);
			}

			// one-time updates - these updates are done once for the company,
			// not for each group
			if (company.isRenewalCompany()) {
				// Do these for renewal clients.
				// update all paygroups except NP
				this.updatePaygroup(company, "NP");
			} else {
				// Do these for new clients.
				// Update the benefits start date on Client Options.
				this.updateBenefitsStartDate(company);
				// update all paygroups
				this.updatePaygroup(company, " ");
				if(company.isProspectConvertedOnboardingClient()) {
					String aleTaxYear = DateUtils.extractYear(companyPlanStartDate);
					// updating the ALE status for onboarding companies
					this.updateTaxYear(company, aleTaxYear);
					this.updateAleStatus(company);
					this.insertAceMeasurementAndStabilityPeriods(company, aleTaxYear);
				}
			}
			
			

			/*
			 * for each group in groups -- PASS #3 third pass through the loop,
			 * set the ELIG_CONFIG1 eligibility criteria for each group and
			 * setup the plan selections and contributions for each plan.
			 */
			for (BenefitGroup group : benefitGroups) {
				if (!company.isRenewalCompany()) {
					/* Ensure there are benefit program rows as of the earliest required date
					 * for the company.  That date is returned by the method.
					 */
					String setupDate = this.insertCompanySetupDate(company, group);

					/* Also insert preserved leave and savings plans using the same effective
					 * date used by the above method.
					 */
					if (savedPlans.areLeavePlansSaved()) {
						savedPlans.restoreLeavePlans(group.getBenefitProgram(), setupDate);
					}
					if (savedPlans.areSavingsPlansSaved()) {
						savedPlans.restoreSavingsPlans(group.getBenefitProgram(), setupDate);
					}

					Date tmpSetupDt = Utils.convertStringToDate( setupDate, Constants.DATE_FORMAT );
					Date tmpStartDt = Utils.convertStringToDate( company.getPlanStartDate(), Constants.DATE_FORMAT );
					if (savedPlans.areFsaPlansSaved() && tmpSetupDt.compareTo( tmpStartDt ) < 0 ) {
						savedPlans.restoreFsaPlans(group.getBenefitProgram(), setupDate );
					}
				}

				logger.info("ELIG CONFIG ID : {}\t PLAN START DATE : {}\t SRC DATE : {}", group.getEligConfig1(),
						company.getPlanStartDate(), Utils.convertDateToString(targetDate, Constants.DATE_FORMAT));

				psBenEligRules.setupEligConfig(group, benefitGroups, company, targetDate);

				this.insertBenefitProgramFunding(company, group);
				
				// insert or update excess option data.
				for (BenefitOffer benefitOffer : group.getBenefitOffers()) {
					if (benefitOffer.getSummary().getType().equals("medical") && null != benefitOffer.getPlanPackage()
							&& BSSApplicationConstants.BSUPP.equals(benefitOffer.getPlanPackage().getFundingType())) {
						this.updateExcessOptionSelections(company,  benefitOffer, group.getBenefitProgram());
					}
				}
				
				deleteBenDefForMedicalException(company, benOfferExceptions, group);
				
				logger.info("CALLING updateBenefitDefitionOption");
				// update selected benefit plans
				updateBenDefnOptn(company, group);
				
				this.updateSTDBenefitDefitionOption(company, group);
				this.updateLTDBenefitDefitionOption(group);
				this.updateFSABenefitDefitionOption(company, group, strategyPortfolios);

				// during this method, the WAIVE_COVERAGE flag will be set for
				// selected plan types and disability
				updateAndInsertSelectedBenefitPlanTypes(company, group);

				// update eligibility rules and event rules depending on waiting
				// period
				updateEligRuleForWaitPeriod(company, group);
				updateEventRuleForWaitPeriod(company, group);


				// for clients with an HSA strategy, go setup client plans
				// If HSA strategy is missing, create a dummy object and assume Level-0 HSA
				if( hsaStrategy == null ) {
					hsaStrategy = new StrategyHsaFundingDto();
					hsaStrategy.setOptionId( 0 );
				}
				hsaPlanService.setupHSABenefitPlans( company, group, hsaStrategy, bssEM );


				// check if commuter is offered. Ambrose moving forward does not
				// offer Commuter benefit
				if (realmDataDao.isCommuterBenefitOffered(company.getRealmPlanYear().getId())) {
					insertSelectedCommuterEntries(company, group);
				}

				// Update and delete unselected plan Types
				updateAndDeleteUnselectedBenefitPlanTypes(company, group);

				// Update COST rows for activated plans with client's RateID
				this.updateBenefitDefinitionOptionCost(company, group);

				// set client benefit option flags
				setClientBenefitOptionFlags(company, group, strategyId);

				// do this for all selected benefit plans of Medical, Dental,
				// Vision and optional
				deleteBenefitRateData(company, group);

				this.insertBenefitPlanRateData(company, group, plyrPlanMap, payInRatesMap);

				// updating the funding for auto selected plans
				updateAutoSelectedPlansFunding(company, group, strategy, benOfferExceptions, payInRatesMap);

				// changes for AMBIS-4153
				// adding the condition to bypass AMBROSE form updating the
				// rateTableId's
				if (!BenExchngEnums.TRINET_IV.getBenExchng().equals(company.getRealm().getBenExchange())) {
					updateLifeDisablityRates(company, group);
				}
				// changes for AMBIS-4153
				// during new client setup, delete futures
				if (!company.isRenewalCompany()) {
					this.deleteFutureRates(company, group);
					this.deleteFuturePrclBn(company, group);
				}
				logger.info(" *********** END OF CALLING SUBMIT DATA FOR BENEFIT GROUP {} ****************", group.getName());
			}

			// delete/inactivate in PeopleSoft any BSS groups not part of this strategy
			updateInactiveGroups(company, strategyId);

			psEntityManager.getTransaction().commit();
			status = BSSApplicationConstants.SUCCESS;

		} catch (RollbackException ex) {
			exc = ex;
			logger.info("ROLLBACKING partial commits");
			CommonUtils.logExceptions(ex, logger, company.getCode(), userId);
			if (psEntityManager.getTransaction().isActive())
				psEntityManager.getTransaction().rollback();
			logger.info(" *********** END OF CALLING SUBMIT DATA WITH EXCEPTION ****************");
		} catch (Exception ex) {
			exc = ex;
			logger.info("ROLLBACKING partial commits due to some exception");
			CommonUtils.logExceptions(ex, logger, company.getCode(), userId);
			if (psEntityManager.getTransaction().isActive())
				psEntityManager.getTransaction().rollback();
			logger.info(" *********** END OF CALLING SUBMIT DATA WITH EXCEPTION ****************");
		} finally {
			closeEntityManagers();
			try {
				SubmitStatus submitStatus = submitStatusService.findByConfirmationNumber(company.getCode(),
						confirmationId);
				submitStatus
						.setStatus((exc == null) ? (BSSApplicationConstants.SUCCESS) : BSSApplicationConstants.ERROR);

				SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder()
						.defaultSubmit(isDefaultSubmit).resubmit(isResubmit)
						.withEmailInfo().bdmCounts(adminCounts).buildEmailInfo()
						.withSubmitStatusInfo().submitStatus(submitStatus).exception(exc).buildSubmissionInfo()
						.buildPostSubmit();
				submitService.postSubmit(company, submissionInfo);
				
				if(ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName().equals(userId)) {
					strategyDataDao.inactivateUnSubmittedStrategiesByCompany(company);
				}
			} catch (Exception e) {
				CommonUtils.logExceptions(e, logger, company.getCode(), userId);
			}
		}
		return status;
	}

	private void updateExcessOptionSelections(Company company, BenefitOffer benefitOffer, String benefitProgram) {
		Query deleteData1 = psEntityManager.createNamedQuery(DELETE_PS_EXC_CR_CALC_INC);
		deleteData1.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram);
		deleteData1.setParameter(BSSQueryConstants.EFF_DT, company.getPlanStartDate());
		DaoUtils.executeUpdate(deleteData1, DELETE_PS_EXC_CR_CALC_INC);

		Query deleteData2 = psEntityManager.createNamedQuery(DELETE_PS_EXC_CR_CALC_TBL);
		deleteData2.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram);
		deleteData2.setParameter(BSSQueryConstants.EFF_DT, company.getPlanStartDate());
		DaoUtils.executeUpdate(deleteData2, DELETE_PS_EXC_CR_CALC_TBL);

		Set<String> excessCreditPlanTypes = new HashSet<>();
		if (CollectionUtils.isNotEmpty(benefitOffer.getPlanPackage().getBsuppSelectedVolPlanTypes())) {
			excessCreditPlanTypes.addAll(benefitOffer.getPlanPackage().getBsuppSelectedVolPlanTypes());
		}
		if (ExcessOptionEnum.CASH.getType() != benefitOffer.getPlanPackage().getBsuppExcessOption().intValue()) {
			Query insertData = psEntityManager.createNamedQuery(INSERT_PS_EXC_CR_CALC_TBL);
			insertData.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram);
			insertData.setParameter(BSSQueryConstants.EFF_DT, company.getPlanStartDate());
			DaoUtils.executeUpdate(insertData, INSERT_PS_EXC_CR_CALC_TBL);

			List<String> excessCreditDpPlanTypes = new ArrayList<>();
			for (String planType : excessCreditPlanTypes) {
				if (BSSApplicationConstants.DENTAL_PLAN_TYPE.equals(planType)) {
					excessCreditDpPlanTypes.add(BSSApplicationConstants.DENTAL_PLAN_TYPE_DP);
				}
				if (BSSApplicationConstants.VISION_PLAN_TYPE.equals(planType)) {
					excessCreditDpPlanTypes.add(BSSApplicationConstants.VISION_PLAN_TYPE_DP);
				}
				if (BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE.equals(planType)) {
					excessCreditDpPlanTypes.add(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE_DP);
				}
				if (BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE.equals(planType)) {
					excessCreditDpPlanTypes.add(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE_DP);
				}
				if (BSSApplicationConstants.ACCIDENT_PLAN_TYPE.equals(planType)) {
					excessCreditDpPlanTypes.add(BSSApplicationConstants.ACCIDENT_PLAN_TYPE_DP);
				}
				if (BSSApplicationConstants.CRIT_ILLNESS_PLAN_TYPE.equals(planType)) {
					excessCreditDpPlanTypes.add(BSSApplicationConstants.CRIT_ILLNESS_PLAN_TYPE_DP);
				}
				if (BSSApplicationConstants.INDEMNITY_PLAN_TYPE.equals(planType)) {
					excessCreditDpPlanTypes.add(BSSApplicationConstants.INDEMNITY_PLAN_TYPE_DP);
				}
			}
			excessCreditPlanTypes.add(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
			excessCreditPlanTypes.add(BSSApplicationConstants.MEDICAL_PLAN_TYPE_DP);
			excessCreditPlanTypes.addAll(excessCreditDpPlanTypes);

			for (String planType : excessCreditPlanTypes) {
				if (!"XX".equals(planType)) {
					Query insertData2 = psEntityManager.createNamedQuery(INSERT_PS_EXC_CR_CALC_INC);
					insertData2.setParameter(BSSQueryConstants.BENEFIT_PROGRAM, benefitProgram);
					insertData2.setParameter(BSSQueryConstants.EFF_DT, company.getPlanStartDate());
					insertData2.setParameter(BSSQueryConstants.PLAN_TYPE, planType);
					DaoUtils.executeUpdate(insertData2, INSERT_PS_EXC_CR_CALC_INC);
				}
			}
		}
	}

	private void createBenefitProgram(Company company, BenefitGroup benefitGroup,
			StrategyHsaFundingDto hsaOptions) {

		benefitProgramCreator.createBenefitProgram( company, benefitGroup );
		benefitOptionsCreator.createClientBenefitOptions( company, benefitGroup, hsaOptions );

		// if this is a new company setup, COWEB1 may have created future-dated rows and these can be cleaned up now
		if( ! company.isRenewalCompany() ) {
			benefitProgramCreator.deleteFutureProgram( company, benefitGroup );
			benefitOptionsCreator.deleteFutureOptions( company, benefitGroup );
		}
		
		this.insertBenefitPlanRateTbl( company, benefitGroup );

	}


	/**
	 * Runs a query to get the inactive benefit programs for the current strategy.
	 * This data is returned as a Set so that values are assured to be unique.
	 * @param companyId The database unique identifier of a COMPANY
	 * @param strategyId The database unique identifier of a STRATEGY
	 * @return A List of String arrays.  Each element of the List will contain an
	 * array where String[0] is the benefit program and String[1] is ELIG_CONFIG1 
	 */
	private List<String[]> getInactiveBenefitPrograms( long companyId, long strategyId ) {
		Query selectInactive = bssEM.createNamedQuery(GET_INACTIVE_BENEFIT_PROGRAMS);
		selectInactive.setParameter( BSSQueryConstants.COMPANY_ID, companyId );
		selectInactive.setParameter( BSSQueryConstants.STRATEGY_ID, strategyId );

		List<String[]> inactiveList = new ArrayList<>();
		try {
			List<Object[]> result = DaoUtils.getResultList(selectInactive, GET_INACTIVE_BENEFIT_PROGRAMS);
			for( Object[] r : result ) {
				String[] oneResult = new String[2];
				oneResult[0] = (String) r[0];
				oneResult[1] = (String) r[1];
				inactiveList.add( oneResult );
			}
		} catch( Exception e ) {
			/* Suppress this exception */
			/* No result was found; return the empty set */
		}
		return inactiveList;
	}


	private void deleteFutureRates( Company company, BenefitGroup group ) {

		for( GroupRate gr : group.getGroupRate() ) {
			String rateId = gr.getId().getRateTblId();

			Query deleteData = psEntityManager.createNamedQuery(DELETE_FUTURE_RATE_DATA);
			deleteData.setParameter( BSSQueryConstants.RATE_TBL_ID, rateId );
			deleteData.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
			int num = DaoUtils.executeUpdate(deleteData, DELETE_FUTURE_RATE_DATA);
			logger.info( "Number of records deleted from BN_RATE_DATA: {}", num );
	
			Query deleteTbl = psEntityManager.createNamedQuery(DELETE_FUTURE_RATE_TBL);
			deleteTbl.setParameter( BSSQueryConstants.RATE_TBL_ID, rateId );
			deleteTbl.setParameter( BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate() );
			num = DaoUtils.executeUpdate(deleteTbl, DELETE_FUTURE_RATE_TBL);
			logger.info("Number of records deleted from BN_RATE_TBL: {}", num);
		}
	}


	private void updateAndDeleteUnselectedBenefitPlanTypes(Company company, BenefitGroup group) {		
		List<String> planTypes = getUnSelectedBenefitPlanTypes(company, group);
		if (!planTypes.isEmpty()) {
		   this.updateBenefitDefitionPlan(company, planTypes, group);
		   this.deleteBenefitDefinitionOptionOfTypeW(company, planTypes, group);
		}
		if (realmDataDao.isCommuterBenefitOffered(company.getRealmPlanYear().getId())) {
		    deleteCommuterEntries(group);
		}
	}


	public int deleteBenefitProgram(Company company, BenefitGroup group) {
		Query query = psEntityManager.createNamedQuery(DELETE_BENEFIT_PROGRAM);
		query.setParameter(PRODUCT_CODE, company.getRealm().getPeoid());
		query.setParameter(BSSQueryConstants.COMPANY_ID, company.getCode());
		query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
		query.setParameter(START_DATE, companyPlanStartDate);
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
		int num = DaoUtils.executeUpdate(query, DELETE_BENEFIT_PROGRAM);
		logger.info("NUMBER OF ITEMS DELETED FROM PS_T2_PRCL_BN_PGM : {}", num);
		return num;
	}

	public int insertBenefitDefinitionPlanOfTypeA3(Company company, BenefitGroup group) {
		Query query = psEntityManager.createNamedQuery(INSERT_BENEFIT_DEFN_PLAN_OF_TYPE_A3);
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
		query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
		query.setParameter(BEN_PROG_FLAG, group.getType() );   // "K1" for K1 clone, otherwise standard clone
		query.setParameter(BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYearId());
		int num = DaoUtils.executeUpdate(query, INSERT_BENEFIT_DEFN_PLAN_OF_TYPE_A3);
		logger.info("NUMBER OF ITEMS INSERTED INTO PS_BEN_DEFN_PLAN : {}", num);
		return num;

	}

	public int insertBenefitDefinitionOptionOfTypeA3(Company company, BenefitGroup group) {
		Query query = psEntityManager.createNamedQuery(INSERT_BENEFIT_DEFN_OPTN_OF_TYPE_A3);
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
		query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
		query.setParameter(BEN_PROG_FLAG, group.getType() );  // "K1" for K1 clone, otherwise standard clone
		query.setParameter(BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYearId());
		int num = DaoUtils.executeUpdate(query, INSERT_BENEFIT_DEFN_OPTN_OF_TYPE_A3);
		logger.info("NUMBER OF ITEMS INSERTED INTO PS_BEN_DEFN_PLAN : {}", num);
		return num;
	}

	public int insertBenefitDefinitionCostOfTypeA3(Company company, BenefitGroup group) {
		Query query = psEntityManager.createNamedQuery(INSERT_BENEFIT_DEFN_COST_OF_TYPE_A3);
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
		query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
 		query.setParameter(BEN_PROG_FLAG, group.getType() );  // "K1" for K1 clone, otherwise standard clone 
		query.setParameter(BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYearId());		
		return DaoUtils.executeUpdate(query, INSERT_BENEFIT_DEFN_COST_OF_TYPE_A3);
	}

	/**
	 * Initialize the RATE_TYPE 7 rate IDs only.  At present, these are the '10', '15', and 'OTHER' rates
	 * @return the number of rows inserted/updated
	 */
	@Override
	public int insertBenefitPlanRateTbl(Company company, BenefitGroup group) {
		Map<String,String> rateIdMap = BenefitGroupRateMapper.convertGroupRateToMap( group.getGroupRate() );
		this.batchOfBnRateTbl = new ArrayList<>();

		int rows = buildRateTblRow( company, group, rateIdMap.get( BSSRateType.MEDICAL.rateIdType() ));
		rows += buildRateTblRow( company, group, rateIdMap.get( BSSRateType.DP_MEDICAL.rateIdType() ));
		rows += buildRateTblRow( company, group, rateIdMap.get( BSSRateType.OTHER.rateIdType() ));

		this.batchInsertBnRateTbl();

		return rows;
	}

	private int buildRateTblRow(Company company, BenefitGroup group, String rateTableId) {
		if( rateTableId != null ) {
			BnRateTblForInsert bnRateTblRow = new BnRateTblForInsert();
			bnRateTblRow.setRateTblId( rateTableId );
			bnRateTblRow.setEffdtStr( company.getPlanStartDate() );
			bnRateTblRow.setGroupDescr( group.getName() );
			bnRateTblRow.setDescrShort( group.getName() );
			bnRateTblRow.setRateType( "7" );
			bnRateTblRow.setPfClient( company.getPfClient() );
			batchOfBnRateTbl.add( bnRateTblRow );
			return 1;
		} else {
			return 0;
		}
	}


	private List<String> getUnSelectedBenefitPlanTypes(Company company, BenefitGroup group) {
		List<String> planTypes = new ArrayList<>();
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();		
		boolean dnNotOffered = true;
		boolean vsNotOffered = true;
		
		boolean stdNotOffered = true;
		boolean ltdNotOffered = true;
		boolean lfNotOffered = true;
		boolean dnOptionalOffered = false;
		boolean vsOptionalOffered = false;		

		for (BenefitOffer benefitOffer : benefitOffers) {
			if (benefitOffer.getSummary().getType().equals(BSSApplicationConstants.MEDICAL)
					&& this.getSelectedHDPlans(company, group).isEmpty()) {
				planTypes.add("67");
			}

			if (benefitOffer.getSummary().getType().equals(BSSApplicationConstants.DENTAL)) {
				dnNotOffered = false;
				if (SubmitUtil.isEmployeePaid(benefitOffer)) {
					dnOptionalOffered = true;
				}

			} else if (benefitOffer.getSummary().getType().equals(BSSApplicationConstants.VISION)) {
				vsNotOffered = false;
				if (SubmitUtil.isEmployeePaid(benefitOffer)) {
					vsOptionalOffered = true;
				}
			} else if (benefitOffer.getSummary().getType()
					.equals(BSSApplicationConstants.ADDITIONAL)) {
				List<AdditionalBenefitOffer> additionalBenefits = new ArrayList<>(
						benefitOffer.getAdditionalBenefitOffers());				
				for (AdditionalBenefitOffer addlBen : additionalBenefits) {
					if (addlBen.getSummary().getType().equals(BSSApplicationConstants.DISABILITY)) {
						// Get option plans
						List<AdditionalBenefitPlan> planOptions = addlBen
								.getAdditionalBenefitPlans();
						for (AdditionalBenefitPlan option : planOptions) {
							List<DisabilityBenefitOptionPlans> plans = option
									.getOptionPlans();
							for (DisabilityBenefitOptionPlans plan : plans) {
								if (plan.getPlanType().equals(BSSApplicationConstants.STD_CODE)) {
									stdNotOffered = false;
								}
								if (plan.getPlanType().equals(BSSApplicationConstants.LTD_CODE)) {
									ltdNotOffered = false;
								} 
							}
						}
					}
					else if (addlBen.getSummary().getType().equals(BSSApplicationConstants.LIFE)) {
						lfNotOffered = false;
					}
				}
			}
		}
		
		if (dnNotOffered) {
			planTypes.add(BSSApplicationConstants.DENTAL_PLAN_TYPE);
			planTypes.add("16");
			planTypes.add(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
			planTypes.add("1E");			
		}		
		else {
			
			if (dnOptionalOffered) {
				planTypes.add(BSSApplicationConstants.DENTAL_PLAN_TYPE);
				planTypes.add("16");
			   
			}
			else {
				planTypes.add(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
				planTypes.add("1E");
			}
		}
		
		if (vsNotOffered) {
			planTypes.add(BSSApplicationConstants.VISION_PLAN_TYPE);
			planTypes.add("17");
			planTypes.add(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE);
			planTypes.add("1U");	
		}
		else {
		    if (vsOptionalOffered) {
		    	 planTypes.add(BSSApplicationConstants.VISION_PLAN_TYPE);
				 planTypes.add("17");
		    }
		    else {
		    	 planTypes.add(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE);
				 planTypes.add("1U");		      
		    }
		}	
		if (stdNotOffered) {
			planTypes.add(BSSApplicationConstants.STD_CODE);
		}
		if (ltdNotOffered) {
			planTypes.add(BSSApplicationConstants.LTD_CODE);
		}
		if (lfNotOffered) {
                    planTypes.add(BSSApplicationConstants.LIFE_CODE);
                    boolean flag = false;
                    // If Basic Life plan type is not offered, still we need to make supplemental life plan types active for all excahges
                    // except for Ambrose for current plan year 10-OCT-2016 To 30-SEP-2017
                    List<String> lifePlanTypes = realmDataDao.getLifeSupplementalPlanTypes(company.getRealmPlanYear().getId());
                    for(String type : lifePlanTypes) {
                       if (type.equals("27")) {
                           flag = true;
                           break;
                       }
                    }

                    // For Ambrose for current plan year 10-OCT-2016 To 30-SEP-2017 make inactive supplemental life plan types
                    if(!flag) {
                       planTypes.addAll(lifePlanTypes);
                    }      
		}
		logger.info("Plan Types that are not offered : {}", planTypes);
		return planTypes;
	}
 

	private void updateStdAndLtdWaiveRow(Company company, BenefitGroup group, List<String> list) {
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();
		Set<String> eePaidTypes = new HashSet<>();
		Set<String> coPaidTypes = new HashSet<>();
		
		for(BenefitOffer benefitOffer : benefitOffers) {
			BenefitOfferSummary summary = benefitOffer.getSummary();
			if (summary.getType().equals(BSSApplicationConstants.ADDITIONAL)) {
				List<AdditionalBenefitOffer> additionalBenefits = 
						new ArrayList<> (benefitOffer.getAdditionalBenefitOffers());
				for(AdditionalBenefitOffer addlBen : additionalBenefits) {
					if (addlBen.getSummary().getType().equals(BSSApplicationConstants.DISABILITY)) {
						// Get option plans
						List<AdditionalBenefitPlan> planOptions = addlBen.getAdditionalBenefitPlans();
						for(AdditionalBenefitPlan option : planOptions) {
						   List<DisabilityBenefitOptionPlans> plans = option.getOptionPlans();
						   for(DisabilityBenefitOptionPlans plan : plans) {
						       if (plan.getPlanType().equals(Constants.STD_CODE)) {
								   if (plan.isEmployeePaid()) {
									   eePaidTypes.add(Constants.STD_CODE);
								   } 
								   else {
									   coPaidTypes.add(Constants.STD_CODE);
								   }
						       } else if (plan.getPlanType().equals(Constants.LTD_CODE)) {
								   if (plan.isEmployeePaid()) {
									   eePaidTypes.add(Constants.LTD_CODE);
								   }
								   else {
									   coPaidTypes.add(Constants.LTD_CODE);
								   }
						      }
						   }
					    }								
				    }					
				}
			}
		}

		list.addAll( eePaidTypes );

		if (!eePaidTypes.isEmpty()) {
			updateWaiveRow(company, new ArrayList<>( eePaidTypes ), group);
		}
		/* drop the waive option from any disability plan type that is company-paid */
		if (!coPaidTypes.isEmpty()) {
			deleteBenefitDefinitionOptionOfTypeW(company, new ArrayList<>( coPaidTypes ), group);
		}
	}
	
	
	private List<String> getSelectedBenefitPlanTypes(Company company, BenefitGroup group) {
		List<String> planTypes = new ArrayList<>();
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();		
		boolean dnNotOffered = true;
		boolean mdNotOffered = true;
		boolean vsNotOffered = true;
		boolean lfNotOffered = true;
		boolean dnOptionalOffered = false;
		boolean vsOptionalOffered = false;
		

		for(BenefitOffer benefitOffer : benefitOffers) {
			BenefitOfferSummary summary = benefitOffer.getSummary();
			if (summary.getType().equals(BSSApplicationConstants.MEDICAL)) {
				mdNotOffered = false;	
			}
			else if (summary.getType().equals(BSSApplicationConstants.DENTAL)) {
				dnNotOffered = false;	
				if (SubmitUtil.isEmployeePaid(benefitOffer)) {
					dnOptionalOffered = true;
				}
			} else if (summary.getType().equals(BSSApplicationConstants.VISION)) {
				vsNotOffered = false;
				if (SubmitUtil.isEmployeePaid(benefitOffer)) {
					vsOptionalOffered = true;
				}
			} else if (summary.getType().equals(BSSApplicationConstants.ADDITIONAL)) {
				List<AdditionalBenefitOffer> additionalBenefits = 
						new ArrayList<> (benefitOffer.getAdditionalBenefitOffers());
				for(AdditionalBenefitOffer abDTOImpl : additionalBenefits) {
				    if (abDTOImpl.getSummary().getType().equals(BSSApplicationConstants.LIFE)) {	
			            logger.info("LIFE IS OFFERED .........");
				         lfNotOffered = false;
				         break;
			        }
				}
		    }
		}
		
		if (!mdNotOffered) {
			planTypes.add(BSSApplicationConstants.MEDICAL_PLAN_TYPE);
			planTypes.add(BSSApplicationConstants.MEDICAL_PLAN_TYPE_DP);
			if (!this.getSelectedHDPlans(company, group).isEmpty()) {
				planTypes.add("67");
			}
		}
		
		if (!dnNotOffered) {
			if (dnOptionalOffered) {
				planTypes.add(BSSApplicationConstants.VOLUNTARY_DENTAL_PLAN_TYPE);
				planTypes.add("1E");
			}
			else {
			   planTypes.add(BSSApplicationConstants.DENTAL_PLAN_TYPE);
			   planTypes.add("16");
			}
			
		}
		if (!vsNotOffered) {
			if(vsOptionalOffered) {
			   planTypes.add(BSSApplicationConstants.VOLUNTARY_VISION_PLAN_TYPE);
			   planTypes.add("1U");
			}
			else {
			  planTypes.add(BSSApplicationConstants.VISION_PLAN_TYPE);
			  planTypes.add("17");
			}			
		}	
			
		if (!lfNotOffered) {
			logger.info("************ LIFE IS OFFERED **************");	
			planTypes.add(BSSApplicationConstants.LIFE_CODE);
			// as long as this group is not a K1-type group, add the supplemental insurance types
			if( ! "K1".equals( group.getType() )) {
				planTypes.addAll(realmDataDao.getLifeSupplementalPlanTypes(company.getRealmPlanYear().getId()));
			}
		}
			
		logger.info("Plan Types that are offered : {}", planTypes);
		return planTypes;
	}
	
	private void deleteCommuterEntries(BenefitGroup group) {
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();	
		boolean cmNotOffered = true;
		
		for(BenefitOffer benefitOffer : benefitOffers)  {
			if (benefitOffer.getSummary().getType().equals(BSSApplicationConstants.ADDITIONAL)) {
				List<AdditionalBenefitOffer> additionalBenefits = 
						new ArrayList<>(benefitOffer.getAdditionalBenefitOffers());
				for(AdditionalBenefitOffer  abDTOImpl : additionalBenefits) {
				    if (abDTOImpl.getSummary().getType().equals(BSSApplicationConstants.CMTR)) {				        
				    	   cmNotOffered = false;
				    	   break;  	       
				    }
				}
			}		    			
		}
		if (cmNotOffered) {
		   this.deleteBenefitDefinitionPlanOfTypeA3(group);
		   this.deleteBenefitDefinitionOptionOfTypeA3(group);
		   this.deleteBenefitDefinitionCostOfTypeA3(group);
		}
	}
	
	private void insertSelectedCommuterEntries(Company company, BenefitGroup group) {
		List<BenefitOffer> benefitOffers = group.getBenefitOffers();	
		boolean cmNotOffered = true;
		
		for(BenefitOffer benefitOffer : benefitOffers) {
			if (benefitOffer.getSummary().getType().equals(BSSApplicationConstants.ADDITIONAL)) {
				List<AdditionalBenefitOffer> additionalBenefits = 
						new ArrayList<> (benefitOffer.getAdditionalBenefitOffers());
				for(AdditionalBenefitOffer abDTOImpl : additionalBenefits) {
				    if (abDTOImpl.getSummary().getType().equals(BSSApplicationConstants.CMTR)) {
				       cmNotOffered = false;
				       break;
				    }
				}
		    }			
		}
		if (!cmNotOffered) {
			Query query = psEntityManager.createNamedQuery(CHECK_A3_PLAN_EXISTS);	
			query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
			query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
			
			BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, CHECK_A3_PLAN_EXISTS);
	        logger.debug("NUMBER OF A3 RECORDS : {}", count );
	       if (count.intValue() == 0) {
		      this.insertBenefitDefinitionOptionOfTypeA3(company, group);
		      this.insertBenefitDefinitionPlanOfTypeA3(company, group);
		      this.insertBenefitDefinitionCostOfTypeA3(company, group);
	       }
		}
	}
	
	private List<String> getLifeBenefitPlans(Company company, BenefitGroup group, List<String> planTypes) {
		Query query = psEntityManager.createNamedQuery(GET_LIFE_BENEFIT_PLANS);
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram() );
		query.setParameter(EFF_DT_LOWER, company.getPlanStartDate());
		query.setParameter(BSSQueryConstants.PLAN_TYPES, planTypes);
		
		List<Object[]> list = DaoUtils.getResultList(query, GET_LIFE_BENEFIT_PLANS);
		List<String> benefitPlans = new ArrayList<>();
		for( Object tempValue : list ) {
			logger.info("LIFE BENEFIT PLAN : {}", tempValue );
			benefitPlans.add( tempValue.toString() );
		}
		return benefitPlans;
	}

	
	@Override
	public int insertBenefitDefinitionOption(Company company, BenefitGroup group, List<String> list) {
		int total = 0;
		int num = 0;	
		
		for(String planType : list) {
			Query query = psEntityManager.createNamedQuery(CHECK_PS_BEN_DEFN_OPTN_RECORD_EXISTS);
			
			query.setParameter(EFF_DT_LOWER, companyPlanStartDate);			
			query.setParameter(BSSQueryConstants.PLAN_TYPE, planType);
			query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());			
			
			BigDecimal count = (BigDecimal) DaoUtils.getSingleResult(query, CHECK_PS_BEN_DEFN_OPTN_RECORD_EXISTS);
            logger.info("NUMBER OF RECORDS : {}", count );
            
            if (count.intValue() == 0) {
            	Query query1 = psEntityManager.createNamedQuery(INSERT_PS_BEN_DEFN_OPTN);
			    query1.setParameter(EFF_DT_LOWER, companyPlanStartDate);
			    query1.setParameter(BSSQueryConstants.PLAN_TYPE, planType);	
			    query1.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());	
			    query1.setParameter(BEN_PROG_FLAG, group.getType() );
			    query1.setParameter(BSSQueryConstants.REALM_YEAR_ID, company.getRealmPlanYearId());
				num = DaoUtils.executeUpdate(query1, INSERT_PS_BEN_DEFN_OPTN);
		        total += num;
		    }
		}		
		return total;
	}


	/**
	 * Delete the BN_RATE_DATA rows for ALL of the rate IDs associated with a given benefit group.
	 */
	@Override
	public int deleteBenefitRateData(Company company, BenefitGroup group) {
		int num = 0;
		for( GroupRate gr : group.getGroupRate() ) {
			String rateTblId = gr.getId().getRateTblId();
			Query query = psEntityManager.createNamedQuery(DELETE_PS_BN_RATE_DATA);
			query.setParameter(BSSQueryConstants.RATE_TBL_ID, rateTblId);
			query.setParameter(BSSQueryConstants.EFF_DATE_STR, companyPlanStartDate);
			query.setParameter(BSSQueryConstants.PF_CLIENT, company.getPfClient());
			num = DaoUtils.executeUpdate(query, DELETE_PS_BN_RATE_DATA);
			logger.info("NUMBER RECORDS DELETED FROM BN_RATE_DATA : {}", num);
		}
		return num;
	}

	@Override
	public int updateWaiveRow(Company company, List<String> list, BenefitGroup group) {
		Query query = psEntityManager.createNamedQuery(UPDATE_WAIVE_ROW);		
		int num = 0;
		for( String planType : list ) {
			query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
			query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
			query.setParameter(BSSQueryConstants.PLAN_TYPE, planType);
			num += DaoUtils.executeUpdate(query, UPDATE_WAIVE_ROW);
		}
		logger.info("NUMBER OF RECORDS UPDATED IN PS_BEN_DEFN_PLAN : {} WAIVE ROWS FOR PLAN TYPES : {}", num, list);
		return num;		
	}

	@Override
	public int updateWaiveRowForAdditionalBenefits(Company company, BenefitGroup group) {
		Query query = psEntityManager.createNamedQuery(UPDATE_WAIVE_ROW_FOR_ADDITIONAL_BENEFITS);		
		query.setParameter(EFF_DT_LOWER, companyPlanStartDate);
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
		int num = DaoUtils.executeUpdate(query, UPDATE_WAIVE_ROW_FOR_ADDITIONAL_BENEFITS);
		logger.info("NUMBER RECORDS UPDATED IN PS_BEN_DEFN_PLAN FOR ADDITIONAL BENEFITS : {}", num);
		return num;
	}	
	
	 private void updateAndInsertSelectedBenefitPlanTypes(Company company, BenefitGroup group) {
		 logger.info("UPDATING WAIVE ROWS ....... FOR COMPANY : {}  FOR STRATEGY : {}", company.getCode(), group.getBenefitProgram());
		 updateWaiveRowForAdditionalBenefits(company, group);
		 List<String> list = getSelectedBenefitPlanTypes(company, group);
		 updateWaiveRow(company, list, group);
		 updateStdAndLtdWaiveRow(company, group, list);
		 logger.info("SELECTED BENEFIT PLAN TYPES : {}", list);
		 
		 insertBenefitDefinitionOption(company, group, list);			
	}

	public void defaultSubmit(List<String> companies, boolean isSingleClientSubmit, String userId, long realmPlanYearId) {
		long startTime = System.currentTimeMillis();
		List<String> failedList = new ArrayList<>();
		List<CompanyAndConfNumberDto> failedCompanyAndConfNumberDtos = new ArrayList<>();

		int i = 0;
		String oeQuarter = null;
		try {
			Map<String, Integer> bdmCounts = emailGenService.getBDMCount(companies);
			for (String companyCode : companies) {
				logger.info("defaultSubmit(): submitting {} company : {}", i++, companyCode);
				String confirmationId = null;
				Company company = null;
				SubmitStatus submitStatus = null;
				Exception submitException = null;
				String submitDataStatus = BSSApplicationConstants.ERROR;
				boolean isDeafultEligible = true;
				try {
					createEntityManager();
					 company = companyService.getCompanyDetails(companyCode, false, userId, null);
					if (company.isRenewalCompany() && company.getRealmPlanYearId() == realmPlanYearId) {
						long defaultStrategyId = 0;
						StrategyData strategyData = null;
						List<Strategy> strategies = strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
						if (strategies.isEmpty()) {
							// creating future strategies.
							strategyService.createFutureStrategies(company, true, false);
							strategies = strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
						}
						for (Strategy strategy : strategies) {
							if (BSSApplicationConstants.STRATEGY_TYPE_RECOMMENDED.equals(strategy.getType())) {
								defaultStrategyId = strategy.getId();
							}
						}
						Date currentDate = new Date();
						// sync the strategy data before submitting.
						strategySyncService.syncStrategyData(company, defaultStrategyId);
						strategyData = strategyService.getStrategyById(company, defaultStrategyId, true);
						strategyData.getStrategySummary().setSubmitDate(currentDate);
						strategyData.getStrategySummary().setSubmitted(true);
						strategyDao.updateToSubmitted(defaultStrategyId, true, currentDate);

						boolean sendEmail = true;
						boolean resubmitFlag = false;
					    confirmationId = CommonServiceHelper.randomAlphanumeric();

						String payLoad = CommonServiceHelper.objectToJsonString(strategyData);
						SubmitPayload submitPayload = SubmitPayload.builder().payload(payLoad).build();
						 submitStatus = SubmitStatus.builder()
								.strategyId(strategyData.getStrategySummary().getId())
								.status(BSSApplicationConstants.PROCESSING).submitPayload(submitPayload)
								.createTime(currentDate).userId(userId).company(company.getCode()).emailSentStatus(false)
								.confirmationNumber(confirmationId).realmYrId(company.getRealmPlanYear().getId())
								.serviceOrder(company.getServiceOrderNumber()).statementUploadStatus(null)
								.updateTime(null).sendEmail(sendEmail).build();
						submitPayload.setSubmitStatus(submitStatus);
						submitStatusService.createUpdateSubmitStatus(submitStatus);
						
						Map<String, Integer> bdmCountsFinal = null;
						if (BSSApplicationConstants.ADMIN_COMPANY_CODE.equals(companyCode)) {
							bdmCountsFinal = emailGenService.getBenCorpAdminCount(Arrays.asList(companyCode));
						} else {
							bdmCountsFinal = bdmCounts;
						}
						
						SubmissionInfo submissionInfo = new SubmissionInfo.SubmissionInfoBuilder()
								.defaultSubmit(true)
								.withEmailInfo().bdmCounts(bdmCountsFinal).buildEmailInfo()
								.withSubmitStatusInfo().submitStatus(submitStatus).buildSubmissionInfo()
								.buildPreSubmit();
						
						submitService.preSubmit(company, submissionInfo);

						submitDataStatus = submitData(company, strategyData, userId, sendEmail, confirmationId, resubmitFlag, true, bdmCountsFinal);
						
						if(isSingleClientSubmit) {
							//setting inactive status to the strategies that are not in sync with the submitted strategies.
							strategyDataDao.inactivateUnSubmittedStrategiesByCompany(company);
						}
						
					} else {
						logger.error(
								"Company is picked by default submit query but not recognised as renewal company : {}",
								company.getCode());
						isDeafultEligible = false ;
					}
				} catch (Exception e) {
					CommonUtils.logExceptions(e, logger, companyCode, "");
					submitException = e;
				} finally {
					closeEntityManagers();
					if ((submitDataStatus.equalsIgnoreCase(BSSApplicationConstants.ERROR) || submitException != null) && isDeafultEligible) {
						if (!failedList.contains(companyCode)) {
							failedList.add(companyCode);
							oeQuarter = company.getQuater();
							failedCompanyAndConfNumberDtos.add(prepareCompanyAndConfDTO(company, confirmationId));
						}
					}
				}
			}
			if(!isSingleClientSubmit) {
				//setting inactive status to the strategies that are not in sync with the submitted strategies.
				strategyDataDao.inactivateUnSubmittedStrategiesByPlanYear(realmPlanYearId);
			}
			
			
		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, "001", "");
		} finally {
			logger.info("BSS_INFO : Total companies submitted: {}", companies);
			logger.info("BSS_INFO : Total companies failed = {} and are as follows : {}", failedList.size(),
					failedList);
			if (!isSingleClientSubmit) {
				emailGenService.createDefaultEmail(companies.size() - failedList.size(), userId);
				emailGenService.generateSubmissionIssueReport(null, userId);
			}
		
			if (AppRulesAndConfigsUtils.isSnowEmailsEnabled() && CollectionUtils.isNotEmpty(failedList)) {
				String processType = BSSApplicationConstants.DEFAULT_SUBMIT;
				if (ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName().equals(userId)) {
					processType = ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT.getProcessName();
				}
				emailGenService.sendBssSubmissionFailureEmail(SubmissionEmailDto.builder()
						.companyAndConfNumberDtos(failedCompanyAndConfNumberDtos).userId(userId).sendToBssTeam(true)
						.isSingleClient(isSingleClientSubmit).bssProcessType(processType).oeQuarter(oeQuarter).build());
			}
			long endTime = System.currentTimeMillis();
			logger.info("BSS_INFO : defaultSubmit(): TOOK : {} seconds", (endTime - startTime) / 1000);
		}

	}
	
	
	/**
	 * This method is to add failed list and prepare data for email
	 **/

	private CompanyAndConfNumberDto prepareCompanyAndConfDTO(Company company, String confirmationId) {
		if (null == confirmationId) {
			confirmationId = BSSApplicationConstants.SYNC_FAILURE;
		}
		return CompanyAndConfNumberDto.builder().companyCode(company.getCode()).companyName(company.getName())
				.confirmationNumber(confirmationId).build();
	}
	
	/**
	 * validates the if the company has one or more groups last year and returns
	 * true or false
	 * 
	 * @param company
	 * @return
	 */
	private boolean hasMultipleBenefitPrograms(Company company) {
		Query query = psEntityManager.createNamedQuery(CHK_T2_BEN_PROG_GROUP);
		query.setParameter(BSSQueryConstants.COMPANY, company.getCode());
		query.setParameter(BSSQueryConstants.EFF_DT, company.getPlanStartDate());
		String result = (String) DaoUtils.getSingleResult(query, CHK_T2_BEN_PROG_GROUP);
		return Boolean.parseBoolean(result);
	}

	/**
	 * This method is for update the PS_BEN_DEFN_COST with the right
	 * RateTableId.
	 * @param company BSS Company
	 */
	private void updateLifeDisablityRates(Company company, BenefitGroup group) {
		Query query = psEntityManager.createNamedQuery(GET_LIFE_DISB_COST_FOR_UPDATE);
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, group.getBenefitProgram());
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate());
		List<BenefitPlanRate> benefitPlanRates = new ArrayList<>();
		List<Object[]> results = DaoUtils.getResultList(query, GET_LIFE_DISB_COST_FOR_UPDATE);
		for (Object[] r : results) {
			BenefitPlanRate br = new BenefitPlanRate();
			br.setBenefitProgram((String) r[0]);
			br.setEffDt((Date) r[1]);
			br.setPlanType((String) r[2]);
			br.setOptionId((BigDecimal) r[3]);
			br.setCostId((BigDecimal) r[4]);
			br.setBenefitPlan((String) r[5]);
			benefitPlanRates.add(br);
		}

		for (BenefitPlanRate bpr : benefitPlanRates) {
			String rateId = getLifeDisablityRateId(company, bpr);
			if (null != rateId) {
				updateLifeDisablityRateId(company, bpr, rateId);
			}
		}
	}

	/**
	 * This method is for fetching the RateTableId.
	 * @param company
	 * @param br
	 * @return RateTableId
	 */
	private String getLifeDisablityRateId(Company company, BenefitPlanRate br) {
		Query query = psEntityManager.createNamedQuery(GET_LIFE_DISB_RATE_ID);
		query.setParameter(OE_QUARTER, company.getQuater());
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate());
		query.setParameter(BSSQueryConstants.PLAN_TYPE, br.getPlanType());
		query.setParameter(BENEFIT_PLAN, br.getBenefitPlan());
		String bandCode = null;
		if(Objects.equals(RiskTypeEnum.DIFFERENTIALS, company.getRiskType())){
			bandCode = getBandCode(company, PlanTypesEnum.getName(br.getPlanType()));
		} else {
			BandCodes bandCodes = null;
			if (null != company.getBandCodes()) {
				bandCodes = company.getBandCodes();
				if (Constants.LIFE_CODE.equals(br.getPlanType())) {
					if (null != bandCodes.getLifeBandCode()) {
						bandCode = bandCodes.getLifeBandCode();
					}
				} else if (Constants.LTD_CODE.equals(br.getPlanType()) || Constants.STD_CODE.equals(br.getPlanType())) {
					if (null != bandCodes.getDisBandCode()) {
						bandCode = bandCodes.getDisBandCode();
					}
				}
			}
		}
		if (null != bandCode) {
			query.setParameter("bandCode", bandCode);
		} else {
			Map<String, Object> queryMap = DaoUtils.generateQueryMap(query);
			throw new BSSApplicationException(
					new BSSApplicationError(BSSErrorResponseCodes.BSS_NO_DISABILITY_BAND_CODES,
							BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, PsCompanyDaoImpl.class.getName(),
							String.format("COMPANY HAS NO BAND CODES SET FOR PLAN TYPE : %s", br.getPlanType()),
							GET_LIFE_DISB_RATE_ID, queryMap));
		}
		String rateTableId = null;
		
		List<String> results = DaoUtils.getResultStringList(query, GET_LIFE_DISB_RATE_ID);
		if (!results.isEmpty()) {
			rateTableId = results.get(0);
		}		
		
		logger.info("rateTableId | BenefitPlan  : {} | {}", rateTableId, br.getBenefitPlan());
		return rateTableId;
	}

	private String getBandCode(Company company, String bandCodeType) {
		Date effectiveDate = CommonUtils.formatStringToDate(company.getPlanStartDate(), BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY);
		String bssNaicsCode = company.getBssNaicsCode() == null ? null : String.valueOf(company.getBssNaicsCode());
		BenExchngEnums exchangeEnum = BenExchngEnums.getByBenExchange(company.getRealm().getBenExchange());
		if (exchangeEnum != null) {
			return bandCodesService.getBandCodeByType(bssNaicsCode, effectiveDate, bandCodeType, exchangeEnum);
		}
		return null;
	}

	/**
	 * This method is for update the PS_BEN_DEFN_COST with the right
	 * RateTableId.
	 * @param company
	 * @param br
	 * @param rateTableId
	 * @return
	 */
	private int updateLifeDisablityRateId(Company company, BenefitPlanRate br, String rateTableId) {
		Query query = psEntityManager.createNamedQuery(UPDATE_LIFE_DISB_RATE_ID);
		query.setParameter(BSSQueryConstants.RATE_TBL_ID, rateTableId);
		query.setParameter(BSSQueryConstants.EFF_DATE_STR, company.getPlanStartDate());
		query.setParameter(BSSQueryConstants.BEN_PROGRAM, br.getBenefitProgram());
		query.setParameter(BSSQueryConstants.PLAN_TYPE, br.getPlanType());
		query.setParameter(BSSQueryConstants.OPTION_ID, br.getOptionId());
		query.setParameter("costId", br.getCostId());
		return DaoUtils.executeUpdate(query, UPDATE_LIFE_DISB_RATE_ID);
	}
	
	private void updateInactiveGroups(Company company, long strategyId) {
		
		// query to get inactive groups
		List<String[]> inactiveGroups = this.getInactiveBenefitPrograms( company.getId(), strategyId );

		// deactivate the benefit programs
		if( inactiveGroups != null ) {
			for( String[] gp : inactiveGroups ) {
				benefitProgramCreator.updateInactiveBenefitPrograms( company, gp[0], gp[1], this.psEntityManager );
			}
		}
	}

   /**
    * This method is for inserting rate data into to PS for Auto Selected Plans.
	 * @param company
	 * @param group
	 * @param strategy
	 * @param benOfferExceptions
	 */
	private void updateAutoSelectedPlansFunding(Company company, BenefitGroup group, StrategyData strategy,
			Map<String, Boolean> benOfferExceptions, Map<String, List<PayInRateInfo>> payInRatesMap) {
		List<Contribution> contributions = new ArrayList<>();
		Map<String, List<Contribution>> benefitPlanContributions = null;
		Set<String> autoSelectPlans = new HashSet<>();
		Map<String, List<String>> autoSelectMapByRegion = null;
		Map<String, String> autoSelectPlanTypes = new HashMap<>();
		boolean isPickChoose = RulesAndConfigsUtils.findPickChooseWithExceptions( company );
		if (isPickChoose) {
			Map<String, Set<String>> planTypeSelectedPlans = SubmitServiceHelper.getSelectedBenefitPlans(group);
			if (MapUtils.isNotEmpty(planTypeSelectedPlans)) {
				Set<String> allSelectedPlans = new HashSet<>();
				for (Map.Entry<String, Set<String>> planTypeEntry : planTypeSelectedPlans.entrySet()) {
					allSelectedPlans.addAll(planTypeEntry.getValue());
				}
				Map<String, Map<String, List<String>>> planTypeAutoSelectPlans = realmDataDao
						.getAutoSelectedPlansByRegion(allSelectedPlans, company.getRealmPlanYear().getId());
				for (Map.Entry<String, Map<String, List<String>>> planTypeEntry : planTypeAutoSelectPlans.entrySet()) {
					if (BSSApplicationConstants.MEDICAL_PLAN_TYPE.equals(planTypeEntry.getKey())) {
						autoSelectMapByRegion = planTypeEntry.getValue();
					}
					for (String region : planTypeEntry.getValue().keySet()) {
						autoSelectPlans.addAll(planTypeEntry.getValue().get(region));
						for (String bp : planTypeEntry.getValue().get(region)) {
							autoSelectPlanTypes.put(bp, planTypeEntry.getKey());
						}
					}
				}
			}
		}

		if (CollectionUtils.isNotEmpty(autoSelectPlans)) {
			Map<String, XbssRealmPlyrPlan> plyrPlanMap = realmPlyrPlanService
					.getMapForRealmPlanYear(company.getRealmPlanYear().getId());

			Map<String, List<CoverageLevel>> mapOfCoverageLevels = realmDataDao.getCoverageCodesDescByPlanTypes(
					Constants.primaryPlanTypesCodes, company.getRealmPlanYear().getId());

			Set<String> widelyAvailablePlanSet = benefitPlanDao.getWidelyAvailablePlans(autoSelectPlans, company.getRealmPlanYearId());

			Map<String, String> planVendorMap = realmDataDao.getPlanVendors(autoSelectPlans,
					company.getRealmPlanYearId());

			Map<String, Map<String, Set<Long>>> selectedBenefitProgramPortfolios = strategyGroupDataDao
					.getStrategyPortfoliosByPlanType(strategy.getStrategySummary().getId());
			// Set the carrierId -> minimum funding map to benefitOffers so that
			// UI can use
			// it for min funding calculation.
			List<CarrierMinimumFunding> minFundings = benefitPlanService.getLowestCostPlanPerCarrier(company);
			List<Contribution> benOffersContributions = SubmitServiceHelper.getAllBenOffersContributions(group);
			Map<String, BigDecimal> minimumFundingMap = RenewalServiceHelper.getMinimumFunding(benOffersContributions,
					java.util.Collections.emptyList(), company,
					selectedBenefitProgramPortfolios.get(group.getBenefitProgram()), minFundings);

			// getting funding details
			Map<String, Map<String, Map<String, Object>>> groupFundingDetails = realmDataDao
					.getRenewalFundingDetailsBSS(company.getCode(), company.getRealmPlanYear().getId());

			// getting funding details for previous year
			RealmPlanYear previousYearRealm = realmPlanYearService.getPreviousRealmPlanYear(company.getRealmPlanYear());
			Map<String, Map<String, Map<String, Object>>> previousGroupFundingDetails = realmDataDao
					.getRenewalFundingDetailsBSS(company.getCode(), previousYearRealm.getId());

			Map<String, List<BenefitPlanRate>> rates = planRatesService.getBenefitPlanRatesBy(company);

			if (null != groupFundingDetails && null != groupFundingDetails.get(group.getBenefitProgram())) {
				RenewalServiceHelper.updateFundingDetailsForBasePlan(groupFundingDetails.get(group.getBenefitProgram()),
						rates, company, null, realmDataDao, null, benOfferExceptions);
			}

			String fundingType = "";
			for (String bp : autoSelectPlans) {
				BenefitPlan aBp = new BenefitPlan();
				aBp.setId(bp);
				aBp.setPlanType(autoSelectPlanTypes.get(bp));
				aBp.setVendorId(planVendorMap.get(bp));
				RenewalServiceHelper.addBlankContributions(aBp, mapOfCoverageLevels.get(Constants.MEDICAL));
				aBp.setPpoPlan(widelyAvailablePlanSet.contains(bp));
				aBp.setWidelyAvailablePlan(widelyAvailablePlanSet.contains(bp));
				Map<String, Object> coverageLevelFunding = null;
				if (null != groupFundingDetails && null != groupFundingDetails.get(group.getBenefitProgram())) {
					coverageLevelFunding = groupFundingDetails.get(group.getBenefitProgram())
							.get(autoSelectPlanTypes.get(bp));
				}

				Map<String, Object> previousCoverageLevelFunding = null;
				if (null != previousGroupFundingDetails
						&& null != previousGroupFundingDetails.get(group.getBenefitProgram())) {
					previousCoverageLevelFunding = previousGroupFundingDetails.get(group.getBenefitProgram())
							.get(autoSelectPlanTypes.get(bp));
				}
				
				// If coverageLevelFunding is null, Look at last year for EEC,
				// if EEC then use that funding for BASE FUNDING call
				if ((coverageLevelFunding == null || coverageLevelFunding.isEmpty())
						&& previousCoverageLevelFunding != null && !previousCoverageLevelFunding.isEmpty()
						&& null != previousCoverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE)
						&& BSSApplicationConstants.EEC.equals(
								(String) previousCoverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE))) {
					coverageLevelFunding = previousCoverageLevelFunding;
				}

				if (null != coverageLevelFunding && !coverageLevelFunding.isEmpty()) {
					if (null != coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE)) {
						fundingType = (String) coverageLevelFunding.get(BSSApplicationConstants.FUNDING_TYPE);
					}
					contributions.addAll(RenewalServiceHelper.createUpdateContributionsByBaseFunding(aBp, null,
							rates, null, coverageLevelFunding, null, false));
				} else {
					List<Contribution> contribList = new ArrayList<>();
					RenewalServiceHelper.constructContributionsByPercentIncrease(aBp, null, null, rates, null,
							contribList, null);
					contributions.addAll(contribList);
				}
			}

			Map<String, List<String>> selectedPlansByRegion = realmDataDao.getRegionForSelectedPlans(autoSelectPlans,
					company.getRealmPlanYearId());
			Map<String, List<String>> planRegions = RenewalServiceHelper.preparePlanToRegionMap(selectedPlansByRegion);
			benefitPlanContributions = SubmitServiceHelper.createMapOfContributions(contributions);
			// updating the plan contributions to apply min-funding.
			RenewalServiceHelper.updateContributionsForMinimumFunding(minimumFundingMap, contributions, company,
					planRegions, benefitPlanContributions, fundingType, groupFundingDetails.get(group.getBenefitProgram()));

			benefitPlanContributions = SubmitServiceHelper.createMapOfContributions(contributions);

			int acaFplOpted = strategy.getStrategySummary().isAcaFplOpted() ? BSSApplicationConstants.ACA_FPL_OPTED_IN
					: BSSApplicationConstants.ACA_FPL_OPTED_OUT;
			
			// updating the contributions to meet FPL
			if (RenewalServiceHelper.isFplApplicable(company, acaFplOpted)) {
				Map<String, String> fplPLansByRegion = SubmitServiceHelper
						.findLowCostPpoPlanByRegion(autoSelectMapByRegion, benefitPlanContributions);
				SubmitServiceHelper.setFPLForLowCostPpoPlan(contributions, fplPLansByRegion, company, null);
				benefitPlanContributions = SubmitServiceHelper.createMapOfContributions(contributions);
			}

			// inserting the rate data to people soft.
			insertAutoSelectedBenefitPlanRateData(company, group, benefitPlanContributions, plyrPlanMap,
					autoSelectPlanTypes, payInRatesMap);
		}
	}

	private void deleteBenDefForMedicalException(Company company, Map<String, Boolean> benOfferExceptions,
			BenefitGroup group) {
		boolean medException = benOfferExceptions.get(PlanTypesEnum.MEDICAL.getCode()) != null
				&& benOfferExceptions.get(PlanTypesEnum.MEDICAL.getCode());
		if (medException) {
			Set<String> nomedNotApplicablePlanTypes = new HashSet<>(Arrays.asList("10", "15", "1S", "60", "61", "67"));
			benefitOptionsCreator.clearHSAOptions( company, group );
			benefitProgramCreator.deleteBenDefn(company, group, nomedNotApplicablePlanTypes);
			this.updateProductLine( company, "NOMD" );
		}
	}
	
	private void closeEntityManagers() {
		if (psEntityManager != null && psEntityManager.isOpen()) {
			psEntityManager.close();
		}
		if (bssEM != null && bssEM.isOpen()) {
			bssEM.close();
		}
	}

	/**
	 * Looks up the pay-in rate for a specific benefit plan and coverage level.
	 * The rate is returned with a scale of 2 decimal places using HALF_UP rounding.
	 *
	 * @param payInRatesMap Map of plan IDs to lists of PayInRateInfo
	 * @param planId        The benefit plan ID to look up
	 * @param coverageLevel The coverage level code (e.g., "1", "2", "C", "4")
	 * @return BigDecimal pay-in rate with 2 decimal places precision
	 * @throws BSSBadDataException if any parameter is null, if no rate information exists for the plan ID,
	 *                             or if no pay-in rate is found for the specified coverage level
	 */
	private BigDecimal lookupPayInRate(Map<String, List<PayInRateInfo>> payInRatesMap, String planId, String coverageLevel) {
		if (payInRatesMap == null || planId == null || coverageLevel == null) {
			throw new BSSBadDataException("Invalid arguments for lookupPayInRate: payInRatesMap, planId, and coverageLevel must not be null" );
		}
		List<PayInRateInfo> rateInfos = payInRatesMap.get(planId);
		if (rateInfos == null || rateInfos.isEmpty()) {
			throw new BSSBadDataException(String.format("No pay-in rate information found for plan ID: %s", planId));
		}

		for (PayInRateInfo rateInfo : rateInfos) {
			if (coverageLevel.equals(rateInfo.getCoverageLevel())) {
				return BigDecimal.valueOf(rateInfo.getPayInRate()).setScale(2, RoundingMode.HALF_UP);
			}
		}
		throw new BSSBadDataException(String.format("No pay-in rate found for plan: %s and coverage level: %s", planId, coverageLevel));
	}


}