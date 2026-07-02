//package com.trinet.ambis.persistence.sp;
//
//import static org.mockito.Mockito.mock;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.sql.DataSource;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.JUnit4;
//import org.mockito.Mockito;
//import org.powermock.api.mockito.PowerMockito;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.springframework.jdbc.object.StoredProcedure;
//
//@RunWith(JUnit4.class)
//@PrepareForTest(StoredProcedure.class)
//public class GetNextEligRulesIdTest {
//
//	DataSource ds;
//	GetNextEligRulesId nextEligRuleId;
//
//	@Before
//	public void setUp() {
//		ds = mock(DataSource.class);
//		nextEligRuleId = new GetNextEligRulesId(ds);
//	}
//
//	@Test
//	public void execute() {
//		Map<String, Object> result = new HashMap<String, Object>();
//		result.put(" ", "id");
//		GetNextEligRulesId spy = Mockito.spy(GetNextEligRulesId.class);
//
//		PowerMockito.suppress(PowerMockito.methods(StoredProcedure.class, "execute"));
//
////		Mockito.when(((StoredProcedure) spy).execute()).thenReturn(result);
////
//		String actual = nextEligRuleId.execute();
////
////		assertEquals("id", actual);
//	}
//
//}
