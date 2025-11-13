/**
 * QUESTÃO 7: Use Case / User Story - Sistema XYZ (Gestão de Plantas)
 * 
 * ============================================================================
 * PARTE 1: USER STORIES (Formato Ágil)
 * ============================================================================
 */

// ============================================================================
// EPIC: Fase 1 - Cadastro de Plantas
// ============================================================================

/**
 * USER STORY 1: Criar Planta
 * 
 * Como administrador do sistema
 * Quero criar um novo registro de planta
 * Para que possa ser utilizado como entrada nas próximas fases do projeto
 * 
 * CRITÉRIOS DE ACEITE:
 * 
 * ✅ Cenário 1: Criação bem-sucedida
 *    DADO que estou autenticado como admin
 *    QUANDO eu preencho o código "12345" (numérico, único)
 *    E preencho a descrição "Plant A" (opcional, máx 10 caracteres)
 *    E clico em "Salvar"
 *    ENTÃO o sistema deve salvar a planta
 *    E exibir mensagem "Planta criada com sucesso"
 *    E redirecionar para a lista de plantas
 * 
 * ✅ Cenário 2: Validação de código obrigatório
 *    DADO que estou criando uma planta
 *    QUANDO eu deixo o campo código vazio
 *    E clico em "Salvar"
 *    ENTÃO o sistema deve exibir erro "Código é obrigatório"
 *    E não deve salvar a planta
 * 
 * ✅ Cenário 3: Validação de código numérico
 *    DADO que estou criando uma planta
 *    QUANDO eu digito "ABC123" no campo código
 *    E clico em "Salvar"
 *    ENTÃO o sistema deve exibir erro "Código deve conter apenas números"
 *    E não deve salvar a planta
 * 
 * ✅ Cenário 4: Validação de código duplicado
 *    DADO que existe uma planta com código "12345"
 *    QUANDO eu tento criar outra planta com código "12345"
 *    E clico em "Salvar"
 *    ENTÃO o sistema deve exibir erro "Código já cadastrado"
 *    E não deve salvar a planta
 * 
 * ✅ Cenário 5: Validação de tamanho da descrição
 *    DADO que estou criando uma planta
 *    QUANDO eu digito "Esta descrição tem mais de 10 caracteres"
 *    E clico em "Salvar"
 *    ENTÃO o sistema deve exibir erro "Descrição deve ter no máximo 10 caracteres"
 *    E não deve salvar a planta
 * 
 * ✅ Cenário 6: Descrição opcional
 *    DADO que estou criando uma planta
 *    QUANDO eu preencho apenas o código "12345"
 *    E deixo a descrição vazia
 *    E clico em "Salvar"
 *    ENTÃO o sistema deve salvar a planta sem descrição
 *    E exibir mensagem de sucesso
 */

/**
 * USER STORY 2: Atualizar Planta
 * 
 * Como usuário do sistema
 * Quero editar uma planta existente
 * Para manter as informações atualizadas
 * 
 * CRITÉRIOS DE ACEITE:
 * 
 * ✅ Cenário 1: Atualização bem-sucedida
 *    DADO que existe uma planta com código "12345"
 *    QUANDO eu edito a descrição para "Plant B"
 *    E clico em "Salvar"
 *    ENTÃO o sistema deve atualizar a planta
 *    E exibir mensagem "Planta atualizada com sucesso"
 * 
 * ✅ Cenário 2: Código não pode ser alterado
 *    DADO que estou editando uma planta
 *    QUANDO tento alterar o campo código
 *    ENTÃO o campo deve estar desabilitado/readonly
 *    (Ou: alteração de código exige um fluxo especial)
 * 
 * ✅ Cenário 3: Validações aplicam-se na atualização
 *    DADO que estou editando uma planta
 *    QUANDO tento salvar com descrição > 10 caracteres
 *    ENTÃO deve exibir erro de validação
 *    E não deve atualizar
 */

/**
 * USER STORY 3: Deletar Planta (Apenas Admin)
 * 
 * Como administrador do sistema
 * Quero deletar uma planta
 * Para remover registros obsoletos ou incorretos
 * 
 * CRITÉRIOS DE ACEITE:
 * 
 * ✅ Cenário 1: Deleção bem-sucedida (Admin)
 *    DADO que estou autenticado como admin
 *    E existe uma planta com código "12345"
 *    QUANDO eu clico em "Deletar" na planta
 *    E confirmo a ação no modal de confirmação
 *    ENTÃO o sistema deve deletar a planta
 *    E exibir mensagem "Planta deletada com sucesso"
 *    E remover a planta da listagem
 * 
 * ✅ Cenário 2: Usuário comum não pode deletar
 *    DADO que estou autenticado como usuário comum (não admin)
 *    QUANDO eu visualizo a lista de plantas
 *    ENTÃO o botão "Deletar" NÃO deve estar visível
 * 
 * ✅ Cenário 3: Confirmação antes de deletar
 *    DADO que sou admin
 *    QUANDO clico em "Deletar"
 *    ENTÃO deve exibir modal "Tem certeza que deseja deletar a planta 12345?"
 *    E botões "Confirmar" e "Cancelar"
 * 
 * ✅ Cenário 4: Soft delete (opcional)
 *    DADO que a planta está sendo usada em outras fases
 *    QUANDO eu deleto a planta
 *    ENTÃO o sistema deve fazer soft delete (flag deleted=true)
 *    E não exibir na listagem principal
 *    E manter referências em outras tabelas
 */

/**
 * USER STORY 4: Buscar/Listar Plantas
 * 
 * Como usuário do sistema
 * Quero visualizar e buscar plantas cadastradas
 * Para encontrar rapidamente as informações que preciso
 * 
 * CRITÉRIOS DE ACEITE:
 * 
 * ✅ Cenário 1: Listar todas as plantas
 *    DADO que existem plantas cadastradas
 *    QUANDO eu acesso a tela de plantas
 *    ENTÃO deve exibir lista com código, descrição e ações
 *    E ordenar por código crescente
 * 
 * ✅ Cenário 2: Busca por código
 *    DADO que existem múltiplas plantas
 *    QUANDO eu digito "123" no campo de busca
 *    ENTÃO deve filtrar plantas com código contendo "123"
 * 
 * ✅ Cenário 3: Busca por descrição
 *    DADO que existem múltiplas plantas
 *    QUANDO eu digito "Plant" no campo de busca
 *    ENTÃO deve filtrar plantas com descrição contendo "Plant"
 * 
 * ✅ Cenário 4: Paginação (se muitos registros)
 *    DADO que existem mais de 50 plantas
 *    QUANDO eu acesso a lista
 *    ENTÃO deve exibir 50 por página
 *    E controles de paginação
 */

// ============================================================================
// PARTE 2: USE CASE TRADICIONAL (Formato Formal)
// ============================================================================

/**
 * USE CASE: UC-001 - Criar Planta
 * 
 * ATOR PRINCIPAL: Usuário do Sistema (Admin ou Operador)
 * 
 * PRECONDIÇÕES:
 * - Usuário está autenticado no sistema
 * - Usuário possui permissão de criação de plantas
 * 
 * PÓS-CONDIÇÕES:
 * - Nova planta é persistida no banco de dados
 * - Planta está disponível para uso nas fases seguintes
 * - Evento de auditoria é registrado (log)
 * 
 * FLUXO PRINCIPAL:
 * 
 * 1. Usuário acessa menu "Cadastros > Plantas"
 * 2. Sistema exibe lista de plantas cadastradas
 * 3. Usuário clica em botão "Nova Planta"
 * 4. Sistema exibe formulário de cadastro com campos:
 *    - Código (input numérico, obrigatório)
 *    - Descrição (input texto, opcional, max 10 chars)
 * 5. Usuário preenche código "12345"
 * 6. Usuário preenche descrição "Plant A" (opcional)
 * 7. Usuário clica em "Salvar"
 * 8. Sistema valida dados (ver regras de negócio)
 * 9. Sistema verifica unicidade do código
 * 10. Sistema salva planta no banco de dados
 * 11. Sistema registra log de auditoria
 * 12. Sistema exibe mensagem de sucesso
 * 13. Sistema redireciona para lista de plantas
 * 
 * FLUXOS ALTERNATIVOS:
 * 
 * FA1: Código vazio (passo 8)
 *   8a. Sistema detecta código vazio
 *   8b. Sistema exibe mensagem "Código é obrigatório"
 *   8c. Sistema mantém formulário aberto
 *   8d. Retorna ao passo 5
 * 
 * FA2: Código não numérico (passo 8)
 *   8a. Sistema detecta caracteres não numéricos
 *   8b. Sistema exibe mensagem "Código deve conter apenas números"
 *   8c. Retorna ao passo 5
 * 
 * FA3: Código duplicado (passo 9)
 *   9a. Sistema encontra código já cadastrado
 *   9b. Sistema exibe mensagem "Código 12345 já está cadastrado"
 *   9c. Sistema sugere código disponível próximo (opcional)
 *   9d. Retorna ao passo 5
 * 
 * FA4: Descrição muito longa (passo 8)
 *   8a. Sistema detecta descrição > 10 caracteres
 *   8b. Sistema exibe mensagem "Descrição deve ter no máximo 10 caracteres (atual: 15)"
 *   8c. Retorna ao passo 6
 * 
 * FA5: Usuário cancela (após passo 7)
 *   7a. Usuário clica em "Cancelar"
 *   7b. Sistema exibe modal "Descartar alterações?"
 *   7c. Usuário confirma
 *   7d. Sistema retorna à lista sem salvar
 * 
 * FLUXO DE EXCEÇÃO:
 * 
 * FE1: Erro de banco de dados (passo 10)
 *   10a. Sistema tenta salvar mas banco falha
 *   10b. Sistema exibe mensagem "Erro ao salvar planta. Tente novamente."
 *   10c. Sistema registra erro no log técnico
 *   10d. Sistema mantém dados do formulário
 *   10e. Retorna ao passo 7
 * 
 * FE2: Timeout de sessão
 *   - Sistema detecta sessão expirada
 *   - Sistema salva rascunho (opcional)
 *   - Sistema redireciona para login
 */

// ============================================================================
// PARTE 3: REGRAS DE NEGÓCIO
// ============================================================================

/**
 * RN-001: Validação de Código
 * Descrição: O código da planta deve ser numérico, obrigatório e único no sistema
 * Implementação:
 * - Validação client-side: Regex /^[0-9]+$/
 * - Validação server-side: Integer.parseInt() + try-catch
 * - Constraint DB: UNIQUE INDEX no campo codigo
 * - Tipo: INT UNSIGNED ou BIGINT
 */

/**
 * RN-002: Unicidade de Código
 * Descrição: Não pode existir duas plantas com mesmo código
 * Implementação:
 * - Verificação antes do INSERT: SELECT COUNT(*) WHERE codigo = ?
 * - Constraint DB: UNIQUE INDEX
 * - Tratamento de exception: DataIntegrityViolationException (Spring)
 * - Mensagem ao usuário: "Código [X] já cadastrado"
 */

/**
 * RN-003: Validação de Descrição
 * Descrição: Descrição é opcional, alfanumérica, máximo 10 caracteres
 * Implementação:
 * - Validação: @Size(max=10) (Bean Validation)
 * - Tipo DB: VARCHAR(10) NULL
 * - Trimming: Remover espaços extras antes de salvar
 * - Aceita: letras, números, espaços, hífen
 */

/**
 * RN-004: Permissão de Deleção
 * Descrição: Apenas usuários com perfil ADMIN podem deletar plantas
 * Implementação:
 * - Anotação: @PreAuthorize("hasRole('ADMIN')") (Spring Security)
 * - Check no frontend: *ngIf="isAdmin" (Angular)
 * - Resposta HTTP: 403 Forbidden se não autorizado
 */

/**
 * RN-005: Soft Delete vs Hard Delete
 * Descrição: Se planta está em uso em fases posteriores, fazer soft delete
 * Implementação:
 * - Coluna: deleted_at TIMESTAMP NULL
 * - Queries: WHERE deleted_at IS NULL
 * - Ao deletar: UPDATE plantas SET deleted_at = NOW()
 * - Verificar dependências antes de hard delete
 */

/**
 * RN-006: Auditoria
 * Descrição: Toda operação (CRUD) deve ser auditada
 * Implementação:
 * - Colunas: created_at, created_by, updated_at, updated_by
 * - Framework: Envers (Hibernate) ou AuditingEntityListener (JPA)
 * - Log: Registrar operação, usuário, timestamp, IP
 */

// ============================================================================
// PARTE 4: VALIDAÇÕES E MEDIDAS DE SEGURANÇA
// ============================================================================

/**
 * VALIDAÇÕES CLIENT-SIDE (Angular/Frontend):
 * 
 * 1. Campo Código:
 *    - Tipo: input type="number" ou text com pattern
 *    - Required: required attribute
 *    - Pattern: [0-9]+
 *    - Mensagem imediata ao digitar letra
 * 
 * 2. Campo Descrição:
 *    - Maxlength: maxlength="10"
 *    - Contador de caracteres: "5/10"
 *    - Trim automático
 * 
 * 3. Formulário:
 *    - Botão "Salvar" desabilitado se inválido
 *    - Mensagens de erro inline
 *    - Validação on blur
 */

/**
 * VALIDAÇÕES SERVER-SIDE (Java/Backend):
 */

package exemplos.question7;

import javax.validation.constraints.*;
import javax.persistence.*;
import org.springframework.security.access.prepost.PreAuthorize;

@Entity
@Table(name = "plantas")
public class Planta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // RN-001: Código numérico, obrigatório, único
    @NotNull(message = "Código é obrigatório")
    @Column(nullable = false, unique = true)
    private Integer codigo;
    
    // RN-003: Descrição opcional, máx 10 caracteres
    @Size(max = 10, message = "Descrição deve ter no máximo 10 caracteres")
    @Column(length = 10)
    private String descricao;
    
    // RN-006: Auditoria
    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;
    
    @Column(nullable = false)
    private java.time.LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private String createdBy;
    
    @Column(nullable = false)
    private String updatedBy;
    
    // RN-005: Soft delete
    @Column
    private java.time.LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = java.time.LocalDateTime.now();
        // createdBy = SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
        // updatedBy = SecurityContextHolder.getContext().getAuthentication().getName();
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getCodigo() { return codigo; }
    public void setCodigo(Integer codigo) { this.codigo = codigo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { 
        // Trim e null safety
        this.descricao = descricao != null ? descricao.trim() : null;
    }
}

/**
 * SERVICE com Validações
 */
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlantaService {
    
    // Inject repository
    private final PlantaRepository repository;
    
    public PlantaService(PlantaRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Criar planta com validações
     */
    @Transactional
    public Planta criar(Planta planta) {
        // RN-001: Validar código numérico
        if (planta.getCodigo() == null || planta.getCodigo() <= 0) {
            throw new IllegalArgumentException("Código é obrigatório e deve ser positivo");
        }
        
        // RN-002: Verificar unicidade (mesmo com UNIQUE constraint, validar antes)
        if (repository.existsByCodigo(planta.getCodigo())) {
            throw new DuplicateCodeException("Código " + planta.getCodigo() + " já cadastrado");
        }
        
        // RN-003: Validar descrição
        if (planta.getDescricao() != null && planta.getDescricao().length() > 10) {
            throw new IllegalArgumentException("Descrição deve ter no máximo 10 caracteres");
        }
        
        return repository.save(planta);
    }
    
    /**
     * Atualizar planta
     */
    @Transactional
    public Planta atualizar(Long id, Planta plantaAtualizada) {
        Planta planta = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Planta não encontrada"));
        
        // Código não pode ser alterado (RN especial)
        // planta.setCodigo(plantaAtualizada.getCodigo()); // REMOVIDO
        
        // Apenas descrição pode ser alterada
        planta.setDescricao(plantaAtualizada.getDescricao());
        
        return repository.save(planta);
    }
    
    /**
     * Deletar planta (apenas admin)
     */
    @PreAuthorize("hasRole('ADMIN')") // RN-004
    @Transactional
    public void deletar(Long id) {
        Planta planta = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Planta não encontrada"));
        
        // RN-005: Verificar se está em uso
        if (plantaEstaEmUso(planta)) {
            // Soft delete
            planta.setDeletedAt(java.time.LocalDateTime.now());
            repository.save(planta);
        } else {
            // Hard delete
            repository.delete(planta);
        }
    }
    
    private boolean plantaEstaEmUso(Planta planta) {
        // Verificar se planta é referenciada em outras tabelas
        // Ex.: SELECT COUNT(*) FROM fase2_dados WHERE planta_id = ?
        return false; // Implementação específica
    }
}

/**
 * CONTROLLER com Validações
 */
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/plantas")
public class PlantaController {
    
    private final PlantaService service;
    
    public PlantaController(PlantaService service) {
        this.service = service;
    }
    
    @PostMapping
    public ResponseEntity<Planta> criar(@Valid @RequestBody Planta planta) {
        try {
            Planta created = service.criar(planta);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (DuplicateCodeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Planta>> listar(
        @RequestParam(required = false) String search
    ) {
        List<Planta> plantas = service.buscar(search);
        return ResponseEntity.ok(plantas);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Planta> atualizar(
        @PathVariable Long id,
        @Valid @RequestBody Planta planta
    ) {
        Planta updated = service.atualizar(id, planta);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

/**
 * MEDIDAS DE SEGURANÇA:
 * 
 * 1. AUTENTICAÇÃO E AUTORIZAÇÃO:
 *    - Spring Security com JWT
 *    - Role-based access control (ROLE_ADMIN, ROLE_USER)
 *    - @PreAuthorize em métodos sensíveis
 * 
 * 2. VALIDAÇÃO DE INPUT:
 *    - Bean Validation (@Valid, @NotNull, @Size)
 *    - Sanitização de strings (XSS prevention)
 *    - SQL Injection protection (PreparedStatement/JPA)
 * 
 * 3. AUDITORIA:
 *    - Log de todas as operações CRUD
 *    - Tracking de usuário e timestamp
 *    - IP address logging
 * 
 * 4. RATE LIMITING:
 *    - Limitar tentativas de criação (anti-spam)
 *    - Bucket4j ou Resilience4j
 * 
 * 5. HTTPS:
 *    - Comunicação criptografada
 *    - Certificado SSL/TLS
 */

// ============================================================================
// PARTE 5: ESTRATÉGIA DE TESTES
// ============================================================================

/**
 * TESTES UNITÁRIOS:
 * 
 * 1. Service Layer:
 *    - Criar planta válida
 *    - Criar com código duplicado (expect exception)
 *    - Criar com código null (expect exception)
 *    - Criar com descrição > 10 chars (expect exception)
 *    - Atualizar descrição
 *    - Deletar como admin (sucesso)
 *    - Deletar como user (expect security exception)
 * 
 * 2. Validation:
 *    - Código numérico
 *    - Código obrigatório
 *    - Descrição opcional
 *    - Descrição max length
 */

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlantaServiceTest {
    
    @Test
    void deveCriarPlantaValida() {
        Planta planta = new Planta();
        planta.setCodigo(12345);
        planta.setDescricao("Plant A");
        
        Planta created = service.criar(planta);
        
        assertNotNull(created.getId());
        assertEquals(12345, created.getCodigo());
    }
    
    @Test
    void deveFalharAoCriarComCodigoDuplicado() {
        // Setup: cria planta inicial
        Planta planta1 = new Planta();
        planta1.setCodigo(12345);
        service.criar(planta1);
        
        // Tenta criar duplicata
        Planta planta2 = new Planta();
        planta2.setCodigo(12345);
        
        assertThrows(DuplicateCodeException.class, () -> {
            service.criar(planta2);
        });
    }
    
    @Test
    void deveFalharComDescricaoMuitoLonga() {
        Planta planta = new Planta();
        planta.setCodigo(12345);
        planta.setDescricao("Esta descrição é muito longa");
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.criar(planta);
        });
    }
}

/**
 * TESTES DE INTEGRAÇÃO:
 * - Testar API completa (CRUD)
 * - Testar permissões (admin vs user)
 * - Testar constraints de banco
 * 
 * TESTES E2E:
 * - Cypress ou Selenium
 * - Fluxo completo: criar → editar → deletar
 * - Validações de formulário
 * 
 * EDGE CASES:
 * - Código máximo: Integer.MAX_VALUE
 * - Código zero ou negativo
 * - Descrição apenas com espaços
 * - Caracteres especiais na descrição
 * - Unicode/acentuação
 * - Concorrência (duas criações simultâneas)
 * - Timeout de sessão durante criação
 */

class DuplicateCodeException extends RuntimeException {
    public DuplicateCodeException(String message) {
        super(message);
    }
}

class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}

interface PlantaRepository {
    boolean existsByCodigo(Integer codigo);
    java.util.Optional<Planta> findById(Long id);
    Planta save(Planta planta);
    void delete(Planta planta);
    java.util.List<Planta> buscar(String search);
}
