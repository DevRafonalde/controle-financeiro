package br.com.devrafonalde.controle_financeiro.model.entities.dto;

import br.com.devrafonalde.controle_financeiro.model.entities.TipoConta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContaDTO {
    private Long id;
    private String nome;
    private String banco;
    private TipoConta tipo;
    private PessoaDTO titular;
}