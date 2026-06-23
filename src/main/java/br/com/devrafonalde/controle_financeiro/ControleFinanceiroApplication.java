package br.com.devrafonalde.controle_financeiro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ControleFinanceiroApplication {
	static void main(String[] args) {
		SpringApplication.run(ControleFinanceiroApplication.class, args);
	}

	// TODO Verificar a questão de fechar o mês
	// TODO Transformar todos os ENUMS em tabela
	// TODO Criar tabela de categorias
	// TODO Criar método de pagar fatura (se parcelado, informar quantas parcelas e valor de cada uma) que já cria o lançamento e libera o limite pro mês seguinte
	// TODO Limite do cartão de um mês é condicionado ao pagamento da fatura do mês anterior
}
