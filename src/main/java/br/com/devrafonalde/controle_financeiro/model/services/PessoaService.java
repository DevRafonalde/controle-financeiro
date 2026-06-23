package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.dto.PessoaDTO;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.PessoaORM;
import br.com.devrafonalde.controle_financeiro.model.repositories.PessoaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PessoaService {
    private final PessoaRepository pessoaRepository;
    private final ModelMapper modelMapper;

    public PessoaDTO cadastrar(PessoaORM pessoa) {
        if (pessoaRepository.existsByNome(pessoa.getNome())) {
            throw new IllegalArgumentException("Já existe uma pessoa com o nome: " + pessoa.getNome());
        }
        return modelMapper.map(pessoaRepository.save(pessoa), PessoaDTO.class);
    }

    public List<PessoaDTO> listarTodas() {
        return pessoaRepository.findAll().stream()
                .map(pessoaORM -> modelMapper.map(pessoaORM, PessoaDTO.class))
                .toList();
    }

    public PessoaDTO buscarPorId(Long id) {
        return modelMapper.map(pessoaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada: " + id)), PessoaDTO.class);
    }

    public PessoaDTO atualizar(Long id, PessoaDTO dados) {
        PessoaORM pessoa = pessoaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada: " + id));
        pessoa.setNome(dados.getNome());
        return modelMapper.map(pessoaRepository.save(pessoa), PessoaDTO.class);
    }

    public void remover(Long id) {
        PessoaDTO pessoa = buscarPorId(id);
        pessoaRepository.deleteById(pessoa.getId());
    }
}