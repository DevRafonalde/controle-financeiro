package br.com.devrafonalde.controle_financeiro.model.services;

import br.com.devrafonalde.controle_financeiro.model.entities.dto.CategoriaDTO;
import br.com.devrafonalde.controle_financeiro.model.entities.orm.CategoriaORM;
import br.com.devrafonalde.controle_financeiro.model.repositories.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final ModelMapper modelMapper;

    public CategoriaDTO cadastrar(String nome) {
        if (categoriaRepository.existsByNome(nome)) {
            throw new IllegalArgumentException("CategoriaORM já existe: " + nome);
        }
        CategoriaORM categoria = new CategoriaORM();
        categoria.setNome(nome);
        return modelMapper.map(categoriaRepository.save(categoria), CategoriaDTO.class);
    }

    public List<CategoriaORM> listarTodas() {
        return categoriaRepository.findAll(Sort.by("nome"));
    }

    public CategoriaDTO buscarPorId(Long id) {
        return modelMapper.map(categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CategoriaORM não encontrada: " + id)), CategoriaDTO.class);
    }

    public CategoriaDTO atualizar(Long id, String novoNome) {
        CategoriaDTO categoriaExistente = buscarPorId(id);
        if (categoriaExistente.getNome().equals("Pagamento Fatura")) {
            throw new IllegalArgumentException("A categoria \"Pagamento Fatura\" não pode ser removida.");
        }

        if (categoriaRepository.existsByNome(novoNome)) {
            throw new IllegalArgumentException("Já existe uma categoria com esse nome.");
        }
        CategoriaORM categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CategoriaORM não encontrada: " + id));
        categoria.setNome(novoNome);
        return modelMapper.map(categoriaRepository.save(categoria), CategoriaDTO.class);
    }

    public void remover(Long id) {
        CategoriaDTO categoria = buscarPorId(id);
        if (categoria.getNome().equals("Pagamento Fatura")) {
            throw new IllegalArgumentException("A categoria \"Pagamento Fatura\" não pode ser removida.");
        }

        categoriaRepository.deleteById(id);
    }
}