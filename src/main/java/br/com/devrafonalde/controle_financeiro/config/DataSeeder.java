package br.com.devrafonalde.controle_financeiro.config;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.CategoriaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.TipoContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.TipoLancamentoORM;
import br.com.devrafonalde.controle_financeiro.model.repositories.CategoriaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.TipoContaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.TipoLancamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {
    private final TipoContaRepository tipoContaRepository;
    private final TipoLancamentoRepository tipoLancamentoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedTiposConta();
        seedTiposLancamento();
        seedCategorias();
    }

    private void seedTiposConta() {
        List.of("CORRENTE", "POUPANCA", "INVESTIMENTO")
                .forEach(nome -> {
                    if (!tipoContaRepository.existsByNome(nome)) {
                        TipoContaORM tipo = new TipoContaORM();
                        tipo.setNome(nome);
                        tipoContaRepository.save(tipo);
                    }
                });
    }

    private void seedTiposLancamento() {
        List.of("DEBITO", "CREDITO", "CARTAO", "TRANSFERENCIA")
                .forEach(nome -> {
                    if (!tipoLancamentoRepository.existsByNome(nome)) {
                        TipoLancamentoORM tipo = new TipoLancamentoORM();
                        tipo.setNome(nome);
                        tipoLancamentoRepository.save(tipo);
                    }
                });
    }

    private void seedCategorias() {
        List.of(
                "Alimentação", "Transporte", "Saúde", "Educação",
                "Lazer", "Vestuário", "Mercado", "Moradia",
                "Tecnologia", "Investimento", "Receita", "Outros"
        ).forEach(nome -> {
            if (!categoriaRepository.existsByNome(nome)) {
                CategoriaORM categoria = new CategoriaORM();
                categoria.setNome(nome);
                categoriaRepository.save(categoria);
            }
        });
    }
}