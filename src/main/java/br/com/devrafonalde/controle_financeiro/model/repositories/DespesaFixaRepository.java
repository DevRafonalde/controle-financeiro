package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.DespesaFixaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.PessoaORM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DespesaFixaRepository extends JpaRepository<DespesaFixaORM, Long> {
    List<DespesaFixaORM> findByConta(ContaORM conta);
    List<DespesaFixaORM> findByContaTitular(PessoaORM titular);

    @Query("SELECT SUM(d.valor) FROM DespesaFixa d WHERE d.conta = :conta")
    Optional<BigDecimal> sumValorByConta(ContaORM conta);

    @Query("SELECT SUM(d.valor) FROM DespesaFixa d")
    Optional<BigDecimal> sumValorTotal();
}
