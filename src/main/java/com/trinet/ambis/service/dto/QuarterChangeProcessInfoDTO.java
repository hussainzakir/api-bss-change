package com.trinet.ambis.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuarterChangeProcessInfoDTO {

    private String messageSeq;

    private String oldQuaterId;

    private String newQuaterId;
}
