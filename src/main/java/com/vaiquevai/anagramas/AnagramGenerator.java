package com.vaiquevai.anagramas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Utilitário para gerar anagramas (permutações) de letras distintas.
 * Assume que a entrada contém apenas letras e sem repetições.
 * Resultados retornados em ordem lexicográfica.
 */

public final class AnagramGenerator {

    private AnagramGenerator() {
        // Classe utilitária: construtor privado para evitar instanciação
    }

    /**
     * Gera todos os anagramas da entrada.
     * Estratégia: ordena os caracteres e usa backtracking,
     * marcando posições já utilizadas até completar cada permutação.
     *
     * @param entrada letras distintas, ex.: "abc"
     * @return lista com todas as permutações em ordem lexicográfica
     * @throws IllegalArgumentException se a entrada for nula, vazia, contiver não-letras ou repetição
     */
    public static List<String> gerarAnagramas(String entrada) {
        validarEntrada(entrada);

        char[] letras = entrada.toCharArray();
        Arrays.sort(letras); // ordena para produzir resultado determinístico

        List<String> resultado = new ArrayList<>();
        boolean[] usados = new boolean[letras.length];
        StringBuilder atual = new StringBuilder(letras.length);

        backtrack(letras, usados, atual, resultado);
        return resultado;
    }


    /**
     * Backtracking: adiciona uma letra não usada ao prefixo, explora,
     * e desfaz a escolha ao retornar (fazer → explorar → desfazer).
     */
    private static void backtrack(char[] letras, boolean[] usados, StringBuilder atual, List<String> resultado) {
        if (atual.length() == letras.length) {
            resultado.add(atual.toString());
            return;
        }
        for (int i = 0; i < letras.length; i++) {
            if (!usados[i]) {
                usados[i] = true;
                atual.append(letras[i]);
                backtrack(letras, usados, atual, resultado);
                atual.deleteCharAt(atual.length() - 1);
                usados[i] = false;
            }
        }
    }

    private static void validarEntrada(String entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("Entrada nula não é permitida.");
        }
        entrada = entrada.trim();
        if (entrada.isEmpty()) {
            throw new IllegalArgumentException("Entrada vazia não é permitida.");
        }
        // Apenas letras (aceita Unicode, ex.: acentuação), ajuste se quiser restringir a A-Z/a-z.
        for (char c : entrada.toCharArray()) {
            if (!Character.isLetter(c)) {
                throw new IllegalArgumentException("A entrada deve conter apenas letras. Caractere inválido: '" + c + "'");
            }
        }
        // Verifica repetição (case-sensitive)
        Set<Character> vistos = new HashSet<>();
        for (char c : entrada.toCharArray()) {
            if (!vistos.add(c)) {
                throw new IllegalArgumentException("A entrada deve conter letras distintas (sem repetição). Letra repetida: '" + c + "'");
            }
        }
    }
}