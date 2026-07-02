package com.trinet.ambis.service.impl.outputs;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.trinet.ambis.rest.controllers.dto.outputs.CurrentTrinetPlans;
import com.trinet.ambis.service.outputs.Populator;
import com.trinet.ambis.service.outputs.PopulatorBuilder;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PopulatorBuilderImpl implements PopulatorBuilder {
	
	private List<CompletableFuture<Populator>> plans;
	
	public PopulatorBuilderImpl() {
		plans = new LinkedList<>();
	}

	@Override
	public PopulatorBuilder with(Populator data) {
		Optional.ofNullable(data).ifPresent(plan -> plans.add(CompletableFuture.completedFuture(data)));
		return this;
	}
	
	@Override
	public void populate(CurrentTrinetPlans currentTrinetPlans) {
		plans.stream().forEach(plan -> {
			try {
				plan.get().populate(currentTrinetPlans);
			} catch (InterruptedException | ExecutionException ex) {
				log.info("Exception in populator buider...{} " , ex.getMessage());
				Thread.currentThread().interrupt();
			}
		});
	}

}
