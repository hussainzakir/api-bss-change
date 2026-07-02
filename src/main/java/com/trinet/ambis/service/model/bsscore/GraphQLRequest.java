package com.trinet.ambis.service.model.bsscore;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphQLRequest {
    private String query;
    private Map<String, Object> variables;
}