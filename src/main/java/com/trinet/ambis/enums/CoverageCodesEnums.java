package com.trinet.ambis.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public enum CoverageCodesEnums {

    COV_ALL("0", "All Levels", "all"), 
    COV_EMPLOYEE("1", "Employee Only", "employee"), 
    COV_EMPLOYEE_PLUS_SPOUSE("2", "Employee + Spouse", "employeePlusSpouse"), 
    COV_EMPLOYEE_PLUS_CHILD("C", "Employee + Child(ren)", "employeePlusChild"), 
    COV_EMPLOYEE_FAMILY("4", "Family", "employeePlusFamily"),
    COV_EMPLOYEE_PLUS_ONE("81", "Employee + 1", "employeePlusOne"),
    COV_FAMILY("82", "Family", "family"), 

    COV_EMPLOYEE_PLUS_DP("5", "Employee + DP Adult", "employeeDPAdult"), 
    COV_EMPLOYEE_PLUS_DP_CHILD("6", "Employee + DP Child(ren)", "employeeDPChildren"), 
    COV_EMPLOYEE_PLUS_DP_ADULT_CHILD("7", "Employee + DP Adult + DP Child(ren)", "employeeDpAdultDpChildren"), 
    COV_EMPLOYEE_PLUS_TWO_DP_ADULT("8", "Employee + 2 Qualified children + DP Adult", "employeePlusTwoQualifiedChildrenDpAdult"), 
    COV_EMPLOYEE_PLUS_ONE_DP("83", "Employee + 1 DP Non-Qualified", "EmployeePlusOneDpNonQualified"), 
    COV_EMPLOYEE_ONLY_DP("84", "Employee Only + DP Family", "EmployeeDpFamily"), 
    COV_EMPLOYEE_PLUS_ONE_OR_MORE_DP("85", "Employee + 1 or more + DP Spouse/Child", "EmployeePlusOneDpSpouseChild"), 
    COV_EMPLOYEE_PLUS_TWO_OR_MORE_DP("86", "Employee + 2 or more + DP Spouse/Child", "EmployeePlusTwoDpSpouseChild");
    
	private static final String NO_ENUM_ERROR = "No enum const ";
	
	private String code;
	private String name;
	private String id;

	private CoverageCodesEnums(String code, String name, String id) {
		this.code = code;
		this.name = name;
		this.id = id;
	}

	public static final Set<CoverageCodesEnums> CoverageCodesCurrentSet = EnumSet.of(
			CoverageCodesEnums.COV_EMPLOYEE, CoverageCodesEnums.COV_EMPLOYEE_PLUS_ONE, CoverageCodesEnums.COV_FAMILY,
			CoverageCodesEnums.COV_ALL);

	public static final Set<CoverageCodesEnums> CoverageCodesFutureSet = EnumSet.of(CoverageCodesEnums.COV_EMPLOYEE,
			CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE, CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD,
			CoverageCodesEnums.COV_EMPLOYEE_FAMILY, CoverageCodesEnums.COV_ALL);

	// Do not MODIFY the following Sets as they are use strictly by the
	// plan-rate API
	public static final Set<CoverageCodesEnums> PlanRatesCoverageCodesCurrentSet = EnumSet.of(
			CoverageCodesEnums.COV_EMPLOYEE, CoverageCodesEnums.COV_EMPLOYEE_PLUS_ONE, CoverageCodesEnums.COV_FAMILY);

	public static final Set<CoverageCodesEnums> PlanRatesCoverageCodesFutureSet = EnumSet.of(
			CoverageCodesEnums.COV_EMPLOYEE, CoverageCodesEnums.COV_EMPLOYEE_PLUS_SPOUSE,
			CoverageCodesEnums.COV_EMPLOYEE_PLUS_CHILD, CoverageCodesEnums.COV_EMPLOYEE_FAMILY);

	public static String valueOfName(String code) {
		for (CoverageCodesEnums value : values()) {
			if (value.code.equals(code)) {
				return value.name;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_ERROR + CoverageCodesEnums.class + "@code." + code);
	}

	public static String valueOfId(String code) {
		for (CoverageCodesEnums value : values()) {
			if (value.code.equals(code)) {
				return value.id;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_ERROR + CoverageCodesEnums.class + "@code." + code);
	}

	public static String valueOfCode(String id) {
		for (CoverageCodesEnums value : values()) {
			if (value.id.equals(id)) {
				return value.code;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_ERROR + CoverageCodesEnums.class + "@id." + id);
	}

	public static String codeFromId(String id) {
		for (CoverageCodesEnums value : values()) {
			if (value.id.equals(id)) {
				return value.code;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_ERROR + CoverageCodesEnums.class + "@id." + id);
	}
	

	public static String nameFromId(String id) {
		for (CoverageCodesEnums value : values()) {
			if (value.id.equals(id)) {
				return value.name;
			}
		}
		throw new IllegalArgumentException(NO_ENUM_ERROR + CoverageCodesEnums.class + "@id." + id);
	}
	
	public static Set<String> dpCoverageLevels() {
		Set<String> dpCoverageLevelCodes = new HashSet<>();
		dpCoverageLevelCodes.add(COV_EMPLOYEE_PLUS_DP.getCode());
		dpCoverageLevelCodes.add(COV_EMPLOYEE_PLUS_DP_CHILD.getCode());
		dpCoverageLevelCodes.add(COV_EMPLOYEE_PLUS_DP_ADULT_CHILD.getCode());
		dpCoverageLevelCodes.add(COV_EMPLOYEE_PLUS_TWO_DP_ADULT.getCode());
		return dpCoverageLevelCodes;
	}

	public static List<String> coverageLevels() {
		List<String> coverageLevels = new ArrayList<>();
		coverageLevels.add(COV_EMPLOYEE.getId());
		coverageLevels.add(COV_EMPLOYEE_PLUS_SPOUSE.getId());
		coverageLevels.add(COV_EMPLOYEE_PLUS_CHILD.getId());
		coverageLevels.add(COV_EMPLOYEE_FAMILY.getId());
		return coverageLevels;
	}	
	
	/**
	 * Find a CoverageCodesEnums matching the given covCode parameter
	 * @param covCode
	 * @return the matching enum or null if not matched
	 */
	public static CoverageCodesEnums getByCoverageCode(String covCode) {
		Optional<CoverageCodesEnums> optCoverageCode = Arrays.stream( CoverageCodesEnums.values() )
				.filter( x -> x.getCode().equals( covCode ) )
				.findFirst();
		if( optCoverageCode.isPresent() )
			return optCoverageCode.get();
		else
			return null;
	}

	public String getCode() {
		return this.code;
	}

	public String getName() {
		return this.name;
	}

	public String getId() {
		return this.id;
	}
}
