package com.trinet.ambis.service.email.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.trinet.ambis.common.ApiBssPropertiesConstants;
import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.configuration.BSSMessageConfig;
import com.trinet.ambis.enums.BSSProcessTypeToSNFeatureMapping;
import com.trinet.ambis.enums.CoverageCodesEnums;
import com.trinet.ambis.enums.ExcessOptionEnum;
import com.trinet.ambis.persistence.dao.hrp.CommonDataDao;
import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.service.RealmPlanYearRuleConfigService;
import com.trinet.ambis.service.RealmWaitPeriodService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.service.email.EmailNotificationService;
import com.trinet.ambis.service.email.dto.ClientConversionFailureEmailDto;
import com.trinet.ambis.service.email.dto.CompanyAndConfNumberDto;
import com.trinet.ambis.service.email.dto.SubmissionEmailDto;
import com.trinet.ambis.service.email.dto.SupportEmailDto;
import com.trinet.ambis.service.model.AdditionalBenefitOffer;
import com.trinet.ambis.service.model.BenefitOffer;
import com.trinet.ambis.service.model.BenefitPlan;
import com.trinet.ambis.service.model.ContributionHeader;
import com.trinet.ambis.service.model.ContributionPlan;
import com.trinet.ambis.service.model.FundingBasePlan;
import com.trinet.ambis.service.model.FundingType;
import com.trinet.ambis.service.model.PlanCarrier;
import com.trinet.ambis.service.model.PlanContribution;
import com.trinet.ambis.service.model.PlanPackage;
import com.trinet.ambis.service.model.SelectItem;
import com.trinet.ambis.service.model.StrategyBenefitGroup;
import com.trinet.ambis.service.model.StrategyData;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.service.model.StrategySubmitIssueReport;
import com.trinet.ambis.service.model.StrategySubmitIssueReport.Bdm;
import com.trinet.ambis.service.model.notification.DeliveryChannel;
import com.trinet.ambis.service.model.notification.NotificationMessage;
import com.trinet.ambis.service.model.notification.NotificationRequestParam;
import com.trinet.ambis.service.model.notification.Recipient;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.ambis.util.Constants;
import com.trinet.ambis.util.Utils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author jshuali
 */
@Service
public class EmailGenServiceImpl implements EmailGenService {

	@Autowired
	private HrpDao hrpDao;

	@Autowired
	private RealmDataDao realmDataDao;
	
	@Autowired
	private CommonDataDao commonDataDao;
	
	@Autowired
	private StrategyDataDao strategyDataDao;

	@Autowired
	@Qualifier("realmWaitPeriodServiceImpl")
	private RealmWaitPeriodService waitPeriodService;

	@Autowired
	private EmailNotificationService emailNotificationService;
	
	@Autowired
	private RealmPlanYearRuleConfigService realmPlanYearRuleConfigService;
	
	@Autowired
    private Configuration configuration;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailGenServiceImpl.class);

	private static final String BENEFIT_PLANS = "BENEFIT_PLANS";
	private static final String PLAN_CARRIERS = "PLAN_CARRIERS";
	private static final String FUNDING_BASE_PLAN = "FUNDING_BASE_PLAN";
	private static final String PR = "%";
	private static final String DL = "$";
	private static final String DASH_SEPARATOR = " - ";
	private static final String COMMA_SEPARATOR = ", ";
	private static final String NOT_OFFERED = "Not offered";

	private static final int ANNUAL = 12;
	private static final int MONTHLY = 1;
	
	private static final String ANNUALSTR = "Annual";

	private static final String END_DATE = "endDate";
    private static final String MONTHLYSTR = "Monthly";
    private static final String MONTHLY_ANNUAL = "monthlyAnnual";
    
    private static final String HAS_MEDICAL = "hasMedical";
    private static final String HAS_DENTAL = "hasDental";
    private static final String HAS_VISION = "hasVision";
    private static final String HAS_VOL_DENTAL = "hasVolDental";
    private static final String HAS_VOL_VISION = "hasVolVision";
    
    private static final String MEDICAL_CONTRI_HEADER =  "medicalContributionHeaders";
    private static final String DENTAL_CONTRI_HEADER =  "dentalContributionHeaders";
    private static final String VISION_CONTRI_HEADER =  "visionContributionHeaders";
    private static final String ADDITONAL_BEN_CONTRI_HEADER =  "additionalBenefitContributionHeaders";
    
    private static final String EMAIL_CONFIRMATION_FTL = "emailConfirmation.ftl";
    private static final String EMAIL_CONFIRMATION_BDM_FTL = "emailConfirmationBdm.ftl";
    
    private static final String CONFIRM_DISPLAY_ALL_PLANS = "CONFIRM_DISPLAY_ALL_PLANS";
    
    private static final String NO_EMAIL_ERROR = "NO_EMAIL_ERROR";
    private static final String UPLOAD_ERROR = "UPLOAD_ERROR";
    private static final String NO_USERID = "00000000000";

	private static final String HTML_SINGLE_BREAK = "<BR>";

	@Override
	public void sendBssSubmissionFailureEmail(SubmissionEmailDto submissionEmailDto) {
		String companyCode = BSSApplicationConstants.ADMIN_COMPANY_CODE;
		try {
			// Build Recipients
			List<Recipient> supportRecipients = new ArrayList<>();
			supportRecipients.add(new Recipient(
					BSSMessageConfig.getProperty(ApiBssPropertiesConstants.BSS_SUBMISSION_FAILURE_TO_ADDRESS)));
			if (submissionEmailDto.isSendToBssTeam()) {
				supportRecipients.add(
						new Recipient(BSSMessageConfig.getProperty(ApiBssPropertiesConstants.BSS_TEAM_TO_ADDRESS)));
			}

			// Build Subject line
			List<CompanyAndConfNumberDto> companyAndConfNumberDtos = submissionEmailDto.getCompanyAndConfNumberDtos();
			CompanyAndConfNumberDto companyAndConfNumberDto = companyAndConfNumberDtos.get(0);
			companyCode = submissionEmailDto.isSingleClient() ? companyAndConfNumberDto.getCompanyCode()
					: BSSApplicationConstants.ADMIN_COMPANY_CODE;
			String subject = submissionEmailDto.isSingleClient()
					? String.format(
							BSSMessageConfig.getProperty(
									ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_SINGLE_CLIENT),
							companyCode, companyAndConfNumberDto.getCompanyName())
					: String.format(
							BSSMessageConfig
									.getProperty(ApiBssPropertiesConstants.SUBMISSION_FAILURE_SUBJECT_FOR_QUARTER),
							submissionEmailDto.getOeQuarter());

			// Build Body
			StringBuilder body = new StringBuilder();
			body.append("An error was encountered while submitting to PeopleSoft for the selected Benefit Strategy.");
			body.append(HTML_SINGLE_BREAK);
			body.append(HTML_SINGLE_BREAK);
			body.append("Company - Confirmation Number");
			body.append(HTML_SINGLE_BREAK);
			body.append(HTML_SINGLE_BREAK);
			companyAndConfNumberDtos.forEach(
					dto -> body.append(dto.getCompanyCode() + " - " + dto.getConfirmationNumber() + HTML_SINGLE_BREAK));
			body.append(HTML_SINGLE_BREAK);
			body.append("Feature: " + BSSProcessTypeToSNFeatureMapping.get(submissionEmailDto.getBssProcessType())
					.getServiceNowFeatureName());
			body.append(HTML_SINGLE_BREAK);
			body.append(HTML_SINGLE_BREAK);
			
			// Send Email
			NotificationRequestParam clientRequest = createNotificationRequest(submissionEmailDto.getUserId(),
					companyCode, supportRecipients, body.toString(), subject);
			emailNotificationService.sendConfirmationEmail(clientRequest);
		} catch (Exception e) {
			CommonUtils.logExceptions(e, LOGGER, companyCode, submissionEmailDto.getUserId());
		}
	}

	@Override
	public void createSyncFailureEmail( String company, UUID logEyeCatcher ) {
		try {
			// Build Recipients
			List<Recipient> supportRecipients = new ArrayList<>();
			supportRecipients.add(new Recipient(
					BSSMessageConfig.getProperty( ApiBssPropertiesConstants.SUPPORT_ADDRESS_PROPERTY )));

			// Build Subject line
			String subject = String.format( 
					BSSMessageConfig.getProperty( ApiBssPropertiesConstants.REAL_TIME_SYNC_FAILURE_SUBJECT ),
					company );

			// Build Body
			StringBuilder body = new StringBuilder();
			body.append("An error was encountered while performing real-time sync for company ");
			body.append( company );
			body.append(HTML_SINGLE_BREAK);
			body.append(HTML_SINGLE_BREAK);
			if( logEyeCatcher == null ) {
				// skip log eye-catcher
			} else {
				body.append("Log identifier:");
				body.append("<pre style=\"margin-top: 1px;\">");
				body.append( logEyeCatcher.toString() );
				body.append("</pre>");
			}

			// Send Email
			NotificationRequestParam clientRequest = createNotificationRequest( NO_USERID,
					company, supportRecipients, body.toString(), subject );
			emailNotificationService.sendConfirmationEmail(clientRequest);
		} catch( Exception e ) {
			CommonUtils.logExceptions( e, LOGGER, company, NO_USERID );
		}
	}

    @Override
    public void createSupportEmail(SupportEmailDto supportEmailDto) {
    	String subject = supportEmailDto.getEmailSubject();
    	String body = supportEmailDto.getEmailBody();
    	String userId = supportEmailDto.getUserId();
    	String companyCode = supportEmailDto.getCompanyCode();

    	try {
    		String toAddress = supportEmailDto.getToAddress();
    		List<Recipient> supportRecipients = new ArrayList<>();
    		Recipient rc = new Recipient();
    		rc.setId(toAddress);
    		supportRecipients.add(rc);

    		if (supportEmailDto.isSendToBSS()) {
    			supportRecipients.add(new Recipient("BSS-Team@trinet.com"));
    		}

    		NotificationRequestParam clientRequest = createNotificationRequest(userId, companyCode, supportRecipients,
    				body, subject);
    		emailNotificationService.sendConfirmationEmail(clientRequest);
    	} catch (Exception e) {
    		CommonUtils.logExceptions(e, LOGGER, companyCode, userId);
    	}
    }

	@Override
	public void createDefaultEmail(int count, String userId) {
		List<Recipient> supportRecipients = prepareDefaultSubmitRecipientList();

		String subject = BSSMessageConfig.getProperty("defaultSubject");
		String html = "Benefits Strategy Solutions auto defaulting for clients who did not submit a strategy has successfully completed. "
				+ count + " processed successfully.";
		try {
			NotificationRequestParam clientRequest = createNotificationRequest(userId,
					BSSApplicationConstants.ADMIN_COMPANY_CODE, supportRecipients, html, subject);
			emailNotificationService.sendConfirmationEmail(clientRequest);
		} catch (Exception e) {
			CommonUtils.logExceptions(e, LOGGER, BSSApplicationConstants.ADMIN_COMPANY_CODE, userId);
		}
	}

	@Override
	public void createPreLoadEmail(int count, String failedClients, String userId) {

		List<Recipient> supportRecipients = prepareDefaultSubmitRecipientList();
		
		String subject = "PRE LOAD BSS DETAILS";
		String html = "Benefits Strategy Solutions pre loading for clients who did not create a strategy has successfully completed. "
				+ count + " processed successfully and the following clients" + failedClients + " were errored.";

		try {
			NotificationRequestParam clientRequest = createNotificationRequest(userId,
					BSSApplicationConstants.ADMIN_COMPANY_CODE, supportRecipients, html, subject);
			emailNotificationService.sendConfirmationEmail(clientRequest);
		} catch (Exception e) {
			CommonUtils.logExceptions(e, LOGGER, BSSApplicationConstants.ADMIN_COMPANY_CODE, userId);
		}
	}

	private NotificationRequestParam createNotificationRequest(String userId, String code, List<Recipient> recipients,
			String html, String subject) {
		NotificationRequestParam request = new NotificationRequestParam();
		List<NotificationMessage> messages = new ArrayList<>();
		NotificationMessage message = new NotificationMessage();
		message.setMessageType("html");
		message.setSubject(subject);
		message.setCreateAttachment(false);
		message.setCompanyId(code);
		message.setEmployeeId(userId);
		message.setRecipients(recipients);
		message.setHtmlMsg(html);
		message.setTransformRequired(false);
		messages.add(message);
		DeliveryChannel channel = new DeliveryChannel();
		channel.setChannel("email");
		request.setDeliveryChannel(channel);
		request.setNotificationMessages(messages);
		return request;

	}

	/**
	 * Returns a list of recipients. This list includes the email
	 * addresses configured for the property defaultSubmitEmail.
	 * 
	 * @return
	 */
	private List<Recipient> prepareDefaultSubmitRecipientList() {
		List<Recipient> supportRecipients = new ArrayList<>();
		boolean finished = false;
		for (int i = 1; (!finished); i++) {
			String toAddress = BSSMessageConfig.getProperty("defaultSubmitEmail" + i);
			if (StringUtils.isBlank(toAddress)) {
				finished = true;
			} else {
				Recipient rc = new Recipient();
				rc.setId(toAddress);
				supportRecipients.add(rc);
			}
		}
		return supportRecipients;
	}
	
	@Override
	public String generateBssConfirmationStatementHtml(StrategyData dto, String uniqueId, Company company, String name, String serviceOrderNum) {

	    SchedTbl schedTblDates = company.getSchedTbl();
	    
	    Map<String, Object> parameters = Maps.newHashMap();
	    
	    parameters.put("debug", false);

		parameters.put("renewal", String.valueOf(company.isRenewalCompany()));
		parameters.put("serviceOrderNum", String.valueOf(StringUtils.isNotBlank(serviceOrderNum)));
		parameters.put("recipient", name);
		parameters.put("companyName", company.getName());
		parameters.put("uniqueId", uniqueId);
		parameters.put("strategy", dto.getStrategySummary().getName());
		parameters.put("salesOrder", serviceOrderNum);

		String endDateStr = findEndDate(schedTblDates);
		parameters.put(END_DATE, endDateStr);

		String startDate = Utils.convertDateToEmailString(company.getRealmPlanYear().getPlanYearStart());
		String enddate = Utils.convertDateToEmailString(company.getRealmPlanYear().getPlanYearEnd());
		parameters.put("startTillEndDate", startDate + DASH_SEPARATOR + enddate);

		BigDecimal totalBudget = dto.getStrategySummary().getTotalBudget();
		int budgetFactor = dto.getStrategySummary().getBudgetFactor();
		String annualBudget;
		parameters.put(MONTHLY_ANNUAL, ANNUALSTR);
		if (totalBudget != null && totalBudget != BigDecimal.ZERO) {
			BigDecimal totalAnnaulCost = BigDecimal.ZERO;
			if (budgetFactor == MONTHLY) {
				totalAnnaulCost = totalBudget;
				parameters.put(MONTHLY_ANNUAL, MONTHLYSTR);
			} else if (budgetFactor == ANNUAL || budgetFactor == 0) {
				totalAnnaulCost = totalBudget.multiply(new BigDecimal(12)).setScale(2, RoundingMode.HALF_UP);
				parameters.put(MONTHLY_ANNUAL, ANNUALSTR);
			}
			annualBudget = DL + String.format("%.2f", totalAnnaulCost.doubleValue());
		} else {
			if (budgetFactor == MONTHLY) {
				parameters.put(MONTHLY_ANNUAL, MONTHLYSTR);
			} else if (budgetFactor == ANNUAL || budgetFactor == 0) {
				parameters.put(MONTHLY_ANNUAL, ANNUALSTR);
			}
			annualBudget = "None Specified";
		}
		parameters.put("budget", annualBudget);
		
		//HSA
		StrategyHsaFundingDto hsaFunding = dto.getStrategyHsaFunding();

	    parameters.put("hsaFunding", hsaFunding);
	    
        parameters.put(MEDICAL_CONTRI_HEADER, new ArrayList<ContributionHeader>());
        parameters.put(DENTAL_CONTRI_HEADER, new ArrayList<ContributionHeader>());
        parameters.put(VISION_CONTRI_HEADER, new ArrayList<ContributionHeader>());
        parameters.put(ADDITONAL_BEN_CONTRI_HEADER, Lists.newArrayList());
        
        parameters.put("employeeCoverageCode", CoverageCodesEnums.COV_EMPLOYEE.getName());
        parameters.put("spouseCoverageCode", CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getName());
        parameters.put("childCoverageCode", CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getName());
        parameters.put("familyCovarageCode", CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getName());
		
		setBenefitGroups(dto, company, parameters);
		
		parameters.put("exchange", company.getRealm().getId());
		String emailContentBaseUrl = BSSMessageConfig.getProperty("external.email.content.baseUrl");
		parameters.put("emailContentBaseUrl", emailContentBaseUrl);
		String adminBenefitPortalUrl = BSSMessageConfig.getProperty("admin.benefit.portal.url");
        parameters.put("adminBenefitPortalUrl", adminBenefitPortalUrl);
		
		parameters.put("TriNetIVrule", 0);
		Map<String, String> ruleConfigs = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(company.getRealmPlanYearId());
		if (!ruleConfigs.isEmpty() && ruleConfigs.get(CONFIRM_DISPLAY_ALL_PLANS) != null && ruleConfigs.get(CONFIRM_DISPLAY_ALL_PLANS).equals("true")) {
		    parameters.put("TriNetIVrule", 1);
		}
		
		return transform(parameters, false);
	}
	
	public String transform(Map<String, Object> parameters, boolean isClientEmail) {
		String templateLocation = EMAIL_CONFIRMATION_FTL;
		if(isClientEmail) {
			templateLocation = EMAIL_CONFIRMATION_BDM_FTL;
		}
		LOGGER.info("templateLocation: {}", templateLocation);
		StringWriter dataModel = new StringWriter();

		try {
			Template template = configuration.getTemplate(templateLocation, StandardCharsets.UTF_8.name());
			template.process(parameters, dataModel);
		} catch (TemplateException | IOException e) {
			LOGGER.error(e.getMessage());
		}

		return dataModel.toString();
	}
    
	private void setBenefitGroups(StrategyData dto, Company company, Map<String, Object> parameters) {
	    
	    List<List<ContributionPlan>> groupsMedicalPlanContribution = Lists.newArrayList();
	    List<List<ContributionPlan>> groupsDentalPlanContribution = Lists.newArrayList();
	    List<List<ContributionPlan>> groupsVisionPlanContribution = Lists.newArrayList();
	    List<List<ContributionPlan>> groupsAdditionalPlanContribution = Lists.newArrayList();
	    
	    parameters.put("benefitGroups", dto.getBenefitGroups());
	    parameters.put("isEligAle", String.valueOf(company.isEligAle()));
	    
		int acaFplOpted = dto.getStrategySummary().isAcaFplOpted() ? BSSApplicationConstants.ACA_FPL_OPTED_IN
				: BSSApplicationConstants.ACA_FPL_OPTED_OUT;
		
        parameters.put("AcaFplOpted", String.valueOf(acaFplOpted));
        
        List<SelectItem> bsuppVoluntaryPlanTypes = commonDataDao.getBsuppVolPlanTypes(company.getRealm().getId());
		Map<String,String> waitPeriodMap = waitPeriodService.getWaitPeriodDescr();

		for (StrategyBenefitGroup group : dto.getBenefitGroups()) {
		    
		    List<ContributionPlan> medicalPlanContributions = Lists.newArrayList();
		    groupsMedicalPlanContribution.add(medicalPlanContributions);
		    parameters.put("groupsMedicalPlanContribution", groupsMedicalPlanContribution);
		    
		    List<ContributionPlan> dentalPlanContributions = Lists.newArrayList();
            groupsDentalPlanContribution.add(dentalPlanContributions);
            parameters.put("groupsDentalPlanContribution", groupsDentalPlanContribution);
            
            List<ContributionPlan> visionPlanContributions = Lists.newArrayList();
            groupsVisionPlanContribution.add(visionPlanContributions);
            parameters.put("groupsVisionPlanContribution", groupsVisionPlanContribution);
            
            List<ContributionPlan> additionalPlanContribution = Lists.newArrayList();
            groupsAdditionalPlanContribution.add(additionalPlanContribution);
            parameters.put("groupsAdditionalPlanContribution", groupsAdditionalPlanContribution);
			
			group.setWaitPeriodDescr( waitPeriodMap.get( group.getWaitingPeriod() ) );
			
			setBenefitOffers(group, company,  medicalPlanContributions, dentalPlanContributions, visionPlanContributions, parameters, bsuppVoluntaryPlanTypes);
		}
	}

	private void setBenefitOffers(StrategyBenefitGroup group, Company company,
			List<ContributionPlan> medicalPlanContributions, List<ContributionPlan> dentalPlanContributions,
			List<ContributionPlan> visionPlanContributions, Map<String, Object> parameters, List<SelectItem> bsuppVoluntaryPlanTypes) {
		long groupId = group.getId();
		
		parameters.put(HAS_MEDICAL, false);
		parameters.put(HAS_DENTAL, false);
		parameters.put(HAS_VISION, false);
		parameters.put(HAS_VOL_DENTAL+groupId, false);
		parameters.put(HAS_VOL_VISION+groupId, false);
		
		@SuppressWarnings("unchecked")
		List<ContributionHeader> medContriHeader = (List<ContributionHeader>) parameters.get(MEDICAL_CONTRI_HEADER);
		@SuppressWarnings("unchecked")
		List<ContributionHeader> denContriHeader = (List<ContributionHeader>) parameters.get(DENTAL_CONTRI_HEADER);
		@SuppressWarnings("unchecked")
		List<ContributionHeader> visContriHeader = (List<ContributionHeader>) parameters.get(VISION_CONTRI_HEADER);
		@SuppressWarnings("unchecked")
		List<ContributionHeader> addBenContriHeader = (List<ContributionHeader>) parameters.get(ADDITONAL_BEN_CONTRI_HEADER);

		List<BenefitOffer> additionalOffer = group.getBenefitOffers().stream()
		                                                             .filter(p -> p.getSummary().getType().equals(BSSApplicationConstants.ADDITIONAL))
		                                                             .collect(Collectors.toList()); 
		//The JSON does not have the "type": "additionalBenefit", for this group so fake it.
		if (additionalOffer.isEmpty()) {
		    addBenContriHeader.add(new ContributionHeader());
		}
		
		for (BenefitOffer offer : group.getBenefitOffers()) {
			if (BSSApplicationConstants.MEDICAL.equals(offer.getSummary().getType())) {
				setOffer(offer, company, medicalPlanContributions, medContriHeader, bsuppVoluntaryPlanTypes);
				parameters.put(HAS_MEDICAL, true);
			} else if (BSSApplicationConstants.DENTAL.equals(offer.getSummary().getType())) {
				setOffer(offer, company, dentalPlanContributions, denContriHeader, bsuppVoluntaryPlanTypes);
				parameters.put(HAS_DENTAL, true);
				if (offer.getPlanPackage().isEmployeePaid()) {
					group.setHasVolDental(true);
				}
			} else if (BSSApplicationConstants.VISION.equals(offer.getSummary().getType())) {
				setOffer(offer, company, visionPlanContributions, visContriHeader, bsuppVoluntaryPlanTypes);
				parameters.put(HAS_VISION, true);
				if (offer.getPlanPackage().isEmployeePaid()) {
					group.setHasVolVision(true);
				}
			} else if (BSSApplicationConstants.ADDITIONAL.equals(offer.getSummary().getType())) {
				setAdditionalOffer(offer, addBenContriHeader, group.getType());
			}
		}
	}

	private Map<String, String> getParamsMap(BenefitOffer offer) {

		Map<String, String> paramsMap = new HashMap<>();
		String planCarriersStr;
		String benefitPlansStr;
		String fundingBasePlan = DASH_SEPARATOR;
		String fundingPlanName = DASH_SEPARATOR;

		benefitPlansStr = offer.getBenefitPlans().stream().map(BenefitPlan::getName).filter(StringUtils::isNotBlank)
				.sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.joining(COMMA_SEPARATOR));
		LOGGER.error("Benefit Plan: {}", benefitPlansStr);

		planCarriersStr = offer.getPlanCarriers().stream().map(PlanCarrier::getName).filter(StringUtils::isNotBlank)
				.sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.joining(COMMA_SEPARATOR));

		LOGGER.error("Plan Carriers: {}", planCarriersStr);

		PlanPackage planPackage = offer.getPlanPackage();

		if (!planPackage.isEmployeePaid() && !BSSApplicationConstants.FLAT.equals(planPackage.getFundingType())) {
			List<FundingBasePlan> fundingBasePlans = planPackage.getFundingBasePlans();
			if (CollectionUtils.isNotEmpty(fundingBasePlans)) {
				for (FundingBasePlan fbp : fundingBasePlans) {
					fundingBasePlan = fbp.getFundingBasePlan();
					LOGGER.info("Funding Base Code: {}", fundingBasePlan);
				}
			}
		}

		for (BenefitPlan benefitPlanDTO : offer.getBenefitPlans()) {
			if (benefitPlanDTO.getId().equals(fundingBasePlan)) {
				fundingPlanName = benefitPlanDTO.getName();
				LOGGER.info("Funding Base Plan: {}", fundingPlanName);
			}
		}

		paramsMap.put(BENEFIT_PLANS, benefitPlansStr);
		paramsMap.put(PLAN_CARRIERS, planCarriersStr);
		paramsMap.put(FUNDING_BASE_PLAN, fundingPlanName);

		return paramsMap;
	}

	private void setOffer(BenefitOffer offer, Company company, List<ContributionPlan> planContributions, 
	                      List<ContributionHeader> contributionHeaders, List<SelectItem> bsuppVoluntaryPlanTypes) {
	    
	    ContributionHeader contributionHeader = new ContributionHeader();
	    contributionHeaders.add(contributionHeader);
	    
		Map<String, String> paramsMap = getParamsMap(offer);
		contributionHeader.setPlanCarriers(paramsMap.get(PLAN_CARRIERS));

		// FundingBase Plan variables
		PlanPackage planPackage = offer.getPlanPackage();

		if (null != paramsMap.get(BENEFIT_PLANS)) {
			contributionHeader.setBenefitPlans(paramsMap.get(BENEFIT_PLANS));
		} else {
			contributionHeader.setBenefitPlans(StringUtils.EMPTY);
		}

		if (null != paramsMap.get(BENEFIT_PLANS)) {
		    if (BSSApplicationConstants.FLAT_MAX.equals(planPackage.getFundingBasePlan())) {
		        contributionHeader.setFundingBasePlan(BSSApplicationConstants.FLATMAX_LABEL);
		    }
		    else {
		    	boolean isFundingFlatMax = false;
		        contributionHeader.setFundingBasePlan(paramsMap.get(FUNDING_BASE_PLAN));
		        Map<String, BigDecimal> fundingBasePlanLimits = planPackage.getCoverageLevelBasePlanLimits();
		        BigDecimal fbpEmployeeLimit = fundingBasePlanLimits.get(CoverageCodesEnums.COV_EMPLOYEE.getId());
	            BigDecimal fbpEmployeePlusSpouseLimit = fundingBasePlanLimits.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId());
	            BigDecimal fbpEmployeePlusChildLimit = fundingBasePlanLimits.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId());
	            BigDecimal fbpEmployeePlusFamilyLimit = fundingBasePlanLimits.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId());
		        contributionHeader.setFbpEmployeeLimit(fbpEmployeeLimit != null ? DL + fbpEmployeeLimit.setScale( 2, RoundingMode.HALF_UP).toString()
                        : BSSApplicationConstants.ZERO_DOLLAR);
		        contributionHeader.setFbpEmployeePlusSpouseLimit(fbpEmployeePlusSpouseLimit != null ? DL + fbpEmployeePlusSpouseLimit.setScale( 2, RoundingMode.HALF_UP).toString()
	                     : BSSApplicationConstants.ZERO_DOLLAR);
		        contributionHeader.setFbpEmployeePlusChildLimit(fbpEmployeePlusChildLimit != null ? DL + fbpEmployeePlusChildLimit.setScale( 2, RoundingMode.HALF_UP).toString()
	                     : BSSApplicationConstants.ZERO_DOLLAR);
		        contributionHeader.setFbpEmployeePlusFamilyLimit(fbpEmployeePlusFamilyLimit != null ? DL + fbpEmployeePlusFamilyLimit.setScale( 2, RoundingMode.HALF_UP).toString()
	                     : BSSApplicationConstants.ZERO_DOLLAR);
				contributionHeader.setIsFundingFlatMax(String.valueOf(isFundingFlatMax));
		    }
		} else {
			contributionHeader.setFundingBasePlan(DASH_SEPARATOR);
		}
		
		boolean isWaiverAllowance = false;
        BigDecimal waiverAllowance = planPackage.getWaiverAllowance();
        if (waiverAllowance != null) {
            isWaiverAllowance = true;
            String waiverAll = DL + waiverAllowance.toString();
            contributionHeader.setWaiverAllowance(waiverAll);
        }
        contributionHeader.setIsWaiverAllowance(String.valueOf(isWaiverAllowance));

		Map<String, BigDecimal> coverageLevelFunding = planPackage.getCoverageLevelFunding();
		Map<String, String> covrgCodes = hrpDao.getCovrgCdMap();
		String coverageCode = null;
		
		//Maximum Funding Limit
		boolean isFundingFlatMax = false;
		contributionHeader.setIsFundingFlatMax(String.valueOf(isFundingFlatMax));
	    Map<String, BigDecimal> coverageLevelFundingFlatMax = planPackage.getCoverageLevelFundingFlatMax();
	    
	    if (MapUtils.isNotEmpty(coverageLevelFundingFlatMax)) {
	    
	        isFundingFlatMax = true;
    		BigDecimal employeeFlatMax = coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE.getId());
            BigDecimal employeePlusSpouseFlatMax = coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId());
            BigDecimal employeePlusChildFlatMax = coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId());
            BigDecimal employeePlusFamilyFlatMax = coverageLevelFundingFlatMax.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId());
    
			contributionHeader.setEmployeeFlatMax(employeeFlatMax != null ? DL + employeeFlatMax.setScale(2, RoundingMode.HALF_UP).toString()
			                                     : BSSApplicationConstants.ZERO_DOLLAR);
			contributionHeader.setSpouseFlatMax(employeePlusSpouseFlatMax != null ? DL + employeePlusSpouseFlatMax.setScale(2, RoundingMode.HALF_UP).toString()
							                     : BSSApplicationConstants.ZERO_DOLLAR);
			contributionHeader.setChildFlatMax(employeePlusChildFlatMax != null ? DL + employeePlusChildFlatMax.setScale(2, RoundingMode.HALF_UP).toString()
							                     : BSSApplicationConstants.ZERO_DOLLAR);
			contributionHeader.setFamilyFlatMax(employeePlusFamilyFlatMax != null ? DL + employeePlusFamilyFlatMax.setScale(2, RoundingMode.HALF_UP).toString()
							                     : BSSApplicationConstants.ZERO_DOLLAR);
			contributionHeader.setIsFundingFlatMax(String.valueOf(isFundingFlatMax));
	    }
	    
	    List<FundingType> fundingTypes = realmDataDao.getRealmFundingTypes(company.getRealmPlanYearId());
	    List<String> fundingType = fundingTypes.stream()
	                                           .filter(p -> p.getId().equals(planPackage.getFundingType()))
	                                           .map(FundingType::getDescription)
	                                           .collect(Collectors.toList()); 
	    
		if (planPackage.getFundingType() != null) {
			contributionHeader.setFundingTypeCode(planPackage.getFundingType());
		} else {
			contributionHeader.setFundingTypeCode(DASH_SEPARATOR);
		}
		if (!fundingType.isEmpty()) {
			contributionHeader.setFundingTypeDescription(fundingType.get(0));
		} else {
			contributionHeader.setFundingTypeDescription(DASH_SEPARATOR);
		}

	    BigDecimal companyPercent = coverageLevelFunding.get(CoverageCodesEnums.COV_ALL.getId());
	    BigDecimal employee = coverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE.getId());
	    BigDecimal employeePlusSpouse = coverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE.getId());
	    BigDecimal employeePlusFamily = coverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE_FAMILY.getId());
	    BigDecimal employeePlusChild = coverageLevelFunding.get(CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD.getId());
		
		// Base Plan Percent
		if (BSSApplicationConstants.BFPCT.equals(planPackage.getFundingType())) {
		    
			if (!coverageLevelFunding.isEmpty()) {
				coverageCode = coverageLevelFunding.keySet().iterator().next();
				companyPercent = coverageLevelFunding.get(coverageCode);
			}
			contributionHeader.setCompanyPercent(companyPercent != null ? companyPercent.toString().concat(PR) : BSSApplicationConstants.ZERO_PERCENT);
			contributionHeader.setCoverageLevel(covrgCodes.get(coverageCode));
			
		// Covered Person Percent
		} else if (BSSApplicationConstants.CFPCT.equals(planPackage.getFundingType())) {
			setCoverageLevelFunding(contributionHeader, companyPercent, employee, employeePlusSpouse,
					employeePlusFamily, employeePlusChild, PR);
		// Flat
		} else if (BSSApplicationConstants.FLAT.equals(planPackage.getFundingType())) {
			setCoverageLevelFunding(contributionHeader, companyPercent, employee, employeePlusSpouse,
					employeePlusFamily, employeePlusChild, DL);
		} else if (BSSApplicationConstants.BSUPP.equals(planPackage.getFundingType())) {
			String surExcessOption = getSurplusExcessOptionDescr(planPackage.getBsuppExcessOption());
			List<String> bsuppSelectedVolPlanTypes = planPackage.getBsuppSelectedVolPlanTypes();
			contributionHeader.setSurBenSupplement(surExcessOption);
			contributionHeader.setSurBenSupplementId(planPackage.getBsuppExcessOption().toString());
			if (ExcessOptionEnum.FORFEIT.getType() == Integer.valueOf(planPackage.getBsuppExcessOption().toString())) {
				List<String> planDesc = bsuppVoluntaryPlanTypes.stream()
						.filter(e -> bsuppSelectedVolPlanTypes.contains(e.getId()))
						.filter(e -> !BSSApplicationConstants.BSUPP_ALL_VOL_PLAN_ID.equals(e.getId()))
						.map(SelectItem::getDescription).collect(Collectors.toList());
				contributionHeader.setSurPlanAllocation(planDesc);
			}
			setCoverageLevelFunding(contributionHeader, companyPercent, employee, employeePlusSpouse,
					employeePlusFamily, employeePlusChild, DL);
		} else {
			setCoverageLevelFunding(contributionHeader, null, null, null, null, null, DL);
		} 
		setPlanSelections(offer, planContributions, company);
	}

	private void setAdditionalOffer(BenefitOffer offer, List<ContributionHeader> additionalBenefitContributionHeaders , String groupType) {

	    ContributionHeader contributionHeader = new ContributionHeader();
        if(BSSApplicationConstants.ADDITIONAL.equals(offer.getSummary().getType())){
            additionalBenefitContributionHeaders.add(contributionHeader);
        }
        
	    List<AdditionalBenefitOffer> abs = offer.getAdditionalBenefitOffers();
		boolean disabilityOffered = false;
		boolean lifeOffered = false;
		boolean cmtrOffered = false;
		if (abs != null && !abs.isEmpty()) {
			for (AdditionalBenefitOffer ab : abs) {
				if (Constants.DISABILITY.equals(ab.getSummary().getType())) {
					contributionHeader.setDisability(ab.getAdditionalBenefitPlans().get(0).getDescription());
					disabilityOffered = true;
				} else if (Constants.LIFE.equalsIgnoreCase(ab.getSummary().getType())) {
					contributionHeader.setLife(ab.getAdditionalBenefitPlans().get(0).getDescription());
					lifeOffered = true;
				} else if (Constants.CMTR.equals(ab.getSummary().getType())
						&& !BSSApplicationConstants.K1_GROUP_TYPE.equals(groupType)) {
					contributionHeader.setCommuter("Offering");
					cmtrOffered = true;
				}
			}
		}

		if (!disabilityOffered) {
			contributionHeader.setDisability(NOT_OFFERED);
		}
		if (!lifeOffered) {
			contributionHeader.setLife(NOT_OFFERED);
		}
		if (!cmtrOffered) {
		    contributionHeader.setCommuter(NOT_OFFERED);
		}
	}

	private void setPlanSelections(BenefitOffer offer, List<ContributionPlan> planContributions, Company company) {
	    
	    Collection<BenefitPlan> benefitPlans = offer.getBenefitPlans();
	    
		// define a format for the rate dollar amounts
		DecimalFormat rateFormat = new DecimalFormat("###,##0.00");

		// create a map for translating the covrgCd code-strings to proper labels
		Map<String, String> covrgCodes = hrpDao.getCovrgCdMap();

		boolean includePlan = false;
		Map<String, String> ruleConfigs = realmPlanYearRuleConfigService.getRulesAndConfigsByRealmPlanYearId(company.getRealmPlanYearId());
        if (!ruleConfigs.isEmpty() && ruleConfigs.get(CONFIRM_DISPLAY_ALL_PLANS) != null &&
            ruleConfigs.get(CONFIRM_DISPLAY_ALL_PLANS).equals("true")) {
            includePlan = true;
        }
		
		for (BenefitPlan benefitPlanDTO : benefitPlans) {
		    
			String planName = benefitPlanDTO.getName();

			List<PlanContribution> pcs = benefitPlanDTO.getContributions();
			for (String coverageCode : covrgCodes.keySet()) {
			    
				for (PlanContribution pc : pcs) {
				    
				        //If the JSON's "overrideType": "BASE" - do not display the plan in the email 
				        //unless CONFIRM_DISPLAY_ALL_PLANS is true
    					if( BSSApplicationConstants.PLAN_OVERRIDE_BASE.equals(pc.getOverrideType()) && !includePlan ) {
    						continue;
    					}

					if (coverageCode.equalsIgnoreCase(pc.getType())) {
						ContributionPlan planContributionOverride = preparePlanContributionOverride(rateFormat, covrgCodes, planName, pc);

				        planContributions.add(planContributionOverride);
					}
				}
			}
		}
	}

	private ContributionPlan preparePlanContributionOverride(DecimalFormat rateFormat, Map<String, String> covrgCodes,
			String planName, PlanContribution pc) {
	    
		String companyPercentStr;
		String companyCostStr;
		String employeeCostStr;
		String employeePercentCostStr;
		BigDecimal companyPercent = pc.getEmployerPercent().setScale(2, RoundingMode.HALF_UP);
		BigDecimal employeePercent = new BigDecimal(100).subtract(companyPercent).setScale(2, RoundingMode.HALF_UP);
		BigDecimal companyCost = pc.getEmployerContribution();

		companyPercentStr = companyPercent.toString().concat(PR);
		employeePercentCostStr = employeePercent.toString().concat(PR);

		if (companyCost == null) {
			if (companyPercent.compareTo(BigDecimal.ZERO) > 0) {
				companyCost = pc.getPlanCost().multiply(pc.getEmployerPercent().divide(new BigDecimal(100))).setScale(2, RoundingMode.HALF_UP);
			} else {
				companyCost = BigDecimal.ZERO;
			}
		}
		
		BigDecimal employeeCost = BigDecimal.ZERO;
		if (pc.getPlanCost() != null) {
		    employeeCost = pc.getPlanCost().subtract(companyCost);
		}
		companyCostStr = DL.concat(rateFormat.format(companyCost));
		employeeCostStr = DL.concat(rateFormat.format(employeeCost));

		LOGGER.info("Company percentage: {}, Employee percentages: {}, Company Cost: {}, Employee Cost: {}",
				companyPercentStr, employeePercentCostStr, companyCostStr, employeeCostStr);

		ContributionPlan planContributionOverride = new ContributionPlan();
		planContributionOverride.setPlanName(planName);
		planContributionOverride.setCoverageCode(covrgCodes.get(pc.getType()));
		planContributionOverride.setCompanyPercent(companyPercentStr);
		planContributionOverride.setCompanyCost(companyCostStr);
		planContributionOverride.setEmployeePercent(employeePercentCostStr);
		planContributionOverride.setEmployeeCost(employeeCostStr);
		return planContributionOverride;
	}

	private String findEndDate(SchedTbl schedTblDates) {
		String endDateStr;
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		if (schedTblDates.getCloseDate() != null) {
			if (schedTblDates.getExtensionEndDate() != null) {
				// both dates exist; use whichever date is later
				if (schedTblDates.getCloseDate().compareTo(schedTblDates.getExtensionEndDate()) > 0) {
					// use close date
					endDateStr = formatter.format(schedTblDates.getCloseDate());
				} else {
					// use extension date
					endDateStr = formatter.format(schedTblDates.getExtensionEndDate());
				}
			} else {
				// only closeDate exists; use that one
				endDateStr = formatter.format(schedTblDates.getCloseDate());
			}
		} else {
			if (schedTblDates.getExtensionEndDate() != null) {
				// only extensionDate exists; use that one
				endDateStr = formatter.format(schedTblDates.getExtensionEndDate());
			} else {
				// neither date is available; send "date missing"
				endDateStr = "(deadline date missing)";
			}
		}
		return endDateStr;
	}

	@Override
	public void generateSubmissionIssueReport(String companyCode, String userId) {
		if (companyCode == null) {
			companyCode = BSSApplicationConstants.ADMIN_COMPANY_CODE;
		}

		Map<String, List<StrategySubmitIssueReport>> reportDataMap = getSubmittedStrategyIssueReportDataMap();

		sendUploadSupportEmail(companyCode, userId, reportDataMap);

	}
    
	private Map<String, List<StrategySubmitIssueReport>> getSubmittedStrategyIssueReportDataMap() {
		Map<String, List<StrategySubmitIssueReport>> returnData = new HashMap<>();
		List<StrategySubmitIssueReport> noEmailSentList = new ArrayList<>();
		List<StrategySubmitIssueReport> uploadErrorList = new ArrayList<>();

		List<StrategySubmitIssueReport> reportData = strategyDataDao.getSubmittedStrategyIssueReportData();
		for (StrategySubmitIssueReport reportRow : reportData) {
			if (BSSApplicationConstants.ERROR.equals(reportRow.getStatementUploadStatus())) {
				uploadErrorList.add(reportRow);
			}
			else if (!reportRow.isEmailSent()) {
				noEmailSentList.add(reportRow);
			}
		}
		returnData.put(NO_EMAIL_ERROR, noEmailSentList);
		returnData.put(UPLOAD_ERROR, uploadErrorList);
		return returnData;
	}

	private void sendUploadSupportEmail(String companyCode, String userId,
			Map<String, List<StrategySubmitIssueReport>> reportDataMap) {
		try {
			String toAddress = BSSMessageConfig.getProperty("Tier2SupportEmail");
			List<Recipient> supportRecipients = new ArrayList<>();
			Recipient rc = new Recipient();
			rc.setId(toAddress);
			supportRecipients.add(rc);

			String subject = "BSS Support:  BSS Confirmation Not Uploaded";
			String html = "Tier II,<BR><BR>"
					+ "The confirmation statements listed below did not upload to BSS.  Please work to resolve the issues.<BR><BR>"
					+ "Sincerely,<BR><BR>Benefits Strategy Solutions";
			if (reportDataMap.containsKey(UPLOAD_ERROR)) {
				html = html.concat(
						"<BR><BR>The following customers' confirmation statements did not upload into BSS.<BR><BR>");
				html = html.concat(generateErrorEmailBody(reportDataMap.get(UPLOAD_ERROR)));
			}
			NotificationRequestParam clientRequest = createNotificationRequest(userId, companyCode, supportRecipients,
					html, subject);
			emailNotificationService.sendConfirmationEmail(clientRequest);
		} catch (Exception e) {
			CommonUtils.logExceptions(e, LOGGER, companyCode, userId);
		}
	}
	
	private String generateErrorEmailBody(List<StrategySubmitIssueReport> reportData) {
		
		String tr        		= "<tr valign=top>";
	    String tdStyle          = "width=175 valign=top style=\"border-width : 1px; border-color: #bfbfbf; border-style: solid; padding: 0px;\"";
	    String tableStyle       = "\"border-width: 0px; border-collapse: collapse;\"";
	    
		StringBuilder emailBody = new StringBuilder();
		emailBody.append("<table border=1 cellpadding=0 cellspacing=-1 style=" + tableStyle + ">");
		emailBody.append(tr);
		emailBody.append("<td width=350 valign=top style=" + tdStyle + "> <p>Customer</br>Company Number</p> </td>");
		emailBody.append("<td width=250 valign=top style=" + tdStyle + "> <p>Customer</br>Exchange</p> </td>");
		emailBody.append("<td width=350 valign=top style=" + tdStyle + "> <p>Customer</br>Benefit Quarter ID</p> </td>");
		emailBody.append("<td width=485 valign=top style=" + tdStyle + "> <p>Customer</br>Legal Name</p> </td>");
		emailBody.append("<td width=485 valign=top style=" + tdStyle + "> <p>Customer</br>DBA Name</p> </td>");
		emailBody.append(
				"<td width=425 valign=top style=" + tdStyle + "> <p>Submission or</br>Default Submit Date</p> </td>");
		emailBody.append("<td width=485 valign=top style=" + tdStyle
				+ "> <p>Customer</br>Benefit Decision Maker</br>Employee ID</p> </td>");
		emailBody.append(
				"<td width=485 valign=top style=" + tdStyle + "> <p>Customer</br>Benefit Decision Maker</br>Name</p> </td>");
		emailBody.append("<td width=250 valign=top style=" + tdStyle + "> <p>Submitter</p> </td>");
		emailBody.append("</tr>");

		for (StrategySubmitIssueReport reportRow : reportData) {
			emailBody.append(tr);

			emailBody.append("<td rowspan=" + reportRow.getBdms().size() + ">" + reportRow.getCompanyCode() + "</td>");
			emailBody.append("<td rowspan=" + reportRow.getBdms().size() + ">" + reportRow.getExchange() + "</td>");
			emailBody.append("<td rowspan=" + reportRow.getBdms().size() + ">" + reportRow.getOeQuarter() + "</td>");
			emailBody.append(
					"<td rowspan=" + reportRow.getBdms().size() + ">" + reportRow.getCompanyLegalName() + "</td>");
			emailBody.append("<td rowspan=" + reportRow.getBdms().size() + ">" + reportRow.getCompanyName() + "</td>");
			emailBody
					.append("<td rowspan=" + reportRow.getBdms().size() + ">" + reportRow.getSubmitDateStr() + "</td>");

			int bdmCount = 0;

			for (Bdm bdm : reportRow.getBdms()) {
				if (bdmCount > 0) {
					emailBody.append(tr);
				}
				emailBody.append("<td>" + bdm.getEmployeeId() + "</td>");
				emailBody.append("<td>" + bdm.getEmployeeFirstName() + " " + bdm.getEmployeeLastName() + "</td>");
				if (reportRow.isSubmittedByBdm()) {
					emailBody.append("<td>" + (("Y").equals(bdm.getSubmitter()) ? "Submitted" : " ") + "</td>");
				}
				else if (bdmCount == 0) {
					emailBody.append("<td rowspan=" + reportRow.getBdms().size() + ">BSS Default Submit</td>");
				}
				emailBody.append("</tr>");
				bdmCount++;
			}
		}
		emailBody.append("</table>");
		return emailBody.toString();
	}

	private void setCoverageLevelFunding(ContributionHeader contributionHeader, BigDecimal companyPercent,
			BigDecimal employee, BigDecimal employeePlusSpouse, BigDecimal employeePlusFamily,
			BigDecimal employeePlusChild, String dlPr) {
	    
	    String dollar = "";
	    String percent = "";
	    
	    if (DL.equals(dlPr)) dollar = DL;
	    else percent = PR;
	    
		String zeroDlOrPr = DL.equals(dlPr) ? BSSApplicationConstants.ZERO_DOLLAR: BSSApplicationConstants.ZERO_PERCENT;
		
		contributionHeader.setCompanyPercent(companyPercent != null ? dollar + companyPercent.setScale(2, RoundingMode.HALF_UP).toString() + percent : zeroDlOrPr);
		contributionHeader.setEmployeePercent(employee != null ? dollar + employee.setScale(2, RoundingMode.HALF_UP).toString() + percent : zeroDlOrPr);
		contributionHeader.setEmployeePlusSpousePercent(employeePlusSpouse != null ? dollar + employeePlusSpouse.setScale(2, RoundingMode.HALF_UP).toString() + percent : zeroDlOrPr);
		contributionHeader.setEmployeePlusChildPercent(employeePlusChild != null ? dollar + employeePlusChild.setScale(2, RoundingMode.HALF_UP).toString() + percent : zeroDlOrPr);
		contributionHeader.setEmployeePlusFamilyPercent(employeePlusFamily != null ? dollar + employeePlusFamily.setScale(2, RoundingMode.HALF_UP).toString() + percent : zeroDlOrPr);
	}
    
	private String getSurplusExcessOptionDescr(BigDecimal bsuppExcessOption) {
		String descr = null;
		switch (bsuppExcessOption.toString()) {
		case "1":
			descr = ExcessOptionEnum.CASH.getName();
			break;
		case "2":
			descr = "Apply toward cost of other plans";
			break;
		case "3":
			descr = ExcessOptionEnum.OTHER.getName();
			break;
		default:
			break;
		}
		return descr;
	}

    @Override
	public boolean isEmailAvailableForCompany(Map<String, Integer> adminCounts, String companyCode) {
		return (null != adminCounts && null != adminCounts.get(companyCode) && adminCounts.get(companyCode) > 0);
	}
    
    @Override
	public Set<String> getAdminEmails(String companyCode) {
		if (BSSApplicationConstants.ADMIN_COMPANY_CODE.equals(companyCode)) {
			return hrpDao.getRoleEmails(companyCode, "BEN_CORP_AD");
		} else {
			return hrpDao.getBDMEmails(companyCode);
		}
	}
    
    @Override
	public Map<String, Integer> getAdminEmailCount(String companyCode) {
		if (BSSApplicationConstants.ADMIN_COMPANY_CODE.equals(companyCode)) {
			return getBenCorpAdminCount(Arrays.asList(companyCode));
		} else {
			return getBDMCount(Arrays.asList(companyCode));
		}
	}

    @Override
	public Map<String, Integer> getBDMCount(List<String> companyList) {
		return hrpDao.getBDMCount(companyList);
	}

    @Override
	public Map<String, Integer> getBenCorpAdminCount(List<String> companyList) {
		return hrpDao.getBenCorpAdminCount(companyList);
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	// @formatter:on

}
