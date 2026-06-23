package br.com.devrafonalde.controle_financeiro.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "t_despesas_fixas")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DespesaFixaORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "dia_vencimento")
    private Integer diaVencimento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conta_id")
    private ContaORM conta;
}