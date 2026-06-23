package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.PessoaORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PessoaRepository extends JpaRepository<PessoaORM, Long> {
    Optional<PessoaORM> findByNome(String nome);
    boolean existsByNome(String nome);
}
