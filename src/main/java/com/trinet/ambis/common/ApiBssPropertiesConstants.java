package com.trinet.ambis.common;

public class ApiBssPropertiesConstants {

	public static final String BSS_SUBMISSION_FAILURE_TO_ADDRESS = "bssSubmissionFailureToAddress";
	public static final String BSS_TEAM_TO_ADDRESS = "bssTeamToAddress";
	public static final String SUPPORT_ADDRESS_PROPERTY = "bssSupportEmailAddress";
	public static final String SUBMISSION_FAILURE_SUBJECT_FOR_SINGLE_CLIENT = "submissionFailureSubjectForSingleClient";
	public static final String SUBMISSION_FAILURE_SUBJECT_FOR_QUARTER = "submissionFailureSubjectForQuarter";
	public static final String REAL_TIME_SYNC_FAILURE_SUBJECT = "realTimeSyncFailureSubject";
	public static final String TOKEN = "token";
	public static final String COOKIE = "Cookie";

	public static final String CARRIER_CODE_TO_DESC_MAPPING = "CarrierCodeToDescMapping";

	private ApiBssPropertiesConstants() {
		throw new IllegalStateException(
				"Constants class " + ApiBssPropertiesConstants.class.getName() + " can not be instantiated.");
	}

}
