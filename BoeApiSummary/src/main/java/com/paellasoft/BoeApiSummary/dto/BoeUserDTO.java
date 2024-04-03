package com.paellasoft.BoeApiSummary.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BoeUserDTO implements Serializable {
    private Long id;
    private Long userId;
    private Long boeId;
}
