package com.trinet.ambis.service.impl;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trinet.ambis.persistence.model.Company;
import com.trinet.ambis.persistence.model.Contribution;
import com.trinet.ambis.persistence.model.PlanSelection;
import com.trinet.ambis.service.HeadCountDistributionService;
import com.trinet.ambis.service.model.HeadCountData;

/**
 * 
 * @author akaparaboyna
 *
 */
@Service
public class HeadCountDistributionServiceImpl implements HeadCountDistributionService {
	private static final Logger logger = LoggerFactory.getLogger(HeadCountDistributionServiceImpl.class);

	/**
	 * Distribute head count to plans based on some rules.
	 */
	@Override
	public Map<String, Map<String, Integer>> planHeadCountDistribution(
			Company company, List<PlanSelection> plans, HeadCountData headCountData) {
		return getPlansHeadCount(company, plans, headCountData);
	}
	
	
	private Set<String> sortByPlanCost(List<PlanSelection> planList) {
		Map<String, BigDecimal> costMap = new HashMap<>();
		for(PlanSelection ps : planList) {
			BigDecimal totalPlanCost = new BigDecimal(0);
			for(Contribution contrib :  ps.getContributions()) {
				logger.debug("#### PLAN : " + ps.getBenefitPlan() + "\tPLAN COST : " + contrib.getPlanCost() + 
						" PLAN TYPE : " + ps.getPlanType() + "\t COVERAGE CODE : " + contrib.getCoverageLevel());
				if (null != contrib.getPlanCost()) {
					totalPlanCost = totalPlanCost.add(contrib.getPlanCost());
				}			
			}
			logger.info("#### PLAN : " + ps.getBenefitPlan() + "\tTOTAL PLAN COST : " + totalPlanCost);
			costMap.put(ps.getBenefitPlan(), totalPlanCost);	
			
		}
		for(Entry<String, BigDecimal> entry : costMap.entrySet()) {
			logger.debug(" PLAN : " + entry.getKey() + "\t COST : " + entry.getValue());			
		}
		logger.debug("PLANS : {}" , costMap.keySet().toString());
		MyComparator comp=new MyComparator(costMap);

	    Map<String, BigDecimal> newMap = new TreeMap<>(comp);
		newMap.putAll(costMap);
		logger.debug("******************");
		for(Entry<String, BigDecimal> entry : costMap.entrySet()) {
			logger.debug(" PLAN : " + entry.getKey() + "\t COST : " + entry.getValue());
		}
		logger.info("SORT PLANS  IN DESCENDING ORDER : {}" , newMap.keySet().toString());
		return newMap.keySet();
	}
	
	class MyComparator implements Comparator<String> {
	    private final Map<String, BigDecimal> map;

	    public MyComparator(Map<String, BigDecimal> map) {
	        this.map = map;
	    }

	    @Override
	    public int compare(String o1, String o2) {
	        int res = map.get(o2).compareTo(map.get(o1));
	        return res != 0 ? res : o1.compareTo(o2);
	    }
	}
	
	private Map<String, Map<String, Integer>> getPlansHeadCount(Company company, List<PlanSelection> plans, 
			HeadCountData headCountData) {
		Map<String, Integer>allocations     = new HashMap<>();
	    Map<String, Integer> highAllocations = new HashMap<>();
	    Map<String, Integer> lowAllocations  = new HashMap<>(); 
	    Map<String, Integer> leftovers       = new HashMap<>(); 
	    Map<String, Map<String, Integer>>  contributionAllocation = new HashMap<>();
		  
		  int highAllocationPlan = 0;
		  int numberOfPlans   = plans.size();
		  String vertical = company.getIndustry().getIndustryType().name();
		  
		  if (numberOfPlans > 0) {
	          if (numberOfPlans < 2 || vertical.equals("financialService") || (vertical.equals("financialServices"))) {
	              highAllocationPlan = 0;
	          } else {
					int tempVal = (int) Math.ceil((double) numberOfPlans / 2);
					highAllocationPlan = tempVal - 1;
	          }
		  }
		  
		   //separate allocations
		  Map<String,Integer> covrgMap = headCountData.getCovrgHeadCountMap();
		  for(Entry<String, Integer> entry : covrgMap.entrySet()) {
			  String key = entry.getKey();
			  Integer val = entry.getValue();
			  highAllocations.put(key, val);
              if (numberOfPlans > 1) {
            	  lowAllocations.put(key, (int)Math.floor(val*0.5));
            	  Integer lowVal = lowAllocations.get(key);
            	  Integer highVal = highAllocations.get(key);
            	  highAllocations.put(key, highVal-lowVal);
            	  Integer allocVal = (int)Math.floor((lowVal/(numberOfPlans - 1)));
            	  allocations.put(key, allocVal);
            	  Integer leftOverVal = (lowVal % (numberOfPlans - 1));
            	  leftovers.put(key, leftOverVal);
            	  lowAllocations.put(key, (lowVal-leftOverVal));
              }
          }
		  // print allocations for debugging
		  //printAllAllocations(highAllocations, allocations, lowAllocations, leftovers);
		  
		  
		  //order by highest cost first
         Set<String> benefitPlans = sortByPlanCost(plans);
         
         
         //allocate
         int index = 0;
         for(String benefitPlan : benefitPlans) {
        	 logger.debug("******* ITERATION {} ******" , index);
        	 logger.debug("#### BENEFIT PLAN : {}" , benefitPlan);
        	
        	 if(index == highAllocationPlan) {
        		contributionAllocation.put(benefitPlan, highAllocations);
        	 }
        	 else {
        		//subtract allocated amount from running total
        		 Map<String, Integer> contribAllocMap = new HashMap<>();
        		 for (Entry<String, Integer> entry : lowAllocations.entrySet()) {        			
        			 String key = entry.getKey();
        			 Integer lowVal = lowAllocations.get(key);
        			 contribAllocMap.put(key, 0);
        			 
                     if (lowAllocations.get(key) < allocations.get(key)) {
                    	 allocations.put(key, lowVal);
                     }
                     Integer allocVal = allocations.get(key);
                     lowAllocations.put(key, (lowVal - allocVal));
                     contribAllocMap.put(key, allocVal);
                     //apply fractions
                     Integer contribVal = contribAllocMap.get(key);
                     Integer leftOverVal = leftovers.get(key);
                     if (leftovers.get(key) > 0) {
                    	 contribAllocMap.put(key, (contribVal+1));
                    	 leftovers.put(key, (leftOverVal-1));
                     } 
                 }
        		 contributionAllocation.put(benefitPlan, contribAllocMap);                       		         		 
        	 }
        	 //printAllAllocations(highAllocations, allocations, lowAllocations, leftovers);
        	 logger.debug("******* END OF ITERATION {} ******" , index);
        	 index++;       	 
        }
        printFinalPlanAllocations(contributionAllocation);
		return contributionAllocation;
	}
	
	private void printFinalPlanAllocations(Map<String, Map<String, Integer>>  contributionAllocation) {
		for (Entry<String, Map<String, Integer>> entry : contributionAllocation.entrySet()) { 
			logger.debug("**** HEAD COUNT ALLOCATION FOR PLAN : {}" , entry.getKey());
			for(Entry<String, Integer> entry1 : entry.getValue().entrySet()) {
				logger.debug("{} \t : {} \n", entry1.getKey(), entry1.getValue());
			}
			logger.debug("**** END OF HEAD COUNT ALLOCATION FOR PLAN : {}" , entry.getKey());			 
		 }					
	}

}
