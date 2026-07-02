package com.trinet.ambis.service.model.plancompare;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanCompareTemplate {

	private String type;
	private String name;
	private int displayOrder;
	private List<Attribute> children;
}
