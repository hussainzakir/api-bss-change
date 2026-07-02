/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.StrategyDao;
import com.trinet.ambis.persistence.dao.hrp.StrategyPreLoadDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.ProcessStatus;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.ProcessStatusService;
import com.trinet.ambis.service.StrategyService;
import com.trinet.ambis.service.email.EmailGenService;
import com.trinet.ambis.util.ApplicationContextProvider;
import com.trinet.ambis.util.CommonUtils;

/**
 * @author rvutukuri
 *
 */
public class StrategyPreLoadDaoImpl implements StrategyPreLoadDao {

	private PsDao psDao;
	private CompanyService companyService;
	private StrategyService strategyService;
	private EmailGenService emailGenService;
	private StrategyDao strategyDao;
	private ProcessStatusService processStatusService;

	private static final Logger logger = LoggerFactory.getLogger(StrategyPreLoadDaoImpl.class);

	public void createEntityManager() {
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		if (context != null) {
			emailGenService = (EmailGenService) context.getBean("emailGenService");
			psDao = (PsDao) context.getBean("psDao");
			companyService = (CompanyService) context.getBean("companyService");
			strategyService = (StrategyService) context.getBean("strategyService");
			strategyDao = (StrategyDao) context.getBean("strategyDao");
			processStatusService = (ProcessStatusService) context.getBean("processStatusService");

		}
	}

	@Override
	public void preLoadBssStrategies(String peodId, String quarter, Long relamYearId, Date payrollCutOffDate,
			String emplId) {
		long startTime = System.currentTimeMillis();
		ProcessStatus ps = processStatusService.createPreLoadProcess(quarter, emplId);
		List<String> companyList = psDao.getPreLoadClients(peodId, quarter, relamYearId, payrollCutOffDate);
		List<String> failedList = new ArrayList<>();
		try {
			if (null != companyList && !companyList.isEmpty()) {
				for (String companyCode : companyList) {
					boolean processStatusFlag = processStatusService.isStrategySummariesProcessed(companyCode);
					if (processStatusFlag) {
						try {
							createEntityManager();
							Company company = companyService.getCompanyDetails(companyCode, false, emplId, null);
							if (company.isRenewalCompany()) {
								List<Strategy> strategies = strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE);

								// creating future strategies if the strategies
								// don't exist.
								if (strategies.isEmpty()) {
									strategyService.createFutureStrategies(company, false, true);
								}
							}
						} catch (Exception e) {
							CommonUtils.logExceptions(e, logger, companyCode, "");
							failedList.add(companyCode);
						}
					}
				}
			}
		} catch (Exception e) {
			CommonUtils.logExceptions(e, logger, "001", "");
		} finally {
			if (null != ps) {
				ps.setProcessStatus(BSSApplicationConstants.PROCESS_STATUS_PROCESSED);
				processStatusService.updateProcessStatus(ps);
			}
			emailGenService.createPreLoadEmail(companyList == null ? 0 : companyList.size() - failedList.size(),
					failedList.toString(), emplId);
			long endTime = System.currentTimeMillis();
			logger.info("BSS_INFO : defaultSubmit(): TOOK : " + (endTime - startTime) / 1000 + " seconds");
		}
	}

	@Override
	public void preLoadClientStrategies(Company company) {
		if (company.isRenewalCompany()) {
			List<Strategy> strategies = strategyDao.findByCompanyIdAndStatus(company.getId(), BSSApplicationConstants.STATUS_ACTIVE);
			boolean processStatusFlag = processStatusService.isStrategySummariesProcessed(company.getCode());
			// creating future strategies if the strategies
			// don't exist.
			if (strategies.isEmpty() && processStatusFlag) {
				strategyService.createFutureStrategies(company, false, true);
			}
		}
	}

}
