package com.trinet.ambis.service.model.bsscore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphQLResponse<T> {
    private T data;
}