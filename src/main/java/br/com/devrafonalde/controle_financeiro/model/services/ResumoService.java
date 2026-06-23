package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.HistoricoSaldoORM;
import br.com.devrafonalde.controle_financeiro.model.repositories.ContaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.HistoricoSaldoRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.LancamentosRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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
        BigDecimal totalReceitas  = lancamentoRepository.calcularReceitasTotais(mesAno);
        BigDecimal totalDespesas  = lancamentoRepository.calcularDespesasTotais(mesAno);
        BigDecimal totalFixos     = despesaFixaService.calcularTotalFixos();
        BigDecimal saldo          = totalReceitas.subtract(totalDespesas).subtract(totalFixos);

        List<SaldoConta> saldosPorConta = contaRepository.findAll().stream()
                .map(conta -> {
                    BigDecimal saldoAtual = contaService.calcularSaldoAtual(conta.getId());
                    return new SaldoConta(conta, saldoAtual);
                })
                .toList();

        List<GastoPorCategoria> gastosPorCategoria = lancamentoRepository
                .calcularTotalPorCategoria(mesAno)
                .stream()
                .map(row -> new GastoPorCategoria((String) row[0], (BigDecimal) row[1]))
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

    public void fecharMes(String mesAno) {
        contaRepository.findAll().forEach(conta -> {
            if (historicoSaldoRepository.existsByContaAndMesAno(conta, mesAno)) return;

            BigDecimal saldoFinal = contaService.calcularSaldoAtual(conta.getId());
            BigDecimal movimentacao = lancamentoRepository.calcularMovimentacaoNaMesAno(conta, mesAno);
            BigDecimal saldoInicial = saldoFinal.subtract(movimentacao);

            HistoricoSaldoORM historico = new HistoricoSaldoORM();
            historico.setConta(conta);
            historico.setMesAno(mesAno);
            historico.setSaldoInicial(saldoInicial);
            historico.setSaldoFinal(saldoFinal);
            historicoSaldoRepository.save(historico);
        });
    }

    // Records para compor o retorno do resumo
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

    public record GastoPorCategoria(String categoria, BigDecimal total) {}
}