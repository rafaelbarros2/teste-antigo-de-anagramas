package com.vaiquevai.anagramas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnagramGeneratorTest {

    @Test
    void deveGerarAnagramasParaABC() {
        List<String> esperado = List.of("abc", "acb", "bac", "bca", "cab", "cba");
        List<String> resultado = AnagramGenerator.gerarAnagramas("abc");
        assertEquals(esperado, resultado, "As permutações de 'abc' devem corresponder exatamente");
    }

    @Test
    void deveGerarApenasUmParaLetraUnica() {
        List<String> esperado = List.of("a");
        List<String> resultado = AnagramGenerator.gerarAnagramas("a");
        assertEquals(esperado, resultado, "Para entrada de uma única letra, deve haver exatamente um anagrama");
    }

    @Test
    void deveFalharParaEntradaVazia() {
        Executable exec = () -> AnagramGenerator.gerarAnagramas("");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, exec);
        assertTrue(ex.getMessage().toLowerCase().contains("vazia"));
    }

    @Test
    void deveFalharParaCaracterNaoLetra() {
        Executable exec = () -> AnagramGenerator.gerarAnagramas("a1");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, exec);
        assertTrue(ex.getMessage().toLowerCase().contains("apenas letras"));
    }

    @Test
    void deveFalharParaLetrasRepetidas() {
        Executable exec = () -> AnagramGenerator.gerarAnagramas("aba");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, exec);
        assertTrue(ex.getMessage().toLowerCase().contains("distintas"));
    }
}