package com.trinet.ambis.persistence.dao.hrp;

import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.service.model.BenConfirmationStatement;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContributionDataDao {

	void saveContributionData(List<Contribution> contributionsList);

}
