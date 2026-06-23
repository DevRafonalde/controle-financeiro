package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.PessoaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.TipoContaORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContaRepository extends JpaRepository<ContaORM, Long> {
    List<ContaORM> findByTitular(PessoaORM titular);
    List<ContaORM> findByTipo(TipoContaORM tipo);
    boolean existsByNomeAndTitular(String nome, PessoaORM titular);
}
