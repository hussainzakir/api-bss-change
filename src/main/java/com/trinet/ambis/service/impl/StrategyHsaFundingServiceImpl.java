package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.StrategyHsaFundingDao;
import com.trinet.ambis.persistence.dao.ps.RenewalDataDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Strategy;
import com.trinet.ambis.persistence.model.StrategyHsaFunding;
import com.trinet.ambis.service.StrategyHsaFundingService;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.util.CommonUtils;

/**
 * @author hliddle
 */
@Service
public class StrategyHsaFundingServiceImpl implements StrategyHsaFundingService {

	@Autowired
	StrategyHsaFundingDao strategyHsaFundingDao;
	
	@Autowired
	RenewalDataDao renewalDataDao;

	@Override
	public StrategyHsaFundingDto findById(long id) {
		StrategyHsaFundingDto result = null;
		StrategyHsaFunding strategyHsaFunding = strategyHsaFundingDao.findByStrategyId(id);
		if (null != strategyHsaFunding) {
			result = CommonUtils.createNewObjectUsing(strategyHsaFunding, StrategyHsaFundingDto.class);
		}
		return result;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public StrategyHsaFundingDto save(StrategyHsaFundingDto strategyHsaFundingDto) {

		StrategyHsaFunding entity = CommonUtils.createNewObjectUsing(strategyHsaFundingDto, StrategyHsaFunding.class);
		strategyHsaFundingDao.save(entity);
		return findById(entity.getStrategyId());
		
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public List<StrategyHsaFundingDto> saveAll(List<StrategyHsaFundingDto> strategyHsaFundingDtoList) {
		List<StrategyHsaFundingDto> newStrategyHsaFundingDtoList = new ArrayList<>();
		for (StrategyHsaFundingDto strategyHsaFundingDto : strategyHsaFundingDtoList) {
			newStrategyHsaFundingDtoList.add(save(strategyHsaFundingDto));
		}
		return newStrategyHsaFundingDtoList;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void createFutureStrategyHsaFunding(List<Strategy> strategyList, Company company,
			Map<String, String> realmRuleConfigurations) {

		// Getting the current HSA information from PS
		StrategyHsaFundingDto currentHsaFunding = renewalDataDao.getPsHsaFundingDetails(company.getCode(),
				company.getRealmPlanYear().getPlanYearStart());

		if (currentHsaFunding != null) {
			
			//Verify family amount(s) are at least equal to the employee amount(s)
			validateHsaFamilyAmounts(currentHsaFunding);

			// Get total contributions to increase/decrease funding as needed
			BigDecimal totalEeContribution = BigDecimal.ZERO;
			BigDecimal totalFamilyContribution = BigDecimal.ZERO;
			
			if (BSSApplicationConstants.HSA_ANNUAL.equals(currentHsaFunding.getLumpSumFrequency())) {
				totalEeContribution = totalEeContribution.add(currentHsaFunding.getAnnualEeAmount());
				totalFamilyContribution = totalFamilyContribution.add(currentHsaFunding.getAnnualFamilyAmount());
			}
			else if (BSSApplicationConstants.HSA_QUARTERLY.equals(currentHsaFunding.getLumpSumFrequency())) {
				totalEeContribution = totalEeContribution.add(currentHsaFunding.getQuarterlyEeAmount().multiply(new BigDecimal(4)));
				totalFamilyContribution = totalFamilyContribution.add(currentHsaFunding.getQuarterlyFamilyAmount().multiply(new BigDecimal(4)));
			}
			
			if (BSSApplicationConstants.HSA_MONTHLY.equals(currentHsaFunding.getContributionFrequency())) {
				totalEeContribution = totalEeContribution.add(currentHsaFunding.getMonthlyEeAmount().multiply(new BigDecimal(12)));
				totalFamilyContribution = totalFamilyContribution.add(currentHsaFunding.getMonthlyFamilyAmount().multiply(new BigDecimal(12)));
			}
			
			// If the client isn't meeting minimums, increase the values.
			validateHsaMinimumAmounts(currentHsaFunding, realmRuleConfigurations, totalEeContribution, totalFamilyContribution);

			// If the client is contributing more than the maximums, decrease
			// the values.
			validateHsaMaximumAmounts(currentHsaFunding, realmRuleConfigurations, totalEeContribution, totalFamilyContribution);		

			List<StrategyHsaFundingDto> strategyHsaFundingDtoList = new ArrayList<>();
			for (Strategy strategy : strategyList) {
				Long strategyId = strategy.getId();
				StrategyHsaFundingDto strategyHsaFundingDto = new StrategyHsaFundingDto(currentHsaFunding, strategyId);
				strategyHsaFundingDtoList.add(strategyHsaFundingDto);
			}
			saveAll(strategyHsaFundingDtoList);
		}
	}
	
	/**
	 * If the HSA family amount(s) are not at least the employee amount(s),
	 * increase them.
	 * 
	 * @param currentHsaFunding
	 */
	private void validateHsaFamilyAmounts(StrategyHsaFundingDto strategyHsaFundingDto) {
		if (strategyHsaFundingDto.getAnnualEeAmount() != null && strategyHsaFundingDto.getAnnualFamilyAmount() != null
				&& strategyHsaFundingDto.getAnnualEeAmount().compareTo(strategyHsaFundingDto.getAnnualFamilyAmount()) > 0) {
			strategyHsaFundingDto.setAnnualFamilyAmount(strategyHsaFundingDto.getAnnualEeAmount());
		}
		if (strategyHsaFundingDto.getMonthlyEeAmount() != null && strategyHsaFundingDto.getMonthlyFamilyAmount() != null
				&& strategyHsaFundingDto.getMonthlyEeAmount().compareTo(strategyHsaFundingDto.getMonthlyFamilyAmount()) > 0) {
			strategyHsaFundingDto.setMonthlyFamilyAmount(strategyHsaFundingDto.getMonthlyEeAmount());
		}
		if (strategyHsaFundingDto.getQuarterlyEeAmount() != null && strategyHsaFundingDto.getQuarterlyFamilyAmount() != null
				&& strategyHsaFundingDto.getQuarterlyEeAmount()
						.compareTo(strategyHsaFundingDto.getQuarterlyFamilyAmount()) > 0) {
			strategyHsaFundingDto.setQuarterlyFamilyAmount(strategyHsaFundingDto.getQuarterlyEeAmount());
		}
	}
	
	/**
	 * If the client isn't meeting HSA minimums, increase the values.
	 * 
	 * @param currentHsaFunding
	 * @param realmRuleConfigurations
	 * @param totalEeContribution
	 * @param totalFamilyContribution
	 */
	private void validateHsaMinimumAmounts(StrategyHsaFundingDto strategyHsaFundingDto,
			Map<String, String> realmRuleConfigurations, BigDecimal totalEeContribution,
			BigDecimal totalFamilyContribution) {

		BigDecimal hsaAnnualEeMinimum = realmRuleConfigurations.get("HSA_ANNUAL_EMPLOYEE_MINIMUM") == null
				? BigDecimal.ZERO : new BigDecimal(realmRuleConfigurations.get("HSA_ANNUAL_EMPLOYEE_MINIMUM"));
		if (hsaAnnualEeMinimum.compareTo(totalEeContribution) > 0) {
			BigDecimal belowAmount = hsaAnnualEeMinimum.subtract(totalEeContribution);
			if (BSSApplicationConstants.HSA_ANNUAL.equals(strategyHsaFundingDto.getLumpSumFrequency())) {
				strategyHsaFundingDto.setAnnualEeAmount(strategyHsaFundingDto.getAnnualEeAmount().add(belowAmount));
			} else if (BSSApplicationConstants.HSA_QUARTERLY.equals(strategyHsaFundingDto.getLumpSumFrequency())) {
				strategyHsaFundingDto.setQuarterlyEeAmount(strategyHsaFundingDto.getQuarterlyEeAmount()
						.add(belowAmount.divide(new BigDecimal(4), 2, RoundingMode.HALF_UP)));
			} else if (BSSApplicationConstants.HSA_MONTHLY.equals(strategyHsaFundingDto.getContributionFrequency())) {
				strategyHsaFundingDto.setMonthlyEeAmount(strategyHsaFundingDto.getMonthlyEeAmount()
						.add(belowAmount.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP)));
			}
			totalEeContribution = hsaAnnualEeMinimum;
		}

		BigDecimal hsaAnnualFamilyMinimum = realmRuleConfigurations.get("HSA_ANNUAL_FAMILY_MINIMUM") == null
				? BigDecimal.ZERO : new BigDecimal(realmRuleConfigurations.get("HSA_ANNUAL_FAMILY_MINIMUM"));
		if (hsaAnnualFamilyMinimum.compareTo(totalFamilyContribution) > 0) {
			BigDecimal belowAmount = hsaAnnualFamilyMinimum.subtract(totalFamilyContribution);
			if (BSSApplicationConstants.HSA_ANNUAL.equals(strategyHsaFundingDto.getLumpSumFrequency())) {
				strategyHsaFundingDto.setAnnualFamilyAmount(strategyHsaFundingDto.getAnnualFamilyAmount().add(belowAmount));
			} else if (BSSApplicationConstants.HSA_QUARTERLY.equals(strategyHsaFundingDto.getLumpSumFrequency())) {
				strategyHsaFundingDto.setQuarterlyFamilyAmount(strategyHsaFundingDto.getQuarterlyFamilyAmount()
						.add(belowAmount.divide(new BigDecimal(4), 2, RoundingMode.HALF_UP)));
			} else if (BSSApplicationConstants.HSA_MONTHLY.equals(strategyHsaFundingDto.getContributionFrequency())) {
				strategyHsaFundingDto.setMonthlyFamilyAmount(strategyHsaFundingDto.getMonthlyFamilyAmount()
						.add(belowAmount.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP)));
			}
			totalFamilyContribution = hsaAnnualFamilyMinimum;
		}
	}
	
	/**
	 * If the client is contributing more than the HSA maximums, decrease the
	 * values.
	 * 
	 * @param currentHsaFunding
	 * @param realmRuleConfigurations
	 * @param totalEeContribution
	 * @param totalFamilyContribution
	 */
	private void validateHsaMaximumAmounts(StrategyHsaFundingDto strategyHsaFundingDto,
			Map<String, String> realmRuleConfigurations, BigDecimal totalEeContribution,
			BigDecimal totalFamilyContribution) {

		BigDecimal hsaAnnualEeMaximum = realmRuleConfigurations.get("HSA_ANNUAL_EMPLOYEE_MAXIMUM") == null
				? BigDecimal.ZERO : new BigDecimal(realmRuleConfigurations.get("HSA_ANNUAL_EMPLOYEE_MAXIMUM"));
		if (hsaAnnualEeMaximum.compareTo(totalEeContribution) < 0) {
			BigDecimal overAmount = totalEeContribution.subtract(hsaAnnualEeMaximum);
			if (BSSApplicationConstants.HSA_ANNUAL.equals(strategyHsaFundingDto.getLumpSumFrequency())) {
				if (strategyHsaFundingDto.getAnnualEeAmount().compareTo(overAmount) < 0) {
					overAmount = overAmount.subtract(strategyHsaFundingDto.getAnnualEeAmount());
					strategyHsaFundingDto.setAnnualEeAmount(BigDecimal.ZERO);
				} else {
					strategyHsaFundingDto.setAnnualEeAmount(strategyHsaFundingDto.getAnnualEeAmount().subtract(overAmount));
					overAmount = BigDecimal.ZERO;
				}
			}
			if (overAmount.compareTo(BigDecimal.ZERO) > 0 && BSSApplicationConstants.HSA_QUARTERLY.equals(strategyHsaFundingDto.getLumpSumFrequency())) {
				if (strategyHsaFundingDto.getQuarterlyEeAmount().multiply(new BigDecimal(4)).compareTo(overAmount) < 0) {
					overAmount = overAmount.subtract(strategyHsaFundingDto.getQuarterlyEeAmount().multiply(new BigDecimal(4)));
					strategyHsaFundingDto.setQuarterlyEeAmount(BigDecimal.ZERO);
				} else {
					strategyHsaFundingDto.setQuarterlyEeAmount(strategyHsaFundingDto.getQuarterlyEeAmount()
							.subtract(overAmount.divide(new BigDecimal(4), 2, RoundingMode.HALF_UP)));
					overAmount = BigDecimal.ZERO;
				}
			}
			if (overAmount.compareTo(BigDecimal.ZERO) > 0 && BSSApplicationConstants.HSA_MONTHLY.equals(strategyHsaFundingDto.getContributionFrequency())) {
				if (strategyHsaFundingDto.getMonthlyEeAmount().multiply(new BigDecimal(12)).compareTo(overAmount) < 0) {
					overAmount = overAmount.subtract(strategyHsaFundingDto.getMonthlyEeAmount().multiply(new BigDecimal(12)));
					strategyHsaFundingDto.setMonthlyEeAmount(BigDecimal.ZERO);
				} else {
					strategyHsaFundingDto.setMonthlyEeAmount(strategyHsaFundingDto.getMonthlyEeAmount()
							.subtract(overAmount.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP)));
					overAmount = BigDecimal.ZERO;
				}				
			}
			totalEeContribution = hsaAnnualEeMaximum.add(overAmount);
		}

		BigDecimal hsaAnnualFamilyMaximum = realmRuleConfigurations.get("HSA_ANNUAL_FAMILY_MAXIMUM") == null
				? BigDecimal.ZERO : new BigDecimal(realmRuleConfigurations.get("HSA_ANNUAL_FAMILY_MAXIMUM"));
		if (hsaAnnualFamilyMaximum.compareTo(totalFamilyContribution) < 0) {
			BigDecimal overAmount = totalFamilyContribution.subtract(hsaAnnualFamilyMaximum);
			if (BSSApplicationConstants.HSA_ANNUAL.equals(strategyHsaFundingDto.getLumpSumFrequency())) {
				if (strategyHsaFundingDto.getAnnualFamilyAmount().compareTo(overAmount) < 0) {
					overAmount = overAmount.subtract(strategyHsaFundingDto.getAnnualFamilyAmount());
					strategyHsaFundingDto.setAnnualFamilyAmount(BigDecimal.ZERO);
				} else {
					strategyHsaFundingDto.setAnnualFamilyAmount(strategyHsaFundingDto.getAnnualFamilyAmount().subtract(overAmount));
					overAmount = BigDecimal.ZERO;
				}
			}
			if (overAmount.compareTo(BigDecimal.ZERO) > 0 && BSSApplicationConstants.HSA_QUARTERLY.equals(strategyHsaFundingDto.getLumpSumFrequency())) {
				if (strategyHsaFundingDto.getQuarterlyFamilyAmount().multiply(new BigDecimal(4)).compareTo(overAmount) < 0) {
					overAmount = overAmount.subtract(strategyHsaFundingDto.getQuarterlyFamilyAmount().multiply(new BigDecimal(4)));
					strategyHsaFundingDto.setQuarterlyFamilyAmount(BigDecimal.ZERO);
				} else {
					strategyHsaFundingDto.setQuarterlyFamilyAmount(strategyHsaFundingDto.getQuarterlyFamilyAmount()
							.subtract(overAmount.divide(new BigDecimal(4), 2, RoundingMode.HALF_UP)));
					overAmount = BigDecimal.ZERO;
				}
			}
			if (overAmount.compareTo(BigDecimal.ZERO) > 0 && BSSApplicationConstants.HSA_MONTHLY.equals(strategyHsaFundingDto.getContributionFrequency())) {
				if (strategyHsaFundingDto.getMonthlyFamilyAmount().multiply(new BigDecimal(12)).compareTo(overAmount) < 0) {
					overAmount = overAmount.subtract(strategyHsaFundingDto.getMonthlyFamilyAmount().multiply(new BigDecimal(12)));
					strategyHsaFundingDto.setMonthlyFamilyAmount(BigDecimal.ZERO);
				} else {
					strategyHsaFundingDto.setMonthlyFamilyAmount(strategyHsaFundingDto.getMonthlyFamilyAmount()
							.subtract(overAmount.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP)));
					overAmount = BigDecimal.ZERO;
				}				
			}
			totalFamilyContribution = hsaAnnualFamilyMaximum.add(overAmount);
		}		
	}	
	
}
