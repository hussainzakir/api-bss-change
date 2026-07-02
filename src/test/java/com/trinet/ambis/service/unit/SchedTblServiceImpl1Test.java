package com.trinet.ambis.service.unit;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.persistence.dao.hrp.SchedMidYearFundingDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.SchedMidYearFunding;
import com.trinet.ambis.persistence.model.SchedTbl;
import com.trinet.ambis.persistence.model.SchedTblId;
import com.trinet.ambis.service.CompanyService;
import com.trinet.ambis.service.impl.SchedTblServiceImpl;
import com.trinet.ambis.service.model.SchedMidYearFundingDto;

@RunWith(MockitoJUnitRunner.class)
public class SchedTblServiceImpl1Test extends ServiceUnitTest {

	@InjectMocks
	SchedTblServiceImpl schedTblService;

	@Mock
	CompanyService companyService;

	@Mock
	SchedMidYearFundingDao schedMidYearFundingDao;
	
	public @Rule
	ExpectedException expectedException = ExpectedException.none();

	private static final String ERROR_MESSAGE_INVALID_SERVICE_ORDER_NUMBER = "Invalid service order number";
	private static final String ERROR_MESSAGE_INVALID_COMPANY_CODE = "Invalid company code";

	/*
	 * 
	 * Setup methods
	 * 
	 */
	private Company prepareCompany(String companyCode) {
		Company company = new Company();
		company.setCode(companyCode);
		SchedTbl schedTbl = new SchedTbl();
		SchedTblId schedTblId = new SchedTblId();
		schedTbl.setSched(schedTblId);
		company.setSchedTbl(schedTbl);
		return company;
	}

	@Test
	public void validateRequestDataForInvalidServiceOrderNumber() throws Exception{
		String companyCode = "GSU";
		SchedMidYearFundingDto schedMidYearFundingDto = new SchedMidYearFundingDto();
		schedMidYearFundingDto.setCompanyCode(companyCode);
		List<SchedMidYearFunding> schedMidYearFundingList =  prepareSchedMidYearFundingList();
		Company company = prepareCompany(companyCode);
        
		// when
		when(schedMidYearFundingDao.findByCompanyCode(company.getCode())).thenReturn(schedMidYearFundingList);

		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_SERVICE_ORDER_NUMBER);
		schedMidYearFundingDto.setServiceOrderNumber("TEST".concat("$"));
		schedTblService.createUpdateMidYearDetails(schedMidYearFundingDto, company, false);
	}
	
	@Test
	public void validateRequestDataForInvalidCompanyCode() throws Exception{
		String companyCode = "TEST";
		SchedMidYearFundingDto schedMidYearFundingDto = new SchedMidYearFundingDto();
		schedMidYearFundingDto.setCompanyCode(companyCode);
		List<SchedMidYearFunding> schedMidYearFundingList =  prepareSchedMidYearFundingList();
		Company company = prepareCompany(companyCode);
        
		// when
		when(schedMidYearFundingDao.findByCompanyCode(company.getCode())).thenReturn(schedMidYearFundingList);

		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_COMPANY_CODE);
		schedMidYearFundingDto.setCompanyCode(companyCode.concat("$"));
		schedTblService.createUpdateMidYearDetails(schedMidYearFundingDto, company, false);
	}

	
	@Test
	public void validateUpdateMidYearFundingDetailsforInvalidCompanyCode() {
		String companyCode = "TEST";
		SchedMidYearFundingDto schedMidYearFundingDto = new SchedMidYearFundingDto();
		schedMidYearFundingDto.setCompanyCode(companyCode);
		List<SchedMidYearFunding> schedMidYearFundingList =  prepareSchedMidYearFundingList();
		Company company = prepareCompany(companyCode);
        
		// when
		when(schedMidYearFundingDao.findByCompanyCode(company.getCode())).thenReturn(schedMidYearFundingList);

		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_COMPANY_CODE);
		schedMidYearFundingDto.setCompanyCode(companyCode.concat("$"));
		schedTblService.createUpdateMidYearDetails(schedMidYearFundingDto, company, true);
	}
	
	@Test
	public void validateUpdateMidYearFundingDetailsforInvalidServiceOrderNumber() {
		String companyCode = "GSU";
		SchedMidYearFundingDto schedMidYearFundingDto = new SchedMidYearFundingDto();
		schedMidYearFundingDto.setCompanyCode(companyCode);
		List<SchedMidYearFunding> schedMidYearFundingList =  prepareSchedMidYearFundingList();
		Company company = prepareCompany(companyCode);
        
		// when
		when(schedMidYearFundingDao.findByCompanyCode(company.getCode())).thenReturn(schedMidYearFundingList);

		// then
		expectedException.expect(BSSApplicationException.class);
		expectedException.expectMessage(ERROR_MESSAGE_INVALID_SERVICE_ORDER_NUMBER);
		schedMidYearFundingDto.setServiceOrderNumber(" ".concat("$"));
		schedTblService.createUpdateMidYearDetails(schedMidYearFundingDto, company, true);
	}

	private List<SchedMidYearFunding> prepareSchedMidYearFundingList() {
		List<SchedMidYearFunding> schedMidYearFundingList = new ArrayList<>();
		
		SchedMidYearFunding schedMidYearFunding = new SchedMidYearFunding();
		schedMidYearFunding.setId(15);
		schedMidYearFunding.setCompanyId(9807);
		schedMidYearFunding.setServiceOrderNumber("05707785");
		schedMidYearFunding.setMidYearFundingEffDate(new Date());
		
		schedMidYearFundingList.add(schedMidYearFunding);
		
		return schedMidYearFundingList;
	}
}
