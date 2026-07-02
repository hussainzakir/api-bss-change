package com.trinet.ambis.service.prospect.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * DTO for handling the standardized prospect API response
 * 
 * @param <T>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiRes<T> {

	private T data;

	private Error error;

	private String requestId;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Error {

		private String code;

		private String description;

	}

}
