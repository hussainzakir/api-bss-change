package com.trinet.ambis.service.model;

import lombok.Data;

@Data
public class ActiveEligibleEECount {

	String benProg;
	int totalHeadCount;
	int primaryHeadCount;
	int secondaryHeadCount;

	public ActiveEligibleEECount() {
		super();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((benProg == null) ? 0 : benProg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActiveEligibleEECount other = (ActiveEligibleEECount) obj;
		if (benProg == null) {
			if (other.benProg != null)
				return false;
		} else if (!benProg.equals(other.benProg))
			return false;
		return true;
	}

}
