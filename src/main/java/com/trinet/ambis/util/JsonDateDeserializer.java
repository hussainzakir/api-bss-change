package com.trinet.ambis.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.trinet.ambis.common.BSSHttpStatusConstants;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.exception.BSSErrorResponseCodes;

public class JsonDateDeserializer extends JsonDeserializer<Date> {

	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public Date deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
		if (jp.getCurrentToken().equals(JsonToken.VALUE_STRING)) {
			try {
				return format.parse(jp.getText());
			} catch (ParseException e) {
				throw new BSSApplicationException(new BSSApplicationError(BSSErrorResponseCodes.BSS_UNHANDLED_EXCEPTION,
						BSSHttpStatusConstants.INTERNAL_SERVER_ERROR, JsonDateDeserializer.class.getName(),
						"Error occurred while deserializing the date", null, null));
			}
		}
		return null;
	}

}