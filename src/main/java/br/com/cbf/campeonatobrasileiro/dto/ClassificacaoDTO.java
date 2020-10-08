package br.com.cbf.campeonatobrasileiro.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ClassificacaoDTO {

    private List<ClassificacaoTimeDTO> times = new ArrayList<>();
}
