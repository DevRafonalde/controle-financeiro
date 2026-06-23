package br.com.devrafonalde.controle_financeiro.model.events;

import br.com.devrafonalde.controle_financeiro.model.entities.orm.LancamentoORM;

public record LancamentoSalvoEvent(LancamentoORM lancamento) {}