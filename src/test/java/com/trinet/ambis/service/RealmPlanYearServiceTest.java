package com.trinet.ambis.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.dto.RealmPlanYearDetailsDto;
import com.trinet.ambis.service.impl.RealmPlanYearServiceImpl;
import com.trinet.ambis.service.unit.ServiceUnitTest;
import com.trinet.ambis.util.CommonUtils;

@RunWith(MockitoJUnitRunner.class)
public class RealmPlanYearServiceTest extends ServiceUnitTest {

	@InjectMocks
	private RealmPlanYearServiceImpl realmPlanYearService;

	@Mock
	private RealmPlanYearDao realmPlanYearDao;

	/**
	 * given TriNet I exchange id </br>
	 * when findByRealmId method is called </br>
	 * then return realm plan year details dtos</br>
	 **/
	@Test
	public void findByRealmIdTest1() {
		// given
		// data
		long realmId = 5;
		List<RealmPlanYear> realmPlanYears = buildRealmPlanYearTrinetI();
		// method mocks
		when(realmPlanYearDao.findByRealmId(realmId)).thenReturn(realmPlanYears);
		// when
		List<RealmPlanYearDetailsDto> actualResult = realmPlanYearService.findByRealmId(realmId);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		RealmPlanYearDetailsDto actualRealmPlanYearDetailsDto = actualResult.get(0);
		RealmPlanYear expectedRealmPlanYear = realmPlanYears.get(0);
		assertEquals(expectedRealmPlanYear.getRealmId(), actualRealmPlanYearDetailsDto.getRealmId());
		assertEquals(expectedRealmPlanYear.getId(), actualRealmPlanYearDetailsDto.getId());
		assertEquals(expectedRealmPlanYear.getOeQuarter(), actualRealmPlanYearDetailsDto.getQuarter());
		assertEquals(CommonUtils.formatDateToString(expectedRealmPlanYear.getPlanYearStart(), "yyyy-MM-dd"),
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto.getStartDate(), "yyyy-MM-dd"));
		assertEquals(CommonUtils.formatDateToString(expectedRealmPlanYear.getPlanYearEnd(), "yyyy-MM-dd"),
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto.getEndDate(), "yyyy-MM-dd"));
		// verify
		verify(realmPlanYearDao, times(1)).findByRealmId(realmId);
	}

	/**
	 * given TriNet II exchange id </br>
	 * when findByRealmId method is called </br>
	 * then return realm plan year details dtos for only SY quarter always</br>
	 **/
	@Test
	public void findByRealmIdTest2() {
		// given
		// data
		long realmId = 4;
		List<RealmPlanYear> realmPlanYears = buildRealmPlanYearTrinetII();
		// method mocks
		when(realmPlanYearDao.findByRealmId(realmId)).thenReturn(realmPlanYears);
		// when
		List<RealmPlanYearDetailsDto> actualResult = realmPlanYearService.findByRealmId(realmId);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		RealmPlanYearDetailsDto actualRealmPlanYearDetailsDto = actualResult.get(0);
		RealmPlanYear expectedRealmPlanYear = realmPlanYears.get(0);
		assertEquals(expectedRealmPlanYear.getRealmId(), actualRealmPlanYearDetailsDto.getRealmId());
		assertEquals(expectedRealmPlanYear.getId(), actualRealmPlanYearDetailsDto.getId());
		assertEquals(expectedRealmPlanYear.getOeQuarter(), actualRealmPlanYearDetailsDto.getQuarter());
		assertEquals(CommonUtils.formatDateToString(expectedRealmPlanYear.getPlanYearStart(), "yyyy-MM-dd"),
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto.getStartDate(), "yyyy-MM-dd"));
		assertEquals(CommonUtils.formatDateToString(expectedRealmPlanYear.getPlanYearEnd(), "yyyy-MM-dd"),
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto.getEndDate(), "yyyy-MM-dd"));
		// verify
		verify(realmPlanYearDao, times(1)).findByRealmId(realmId);
	}

	/**
	 * given TriNet III exchange id </br>
	 * when findByRealmId method is called </br>
	 * then return realm plan year details dtos for only Q1,Q2,Q3,Q4 quarter with
	 * modified end dates</br>
	 **/
	@Test
	public void findByRealmIdTest3() {
		// given
		// data
		long realmId = 3;
		List<RealmPlanYear> realmPlanYears = buildRealmPlanYearTrinetIII();
		// method mocks
		when(realmPlanYearDao.findByRealmId(realmId)).thenReturn(realmPlanYears);
		// when
		List<RealmPlanYearDetailsDto> actualResult = realmPlanYearService.findByRealmId(realmId);
		// then
		// assertions
		assertEquals(4, actualResult.size());
		RealmPlanYearDetailsDto actualRealmPlanYearDetailsDto1 = actualResult.get(0);
		assertEquals(3, actualRealmPlanYearDetailsDto1.getRealmId());
		assertEquals(68, actualRealmPlanYearDetailsDto1.getId());
		assertEquals("Q1", actualRealmPlanYearDetailsDto1.getQuarter());
		assertEquals("2024-01-01",
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto1.getStartDate(), "yyyy-MM-dd"));
		assertEquals("2024-03-31",
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto1.getEndDate(), "yyyy-MM-dd"));
		RealmPlanYearDetailsDto actualRealmPlanYearDetailsDto2 = actualResult.get(1);
		assertEquals(3, actualRealmPlanYearDetailsDto2.getRealmId());
		assertEquals(70, actualRealmPlanYearDetailsDto2.getId());
		assertEquals("Q2", actualRealmPlanYearDetailsDto2.getQuarter());
		assertEquals("2024-04-01",
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto2.getStartDate(), "yyyy-MM-dd"));
		assertEquals("2025-03-31",
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto2.getEndDate(), "yyyy-MM-dd"));
		RealmPlanYearDetailsDto actualRealmPlanYearDetailsDto3 = actualResult.get(2);
		assertEquals(3, actualRealmPlanYearDetailsDto3.getRealmId());
		assertEquals(63, actualRealmPlanYearDetailsDto3.getId());
		assertEquals("Q3", actualRealmPlanYearDetailsDto3.getQuarter());
		assertEquals("2023-07-01",
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto3.getStartDate(), "yyyy-MM-dd"));
		assertEquals("2023-09-30",
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto3.getEndDate(), "yyyy-MM-dd"));
		RealmPlanYearDetailsDto actualRealmPlanYearDetailsDto4 = actualResult.get(3);
		assertEquals(3, actualRealmPlanYearDetailsDto4.getRealmId());
		assertEquals(64, actualRealmPlanYearDetailsDto4.getId());
		assertEquals("Q4", actualRealmPlanYearDetailsDto4.getQuarter());
		assertEquals("2023-10-01",
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto4.getStartDate(), "yyyy-MM-dd"));
		assertEquals("2023-12-31",
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto4.getEndDate(), "yyyy-MM-dd"));
		// verify
		verify(realmPlanYearDao, times(1)).findByRealmId(realmId);
	}

	/**
	 * given TriNet IV exchange id </br>
	 * when findByRealmId method is called </br>
	 * then return realm plan year details dtos </br>
	 **/
	@Test
	public void findByRealmIdTest4() {
		// given
		// data
		long realmId = 1;
		List<RealmPlanYear> realmPlanYears = buildRealmPlanYearTrinetIV();
		// method mocks
		when(realmPlanYearDao.findByRealmId(realmId)).thenReturn(realmPlanYears);
		// when
		List<RealmPlanYearDetailsDto> actualResult = realmPlanYearService.findByRealmId(realmId);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		RealmPlanYearDetailsDto actualRealmPlanYearDetailsDto = actualResult.get(0);
		RealmPlanYear expectedRealmPlanYear = realmPlanYears.get(0);
		assertEquals(expectedRealmPlanYear.getRealmId(), actualRealmPlanYearDetailsDto.getRealmId());
		assertEquals(expectedRealmPlanYear.getId(), actualRealmPlanYearDetailsDto.getId());
		assertEquals(expectedRealmPlanYear.getOeQuarter(), actualRealmPlanYearDetailsDto.getQuarter());
		assertEquals(CommonUtils.formatDateToString(expectedRealmPlanYear.getPlanYearStart(), "yyyy-MM-dd"),
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto.getStartDate(), "yyyy-MM-dd"));
		assertEquals(CommonUtils.formatDateToString(expectedRealmPlanYear.getPlanYearEnd(), "yyyy-MM-dd"),
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto.getEndDate(), "yyyy-MM-dd"));
		// verify
		verify(realmPlanYearDao, times(1)).findByRealmId(realmId);
	}

	/**
	 * given TriNet XI exchange id </br>
	 * when findByRealmId method is called </br>
	 * then return realm plan year details dtos </br>
	 **/
	@Test
	public void findByRealmIdTest5() {
		// given
		// data
		long realmId = 2;
		List<RealmPlanYear> realmPlanYears = buildRealmPlanYearTrinetXI();
		// method mocks
		when(realmPlanYearDao.findByRealmId(realmId)).thenReturn(realmPlanYears);
		// when
		List<RealmPlanYearDetailsDto> actualResult = realmPlanYearService.findByRealmId(realmId);
		// then
		// assertions
		assertEquals(1, actualResult.size());
		RealmPlanYearDetailsDto actualRealmPlanYearDetailsDto = actualResult.get(0);
		RealmPlanYear expectedRealmPlanYear = realmPlanYears.get(0);
		assertEquals(expectedRealmPlanYear.getRealmId(), actualRealmPlanYearDetailsDto.getRealmId());
		assertEquals(expectedRealmPlanYear.getId(), actualRealmPlanYearDetailsDto.getId());
		assertEquals(expectedRealmPlanYear.getOeQuarter(), actualRealmPlanYearDetailsDto.getQuarter());
		assertEquals(CommonUtils.formatDateToString(expectedRealmPlanYear.getPlanYearStart(), "yyyy-MM-dd"),
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto.getStartDate(), "yyyy-MM-dd"));
		assertEquals(CommonUtils.formatDateToString(expectedRealmPlanYear.getPlanYearEnd(), "yyyy-MM-dd"),
				CommonUtils.formatDateToString(actualRealmPlanYearDetailsDto.getEndDate(), "yyyy-MM-dd"));
		// verify
		verify(realmPlanYearDao, times(1)).findByRealmId(realmId);
	}

	private List<RealmPlanYear> buildRealmPlanYearTrinetI() {
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(69);
		realmPlanYear.setRealmId(5);
		realmPlanYear.setOeQuarter("AC");
		realmPlanYear.setPlanYearStart(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"));
		realmPlanYear.setPlanYearEnd(CommonUtils.formatStringToDate("2024-12-31", "yyyy-MM-dd"));
		return List.of(realmPlanYear);
	}

	private List<RealmPlanYear> buildRealmPlanYearTrinetII() {
		RealmPlanYear realmPlanYear1 = new RealmPlanYear();
		realmPlanYear1.setId(72);
		realmPlanYear1.setRealmId(4);
		realmPlanYear1.setOeQuarter("SY");
		realmPlanYear1.setPlanYearStart(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"));
		realmPlanYear1.setPlanYearEnd(CommonUtils.formatStringToDate("2025-03-31", "yyyy-MM-dd"));
		RealmPlanYear realmPlanYear2 = new RealmPlanYear();
		realmPlanYear2.setId(71);
		realmPlanYear2.setRealmId(4);
		realmPlanYear2.setOeQuarter("SM");
		realmPlanYear2.setPlanYearStart(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"));
		realmPlanYear2.setPlanYearEnd(CommonUtils.formatStringToDate("2025-03-31", "yyyy-MM-dd"));
		return List.of(realmPlanYear1, realmPlanYear2);
	}

	private List<RealmPlanYear> buildRealmPlanYearTrinetIII() {
		RealmPlanYear realmPlanYear1 = new RealmPlanYear();
		realmPlanYear1.setId(68);
		realmPlanYear1.setRealmId(3);
		realmPlanYear1.setOeQuarter("Q1");
		realmPlanYear1.setPlanYearStart(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"));
		realmPlanYear1.setPlanYearEnd(CommonUtils.formatStringToDate("2024-12-31", "yyyy-MM-dd"));
		RealmPlanYear realmPlanYear2 = new RealmPlanYear();
		realmPlanYear2.setId(70);
		realmPlanYear2.setRealmId(3);
		realmPlanYear2.setOeQuarter("Q2");
		realmPlanYear2.setPlanYearStart(CommonUtils.formatStringToDate("2024-04-01", "yyyy-MM-dd"));
		realmPlanYear2.setPlanYearEnd(CommonUtils.formatStringToDate("2025-03-31", "yyyy-MM-dd"));
		RealmPlanYear realmPlanYear3 = new RealmPlanYear();
		realmPlanYear3.setId(63);
		realmPlanYear3.setRealmId(3);
		realmPlanYear3.setOeQuarter("Q3");
		realmPlanYear3.setPlanYearStart(CommonUtils.formatStringToDate("2023-07-01", "yyyy-MM-dd"));
		realmPlanYear3.setPlanYearEnd(CommonUtils.formatStringToDate("2024-06-30", "yyyy-MM-dd"));
		RealmPlanYear realmPlanYear4 = new RealmPlanYear();
		realmPlanYear4.setId(64);
		realmPlanYear4.setRealmId(3);
		realmPlanYear4.setOeQuarter("Q4");
		realmPlanYear4.setPlanYearStart(CommonUtils.formatStringToDate("2023-10-01", "yyyy-MM-dd"));
		realmPlanYear4.setPlanYearEnd(CommonUtils.formatStringToDate("2024-09-30", "yyyy-MM-dd"));
		RealmPlanYear realmPlanYear5 = new RealmPlanYear();
		realmPlanYear5.setId(65);
		realmPlanYear5.setRealmId(3);
		realmPlanYear5.setOeQuarter("Q5");
		realmPlanYear5.setPlanYearStart(CommonUtils.formatStringToDate("2023-10-01", "yyyy-MM-dd"));
		realmPlanYear5.setPlanYearEnd(CommonUtils.formatStringToDate("2024-09-30", "yyyy-MM-dd"));
		return List.of(realmPlanYear1, realmPlanYear2, realmPlanYear3, realmPlanYear4, realmPlanYear5);
	}

	private List<RealmPlanYear> buildRealmPlanYearTrinetIV() {
		RealmPlanYear realmPlanYear1 = new RealmPlanYear();
		realmPlanYear1.setId(66);
		realmPlanYear1.setRealmId(1);
		realmPlanYear1.setOeQuarter("8Y");
		realmPlanYear1.setPlanYearStart(CommonUtils.formatStringToDate("2024-10-01", "yyyy-MM-dd"));
		realmPlanYear1.setPlanYearEnd(CommonUtils.formatStringToDate("2025-09-30", "yyyy-MM-dd"));
		return List.of(realmPlanYear1);
	}

	private List<RealmPlanYear> buildRealmPlanYearTrinetXI() {
		RealmPlanYear realmPlanYear1 = new RealmPlanYear();
		realmPlanYear1.setId(67);
		realmPlanYear1.setRealmId(2);
		realmPlanYear1.setOeQuarter("AL");
		realmPlanYear1.setPlanYearStart(CommonUtils.formatStringToDate("2024-01-01", "yyyy-MM-dd"));
		realmPlanYear1.setPlanYearEnd(CommonUtils.formatStringToDate("2025-12-31", "yyyy-MM-dd"));
		return List.of(realmPlanYear1);
	}

}
