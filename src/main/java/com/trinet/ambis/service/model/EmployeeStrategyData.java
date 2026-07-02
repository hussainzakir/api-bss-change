/**
 * 
 */
package com.trinet.ambis.service.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author clamorie
 *
 */

public class EmployeeStrategyData implements Comparable<EmployeeStrategyData> {
	private String emplId;
	private String emplFirstName;
	private String emplMiddleName;
	private String emplLastName;
	private String deptId;
	private String deptName;
	private String locationId;
	private String locationName;
	private BigDecimal currentGroupId;
	private String currentGroupName;
	List<EmployeeStrategyPlanData> strategyDetails = new ArrayList<>();

	/**
	 * @return the emplId
	 */
	public String getEmplId() {
		return emplId;
	}

	/**
	 * @param emplId
	 *            the emplId to set
	 */
	public void setEmplId(String emplId) {
		this.emplId = emplId;
	}

	/**
	 * @return the emplFirstName
	 */
	public String getEmplFirstName() {
		return emplFirstName;
	}

	/**
	 * @param emplFirstName
	 *            the emplFirstName to set
	 */
	public void setEmplFirstName(String emplFirstName) {
		this.emplFirstName = emplFirstName;
	}

	/**
	 * @return the emplMiddleName
	 */
	public String getEmplMiddleName() {
		return emplMiddleName;
	}

	/**
	 * @param emplMiddleName
	 *            the emplMiddleName to set
	 */
	public void setEmplMiddleName(String emplMiddleName) {
		this.emplMiddleName = emplMiddleName;
	}

	/**
	 * @return the emplLastName
	 */
	public String getEmplLastName() {
		return emplLastName;
	}

	/**
	 * @param emplLastName
	 *            the emplLastName to set
	 */
	public void setEmplLastName(String emplLastName) {
		this.emplLastName = emplLastName;
	}

	/**
	 * @return the emplLastName
	 */
	public String getEmplFullName() {
		String emplFullName = emplLastName + ", " + emplFirstName;
		if (emplMiddleName != null && !("").equals(emplMiddleName)) {
			emplFullName = emplFullName + " " + emplMiddleName;
		}
		return emplFullName;
	}

	/**
	 * @return the deptId
	 */
	public String getDeptId() {
		return deptId;
	}

	/**
	 * @param deptId
	 *            the deptId to set
	 */
	public void setDeptId(String deptId) {
		this.deptId = deptId;
	}

	/**
	 * @return the deptName
	 */
	public String getDeptName() {
		return deptName;
	}

	/**
	 * @param deptName
	 *            the deptName to set
	 */
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	/**
	 * @return the locationId
	 */
	public String getLocationId() {
		return locationId;
	}

	/**
	 * @param locationId
	 *            the locationId to set
	 */
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	/**
	 * @return the locationName
	 */
	public String getLocationName() {
		return locationName;
	}

	/**
	 * @param locationName
	 *            the locationName to set
	 */
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	/**
	 * @return the currentGroupId
	 */
	public BigDecimal getCurrentGroupId() {
		return currentGroupId;
	}

	/**
	 * @param currentGroupId
	 *            the currentGroupId to set
	 */
	public void setCurrentGroupId(BigDecimal currentGroupId) {
		this.currentGroupId = currentGroupId;
	}

	/**
	 * @return the currentGroupName
	 */
	public String getCurrentGroupName() {
		return currentGroupName;
	}

	/**
	 * @param currentGroupName
	 *            the currentGroupName to set
	 */
	public void setCurrentGroupName(String currentGroupName) {
		this.currentGroupName = currentGroupName;
	}

	/**
	 * @return the strategyDetails
	 */
	public List<EmployeeStrategyPlanData> getStrategyDetails() {
		return strategyDetails;
	}

	/**
	 * @param strategyDetails
	 *            the strategyDetails to set
	 */
	public void setStrategyDetails(List<EmployeeStrategyPlanData> strategyDetails) {
		this.strategyDetails = strategyDetails;
	}

	@Override
	public int compareTo(EmployeeStrategyData o) {
		// Handle null object
		if (o == null) {
			return 1;
		}
		
		// Compare last names (handle nulls)
		String thisLastName = this.getEmplLastName();
		String otherLastName = o.getEmplLastName();
		
		if (thisLastName == null && otherLastName == null) {
			// Both null, continue to first name comparison
		} else if (thisLastName == null) {
			return -1; // null comes first
		} else if (otherLastName == null) {
			return 1; // non-null comes after null
		} else {
			int lastNameComparison = thisLastName.compareTo(otherLastName);
			if (lastNameComparison != 0) {
				return lastNameComparison;
			}
		}
		
		// Compare first names (handle nulls)
		String thisFirstName = this.getEmplFirstName();
		String otherFirstName = o.getEmplFirstName();
		
		if (thisFirstName == null && otherFirstName == null) {
			// Both null, continue to middle name comparison
		} else if (thisFirstName == null) {
			return -1; // null comes first
		} else if (otherFirstName == null) {
			return 1; // non-null comes after null
		} else {
			int firstNameComparison = thisFirstName.compareTo(otherFirstName);
			if (firstNameComparison != 0) {
				return firstNameComparison;
			}
		}
		
		// Compare middle names (handle nulls)
		String thisMiddleName = this.getEmplMiddleName();
		String otherMiddleName = o.getEmplMiddleName();
		
		if (thisMiddleName == null && otherMiddleName == null) {
			return 0; // Both null, they are equal
		} else if (thisMiddleName == null) {
			return -1; // null comes first
		} else if (otherMiddleName == null) {
			return 1; // non-null comes after null
		} else {
			return thisMiddleName.compareTo(otherMiddleName);
		}
	}

}