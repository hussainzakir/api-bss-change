package com.trinet.ambis.service.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsLogoDto implements Serializable {

   
	private static final long serialVersionUID = 1L;
	private List<LogoDto> assets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LogoDto implements Serializable{
     
		private static final long serialVersionUID = 1L;
		private String uid;
        private String filename;
        private String url;
        @JsonProperty("_metadata")
        private MetaData metadata;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaData implements Serializable{
     
		private static final long serialVersionUID = 1L;
		private Extensions extensions;
        
    }
    
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Extensions implements Serializable{
     
		private static final long serialVersionUID = 1L;
        @JsonProperty("bltc0363329561f2b3f")
		private List<CarrierDetails> carrierDetailsList;
        
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CarrierDetails implements Serializable{
     
		private static final long serialVersionUID = 1L;
        @JsonProperty("carrierNames")
		private List<String> carrierNames;
        
    }
}
