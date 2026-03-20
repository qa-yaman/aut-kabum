package steps;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import pages.KabumBuscaPage;
import runner.RunCucumberTest;

public class KabumBuscaSteps {

	private KabumBuscaPage kabumBuscaPage;

	@Dado("que acesso a home da Kabum para realizar uma busca")
	public void queAcessoAHomeDaKabumParaRealizarUmaBusca() {
		assertNotNull(RunCucumberTest.driver, "WebDriver nao foi inicializado.");
		kabumBuscaPage = new KabumBuscaPage(RunCucumberTest.driver);
		kabumBuscaPage.abrirHome();
	}

	@Quando("informo {string} no campo de busca da home")
	public void informoNoCampoDeBuscaDaHome(String produto) {
		assertNotNull(kabumBuscaPage, "Page Object da busca nao foi inicializado.");
		kabumBuscaPage.informarProdutoNaBusca(produto);
	}

	@Quando("seleciono a primeira sugestao da busca")
	public void selecionoAPrimeiraSugestaoDaBusca() {
		assertNotNull(kabumBuscaPage, "Page Object da busca nao foi inicializado.");
		kabumBuscaPage.selecionarPrimeiraSugestaoDaBusca();
	}

	@Entao("devo visualizar a listagem de busca para {string}")
	public void devoVisualizarAListagemDeBuscaPara(String produto) {
		assertNotNull(kabumBuscaPage, "Page Object da busca nao foi inicializado.");
		assertTrue(
				kabumBuscaPage.estaNaListagemDeBuscaPor(produto),
				"Resultado de busca nao encontrado. URL atual: " + kabumBuscaPage.getUrlAtual());
	}
}
