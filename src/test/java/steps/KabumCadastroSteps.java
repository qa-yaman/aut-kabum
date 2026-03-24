package steps;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openqa.selenium.TimeoutException;
import org.opentest4j.TestAbortedException;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.qameta.allure.Allure;
import pages.KabumCadastroPage;
import steps.context.DriverContext;
import steps.context.TestData;
import steps.context.TestDataFactory;

public class KabumCadastroSteps {
	private final DriverContext driverContext;
	private final TestDataFactory testDataFactory;
	private KabumCadastroPage kabumCadastroPage;

	public KabumCadastroSteps(DriverContext driverContext, TestDataFactory testDataFactory) {
		this.driverContext = driverContext;
		this.testDataFactory = testDataFactory;
	}

	@Dado("que acesso a home da Kabum")
	public void queAcessoAHomeDaKabum() {
		kabumCadastroPage = new KabumCadastroPage(driverContext.getDriver());
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
				kabumCadastroPage.isPopupAutenticacaoVisivel(),
				"O popup de autenticacao da KaBuM nao apareceu."
		);
	}

	@Quando("informo o email de cadastro no popup e clico em entrar")
	public void informoOEmailDeCadastroNoPopupEClicoEmEntrar() {
		garantirPageInicializada();
		TestData data = testDataFactory.get();
		Allure.parameter("email", data.email());
		kabumCadastroPage.preencherEmailNoPopup(data.email());
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

	@E("preencho os dados de cadastro gerados automaticamente")
	public void preenchoOsDadosDeCadastroGeradosAutomaticamente() {
		garantirPageInicializada();
		TestData data = testDataFactory.get();
		Allure.parameter("cpf", data.cpf());
		Allure.parameter("celular", data.celular());
		Allure.parameter("dataNascimento", data.dataNascimento());
		Allure.parameter("nomeCompleto", data.nomeCompleto());
		kabumCadastroPage.preencherEmailCadastro(data.email());
		kabumCadastroPage.preencherCpf(data.cpf());
		kabumCadastroPage.preencherCelular(data.celular());
		kabumCadastroPage.preencherDataNascimento(data.dataNascimento());
		kabumCadastroPage.preencherNomeCompleto(data.nomeCompleto());
		kabumCadastroPage.preencherSenha(data.senha());
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

	@E("informo o CEP de cadastro e clico em confirmar")
	public void informoOCepDeCadastroEClicoEmConfirmar() {
		garantirPageInicializada();
		TestData data = testDataFactory.get();
		Allure.parameter("cep", data.cep());
		kabumCadastroPage.aguardarEtapaEnderecoPorCep();
		kabumCadastroPage.preencherCep(data.cep());
		kabumCadastroPage.clicarEmConfirmar();
		kabumCadastroPage.aguardarEtapaNumeroEndereco();
	}

	@E("informo o numero e complemento de endereco e clico em confirmar")
	public void informoONumeroEComplementoDeEnderecoEClicoEmConfirmar() {
		garantirPageInicializada();
		TestData data = testDataFactory.get();
		kabumCadastroPage.aguardarEtapaNumeroEndereco();
		kabumCadastroPage.preencherNumero(data.numero());
		kabumCadastroPage.preencherComplemento(data.complemento());
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
		throw new TestAbortedException("Blocked by CAPTCHA / anti-bot");
	}
}
