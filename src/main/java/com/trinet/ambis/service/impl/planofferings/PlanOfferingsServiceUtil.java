package com.trinet.ambis.service.impl.planofferings;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.dao.hrp.RealmPlanYearDao;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Realm;
import com.trinet.ambis.persistence.model.RealmPlanYear;
import com.trinet.ambis.rest.controllers.dto.planofferings.PlanOfferingsRequest;
import com.trinet.ambis.util.Utils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlanOfferingsServiceUtil {

	private final RealmPlanYearDao realmPlanYearDao;

	public Company buildCompany(PlanOfferingsRequest planOfferingsRequest) {
		RealmPlanYear realmPlanYear = realmPlanYearDao.findByOeQuarterAndPlanYearStart(
				planOfferingsRequest.getQuarter(), Utils.convertStringToDate(planOfferingsRequest.getPlanYearStartDate(),
						BSSApplicationConstants.DATE_PATTERN_MM_DD_YYYY));
		Company company = new Company();
		company.setCode(BSSApplicationConstants.DUMMY);
		company.setRealmPlanYear(realmPlanYear);
		company.setRealmPlanYearId(realmPlanYear.getId());
		company.setPlanStartDate(Utils.convertDateToString(realmPlanYear.getPlanYearStart(),
				BSSApplicationConstants.DATE_FORMAT_DD_MMM_YYYY));
		Realm realm = new Realm();
		realm.setBenExchange(planOfferingsRequest.getExchange());
		company.setRealm(realm);
		company.setZipCode(StringUtils.isEmpty(planOfferingsRequest.getHqZipCode()) ? BSSApplicationConstants.DUMMY
				: planOfferingsRequest.getHqZipCode());
		company.setQuater(planOfferingsRequest.getQuarter());

		company.setHeadQuatersState(StringUtils.isEmpty(planOfferingsRequest.getHqState()) ? BSSApplicationConstants.DUMMY
				: planOfferingsRequest.getHqState());
		return company;
	}

}
