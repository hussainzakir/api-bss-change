package com.trinet.ambis.service.unit;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;

import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.service.model.BandCodes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import com.trinet.ambis.persistence.dao.hrp.RealmDataDao;
import com.trinet.ambis.persistence.dao.ps.BenefitOptionsDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.RealmCloneProgram;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.service.impl.BenefitOptionsServiceImpl;
import com.trinet.ambis.service.model.StrategyHsaFundingDto;
import com.trinet.ambis.util.ApplicationContextProvider;

@RunWith(MockitoJUnitRunner.class)
public class BenefitOptionsServiceImplTest extends ServiceUnitTest {

	@Mock
	ApplicationContext context;

	@Mock
	RealmDataDao realmDataDao;

	@Mock
	BenefitOptionsDao benefitOptionsDao;

	@Mock
	EntityManager em;
    private MockedStatic<ApplicationContextProvider> applicationContextProviderMock;

	@Before
	public void setUp() {
        applicationContextProviderMock = Mockito.mockStatic(ApplicationContextProvider.class);
        applicationContextProviderMock.when(ApplicationContextProvider::getApplicationContext)
                .thenReturn(context);
        when(context.getBean("realmDataDao")).thenReturn(realmDataDao);
		when(context.getBean("benefitOptionsDao")).thenReturn(benefitOptionsDao);
	}

    @After
    public void tearDown() {
        applicationContextProviderMock.close();
    }

	@Test
	public void createClientBenefitOptions() {
		Company company = prepareCompany();
		// company has no riskType (defaults to BANDS) — band code fields are null, passed through safely
		BenefitGroup group = new BenefitGroup();
		group.setType("K1");
		group.setDefaultGroup(true);
		StrategyHsaFundingDto hsaOptions = new StrategyHsaFundingDto();
		String cloneBenProg = "STDCLN";
		String cloneK1Prog = "K1ClPrg";
		RealmCloneProgram realmClonePgm = new RealmCloneProgram();
		realmClonePgm.setCloneProgram( cloneBenProg );
		realmClonePgm.setCloneK1Program(cloneK1Prog);
		String cloneCompany = "AAA";

		when(realmDataDao.getRealmCloneProgram(company.getRealmPlanYear().getId())).thenReturn(realmClonePgm);
		when(benefitOptionsDao.getCloneCompany(company)).thenReturn(cloneCompany);

		when(benefitOptionsDao.insertOptn2(company, group, null, null)).thenReturn(1);

		when(benefitOptionsDao.populateHsaParameters(company, group, hsaOptions)).thenReturn(0);

		when(benefitOptionsDao.insertOpt2A(company, group)).thenReturn(0);
		when(benefitOptionsDao.insertOpt2AFromBenProg(company, group)).thenReturn(0);
		when(benefitOptionsDao.insertOpt2ASkeleton(company, group)).thenReturn(0);
		when(benefitOptionsDao.insertOptn3(company, group, null, null)).thenReturn(0);
		when(benefitOptionsDao.insertOptn3FromBenProg(company, group, null, null)).thenReturn(0);
		when(benefitOptionsDao.insertOptn3FromClone(company, group, cloneBenProg, cloneCompany, null, null)).thenReturn(0);
		when(benefitOptionsDao.resetOptn3Bands(company, group, null, null)).thenReturn(0);
		when(benefitOptionsDao.setDefaultBenProg(company, group)).thenReturn(0);

		BenefitOptionsServiceImpl service = new BenefitOptionsServiceImpl(em);

		service.createClientBenefitOptions(company, group, hsaOptions);

		InOrder inOrder = Mockito.inOrder(realmDataDao, benefitOptionsDao);

		inOrder.verify(realmDataDao, times(1)).getRealmCloneProgram(company.getRealmPlanYear().getId());
		inOrder.verify(benefitOptionsDao, times(1)).getCloneCompany(company);
		inOrder.verify(benefitOptionsDao, times(1)).insertOptn2(company, group, null, null);
		inOrder.verify(benefitOptionsDao, times(1)).populateHsaParameters(company, group, hsaOptions);
		inOrder.verify(benefitOptionsDao, times(1)).insertOpt2A(company, group);
		inOrder.verify(benefitOptionsDao, times(1)).insertOpt2AFromBenProg(company, group);
		inOrder.verify(benefitOptionsDao, times(1)).insertOpt2ASkeleton(company, group);
		inOrder.verify(benefitOptionsDao, times(1)).insertOptn3(company, group, null, null);
		inOrder.verify(benefitOptionsDao, times(1)).insertOptn3FromBenProg(company, group, null, null);
		inOrder.verify(benefitOptionsDao, times(1)).insertOptn3FromClone(company, group, cloneBenProg, cloneCompany, null, null);
		inOrder.verify(benefitOptionsDao, times(1)).resetOptn3Bands(company, group, null, null);
		inOrder.verify(benefitOptionsDao, times(1)).setDefaultBenProg(company, group);

	}

	@Test
	public void createClientBenefitOptions_DifferentialsRiskType() {
		Company company = prepareCompany();
		company.setRiskType(RiskTypeEnum.DIFFERENTIALS);
		BandCodes bandCodes = new BandCodes();
		bandCodes.setLifeBandCode("LB01");
		bandCodes.setDisBandCode("DB01");
		company.setBandCodes(bandCodes);
		BenefitGroup group = new BenefitGroup();
		group.setType("K1");
		group.setDefaultGroup(true);
		StrategyHsaFundingDto hsaOptions = new StrategyHsaFundingDto();
		String cloneBenProg = "STDCLN";
		String cloneK1Prog = "K1ClPrg";
		RealmCloneProgram realmClonePgm = new RealmCloneProgram();
		realmClonePgm.setCloneProgram(cloneBenProg);
		realmClonePgm.setCloneK1Program(cloneK1Prog);
		String cloneCompany = "AAA";

		when(realmDataDao.getRealmCloneProgram(company.getRealmPlanYear().getId())).thenReturn(realmClonePgm);
		when(benefitOptionsDao.getCloneCompany(company)).thenReturn(cloneCompany);
		when(benefitOptionsDao.insertOptn2(company, group, "LB01", "DB01")).thenReturn(1);
		when(benefitOptionsDao.populateHsaParameters(company, group, hsaOptions)).thenReturn(0);
		when(benefitOptionsDao.insertOpt2A(company, group)).thenReturn(0);
		when(benefitOptionsDao.insertOpt2AFromBenProg(company, group)).thenReturn(0);
		when(benefitOptionsDao.insertOpt2ASkeleton(company, group)).thenReturn(0);
		when(benefitOptionsDao.insertOptn3(company, group, "LB01", "DB01")).thenReturn(0);
		when(benefitOptionsDao.insertOptn3FromBenProg(company, group, "LB01", "DB01")).thenReturn(0);
		when(benefitOptionsDao.insertOptn3FromClone(company, group, cloneBenProg, cloneCompany, "LB01", "DB01")).thenReturn(0);
		when(benefitOptionsDao.resetOptn3Bands(company, group, "LB01", "DB01")).thenReturn(0);
		when(benefitOptionsDao.setDefaultBenProg(company, group)).thenReturn(0);

		BenefitOptionsServiceImpl service = new BenefitOptionsServiceImpl(em);
		service.createClientBenefitOptions(company, group, hsaOptions);

		InOrder inOrder = Mockito.inOrder(realmDataDao, benefitOptionsDao);

		inOrder.verify(realmDataDao, times(1)).getRealmCloneProgram(company.getRealmPlanYear().getId());
		inOrder.verify(benefitOptionsDao, times(1)).getCloneCompany(company);
		inOrder.verify(benefitOptionsDao, times(1)).insertOptn2(company, group, "LB01", "DB01");
		inOrder.verify(benefitOptionsDao, times(1)).populateHsaParameters(company, group, hsaOptions);
		inOrder.verify(benefitOptionsDao, times(1)).insertOpt2A(company, group);
		inOrder.verify(benefitOptionsDao, times(1)).insertOpt2AFromBenProg(company, group);
		inOrder.verify(benefitOptionsDao, times(1)).insertOpt2ASkeleton(company, group);
		inOrder.verify(benefitOptionsDao, times(1)).insertOptn3(company, group, "LB01", "DB01");
		inOrder.verify(benefitOptionsDao, times(1)).insertOptn3FromBenProg(company, group, "LB01", "DB01");
		inOrder.verify(benefitOptionsDao, times(1)).insertOptn3FromClone(company, group, cloneBenProg, cloneCompany, "LB01", "DB01");
		inOrder.verify(benefitOptionsDao, times(1)).resetOptn3Bands(company, group, "LB01", "DB01");
		inOrder.verify(benefitOptionsDao, times(1)).setDefaultBenProg(company, group);
	}

	@Test
	public void deleteFutureOptions() {
		Company company = prepareCompany();
		BenefitGroup group = new BenefitGroup();

		doNothing().when(benefitOptionsDao).deleteFutureOptn2(company, group);
		doNothing().when(benefitOptionsDao).deleteFutureOpt2A(company, group);
		doNothing().when(benefitOptionsDao).deleteFutureOptn3(company, group);
		doNothing().when(benefitOptionsDao).deleteFutureEfdt(company, group);

		BenefitOptionsServiceImpl service = new BenefitOptionsServiceImpl(em);
		service.deleteFutureOptions(company, group);

		InOrder inOrder = Mockito.inOrder(benefitOptionsDao);

		inOrder.verify(benefitOptionsDao, times(1)).deleteFutureOptn2(company, group);
		inOrder.verify(benefitOptionsDao, times(1)).deleteFutureOpt2A(company, group);
		inOrder.verify(benefitOptionsDao, times(1)).deleteFutureOptn3(company, group);
		inOrder.verify(benefitOptionsDao, times(1)).deleteFutureEfdt(company, group);
	}

	private Company prepareCompany() {
		Company comp = new Company();
		RealmPlanYear realmPlanYear = new RealmPlanYear();
		realmPlanYear.setId(31);
		comp.setRealmPlanYear(realmPlanYear);
		comp.setBandCodes(new BandCodes());
		return comp;
	}

}
