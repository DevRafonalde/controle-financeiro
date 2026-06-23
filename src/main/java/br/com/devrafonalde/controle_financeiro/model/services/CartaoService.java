package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.dto.CartaoDTO;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.CartaoORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.PessoaORM;
import br.com.devrafonalde.controle_financeiro.model.repositories.CartaoRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.ContaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.LancamentosRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.PessoaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartaoService {
    private final CartaoRepository cartaoRepository;
    private final PessoaRepository pessoaRepository;
    private final LancamentosRepository lancamentoRepository;
    private final ContaRepository contaRepository;
    private final ModelMapper modelMapper;

    public CartaoDTO cadastrar(CartaoDTO cartao) {
        PessoaORM titular = pessoaRepository.findById(cartao.getTitular().getId()).orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada."));

        if (cartaoRepository.existsByNomeAndTitular(cartao.getNome(), titular)) {
            throw new IllegalArgumentException("Esse titular já possui um cartão com esse nome.");
        }

        ContaORM conta = contaRepository.findById(cartao.getContaPagamento().getId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada."));

        CartaoORM cartaoCadastrado = cartaoRepository.save(CartaoORM.builder()
                .nome(cartao.getNome())
                .limite(cartao.getLimite())
                .diaFechamento(cartao.getDiaFechamento())
                .diaVencimento(cartao.getDiaVencimento())
                .contaPagamento(conta)
                .titular(titular)
                .build()
        );
        return modelMapper.map(cartaoCadastrado, CartaoDTO.class);
    }

    public List<CartaoDTO> listarTodos() {
        return cartaoRepository.findAll().stream()
                .map(cartaoORM -> modelMapper.map(cartaoORM, CartaoDTO.class))
                .toList();
    }

    public List<CartaoDTO> listarPorTitular(Long pessoaId) {
        PessoaORM titular = pessoaRepository.findById(pessoaId).orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada"));
        return cartaoRepository.findByTitular(titular).stream()
                .map(cartaoORM -> modelMapper.map(cartaoORM, CartaoDTO.class))
                .toList();
    }

    public CartaoDTO buscarPorId(Long id) {
        return modelMapper.map(cartaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado: " + id)), CartaoDTO.class);
    }

    public CartaoDTO atualizar(Long id, CartaoDTO dados) {
        ContaORM conta = contaRepository.findById(dados.getContaPagamento().getId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada."));

        CartaoORM cartao = cartaoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado: " + id));
        cartao.setNome(dados.getNome());
        cartao.setLimite(dados.getLimite());
        cartao.setDiaFechamento(dados.getDiaFechamento());
        cartao.setDiaVencimento(dados.getDiaVencimento());
        cartao.setContaPagamento(conta);

        return modelMapper.map(cartaoRepository.save(cartao), CartaoDTO.class);
    }

    public void remover(Long id) {
        CartaoDTO cartao = buscarPorId(id);
        cartaoRepository.deleteById(cartao.getId());
    }

    public BigDecimal calcularFatura(Long cartaoId, String mesAno) {
        CartaoORM cartao = cartaoRepository.findById(cartaoId).orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado: " + cartaoId));
        return lancamentoRepository.calcularFaturaCartao(cartao, mesAno);
    }

    public BigDecimal calcularLimiteDisponivel(Long cartaoId, String mesAno) {
        CartaoORM cartao = cartaoRepository.findById(cartaoId).orElseThrow(() -> new EntityNotFoundException("Cartão não encontrado: " + cartaoId));
        BigDecimal fatura = lancamentoRepository.calcularFaturaCartao(cartao, mesAno);
        return cartao.getLimite().subtract(fatura);
    }
}