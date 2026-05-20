package com.bolt.tecnhical.challenger.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.bolt.tecnhical.challenger.exception.BusinessException;

class DocumentoUtilTest {

	@Test
	void deveNormalizarCpfComMascara() {
		assertThat(DocumentoUtil.normalizar("529.982.247-25")).isEqualTo("52998224725");
	}

	@Test
	void deveNormalizarCnpjNumericoComMascara() {
		assertThat(DocumentoUtil.normalizar("12.345.678/0001-95")).isEqualTo("12345678000195");
	}

	@Test
	void deveNormalizarCnpjAlfanumericoComMascara() {
		assertThat(DocumentoUtil.normalizar("12.AB.34CD/0001-78")).isEqualTo("12AB34CD000178");
	}

	@Test
	void deveAceitarCnpjAlfanumericoEmMaiusculas() {
		assertThat(DocumentoUtil.normalizar("AB12CD34EF5678")).isEqualTo("AB12CD34EF5678");
	}

	@Test
	void deveConverterLetrasMinusculasParaMaiusculasNoCnpj() {
		assertThat(DocumentoUtil.normalizar("ab12cd34ef5678")).isEqualTo("AB12CD34EF5678");
	}

	@Test
	void deveRejeitarCnpjComLetraNosDigitosVerificadores() {
		assertThatThrownBy(() -> DocumentoUtil.normalizar("AB12CD34EF56AB"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("dígitos verificadores");
	}

	@Test
	void deveRejeitarDocumentoComTamanhoInvalido() {
		assertThatThrownBy(() -> DocumentoUtil.normalizar("123"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("11 caracteres (CPF) ou 14 caracteres (CNPJ)");
	}

	@Test
	void deveRejeitarCpfComTamanhoDiferenteDeOnze() {
		assertThatThrownBy(() -> DocumentoUtil.normalizar("123456789012"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("11 caracteres (CPF) ou 14 caracteres (CNPJ)");
	}

	@Test
	void deveRejeitarCpfComLetras() {
		assertThatThrownBy(() -> DocumentoUtil.normalizar("5299822472A"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("CPF deve conter apenas números");
	}

}
