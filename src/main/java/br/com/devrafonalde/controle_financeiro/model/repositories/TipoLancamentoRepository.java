package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.TipoLancamentoORM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoLancamentoRepository extends JpaRepository<TipoLancamentoORM, Long> {
    Optional<TipoLancamentoORM> findByNome(String nome);
    boolean existsByNome(String nome);
}
