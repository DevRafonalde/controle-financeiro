package br.com.devrafonalde.controle_financeiro.model.entities.orm;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_tipos_conta")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TipoContaORM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;                // "CORRENTE", "POUPANCA", "INVESTIMENTO"
}