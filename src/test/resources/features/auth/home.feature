# language: pt
Funcionalidade: Smoke test da home da Kabum

Como visitante da Kabum
Quero validar o acesso inicial ao site
Para confirmar que a home esta disponivel

@smoke-home
Cenario: Home carregada com sucesso
Dado que acesso o site da Kabum
Entao o titulo da pagina deve conter "KaBuM"
