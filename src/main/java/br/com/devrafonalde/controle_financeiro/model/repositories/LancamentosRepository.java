package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface LancamentosRepository extends JpaRepository<LancamentoORM, Long> {
    List<LancamentoORM> findByMesAnoOrderByDataAsc(String mesAno);
    List<LancamentoORM> findByMesAnoAndPessoa(String mesAno, PessoaORM pessoa);
    List<LancamentoORM> findByMesAnoAndTipo(String mesAno, TipoLancamentoORM tipo);
    List<LancamentoORM> findByCartaoAndMesAno(CartaoORM cartao, String mesAno);
    List<LancamentoORM> findByContaAndMesAno(ContaORM conta, String mesAno);

    // Saldo dinâmico de uma conta em um mês:
    // créditos - débitos - transferências saindo + transferências chegando
    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN l.tipo.nome = 'CREDITO' AND l.conta = :conta THEN l.valor
                WHEN l.tipo.nome = 'DEBITO'  AND l.conta = :conta THEN -l.valor
                WHEN l.tipo.nome = 'TRANSFERENCIA' AND l.conta        = :conta THEN -l.valor
                WHEN l.tipo.nome = 'TRANSFERENCIA' AND l.contaDestino = :conta THEN  l.valor
                ELSE 0
            END
        ), 0)
        FROM LancamentoORM l
        WHERE l.mesAno = :mesAno
    """)
    BigDecimal calcularMovimentacaoNaMesAno(ContaORM conta, String mesAno);

    // Total da fatura de um cartão em um mês
    @Query("""
        SELECT COALESCE(SUM(l.valor), 0)
        FROM LancamentoORM l
        WHERE l.cartao = :cartao
        AND l.mesAno = :mesAno
        AND l.tipo.nome = 'CARTAO'
    """)
    BigDecimal calcularFaturaCartao(CartaoORM cartao, String mesAno);

    // Total gasto por pessoa em um mês (débitos + cartão)
    @Query("""
        SELECT COALESCE(SUM(l.valor), 0)
        FROM LancamentoORM l
        WHERE l.pessoa = :pessoa
        AND l.mesAno = :mesAno
        AND l.tipo.nome IN ('DEBITO', 'CARTAO')
    """)
    BigDecimal calcularTotalGastoPorPessoa(PessoaORM pessoa, String mesAno);

    // Total gasto por categoria em um mês
    @Query("""
        SELECT l.categoria, COALESCE(SUM(l.valor), 0)
        FROM LancamentoORM l
        WHERE l.mesAno = :mesAno
        AND l.tipo.nome IN ('DEBITO', 'CARTAO')
        GROUP BY l.categoria
        ORDER BY SUM(l.valor) DESC
    """)
    List<Object[]> calcularTotalPorCategoria(String mesAno);

    // Receitas totais de um mês
    @Query("""
        SELECT COALESCE(SUM(l.valor), 0)
        FROM LancamentoORM l
        WHERE l.mesAno = :mesAno
        AND l.tipo.nome = 'CREDITO'
    """)
    BigDecimal calcularReceitasTotais(String mesAno);

    // Despesas totais de um mês (sem transferências)
    @Query("""
        SELECT COALESCE(SUM(l.valor), 0)
        FROM LancamentoORM l
        WHERE l.mesAno = :mesAno
        AND l.tipo.nome IN ('DEBITO', 'CARTAO')
    """)
    BigDecimal calcularDespesasTotais(String mesAno);

    // Soma de lançamentos DEBITO gerados por pagamento de fatura
    // com mesAno >= mesAno atual, para um cartão específico
    // Representa o limite já comprometido por parcelamentos futuros
        @Query("""
        SELECT COALESCE(SUM(l.valor), 0)
        FROM LancamentoORM l
        WHERE l.pagamentoFatura.cartao = :cartao
        AND l.mesAno > :mesAno
        AND l.tipo.nome = 'DEBITO'
    """)
    BigDecimal calcularParcelasFuturasPorCartao(CartaoORM cartao, String mesAno);
}
