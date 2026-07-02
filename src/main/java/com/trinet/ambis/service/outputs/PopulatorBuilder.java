package com.trinet.ambis.service.outputs;

import java.util.function.Supplier;

import com.trinet.ambis.service.impl.outputs.PopulatorBuilderImpl;

public interface PopulatorBuilder extends Populator {
	
	PopulatorBuilder with(Populator data);
	
	public static PopulatorBuilderImpl create(Supplier<PopulatorBuilderImpl> builder) {
		return builder.get();
	}

}
