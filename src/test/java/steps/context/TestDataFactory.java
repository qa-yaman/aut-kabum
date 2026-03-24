package steps.context;

import java.time.Instant;
import java.util.Random;

/**
 * Fábrica de dados de teste dinâmicos, com escopo por cenário (via PicoContainer).
 * Gera uma massa única por execução — sem hardcode de dados sensíveis nas features.
 *
 * Prioridade dos dados:
 *   1. Variável de ambiente ou propriedade JVM (ex: KABUM_TEST_EMAIL)
 *   2. Valor gerado automaticamente (fallback seguro)
 */
public final class TestDataFactory {

    private static final Random RANDOM = new Random();

    private TestData dados;

    public TestData get() {
        if (dados == null) {
            dados = gerar();
        }
        return dados;
    }

    private static TestData gerar() {
        long ts = Instant.now().toEpochMilli();

        String email    = SensitiveDataResolver.resolveOrDefault("KABUM_TEST_EMAIL",    "kabum.auto." + ts + "@mailnull.com");
        String cpf      = SensitiveDataResolver.resolveOrDefault("KABUM_TEST_CPF",      gerarCpfValido());
        String senha    = SensitiveDataResolver.resolveOrDefault("KABUM_TEST_PASSWORD", "AutoTest@" + (ts % 100000));
        String cep      = SensitiveDataResolver.resolveOrDefault("KABUM_TEST_CEP",      "01310100");

        return new TestData(
                email,
                cpf,
                senha,
                cep,
                gerarCelular(),
                "20121982",
                "Usuario Teste Automatizado",
                "100",
                "001"
        );
    }

    /**
     * Gera CPF matematicamente válido (não necessariamente fiscal).
     * Evita sequências inválidas como 111.111.111-11.
     */
    static String gerarCpfValido() {
        int[] n = new int[9];
        for (int i = 0; i < 9; i++) {
            n[i] = RANDOM.nextInt(10);
        }

        // Evita sequência com todos os dígitos iguais (CPF inválido)
        boolean todoIguais = true;
        for (int i = 1; i < 9; i++) {
            if (n[i] != n[0]) {
                todoIguais = false;
                break;
            }
        }
        if (todoIguais) {
            n[0] = (n[0] + 1) % 10;
        }

        // Primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += n[i] * (10 - i);
        int d1 = 11 - (soma % 11);
        if (d1 >= 10) d1 = 0;

        soma = 0;
        for (int i = 0; i < 9; i++) soma += n[i] * (11 - i);
        soma += d1 * 2;
        int d2 = 11 - (soma % 11);
        if (d2 >= 10) d2 = 0;

        return String.format("%d%d%d.%d%d%d.%d%d%d-%d%d",
                n[0], n[1], n[2], n[3], n[4], n[5], n[6], n[7], n[8], d1, d2);
    }

    private static String gerarCelular() {
        return "51 9" + String.format("%08d", RANDOM.nextInt(100_000_000));
    }
}
