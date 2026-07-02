package com.trinet.ambis.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PlanCarrier implements Comparable<PlanCarrier>, Serializable {

	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
    @JsonIgnore
	private String code;
    @JsonIgnore
	private String planType;
	private boolean mandatory = false;
	private boolean restricted = false;
	private boolean employeePaid = false;
	private List<String> parentId;
	private boolean isPrimaryCarrier;
	private List<Integer> regionalCarriers = new ArrayList<>();

	public PlanCarrier() {
		super();
	}

	public PlanCarrier(int id, String name, String code) {
		super();
		this.id = id;
		this.name = name;
		this.code = code;
	}

	@Override
	public int compareTo(PlanCarrier obj) {
		return ((Integer) obj.getId()).compareTo(getId());
	}

}
