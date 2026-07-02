package com.trinet.ambis.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.enums.PlanTypesEnum;
import com.trinet.ambis.persistence.dao.hrp.ExceptionAttributeDao;
import com.trinet.ambis.persistence.dao.ps.PsCompanyDao;
import com.trinet.ambis.persistence.dao.ps.PsDao;
import com.trinet.ambis.persistence.model.ExceptionTypeAttribute;
import com.trinet.ambis.service.ExceptionAttributeService;
import com.trinet.ambis.service.model.AttributeDto;
import com.trinet.ambis.service.model.AttributeValueDto;
import com.trinet.ambis.service.model.ExceptionAttributeDto;

@Service
public class ExceptionAttributeServiceImpl implements ExceptionAttributeService {

	@Autowired
	ExceptionAttributeDao exceptionAttributeDao;

	@Autowired
	PsCompanyDao psCompanyDao;

	@Autowired
	PsDao psDao;

	@Override
	public List<ExceptionAttributeDto> findAllExceptionAttributes() {

		List<ExceptionTypeAttribute> exceptionAttributes = exceptionAttributeDao.findAll();
		List<ExceptionAttributeDto> finalResult = new ArrayList<>(exceptionAttributes.size());
		Map<Long, ExceptionAttributeDto> exceptionAttributeDtos = new HashMap<>();
		Set<String> emplIds = new HashSet<>();

		if (CollectionUtils.isNotEmpty(exceptionAttributes)) {
			for (ExceptionTypeAttribute exceptionAttribute : exceptionAttributes) {
				long exceptionId = exceptionAttribute.getExceptionType().getId();
				long attributeId = exceptionAttribute.getAttribute().getId();
				String attributeName = exceptionAttribute.getAttribute().getAttributeName();
				ExceptionAttributeDto exceptionAttributeDto = exceptionAttributeDtos.get(exceptionId);
				AttributeDto attributeDto = new AttributeDto();

				if (BSSApplicationConstants.APPROVERS.equals(attributeName)) {
					emplIds.add(exceptionAttribute.getAttributeValue());
				}

				/* checking if exception Map has exception id */
				if (exceptionAttributeDto != null) {

					Map<Long, AttributeDto> attributeMap = exceptionAttributeDto.getTempAttributes().get(exceptionId);
					/** checking if exception Map has Attribute id */
					if (attributeMap != null) {
						attributeDto = attributeMap.get(attributeId);
						if (attributeDto != null) {
							AttributeValueDto attributeValue = createAttributeValueDto(exceptionAttribute);
							attributeDto.getTempAttributeValues().get(attributeId).add(attributeValue);
						} else {
							attributeDto = createAttributeDto(exceptionAttribute);
							exceptionAttributeDto.getAttributes().add(attributeDto);
							attributeMap.put(attributeId, attributeDto);
						}
					} else {

						Map<Long, AttributeDto> attributeDtoMap = new HashMap<>();
						Map<Long, Map<Long, AttributeDto>> exceptionAttributeMap = new HashMap<>();
						attributeDto = createAttributeDto(exceptionAttribute);
						attributeDtoMap.put(attributeId, attributeDto);
						exceptionAttributeMap.put(exceptionId, attributeDtoMap);
						exceptionAttributeDto.setTempAttributes(exceptionAttributeMap);
						exceptionAttributeDto.getAttributes().add(attributeDto);
					}
				} else {
					exceptionAttributeDto = createExceptionAttributeDto(exceptionAttribute);
					exceptionAttributeDtos.put(exceptionAttributeDto.getExceptionId(), exceptionAttributeDto);
				}
			}

			for (long id : exceptionAttributeDtos.keySet()) {
				ExceptionAttributeDto exceptionDto = exceptionAttributeDtos.get(id);
				finalResult.add(exceptionDto);
			}
			Map<String, String> emplNames = getEmployeeNames(emplIds);
			setAttributesInfoAndValues(finalResult, emplNames);
		}

		return finalResult;
	}

	private ExceptionAttributeDto createExceptionAttributeDto(ExceptionTypeAttribute exceptionAttribute) {
		ExceptionAttributeDto exceptionAttributeDto = new ExceptionAttributeDto();
		List<AttributeDto> attributesDto = new ArrayList<>();
		Map<Long, AttributeDto> attributeMap = new HashMap<>();
		Map<Long, Map<Long, AttributeDto>> exceptionAttributeMap = new HashMap<>();

		exceptionAttributeDto.setExceptionId(exceptionAttribute.getExceptionType().getId());
		exceptionAttributeDto.setExceptionName(exceptionAttribute.getExceptionType().getExceptionName());
		AttributeDto attributeDto = createAttributeDto(exceptionAttribute);
		attributesDto.add(attributeDto);
		attributeMap.put(exceptionAttribute.getAttribute().getId(), attributeDto);
		exceptionAttributeMap.put(exceptionAttributeDto.getExceptionId(), attributeMap);
		exceptionAttributeDto.setTempAttributes(exceptionAttributeMap);
		exceptionAttributeDto.setAttributes(attributesDto);

		return exceptionAttributeDto;
	}

	private AttributeDto createAttributeDto(ExceptionTypeAttribute exceptionAttribute) {
		AttributeDto attributeDto = new AttributeDto();
		List<AttributeValueDto> attributeValuesDto = new ArrayList<>();
		Map<Long, List<AttributeValueDto>> attributeDtoMap = new HashMap<>();

		attributeDto.setAttributeId(exceptionAttribute.getAttribute().getId());
		attributeDto.setAttributeName(exceptionAttribute.getAttribute().getAttributeName());
		attributeValuesDto.add(createAttributeValueDto(exceptionAttribute));
		attributeDtoMap.put(attributeDto.getAttributeId(), attributeValuesDto);
		attributeDto.setTempAttributeValues(attributeDtoMap);
		attributeDto.setValues(attributeValuesDto);

		return attributeDto;
	}

	private AttributeValueDto createAttributeValueDto(ExceptionTypeAttribute exceptionAttribute) {
		AttributeValueDto attributeValueDto = new AttributeValueDto();
		String attributeName = exceptionAttribute.getAttribute().getAttributeName();
		attributeValueDto.setAttributeValue(exceptionAttribute.getAttributeValue());
		if (BSSApplicationConstants.PLANTYPE.equals(attributeName)) {
			attributeValueDto.setName(PlanTypesEnum.getName(attributeValueDto.getAttributeValue()));
		} else if (BSSApplicationConstants.ORIGINATION.equals(attributeName)
				|| (BSSApplicationConstants.EXCEPTIONVALUETYPE.equals(attributeName))) {
			attributeValueDto.setName(exceptionAttribute.getAttributeValue());
		}
		return attributeValueDto;
	}

	private List<ExceptionAttributeDto> setAttributesInfoAndValues(List<ExceptionAttributeDto> exceptionAttributeDtos,
			Map<String, String> emplNames) {
		List<ExceptionAttributeDto> benOfferExceptionDtos = new ArrayList<>(exceptionAttributeDtos.size());
		if (CollectionUtils.isNotEmpty(exceptionAttributeDtos)) {
			for (ExceptionAttributeDto exceptionAttributeDto : exceptionAttributeDtos) {
				List<AttributeDto> attributeDtos = new ArrayList<>(exceptionAttributeDto.getAttributes().size());
				attributeDtos = exceptionAttributeDto.getAttributes();

				for (AttributeDto attributeDto : attributeDtos) {
					String attributeName = attributeDto.getAttributeName();
					List<AttributeValueDto> attributeValueDtos = new ArrayList<>(attributeDto.getValues().size());
					attributeValueDtos = attributeDto.getValues();
					if (attributeName != null && BSSApplicationConstants.APPROVERS.equals(attributeName)) {
						for (AttributeValueDto attributeValueDto : attributeValueDtos) {
							attributeValueDto.setName(emplNames.get(attributeValueDto.getAttributeValue()));
						}
					}

				}

			}
		}
		return benOfferExceptionDtos;
	}

	private Map<String, String> getEmployeeNames(Set<String> emplIds) {
		return psDao.getEmployeesFullName(emplIds);
	}

}
