package br.com.devrafonalde.controle_financeiro.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "t_historico_saldo")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoricoSaldoORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mes_ano", nullable = false)
    private String mesAno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conta_id")
    private ContaORM conta;

    @Column(name = "saldo_inicial", nullable = false)
    private BigDecimal saldoInicial;

    @Column(name = "saldo_final", nullable = false)
    private BigDecimal saldoFinal;
}