package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.HistoricoSaldoORM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HistoricoSaldoRepository extends JpaRepository<HistoricoSaldoORM, Long> {
    Optional<HistoricoSaldoORM> findByContaAndMesAno(ContaORM conta, String mesAno);
    List<HistoricoSaldoORM> findByMesAnoOrderByContaIdAsc(String mesAno);
    List<HistoricoSaldoORM> findByContaOrderByMesAnoAsc(ContaORM conta);
    boolean existsByContaAndMesAno(ContaORM conta, String mesAno);

    // Todos os meses distintos no histórico a partir de mesAno, em ordem cronológica
    @Query("""
        SELECT DISTINCT h.mesAno
        FROM HistoricoSaldo h
        WHERE h.mesAno >= :mesAno
        ORDER BY h.mesAno ASC
    """)
    List<String> findMesesAnoAPartirDe(String mesAno);

    // Mês imediatamente anterior no histórico para uma conta
    @Query("""
        SELECT h FROM HistoricoSaldo h
        WHERE h.conta = :conta
        AND h.mesAno < :mesAno
        ORDER BY h.mesAno DESC
        LIMIT 1
    """)
    Optional<HistoricoSaldoORM> findMesAnterior(ContaORM conta, String mesAno);
}
