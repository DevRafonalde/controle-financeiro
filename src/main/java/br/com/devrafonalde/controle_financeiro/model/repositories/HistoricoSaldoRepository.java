package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.HistoricoSaldoORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistoricoSaldoRepository extends JpaRepository<HistoricoSaldoORM, Long> {
    Optional<HistoricoSaldoORM> findByContaAndMesAno(ContaORM conta, String mesAno);
    List<HistoricoSaldoORM> findByMesAnoOrderByContaIdAsc(String mesAno);
    List<HistoricoSaldoORM> findByContaOrderByMesAnoAsc(ContaORM conta);
    boolean existsByContaAndMesAno(ContaORM conta, String mesAno);
}
