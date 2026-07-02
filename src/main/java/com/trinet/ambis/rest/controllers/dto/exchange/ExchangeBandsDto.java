package com.trinet.ambis.rest.controllers.dto.exchange;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.trinet.ambis.enums.OmsOfferingEnum;
import com.trinet.ambis.enums.RiskTypeEnum;
import com.trinet.ambis.rest.controllers.views.ExchangeBandsViews;
import com.trinet.ambis.util.JsonDateDeserializer;
import com.trinet.ambis.util.JsonDateSerializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder(buildMethodName = "buildExchangeBands")
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeBandsDto {

	@JsonView({ ExchangeBandsViews.GetResView.class, ExchangeBandsViews.PutResView.class })
	private String exchangeId;

	@JsonView(ExchangeBandsViews.GetResView.class)
	private String exchangeName;

	@JsonView(ExchangeBandsViews.PutResView.class)
	private boolean benefitsQuarterException;

	@JsonView(ExchangeBandsViews.PutResView.class)
	private boolean oldBenefitsQuarterException;

	@JsonView({ ExchangeBandsViews.GetResView.class, ExchangeBandsViews.PutResView.class })
	List<CarrierBand> bands;

	@JsonView(ExchangeBandsViews.PutResView.class)
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date benefitsStartDate;

	@JsonView(ExchangeBandsViews.PutResView.class)
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date oldBenefitsStartDate;

	@JsonView(ExchangeBandsViews.PutResView.class)
	@JsonSerialize(using = JsonDateSerializer.class)
	@JsonDeserialize(using = JsonDateDeserializer.class)
	private Date oldQuarterEffectiveDate;
	
	@JsonView(ExchangeBandsViews.PutResView.class)
	private String bundleId;
	
	@JsonView(ExchangeBandsViews.PutResView.class)
	private OmsOfferingEnum omsOffering;

	@JsonView({ ExchangeBandsViews.PutResView.class })
	private RiskTypeEnum riskType;

	private String naicsCode;
	
	public enum BandTypeEnum {
		PRIMARY, ALTERNATE;
	}

	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder(buildMethodName = "buildCarrierBand")
	public static class CarrierBand implements Comparable<CarrierBand> {

		@JsonView({ ExchangeBandsViews.GetResView.class, ExchangeBandsViews.PutResView.class })
		@JsonSerialize(using = JsonDateSerializer.class)
		@JsonDeserialize(using = JsonDateDeserializer.class)
		private Date effectiveDate;

		@Builder.Default
		@JsonView(ExchangeBandsViews.GetResView.class)
		private String bandType = BandTypeEnum.ALTERNATE.name().toLowerCase();

		@JsonView(ExchangeBandsViews.GetResView.class)
		private List<CarrierBandDetails> carrierBands;

		@JsonView(ExchangeBandsViews.PutResView.class)
		private long companyId;

		@JsonView(ExchangeBandsViews.PutResView.class)
		private String oeQuarter;

		@Data
		@AllArgsConstructor
		@NoArgsConstructor
		@Builder(buildMethodName = "buildCarrierBandDetails")
		public static class CarrierBandDetails {

			@JsonView(ExchangeBandsViews.GetResView.class)
			private String carrier;

			@EqualsAndHashCode.Exclude
			@JsonView(ExchangeBandsViews.GetResView.class)
			private String bandCode;
		}

		@Override
		public int compareTo(CarrierBand o) {
			return getEffectiveDate().compareTo(o.getEffectiveDate());
		}

	}

}
