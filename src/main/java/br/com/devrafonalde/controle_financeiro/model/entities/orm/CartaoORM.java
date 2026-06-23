package br.com.devrafonalde.controle_financeiro.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "t_cartoes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartaoORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private BigDecimal limite;

    @Column(name = "dia_fechamento", nullable = false)
    private Integer diaFechamento;

    @Column(name = "dia_vencimento", nullable = false)
    private Integer diaVencimento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "titular_id")
    private PessoaORM titular;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conta_pagamento_id")
    private ContaORM contaPagamento;
}