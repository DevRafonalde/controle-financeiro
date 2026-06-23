package br.com.devrafonalde.controle_financeiro.model.entities;

public enum TipoLancamento {
    DEBITO,         // saída de uma conta
    CREDITO,        // entrada em uma conta
    CARTAO,         // gasto no cartão
    TRANSFERENCIA   // movimentação entre contas
}