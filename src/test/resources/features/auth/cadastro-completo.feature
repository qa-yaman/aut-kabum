# language: pt
Funcionalidade: Cadastro completo pela home da Kabum

Como visitante da Kabum
Quero concluir o cadastro a partir da home
Para visualizar a confirmacao de usuario cadastrado com sucesso

@cadastro-completo @login @bloqueado
Cenario: Cadastrar novo usuario com sucesso pela home
  Dado que acesso a home da Kabum
  Quando clico no botao Entre ou Cadastre-se da home
  Entao o popup Acesse sua conta ou cadastre-se deve aparecer
  Quando informo o email "${KABUM_TEST_EMAIL}" no popup e clico em entrar
  E seleciono a opcao cadastrar com CPF
  E preencho os dados destacados com email "${KABUM_TEST_EMAIL}", CPF "${KABUM_TEST_CPF}", celular "519 96491220", data de nascimento "20121982", nome completo "Rose Da Silveira Dias" e senha "${KABUM_TEST_PASSWORD}"
  E marco o checkbox Li e estou de acordo
  E clico em continuar no cadastro
  E informo o CEP "${KABUM_TEST_CEP}" e clico em confirmar
  E informo o numero "100" e complemento "001" e clico em confirmar
  Entao devo visualizar a mensagem de sucesso de cadastro "Usuario cadastrado com sucesso."
