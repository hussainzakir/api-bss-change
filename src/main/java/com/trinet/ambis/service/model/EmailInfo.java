package com.trinet.ambis.service.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trinet.ambis.service.model.SubmissionInfo.SubmissionInfoBuilder;
import com.trinet.ambis.service.model.notification.Recipient;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Generated
@ToString
public class EmailInfo {
	@Getter
	private Map<String, Integer> bdmCounts;
	@Getter
	@Setter
	private String recipientName;
	@Getter
	@Setter
	private List<Recipient> clientRecipients;
	@Getter
	@Setter
	private List<Recipient> nonClientRecipients;
	@Getter
	@Setter
	private String confirmationStmtHtmlBody;
	@Getter
	@Setter
	private boolean isClientEmailSent;
	@Getter
	@Setter
	private boolean isAleEmailSent;
	@Getter
	private final boolean sendClientEmail;
	@Getter
	private final boolean isResendEmail;

	public EmailInfo(Map<String, Integer> bdmCounts, boolean sendClientEmail, boolean isResendEmail) {
		super();
		this.bdmCounts = bdmCounts;
		this.sendClientEmail = sendClientEmail;
		this.isResendEmail = isResendEmail;
	}

	public static class EmailInfoBuilder {
		private Map<String, Integer> bdmCounts = new HashMap<>();
		private boolean sendClientEmail;
		private boolean resendEmail;

		private SubmissionInfoBuilder submissionInfoBuilder;

		public EmailInfoBuilder bdmCounts(Map<String, Integer> bdmCounts) {
			this.bdmCounts = bdmCounts;
			return this;
		}

		public EmailInfoBuilder resendEmail(boolean resendEmail) {
			this.resendEmail = resendEmail;
			return this;
		}
		
		protected EmailInfoBuilder sendClientEmail(boolean sendClientEmail) {
			this.sendClientEmail = sendClientEmail;
			return this;
		}

		public EmailInfoBuilder(SubmissionInfoBuilder submissionInfoBuilder) {
			this.submissionInfoBuilder = submissionInfoBuilder;
		}

		public SubmissionInfoBuilder buildEmailInfo() {
			return this.submissionInfoBuilder;
		}

		protected EmailInfo build() {
			return new EmailInfo(bdmCounts, sendClientEmail, resendEmail);
		}
	}

}
