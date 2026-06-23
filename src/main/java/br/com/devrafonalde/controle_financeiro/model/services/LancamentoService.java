package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.dto.LancamentosDTO;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.CartaoORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.LancamentosORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.PessoaORM;
import br.com.devrafonalde.controle_financeiro.model.repositories.CartaoRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.ContaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.LancamentosRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.PessoaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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

    public LancamentosDTO cadastrar(LancamentosDTO lancamento) {
        validar(lancamento);

        ContaORM conta = contaRepository.findById(lancamento.getConta().getId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));
        ContaORM contaDestino = contaRepository.findById(lancamento.getConta().getId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));
        CartaoORM cartao = cartaoRepository.findById(lancamento.getCartao().getId()).orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado"));
        PessoaORM pessoa = pessoaRepository.findById(lancamento.getPessoa().getId()).orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada"));

        LancamentosORM lancamentoCadastrado = lancamentoRepository.save(LancamentosORM.builder()
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

        return modelMapper.map(lancamentoCadastrado, LancamentosDTO.class);
    }

    public List<LancamentosDTO> listarPorMes(String mesAno) {
        return lancamentoRepository.findByMesAnoOrderByDataAsc(mesAno).stream()
                .map(lancamentosORM -> modelMapper.map(lancamentosORM, LancamentosDTO.class))
                .toList();
    }

    public List<LancamentosDTO> listarPorMesEPessoa(Long pessoaId, String mesAno) {
        PessoaORM pessoa = pessoaRepository.findById(pessoaId).orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada"));
        return lancamentoRepository.findByMesAnoAndPessoa(mesAno, pessoa).stream()
                .map(lancamentosORM -> modelMapper.map(lancamentosORM, LancamentosDTO.class))
                .toList();
    }

    public List<LancamentosDTO> listarPorCartaoEMes(Long cartaoId, String mesAno) {
        CartaoORM cartao = cartaoRepository.findById(cartaoId).orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado"));
        return lancamentoRepository.findByCartaoAndMesAno(cartao, mesAno).stream()
                .map(lancamentosORM -> modelMapper.map(lancamentosORM, LancamentosDTO.class))
                .toList();
    }

    public LancamentosDTO buscarPorId(Long id) {
        return modelMapper.map(lancamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado: " + id)), LancamentosDTO.class);
    }

    public LancamentosDTO atualizar(Long id, LancamentosDTO dados) {
        validar(dados);
        LancamentosORM lancamento = lancamentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado: " + id));

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

        return modelMapper.map(lancamentoRepository.save(lancamento), LancamentosDTO.class);
    }

    public void remover(Long id) {
        lancamentoRepository.deleteById(id);
    }

    private void validar(LancamentosDTO l) {
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