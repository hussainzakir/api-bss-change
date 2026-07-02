package com.trinet.ambis.service.prospect.enums;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ProcessStatusEnumTest {

    @Test
    public void testProspectStrategySyncPlyrChangeEnum() {
        ProcessStatusEnum processStatus = ProcessStatusEnum.STRATEGY_SYNC_PLYR_CHANGE;
        assertNotNull(processStatus);
        assertEquals("STRATEGY_SYNC_PLYR_CHANGE", processStatus.getProcessName());
        assertEquals("PROSPECT_ID", processStatus.getIdentifierName());
    }

    @Test
    public void testStrategyCreateProcessEnum() {
        ProcessStatusEnum processStatus = ProcessStatusEnum.STRATEGY_CREATE_PROCESS;
        assertNotNull(processStatus);
        assertEquals("STRATEGY_CREATE", processStatus.getProcessName());
        assertEquals("COMPANY_CODE", processStatus.getIdentifierName());
    }

    @Test
    public void testPreLoadEnum() {
        ProcessStatusEnum processStatus = ProcessStatusEnum.PRE_LOAD;
        assertNotNull(processStatus);
        assertEquals("PRE_LOAD", processStatus.getProcessName());
        assertEquals("QUARTER", processStatus.getIdentifierName());
    }

    @Test
    public void testProspectBandUpdateEventEnum() {
        ProcessStatusEnum processStatus = ProcessStatusEnum.BAND_UPDATE_EVENT;
        assertNotNull(processStatus);
        assertEquals("BAND_UPDATE_EVENT", processStatus.getProcessName());
        assertEquals("PROSPECT_ID", processStatus.getIdentifierName());
    }

    @Test
    public void testTermedClientDefaultSubmitEnum() {
        ProcessStatusEnum processStatus = ProcessStatusEnum.TERMED_CLIENT_DEFAULT_SUBMIT;
        assertNotNull(processStatus);
        assertEquals("TERM_DEFAULT", processStatus.getProcessName());
        assertEquals("CONF_NUMBER", processStatus.getIdentifierName());
    }

    @Test
    public void testBandCodeResubmitProcessEnum() {
        ProcessStatusEnum processStatus = ProcessStatusEnum.BAND_CODE_RESUBMIT_PROCESS;
        assertNotNull(processStatus);
        assertEquals("BANDCODE_RESUBMIT", processStatus.getProcessName());
        assertEquals("CONF_NUMBER", processStatus.getIdentifierName());
    }

    @Test
    public void testResubmitProcessEnum() {
        ProcessStatusEnum processStatus = ProcessStatusEnum.RESUBMIT_PROCESS;
        assertNotNull(processStatus);
        assertEquals("RESUBMIT", processStatus.getProcessName());
        assertEquals("CONF_NUMBER", processStatus.getIdentifierName());
    }

    @Test
    public void testSubmitProcessEnum() {
        ProcessStatusEnum processStatus = ProcessStatusEnum.SUBMIT_PROCESS;
        assertNotNull(processStatus);
        assertEquals("SUBMIT", processStatus.getProcessName());
        assertEquals("CONF_NUMBER", processStatus.getIdentifierName());
    }

}
