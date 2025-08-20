# Gerador de Anagramas (Java)

Função utilitária para gerar todos os **anagramas** possíveis a partir de um conjunto de **letras distintas**.
Inclui **validação básica**, **testes unitários (JUnit 5)** e um `main` de demonstração.

## Requisitos atendidos

1. Aceita qualquer grupo de letras **distintas** como entrada e produz todos os anagramas.
2. Código otimizado para **legibilidade e clareza**.
3. **Validação básica**: entrada não pode ser vazia/nula; deve conter apenas letras; não deve conter letras repetidas.
4. **Testes unitários** (JUnit 5) cobrindo casos normais e extremos (ex.: única letra e entrada vazia).
5. Código **documentado** explicando a lógica de geração.

## Pré-requisitos

* **Java 17+**
* **Maven 3.8+**

## Como executar

```bash
# Rodar testes
mvn -q test

# Empacotar jar executável
mvn -q -DskipTests package

# Executar (passe as letras como argumento, ex.: "abc")
java -jar target/anagramas-1.0.0.jar abc

# Ou sem argumentos (o programa pedirá a entrada no console)
java -jar target/anagramas-1.0.0.jar
```

## Exemplo

Entrada: `abc`
Saída:

```
abc
acb
bac
bca
cab
cba
```

## Observações de validação

* **Somente letras** (`Character.isLetter`).
* **Sem repetição** de letras (case-sensitive).
* **Entrada vazia** ou **nula** não é permitida.

## Contagem de anagramas e complexidade

* Letras **distintas**: quantidade = **n!**
  Ex.: `abc` → 3! = 6 → `abc, acb, bac, bca, cab, cba`.

* **Com repetições** (se adaptado):
  quantidade = **n! / (m1! \* m2! \* ... \* mk!)**, onde `mi` é a contagem de cada letra repetida.
  Exemplos:

    * `aab` → 3!/2! = **3**
    * `banana` → 6!/(3!\*2!) = **60**

**Tempo para listar todos**: Θ(n · R), onde `R` é o número de anagramas gerados.

* Distintas: `R = n!` → Θ(n · n!)
* Com repetições: `R = n! / (m1! * ... * mk!)`
  Como cada anagrama tem tamanho `n`, o custo por resultado é `O(n)`.

### Anagramas com letras repetidas (opcional)

Para permitir letras repetidas **sem** produzir duplicatas, ordene o array de caracteres e, no laço do backtracking, pule ramos duplicados:

```java
if (i > 0 && letras[i] == letras[i - 1] && !usados[i - 1]) continue;
```

