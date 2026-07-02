package com.trinet.ambis.service.model;

import com.trinet.ambis.service.model.EmailInfo.EmailInfoBuilder;
import com.trinet.ambis.service.model.SubmitStatusInfo.SubmitStatusInfoBuilder;

import lombok.Generated;
import lombok.Getter;
import lombok.ToString;

@Generated
@ToString
public class SubmissionInfo {

	@Getter
	private final EmailInfo emailInfo;
	@Getter
	private final SubmitStatusInfo submitStatusInfo;
	@Getter
	private final String companyCode;
	@Getter
	private final boolean isQueuedSubmit;
	@Getter
	private final boolean isResubmit;
	@Getter
	private boolean isDefaultSubmit;
	@Getter
	private boolean isPreSubmit;

	private SubmissionInfo(EmailInfo emailInfo, SubmitStatusInfo submitStatusInfo, String companyCode,
			boolean isQueuedSubmit, boolean isResubmit, boolean isDefaultSubmit, boolean isPreSubmit) {
		super();
		this.emailInfo = emailInfo;
		this.submitStatusInfo = submitStatusInfo;
		this.companyCode = companyCode;
		this.isQueuedSubmit = isQueuedSubmit;
		this.isResubmit = isResubmit;
		this.isDefaultSubmit = isDefaultSubmit;
		this.isPreSubmit = isPreSubmit;
	}

	public static class SubmissionInfoBuilder {
		private EmailInfo emailInfo;
		private SubmitStatusInfo submitStatusInfo;
		private String companyCode;
		private boolean isQueuedSubmit;
		private boolean isResubmit;
		private boolean isDefaultSubmit;

		private EmailInfoBuilder emailInfoBuilder;
		private SubmitStatusInfoBuilder submitStatusInfoBuilder;

		protected SubmissionInfoBuilder companyCode(String companyCode) {
			this.companyCode = companyCode;
			return this;
		}

		public SubmissionInfoBuilder queuedSubmit(boolean isQueuedSubmit) {
			this.isQueuedSubmit = isQueuedSubmit;
			return this;
		}

		public SubmissionInfoBuilder resubmit(boolean isResubmit) {
			this.isResubmit = isResubmit;
			return this;
		}

		public SubmissionInfoBuilder defaultSubmit(boolean isDefaultSubmit) {
			this.isDefaultSubmit = isDefaultSubmit;
			return this;
		}

		public SubmissionInfoBuilder() {
			this.emailInfoBuilder = new EmailInfoBuilder(this);
			this.submitStatusInfoBuilder = new SubmitStatusInfoBuilder(this);
		}

		public EmailInfoBuilder withEmailInfo() {
			return this.emailInfoBuilder;
		}

		public SubmitStatusInfoBuilder withSubmitStatusInfo() {
			return this.submitStatusInfoBuilder;
		}

		public SubmissionInfo buildPreSubmit() {
			this.emailInfo = this.emailInfoBuilder.build();
			this.submitStatusInfo = this.submitStatusInfoBuilder.build();
			return new SubmissionInfo(emailInfo, submitStatusInfo, companyCode, isQueuedSubmit, false, isDefaultSubmit,
					true);
		}

		public SubmissionInfo buildPostSubmit() {
			this.emailInfo = this.emailInfoBuilder.build();
			this.submitStatusInfo = this.submitStatusInfoBuilder.build();
			return new SubmissionInfo(emailInfo, submitStatusInfo, companyCode, isQueuedSubmit, isResubmit,
					isDefaultSubmit, false);
		}

	}

}
