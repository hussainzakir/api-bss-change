package com.trinet.ambis.service.prospect.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.service.prospect.exception.NotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class BenefitTypeEnumTest {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void getBenTypeCodeFromBcrBenTypeDescMedicalTest() {
		// then
		String actualResult = BenefitTypeEnum.getBenTypeCodeFromBcrBenTypeDesc("Medical");
		// assertion
		assertEquals("10", actualResult);
	}

	@Test
	public void getBenTypeCodeFromBcrBenTypeDescDentalTest() {
		// then
		String actualResult = BenefitTypeEnum.getBenTypeCodeFromBcrBenTypeDesc("Dental");
		// assertion
		assertEquals("11", actualResult);
	}

	@Test
	public void getBenTypeCodeFromBcrBenTypeDescVisionTest() {
		// then
		String actualResult = BenefitTypeEnum.getBenTypeCodeFromBcrBenTypeDesc("Vision");
		// assertion
		assertEquals("14", actualResult);
	}

	@Test
	public void getBenTypeCodeFromBcrBenTypeDescTest() {
		// when
		exception.expect(NotFoundException.class);
		// then
		BenefitTypeEnum.getBenTypeCodeFromBcrBenTypeDesc("medical");
		// assertion
		exception.expectMessage("Bcr benefit type desc = medical not found.");
	}

}