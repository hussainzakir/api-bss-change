package com.trinet.ambis.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.client.utils.DateUtils;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.enums.BenExchngEnums;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;
import com.trinet.ambis.persistence.model.IExceptionDto;
import com.trinet.ambis.service.model.AttributeDto;
import com.trinet.ambis.service.model.AttributeValueDto;
import com.trinet.ambis.service.model.BenOfferExceptionDto;
import com.trinet.ambis.service.model.ExceptionAttributeDto;
import com.trinet.ambis.service.model.ExceptionDto;
import com.trinet.ambis.service.model.HqOverridesDto;
import com.trinet.ambis.service.model.MinFundExceptionDto;
import com.trinet.ambis.util.CommonUtils;
import com.trinet.security.util.SecurityUtils;

public class ExceptionServiceHelper {
	 
	public static final String PRODUCT_QUARTER = "PRODUCT QUARTER";
	public static final String FUND_TYPE = "FUND TYPE";
	public static final String BENEFIT_OFFER_EXCEPTION = "BENEFIT OFFER EXCEPTION";
	public static final String MIN_FUNDING_EXCEPTION ="MIN FUNDING EXCEPTION";
	
	public static final String ERROR_MESSAGE_INVALID_COMPANY_CODE = "Invalid company code";
	public static final String ERROR_MESSAGE_INVALID_PLAN_TYPE = "Invalid plan type";
	public static final String ERROR_MESSAGE_INVALID_ORIGINATION = "Invalid origin dept";
	public static final String ERROR_MESSAGE_INVALID_FUND_TYPE = "Invalid Fund Type";
	public static final String ERROR_MESSAGE_INVALID_APPROVER_ID = "Invalid approver id";
	public static final String ERROR_MESSAGE_INVALID_QUARTER = "Invalid quarter";
	public static final String ERROR_MESSAGE_INVALID_SERVICE_ORDER_NUMBER = "Invalid service order number";
	public static final String SERVICE_ORDER_NUMBER_REGEX = "^[a-zA-Z0-9]+$"; 
	
	public static final String ERROR_MESSAGE_INVALID_HQ_STATE = "Invalid state";
	public static final String ERROR_MESSAGE_INVALID_HQ_POSTAL_CODE = "Invalid postal code";
	public static final String  HQ_POSTAL_CODE_REGEX = "^\\d{5}$";
	
	private ExceptionServiceHelper() {
		throw new IllegalStateException(
				"Utility class " + ExceptionServiceHelper.class.getName() + " can not be instantiated.");
	}
	
	/**
	 * This method validates if the start date is not after end date in given dto
	 * and are not overlapping with the start and end dates in existing exception
	 * records.
	 * 
	 * @param dto
	 * @param existingEntities
	 * @param isUpdate
	 */
	public static void validateStartAndEndDts(ExceptionDto dto, Set<? extends IExceptionDto> existingEntities) {
		String formattedStartDt = DateUtils.formatDate(dto.getStartDate(), "yyyy-MM-dd");
		String formattedEndDt = DateUtils.formatDate(dto.getEndDate(), "yyyy-MM-dd");
		if (!isStartDtEqualsOrBeforeEndDt(dto.getStartDate(), dto.getEndDate())) {
			throwException(String.format("Start date %s should be same or before End date %s.", formattedStartDt,
					formattedEndDt));
		} else if (isStartOrEndDtOverlapping(dto, existingEntities)) {
			throwException(String.format("Start date %s or End date %s can't fall in between existing dates.",
					formattedStartDt, formattedEndDt));
		}
	}

	private static boolean isStartOrEndDtOverlapping(ExceptionDto dto, Set<? extends IExceptionDto> existingEntities) {
		boolean result = false;
		for (IExceptionDto existingEntity : existingEntities) {
			Date newStartDate = dto.getStartDate();
			Date newEndDate = dto.getEndDate();
			Date existingStartDate = existingEntity.getStartDate();
			Date existingEndDate = existingEntity.getEndDate();
			if (dto.getId() != existingEntity.getId() && (CommonUtils.checkIfDateIsInRangeInclusive(newStartDate,
					existingStartDate, existingEndDate)
					|| CommonUtils.checkIfDateIsInRangeInclusive(newEndDate, existingStartDate, existingEndDate))) {
				result = true;
				break;
			}
		}
		return result;
	}

	private static boolean isStartDtEqualsOrBeforeEndDt(Date startDt, Date endDt) {
		return startDt.equals(endDt) || startDt.before(endDt);
	}

	private static void throwException(String errorMsg) {
		throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_MIN_FUNDING_EXCEPTION_ERROR,
				BSSHttpStatusConstants.BAD_REQUEST, "", errorMsg, null, null));
	}
	
	public static void throwException(String errorMsg, String errorCode) {
		throw new BSSApplicationException(
				new BSSApplicationError(errorCode, BSSHttpStatusConstants.BAD_REQUEST, "", errorMsg, null, null));
	}
	
	
	/**
	 * This method validates input validations for benefit offer exceptions
	 *
	 * 
	 * @param dto
	 * @param exceptionName
	 * 
	 */
	public static void validateRequestData(ExceptionDto dto, List<ExceptionAttributeDto> exceptionAttributes, List<String> products) {
		
		Map<String, List<String>> exceptionAttributeValueMap = getExceptionAttributeValues(dto, exceptionAttributes, products);
		
		// validate company code
		if(!SecurityUtils.isValidCompany(dto.getCompanyCode())) {
			throwException(String.format(ERROR_MESSAGE_INVALID_COMPANY_CODE));
		}
			
		// validate plan type
		if(!exceptionAttributeValueMap.get(BSSApplicationConstants.PLANTYPE).contains(dto.getPlanType())) {
			throwException(String.format(ERROR_MESSAGE_INVALID_PLAN_TYPE));
		}
		
		// validate origin dept and min fund type
		if(dto instanceof BenOfferExceptionDto) {
			BenOfferExceptionDto request = (BenOfferExceptionDto) dto;
			
			if(!exceptionAttributeValueMap.get(BSSApplicationConstants.ORIGINATION).contains(request.getOriginDept())) {
				throwException(String.format(ERROR_MESSAGE_INVALID_ORIGINATION));
			}
		} else if(dto instanceof MinFundExceptionDto) {
			MinFundExceptionDto request = (MinFundExceptionDto) dto;
			
			if(!exceptionAttributeValueMap.get(FUND_TYPE).contains(request.getMinFundType())) {
				throwException(String.format(ERROR_MESSAGE_INVALID_FUND_TYPE));
			}
		}
		
		// validate approver id
		if(!exceptionAttributeValueMap.get(BSSApplicationConstants.APPROVERS).contains(dto.getApproverId())) {
			throwException(String.format(ERROR_MESSAGE_INVALID_APPROVER_ID));
		}
		
		//// validate product
		if(!exceptionAttributeValueMap.get(PRODUCT_QUARTER).contains(dto.getQuarter())) {
			throwException(String.format(ERROR_MESSAGE_INVALID_QUARTER));
		}
		
	}
	
	/**
	 * This method validates the company code <br>
	 * If company is invalid then exception is thrown
	 * 
	 * @param companyCode
	 */
	public static void validateCompanyCode(String companyCode) {
		if (!SecurityUtils.isValidCompany(companyCode)) {
			throwException(ERROR_MESSAGE_INVALID_COMPANY_CODE);
		}
	}

	/**
	 * This method validates oe quarter <br>
	 * If oe quarter is invalid then exception is thrown
	 * @param oeQuarter
	 */
	public static void validateOeQuarter(String oeQuarter) {
		if (!BenExchngEnums.isValidQuarter(oeQuarter)) {
			throwException(ERROR_MESSAGE_INVALID_QUARTER);
		}
	}
	
	private static Map<String, List<String>> getExceptionAttributeValues(ExceptionDto dto, List<ExceptionAttributeDto> exceptionAttributes, List<String> products) {
		Map<String, List<String>> attributeValueMap = new HashMap<>();
		List<String> planTypes = new ArrayList<>();
		List<String> approvers = new ArrayList<>();
		List<String> originDepts = new ArrayList<>();
		List<String> fundTypes = new ArrayList<>();
		List<AttributeDto> attribute = null;
		
		if (CollectionUtils.isNotEmpty(exceptionAttributes)) {
			attribute = getAttributes(dto, exceptionAttributes);
			if(CollectionUtils.isNotEmpty(attribute)) {
				if(dto instanceof BenOfferExceptionDto) {
					originDepts = extractGivenAttributeValue(attribute, BSSApplicationConstants.ORIGINATION);
				} else if(dto instanceof MinFundExceptionDto) {
					fundTypes = extractGivenAttributeValue(attribute, BSSApplicationConstants.EXCEPTIONVALUETYPE);
				}
				planTypes = extractGivenAttributeValue(attribute, BSSApplicationConstants.PLANTYPE);
				approvers = extractGivenAttributeValue(attribute, BSSApplicationConstants.APPROVERS);
				
				attributeValueMap.put(BSSApplicationConstants.PLANTYPE, planTypes);
				attributeValueMap.put(BSSApplicationConstants.ORIGINATION, originDepts);
				attributeValueMap.put(BSSApplicationConstants.APPROVERS, approvers);
				attributeValueMap.put(PRODUCT_QUARTER, products);
				attributeValueMap.put(FUND_TYPE, fundTypes);
			}
		}
		return attributeValueMap;
	}

	private static List<AttributeDto> getAttributes(ExceptionDto dto, List<ExceptionAttributeDto> exceptionAttributes) {
		List<AttributeDto> attribute = null;
		if(dto instanceof BenOfferExceptionDto) {
			attribute = getAttributesByExceptionName(exceptionAttributes, BENEFIT_OFFER_EXCEPTION);
		} else if(dto instanceof MinFundExceptionDto) {
			attribute = getAttributesByExceptionName(exceptionAttributes, MIN_FUNDING_EXCEPTION);
		}
		return attribute;
	}
	
	private static List<AttributeDto> getAttributesByExceptionName(List<ExceptionAttributeDto> exceptionAttributes, String name) {
		return exceptionAttributes.stream()
		                          .filter(e -> e.getExceptionName().equals(name)).collect(Collectors.toList()).stream()
                                  .flatMap(exceptionAttribute -> exceptionAttribute.getAttributes().stream())
                                  .collect(Collectors.toList());
	}
	
	private static List<String> extractGivenAttributeValue(List<AttributeDto> attribute, String name){
		 return attribute.stream()
				 		 .filter(attributes -> attributes.getAttributeName().equals(name))
				 		 .flatMap(attributeValue -> attributeValue.getValues().stream())
				 		 .map(AttributeValueDto::getAttributeValue)
				 		 .collect(Collectors.toList());
	}
	
	/**
	 * This method validates input validations for mid year funding details 
	 *
	 * 
	 * @param companyCode
	 * @param serviceOrderNuber
	 * 
	 */
	public static void validateMidYearFundingRequestData(String companyCode, String serviceOrderNuber) {
		// validate company code
		if(!SecurityUtils.isValidCompany(companyCode)) {
			throwException(String.format(ERROR_MESSAGE_INVALID_COMPANY_CODE));
		} 
		// validate service order number
		if(!serviceOrderNuber.matches(SERVICE_ORDER_NUMBER_REGEX)) {
			throwException(String.format(ERROR_MESSAGE_INVALID_SERVICE_ORDER_NUMBER));
		} 
	}
	
	/**
	 * This method validates input validations for hq overrides details 
	 *
	 * 
	 * @param dto
	 * 
	 */
	public static void validateHQOverridesRequestData(HqOverridesDto dto) {
		// validate company code
		if(!SecurityUtils.isValidCompany(dto.getCompanyCode())) {
			throwException(ERROR_MESSAGE_INVALID_COMPANY_CODE);
		}
		// validate hq state
		if(!BSSApplicationConstants.ALL_LOCATIONS.contains(dto.getOverrideHqState())) {
			throwException(ERROR_MESSAGE_INVALID_HQ_STATE);
		}
		// validate hq zip
		if(!dto.getOverrideHqZip().matches(HQ_POSTAL_CODE_REGEX)) {
			throwException(ERROR_MESSAGE_INVALID_HQ_POSTAL_CODE);
		}
	}

}
