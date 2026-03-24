package steps;

import org.openqa.selenium.WebDriver;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.PendingException;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import steps.context.DriverContext;
import steps.context.EvidenceCapture;

public class ScenarioHooks {
	private final DriverContext driverContext;

	public ScenarioHooks(DriverContext driverContext) {
		this.driverContext = driverContext;
	}

	@Before(value = "@blocked or @bloqueado or @flaky", order = -1)
	public void marcarCenarioComoSkippedNoAllure(Scenario scenario) {
		String reason = "Cenario marcado com tag de nao execucao na pipeline: " + scenario.getSourceTagNames();
		Allure.step("Skipped por tag de governanca");
		Allure.addAttachment("skip_reason", reason);
		throw new PendingException(reason);
	}

	@Before(order = 0)
	public void iniciarDriver() {
		driverContext.start();
	}

	@After(order = 1)
	public void anexarEvidenciasEmFalha(Scenario scenario) {
		if (!scenario.isFailed()) {
			return;
		}

		WebDriver driver;
		try {
			driver = driverContext.getDriver();
		} catch (IllegalStateException exception) {
			Allure.addAttachment("evidencia_erro", "Driver nao foi inicializado para o cenario.");
			return;
		}

		EvidenceCapture evidenceCapture = new EvidenceCapture(driver);
		String failureContext = String.format(
				"Cenario: %s | Status: %s",
				scenario.getName(),
				scenario.getStatus()
		);
		evidenceCapture.captureAllEvidence(failureContext);
	}

	@After(order = 0)
	public void finalizarDriver() {
		driverContext.stop();
	}
}
