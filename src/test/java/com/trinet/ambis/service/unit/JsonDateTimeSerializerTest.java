package com.trinet.ambis.service.unit;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.trinet.ambis.util.JsonDateTimeSerializer;

import lombok.SneakyThrows;

@RunWith(JUnit4.class)

public class JsonDateTimeSerializerTest {

	@InjectMocks
	private JsonDateTimeSerializer serializer;

	@Mock
	private JsonGenerator gen;

	@Mock
	private SerializerProvider provider;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	@SneakyThrows({ JsonParseException.class, IOException.class })
	public void serialize() {
		Date date = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd'T'HH:mm:ss.SSSZ");
		try {
			date = df.parse("2019-JUL-21T00:00:00.000-0400");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		serializer.serialize(date, gen, provider);
//		verify(gen, times(1)).writeString("2019-07-21T00:00:00.000-0400");
	}

}
