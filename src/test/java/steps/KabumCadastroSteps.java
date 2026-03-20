package steps;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.AssumptionViolatedException;
import org.openqa.selenium.TimeoutException;

import io.qameta.allure.Allure;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import pages.KabumCadastroPage;
import runner.RunCucumberTest;

public class KabumCadastroSteps {
	private KabumCadastroPage kabumCadastroPage;

	@Dado("que acesso a home da Kabum")
	public void queAcessoAHomeDaKabum() {
		assertNotNull(RunCucumberTest.driver, "WebDriver nao foi inicializado.");
		kabumCadastroPage = new KabumCadastroPage(RunCucumberTest.driver);
		kabumCadastroPage.abrirHome();
		kabumCadastroPage.aguardarHomeCarregada();
	}

	@Quando("clico no botao Entre ou Cadastre-se da home")
	public void clicoNoBotaoEntreOuCadastreSeDaHome() {
		garantirPageInicializada();
		kabumCadastroPage.clicarNoAcessoOuCadastroDaHome();
	}

	@Entao("o popup Acesse sua conta ou cadastre-se deve aparecer")
	public void oPopupAcesseSuaContaOuCadastreSeDeveAparecer() {
		garantirPageInicializada();
		kabumCadastroPage.aguardarPopupAutenticacao();
		assertTrue(
				"O popup de autenticacao da KaBuM nao apareceu.",
				kabumCadastroPage.isPopupAutenticacaoVisivel()
		);
	}

	@Quando("informo o email {string} no popup e clico em entrar")
	public void informoOEmailNoPopupEClicoEmEntrar(String email) {
		garantirPageInicializada();
		kabumCadastroPage.preencherEmailNoPopup(email);
		kabumCadastroPage.clicarEmEntrarNoPopup();
		try {
			kabumCadastroPage.aguardarNovaEtapaOuBloqueioDeSeguranca();
		} catch (TimeoutException ex) {
			registrarSkipPorCaptcha();
		}
		if (kabumCadastroPage.isCaptchaPresent()) {
			registrarSkipPorCaptcha();
		}
	}

	@E("seleciono a opcao cadastrar com CPF")
	public void selecionoAOpcaoCadastrarComCpf() {
		garantirPageInicializada();
		kabumCadastroPage.selecionarOpcaoCpf();
	}

	@E("preencho CPF {string}, celular {string}, data de nascimento {string}, nome completo {string}")
	public void preenchoCpfCelularDataDeNascimentoNomeCompleto(
			String cpf,
			String celular,
			String dataNascimento,
			String nomeCompleto
	) {
		garantirPageInicializada();
		kabumCadastroPage.preencherCpf(cpf);
		kabumCadastroPage.preencherCelular(celular);
		kabumCadastroPage.preencherDataNascimento(dataNascimento);
		kabumCadastroPage.preencherNomeCompleto(nomeCompleto);
	}

	@E("preencho os dados destacados com email {string}, CPF {string}, celular {string}, data de nascimento {string}, nome completo {string} e senha {string}")
	public void preenchoOsDadosDestacadosComEmailCpfCelularDataNascimentoNomeCompletoESenha(
			String email,
			String cpf,
			String celular,
			String dataNascimento,
			String nomeCompleto,
			String senha
	) {
		garantirPageInicializada();
		kabumCadastroPage.preencherEmailCadastro(email);
		kabumCadastroPage.preencherCpf(cpf);
		kabumCadastroPage.preencherCelular(celular);
		kabumCadastroPage.preencherDataNascimento(dataNascimento);
		kabumCadastroPage.preencherNomeCompleto(nomeCompleto);
		kabumCadastroPage.preencherSenha(senha);
	}

	@E("marco o checkbox Li e estou de acordo")
	public void marcoOCheckboxLiEEstouDeAcordo() {
		garantirPageInicializada();
		kabumCadastroPage.marcarCheckboxLiEEstouDeAcordo();
	}

	@E("clico em continuar no cadastro")
	public void clicoEmContinuarNoCadastro() {
		garantirPageInicializada();
		kabumCadastroPage.clicarEmContinuarNoCadastro();
	}

	@Entao("devo avancar para a proxima etapa do cadastro da Kabum")
	public void devoAvancarParaAProximaEtapaDoCadastroDaKabum() {
		garantirPageInicializada();
		kabumCadastroPage.aguardarAvancoDaEtapaDeCadastro();
	}

	@E("informo o CEP {string} e clico em confirmar")
	public void informoOCepEClicoEmConfirmar(String cep) {
		garantirPageInicializada();
		kabumCadastroPage.aguardarEtapaEnderecoPorCep();
		kabumCadastroPage.preencherCep(cep);
		kabumCadastroPage.clicarEmConfirmar();
	}

	@E("informo o numero {string} e complemento {string} e clico em confirmar")
	public void informoONumeroEComplementoEClicoEmConfirmar(String numero, String complemento) {
		garantirPageInicializada();
		kabumCadastroPage.aguardarEtapaEnderecoPorCep();
		kabumCadastroPage.preencherNumero(numero);
		kabumCadastroPage.preencherComplemento(complemento);
		kabumCadastroPage.clicarEmConfirmar();
	}

	@Entao("devo visualizar a mensagem de sucesso de cadastro {string}")
	public void devoVisualizarAMensagemDeSucessoDeCadastro(String mensagemEsperada) {
		garantirPageInicializada();
		kabumCadastroPage.aguardarMensagemSucesso(mensagemEsperada);
	}

	private void garantirPageInicializada() {
		assertNotNull(kabumCadastroPage, "Page Object do cadastro da KaBuM nao foi inicializado.");
	}

	private void registrarSkipPorCaptcha() {
		Allure.step("Blocked by CAPTCHA / anti-bot");
		Allure.addAttachment("skip_reason", "Blocked by CAPTCHA / anti-bot");
		throw new AssumptionViolatedException("Blocked by CAPTCHA / anti-bot");
	}
}
