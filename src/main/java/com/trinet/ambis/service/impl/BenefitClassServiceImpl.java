package com.trinet.ambis.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.dao.hrp.BenefitClassDao;
import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.service.BenefitClassService;

@Service
public class BenefitClassServiceImpl implements BenefitClassService {
	private static final Logger logger = LoggerFactory.getLogger(BenefitClassServiceImpl.class);

	@Autowired
	BenefitClassDao benefitClassDao;

	// save a map of company codes and saved class data objects
	private Map< String, CompanyClass > savedCompanyClass;

	public BenefitClassServiceImpl() {
		this.savedCompanyClass = new HashMap<>();
	}

	@Override
	public String generateClassCode( Company company, BenefitGroup group ) {

		String newEligConfig1 = null;
		CompanyClass cc = getCompanyClassData( company.getCode() );

		// See whether this BenefitGroup object already has an ELIG_CONFIG1 benefit class code
		if( group.getEligConfig1() == null || "".equals( group.getEligConfig1().trim() ) ) {
			// try the database or recently created class codes
			newEligConfig1 = lookupExistingClassCode( company, group );
		} else {
			// This BenefitGroup already has an ELIG_CONFIG1 value.  Return that value.
			newEligConfig1 = group.getEligConfig1();
		}

		// If a benefit class code still has not been discovered, create a new one
		if( newEligConfig1 == null || "".equals( newEligConfig1 ) ) {
			newEligConfig1 = deriveNewBenClass( company );
		}

		logger.info( "Company: {}, Benefit Program: {}, newEligConfig1: {}", company.getCode(), group.getBenefitProgram(), newEligConfig1 );

		// save ELIG_CONFIG1 value in map
		cc.savedMappings.put( group.getBenefitProgram(), newEligConfig1 );

		return newEligConfig1;
	}


	@Override
	public List<BenefitGroup> generateAllClassCodes(Company company, List<BenefitGroup> groups) {
		for( BenefitGroup group : groups ) {
			group.setEligConfig1( generateClassCode( company, group ) );
		}
		return groups;
	}


	/**
	 * Get a new prefix and append to the company code to create a new ben class (ELIG_CONFIG1) code
	 * @param company
	 * @return a brand new ben class value that can be assigned to a benefit program
	 */
	private String deriveNewBenClass( Company company ) {
		return deriveNextBenClassPrefix( company ).concat( company.getCode() );
	}


	/**
	 * Examine the existing ben class codes for a company and compute a new prefix that can be used
	 * to create a brand new ben class code value
	 * @param company
	 * @return
	 */
	private String deriveNextBenClassPrefix( Company company ) {

		// get the currently used ELIG_CONFIG1 values from the internal saved mappings
		CompanyClass cc = getCompanyClassData( company.getCode() );
		for( Map.Entry<String,String> entry : cc.savedMappings.entrySet() ) {
			stripCompanyAndSaveMaxIndex( entry.getValue(), company.getCode(), cc );
		}

		// get the currently used ELIG_CONFIG1 values from the DB
		Map<String,String> currentBenClassMappings = benefitClassDao.getBenProgramBenClassMappings( company );

		// convert that list to integers and determine the max prefix integer value
		for( Map.Entry<String,String> entry : currentBenClassMappings.entrySet() ) {
			stripCompanyAndSaveMaxIndex( entry.getValue(), company.getCode(), cc );
		}

		// add 1 to the max integer
		cc.lastMaxIndex++;
		
		// convert back to upper-case base 36 and return new prefix
		return Integer.toString( cc.lastMaxIndex, 36 ).toUpperCase();
	}


	private void stripCompanyAndSaveMaxIndex( String eligConfig1, String companyCode, CompanyClass cc ) {
		Integer prefixIndex;
		if( eligConfig1 == null || "".equals( eligConfig1.trim() )) {
			// catch case where incoming eligConfig1 is null or blank
			prefixIndex = -1;
		} else {
			// strip the company code, leaving the prefix
			String prefix = eligConfig1.substring( 0, ( eligConfig1.length() - companyCode.length() ) );
			prefixIndex = Integer.parseInt( prefix, 36 );
		}
		cc.lastMaxIndex = ( prefixIndex > cc.lastMaxIndex ) ? prefixIndex : cc.lastMaxIndex;
	}


	private String lookupExistingClassCode( Company company, BenefitGroup group ) {
		//  First, lookup the benefit program.  If an ELIG_CONFIG1 value is already assigned, done.
		String benClassCode = benefitClassDao.getEligClass( company, group );

		if( benClassCode == null || "".equals( benClassCode.trim() ) ) {
			// look for a recently created benefit class for this group
			CompanyClass cc = getCompanyClassData( company.getCode() );
			benClassCode = cc.savedMappings.get( group.getBenefitProgram() );
		}
		return benClassCode;
	}


	private CompanyClass getCompanyClassData( String companyCode ) {
		// get saved class data for this company
		// create if nothing currently exists
		return this.savedCompanyClass.computeIfAbsent( companyCode, key -> new CompanyClass() );
	}




	class CompanyClass {
		// save a map of BenefitPrograms to Benefit Class codes (elig-config1)
		Map<String,String> savedMappings;
		Integer lastMaxIndex;
		CompanyClass() {
			savedMappings = new HashMap<>();
			lastMaxIndex = -1;
		}
	}

}
