package com.trinet.ambis.enums;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum IndustryType {
    
    //Ambrose
    FS("financialServices", Arrays.asList("CA", "HI", "NV", "NY", "PR")),
    TM("techMedia", Arrays.asList("CA", "HI", "NV", "NY", "PR")),
	BS("businessServices", Arrays.asList("CA", "HI", "NV", "NY", "PR")),
	
	//SOI (Old, not used anymore)
	AT("agricultureTransportationOther", Arrays.asList("AZ","CA","CO","FL","GA","MA","NC","SC","NJ","NY","CT","TX")),
	CN("construction", Arrays.asList("AZ","CA","CO","FL","GA","MA","NC","SC","NJ","NY","CT","TX")),
	DG("durableNondurableGoods", Arrays.asList("AZ","CA","CO","FL","GA","IL","MA","MO","NC","SC","NJ","NY","CT","OK","PA","TX")),
//	EH("educationHealth", Arrays.asList("AZ","CA","CO","FL","GA","IL","MA","NC","SC","NJ","NY","CT","TX")),
	FB("financeProfessionalBusiness", Arrays.asList("AZ","CA","CO","FL","GA","IL","MA","MO","NC","SC","NJ","NY","CT","NM","TN","TX")),
//	LH("leisureHospitality", Arrays.asList("AZ","CA","CO","FL","GA","MA","NC","SC","NJ","NY","CT","TX")),
//	RW("retailWholesale", Arrays.asList("AZ","CA","CO","FL","GA","IL","MA","MO","NC","SC","NJ","NY","CT","TN","TX")),
    
	//SOI
	EX("Exchange11industry", Arrays.asList("NY")),

	//Testing
    HS("hospitality", Arrays.asList("")),
    AG("agriculture", Arrays.asList("")),
    TX("textiles", Arrays.asList("")),
	
	//Passport
	/** AMBIS-4537
    21.22.23, 31 - 33, 42, 44-45, 48-49 - ManufWhlSlRetWhtr
    51 - InfoServ
    52, 53 - FinInsRlEst
    54 - ProfSciTechServ
    55, 56, 61,62, 71, 72, 81, 92 - AdminOthBusSrv
	 */
    
    MF("ManufWhlSlRetWhtr", US.allStates),
    IS("InfoServ", US.allStates),
    FR("FinInsRlEst", US.allStates),
    FN("Finance", US.allStates),
    DR("Durable", US.allStates),
    PT("ProfSciTechServ", US.allStates),
    AO("AdminOthBusSrv", US.allStates),
    
    AM("AgricultureMiningDurableGoods", US.allStates),
    EH("EducationHealth", US.allStates),
    FP("FinanceProfessionalBusiness", US.allStates),
    LH("LeisureHospitality", US.allStates),
    RW("RetailWholesale", US.allStates),
    TC("TransportationConstructionRepairOther", US.allStates),
    
    //Test templates
    PS("passport", Arrays.asList("CA", "FL", "ID", "MN", "NC","OT"));
    
    public static final Set<IndustryType> IndustryTypePassportSet = EnumSet.of(
                                                                                    IndustryType.AM,
                                                                                    IndustryType.EH,
                                                                                    IndustryType.FP,
                                                                                    IndustryType.LH,
                                                                                    IndustryType.RW,
                                                                                    IndustryType.TC
                                                                                    );
    
    //For testing only
    public static final Set<IndustryType> IndustryTypePassportTest = EnumSet.of(IndustryType.RW);

	private String type;
	private List<String> states;

	private IndustryType(String type, List<String> states) {
		this.type = type;
		this.setStates(states);
	}
	
	public static final Set<IndustryType> IndustryTypeAmbroseSet = EnumSet.of(IndustryType.FS,
	                                                                              IndustryType.TM,
	                                                                              IndustryType.BS);
	
	public static final Set<IndustryType> IndustryTypeSOISet = EnumSet.of(IndustryType.AT,
                                                                              IndustryType.CN,
                                                                              IndustryType.DG,
                                                                              IndustryType.EH,
                                                                              IndustryType.FB,
                                                                              IndustryType.LH,
                                                                              IndustryType.RW);
	
	public static final Set<IndustryType> IndustryTypeSOInoMedSet = EnumSet.of(IndustryType.EX);
	
	public String getType() {
		return type;
	}
	
	public List<String> getStates() {
        return states;
    }

    public void setStates(List<String> states) {
        this.states = states;
    }
    
    public static String getNameFromType(String type) {
        for (IndustryType industryType : IndustryType.values()) {
            if (industryType.getType()
                    .equals(type)) {
                return industryType.name();
            }
        }
        return null;
    }
}