package com.paellasoft.BoeApiSummary.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class BoeDTO implements Serializable {
    private Long id;
    private String tituloOriginal;
    private String contenidoResumido;
    private String fechaBoe;
    private List<Long> subscriptionsIds;
}
