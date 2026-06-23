package br.com.devrafonalde.controle_financeiro.model.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LancamentoDTO {
    private Long id;
    private LocalDate data;
    private String descricao;
    private CategoriaDTO categoria;
    private String mesAno;
    private BigDecimal valor;
    private TipoLancamentoDTO tipo;
    private ContaDTO conta;
    private ContaDTO contaDestino;
    private CartaoDTO cartao;
    private PessoaDTO pessoa;
    private Integer numParcelas;
    private Integer parcelaAtual;
    private BigDecimal valorTotalCompra;
}