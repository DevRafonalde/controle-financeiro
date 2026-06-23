package br.com.devrafonalde.controle_financeiro.model.listener;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.HistoricoSaldoORM;
import br.com.devrafonalde.controle_financeiro.model.events.LancamentoSalvoEvent;
import br.com.devrafonalde.controle_financeiro.model.repositories.ContaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.DespesaFixaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.HistoricoSaldoRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.LancamentosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HistoricoSaldoListener {
    private final HistoricoSaldoRepository historicoSaldoRepository;
    private final LancamentosRepository lancamentoRepository;
    private final DespesaFixaRepository despesaFixaRepository;
    private final ContaRepository contaRepository;

    @Async("historicoExecutor")
    @EventListener
    @Transactional
    public void onLancamentoSaved(LancamentoSalvoEvent event) {
        String mesAnoInicial = event.lancamento().getMesAno();
        List<ContaORM> contas = contaRepository.findAll();

        // Busca todos os meses no histórico a partir do mês do lançamento, em ordem
        List<String> mesesAfetados = historicoSaldoRepository
                .findMesesAnoAPartirDe(mesAnoInicial);

        // Garante que o mês do lançamento esteja na lista
        if (!mesesAfetados.contains(mesAnoInicial)) {
            mesesAfetados = new ArrayList<>(mesesAfetados);
            mesesAfetados.addFirst(mesAnoInicial);
        }

        for (String mesAno : mesesAfetados) {
            for (ContaORM conta : contas) {
                recalcularEPersistir(conta, mesAno);
            }
        }
    }

    private void recalcularEPersistir(ContaORM conta, String mesAno) {
        // Saldo inicial: saldoFinal do mês anterior no histórico,
        // ou saldoInicial da conta se for o primeiro mês
        BigDecimal saldoInicial = historicoSaldoRepository
                .findMesAnterior(conta, mesAno)
                .map(HistoricoSaldoORM::getSaldoFinal)
                .orElse(conta.getSaldoInicial());

        // Movimentação do mês: créditos - débitos - transferências saindo
        //                      + transferências chegando
        BigDecimal movimentacao = lancamentoRepository
                .calcularMovimentacaoNaMesAno(conta, mesAno);

        // Fixas debitadas desta conta
        BigDecimal totalFixas = despesaFixaRepository
                .sumValorByConta(conta)
                .orElse(BigDecimal.ZERO);

        BigDecimal saldoFinal = saldoInicial
                .add(movimentacao)
                .subtract(totalFixas);

        HistoricoSaldoORM historico = historicoSaldoRepository
                .findByContaAndMesAno(conta, mesAno)
                .orElse(new HistoricoSaldoORM());

        historico.setConta(conta);
        historico.setMesAno(mesAno);
        historico.setSaldoInicial(saldoInicial);
        historico.setSaldoFinal(saldoFinal);

        historicoSaldoRepository.save(historico);
    }
}