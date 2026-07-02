package com.trinet.ambis.jpa.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

/**
 * @author schaudhari
 *
 *         This class helps to convert the Boolean datatype of the JPA entity
 *         attribute to "Y" or "N" String representation in of the table column.
 */
@Converter
public class BooleanToStringConverter implements AttributeConverter<Boolean, String> {

	@Override
	public String convertToDatabaseColumn(Boolean value) {
		return (value != null && value) ? "Y" : "N";
	}

	@Override
	public Boolean convertToEntityAttribute(String value) {
		return (StringUtils.isEmpty(value) || "N".equals(value)) ? false : true;
	}
}