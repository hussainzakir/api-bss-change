/**
 * 
 */
package com.trinet.ambis.service.model;

import java.util.LinkedList;

import lombok.Data;

/**
 * @author hliddle
 *
 */
@Data
public class ModelCompareStrategyHeadcount {
	private Long strategyId;
	private LinkedList<ModelComparePlanTypeCost> strategyHeadcount;
}