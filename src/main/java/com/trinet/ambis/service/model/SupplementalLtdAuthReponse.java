package com.trinet.ambis.service.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.trinet.ambis.util.JsonDateSerializer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown=true)
public class SupplementalLtdAuthReponse {
	private String userId;
	private String firstName;
	private String lastName;
	private String email;
	private boolean displayPopup;
	private boolean displayBanners;
	private Character answer;
	private String authUserId;
	private String authFirstName;
	private String authLastName;
	private String authEmail;
	@JsonSerialize(using = JsonDateSerializer.class)
	private Date authDate;
}