package com.trinet.ambis.service.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.trinet.ambis.persistence.model.BenefitGroup;
import com.trinet.ambis.persistence.model.GroupRate;
import com.trinet.ambis.persistence.model.GroupRatePK;

/**
 * A set of utilities for working with benefit groups and their rate IDs
 * @author mbrothers
 *
 */
public class BenefitGroupRateMapper {
	
	private BenefitGroupRateMapper() {
		throw new IllegalStateException(
				"Utility class " + BenefitGroupRateMapper.class.getName() + " can not be instantiated.");
	}

	/**
	 * Transform a GroupRate collection from the JPA model class into a simple map, which can
	 * be used to lookup rate-table-id values by type.
	 * @param groupRates
	 * @return a searchable map of the key-values pairs in the GroupRate collection
	 */
	public static Map<String, String> convertGroupRateToMap(Collection<GroupRate> groupRates) {
		Map<String, String> groupRateMap = new HashMap<>();
		if (null != groupRates) {
			for (GroupRate gr : groupRates) {
				groupRateMap.put(gr.getRateIdType(), gr.getId().getRateTblId());
			}
		}
		return groupRateMap;
	}

	/**
	 * Transform a map of <rate-table-id-type, rate-table-id> to a GroupRate collection.
	 * The returned collection can be added to the JPA model BenefitGroup class.
	 * @param rateMap
	 * @return a GroupRate collection in the format required by the BenefitGroup model class
	 */
	public static Set<GroupRate> convertMapToGroupRate( Map<String,String> rateMap, BenefitGroup benefitGroup ) {
		if( rateMap == null ) {
			return null;
		}
		Set<GroupRate> groupRates = new HashSet<>();
		for( Entry<String, String> e : rateMap.entrySet() ) {
			GroupRate gr = new GroupRate();
			gr.setId( new GroupRatePK() );
			gr.getId().setRateTblId( e.getValue() );
			gr.setRateIdType( e.getKey() );
			gr.setBenefitGroup(benefitGroup);
			groupRates.add( gr );
		}
		return groupRates;
	}

}
