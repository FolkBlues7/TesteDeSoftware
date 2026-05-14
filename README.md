# 🎮 Jogo de Exploração de Labirinto (Avaliação 2)

**Repositório dedicado ao arquivamento do(s) software(s) criado(s) na Disciplina de Teste de Software — Ciência da Computação, UFERSA.**

Este projeto consiste em um jogo de exploração desenvolvido em **Java 21** utilizando **JavaFX**, focado na aplicação de técnicas rigorosas de Teste de Software. O objetivo do jogador é navegar por um cenário gerado aleatoriamente, coletar moedas e evitar obstáculos até completar a missão.

---

## 🚀 Funcionalidades

- **Geração Dinâmica:** Mapas com obstáculos aleatórios e moedas (1 a 3).
- **Garantia de Acessibilidade:** O sistema utiliza um algoritmo de busca em largura (BFS) para garantir que todas as moedas sejam alcançáveis.
- **Visualização de Trajeto:** Renderização gráfica em tempo real do rastro percorrido pelo jogador.
- **Progressão:** Transição automática de fase ao coletar todas as moedas.

---

## 🧪 Engenharia de Testes

O coração deste projeto é a sua robustez técnica. Foram aplicadas metodologias sistemáticas para garantir **100% de cobertura de código** nas regras de negócio.

### 1. Particionamento de Domínio e Classes de Equivalência

O domínio de entrada para a movimentação foi dividido em:

- **Classe Válida:** Coordenadas dentro dos limites do mapa (`0 ≤ x < colunas`, `0 ≤ y < linhas`) e que não possuam obstáculos.
- **Classe Inválida (Obstáculo):** Coordenadas internas que colidem com paredes.
- **Classe Inválida (Limites):** Coordenadas externas ao plano cartesiano do mapa.

### 2. Análise de Valor de Fronteira

Os testes focam nos limites críticos onde falhas de lógica costumam ocorrer:

- **Fronteira Mínima:** Posição `(0, 0)`.
- **Fronteira Máxima:** Posição `(largura - 1, altura - 1)`.
- **Extrapolação:** Testes com valores negativos (`-1`) e valores acima do limite máximo (`limite + 1`).

### 3. Cobertura MC/DC (Modified Condition/Decision Coverage)

No método `podeMover`, a lógica de decisão composta foi testada para garantir que cada subcondição afete o resultado final de forma independente:

> `(DentroDoMapa) AND (CaminhoLivre)`

Os testes validam cenários onde:

- O jogador tenta sair do mapa (O resultado deve ser `false` independente do obstáculo).
- O jogador atinge um obstáculo interno (O resultado deve ser `false` mesmo estando dentro do mapa).
- O jogador move para uma área livre (O resultado deve ser `true`).

### 4. Testes Detalhados por Componente

#### 4.1. `GameControllerTest.java`

- **Testes de Domínio:**
    - `construtorComUsuarioSessaoEMapaIniciaModoTeste`: Verifica se o controller inicia em modo de teste com coordenadas zeradas.
    - `construtorSemSessaoEMapaIniciaModoNormal`: Garante que o controller inicia em modo normal sem sessão e mapa.
    - `carregarNivelResetaCoordenadas`: Valida se o carregamento de nível redefine as coordenadas x e y para zero.
    - `getOnVoltarMenuRetornaRunnableInjetado`: Verifica a recuperação da ação de voltar ao menu.
    - `movimentoValidoAtualizaCoordenadasERegistraMovimento`: Confirma que movimentações válidas mudam as coordenadas e registram os passos.
- **Testes Estruturais (MC/DC):**
    - `carregarNivelComMapaNuloCriaNovoMapaENotifica`: Valida a criação de um novo mapa e notificação do listener quando o mapa na sessão é nulo.
    - `carregarNivelSemListenerNaoLancaExcecao`: Verifica que a ausência de um listener não causa exceção.
    - `mcdc_MovimentoParaPosicaoBloqueadaNaoAlteraEstado`: Garante que tentar ir para uma posição bloqueada não altera as coordenadas do jogador.
    - `mcdc_AlcapaoSemItemVoltaNivel`: Verifica se cair no alçapão sem o item especial faz a sessão voltar um nível.
    - `mcdc_AlcapaoComItemAvancaNivel`: Verifica se acessar o alçapão com o item especial avança a sessão para o próximo nível.
    - `mcdc_MovimentoComMoedaAdicionaPontos`: Valida que mover-se para um local com moeda resulta em pontos.
    - `mcdc_MovimentoComCristalAtivaItemEspecial`: Valida a coleta de cristal ativando o item especial na sessão.
- **Testes com Dublê:**
    - `carregarNivelEmModoNormalNotificaListener`: Garante que o carregamento do nível em modo normal chama os métodos de renderização e atualização do HUD do listener.
    - `movimentoValidoEmModoNormalNotificaListener`: Verifica as notificações à UI após o movimento ser bem-sucedido.
- **Testes de Fronteira:**
    - `movimentoForaDosLimitesNaoAlteraPosicao`: Garante que movimentações para coordenadas inválidas/limites não alteram a posição do jogador.
- **Testes de Propriedade:**
    - `movimentoInvalidoNuncaAlteraPosicao`: Checa em larga escala diversas coordenadas fora dos limites para assegurar a imutabilidade da posição.

#### 4.2. `LoginControllerTest.java`

- **Testes de Domínio:**
    - `loginComCredenciaisCorretasRetornaUsuarioEIncrementaSessao`: Assegura o sucesso do login e incremento de sessões.
    - `loginComSenhaIncorretaRetornaNulo`: Confirma a recusa de autenticação com senhas erradas.
    - `cadastroComSucesso`: Valida um fluxo feliz de registro de novo usuário.
    - `cadastroComUsuarioExistenteFalha`: Evita o cadastro de usuários com logins duplicados.
    - `excluirUsuarioPorAdminRemoveELograSucesso`: Testa as permissões do administrador ao deletar perfis.
    - `excluirUsuarioInexistenteRetornaMensagem`: Valida mensagens corretas para contas não achadas ou na tentativa de deletar o admin.
- **Testes de Fronteira:**
    - `loginComCredenciaisEmBrancoOuNulasRetornaNulo`: Teste parametrizado para strings nulas, espaços e tabulações.
    - `loginComStringsMuitoLongasNaoLancaExcecao`: Submete credenciais enormes para confirmar a estabilidade.
- **Testes Estruturais (MC/DC):**
    - `cadastroComLoginNulo`, `cadastroComLoginVazio`, `cadastroComSenhaNula`, `cadastroComSenhaVazia`: Garantem a recusa de dados parciais.
    - `excluirComSenhaAdminIncorretaFalha`: Impede falhas na autorização da senha de exclusão.
    - `excluirQuandoAdminNaoESuperUsuarioFalha`: Simula um admin sem flag de super usuário tentando realizar exclusões.
    - `carregarArquivoIgnoraLinhasMalformadas`: Assegura que corrupções parciais de arquivo sejam puladas.
    - `carregarArquivoComIOExceptionNaoInterrompeConstructor`: Injeta um diretório para forçar falha física na leitura.
- **Testes de Propriedade:**
    - `qualquerCredencialAleatoriaNaoExistenteRetornaNulo`: Confirma que credenciais sintéticas não ativas não conseguem fazer bypass na verificação.

#### 4.3. `MapaTest.java`

- **Testes de Domínio:**
    - `adicionarMovimento`: Foca no preenchimento do trajeto percorrido e detecção de encerramento da fase.
    - `deveGarantirIdempotenciaNaColetaDeMoedas`: Confirma que o usuário não ganha moedas repetidas retornando na mesma casa.
    - `deveRegistrarTrajetoMesmoAoVisitarMesmaCasa`: Afirma que bater idas e vindas marca todos os passos para trilhar.
    - `naoDeveAdicionarMovimentoSeHouverObstaculo`: Barra salvamento de trajetórias em blocos proibidos.
    - `deveValidarAcessibilidadeComBFS`: Constrói cenários bloqueados e checa comportamentos.
    - `coletarMoeda_deveRemoverMoedaDaLista`: Testa a remoção de moedas da lista.
    - `isObstaculo_quandoPontoEstaNaLista_retornaTrue` / `..._retornaFalse`: Busca exatidão nas colisões com obstáculos.
    - `Testes de isAlcapao e isItemEspecial`: Seis testes confirmando todas as permutações desses dois elementos.
- **Testes de Fronteira:**
    - `podeMover`: Valida o movimento nas bordas superior, inferior, direita e esquerda.
    - `testesDeRobustezExtrema`: Lança valores de `MAX_VALUE` e `MIN_VALUE` em coordenadas de movimentação e `null pointers` no `Randomizer`.
- **Testes Estruturais (MC/DC):**
    - `mcdc_AdicionarMovimento`: Três testes batendo em caminhos vazios, caminhos contendo moedas e batidas em obstáculos.
- **Testes com Dublê:**
    - `gerarCenarioAleatorio_comSeedFixa_geraMapaCompletoEValido`: Simula a classe utilitária random garantindo a disposição do item, das moedas e da portinhola com resultados predefinidos.
- **Testes de Propriedade:**
    - `propriedade_GerarCenarioAleatorio_SempreAcessivel`: Garante que, a despeito do gerador randômico, todos os itens ofereçam rotas seguras alcançáveis a partir da posição inicial.

#### 4.4. `SessaoJogoTest.java`

- **Testes de Domínio:**
    - `dominio_EstadoInicialDaSessao`: Valida valores padrões de início (nível 1 e sem cristal).
    - `dominio_PodePegarItemEspecial`: Testa a manipulação do flag do item especial.
- **Testes Estruturais (MC/DC):**
    - `estrutural_AvancarNivelConsomeItemEAvanca`: Valida o avanço do level perdendo o cristal coletado.
    - `estrutural_VoltarNivelDeFaseAvancadaDiminuiONivelEPerdeItem`: Verifica que o declínio de fases descarta o cristal.
    - `estrutural_VoltarNivelNoNivelUmNaoVaiParaZero`: Assegura impossibilidade de voltar ao nível 0 do jogo.
- **Testes de Integração:**
    - `integracao_SalvarERecuperarMapasCorretamente`: Foca na serialização transitória das memórias das malhas recuadas (com Mockito).
- **Testes de Fronteira:**
    - `fronteira_AvancarEVoltarMuitasVezes_NivelNuncaMenorQueUm`: Teste com `for loop` exaustivo retroagindo fases garantindo segurança contra estouramentos numéricos.
- **Testes de Propriedade:**
    - `propriedade_NivelNuncaNegativoENaoEstouraIndice`: Teste parametrizado retroagindo e avançando n níveis para solidificar a robustez contra índices inválidos.

#### 4.5. `UsuarioTest.java`

- **Testes de Domínio:**
    - `dominio_ConstrutorNovoUsuarioIniciaComZeroPontosESessoes`: Testa o construtor primário.
    - `dominio_ConstrutorDeCarregamentoRestauraValoresCorretamente`: Testa o construtor parametrizado (para reconstrução de saves).
    - `dominio_AdicionarPontosSomaAoTotal`: Valida somas sequenciais no Score.
    - `dominio_IncrementarSessoesAumentaDeUmEmUm`: Contabiliza uso do App.
    - `dominio_GetNomeRetornaOLoginParaOHud`: Testa o retorno do login para o HUD.
- **Testes de Fronteira:**
    - `fronteira_AdicionarPontosNegativos`: Identifica se a aplicação aceita números negativos em Score.
    - `fronteira_PontuacaoAlemDoLimiteInteiro`: Força Overflow de números `int` nas moedas de pontuação.
- **Testes de Propriedade:**
    - `propriedade_PontuacaoESessoesNuncaNegativasAposOperacoes`: Afirma que as manipulações intensas não forçam sessões a marcarem valores menores do que zero.

#### 4.6. `JavaRandomGeneratorTest.java`

- **Testes de Domínio:**
    - `nextInt_alwaysReturnsValueWithinBounds`: Assegura obediência da limitação solicitada ao randômico base inteiro.
    - `nextDouble_alwaysReturnsValueBetweenZeroAndOne`: Valida comportamento de pontos flutuantes em casa decimal padronizada.
- **Testes de Fronteira:**
    - `nextInt_boundZero_throwsException`: Induz um random de zero e valida acionamento da `IllegalArgumentException`.
- **Testes de Propriedade:**
    - `nextInt_anyBound_returnsValueInRange`: Gera números caóticos em massa dentro de amarras fixadas pelo framework de teste Jqwik.
    - `nextDouble_alwaysBetweenZeroAndOne`: Semelhante ao de inteiros usando sementes dinâmicas (`seed`) para ratificar inquebrabilidade do método `double`.

---

## 📊 Rastreabilidade de Testes

Para garantir que todos os requisitos da especificação foram atendidos, o projeto segue a seguinte matriz de rastreabilidade:

| ID | Requisito | Teste Automatizado |
| :--- | :--- | :--- |
| **REQ-01** | Movimentação em diferentes locais | `podeMover()`, `adicionarMovimento()` |
| **REQ-02** | Bloqueio por obstáculos | `podeMover()` (Validação de colisão) |
| **REQ-03** | Visualização gráfica do trajeto | Verificado via Integração e `getTrajeto().size()` |
| **REQ-04** | Coleta de itens e fim de missão | `adicionarMovimento()` e `faseConcluida()` |
| **REQ-05** | Geração de cenário aleatório | `gerarCenarioAleatorio()` com injeção de `Random` |

---

## 🛠️ Tecnologias Utilizadas

- **Java 21:** Versão base do projeto.
- **JavaFX:** Interface gráfica e tratamento de eventos de teclado.
- **JUnit 5:** Framework de execução de testes.
- **AssertJ:** Biblioteca de asserções fluídas para maior legibilidade.
- **JaCoCo:** Plugin para medição e relatório de cobertura de código.
- **Maven:** Gestão de dependências e automação de build.

---

## 📝 Detalhes Técnicos de Implementação

O projeto utiliza o padrão **Strategy** para a geração de números aleatórios (`RandomGenerator`), permitindo que nos testes de unidade possamos "mockar" ou predefinir comportamentos aleatórios. Isso garante que os testes sejam determinísticos, mesmo em um jogo procedimental.

---

## 📦 Como Executar

### Pré-requisitos

- JDK 21
- Maven 3.6+

### Rodar o Jogo

```bash
mvn clean javafx:run
```

### Rodar os Testes e Gerar Relatório de Cobertura

```bash
mvn test
```

> **Nota:** O relatório do JaCoCo estará disponível em: `target/site/jacoco/index.html`
