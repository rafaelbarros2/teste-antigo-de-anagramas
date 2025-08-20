package com.vaiquevai.anagramas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Aplicação de console para demonstrar o gerador de anagramas.
 */
public class App {
    public static void main(String[] args) throws IOException {
        String entrada;
        if (args.length > 0) {
            entrada = args[0];
        } else {
            System.out.print("Digite um grupo de letras distintas (ex.: abc): ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            entrada = br.readLine();
        }

        try {
            List<String> anagramas = AnagramGenerator.gerarAnagramas(entrada);
            anagramas.forEach(System.out::println);
        } catch (IllegalArgumentException e) {
            System.err.println("Erro: " + e.getMessage());
            System.exit(1);
        }
    }
}