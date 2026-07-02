package com.trinet.ambis.service.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class NewCompanyOptions {
	private String vertical;
	private Set<OptionsNew> options = new HashSet<>();

}
