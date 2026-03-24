# language: pt
Funcionalidade: Cadastro completo pela home da Kabum

Como visitante da Kabum
Quero concluir o cadastro a partir da home
Para visualizar a confirmacao de usuario cadastrado com sucesso

@cadastro-completo @login @bloqueado @camada_regressao_funcional @camada_e2e_critica
Cenario: Cadastrar novo usuario com sucesso pela home
Dado que acesso a home da Kabum
Quando clico no botao Entre ou Cadastre-se da home
Entao o popup Acesse sua conta ou cadastre-se deve aparecer
Quando informo o email de cadastro no popup e clico em entrar
E seleciono a opcao cadastrar com CPF
E preencho os dados de cadastro gerados automaticamente
E marco o checkbox Li e estou de acordo
E clico em continuar no cadastro
E informo o CEP de cadastro e clico em confirmar
E informo o numero e complemento de endereco e clico em confirmar
Entao devo visualizar a mensagem de sucesso de cadastro "Usuario cadastrado com sucesso."
