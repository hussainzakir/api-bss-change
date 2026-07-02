package com.trinet.ambis.service.model.bsscore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class CompanyResponse {

	private CompanyPayload companyByCode;

	private CompanyPayload companyByPeoCompanyId;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonIgnoreProperties
	public static class CompanyPayload {

		private List<Census> census;
		private AleStatus aleStatus;

		@Data
		@Builder
		@AllArgsConstructor
		@NoArgsConstructor
		@JsonIgnoreProperties
		public static class Census {

			private String prospectEmployeeId;
			private String firstName;
			private String lastName;
			private BigDecimal annualWages;
			private String homeState;
			private String homePostalCode;
			private String medicalTier;
			private String dentalTier;
			private String visionTier;
            private boolean k1;
		}

		@Data
		@Builder
		@AllArgsConstructor
		@NoArgsConstructor
		@JsonIgnoreProperties
		public static class AleStatus {
			private boolean ale;
			private long companyId;
		}

	}
}