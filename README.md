# aut-kabum

Projeto de Automacao Java - Ecommerce Kabum

## Estrutura do Projeto

```text
src
└── test
    ├── java
    │   ├── helpers
    │   ├── pages
    │   ├── runner
    │   └── steps
    └── resources
        └── features
```

## Como Executar

```bash
mvn clean test
```

Para visualizar relatorios do Allure:

```bash
allure serve allure-results
```

## CI

O projeto possui integracao continua via GitHub Actions no workflow `CI`.

Em cada `push` e `pull_request`, a pipeline:

- realiza o checkout do repositorio
- configura Java 17 com cache Maven
- valida a disponibilidade do Chrome no runner
- executa `mvn -B test -Dheadless=true`
- publica os artefatos `surefire-reports` e `allure-results`

## Observacoes

- o smoke test da home deve executar normalmente no CI
- o cenario protegido de login/cadastro pode ficar como `SKIPPED`
- quando isso acontecer, o motivo esperado e `Blocked by CAPTCHA / anti-bot`
