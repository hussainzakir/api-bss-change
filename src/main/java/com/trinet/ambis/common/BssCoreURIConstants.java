package com.trinet.ambis.common;

public class BssCoreURIConstants {

    public static final String BSS_CORE_API_URI = "bssCoreApiUri";

    /** Property key for the BSS Core REST base URI (non-GraphQL). */
    public static final String BSS_CORE_REST_API_URI = "bssCoreRestApiUri";

    /** Path template for fetching company process statuses from BSS Core. */
    public static final String PROCESS_STATUS_BY_COMPANY_PATH = "/v1/companies/{companyCode}/processes";

    private BssCoreURIConstants() {

    }
}