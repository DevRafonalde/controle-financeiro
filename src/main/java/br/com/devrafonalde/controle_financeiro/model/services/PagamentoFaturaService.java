package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.dto.PagamentoFaturaRequest;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.*;
import br.com.devrafonalde.controle_financeiro.model.events.LancamentoSalvoEvent;
import br.com.devrafonalde.controle_financeiro.model.exceptions.ElementoNaoEncontradoException;
import br.com.devrafonalde.controle_financeiro.model.exceptions.ValidacaoException;
import br.com.devrafonalde.controle_financeiro.model.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoFaturaService {
    private final PagamentoFaturaRepository pagamentoFaturaRepository;
    private final LancamentosRepository lancamentoRepository;
    private final CartaoRepository cartaoRepository;
    private final ContaRepository contaRepository;
    private final CategoriaRepository categoriaRepository;
    private final TipoLancamentoRepository tipoLancamentoRepository;
    private final TipoPagamentoFaturaRepository tipoPagamentoFaturaRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final String CATEGORIA_PAGAMENTO_FATURA = "Pagamento Fatura";

    public PagamentoFaturaORM pagar(PagamentoFaturaRequest request) {
        CartaoORM cartao = cartaoRepository.findById(request.cartaoId())
                .orElseThrow(() -> new ElementoNaoEncontradoException("Cartão não encontrado."));

        ContaORM conta = contaRepository.findById(request.contaId())
                .orElseThrow(() -> new ElementoNaoEncontradoException("Conta não encontrada."));

        TipoPagamentoFaturaORM tipoPagamento = tipoPagamentoFaturaRepository
                .findByNome(request.tipoPagamento())
                .orElseThrow(() -> new ElementoNaoEncontradoException("Tipo de pagamento inválido."));

        BigDecimal valorOriginal = lancamentoRepository
                .calcularFaturaCartao(cartao, request.mesAnoFatura());

        validarPagamento(request, valorOriginal, tipoPagamento.getNome());

        PagamentoFaturaORM pagamento = new PagamentoFaturaORM();
        pagamento.setCartao(cartao);
        pagamento.setMesAnoFatura(request.mesAnoFatura());
        pagamento.setConta(conta);
        pagamento.setTipoPagamento(tipoPagamento);
        pagamento.setValorOriginal(valorOriginal);
        pagamento.setValorPago(request.valorPago());
        pagamento.setObservacao(request.observacao());

        List<LancamentoORM> lancamentosGerados = switch (tipoPagamento.getNome()) {
            case "TOTAL"     -> gerarLancamentoTotal(pagamento, request, cartao);
            case "PARCELADO" -> gerarLancamentosParcelado(pagamento, request, cartao);
            case "PARCIAL"   -> gerarLancamentoParcial(pagamento, request, cartao);
            default -> throw new ValidacaoException("Tipo de pagamento inválido.");
        };

        pagamento.setLancamentos(lancamentosGerados);
        PagamentoFaturaORM salvo = pagamentoFaturaRepository.save(pagamento);

        // Dispara atualização do histórico para cada mês afetado
        lancamentosGerados.forEach(l ->
                eventPublisher.publishEvent(new LancamentoSalvoEvent(l)));

        return salvo;
    }

    private List<LancamentoORM> gerarLancamentoTotal(PagamentoFaturaORM pagamento,
                                                  PagamentoFaturaRequest request,
                                                  CartaoORM cartao) {
        // Mês do vencimento = mês seguinte ao mesAnoFatura
        String mesVencimento = proximoMes(request.mesAnoFatura());

        LancamentoORM lancamento = buildLancamentoDebito(
                pagamento,
                request.valorPago(),
                mesVencimento,
                "Pagamento fatura " + cartao.getNome() + " " + request.mesAnoFatura(),
                1, 1
        );
        return List.of(lancamentoRepository.save(lancamento));
    }

    private List<LancamentoORM> gerarLancamentosParcelado(PagamentoFaturaORM pagamento,
                                                       PagamentoFaturaRequest request,
                                                       CartaoORM cartao) {
        pagamento.setNumParcelas(request.numParcelas());
        pagamento.setValorPrimeiraParcela(request.valorPrimeiraParcela());
        pagamento.setValorDemaisParcelas(request.valorDemaisParcelas());
        pagamento.setValorTotal(
                request.valorPrimeiraParcela()
                        .add(request.valorDemaisParcelas()
                                .multiply(BigDecimal.valueOf(request.numParcelas() - 1)))
        );

        List<LancamentoORM> lancamentos = new ArrayList<>();
        String mesBase = proximoMes(request.mesAnoFatura());  // parcela 1 = mês do vencimento

        for (int i = 1; i <= request.numParcelas(); i++) {
            BigDecimal valorParcela = (i == 1)
                    ? request.valorPrimeiraParcela()
                    : request.valorDemaisParcelas();

            String mesAno = somarMeses(mesBase, i - 1);
            String descricao = String.format("Fatura %s %s — parcela %d/%d",
                    cartao.getNome(), request.mesAnoFatura(), i, request.numParcelas());

            LancamentoORM lancamento = buildLancamentoDebito(
                    pagamento, valorParcela, mesAno, descricao, i, request.numParcelas()
            );
            lancamentos.add(lancamentoRepository.save(lancamento));
        }
        return lancamentos;
    }

    private List<LancamentoORM> gerarLancamentoParcial(PagamentoFaturaORM pagamento,
                                                    PagamentoFaturaRequest request,
                                                    CartaoORM cartao) {
        // ATENÇÃO FRONT: o valor dos juros cobrados pelo banco sobre o saldo restante
        // NÃO está incluso aqui. O usuário deverá lançar a diferença
        // manualmente quando receber a próxima fatura com os juros aplicados.

        String mesVencimento = proximoMes(request.mesAnoFatura());
        LancamentoORM lancamento = buildLancamentoDebito(
                pagamento,
                request.valorPago(),
                mesVencimento,
                "Pagamento parcial fatura " + cartao.getNome() + " " + request.mesAnoFatura(),
                1, 1
        );
        return List.of(lancamentoRepository.save(lancamento));
    }

    private LancamentoORM buildLancamentoDebito(PagamentoFaturaORM pagamento,
                                             BigDecimal valor,
                                             String mesAno,
                                             String descricao,
                                             Integer parcelaAtual,
                                             Integer numParcelas) {
        TipoLancamentoORM tipoDebito = tipoLancamentoRepository.findByNome("DEBITO")
                .orElseThrow(() -> new ElementoNaoEncontradoException("Tipo DEBITO não encontrado."));

        CategoriaORM categoria = categoriaRepository.findByNome(CATEGORIA_PAGAMENTO_FATURA)
                .orElseThrow(() -> new ElementoNaoEncontradoException("Categoria de pagamento não encontrada."));

        LancamentoORM lancamento = new LancamentoORM();
        lancamento.setData(LocalDate.now());
        lancamento.setDescricao(descricao);
        lancamento.setCategoria(categoria);
        lancamento.setMesAno(mesAno);
        lancamento.setValor(valor);
        lancamento.setTipo(tipoDebito);
        lancamento.setConta(pagamento.getConta());
        lancamento.setParcelaAtual(parcelaAtual);
        lancamento.setNumParcelas(numParcelas);
        lancamento.setPagamentoFatura(pagamento);
        return lancamento;
    }

    public BigDecimal calcularSaldoPendente(Long cartaoId, String mesAnoFatura) {
        CartaoORM cartao = cartaoRepository.findById(cartaoId)
                .orElseThrow(() -> new ElementoNaoEncontradoException("Cartão não encontrado."));

        BigDecimal valorFatura = lancamentoRepository
                .calcularFaturaCartao(cartao, mesAnoFatura);

        BigDecimal valorJaPago = pagamentoFaturaRepository
                .sumValorPagoPorFatura(cartao, mesAnoFatura);

        // TODO ATENÇÃO FRONT: o saldo pendente não inclui juros por atraso.
        // TODO Exibir aviso ao usuário de que o valor real cobrado pelo banco pode ser maior.
        return valorFatura.subtract(valorJaPago);
    }

    public BigDecimal calcularLimiteDisponivel(Long cartaoId, String mesAno) {
        CartaoORM cartao = cartaoRepository.findById(cartaoId)
                .orElseThrow(() -> new ElementoNaoEncontradoException("Cartão não encontrado."));

        // Lançamentos CARTAO do mês ainda sem pagamento = comprometem o limite
        BigDecimal faturaAberta = lancamentoRepository
                .calcularFaturaCartao(cartao, mesAno);

        // Parcelas futuras já agendadas (de faturas parceladas) = também comprometem
        BigDecimal parcelasFuturas = lancamentoRepository
                .calcularParcelasFuturasPorCartao(cartao, mesAno);

        return cartao.getLimite()
                .subtract(faturaAberta)
                .subtract(parcelasFuturas);
    }

    private void validarPagamento(PagamentoFaturaRequest request,
                                  BigDecimal valorOriginal,
                                  String tipoPagamento) {
        if (request.valorPago().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacaoException("Valor pago deve ser maior que zero.");
        }
        if (tipoPagamento.equals("PARCELADO")) {
            if (request.numParcelas() == null || request.numParcelas() < 2) {
                throw new ValidacaoException("Parcelado exige ao menos 2 parcelas.");
            }
            if (request.valorPrimeiraParcela() == null || request.valorDemaisParcelas() == null) {
                throw new ValidacaoException("Informe o valor da primeira parcela e das demais.");
            }
        }
    }

    // Helpers de data no formato "yyyy-MM"
    private String proximoMes(String mesAno) {
        return YearMonth.parse(mesAno).plusMonths(1).toString();
    }

    private String somarMeses(String mesAno, int meses) {
        return YearMonth.parse(mesAno).plusMonths(meses).toString();
    }
}