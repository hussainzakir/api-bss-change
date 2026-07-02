package com.trinet.ambis.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeatureFlag {
	private String key;
	private boolean value;
}