package com.trinet.ambis.service.model.notification;

import lombok.Data;

@Data
public class Recipient {

	// @Email // this could be email and phone number.
	private String id;
	private String firstName;
	private String lastName;
	private String middleName;
	private String type = "to";
	private String tenentId;

	public Recipient() {
	}

	public Recipient(String id) {
		setId(id);
	}

}