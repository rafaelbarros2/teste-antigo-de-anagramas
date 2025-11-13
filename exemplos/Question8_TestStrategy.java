/**
 * QUESTÃO 8: Estratégia de Testes - User Registration System
 * 
 * FUNCIONALIDADE:
 * - Tela permite inserir, deletar e atualizar usuários
 * - Propriedades: name, email, address, phone
 * - Name e email são obrigatórios
 * - Email deve ser único
 * - Apenas admins podem deletar usuários
 * 
 * ============================================================================
 * PARTE 1: PIRÂMIDE DE TESTES - Tipos e Escopo
 * ============================================================================
 * 
 * PIRÂMIDE DE TESTES:
 * 
 *        /\
 *       /E2E\         ← 10% (Poucos, lentos, alto valor)
 *      /------\
 *     /Integr.\ ← 20% (Médio número, testa interação)
 *    /----------\
 *   / Unit Tests \    ← 70% (Muitos, rápidos, baixo custo)
 *  /--------------\
 * 
 * ============================================================================
 * TIPO 1: TESTES UNITÁRIOS (70% dos testes)
 * ============================================================================
 * 
 * ESCOPO: Testar classes isoladas (Service, Validation, etc.)
 * FERRAMENTAS: JUnit 5, Mockito, AssertJ
 * CARACTERÍSTICAS: Rápidos (<1s), sem dependências externas
 */

package exemplos.question8;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ============================================================================
 * 1.1 TESTES UNITÁRIOS - Validation Layer
 * ============================================================================
 */
class UserValidatorTest {
    
    private UserValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new UserValidator();
    }
    
    // ========================================================================
    // CENÁRIO 1: Validação de Name (Obrigatório)
    // ========================================================================
    
    @Test
    @DisplayName("Deve passar quando name é válido")
    void devePassarComNameValido() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        
        ValidationResult result = validator.validate(user);
        
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Deve falhar quando name é nulo, vazio ou apenas espaços")
    void deveFalharComNameInvalido(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail("john@example.com");
        
        ValidationResult result = validator.validate(user);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().containsKey("name"));
        assertEquals("Name é obrigatório", result.getErrors().get("name"));
    }
    
    @Test
    @DisplayName("Deve falhar quando name é muito longo (>100 caracteres)")
    void deveFalharComNameMuitoLongo() {
        User user = new User();
        user.setName("A".repeat(101)); // 101 caracteres
        user.setEmail("john@example.com");
        
        ValidationResult result = validator.validate(user);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().containsKey("name"));
    }
    
    // ========================================================================
    // CENÁRIO 2: Validação de Email (Obrigatório + Único + Formato)
    // ========================================================================
    
    @ParameterizedTest
    @ValueSource(strings = {
        "valid@example.com",
        "user.name@example.co.uk",
        "user+tag@example.com",
        "123@example.com"
    })
    @DisplayName("Deve passar com emails válidos")
    void devePassarComEmailsValidos(String email) {
        User user = new User();
        user.setName("John");
        user.setEmail(email);
        
        ValidationResult result = validator.validate(user);
        
        assertTrue(result.isValid());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "invalid",
        "@example.com",
        "user@",
        "user @example.com",
        "user@.com",
        ""
    })
    @DisplayName("Deve falhar com emails inválidos")
    void deveFalharComEmailsInvalidos(String email) {
        User user = new User();
        user.setName("John");
        user.setEmail(email);
        
        ValidationResult result = validator.validate(user);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().containsKey("email"));
    }
    
    @Test
    @DisplayName("Deve falhar quando email é nulo")
    void deveFalharComEmailNulo() {
        User user = new User();
        user.setName("John");
        user.setEmail(null);
        
        ValidationResult result = validator.validate(user);
        
        assertFalse(result.isValid());
        assertEquals("Email é obrigatório", result.getErrors().get("email"));
    }
    
    // ========================================================================
    // CENÁRIO 3: Validação de Phone (Opcional)
    // ========================================================================
    
    @Test
    @DisplayName("Deve passar quando phone é nulo (opcional)")
    void devePassarComPhoneNulo() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPhone(null);
        
        ValidationResult result = validator.validate(user);
        
        assertTrue(result.isValid());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        "(11) 98765-4321",
        "+55 11 98765-4321",
        "11987654321",
        "+1-555-123-4567"
    })
    @DisplayName("Deve passar com formatos de phone válidos")
    void devePassarComPhoneValido(String phone) {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPhone(phone);
        
        ValidationResult result = validator.validate(user);
        
        assertTrue(result.isValid());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"abc", "123", "phone"})
    @DisplayName("Deve falhar com phone inválido")
    void deveFalharComPhoneInvalido(String phone) {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setPhone(phone);
        
        ValidationResult result = validator.validate(user);
        
        assertFalse(result.isValid());
    }
    
    // ========================================================================
    // CENÁRIO 4: Validação de Address (Opcional)
    // ========================================================================
    
    @Test
    @DisplayName("Deve passar com address nulo")
    void devePassarComAddressNulo() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setAddress(null);
        
        ValidationResult result = validator.validate(user);
        
        assertTrue(result.isValid());
    }
}

/**
 * ============================================================================
 * 1.2 TESTES UNITÁRIOS - Service Layer (com Mocks)
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository repository;
    
    @Mock
    private UserValidator validator;
    
    @InjectMocks
    private UserService service;
    
    // ========================================================================
    // CENÁRIO 5: Criar Usuário (Happy Path)
    // ========================================================================
    
    @Test
    @DisplayName("Deve criar usuário válido com sucesso")
    void deveCriarUsuarioValido() {
        // Arrange
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        
        ValidationResult validResult = new ValidationResult(true);
        when(validator.validate(user)).thenReturn(validResult);
        when(repository.existsByEmail(user.getEmail())).thenReturn(false);
        when(repository.save(user)).thenReturn(user);
        
        // Act
        User created = service.createUser(user);
        
        // Assert
        assertNotNull(created);
        assertEquals("John Doe", created.getName());
        verify(validator).validate(user);
        verify(repository).existsByEmail(user.getEmail());
        verify(repository).save(user);
    }
    
    // ========================================================================
    // CENÁRIO 6: Email Duplicado (Unicidade)
    // ========================================================================
    
    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email duplicado")
    void deveFalharComEmailDuplicado() {
        // Arrange
        User user = new User();
        user.setEmail("john@example.com");
        user.setName("John");
        
        ValidationResult validResult = new ValidationResult(true);
        when(validator.validate(user)).thenReturn(validResult);
        when(repository.existsByEmail(user.getEmail())).thenReturn(true); // Email já existe
        
        // Act & Assert
        DuplicateEmailException exception = assertThrows(
            DuplicateEmailException.class,
            () -> service.createUser(user)
        );
        
        assertEquals("Email john@example.com já está cadastrado", exception.getMessage());
        verify(repository, never()).save(any()); // Não deve salvar
    }
    
    // ========================================================================
    // CENÁRIO 7: Atualizar Usuário
    // ========================================================================
    
    @Test
    @DisplayName("Deve atualizar usuário existente")
    void deveAtualizarUsuario() {
        // Arrange
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@example.com");
        existingUser.setName("Old Name");
        
        User updatedData = new User();
        updatedData.setName("New Name");
        updatedData.setEmail("old@example.com"); // Mantém email
        updatedData.setAddress("New Address");
        
        when(repository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        when(validator.validate(any())).thenReturn(new ValidationResult(true));
        when(repository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        
        // Act
        User updated = service.updateUser(userId, updatedData);
        
        // Assert
        assertEquals("New Name", updated.getName());
        assertEquals("New Address", updated.getAddress());
        verify(repository).save(existingUser);
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao atualizar usuário inexistente")
    void deveFalharAoAtualizarUsuarioInexistente() {
        // Arrange
        Long userId = 999L;
        User updatedData = new User();
        
        when(repository.findById(userId)).thenReturn(java.util.Optional.empty());
        
        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            service.updateUser(userId, updatedData);
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao atualizar com email já usado por outro usuário")
    void deveFalharAoAtualizarComEmailDuplicado() {
        // Arrange
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("user1@example.com");
        
        User updatedData = new User();
        updatedData.setEmail("user2@example.com"); // Email de outro usuário
        
        when(repository.findById(userId)).thenReturn(java.util.Optional.of(existingUser));
        when(repository.existsByEmailAndIdNot("user2@example.com", userId)).thenReturn(true);
        
        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> {
            service.updateUser(userId, updatedData);
        });
    }
    
    // ========================================================================
    // CENÁRIO 8: Deletar Usuário (Apenas Admin)
    // ========================================================================
    
    @Test
    @DisplayName("Admin deve conseguir deletar usuário")
    void adminDeveConseguirDeletar() {
        // Arrange
        Long userId = 1L;
        User adminUser = new User();
        adminUser.setRole(UserRole.ADMIN);
        
        User userToDelete = new User();
        userToDelete.setId(userId);
        
        when(repository.findById(userId)).thenReturn(java.util.Optional.of(userToDelete));
        
        // Act
        service.deleteUser(userId, adminUser);
        
        // Assert
        verify(repository).delete(userToDelete);
    }
    
    @Test
    @DisplayName("Usuário comum NÃO deve conseguir deletar")
    void usuarioComumNaoDeveConseguirDeletar() {
        // Arrange
        Long userId = 1L;
        User regularUser = new User();
        regularUser.setRole(UserRole.USER);
        
        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            service.deleteUser(userId, regularUser);
        });
        
        verify(repository, never()).delete(any());
    }
    
    @Test
    @DisplayName("Não deve deletar usuário inexistente")
    void naoDeveDeletarUsuarioInexistente() {
        // Arrange
        Long userId = 999L;
        User adminUser = new User();
        adminUser.setRole(UserRole.ADMIN);
        
        when(repository.findById(userId)).thenReturn(java.util.Optional.empty());
        
        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            service.deleteUser(userId, adminUser);
        });
    }
}

/**
 * ============================================================================
 * TIPO 2: TESTES DE INTEGRAÇÃO (20% dos testes)
 * ============================================================================
 * 
 * ESCOPO: Testar interação entre camadas (Controller → Service → Repository → DB)
 * FERRAMENTAS: Spring Boot Test, TestContainers, MockMvc
 * CARACTERÍSTICAS: Médios (1-5s), usa banco de dados real ou em memória
 */

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback após cada teste
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository repository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }
    
    // ========================================================================
    // CENÁRIO 9: API - Criar Usuário (E2E)
    // ========================================================================
    
    @Test
    @DisplayName("POST /users - Deve criar usuário válido (201 Created)")
    void deveCriarUsuarioViaApi() throws Exception {
        // Arrange
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPhone("11987654321");
        user.setAddress("123 Main St");
        
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.email").value("john@example.com"));
        
        // Verifica que foi salvo no banco
        assertEquals(1, repository.count());
    }
    
    @Test
    @DisplayName("POST /users - Deve retornar 400 com campos obrigatórios vazios")
    void deveFalharComCamposVazios() throws Exception {
        User user = new User();
        // Name e email vazios
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.email").exists());
    }
    
    @Test
    @DisplayName("POST /users - Deve retornar 409 com email duplicado")
    void deveFalharComEmailDuplicadoViaApi() throws Exception {
        // Arrange: cria usuário existente
        User existing = new User();
        existing.setName("Existing");
        existing.setEmail("john@example.com");
        repository.save(existing);
        
        // Act: tenta criar com mesmo email
        User duplicate = new User();
        duplicate.setName("New User");
        duplicate.setEmail("john@example.com");
        
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicate)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("Email já cadastrado"));
    }
    
    // ========================================================================
    // CENÁRIO 10: API - Atualizar Usuário
    // ========================================================================
    
    @Test
    @DisplayName("PUT /users/{id} - Deve atualizar usuário existente")
    void deveAtualizarUsuarioViaApi() throws Exception {
        // Arrange: cria usuário
        User user = new User();
        user.setName("Old Name");
        user.setEmail("old@example.com");
        User saved = repository.save(user);
        
        // Act: atualiza
        User updated = new User();
        updated.setName("New Name");
        updated.setEmail("old@example.com");
        updated.setAddress("New Address");
        
        mockMvc.perform(put("/api/users/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("New Name"))
            .andExpect(jsonPath("$.address").value("New Address"));
    }
    
    @Test
    @DisplayName("PUT /users/{id} - Deve retornar 404 para usuário inexistente")
    void deveFalharAoAtualizarUsuarioInexistente() throws Exception {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        
        mockMvc.perform(put("/api/users/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isNotFound());
    }
    
    // ========================================================================
    // CENÁRIO 11: API - Deletar Usuário (Permissões)
    // ========================================================================
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /users/{id} - Admin deve conseguir deletar")
    void adminDeveConseguirDeletarViaApi() throws Exception {
        // Arrange
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        User saved = repository.save(user);
        
        // Act
        mockMvc.perform(delete("/api/users/" + saved.getId()))
            .andExpect(status().isNoContent());
        
        // Assert
        assertFalse(repository.existsById(saved.getId()));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /users/{id} - Usuário comum NÃO deve conseguir deletar")
    void usuarioComumNaoDeveConseguirDeletarViaApi() throws Exception {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        User saved = repository.save(user);
        
        mockMvc.perform(delete("/api/users/" + saved.getId()))
            .andExpect(status().isForbidden());
        
        // Usuário ainda existe
        assertTrue(repository.existsById(saved.getId()));
    }
    
    // ========================================================================
    // CENÁRIO 12: API - Listar/Buscar Usuários
    // ========================================================================
    
    @Test
    @DisplayName("GET /users - Deve listar todos os usuários")
    void deveListarUsuarios() throws Exception {
        // Arrange: cria múltiplos usuários
        repository.save(createUser("User 1", "user1@example.com"));
        repository.save(createUser("User 2", "user2@example.com"));
        repository.save(createUser("User 3", "user3@example.com"));
        
        // Act & Assert
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3));
    }
    
    @Test
    @DisplayName("GET /users?search=john - Deve buscar por nome")
    void deveBuscarPorNome() throws Exception {
        repository.save(createUser("John Doe", "john@example.com"));
        repository.save(createUser("Jane Smith", "jane@example.com"));
        repository.save(createUser("John Smith", "john2@example.com"));
        
        mockMvc.perform(get("/api/users").param("search", "John"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }
    
    private User createUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }
}

/**
 * ============================================================================
 * TIPO 3: TESTES END-TO-END (E2E) (10% dos testes)
 * ============================================================================
 * 
 * ESCOPO: Testar fluxo completo do ponto de vista do usuário (frontend + backend)
 * FERRAMENTAS: Selenium, Cypress, Playwright
 * CARACTERÍSTICAS: Lentos (10s+), flaky, alto custo de manutenção
 */

/**
 * Exemplo em Cypress (JavaScript):
 * 
 * describe('User Registration E2E', () => {
 *   
 *   beforeEach(() => {
 *     cy.visit('/users');
 *     cy.login('admin@example.com', 'password'); // Login como admin
 *   });
 *   
 *   it('Deve criar novo usuário através da UI', () => {
 *     // Clica em "Novo Usuário"
 *     cy.get('[data-testid="new-user-btn"]').click();
 *     
 *     // Preenche formulário
 *     cy.get('[name="name"]').type('John Doe');
 *     cy.get('[name="email"]').type('john@example.com');
 *     cy.get('[name="phone"]').type('11987654321');
 *     cy.get('[name="address"]').type('123 Main St');
 *     
 *     // Salva
 *     cy.get('[data-testid="save-btn"]').click();
 *     
 *     // Verifica sucesso
 *     cy.get('.success-message').should('contain', 'Usuário criado com sucesso');
 *     cy.get('[data-testid="user-list"]').should('contain', 'John Doe');
 *   });
 *   
 *   it('Deve exibir erro ao criar com email duplicado', () => {
 *     // Cria usuário
 *     cy.createUser({ name: 'Existing', email: 'john@example.com' });
 *     
 *     // Tenta criar duplicata via UI
 *     cy.get('[data-testid="new-user-btn"]').click();
 *     cy.get('[name="name"]').type('New User');
 *     cy.get('[name="email"]').type('john@example.com');
 *     cy.get('[data-testid="save-btn"]').click();
 *     
 *     // Verifica erro
 *     cy.get('.error-message').should('contain', 'Email já cadastrado');
 *   });
 *   
 *   it('Admin deve conseguir deletar usuário', () => {
 *     cy.createUser({ name: 'To Delete', email: 'delete@example.com' });
 *     
 *     cy.get('[data-testid="user-row"]').contains('To Delete')
 *       .parent()
 *       .find('[data-testid="delete-btn"]').click();
 *     
 *     cy.get('[data-testid="confirm-delete"]').click();
 *     
 *     cy.get('.success-message').should('contain', 'Usuário deletado');
 *     cy.get('[data-testid="user-list"]').should('not.contain', 'To Delete');
 *   });
 *   
 *   it('Usuário comum NÃO deve ver botão deletar', () => {
 *     cy.logout();
 *     cy.login('user@example.com', 'password'); // Login como user
 *     
 *     cy.visit('/users');
 *     cy.get('[data-testid="delete-btn"]').should('not.exist');
 *   });
 * });
 */

/**
 * ============================================================================
 * EDGE CASES E CENÁRIOS ESPECIAIS
 * ============================================================================
 */

class UserServiceEdgeCasesTest {
    
    @Test
    @DisplayName("EDGE CASE: Nome com caracteres especiais e acentuação")
    void deveAceitarNomeComAcentuacao() {
        User user = new User();
        user.setName("José María Peñã");
        user.setEmail("jose@example.com");
        
        User created = service.createUser(user);
        
        assertEquals("José María Peñã", created.getName());
    }
    
    @Test
    @DisplayName("EDGE CASE: Email com +tags (gmail alias)")
    void deveAceitarEmailComTags() {
        User user = new User();
        user.setName("John");
        user.setEmail("john+test@example.com");
        
        User created = service.createUser(user);
        
        assertEquals("john+test@example.com", created.getEmail());
    }
    
    @Test
    @DisplayName("EDGE CASE: Nome com apenas 1 caractere")
    void deveAceitarNomeCurto() {
        User user = new User();
        user.setName("A");
        user.setEmail("a@example.com");
        
        User created = service.createUser(user);
        
        assertEquals("A", created.getName());
    }
    
    @Test
    @DisplayName("EDGE CASE: Nome no tamanho máximo (100 caracteres)")
    void deveAceitarNomeMaximo() {
        User user = new User();
        user.setName("A".repeat(100));
        user.setEmail("john@example.com");
        
        User created = service.createUser(user);
        
        assertEquals(100, created.getName().length());
    }
    
    @Test
    @DisplayName("EDGE CASE: Atualizar usuário mantendo mesmo email")
    void devePermitirAtualizarMantendomesmoEmail() {
        User user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(repository.existsByEmailAndIdNot("john@example.com", 1L)).thenReturn(false);
        
        User updated = new User();
        updated.setEmail("john@example.com"); // Mesmo email
        updated.setName("New Name");
        
        assertDoesNotThrow(() -> service.updateUser(1L, updated));
    }
    
    @Test
    @DisplayName("EDGE CASE: Trimming de espaços em branco")
    void deveFazerTrimmingDeEspacos() {
        User user = new User();
        user.setName("  John Doe  ");
        user.setEmail("  john@example.com  ");
        
        User created = service.createUser(user);
        
        assertEquals("John Doe", created.getName());
        assertEquals("john@example.com", created.getEmail());
    }
    
    @Test
    @DisplayName("EDGE CASE: Concorrência - Dois usuários criando com mesmo email")
    void devePrevenirConcorrenciaNaCriacao() {
        // Simula race condition com CountDownLatch ou ExecutorService
        ExecutorService executor = Executors.newFixedThreadPool(2);
        
        CompletableFuture<User> future1 = CompletableFuture.supplyAsync(() -> {
            User user = new User();
            user.setName("User 1");
            user.setEmail("concurrent@example.com");
            return service.createUser(user);
        }, executor);
        
        CompletableFuture<User> future2 = CompletableFuture.supplyAsync(() -> {
            User user = new User();
            user.setName("User 2");
            user.setEmail("concurrent@example.com");
            return service.createUser(user);
        }, executor);
        
        // Um deve suceder, outro deve falhar
        assertThrows(Exception.class, () -> {
            CompletableFuture.allOf(future1, future2).join();
        });
    }
    
    @Test
    @DisplayName("EDGE CASE: SQL Injection attempt no email")
    void deveBloquearSqlInjection() {
        User user = new User();
        user.setName("John");
        user.setEmail("'; DROP TABLE users; --");
        
        // Deve falhar na validação de formato, não executar SQL
        ValidationResult result = validator.validate(user);
        
        assertFalse(result.isValid());
    }
    
    @Test
    @DisplayName("EDGE CASE: XSS attempt no nome")
    void deveBloquearXss() {
        User user = new User();
        user.setName("<script>alert('XSS')</script>");
        user.setEmail("john@example.com");
        
        User created = service.createUser(user);
        
        // Nome deve ser sanitizado ou escapado
        assertFalse(created.getName().contains("<script>"));
    }
}

/**
 * ============================================================================
 * RESUMO DA ESTRATÉGIA DE TESTES
 * ============================================================================
 * 
 * COBERTURA POR TIPO:
 * 
 * ✅ TESTES UNITÁRIOS (70%):
 *    - UserValidator: validações de todos os campos
 *    - UserService: lógica de negócio isolada
 *    - Mocks para dependências (repository, etc.)
 *    - Rápidos (<1s por teste)
 *    - ~50 testes
 * 
 * ✅ TESTES DE INTEGRAÇÃO (20%):
 *    - Controller → Service → Repository → DB
 *    - Validações de API (status codes, JSON)
 *    - Testes de segurança (permissões)
 *    - Banco em memória (H2) ou TestContainers
 *    - ~15 testes
 * 
 * ✅ TESTES E2E (10%):
 *    - Fluxos críticos do usuário
 *    - UI + Backend juntos
 *    - Cypress/Selenium
 *    - ~5 testes
 * 
 * CENÁRIOS COBERTOS:
 * 
 * 1. ✅ Campos obrigatórios (name, email)
 * 2. ✅ Validação de formato (email, phone)
 * 3. ✅ Unicidade de email
 * 4. ✅ Permissões (apenas admin deleta)
 * 5. ✅ CRUD completo
 * 6. ✅ Busca e listagem
 * 7. ✅ Edge cases (nomes especiais, tamanhos limites)
 * 8. ✅ Segurança (SQL Injection, XSS)
 * 9. ✅ Concorrência
 * 10. ✅ Erros (404, 409, 403)
 * 
 * MÉTRICAS DE QUALIDADE:
 * - Cobertura de código: >80%
 * - Cobertura de branches: >70%
 * - Mutation testing score: >75%
 * - Todos os testes devem passar em <2min
 * 
 * FERRAMENTAS:
 * - JUnit 5 + Mockito (unit)
 * - Spring Boot Test (integration)
 * - TestContainers (banco real)
 * - Cypress (E2E)
 * - JaCoCo (cobertura)
 * - PIT (mutation testing)
 */

// Classes auxiliares
class User {
    private Long id;
    private String name;
    private String email;
    private String address;
    private String phone;
    private UserRole role;
    
    // Getters e Setters omitidos por brevidade
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}

enum UserRole { ADMIN, USER }

class ValidationResult {
    private boolean valid;
    private Map<String, String> errors = new HashMap<>();
    
    public ValidationResult(boolean valid) { this.valid = valid; }
    public boolean isValid() { return valid; }
    public Map<String, String> getErrors() { return errors; }
}

class UserValidator {
    public ValidationResult validate(User user) { return new ValidationResult(true); }
}

class UserService {
    public User createUser(User user) { return user; }
    public User updateUser(Long id, User user) { return user; }
    public void deleteUser(Long id, User currentUser) {}
}

interface UserRepository {
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    User save(User user);
    java.util.Optional<User> findById(Long id);
    void delete(User user);
    void deleteAll();
    long count();
    boolean existsById(Long id);
}

class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) { super(message); }
}

class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) { super(message); }
}

class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) { super(message); }
}
