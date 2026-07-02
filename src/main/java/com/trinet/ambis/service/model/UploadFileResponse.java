package com.trinet.ambis.service.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@lombok.Generated
@JsonAutoDetect
@NoArgsConstructor
public class UploadFileResponse {
	private List<FileStatus> errorFiles;
	private List<FileStatus> successFiles;
	private int filesSent;
	private Map<String, String> errors;

}
