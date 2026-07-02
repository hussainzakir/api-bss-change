package com.trinet.ambis.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrokerNotificationDto {

    private String exchange;
    private String quarter;
    private String internalOpenDate;
}