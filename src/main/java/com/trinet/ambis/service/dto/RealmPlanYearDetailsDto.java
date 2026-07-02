package com.trinet.ambis.service.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RealmPlanYearDetailsDto implements Serializable {

	private static final long serialVersionUID = -1817456323378644924L;

	private long id;

	private long realmId;

	private String quarter;

	private Date startDate;

	private Date endDate;

}
