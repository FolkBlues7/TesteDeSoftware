# Jogo de Exploração de Labirinto (Avaliação 2)

**Repositório dedicado ao arquivamento do(s) software(s) criado(s) na Disciplina de Teste de Software – Ciência da Computação, UFERSA.**

Este projeto consiste em um jogo de exploração desenvolvido em **Java 21** utilizando **JavaFX**, focado na aplicação de técnicas rigorosas de Teste de Software. O objetivo do jogador é navegar por um cenário gerado aleatoriamente, coletar moedas e evitar obstáculos até completar a missão.

---

## Funcionalidades

- **Geração Dinâmica:** Mapas com obstáculos aleatórios e moedas (1 a 3).
- **Garantia de Acessibilidade:** O sistema utiliza um algoritmo de busca em largura (BFS) para garantir que todas as moedas sejam alcançáveis.
- **Visualização de Trajeto:** Renderização gráfica em tempo real do rastro percorrido pelo jogador.
- **Progressão:** Transição automática de fase ao coletar todas as moedas.

---

## Engenharia de Testes

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

A cobertura MC/DC é estendida a outros métodos com decisões compostas, como `adicionarMovimento`, `aplicarRegrasDeMovimento` e `tentarExcluir`, assegurando que cada condição seja exercitada de forma independente.

### 4. Testes de Propriedade (Property-Based Testing)

Utilizando a biblioteca **jqwik**, foram escritos testes de propriedade que verificam comportamentos universais do sistema, independentemente de valores concretos:

- **Mapa:** Qualquer configuração aleatória gerada garante acessibilidade a todas as moedas, alçapão e item especial a partir da origem.
- **Sessão de Jogo:** Após qualquer sequência de avanços e recuos, o nível nunca é menor que 1 e não há estouro de índice no histórico de mapas.
- **Controlador de Jogo:** Movimentos inválidos nunca alteram as coordenadas atuais do jogador.
- **Login:** Credenciais aleatórias não cadastradas sempre resultam em autenticação nula.
- **Gerador de Números Aleatórios:** Qualquer chamada a `nextInt(bound)` retorna um valor no intervalo `[0, bound-1]` e `nextDouble()` retorna um valor em `[0.0, 1.0)`.
- **Usuário:** Operações de adição de pontos e incremento de sessões nunca tornam o número de sessões negativo.

### 5. Testes com Dublês (Mocks)

Para isolar unidades de código, o projeto emprega **Mockito** na criação de dublês para dependências como `SessaoJogo`, `Mapa` e listeners da interface gráfica. Exemplos incluem:

- Simulação de um mapa que retorna `false` para qualquer movimento, validando que o controlador mantém o estado.
- Verificação de que o método `carregarNivel()` invoca `salvarMapa` quando o mapa do nível é `null`.
- Confirmação de que os métodos `render` e `atualizarHUD` são chamados exatamente nos momentos esperados.

### 6. Design de Contratos (Design by Contract)

As classes de domínio e controle foram instrumentadas com **asserções** (`assert`) que documentam e validam em tempo de execução (com a opção `-ea`) as pré‑condições, pós‑condições e invariantes. Principais contratos implementados:

- **Mapa:** Invariantes que garantem a disjunção dos elementos (moedas, obstáculos, alçapão e item especial) e a consistência da lista de espaços vazios.
- **GameController:** Pré‑condições de não‑nulidade em construtores e pós‑condições que mantêm as coordenadas dentro dos limites do mapa.
- **SessaoJogo:** Invariante `nivelAtual >= 1` e pré‑condição de mapa não nulo ao salvar.
- **Usuario:** Pré‑condições de `login`/`senha` não nulos e invariante de sessões não negativas.
- **LoginController:** Pré‑condições de parâmetros não nulos em operações sensíveis.

Essas asserções transformam possíveis bugs silenciosos em falhas ruidosas durante o desenvolvimento, alinhando‑se ao princípio de falha rápida (*fail‑fast*).

---

## Rastreabilidade de Testes

Para garantir que todos os requisitos da especificação foram atendidos, o projeto segue a seguinte matriz de rastreabilidade:

| ID | Requisito | Teste Automatizado |
| :--- | :--- | :--- |
| **REQ-01** | Movimentação em diferentes locais | `podeMover()`, `adicionarMovimento()` |
| **REQ-02** | Bloqueio por obstáculos | `podeMover()` (Validação de colisão) |
| **REQ-03** | Visualização gráfica do trajeto | Verificado via Integração e `getTrajeto().size()` |
| **REQ-04** | Coleta de itens e fim de missão | `adicionarMovimento()` e `faseConcluida()` |
| **REQ-05** | Geração de cenário aleatório | `gerarCenarioAleatorio()` com injeção de `Random` |
| **REQ-06** | Persistência e autenticação de usuários | `LoginControllerTest` (todos os cenários de login, cadastro e exclusão) |
| **REQ-07** | Progressão de níveis e gerenciamento de sessão | `SessaoJogoTest` (avanços, recuos e histórico de mapas) |
| **REQ-08** | Imutabilidade do estado em movimentos inválidos | Teste de propriedade `movimentoInvalidoNuncaAlteraPosicao` |

---

## Tecnologias Utilizadas

- **Java 21:** Versão base do projeto.
- **JavaFX:** Interface gráfica e tratamento de eventos de teclado.
- **JUnit 5:** Framework de execução de testes.
- **AssertJ:** Biblioteca de asserções fluídas para maior legibilidade.
- **Mockito:** Criação de dublês (mocks e spies) para testes unitários.
- **jqwik:** Testes de propriedade e geração aleatória de dados.
- **JaCoCo:** Plugin para medição e relatório de cobertura de código.
- **Maven:** Gestão de dependências e automação de build.

---

## Detalhes Técnicos de Implementação

O projeto utiliza o padrão **Strategy** para a geração de números aleatórios (`RandomGenerator`), permitindo que nos testes de unidade possamos injetar sementes fixas ou geradores determinísticos. Isso garante que os testes de geração de cenário e os testes de propriedade sejam reproduzíveis e previsíveis.

---

## Como Executar

### Pré-requisitos

- JDK 21
- Maven 3.6+

### Rodar o Jogo

```bash
mvn clean javafx:run
