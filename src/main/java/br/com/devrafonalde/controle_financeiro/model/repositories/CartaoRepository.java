package br.com.devrafonalde.controle_financeiro.model.repositories;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.CartaoORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.PessoaORM;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartaoRepository extends JpaRepository<CartaoORM, Long> {
    List<CartaoORM> findByTitular(PessoaORM titular);
    List<CartaoORM> findByContaPagamento(ContaORM conta);
    boolean existsByNomeAndTitular(String nome, PessoaORM titular);
}
