package com.trinet.ambis.common;

import java.util.HashMap;
import java.util.Map;

public final class PlanAttributeConstants {

    private PlanAttributeConstants() {
        // Private constructor to prevent instantiation
    }

    public static final String KEY_PLAN_NAME = "planName";
    public static final String KEY_PLAN_RATES = "planRates";
    public static final String KEY_DEDUCTIBLE = "DEDUCTIBLE_GROUP";
    public static final String KEY_OOP = "OOP_GROUP";
    public static final String KEY_VISIT = "VISIT_GROUP";
    public static final String KEY_MED_COINS = "105";
    public static final String KEY_MED_RX = "100";
    public static final String KEY_DEN_PREV = "102";
    public static final String KEY_DEN_BASIC = "103";
    public static final String KEY_DEN_MAJOR = "104";
    public static final String KEY_DEN_ANNUAL_MAX = "105";
    public static final String KEY_DEN_ORTHO = "106";
    public static final String KEY_DEN_ENDO = "47";
    public static final String KEY_VIS_EXAM_COPAY = "100";
    public static final String KEY_VIS_MAT_COPAY = "101";
    public static final String KEY_VIS_FRAMES_ALLOW = "102";
    public static final String KEY_VIS_EXAM_FREQ = "52";
    public static final String KEY_VIS_FRAMES_FREQ = "58";
    public static final String KEY_VIS_LENS_FREQ = "55";

    public static final String PCC_MEDICAL = "PCC_MEDICAL";
    public static final String APX_MEDICAL = "APX_MEDICAL";
    public static final String PCC_DENTAL = "PCC_DENTAL";
    public static final String APX_DENTAL = "APX_DENTAL";
    public static final String PCC_VISION = "PCC_VISION";
    public static final String APX_VISION = "APX_VISION";

    // Medical Comparison Limits
    public static final Double LIMIT_COMP_MED_PLAN_NAME = 29.0;
    public static final Double LIMIT_COMP_MED_PLAN_RATES = 12.0;
    public static final Double LIMIT_COMP_MED_DEDUCTIBLE = 12.0;
    public static final Double LIMIT_COMP_MED_OOP = 15.0;
    public static final Double LIMIT_COMP_MED_COINS = 5.0;
    public static final Double LIMIT_COMP_MED_VISIT = 19.0;
    public static final Double LIMIT_COMP_MED_RX = 18.0;

    // Medical Appendix Limits
    public static final Double LIMIT_APP_MED_PLAN_NAME = 44.0;
    public static final Double LIMIT_APP_MED_PLAN_RATES = 10.0;
    public static final Double LIMIT_APP_MED_DEDUCTIBLE = 17.0;
    public static final Double LIMIT_APP_MED_OOP = 17.0;
    public static final Double LIMIT_APP_MED_COINS = 4.0;
    public static final Double LIMIT_APP_MED_VISIT = 28.0;
    public static final Double LIMIT_APP_MED_RX = 20.0;

    // Dental Comparison Limits
    public static final Double LIMIT_COMP_DEN_PLAN_NAME = 27.0;
    public static final Double LIMIT_COMP_DEN_PLAN_RATES = 11.0;
    public static final Double LIMIT_COMP_DEN_DEDUCTIBLE = 13.0;
    public static final Double LIMIT_COMP_DEN_PREV = 11.0;
    public static final Double LIMIT_COMP_DEN_BASIC = 8.0;
    public static final Double LIMIT_COMP_DEN_MAJOR = 8.0;
    public static final Double LIMIT_COMP_DEN_ANNUAL_MAX = 12.0;
    public static final Double LIMIT_COMP_DEN_ORTHO = 8.0;
    public static final Double LIMIT_COMP_DEN_ENDO = 17.0;

    // Dental Appendix Limits
    public static final Double LIMIT_APP_DEN_PLAN_NAME = 36.0;
    public static final Double LIMIT_APP_DEN_PLAN_RATES = 8.0;
    public static final Double LIMIT_APP_DEN_DEDUCTIBLE = 17.0;
    public static final Double LIMIT_APP_DEN_PREV = 12.0;
    public static final Double LIMIT_APP_DEN_BASIC = 6.0;
    public static final Double LIMIT_APP_DEN_MAJOR = 5.0;
    public static final Double LIMIT_APP_DEN_ANNUAL_MAX = 14.0;
    public static final Double LIMIT_APP_DEN_ORTHO = 11.0;
    public static final Double LIMIT_APP_DEN_ENDO = 17.0;

    // Vision Comparison Limits
    public static final Double LIMIT_COMP_VIS_PLAN_NAME = 24.0;
    public static final Double LIMIT_COMP_VIS_PLAN_RATES = 9.0;
    public static final Double LIMIT_COMP_VIS_EXAM_COPAY = 10.0;
    public static final Double LIMIT_COMP_VIS_MAT_COPAY = 10.0;
    public static final Double LIMIT_COMP_VIS_FRAMES_ALLOW = 11.0;
    public static final Double LIMIT_COMP_VIS_EXAM_FREQ = 16.0;
    public static final Double LIMIT_COMP_VIS_FRAMES_FREQ = 19.0;
    public static final Double LIMIT_COMP_VIS_LENS_FREQ = 23.0;

    // Vision Appendix Limits
    public static final Double LIMIT_APP_VIS_PLAN_NAME = 28.0;
    public static final Double LIMIT_APP_VIS_PLAN_RATES = 10.0;
    public static final Double LIMIT_APP_VIS_EXAM_COPAY = 15.0;
    public static final Double LIMIT_APP_VIS_MAT_COPAY = 16.0;
    public static final Double LIMIT_APP_VIS_FRAMES_ALLOW = 17.0;
    public static final Double LIMIT_APP_VIS_EXAM_FREQ = 15.0;
    public static final Double LIMIT_APP_VIS_FRAMES_FREQ = 15.0;
    public static final Double LIMIT_APP_VIS_LENS_FREQ = 25.0;

    public static final String EE_COST_STD = "EE_COST_STD";
    public static final String EE_COST_ALT = "EE_COST_ALT";

    public static final String KEY_EMP_NAME = "empName";
    public static final String KEY_EMP_STATE = "empState";
    public static final String KEY_EMP_TIERS = "empTiers";
    public static final String KEY_EMP_CURR_PLAN_NAME = "currPlanName";
    public static final String KEY_EMP_PROP_PLAN_NAME = "propPlanName";
    public static final String KEY_EMP_COST = "cost";
    public static final String KEY_EMP_COST_DIFF = "costDiff";

    public static final Double LIMIT_EE_COST_STD_EMP_NAME = 14.0;
    public static final Double LIMIT_EE_COST_STD_STATE = 8.0;
    public static final Double LIMIT_EE_COST_STD_TIERS = 8.0;
    public static final Double LIMIT_EE_COST_STD_CURR_PLAN_NAME = 28.0;
    public static final Double LIMIT_EE_COST_STD_PROP_PLAN_NAME = 31.0;
    public static final Double LIMIT_EE_COST_STD_COST = 9.0;
    public static final Double LIMIT_EE_COST_STD_DIFF = 10.0;

    public static final Double LIMIT_EE_COST_ALT_EMP_NAME = 42.0;
    public static final Double LIMIT_EE_COST_ALT_STATE = 16.0;
    public static final Double LIMIT_EE_COST_ALT_TIERS = 8.0;
    public static final Double LIMIT_EE_COST_ALT_CURR_PLAN_NAME = 30.0;
    public static final Double LIMIT_EE_COST_ALT_PROP_PLAN_NAME = 38.0;
    public static final Double LIMIT_EE_COST_ALT_COST = 18.0;
    public static final Double LIMIT_EE_COST_ALT_DIFF = 6.0;

    public static final int ID_PRIMARY_SPECIALIST = 106;
    public static final int ID_URGENT_CARE = 11;
    public static final int ID_EMERGENCY_ROOM = 7;

    public static final int ID_DEDUCTIBLE_SINGLE_DEN = 100;
    public static final int ID_DEDUCTIBLE_FAMILY_DEN = 101;
    public static final int ID_DEDUCTIBLE_SINGLE_MED = 101;
    public static final int ID_DEDUCTIBLE_FAMILY_MED = 102;
    public static final int ID_OOP_SINGLE = 103;
    public static final int ID_OOP_FAMILY = 104;

    public static final String SEPARATOR_SLASH = " / ";
    public static final String SUFFIX_IN_NETWORK_OON = "(In-Network/OON)";

    public static final int SKIP_ATTR_NAT_NETWORK = 27;
    public static final int SKIP_ATTR_RX_DED_NON_GENERIC = 3;
    public static final int SKIP_ATTR_ORTHO_OR_HOSP_OUTPATIENT = 107;
    public static final int SKIP_ATTR_HOSP_INPATIENT = 6;
    public static final int SKIP_ATTR_WAITING_PERIOD_MAJOR = 108;

    public static final String ATTR_TYPE_CATEGORY = "category";
    public static final String ATTR_TYPE_ATTRIBUTE = "attribute";

    public static final Map<String, Map<String, Double>> LIMITS_MAP = new HashMap<>();

    static {
        // COMPARISON_MEDICAL
        Map<String, Double> compMed = new HashMap<>();
        compMed.put(KEY_PLAN_NAME, LIMIT_COMP_MED_PLAN_NAME);
        compMed.put(KEY_PLAN_RATES, LIMIT_COMP_MED_PLAN_RATES);
        compMed.put(KEY_DEDUCTIBLE, LIMIT_COMP_MED_DEDUCTIBLE);
        compMed.put(KEY_OOP, LIMIT_COMP_MED_OOP);
        compMed.put(KEY_MED_COINS, LIMIT_COMP_MED_COINS);
        compMed.put(KEY_VISIT, LIMIT_COMP_MED_VISIT);
        compMed.put(KEY_MED_RX, LIMIT_COMP_MED_RX);
        LIMITS_MAP.put(PCC_MEDICAL, compMed);

        // APPENDIX_MEDICAL
        Map<String, Double> appMed = new HashMap<>();
        appMed.put(KEY_PLAN_NAME, LIMIT_APP_MED_PLAN_NAME);
        appMed.put(KEY_PLAN_RATES, LIMIT_APP_MED_PLAN_RATES);
        appMed.put(KEY_DEDUCTIBLE, LIMIT_APP_MED_DEDUCTIBLE);
        appMed.put(KEY_OOP, LIMIT_APP_MED_OOP);
        appMed.put(KEY_MED_COINS, LIMIT_APP_MED_COINS);
        appMed.put(KEY_VISIT, LIMIT_APP_MED_VISIT);
        appMed.put(KEY_MED_RX, LIMIT_APP_MED_RX);
        LIMITS_MAP.put(APX_MEDICAL, appMed);

        // COMPARISON_DENTAL
        Map<String, Double> compDen = new HashMap<>();
        compDen.put(KEY_PLAN_NAME, LIMIT_COMP_DEN_PLAN_NAME);
        compDen.put(KEY_PLAN_RATES, LIMIT_COMP_DEN_PLAN_RATES);
        compDen.put(KEY_DEDUCTIBLE, LIMIT_COMP_DEN_DEDUCTIBLE);
        compDen.put(KEY_DEN_PREV, LIMIT_COMP_DEN_PREV);
        compDen.put(KEY_DEN_BASIC, LIMIT_COMP_DEN_BASIC);
        compDen.put(KEY_DEN_MAJOR, LIMIT_COMP_DEN_MAJOR);
        compDen.put(KEY_DEN_ANNUAL_MAX, LIMIT_COMP_DEN_ANNUAL_MAX);
        compDen.put(KEY_DEN_ORTHO, LIMIT_COMP_DEN_ORTHO);
        compDen.put(KEY_DEN_ENDO, LIMIT_COMP_DEN_ENDO);
        LIMITS_MAP.put(PCC_DENTAL, compDen);

        // APPENDIX_DENTAL
        Map<String, Double> appDen = new HashMap<>();
        appDen.put(KEY_PLAN_NAME, LIMIT_APP_DEN_PLAN_NAME);
        appDen.put(KEY_PLAN_RATES, LIMIT_APP_DEN_PLAN_RATES);
        appDen.put(KEY_DEDUCTIBLE, LIMIT_APP_DEN_DEDUCTIBLE);
        appDen.put(KEY_DEN_PREV, LIMIT_APP_DEN_PREV);
        appDen.put(KEY_DEN_BASIC, LIMIT_APP_DEN_BASIC);
        appDen.put(KEY_DEN_MAJOR, LIMIT_APP_DEN_MAJOR);
        appDen.put(KEY_DEN_ANNUAL_MAX, LIMIT_APP_DEN_ANNUAL_MAX);
        appDen.put(KEY_DEN_ORTHO, LIMIT_APP_DEN_ORTHO);
        appDen.put(KEY_DEN_ENDO, LIMIT_APP_DEN_ENDO);
        LIMITS_MAP.put(APX_DENTAL, appDen);

        // COMPARISON_VISION
        Map<String, Double> compVis = new HashMap<>();
        compVis.put(KEY_PLAN_NAME, LIMIT_COMP_VIS_PLAN_NAME);
        compVis.put(KEY_PLAN_RATES, LIMIT_COMP_VIS_PLAN_RATES);
        compVis.put(KEY_VIS_EXAM_COPAY, LIMIT_COMP_VIS_EXAM_COPAY);
        compVis.put(KEY_VIS_MAT_COPAY, LIMIT_COMP_VIS_MAT_COPAY);
        compVis.put(KEY_VIS_FRAMES_ALLOW, LIMIT_COMP_VIS_FRAMES_ALLOW);
        compVis.put(KEY_VIS_EXAM_FREQ, LIMIT_COMP_VIS_EXAM_FREQ);
        compVis.put(KEY_VIS_FRAMES_FREQ, LIMIT_COMP_VIS_FRAMES_FREQ);
        compVis.put(KEY_VIS_LENS_FREQ, LIMIT_COMP_VIS_LENS_FREQ);
        LIMITS_MAP.put(PCC_VISION, compVis);

        // APPENDIX_VISION
        Map<String, Double> appVis = new HashMap<>();
        appVis.put(KEY_PLAN_NAME, LIMIT_APP_VIS_PLAN_NAME);
        appVis.put(KEY_PLAN_RATES, LIMIT_APP_VIS_PLAN_RATES);
        appVis.put(KEY_VIS_EXAM_COPAY, LIMIT_APP_VIS_EXAM_COPAY);
        appVis.put(KEY_VIS_MAT_COPAY, LIMIT_APP_VIS_MAT_COPAY);
        appVis.put(KEY_VIS_FRAMES_ALLOW, LIMIT_APP_VIS_FRAMES_ALLOW);
        appVis.put(KEY_VIS_EXAM_FREQ, LIMIT_APP_VIS_EXAM_FREQ);
        appVis.put(KEY_VIS_FRAMES_FREQ, LIMIT_APP_VIS_FRAMES_FREQ);
        appVis.put(KEY_VIS_LENS_FREQ, LIMIT_APP_VIS_LENS_FREQ);
        LIMITS_MAP.put(APX_VISION, appVis);

        // EE Cost Standard
        Map<String, Double> eeCostStd = new HashMap<>();
        eeCostStd.put(KEY_EMP_NAME, LIMIT_EE_COST_STD_EMP_NAME);
        eeCostStd.put(KEY_EMP_STATE, LIMIT_EE_COST_STD_STATE);
        eeCostStd.put(KEY_EMP_TIERS, LIMIT_EE_COST_STD_TIERS);
        eeCostStd.put(KEY_EMP_CURR_PLAN_NAME, LIMIT_EE_COST_STD_CURR_PLAN_NAME);
        eeCostStd.put(KEY_EMP_PROP_PLAN_NAME, LIMIT_EE_COST_STD_PROP_PLAN_NAME);
        eeCostStd.put(KEY_EMP_COST, LIMIT_EE_COST_STD_COST);
        eeCostStd.put(KEY_EMP_COST_DIFF, LIMIT_EE_COST_STD_DIFF);
        LIMITS_MAP.put(EE_COST_STD, eeCostStd);

        // EE Cost Alternate
        Map<String, Double> eeCostAlt = new HashMap<>();
        eeCostAlt.put(KEY_EMP_NAME, LIMIT_EE_COST_ALT_EMP_NAME);
        eeCostAlt.put(KEY_EMP_STATE, LIMIT_EE_COST_ALT_STATE);
        eeCostAlt.put(KEY_EMP_TIERS, LIMIT_EE_COST_ALT_TIERS);
        eeCostAlt.put(KEY_EMP_CURR_PLAN_NAME, LIMIT_EE_COST_ALT_CURR_PLAN_NAME);
        eeCostAlt.put(KEY_EMP_PROP_PLAN_NAME, LIMIT_EE_COST_ALT_PROP_PLAN_NAME);
        eeCostAlt.put(KEY_EMP_COST, LIMIT_EE_COST_ALT_COST);
        eeCostAlt.put(KEY_EMP_COST_DIFF, LIMIT_EE_COST_ALT_DIFF);
        LIMITS_MAP.put(EE_COST_ALT, eeCostAlt);
    }
}
