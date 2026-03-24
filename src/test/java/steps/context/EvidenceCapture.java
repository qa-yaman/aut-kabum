package steps.context;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import io.qameta.allure.Allure;


public final class EvidenceCapture {

    private final WebDriver driver;

    public EvidenceCapture(WebDriver driver) {
        this.driver = driver;
    }

    public void captureAllEvidence(String failureContext) {
        if (driver == null) {
            Allure.addAttachment("evidencia_erro", "Driver nao foi inicializado para o cenario.");
            return;
        }

        try {
            captureScreenshot();
        } catch (Exception e) {
            Allure.addAttachment("evidencia_screenshot_erro", e.getMessage());
        }

        try {
            capturePageHtml();
        } catch (Exception e) {
            Allure.addAttachment("evidencia_html_erro", e.getMessage());
        }

        try {
            captureUrl();
        } catch (Exception e) {
            Allure.addAttachment("evidencia_url_erro", e.getMessage());
        }

        try {
            captureConsoleLogs();
        } catch (Exception e) {
            Allure.addAttachment("evidencia_console_erro", e.getMessage());
        }

        try {
            captureTechInfo(failureContext);
        } catch (Exception e) {
            Allure.addAttachment("evidencia_techinfo_erro", e.getMessage());
        }
    }

    private void captureScreenshot() {
        if (!(driver instanceof TakesScreenshot)) {
            throw new IllegalArgumentException("Driver nao suporta screenshot");
        }

        byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Allure.addAttachment(
                "01_Screenshot_Falha",
                "image/png",
                new ByteArrayInputStream(screenshot),
                ".png"
        );
    }

    private void capturePageHtml() {
        String htmlContent = driver.getPageSource();
        Allure.addAttachment(
                "02_PageHTML_Falha",
                "text/html",
                new ByteArrayInputStream(htmlContent.getBytes()),
                ".html"
        );
    }

    private void captureUrl() {
        String url = driver.getCurrentUrl();
        Allure.addAttachment("03_URL_Falha", url);
    }

    private void captureConsoleLogs() {
        try {
            LogEntries logs = driver.manage().logs().get(LogType.BROWSER);
            if (logs == null || logs.getAll().isEmpty()) {
                Allure.addAttachment("04_ConsoleLogs_Falha", "Nenhum log do console disponivel");
                return;
            }

            StringBuilder consoleLogs = new StringBuilder();
            logs.getAll().forEach(entry ->
                    consoleLogs.append(String.format(
                            "[%s] %s: %s\n",
                            entry.getLevel(),
                            entry.getTimestamp(),
                            entry.getMessage()
                    ))
            );

            Allure.addAttachment(
                    "04_ConsoleLogs_Falha",
                    "text/plain",
                    new ByteArrayInputStream(consoleLogs.toString().getBytes()),
                    ".txt"
            );
        } catch (WebDriverException e) {
            Allure.addAttachment("04_ConsoleLogs_Falha", "Logs do console nao disponiveis: " + e.getMessage());
        }
    }

    private void captureTechInfo(String failureContext) {
        StringBuilder techInfo = new StringBuilder();
        techInfo.append("=== INFORMACOES TECNICAS DE FALHA ===\n\n");

        techInfo.append("Timestamp: ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))).append("\n");

        techInfo.append("Contexto: ").append(failureContext).append("\n");

        try {
            String userAgent = (String) ((JavascriptExecutor) driver)
                    .executeScript("return navigator.userAgent;");
            techInfo.append("User-Agent: ").append(userAgent).append("\n");
        } catch (Exception e) {
            techInfo.append("User-Agent: Indisponivel\n");
        }

        try {
            Object width = ((JavascriptExecutor) driver)
                    .executeScript("return window.innerWidth;");
            Object height = ((JavascriptExecutor) driver)
                    .executeScript("return window.innerHeight;");
            techInfo.append("Window Size: ").append(width).append("x").append(height).append("\n");
        } catch (Exception e) {
            techInfo.append("Window Size: Indisponivel\n");
        }

        try {
            techInfo.append("\nCookies: ").append(driver.manage().getCookies().size()).append(" cookies\n");
            driver.manage().getCookies().forEach(cookie ->
                    techInfo.append(String.format("  - %s=%s\n", cookie.getName(), cookie.getValue().substring(0, Math.min(50, cookie.getValue().length()))))
            );
        } catch (Exception e) {
            techInfo.append("Cookies: Indisponivel\n");
        }

        Allure.addAttachment(
                "05_TecnicalInfo_Falha",
                "text/plain",
                new ByteArrayInputStream(techInfo.toString().getBytes()),
                ".txt"
        );
    }
}
