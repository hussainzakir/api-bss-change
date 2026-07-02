package com.trinet.ambis.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.trinet.messaging.common.Status;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@lombok.Generated
@JsonPropertyOrder({ "fileName", "status", "message" })
@NoArgsConstructor
public class FileStatus {

	@JsonProperty("File Name")
	private String fileName;
	
	@JsonProperty("Upload Status")
	private Status status;
	
	@JsonProperty("Comments")
	private String message;
}
