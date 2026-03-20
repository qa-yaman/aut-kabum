# aut-kabum

Projeto de Automacao Java para validacao de fluxos do ecommerce KaBuM com Selenium, Cucumber e Allure.

## Stack

- Java 17
- Maven
- Selenium WebDriver
- Cucumber
- JUnit
- Allure Report
- GitHub Actions

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

## Pre-requisitos

- Java 17 configurado no ambiente
- Maven instalado
- Google Chrome disponivel para execucao local
- Allure CLI instalado caso queira abrir o relatorio localmente

## Como Executar

Executar a suite:

```bash
mvn clean test
```

Executar explicitamente em headless:

```bash
mvn clean test -Dheadless=true
```

Gerar e abrir o relatorio Allure local:

```bash
allure serve allure-results
```

## CI e Relatorios

[![CI](https://github.com/qa-yaman/aut-kabum/actions/workflows/ci.yml/badge.svg?branch=main "Relatorio Automacao")](https://github.com/qa-yaman/aut-kabum/actions/workflows/ci.yml)

- Workflow: `CI + Allure Report Deploy`
- Relatorio da branch hml: [https://qa-yaman.github.io/aut-kabum/hml/](https://qa-yaman.github.io/aut-kabum/hml/)

## Como o CI funciona

Em cada `push` e `pull_request` para `main` e `hml`, a pipeline:

- realiza o checkout do repositorio
- configura Java 17 com cache Maven
- valida a disponibilidade do Chrome no runner
- executa `mvn -B clean test -Dheadless=true`
- valida a geracao de `allure-results`
- gera o `allure-report`
- publica os artefatos `surefire-reports`, `allure-results` e `allure-report`

Nos pushes:

- `main` publica o Allure na raiz do `gh-pages`
- `hml` publica o Allure em `gh-pages/hml`

O relatorio so fica disponivel depois que a pipeline concluir com sucesso e publicar a branch `gh-pages`.

## Cenarios atuais

- smoke test da home
- fluxo de cadastro pela home
- tratamento do fluxo protegido de login/cadastro com anti-bot

## Observacoes

- o smoke test da home deve executar normalmente no CI
- ***o cenario protegido de login/cadastro pode ficar como `SKIPPED`***
- ***quando isso acontecer, o motivo esperado e `Blocked by CAPTCHA / anti-bot`***
- ***o fluxo protegido pode oscilar conforme a camada de seguranca da KaBuM***
