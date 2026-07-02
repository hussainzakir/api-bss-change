package com.trinet.ambis.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.math.BigDecimal;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.service.unit.ServiceUnitTest;

@RunWith(MockitoJUnitRunner.class)
public class BenDefnOptnHSATest extends ServiceUnitTest {

	/**
	 * given object to be cloned<br>
	 * when clone method is called<br>
	 * then deep clone the given object and return the new object<br>
	 **/
	@Test
	public void objectCloneTest() {
		// given
		// data
		BenDefnOptnHSA benDefnOptnHSA = prepareBenDefnOptnHSA();
		// when
		BenDefnOptnHSA actualResult = SerializationUtils.clone(benDefnOptnHSA);
		// then
		// assertions
		assertNotSame(benDefnOptnHSA, actualResult);
		assertEquals("a", actualResult.getBenefitProgram());
		assertEquals("b", benDefnOptnHSA.getEffdt());
		assertEquals("c", benDefnOptnHSA.getPlanType());
		assertEquals(new BigDecimal(1), benDefnOptnHSA.getOptionId());
		assertEquals(new BigDecimal(2), benDefnOptnHSA.getDisplayOptSeq());
		assertEquals("d", benDefnOptnHSA.getOptionType());
		assertEquals("e", benDefnOptnHSA.getBenefitPlan());
		assertEquals("f", benDefnOptnHSA.getCovrgCd());
		assertEquals("g", benDefnOptnHSA.getOptionCd());
		assertEquals(new BigDecimal(3), benDefnOptnHSA.getOptionLvl());
		assertEquals("h", benDefnOptnHSA.getDedcd());
		assertEquals("i", benDefnOptnHSA.getDfltOptionInd());
		assertEquals("j", benDefnOptnHSA.getEligRulesId());
		assertEquals("k", benDefnOptnHSA.getLocationTblId());
		assertEquals("l", benDefnOptnHSA.getCrossPlanType());
		assertEquals("m", benDefnOptnHSA.getCrossBenefPlan());
		assertEquals(new BigDecimal(4), benDefnOptnHSA.getCoverageLimitPct());
		assertEquals("n", benDefnOptnHSA.getCrossPlnDpndChk());
		assertEquals("o", benDefnOptnHSA.getPfClient());
	}

	private BenDefnOptnHSA prepareBenDefnOptnHSA() {
		BenDefnOptnHSA benDefnOptnHSA = new BenDefnOptnHSA();
		benDefnOptnHSA.setBenefitProgram("a");
		benDefnOptnHSA.setEffdt("b");
		benDefnOptnHSA.setPlanType("c");
		benDefnOptnHSA.setOptionId(new BigDecimal(1));
		benDefnOptnHSA.setDisplayOptSeq(new BigDecimal(2));
		benDefnOptnHSA.setOptionType("d");
		benDefnOptnHSA.setBenefitPlan("e");
		benDefnOptnHSA.setCovrgCd("f");
		benDefnOptnHSA.setOptionCd("g");
		benDefnOptnHSA.setOptionLvl(new BigDecimal(3));
		benDefnOptnHSA.setDedcd("h");
		benDefnOptnHSA.setDfltOptionInd("i");
		benDefnOptnHSA.setEligRulesId("j");
		benDefnOptnHSA.setLocationTblId("k");
		benDefnOptnHSA.setCrossPlanType("l");
		benDefnOptnHSA.setCrossBenefPlan("m");
		benDefnOptnHSA.setCoverageLimitPct(new BigDecimal(4));
		benDefnOptnHSA.setCrossPlnDpndChk("n");
		benDefnOptnHSA.setPfClient("o");
		return benDefnOptnHSA;
	}

}
