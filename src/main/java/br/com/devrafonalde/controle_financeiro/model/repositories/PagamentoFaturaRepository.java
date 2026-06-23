package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.CartaoORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.PagamentoFaturaORM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PagamentoFaturaRepository extends JpaRepository<PagamentoFaturaORM, Long> {

    List<PagamentoFaturaORM> findByCartaoAndMesAnoFatura(CartaoORM cartao, String mesAnoFatura);

    boolean existsByCartaoAndMesAnoFatura(CartaoORM cartao, String mesAnoFatura);

    // Soma do que já foi pago de uma fatura (pode haver pagamentos parciais)
    @Query("""
        SELECT COALESCE(SUM(p.valorPago), 0)
        FROM PagamentoFatura p
        WHERE p.cartao = :cartao
        AND p.mesAnoFatura = :mesAnoFatura
    """)
    BigDecimal sumValorPagoPorFatura(CartaoORM cartao, String mesAnoFatura);
}