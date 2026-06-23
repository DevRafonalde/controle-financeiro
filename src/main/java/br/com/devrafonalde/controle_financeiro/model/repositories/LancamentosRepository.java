package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.TipoLancamento;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.CartaoORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.LancamentosORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.PessoaORM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface LancamentosRepository extends JpaRepository<LancamentosORM, Long> {
    List<LancamentosORM> findByMesAnoOrderByDataAsc(String mesAno);
    List<LancamentosORM> findByMesAnoAndPessoa(String mesAno, PessoaORM pessoa);
    List<LancamentosORM> findByMesAnoAndTipo(String mesAno, TipoLancamento tipo);
    List<LancamentosORM> findByCartaoAndMesAno(CartaoORM cartao, String mesAno);
    List<LancamentosORM> findByContaAndMesAno(ContaORM conta, String mesAno);

    // Saldo dinâmico de uma conta em um mês:
    // créditos - débitos - transferências saindo + transferências chegando
    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN l.tipo = 'CREDITO' AND l.conta = :conta THEN l.valor
                WHEN l.tipo = 'DEBITO'  AND l.conta = :conta THEN -l.valor
                WHEN l.tipo = 'TRANSFERENCIA' AND l.conta        = :conta THEN -l.valor
                WHEN l.tipo = 'TRANSFERENCIA' AND l.contaDestino = :conta THEN  l.valor
                ELSE 0
            END
        ), 0)
        FROM Lancamento l
        WHERE l.mesAno = :mesAno
    """)
    BigDecimal calcularMovimentacaoNaMesAno(ContaORM conta, String mesAno);

    // Total da fatura de um cartão em um mês
    @Query("""
        SELECT COALESCE(SUM(l.valor), 0)
        FROM Lancamento l
        WHERE l.cartao = :cartao
        AND l.mesAno = :mesAno
        AND l.tipo = 'CARTAO'
    """)
    BigDecimal calcularFaturaCartao(CartaoORM cartao, String mesAno);

    // Total gasto por pessoa em um mês (débitos + cartão)
    @Query("""
        SELECT COALESCE(SUM(l.valor), 0)
        FROM Lancamento l
        WHERE l.pessoa = :pessoa
        AND l.mesAno = :mesAno
        AND l.tipo IN ('DEBITO', 'CARTAO')
    """)
    BigDecimal calcularTotalGastoPorPessoa(PessoaORM pessoa, String mesAno);

    // Total gasto por categoria em um mês
    @Query("""
        SELECT l.categoria, COALESCE(SUM(l.valor), 0)
        FROM Lancamento l
        WHERE l.mesAno = :mesAno
        AND l.tipo IN ('DEBITO', 'CARTAO')
        GROUP BY l.categoria
        ORDER BY SUM(l.valor) DESC
    """)
    List<Object[]> calcularTotalPorCategoria(String mesAno);

    // Receitas totais de um mês
    @Query("""
        SELECT COALESCE(SUM(l.valor), 0)
        FROM Lancamento l
        WHERE l.mesAno = :mesAno
        AND l.tipo = 'CREDITO'
    """)
    BigDecimal calcularReceitasTotais(String mesAno);

    // Despesas totais de um mês (sem transferências)
    @Query("""
        SELECT COALESCE(SUM(l.valor), 0)
        FROM Lancamento l
        WHERE l.mesAno = :mesAno
        AND l.tipo IN ('DEBITO', 'CARTAO')
    """)
    BigDecimal calcularDespesasTotais(String mesAno);
}
