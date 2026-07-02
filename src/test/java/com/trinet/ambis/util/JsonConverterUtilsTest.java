package com.trinet.ambis.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.service.dto.ProcessInfoDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JsonConverterUtilsTest {

    /**
     * Given a valid object
     * When toJson is called
     * Then it should return a valid JSON string
     */
    @Test
    public void convertObjectToJsonTest_validDto_shouldReturnJsonString() {
        // given
        ProcessInfoDto dto = new ProcessInfoDto();
        dto.setOldRealmPlanYear(101L);
        dto.setOldCompanyId(102L);
        dto.setExchangeId(201L);

        // when
        String json = JsonConverterUtils.convertObjectToJson(dto);

        // then
        assertNotNull(json);
        // Optional: validate part of the JSON structure
        assert json.contains("\"oldRealmPlanYear\":101");
    }

    /**
     * Given a valid JSON string
     * When fromJson is called
     * Then it should return a valid DTO object
     */
    @Test
    public void convertJsonTest_validJson_ToObject_shouldReturnDto() {
        // given
        String json = "{\"oldRealmPlanYear\":101,\"oldCompanyId\":102,\"exchangeId\":201}";

        // when
        ProcessInfoDto dto = JsonConverterUtils.convertJsonToObject(json, new TypeReference<ProcessInfoDto>() {});

        // then
        assertNotNull(dto);
        assertEquals(Long.valueOf(101L), dto.getOldRealmPlanYear());
        assertEquals(Long.valueOf(102L), dto.getOldCompanyId());
        assertEquals(Long.valueOf(201L), dto.getExchangeId());
    }

    /**
     * Given invalid JSON
     * When fromJson is called
     * Then it should throw RuntimeException
     */
    @Test(expected = RuntimeException.class)
    public void convertJsonTest_invalidJson_ToObject_shouldThrowException() {
        // given
        String invalidJson = "{\"oldPlanYearId\":}";

        // when
        ProcessInfoDto plYrChangeDto = JsonConverterUtils.convertJsonToObject(invalidJson, new TypeReference<ProcessInfoDto>() {
        });
    }

}
