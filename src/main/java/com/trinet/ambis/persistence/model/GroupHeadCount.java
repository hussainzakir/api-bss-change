package com.trinet.ambis.persistence.model;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="XBSS_GROUP_COV_HEADCOUNT")
public class GroupHeadCount implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="GROUP_ID")
	private long groupId;
	@Column(name="EMPLOYEE")
	private int empHeadCount;	
	@Column(name="SPOUSE")
	private int empSpouseHeadCount;
	@Column(name="CHILDREN")
	private int empChildrenHeadCount;
	@Column(name="FAMILY")
	private int empFamilyHeadCount;
	
	@Column(name="SPOUSE")
	public int getEmpSpouseHeadCount() {
		return empSpouseHeadCount;
	}

	public void setEmpSpouseHeadCount(int empSpouseHeadCount) {
		this.empSpouseHeadCount = empSpouseHeadCount;
	}

	@Column(name="EMPLOYEE")
	public int getEmpHeadCount() {
		return empHeadCount;
	}

	public void setEmpHeadCount(int employeeHeadCount) {
		this.empHeadCount = employeeHeadCount;
	}

	@Column(name="CHILDREN")
	public int getEmpChildrenHeadCount() {
		return empChildrenHeadCount;
	}

	public void setEmpChildrenHeadCount(int empChildrenHeadCount) {
		this.empChildrenHeadCount = empChildrenHeadCount;
	}

	@Column(name="FAMILY")
	public int getEmpFamilyHeadCount() {
		return empFamilyHeadCount;
	}

	public void setEmpFamilyHeadCount(int empFamilyHeadCount) {
		this.empFamilyHeadCount = empFamilyHeadCount;
	}

	@Column(name="GROUP_ID")
	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + empHeadCount;
		result = prime * result + empSpouseHeadCount;
		result = prime * result + empChildrenHeadCount;
		result = prime * result + empFamilyHeadCount;
		result = prime * result + Long.toString(groupId).hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupHeadCount other = (GroupHeadCount) obj;
		
		if (other.getGroupId() != groupId) {
			return false;
		} 
		return true;
	}
	
}


