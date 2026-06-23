package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.TipoContaORM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoContaRepository extends JpaRepository<TipoContaORM, Long> {
    Optional<TipoContaORM> findByNome(String nome);
    boolean existsByNome(String nome);
}
