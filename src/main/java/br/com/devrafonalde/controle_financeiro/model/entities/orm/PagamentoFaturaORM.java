package br.com.devrafonalde.controle_financeiro.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_pagamentos_fatura")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagamentoFaturaORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cartao_id")
    private CartaoORM cartao;

    @Column(name = "mes_ano_fatura", nullable = false)
    private String mesAnoFatura;            // fatura de origem ex: "2026-06"

    @ManyToOne(optional = false)
    @JoinColumn(name = "conta_id")
    private ContaORM conta;                    // conta escolhida pelo usuário para débito

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_pagamento_id")
    private TipoPagamentoFaturaORM tipoPagamento;

    @Column(name = "valor_original", nullable = false)
    private BigDecimal valorOriginal;       // soma dos lançamentos CARTAO do mês

    @Column(name = "valor_pago", nullable = false)
    private BigDecimal valorPago;           // o que o usuário realmente pagou

    // Preenchidos apenas quando tipoPagamento = PARCELADO
    @Column(name = "num_parcelas")
    private Integer numParcelas;

    @Column(name = "valor_primeira_parcela")
    private BigDecimal valorPrimeiraParcela;    // é a "entrada" — pode diferir das demais

    @Column(name = "valor_demais_parcelas")
    private BigDecimal valorDemaisParcelas;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;          // valorPrimeiraParcela + (numParcelas-1) * valorDemaisParcelas

    @Column(name = "observacao")
    private String observacao;

    // Lançamentos DEBITO gerados automaticamente pelo pagamento
    @OneToMany(mappedBy = "pagamentoFatura", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LancamentoORM> lancamentos = new ArrayList<>();
}
