# 🎮 Jogo de Exploração de Labirinto (Avaliação 1)

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
