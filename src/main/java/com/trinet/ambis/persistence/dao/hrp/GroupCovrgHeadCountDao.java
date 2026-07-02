package com.trinet.ambis.persistence.dao.hrp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trinet.ambis.persistence.model.GroupHeadCount;

@Repository
@Transactional(readOnly = true)
public interface GroupCovrgHeadCountDao extends JpaRepository<GroupHeadCount, Long> {
	     @Transactional
	     public GroupHeadCount findByGroupId(long groupId);
}
