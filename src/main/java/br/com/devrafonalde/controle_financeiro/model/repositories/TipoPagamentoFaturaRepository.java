package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.TipoPagamentoFaturaORM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoPagamentoFaturaRepository extends JpaRepository<TipoPagamentoFaturaORM, Long> {
    Optional<TipoPagamentoFaturaORM> findByNome(String nome);
    boolean existsByNome(String nome);
}
