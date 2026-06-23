package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.CategoriaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.HistoricoSaldoORM;
import br.com.devrafonalde.controle_financeiro.model.repositories.ContaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.HistoricoSaldoRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.LancamentosRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ResumoService {
    private final LancamentosRepository lancamentoRepository;
    private final ContaService contaService;
    private final DespesaFixaService despesaFixaService;
    private final HistoricoSaldoRepository historicoSaldoRepository;
    private final ContaRepository contaRepository;
    private final ModelMapper modelMapper;

    public ResumoMensal calcularResumo(String mesAno) {
        YearMonth mesAnoYM = YearMonth.parse(mesAno,
                DateTimeFormatter.ofPattern("MMM/yyyy", Locale.of("pt", "BR")));
        boolean isMesAtual = mesAnoYM.equals(YearMonth.now());

        BigDecimal totalReceitas = lancamentoRepository.calcularReceitasTotais(mesAno);
        BigDecimal totalDespesas = lancamentoRepository.calcularDespesasTotais(mesAno);
        BigDecimal totalFixos    = despesaFixaService.calcularTotalFixos();
        BigDecimal saldo         = totalReceitas.subtract(totalDespesas).subtract(totalFixos);

        List<SaldoConta> saldosPorConta = contaRepository.findAll().stream()
                .map(conta -> {
                    BigDecimal saldoAtual = isMesAtual
                            ? contaService.calcularSaldoAtual(conta.getId())
                            : historicoSaldoRepository
                            .findByContaAndMesAno(conta, mesAno)
                            .map(HistoricoSaldoORM::getSaldoFinal)
                            .orElse(BigDecimal.ZERO);
                    return new SaldoConta(conta, saldoAtual);
                })
                .toList();

        // Inicializa histórico do mês se for a primeira consulta
        if (!isMesAtual) {
            inicializarHistoricoSeNecessario(mesAno);
        }

        List<GastoPorCategoria> gastosPorCategoria = lancamentoRepository
                .calcularTotalPorCategoria(mesAno)
                .stream()
                .map(row -> new GastoPorCategoria((CategoriaORM) row[0], (BigDecimal) row[1]))
                .toList();

        return new ResumoMensal(
                mesAno,
                totalReceitas,
                totalDespesas,
                totalFixos,
                saldo,
                saldosPorConta,
                gastosPorCategoria
        );
    }

    public List<GastoPorCategoria> calcularGastosPorCategoria(String mesAno) {
        return lancamentoRepository
                .calcularTotalPorCategoria(mesAno)
                .stream()
                .map(row -> new GastoPorCategoria((CategoriaORM) row[0], (BigDecimal) row[1]))
                .toList();
    }

    private void inicializarHistoricoSeNecessario(String mesAno) {
        contaRepository.findAll().forEach(conta -> {
            if (historicoSaldoRepository.existsByContaAndMesAno(conta, mesAno)) return;

            BigDecimal saldoInicial = historicoSaldoRepository
                    .findMesAnterior(conta, mesAno)
                    .map(HistoricoSaldoORM::getSaldoFinal)
                    .orElse(conta.getSaldoInicial());

            BigDecimal movimentacao = lancamentoRepository
                    .calcularMovimentacaoNaMesAno(conta, mesAno);

            BigDecimal totalFixas = despesaFixaService
                    .calcularTotalFixosPorConta(conta.getId());

            HistoricoSaldoORM historico = new HistoricoSaldoORM();
            historico.setConta(conta);
            historico.setMesAno(mesAno);
            historico.setSaldoInicial(saldoInicial);
            historico.setSaldoFinal(saldoInicial.add(movimentacao).subtract(totalFixas));
            historicoSaldoRepository.save(historico);
        });
    }

    public record ResumoMensal(
            String mesAno,
            BigDecimal totalReceitas,
            BigDecimal totalDespesas,
            BigDecimal totalFixos,
            BigDecimal saldo,
            List<SaldoConta> saldosPorConta,
            List<GastoPorCategoria> gastosPorCategoria
    ) {}

    public record SaldoConta(ContaORM conta, BigDecimal saldoAtual) {}

    public record GastoPorCategoria(CategoriaORM categoria, BigDecimal total) {}
}