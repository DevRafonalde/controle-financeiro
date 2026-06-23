package br.com.devrafonalde.controle_financeiro.model.entities.dto;

import java.math.BigDecimal;

public record PagamentoFaturaRequest(
        Long cartaoId,
        Long contaId,
        String mesAnoFatura,                // "2026-06"
        String tipoPagamento,               // "TOTAL", "PARCELADO", "PARCIAL"
        BigDecimal valorPago,

        // Apenas para PARCELADO
        Integer numParcelas,
        BigDecimal valorPrimeiraParcela,
        BigDecimal valorDemaisParcelas,

        String observacao
) {}