package com.trinet.ambis.service.model;

import com.trinet.ambis.common.BSSApplicationConstants;
import com.trinet.ambis.persistence.model.SubmitStatus;
import com.trinet.ambis.service.model.SubmissionInfo.SubmissionInfoBuilder;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Generated
@ToString
public class SubmitStatusInfo {

	@Setter
	@Getter
	private String errorMessage;
	@Getter
	private String serviceOrderNumber;
	@Getter
	private final String confirmationNumber;
	@Getter
	private final String payload;
	@Getter
	private String submitStatus;
	@Getter
	private String userId;
	@Setter
	@Getter
	private String statementUploadStatus;
	@Getter
	private Exception submitException;

	private SubmitStatusInfo(String confirmationNumber, String payload, String userId, String submitStatus,
			String statementUploadStatus, String errorMessage, String serviceOrderNumber, Exception exception) {
		super();
		this.confirmationNumber = confirmationNumber;
		this.payload = payload;
		this.userId = userId;
		this.submitStatus = submitStatus;
		this.statementUploadStatus = statementUploadStatus;
		this.errorMessage = errorMessage;
		this.serviceOrderNumber = serviceOrderNumber;
		this.submitException = exception;
	}

	public static class SubmitStatusInfoBuilder {
		private SubmitStatus submitStatus;
		private Exception exception;

		private SubmissionInfoBuilder submissionInfoBuilder;

		public SubmitStatusInfoBuilder submitStatus(SubmitStatus submitStatus) {
			this.submitStatus = submitStatus;
			return this;
		}

		public SubmitStatusInfoBuilder exception(Exception exception) {
			this.exception = exception;
			return this;
		}

		public SubmitStatusInfoBuilder(SubmissionInfoBuilder submissionInfoBuilder) {
			this.submissionInfoBuilder = submissionInfoBuilder;
		}

		public SubmissionInfoBuilder buildSubmissionInfo() {
			this.submissionInfoBuilder.companyCode(this.submitStatus.getCompany());
			this.submissionInfoBuilder.withEmailInfo().sendClientEmail(this.submitStatus.getSendEmail());
			return this.submissionInfoBuilder;
		}

		protected SubmitStatusInfo build() {
			assert this.submitStatus != null : "Please construct SubmissionInfo.SubmitStatusInfo with SubmitStatus";
			String submitStatusStr;
			if (this.exception == null) {
				submitStatusStr = this.submitStatus.getStatus();
			} else {
				submitStatusStr = BSSApplicationConstants.ERROR;
			}

			return new SubmitStatusInfo(this.submitStatus.getConfirmationNumber(), this.submitStatus.getSubmitPayload().getPayload(),
					this.submitStatus.getUserId(), submitStatusStr, this.submitStatus.getStatementUploadStatus(),
					this.submitStatus.getSubmitError() == null ? null
							: this.submitStatus.getSubmitError().getErrorMsg(),
					this.submitStatus.getServiceOrder(), exception);
		}
	}

}
