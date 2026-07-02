package com.trinet.ambis.persistence.dao.hrp;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.GroupRule;
/**
 * @author hliddle
 *
 */
@Repository
@Transactional(readOnly = true)
public interface GroupRuleDao extends JpaRepository<GroupRule, Long> {

	/**
	 * Returns a List of all {@code GroupRule} active on the passed in date
	 * 
	 * @param date
	 * @return {@code List<GroupRule>}
	 */
	@Query("Select gr from GroupRule gr where ?1 between gr.effDate and gr.expDate order by gr.sortOrder")
	List<GroupRule> findByDate(Date date);
	    
}