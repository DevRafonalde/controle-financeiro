package br.com.devrafonalde.controle_financeiro.model.entities.orm;

import br.com.devrafonalde.controle_financeiro.model.entities.TipoConta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "t_contas")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContaORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String banco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoConta tipo;

    @Column(name = "saldo_inicial", nullable = false)
    private BigDecimal saldoInicial;

    @ManyToOne(optional = false)
    @JoinColumn(name = "titular_id")
    private PessoaORM titular;
}