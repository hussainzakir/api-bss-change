/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.trinet.ambis.service.model.SelectItem;

/**
 * @author rvutukuri
 *
 */
@Repository 
public interface CommonDataDao {
	/**
	 * 
	 * @return
	 */
	List<SelectItem> getBsuppExcessOptions();
	/**
	 * 
	 * @param realmId
	 * @return
	 */
	List<SelectItem> getBsuppVolPlanTypes(long realmId);
	
	/**
	 * 
	 * @param realmId
	 * @return
	 */
	List<String> getBsuppVolBenPlanTypes(long realmId);

}
