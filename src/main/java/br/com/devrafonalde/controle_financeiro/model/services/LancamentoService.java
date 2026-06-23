package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.dto.LancamentoDTO;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.CartaoORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.LancamentoORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.PessoaORM;
import br.com.devrafonalde.controle_financeiro.model.events.LancamentoSalvoEvent;
import br.com.devrafonalde.controle_financeiro.model.repositories.CartaoRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.ContaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.LancamentosRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.PessoaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LancamentoService {
    private final LancamentosRepository lancamentoRepository;
    private final ContaRepository contaRepository;
    private final CartaoRepository cartaoRepository;
    private final PessoaRepository pessoaRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public LancamentoDTO cadastrar(LancamentoDTO lancamento) {
        validar(lancamento);

        ContaORM conta = contaRepository.findById(lancamento.getConta().getId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));
        ContaORM contaDestino = contaRepository.findById(lancamento.getConta().getId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));
        CartaoORM cartao = cartaoRepository.findById(lancamento.getCartao().getId()).orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado"));
        PessoaORM pessoa = pessoaRepository.findById(lancamento.getPessoa().getId()).orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada"));

        LancamentoORM lancamentoCadastrado = lancamentoRepository.save(LancamentoORM.builder()
                .conta(conta)
                .data(lancamento.getData())
                .cartao(cartao)
                .categoria(lancamento.getCategoria())
                .contaDestino(contaDestino)
                .descricao(lancamento.getDescricao())
                .tipo(lancamento.getTipo())
                .mesAno(lancamento.getMesAno())
                .numParcelas(lancamento.getNumParcelas())
                .parcelaAtual(lancamento.getParcelaAtual())
                .valorTotalCompra(lancamento.getValorTotalCompra())
                .pessoa(pessoa)
                .build()
        );

        eventPublisher.publishEvent(new LancamentoSalvoEvent(lancamentoCadastrado));

        return modelMapper.map(lancamentoCadastrado, LancamentoDTO.class);
    }

    public List<LancamentoDTO> listarPorMes(String mesAno) {
        return lancamentoRepository.findByMesAnoOrderByDataAsc(mesAno).stream()
                .map(lancamentoORM -> modelMapper.map(lancamentoORM, LancamentoDTO.class))
                .toList();
    }

    public List<LancamentoDTO> listarPorMesEPessoa(Long pessoaId, String mesAno) {
        PessoaORM pessoa = pessoaRepository.findById(pessoaId).orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada"));
        return lancamentoRepository.findByMesAnoAndPessoa(mesAno, pessoa).stream()
                .map(lancamentoORM -> modelMapper.map(lancamentoORM, LancamentoDTO.class))
                .toList();
    }

    public List<LancamentoDTO> listarPorCartaoEMes(Long cartaoId, String mesAno) {
        CartaoORM cartao = cartaoRepository.findById(cartaoId).orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado"));
        return lancamentoRepository.findByCartaoAndMesAno(cartao, mesAno).stream()
                .map(lancamentoORM -> modelMapper.map(lancamentoORM, LancamentoDTO.class))
                .toList();
    }

    public LancamentoDTO buscarPorId(Long id) {
        return modelMapper.map(lancamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado: " + id)), LancamentoDTO.class);
    }

    public LancamentoDTO atualizar(Long id, LancamentoDTO dados) {
        validar(dados);
        LancamentoORM lancamento = lancamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado: " + id));
        String mesAnoAnterior = lancamento.getMesAno();


        ContaORM conta = contaRepository.findById(dados.getConta().getId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));
        ContaORM contaDestino = contaRepository.findById(dados.getConta().getId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));
        CartaoORM cartao = cartaoRepository.findById(dados.getCartao().getId()).orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado"));
        PessoaORM pessoa = pessoaRepository.findById(dados.getPessoa().getId()).orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada"));

        lancamento.setData(dados.getData());
        lancamento.setDescricao(dados.getDescricao());
        lancamento.setCategoria(dados.getCategoria());
        lancamento.setMesAno(dados.getMesAno());
        lancamento.setValor(dados.getValor());
        lancamento.setTipo(dados.getTipo());
        lancamento.setConta(conta);
        lancamento.setContaDestino(contaDestino);
        lancamento.setCartao(cartao);
        lancamento.setPessoa(pessoa);
        lancamento.setNumParcelas(dados.getNumParcelas());
        lancamento.setParcelaAtual(dados.getParcelaAtual());
        lancamento.setValorTotalCompra(dados.getValorTotalCompra());

        LancamentoORM salvo = lancamentoRepository.save(lancamento);

        // Se o mês mudou, precisa disparar atualização do mês anterior também
        if (!mesAnoAnterior.equals(salvo.getMesAno())) {
            lancamento.setMesAno(mesAnoAnterior);
            eventPublisher.publishEvent(new LancamentoSalvoEvent(lancamento));
        }

        eventPublisher.publishEvent(new LancamentoSalvoEvent(salvo));

        return modelMapper.map(salvo, LancamentoDTO.class);
    }

    public void remover(Long id) {
        LancamentoORM lancamento = lancamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado: " + id));
        lancamentoRepository.delete(lancamento);
        // Dispara atualização do mês do lançamento removido
        eventPublisher.publishEvent(new LancamentoSalvoEvent(lancamento));
    }

    private void validar(LancamentoDTO l) {
        switch (l.getTipo()) {
            case DEBITO, CREDITO -> {
                if (l.getConta() == null)
                    throw new IllegalArgumentException("Lançamentos de débito/crédito exigem uma conta.");
                if (l.getCartao() != null)
                    throw new IllegalArgumentException("Lançamentos de débito/crédito não podem ter cartão.");
                if (l.getContaDestino() != null)
                    throw new IllegalArgumentException("Conta destino é exclusiva de transferências.");
            }
            case CARTAO -> {
                if (l.getCartao() == null)
                    throw new IllegalArgumentException("Lançamentos de cartão exigem um cartão.");
                if (l.getConta() != null)
                    throw new IllegalArgumentException("Lançamentos de cartão não devem ter conta.");
                if (l.getContaDestino() != null)
                    throw new IllegalArgumentException("Conta destino é exclusiva de transferências.");
            }
            case TRANSFERENCIA -> {
                if (l.getConta() == null || l.getContaDestino() == null)
                    throw new IllegalArgumentException("Transferências exigem conta de origem e destino.");
                if (l.getConta().equals(l.getContaDestino()))
                    throw new IllegalArgumentException("Conta de origem e destino não podem ser iguais.");
                if (l.getCartao() != null)
                    throw new IllegalArgumentException("Transferências não podem ter cartão.");
            }
        }
    }
}