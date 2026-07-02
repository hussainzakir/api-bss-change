package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.util.BSSSecurityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.helper.StrategyServiceHelper;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.BenefitClassService;
import com.trinet.ambis.service.impl.BenefitGroupServiceImpl;
import com.trinet.ambis.service.model.GroupData;
import com.trinet.ambis.service.prospect.ProspectGroupService;

/**
 * @author rvutukuri
 *
 */

@RunWith(MockitoJUnitRunner.class)
public class BenefitGroupServiceImpl1Test extends ServiceUnitTest {

	@InjectMocks
	BenefitGroupServiceImpl benefitGroupService;

	@Mock
	ProspectGroupService prospectGroupService;

	@Captor
	ArgumentCaptor<String> prospectIdCaptor;

	@Captor
	ArgumentCaptor<Long> sourceGroupIdCaptor;

	@Captor
	ArgumentCaptor<String> groupNameCaptor;

	@Mock
	BenefitClassService benefitClassService;

    private MockedStatic<StrategyServiceHelper> mockStaticBSSSecurityUtils;

    @Before
	public void setUp() {
        mockStaticBSSSecurityUtils = Mockito.mockStatic(StrategyServiceHelper.class);
        mockStaticBSSSecurityUtils.when(() -> StrategyServiceHelper.isProspectStrategy(Mockito.anyLong())).thenReturn(true);
	}

    @After
    public void tearDown() {
        if (mockStaticBSSSecurityUtils != null) {
            mockStaticBSSSecurityUtils.close();
        }
    }

    /**
	 * given company , group data and strategy id</br>
	 * when addGroup Method is called</br>
	 * then verify prospectGroupService.addGroup method call is successfull
	 **/
	@Test
	public void addGroupTest1() {
		// given
		// data
		Company company = new Company();
		company.setCode("0014Z00001IDLTPAAC");
		GroupData groupData = new GroupData();
		groupData.setSourceStrategyGroupId(1);
		groupData.setDestGroupName("TestGroup2");
		long groupId = 2;
		long strategyId = 0;
		// method mocks
		when(prospectGroupService.addGroup(prospectIdCaptor.capture(), sourceGroupIdCaptor.capture(),
				groupNameCaptor.capture())).thenReturn(groupId);
		when(StrategyServiceHelper.isProspectStrategy(strategyId)).thenReturn(true);
		// when
		long actualResult = benefitGroupService.addGroup(company, groupData, strategyId);
		// then
		// assertions
		assertEquals(groupId, actualResult);
		assertEquals(company.getCode(), prospectIdCaptor.getValue());
		assertEquals(groupData.getSourceStrategyGroupId(), sourceGroupIdCaptor.getValue().longValue());
		assertEquals(groupData.getDestGroupName(), groupNameCaptor.getValue());
		// verify
		verify(prospectGroupService).addGroup(prospectIdCaptor.getValue(), sourceGroupIdCaptor.getValue(),
				groupNameCaptor.getValue());
	}

}