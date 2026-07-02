package com.trinet.ambis.service.impl.outputs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.service.dto.CarrierAssetDto;
import com.trinet.ambis.service.dto.CmsLogoDto;
import com.trinet.ambis.service.outputs.CarrierLogoService;
import com.trinet.ambis.util.FileUtils;
import com.trinet.ambis.util.RestApiClient;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;

@Service
@Log4j2
public class CarrierLogoServiceImpl implements CarrierLogoService {

    @Autowired
    private RestApiClient restApiClient;

    @Autowired
    private FileUtils fileUtils;
    
    @Value("${logosUrl}")
    private String cmsLogosUrl;
    
    @Value("${cmsLogosId}")
    private String cmsLogosId;
    
    @Value("${carrier.logo.file}")
    private String logoFile;

    private Map<List<String>, String> carrierLogoIdMap;
    
    @PostConstruct
    public void prepareLogoDetails(){
        CmsLogoDto cmsLogoDto = fetchCarrierLogos();
        CarrierAssetDto carrierAssetDto = fetchCarrierLogos(logoFile, new TypeReference<CarrierAssetDto>() {
        }, fileUtils::readJsonData);

        carrierLogoIdMap = new HashMap<>();
        Map<String, List<String>> carrierUidMap = carrierAssetDto.getAssestDetails().stream()
                .distinct()
                .collect(Collectors.toMap(CarrierAssetDto.AssestDetails::getUid, CarrierAssetDto.AssestDetails::getCarrierNames));
        if(!CollectionUtils.isEmpty(carrierUidMap) && ObjectUtils.isNotEmpty(cmsLogoDto)){
            carrierUidMap.forEach((key, val) -> {
                Optional<CmsLogoDto.LogoDto> logoDetails = cmsLogoDto.getAssets().stream().filter(cms -> cms.getUid().equals(key))
                        .findAny();
                if (ObjectUtils.anyNotNull(logoDetails) && logoDetails.isPresent()) {
                    carrierLogoIdMap.put(val,logoDetails.get().getUrl());
                }
            });
        }
    }

    @Override
    public CmsLogoDto fetchCarrierLogos() {
        log.info("Fetching the carrier logos..{}", cmsLogosUrl);
        return restApiClient.fetchCarrierLogos(cmsLogosUrl, cmsLogosId);
    }

    @Override
    public <T> T fetchCarrierLogos(String filePath, TypeReference<T> typeReference, BiFunction<String,TypeReference<T>, Supplier<T>> fileReadMapper) {
        log.info("Fetching the carrier logos from file..{}", filePath);
        return fileReadMapper.apply(filePath,typeReference).get();
    }

    @Override
    public Function<String,String> fetchLogoUrl(){
        return carrierName -> {
            Optional<Map.Entry<List<String>, String>> carrierLogo = getCarrierLogoIdMap().entrySet().stream().filter(entry -> entry.getKey().contains(carrierName)).findAny();
            return carrierLogo.map(Map.Entry::getValue).orElse(null);
        };
    }

    @Override
    public Map<List<String>, String> getCarrierLogoIdMap(){
        return carrierLogoIdMap;
    }

}
