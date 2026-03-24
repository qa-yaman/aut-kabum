# language: pt
Funcionalidade: Busca de produtos na home da Kabum

Como visitante da Kabum
Quero pesquisar um produto pela barra de busca
Para acessar a listagem com os resultados relacionados

@busca @camada_regressao_funcional @regression
Cenario: Buscar monitor pela home
Dado que acesso a home da Kabum para realizar uma busca
Quando informo "monitor" no campo de busca da home
E seleciono a primeira sugestao da busca
Entao devo visualizar a listagem de busca para "monitor"
