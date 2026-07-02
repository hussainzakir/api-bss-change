package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trinet.ambis.enums.BenExchngEnums;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.HrpDao;
import com.trinet.ambis.service.impl.HrpServiceImpl;
import com.trinet.security.util.SecurityUtils;

@RunWith(MockitoJUnitRunner.class)
public class HrpServiceImplTest extends ServiceUnitTest {

	@InjectMocks
	HrpServiceImpl hrpService;

	@Mock
	HrpDao hrpDao;
    private MockedStatic<SecurityUtils> securityUtilsMockedStatic;

    @Before
    public void setUp() {
        securityUtilsMockedStatic = Mockito.mockStatic(SecurityUtils.class);
    }

    @After
    public void tearDown() {
        if (securityUtilsMockedStatic != null) {
            securityUtilsMockedStatic.close();
            securityUtilsMockedStatic = null;
        }
    }

	@Test
	public void hasAccessToGatewayApp() {
		String appKey = "BSS-APP-1";

		/*
		 * When appRoles is empty
		 */
		Set<String> appRoles = new HashSet<>();
		List<String> userRoles = new ArrayList<>();
		
		when(hrpDao.getGatewayAppAccessibleRolesBy(appKey)).thenReturn(appRoles);
		when(SecurityUtils.getAuthorizedUserRoles()).thenReturn(userRoles);
		
		boolean actualResult = hrpService.hasAccessToGatewayApp(appKey);
		assertEquals(false, actualResult);

		/*
		 * When userRoles is empty
		 */
		appRoles = new HashSet<>(Arrays.asList("APP_ROLE1", "APP_ROLE2"));
		
		when(hrpDao.getGatewayAppAccessibleRolesBy(appKey)).thenReturn(appRoles);
		when(SecurityUtils.getAuthorizedUserRoles()).thenReturn(userRoles);
		
		actualResult = hrpService.hasAccessToGatewayApp(appKey);
		assertEquals(false, actualResult);

		/*
		 * When appRoles does not contain userRoles
		 */
		userRoles = Arrays.asList("APP_ROLE3");
		
		when(hrpDao.getGatewayAppAccessibleRolesBy(appKey)).thenReturn(appRoles);
		when(SecurityUtils.getAuthorizedUserRoles()).thenReturn(userRoles);
		
		actualResult = hrpService.hasAccessToGatewayApp(appKey);
		assertEquals(false, actualResult);

		/*
		 * When appRoles does contain userRoles
		 */
		userRoles = Arrays.asList("APP_ROLE1");
		
		when(hrpDao.getGatewayAppAccessibleRolesBy(appKey)).thenReturn(appRoles);
		when(SecurityUtils.getAuthorizedUserRoles()).thenReturn(userRoles);
		
		actualResult = hrpService.hasAccessToGatewayApp(appKey);
		assertEquals(true, actualResult);

		verify(hrpDao, times(4)).getGatewayAppAccessibleRolesBy(appKey);
	}
	
}