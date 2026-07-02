package com.trinet.ambis.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.model.BenOfferExceptionDto;
import com.trinet.ambis.service.model.BenefitOffer;

@Service
public interface BenefitOfferExceptionService {

	/**
	 * This method returns Map of planType (plan type code) mapped to boolean value
	 * representing the exception. If the realm plan year's start date falls between
	 * exception's start and end date (inclusively) then the exception is considered
	 * applicable exception.
	 * 
	 * @param company
	 * @return
	 */
	Map<String, Boolean> findApplicableBy(Company company);

	/**
	 * This method returns list of all active BenOfferExceptionDto. 
	 * @return
	 */
	List<BenOfferExceptionDto> findAllActive();

	/**
	 * This method creates new BenefitOfferException with active true and mark
	 * the existing one as active false.
	 * 
	 * @param benOfferExceptionDto
	 * @return
	 */
	BenOfferExceptionDto update(BenOfferExceptionDto benOfferExceptionDto);

	/**
	 * This method creates new BenefitOfferException.
	 * 
	 * @param benOfferExceptionDto
	 * @return
	 */
	BenOfferExceptionDto save(BenOfferExceptionDto benOfferExceptionDto);

	/**
	 * This method returns BenOfferExceptionDto for given id.
	 * @param id
	 * @return
	 */
	BenOfferExceptionDto findBy(long id);

	/**
	 * This method removes the BenOffer entry from given map if there is exception
	 * available for given company. Key in the map can be PlanType code or name.
	 * 
	 * @param company
	 * @param benOffers
	 */
	void applyException(Company company, Map<String, ?> benOffers);

	/**
	 * This method removes the BenOffer entry from given list of BenefitOffer
	 * objects if there is exception available for given company.
	 * 
	 * @param company
	 * @param benOffers
	 */
	void applyException(Company company, List<BenefitOffer> benOffers);

}
