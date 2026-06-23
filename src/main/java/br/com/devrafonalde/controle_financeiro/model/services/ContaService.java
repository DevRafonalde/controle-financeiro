package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.dto.ContaDTO;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.*;
import br.com.devrafonalde.controle_financeiro.model.repositories.ContaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.LancamentosRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.PessoaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.TipoContaRepository;
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
    private final TipoContaRepository tipoContaRepository;
    private final ModelMapper modelMapper;

    public ContaDTO cadastrar(ContaDTO conta) {
        PessoaORM titular = pessoaRepository.findById(conta.getTitular().getId()).orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada"));

        if (contaRepository.existsByNomeAndTitular(conta.getNome(), titular)) {
            throw new IllegalArgumentException("Essa pessoa já possui uma conta com esse nome.");
        }
        TipoContaORM tipoConta = tipoContaRepository.findById(conta.getTipo().getId()).orElseThrow(() -> new EntityNotFoundException("Tipo de conta não encontrado"));

        ContaORM contaCadastrada = contaRepository.save(ContaORM.builder()
                .tipo(tipoConta)
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
        TipoContaORM tipoConta = tipoContaRepository.findById(conta.getTipo().getId()).orElseThrow(() -> new EntityNotFoundException("Tipo de conta não encontrado"));
        conta.setNome(dados.getNome());
        conta.setBanco(dados.getBanco());
        conta.setTipo(tipoConta);
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

    private BigDecimal calcularImpactoNaConta(LancamentoORM lancamento, ContaORM conta) {
        String tipo = lancamento.getTipo().getNome();
        return switch (tipo) {
            case "CREDITO" -> conta.equals(lancamento.getConta())
                    ? lancamento.getValor()
                    : BigDecimal.ZERO;
            case "DEBITO" -> conta.equals(lancamento.getConta())
                    ? lancamento.getValor().negate()
                    : BigDecimal.ZERO;
            case "TRANSFERENCIA" -> {
                if (lancamento.getConta() != null && lancamento.getConta().equals(conta))
                    yield lancamento.getValor().negate();
                if (lancamento.getContaDestino() != null && lancamento.getContaDestino().equals(conta))
                    yield lancamento.getValor();
                yield BigDecimal.ZERO;
            }
            case "CARTAO" -> BigDecimal.ZERO;
            default -> throw new IllegalStateException("Tipo de lançamento desconhecido: " + tipo);
        };
    }
}
