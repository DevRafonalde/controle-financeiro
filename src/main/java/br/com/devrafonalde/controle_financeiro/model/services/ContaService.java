package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.dto.ContaDTO;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.LancamentosORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.PessoaORM;
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
public class ContaService {
    private final ContaRepository contaRepository;
    private final PessoaRepository pessoaRepository;
    private final LancamentosRepository lancamentoRepository;
    private final ModelMapper modelMapper;

    public ContaDTO cadastrar(ContaDTO conta) {
        PessoaORM titular = pessoaRepository.findById(conta.getTitular().getId()).orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada"));

        if (contaRepository.existsByNomeAndTitular(conta.getNome(), titular)) {
            throw new IllegalArgumentException("Essa pessoa já possui uma conta com esse nome.");
        }

        ContaORM contaCadastrada = contaRepository.save(ContaORM.builder()
                .tipo(conta.getTipo())
                .banco(conta.getBanco())
                .nome(conta.getNome())
                .saldoInicial(BigDecimal.ZERO)
                .titular(titular)
                .build()
        );

        return modelMapper.map(contaCadastrada, ContaDTO.class);
    }

    public List<ContaDTO> listarTodas() {
        return contaRepository.findAll().stream()
                .map(contaORM -> modelMapper.map(contaORM, ContaDTO.class))
                .toList();
    }

    public List<ContaDTO> listarPorTitular(Long pessoaId) {
        PessoaORM titular = pessoaRepository.findById(pessoaId).orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada"));
        return contaRepository.findByTitular(titular).stream()
                .map(contaORM -> modelMapper.map(contaORM, ContaDTO.class))
                .toList();
    }

    public ContaDTO buscarPorId(Long id) {
        return modelMapper.map(contaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ContaORM não encontrada: " + id)), ContaDTO.class);
    }

    public ContaDTO atualizar(Long id, ContaDTO dados) {
        ContaORM conta = contaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));
        conta.setNome(dados.getNome());
        conta.setBanco(dados.getBanco());
        conta.setTipo(dados.getTipo());
        return modelMapper.map(contaRepository.save(conta), ContaDTO.class);
    }

    public void remover(Long id) {
        ContaDTO conta = buscarPorId(id);
        contaRepository.deleteById(conta.getId());
    }

    public BigDecimal calcularSaldoAtual(Long contaId) {
        ContaORM conta = contaRepository.findById(contaId).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));
        BigDecimal movimentacaoTotal = lancamentoRepository
                .findAll()
                .stream()
                .map(l -> calcularImpactoNaConta(l, conta))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return conta.getSaldoInicial().add(movimentacaoTotal);
    }

    private BigDecimal calcularImpactoNaConta(LancamentosORM lancamento, ContaORM conta) {
        return switch (lancamento.getTipo()) {
            case CREDITO -> lancamento.getConta().equals(conta)
                    ? lancamento.getValor()
                    : BigDecimal.ZERO;
            case DEBITO -> lancamento.getConta().equals(conta)
                    ? lancamento.getValor().negate()
                    : BigDecimal.ZERO;
            case TRANSFERENCIA -> {
                if (lancamento.getConta().equals(conta))
                    yield lancamento.getValor().negate();
                if (lancamento.getContaDestino().equals(conta))
                    yield lancamento.getValor();
                yield BigDecimal.ZERO;
            }
            case CARTAO -> BigDecimal.ZERO;
        };
    }
}
