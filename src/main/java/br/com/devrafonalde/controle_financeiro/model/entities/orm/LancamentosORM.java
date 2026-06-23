package br.com.devrafonalde.controle_financeiro.model.entities.orm;

import br.com.devrafonalde.controle_financeiro.model.entities.TipoLancamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "t_lancamentos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LancamentosORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private String categoria;

    @Column(name = "mes_ano", nullable = false)
    private String mesAno;

    @Column(nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLancamento tipo;

    @ManyToOne
    @JoinColumn(name = "conta_id")
    private ContaORM conta;                    // null quando tipo = CARTAO

    @ManyToOne
    @JoinColumn(name = "conta_destino_id")
    private ContaORM contaDestino;             // preenchido apenas quando tipo = TRANSFERENCIA

    @ManyToOne
    @JoinColumn(name = "cartao_id")
    private CartaoORM cartao;                  // preenchido apenas quando tipo = CARTAO

    @ManyToOne
    @JoinColumn(name = "pessoa_id")
    private PessoaORM pessoa;                  // quem realizou — pode ser qualquer pessoa

    @Column(name = "num_parcelas")
    private Integer numParcelas;

    @Column(name = "parcela_atual")
    private Integer parcelaAtual;

    @Column(name = "valor_total_compra")
    private BigDecimal valorTotalCompra;
}