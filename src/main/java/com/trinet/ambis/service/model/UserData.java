package com.trinet.ambis.service.model;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class UserData {

	private Boolean csaUser;
	private Boolean bmgUser;
	private boolean tmtUser;
	private boolean benCorpAdUser;
	private boolean benAdvisorUser;

	public UserData(boolean csaUser, boolean bmgUser, boolean tmtUser, boolean benCorpAdUser, boolean benAdvisorUser) {
		this.csaUser = csaUser;
		this.bmgUser = bmgUser;
		this.tmtUser = tmtUser;
		this.benCorpAdUser = benCorpAdUser;
		this.benAdvisorUser = benAdvisorUser;
	}

	public UserData() {
		super();
	}

}
