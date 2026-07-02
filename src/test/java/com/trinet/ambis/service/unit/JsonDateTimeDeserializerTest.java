package com.trinet.ambis.service.unit;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinet.ambis.exception.BSSApplicationException;
import com.trinet.ambis.util.JsonDateTimeDeserializer;

import lombok.SneakyThrows;

@RunWith(JUnit4.class)

public class JsonDateTimeDeserializerTest {

	private ObjectMapper mapper;
	private JsonDateTimeDeserializer deserializer;

	@Before
	public void setup() {
		mapper = new ObjectMapper();
		deserializer = new JsonDateTimeDeserializer();
	}

	@Test
	@SneakyThrows(ParseException.class)
	public void deserialize() {
		String json = String.format("{\"submitDate\":%s}", "\"2019-07-21T11:34:56.145-0500\"");

		Date deserialisedDate = deserialiseDate(json);

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		Date expectedDate = df.parse("2019-07-21T11:34:56.145-0500");
		assertEquals(expectedDate, deserialisedDate);
	}

	@Test(expected = BSSApplicationException.class)
	public void deserialize_exception() {
		String json = String.format("{\"submitDate\":%s}", "\"2019-07-2100:00:00.000-0000\"");
		deserialiseDate(json);
	}

	@SneakyThrows({ JsonParseException.class, IOException.class })
	private Date deserialiseDate(String json) {
		InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
		JsonParser parser = mapper.getFactory().createParser(stream);
		DeserializationContext ctxt = mapper.getDeserializationContext();
		parser.nextToken();
		parser.nextToken();
		parser.nextToken();
		return deserializer.deserialize(parser, ctxt);
	}
}
