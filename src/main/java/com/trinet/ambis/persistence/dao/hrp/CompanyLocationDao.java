/**
 * 
 */
package com.trinet.ambis.persistence.dao.hrp;

import java.util.List;

/**
 * @author schaudhari
 *
 */

public interface CompanyLocationDao {
	/**
	 * 
	 * @param companyId
	 * @return
	 */
	List<String> getBandCodesByCompanyId(Long companyId);

}
