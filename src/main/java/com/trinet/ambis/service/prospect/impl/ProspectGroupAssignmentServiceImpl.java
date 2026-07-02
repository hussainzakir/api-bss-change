package com.trinet.ambis.service.prospect.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.trinet.ambis.service.model.prospect.ProspectApiRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.trinet.ambis.common.ProspectConstants;
import com.trinet.ambis.common.ProspectURIConstants;
import com.trinet.ambis.rest.controllers.dto.prospect.EmployeeGroupAssignmentDto;
import com.trinet.ambis.service.model.EmployeeAssignmentData;
import com.trinet.ambis.service.model.EmployeeData;
import com.trinet.ambis.service.model.EmployeeSourceData;
import com.trinet.ambis.service.prospect.ProspectGroupAssignmentService;
import com.trinet.ambis.service.prospect.response.BenefitGroupAssignmentRes;
import com.trinet.ambis.util.ProspectServiceRestClient;
import com.trinet.domain.common.ReturnResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProspectGroupAssignmentServiceImpl implements ProspectGroupAssignmentService {

	private final ProspectServiceRestClient prospectServiceRestClient;

	@SuppressWarnings("unchecked")
	@Override
	public Set<EmployeeData> getEmployeeGroupAssignments(String prospectId) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.put(ProspectConstants.PROSPECT_ID, List.of(prospectId));
		ParameterizedTypeReference<ReturnResponse<List<BenefitGroupAssignmentRes>>> benefitGroupAssignmentBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest<List<BenefitGroupAssignmentRes>> prospectApiGetRequest = ProspectApiRequest.<List<BenefitGroupAssignmentRes>>builder()
				.method(HttpMethod.GET)
				.uri(ProspectURIConstants.EMPLOYEE_GROUP_ASSIGNMENT).queryParams(map)
				.parameterizedTypeReference(benefitGroupAssignmentBean).build();
		List<BenefitGroupAssignmentRes> response = prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiGetRequest);
		if (response != null) {
			return getEmployeeData(response);
		} else {
			return Collections.emptySet();
		}
	}

	@Override
	public void updateEmployeeGroupAssignment(long strategyId, EmployeeAssignmentData employeeAssignmentData) {
		List<EmployeeGroupAssignmentDto> employeeGroupAssignmentDtoList = prepareEmployeeGroupAssignmentDto(
				employeeAssignmentData);
		updateProspectEmployeeGroupAssignment(employeeGroupAssignmentDtoList);
	}

	private Set<EmployeeData> getEmployeeData(List<BenefitGroupAssignmentRes> benefitGroupAssignments) {
		Set<EmployeeData> employees = new HashSet<>();
		benefitGroupAssignments.stream().forEach(benefitGroupAssignment -> {
			EmployeeData employee = new EmployeeData();
			employee.setEmplId(benefitGroupAssignment.getEmployeeId());
			employee.setEmplName(
					String.join(" ", benefitGroupAssignment.getFirstName(), benefitGroupAssignment.getLastName()));
			employee.setBenefitGroupName(benefitGroupAssignment.getBenefitGroupName());
			employee.setBenefitGroupId(benefitGroupAssignment.getBenefitGroupId());
			employee.setStrategyGroupId(benefitGroupAssignment.getBenefitGroupId());
			employees.add(employee);
		});
		return employees;
	}

	private void updateProspectEmployeeGroupAssignment(
			List<EmployeeGroupAssignmentDto> employeeGroupAssignmentDtoList) {
		ParameterizedTypeReference<ReturnResponse<Object>> updateEmpAssignmentBean = new ParameterizedTypeReference<>() {
		};
		ProspectApiRequest prospectApiPutRequest = ProspectApiRequest.builder().method(HttpMethod.PUT)
				.uri(ProspectURIConstants.EMPLOYEE_GROUP_ASSIGNMENT).parameterizedTypeReference(updateEmpAssignmentBean)
				.requestBody(employeeGroupAssignmentDtoList).build();
		prospectServiceRestClient.prepareRequestAndCallEndPoint(prospectApiPutRequest);
	}

	private List<EmployeeGroupAssignmentDto> prepareEmployeeGroupAssignmentDto(
			EmployeeAssignmentData employeeAssignmentData) {
		List<EmployeeSourceData> employeeSourceDataList = employeeAssignmentData.getEmployeesList();
		return employeeSourceDataList.stream()
				.map(employeeSourceData -> employeeSourceData.getEmployees().stream()
						.map(employeeId -> EmployeeGroupAssignmentDto.builder().employeeId(employeeId)
								.benefitGroupId((int) employeeAssignmentData.getDestinationStrategyGroupId()).build())
						.collect(Collectors.toList()))
				.collect(Collectors.toList()).stream().flatMap(Collection::stream).collect(Collectors.toList());
	}

}