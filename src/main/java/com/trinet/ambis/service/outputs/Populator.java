package com.trinet.ambis.service.outputs;

import com.trinet.ambis.rest.controllers.dto.outputs.CurrentTrinetPlans;

@FunctionalInterface
public interface Populator {
	
	void populate(CurrentTrinetPlans currentTrinetPlans);

}
