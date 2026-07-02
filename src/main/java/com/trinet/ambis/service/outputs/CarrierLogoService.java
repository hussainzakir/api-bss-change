package com.trinet.ambis.service.outputs;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.trinet.ambis.service.dto.CarrierAssetDto;
import com.trinet.ambis.service.dto.CmsLogoDto;

public interface CarrierLogoService {

    <T> T  fetchCarrierLogos(String filePath, TypeReference<T> typeReference, BiFunction<String,TypeReference<T>, Supplier<T>> fileReadMapper);

	CmsLogoDto fetchCarrierLogos();

    Function<String,String> fetchLogoUrl();

    Map<List<String>, String> getCarrierLogoIdMap();

}
