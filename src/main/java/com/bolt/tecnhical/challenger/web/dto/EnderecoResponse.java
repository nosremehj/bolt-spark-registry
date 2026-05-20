package com.bolt.tecnhical.challenger.web.dto;

import com.bolt.tecnhical.challenger.domain.Endereco;

public record EnderecoResponse(
		String cep,
		String logradouro,
		String complemento,
		String unidade,
		String bairro,
		String cidade,
		String uf,
		String estado,
		String regiao,
		String ibge,
		String gia,
		String ddd,
		String siafi) {

	private static final String SEM_COMPLEMENTO = "Sem complemento";
	private static final String SEM_UNIDADE = "Sem unidade";
	private static final String SEM_ESTADO = "Sem estado";
	private static final String SEM_REGIAO = "Sem região";
	private static final String SEM_IBGE = "Sem IBGE";
	private static final String SEM_GIA = "Sem GIA";
	private static final String SEM_DDD = "Sem DDD";
	private static final String SEM_SIAFI = "Sem SIAFI";

	public static EnderecoResponse from(Endereco endereco) {
		return new EnderecoResponse(
				endereco.getCep(),
				endereco.getLogradouro(),
				formatarComplemento(endereco.getComplemento()),
				formatarCampo(endereco.getUnidade(), SEM_UNIDADE),
				endereco.getBairro(),
				endereco.getCidade(),
				endereco.getUf(),
				formatarCampo(endereco.getEstado(), SEM_ESTADO),
				formatarCampo(endereco.getRegiao(), SEM_REGIAO),
				formatarCampo(endereco.getIbge(), SEM_IBGE),
				formatarCampo(endereco.getGia(), SEM_GIA),
				formatarCampo(endereco.getDdd(), SEM_DDD),
				formatarCampo(endereco.getSiafi(), SEM_SIAFI));
	}

	private static String formatarComplemento(String complemento) {
		return formatarCampo(complemento, SEM_COMPLEMENTO);
	}

	private static String formatarCampo(String valor, String textoQuandoVazio) {
		if (valor == null || valor.isBlank()) {
			return textoQuandoVazio;
		}
		return valor;
	}

}
