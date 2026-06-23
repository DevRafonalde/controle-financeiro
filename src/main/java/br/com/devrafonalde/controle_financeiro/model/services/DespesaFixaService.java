package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.dto.DespesaFixaDTO;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.ContaORM;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.DespesaFixaORM;
import br.com.devrafonalde.controle_financeiro.model.repositories.ContaRepository;
import br.com.devrafonalde.controle_financeiro.model.repositories.DespesaFixaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DespesaFixaService {
    private final DespesaFixaRepository despesaFixaRepository;
    private final ContaRepository contaRepository;
    private final ModelMapper modelMapper;

    public DespesaFixaDTO cadastrar(DespesaFixaDTO despesaFixa) {
        ContaORM conta = contaRepository.findById(despesaFixa.getConta().getId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

        DespesaFixaORM despesaCadastrada = despesaFixaRepository.save(DespesaFixaORM.builder()
                .valor(despesaFixa.getValor())
                .nome(despesaFixa.getNome())
                .diaVencimento(despesaFixa.getDiaVencimento())
                .conta(conta)
                .build()
        );
        return modelMapper.map(despesaCadastrada, DespesaFixaDTO.class);
    }

    public List<DespesaFixaDTO> listarTodas() {
        return despesaFixaRepository.findAll().stream()
                .map(despesaFixaORM -> modelMapper.map(despesaFixaORM, DespesaFixaDTO.class))
                .toList();
    }

    public List<DespesaFixaDTO> listarPorConta(Long contaId) {
        ContaORM conta = contaRepository.findById(contaId).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));
        return despesaFixaRepository.findByConta(conta).stream()
                .map(despesaFixaORM -> modelMapper.map(despesaFixaORM, DespesaFixaDTO.class))
                .toList();
    }

    public DespesaFixaDTO buscarPorId(Long id) {
        return modelMapper.map(despesaFixaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despesa fixa não encontrada: " + id)), DespesaFixaDTO.class);
    }

    public DespesaFixaDTO atualizar(Long id, DespesaFixaDTO dados) {
        DespesaFixaORM despesa = despesaFixaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Despesa fixa não encontrada: " + id));
        ContaORM conta = contaRepository.findById(dados.getConta().getId()).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));

        despesa.setNome(dados.getNome());
        despesa.setValor(dados.getValor());
        despesa.setDiaVencimento(dados.getDiaVencimento());
        despesa.setConta(conta);
        return modelMapper.map(despesaFixaRepository.save(despesa), DespesaFixaDTO.class);
    }

    public void remover(Long id) {
        despesaFixaRepository.deleteById(id);
    }

    public BigDecimal calcularTotalFixosPorConta(Long contaId) {
        ContaORM conta = contaRepository.findById(contaId).orElseThrow(() -> new EntityNotFoundException("Conta não encontrada"));
        return despesaFixaRepository.sumValorByConta(conta)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal calcularTotalFixos() {
        return despesaFixaRepository.sumValorTotal()
                .orElse(BigDecimal.ZERO);
    }
}
