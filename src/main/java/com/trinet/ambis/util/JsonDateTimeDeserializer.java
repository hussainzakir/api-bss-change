package com.trinet.ambis.util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.trinet.ambis.exception.BSSApplicationError;
import com.trinet.ambis.exception.BSSApplicationException;

@Component
public class JsonDateTimeDeserializer extends StdDeserializer<Date> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JsonDateTimeDeserializer() {
		this(null);
	}

	protected JsonDateTimeDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public Date deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext) {

		String date;
		try {
			date = jsonparser.getText();
		} catch (IOException e1) {
			BSSApplicationError error = new BSSApplicationError(
					"Error occurred while parsing the json string" + e1.getStackTrace());
			throw new BSSApplicationException(error);
		}
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");			
			return dateFormat.parse(date);
		} catch (ParseException e) {
			BSSApplicationError error = new BSSApplicationError(
					"Error occurred while parsing the date." + e.getStackTrace());
			throw new BSSApplicationException(error);
		}
	}
}
