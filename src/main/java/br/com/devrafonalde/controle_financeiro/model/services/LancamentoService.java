package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.dto.LancamentoDTO;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.*;
import br.com.devrafonalde.controle_financeiro.model.events.LancamentoSalvoEvent;
import br.com.devrafonalde.controle_financeiro.model.exceptions.ElementoNaoEncontradoException;
import br.com.devrafonalde.controle_financeiro.model.exceptions.ValidacaoException;
import br.com.devrafonalde.controle_financeiro.model.repositories.*;
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
    private final TipoLancamentoRepository tipoLancamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;

    public LancamentoDTO cadastrar(LancamentoDTO lancamento) {
        validar(lancamento);

        ContaORM conta = contaRepository.findById(lancamento.getConta().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Conta não encontrada"));
        ContaORM contaDestino = contaRepository.findById(lancamento.getConta().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Conta não encontrada"));
        CartaoORM cartao = cartaoRepository.findById(lancamento.getCartao().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Cartão não encontrado"));
        PessoaORM pessoa = pessoaRepository.findById(lancamento.getPessoa().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Pessoa não encontrada"));
        CategoriaORM categoria = categoriaRepository.findById(lancamento.getCategoria().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Categoria não encontrada"));
        TipoLancamentoORM tipoLancamento = tipoLancamentoRepository.findById(lancamento.getTipo().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Tipo de lançamento não encontrado"));

        LancamentoORM lancamentoCadastrado = lancamentoRepository.save(LancamentoORM.builder()
                .conta(conta)
                .data(lancamento.getData())
                .cartao(cartao)
                .categoria(categoria)
                .contaDestino(contaDestino)
                .descricao(lancamento.getDescricao())
                .tipo(tipoLancamento)
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
        PessoaORM pessoa = pessoaRepository.findById(pessoaId).orElseThrow(() -> new ElementoNaoEncontradoException("Pessoa não encontrada"));
        return lancamentoRepository.findByMesAnoAndPessoa(mesAno, pessoa).stream()
                .map(lancamentoORM -> modelMapper.map(lancamentoORM, LancamentoDTO.class))
                .toList();
    }

    public List<LancamentoDTO> listarPorCartaoEMes(Long cartaoId, String mesAno) {
        CartaoORM cartao = cartaoRepository.findById(cartaoId).orElseThrow(() -> new ElementoNaoEncontradoException("Cartão não encontrado"));
        return lancamentoRepository.findByCartaoAndMesAno(cartao, mesAno).stream()
                .map(lancamentoORM -> modelMapper.map(lancamentoORM, LancamentoDTO.class))
                .toList();
    }

    public LancamentoDTO buscarPorId(Long id) {
        return modelMapper.map(lancamentoRepository.findById(id)
                .orElseThrow(() -> new ElementoNaoEncontradoException("Lançamento não encontrado: " + id)), LancamentoDTO.class);
    }

    public LancamentoDTO atualizar(Long id, LancamentoDTO dados) {
        validar(dados);
        LancamentoORM lancamento = lancamentoRepository.findById(id)
                .orElseThrow(() -> new ElementoNaoEncontradoException("Lançamento não encontrado: " + id));
        String mesAnoAnterior = lancamento.getMesAno();


        ContaORM conta = contaRepository.findById(dados.getConta().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Conta não encontrada"));
        ContaORM contaDestino = contaRepository.findById(dados.getConta().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Conta não encontrada"));
        CartaoORM cartao = cartaoRepository.findById(dados.getCartao().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Cartão não encontrado"));
        PessoaORM pessoa = pessoaRepository.findById(dados.getPessoa().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Pessoa não encontrada"));
        CategoriaORM categoria = categoriaRepository.findById(dados.getCategoria().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Categoria não encontrada"));
        TipoLancamentoORM tipoLancamento = tipoLancamentoRepository.findById(dados.getTipo().getId()).orElseThrow(() -> new ElementoNaoEncontradoException("Tipo de lançamento não encontrado"));

        lancamento.setData(dados.getData());
        lancamento.setDescricao(dados.getDescricao());
        lancamento.setCategoria(categoria);
        lancamento.setMesAno(dados.getMesAno());
        lancamento.setValor(dados.getValor());
        lancamento.setTipo(tipoLancamento);
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
                .orElseThrow(() -> new ElementoNaoEncontradoException("Lançamento não encontrado: " + id));
        lancamentoRepository.delete(lancamento);
        // Dispara atualização do mês do lançamento removido
        eventPublisher.publishEvent(new LancamentoSalvoEvent(lancamento));
    }

    private void validar(LancamentoDTO l) {
        String tipo = l.getTipo().getNome();
        switch (tipo) {
            case "DEBITO", "CREDITO" -> {
                if (l.getConta() == null)
                    throw new ValidacaoException("Débito/crédito exige uma conta.");
                if (l.getCartao() != null)
                    throw new ValidacaoException("Débito/crédito não pode ter cartão.");
                if (l.getContaDestino() != null)
                    throw new ValidacaoException("Conta destino é exclusiva de transferências.");
            }
            case "CARTAO" -> {
                if (l.getCartao() == null)
                    throw new ValidacaoException("Lançamento de cartão exige um cartão.");
                if (l.getConta() != null)
                    throw new ValidacaoException("Lançamento de cartão não deve ter conta.");
                if (l.getContaDestino() != null)
                    throw new ValidacaoException("Conta destino é exclusiva de transferências.");
            }
            case "TRANSFERENCIA" -> {
                if (l.getConta() == null || l.getContaDestino() == null)
                    throw new ValidacaoException("Transferência exige conta de origem e destino.");
                if (l.getConta().equals(l.getContaDestino()))
                    throw new ValidacaoException("Origem e destino não podem ser iguais.");
                if (l.getCartao() != null)
                    throw new ValidacaoException("Transferência não pode ter cartão.");
            }
            default -> throw new ValidacaoException("Tipo de lançamento inválido: " + tipo);
        }
    }
}