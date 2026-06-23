package br.com.devrafonalde.controle_financeiro.model.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DespesaFixaDTO {
    private Long id;
    private String nome;
    private BigDecimal valor;
    private Integer diaVencimento;
    private ContaDTO conta;
}