package com.trinet.ambis.persistence.dao.ps.impl;

import com.trinet.ambis.service.model.BnRateData;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This class extends the model for PS_BN_RATE_DATA with some additional values required
 * for the insert of rate table rows during the BSS submit transaction.  These fields do
 * not exist on the database record described by the model.
 * @author mbrothers
 *
 */
@Data
@EqualsAndHashCode( callSuper=true )
@ToString( callSuper=true )
public class BnRateDataForInsert extends BnRateData {
	private String planType;
	private String quarter;
	private String bandCode;
}
