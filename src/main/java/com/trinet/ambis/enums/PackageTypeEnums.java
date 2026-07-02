package com.trinet.ambis.enums;

/**
 * @author kpamulapati
 *
 */
public enum PackageTypeEnums {
	
	CON(1,"CON","Conservative"),
	INT(2,"INT","Intermediate"),
	PRM(3,"PRM","Premier");
	
	private final  int type ;
	private  String code;
	private  String name;
	
	PackageTypeEnums(int type,String code,String name) {
		this.type = type;
		this.code = code;
		this.name= name;
	}
	
	public static String getName(String code) {
        for (PackageTypeEnums value : values()) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        throw new IllegalArgumentException("No enum const " + PackageTypeEnums.class + "@code." + code);
    }

	public int getType() {
		return this.type;
	} 
	
	public String getCode(){
		return this.code;
	}
	
	public String getName() {	
		return this.name;
	}

}
