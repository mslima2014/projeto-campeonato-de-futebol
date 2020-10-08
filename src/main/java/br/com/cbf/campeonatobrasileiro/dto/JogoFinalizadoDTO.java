package br.com.cbf.campeonatobrasileiro.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JogoFinalizadoDTO {

    private Integer golsTime1;
    private Integer golsTime2;
    private Integer publicoPagante;

}
