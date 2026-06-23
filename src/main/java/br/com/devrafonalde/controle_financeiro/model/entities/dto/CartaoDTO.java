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
public class CartaoDTO {
    private Long id;
    private String nome;
    private BigDecimal limite;
    private Integer diaFechamento;
    private Integer diaVencimento;
    private PessoaDTO titular;
    private ContaDTO contaPagamento;
}