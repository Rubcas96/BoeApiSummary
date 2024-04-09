package com.paellasoft.BoeApiSummary.dto;

import com.paellasoft.BoeApiSummary.entity.Boe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoeDTO  {

    private Long id;

    private String tituloOriginal;

    private String fechaBoe;

    //Metodo para pasar Boe a DTO
    public static BoeDTO fromEntity(Boe boe) {
        BoeDTO boeDTO = new BoeDTO();
        boeDTO.setId(boe.getId());
        boeDTO.setTituloOriginal(boe.gettituloOriginal());
        boeDTO.setFechaBoe(boe.getFechaBoe());
        return boeDTO;
    }
    //Metodo para pasar DTO a Boe
    public static Boe toEntity(BoeDTO boeDTO) {
        Boe boe = new Boe();
        boe.setId(boeDTO.getId());
        boe.settituloOriginal(boeDTO.getTituloOriginal());
        boe.setFechaBoe(boeDTO.getFechaBoe());
        return boe;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTituloOriginal() {
        return tituloOriginal;
    }

    public void setTituloOriginal(String tituloOriginal) {
        this.tituloOriginal = tituloOriginal;
    }

    public String getFechaBoe() {
        return fechaBoe;
    }

    public void setFechaBoe(String fechaBoe) {
        this.fechaBoe = fechaBoe;
    }
}
