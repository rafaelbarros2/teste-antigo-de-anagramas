package exemplos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import javax.sql.DataSource;

/**
 * QUESTÃO 4: Técnicas para Prevenir SQL Injection
 * 
 * SQL INJECTION é uma das vulnerabilidades mais críticas (OWASP Top 10).
 * Ocorre quando entrada do usuário é concatenada diretamente na query SQL,
 * permitindo que atacantes executem comandos maliciosos no banco de dados.
 * 
 * ============================================================================
 * TÉCNICAS DE PREVENÇÃO:
 * ============================================================================
 * 
 * 1. ✅ PREPARED STATEMENTS (Parametrized Queries) - RECOMENDADO
 * 2. ✅ ORMs (JPA/Hibernate, MyBatis) com Named Parameters
 * 3. ✅ Stored Procedures (com parâmetros)
 * 4. ✅ Validação e Sanitização de Input (camada adicional)
 * 5. ✅ Principle of Least Privilege (permissões BD)
 * 6. ✅ WAF (Web Application Firewall)
 * 7. ✅ Escape de caracteres especiais (último recurso)
 * 8. ✅ Whitelisting de inputs
 */
public class Question4_SqlInjectionPrevention {

    // ========================================================================
    // ❌ EXEMPLO VULNERÁVEL - NUNCA FAÇA ISSO!
    // ========================================================================
    
    /**
     * VULNERÁVEL: Concatenação direta de input do usuário.
     * 
     * ATAQUE POSSÍVEL:
     * username = "admin' OR '1'='1"
     * password = "anything"
     * 
     * Query resultante:
     * SELECT * FROM users WHERE username='admin' OR '1'='1' AND password='anything'
     * 
     * Resultado: Bypass de autenticação (sempre retorna true)
     */
    public User loginVulnerable(String username, String password) throws SQLException {
        Connection conn = getConnection();
        
        // ❌ VULNERÁVEL - String concatenation
        String sql = "SELECT * FROM users WHERE username='" + username + 
                     "' AND password='" + password + "'";
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        if (rs.next()) {
            return new User(rs.getInt("id"), rs.getString("username"));
        }
        return null;
    }
    
    /**
     * Outro exemplo vulnerável:
     * input = "'; DROP TABLE users; --"
     * 
     * Query resultante:
     * SELECT * FROM products WHERE name=''; DROP TABLE users; --'
     * 
     * Resultado: Tabela users deletada!
     */
    public List<Product> searchProductsVulnerable(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM products WHERE name LIKE '%" + searchTerm + "%'";
        // ❌ VULNERÁVEL ao SQL Injection
        return executeQuery(sql);
    }

    // ========================================================================
    // ✅ TÉCNICA 1: PREPARED STATEMENTS (Solução Recomendada)
    // ========================================================================
    
    /**
     * ✅ SEGURO: Usa PreparedStatement com placeholders (?)
     * 
     * Como funciona:
     * 1. SQL é compilado ANTES de receber os parâmetros
     * 2. Parâmetros são tratados como DADOS, nunca como CÓDIGO
     * 3. Driver JDBC faz escape automático de caracteres especiais
     * 
     * Mesmo se username = "admin' OR '1'='1", será tratado como string literal.
     */
    public User loginSecure(String username, String password) throws SQLException {
        Connection conn = getConnection();
        
        // ✅ SEGURO: PreparedStatement com placeholders
        String sql = "SELECT id, username, email FROM users WHERE username = ? AND password = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Parâmetros são setados de forma segura
            pstmt.setString(1, username);  // índice baseado em 1
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email")
                );
            }
        }
        
        return null;
    }
    
    /**
     * ✅ SEGURO: Busca com LIKE usando PreparedStatement
     * 
     * IMPORTANTE: O wildcard (%) é incluído NO valor do parâmetro,
     * NÃO na query SQL.
     */
    public List<Product> searchProductsSecure(String searchTerm) throws SQLException {
        String sql = "SELECT id, name, price FROM products WHERE name LIKE ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Adiciona wildcards ao parâmetro, não à query
            pstmt.setString(1, "%" + searchTerm + "%");
            
            ResultSet rs = pstmt.executeQuery();
            List<Product> products = new ArrayList<>();
            
            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price")
                ));
            }
            
            return products;
        }
    }
    
    /**
     * ✅ SEGURO: INSERT com PreparedStatement
     */
    public int createUserSecure(String username, String email, String password) throws SQLException {
        String sql = "INSERT INTO users (username, email, password, created_at) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashPassword(password)); // Hash antes de salvar!
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Retorna ID gerado
                }
            }
        }
        
        return -1;
    }
    
    /**
     * ✅ SEGURO: UPDATE com PreparedStatement
     */
    public boolean updateUserEmailSecure(int userId, String newEmail) throws SQLException {
        String sql = "UPDATE users SET email = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newEmail);
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(3, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * ✅ SEGURO: DELETE com PreparedStatement
     */
    public boolean deleteUserSecure(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // ========================================================================
    // ✅ TÉCNICA 2: JPA/HIBERNATE (ORM) - Named Parameters
    // ========================================================================
    
    /**
     * ✅ SEGURO: JPA com Named Parameters
     * 
     * ORMs abstraem SQL e usam parametrização por padrão.
     * NUNCA use concatenação mesmo com JPA!
     */
    @PersistenceContext
    private EntityManager entityManager;
    
    public User findUserByUsernameJPA(String username) {
        // ✅ SEGURO: Named parameter
        String jpql = "SELECT u FROM User u WHERE u.username = :username";
        
        try {
            return entityManager.createQuery(jpql, User.class)
                .setParameter("username", username) // Parametrizado
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    /**
     * ✅ SEGURO: JPQL com múltiplos parâmetros
     */
    public List<Product> findProductsByPriceRangeJPA(double minPrice, double maxPrice) {
        String jpql = "SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max ORDER BY p.price";
        
        return entityManager.createQuery(jpql, Product.class)
            .setParameter("min", minPrice)
            .setParameter("max", maxPrice)
            .getResultList();
    }
    
    /**
     * ✅ SEGURO: Native Query com parâmetros (quando JPQL não é suficiente)
     */
    public List<User> findActiveUsersNativeQuery() {
        String sql = "SELECT * FROM users WHERE status = :status AND last_login > :cutoffDate";
        
        return entityManager.createNativeQuery(sql, User.class)
            .setParameter("status", "ACTIVE")
            .setParameter("cutoffDate", getDateDaysAgo(30))
            .getResultList();
    }
    
    /**
     * ❌ VULNERÁVEL mesmo com JPA - NUNCA concatene strings!
     */
    public List<User> searchUsersVulnerableJPA(String searchTerm) {
        // ❌ VULNERÁVEL: String concatenation em JPQL
        String jpql = "SELECT u FROM User u WHERE u.username LIKE '%" + searchTerm + "%'";
        return entityManager.createQuery(jpql, User.class).getResultList();
    }
    
    /**
     * ✅ SEGURO: Versão correta do search
     */
    public List<User> searchUsersSecureJPA(String searchTerm) {
        String jpql = "SELECT u FROM User u WHERE u.username LIKE :searchTerm";
        
        return entityManager.createQuery(jpql, User.class)
            .setParameter("searchTerm", "%" + searchTerm + "%")
            .getResultList();
    }

    // ========================================================================
    // ✅ TÉCNICA 3: STORED PROCEDURES com Parâmetros
    // ========================================================================
    
    /**
     * ✅ SEGURO: Chamada de Stored Procedure com parâmetros
     * 
     * Stored Procedure no banco:
     * 
     * CREATE PROCEDURE sp_create_user(
     *     IN p_username VARCHAR(50),
     *     IN p_email VARCHAR(100),
     *     IN p_password VARCHAR(255),
     *     OUT p_user_id INT
     * )
     * BEGIN
     *     INSERT INTO users (username, email, password, created_at)
     *     VALUES (p_username, p_email, p_password, NOW());
     *     SET p_user_id = LAST_INSERT_ID();
     * END;
     */
    public int createUserStoredProcedure(String username, String email, String password) throws SQLException {
        String sql = "{CALL sp_create_user(?, ?, ?, ?)}";
        
        try (Connection conn = getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            // Parâmetros IN
            cstmt.setString(1, username);
            cstmt.setString(2, email);
            cstmt.setString(3, hashPassword(password));
            
            // Parâmetro OUT
            cstmt.registerOutParameter(4, Types.INTEGER);
            
            cstmt.execute();
            
            return cstmt.getInt(4); // Retorna user_id
        }
    }

    // ========================================================================
    // ✅ TÉCNICA 4: VALIDAÇÃO E SANITIZAÇÃO DE INPUT
    // ========================================================================
    
    /**
     * CAMADA ADICIONAL DE SEGURANÇA (não substitui PreparedStatement!)
     * 
     * Validações:
     * - Whitelist de caracteres permitidos
     * - Tamanho máximo
     * - Formato esperado (regex)
     * - Tipos de dados corretos
     */
    public User loginWithValidation(String username, String password) throws SQLException {
        // 1. Validação de entrada
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Username inválido");
        }
        
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Password inválido");
        }
        
        // 2. Usa PreparedStatement (principal defesa)
        return loginSecure(username, password);
    }
    
    /**
     * Validação de username: apenas alfanuméricos, underscore e hífen
     */
    private boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        
        // Tamanho entre 3 e 50 caracteres
        if (username.length() < 3 || username.length() > 50) {
            return false;
        }
        
        // Apenas caracteres permitidos
        return username.matches("^[a-zA-Z0-9_-]+$");
    }
    
    /**
     * Validação de password
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        // Mínimo 8 caracteres
        return password.length() >= 8;
    }
    
    /**
     * Validação de email com regex
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Whitelisting para ordenação (ORDER BY)
     * 
     * ORDER BY não aceita parâmetros em PreparedStatement,
     * então usa-se whitelist.
     */
    public List<User> getUsersSorted(String sortField, String sortOrder) throws SQLException {
        // Whitelist de campos permitidos
        List<String> allowedFields = List.of("id", "username", "email", "created_at");
        List<String> allowedOrders = List.of("ASC", "DESC");
        
        if (!allowedFields.contains(sortField)) {
            sortField = "id"; // Default seguro
        }
        
        if (!allowedOrders.contains(sortOrder.toUpperCase())) {
            sortOrder = "ASC"; // Default seguro
        }
        
        // Agora é seguro concatenar (whitelist validada)
        String sql = "SELECT id, username, email FROM users ORDER BY " + sortField + " " + sortOrder;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email")
                ));
            }
            return users;
        }
    }

    // ========================================================================
    // ✅ TÉCNICA 5: MEDIDAS ADICIONAIS DE SEGURANÇA
    // ========================================================================
    
    /**
     * CONFIGURAÇÕES DE BANCO DE DADOS:
     * 
     * 1. PRINCIPLE OF LEAST PRIVILEGE
     *    - Usuário da aplicação NÃO deve ser admin/root
     *    - Apenas permissões necessárias (SELECT, INSERT, UPDATE, DELETE específicos)
     *    - Sem permissões de DROP, ALTER, CREATE
     * 
     * 2. CONTA SEPARADA POR AMBIENTE
     *    - user_app_dev, user_app_prod com permissões diferentes
     * 
     * 3. CONEXÃO SEGURA
     *    - SSL/TLS para conexões de banco
     *    - Credenciais em variáveis de ambiente, não no código
     * 
     * 4. LOGGING E AUDITORIA
     *    - Log de queries suspeitas
     *    - Alertas para múltiplas tentativas de login
     * 
     * 5. RATE LIMITING
     *    - Limitar tentativas de login por IP
     *    - Throttling de queries
     */
    
    /**
     * Exemplo de configuração segura de DataSource (Spring Boot)
     */
    /*
    @Configuration
    public class DataSourceConfig {
        
        @Bean
        public DataSource dataSource() {
            HikariConfig config = new HikariConfig();
            
            // Credenciais de variáveis de ambiente
            config.setJdbcUrl(System.getenv("DB_URL"));
            config.setUsername(System.getenv("DB_USER"));
            config.setPassword(System.getenv("DB_PASSWORD"));
            
            // SSL habilitado
            config.addDataSourceProperty("useSSL", "true");
            config.addDataSourceProperty("requireSSL", "true");
            
            // Connection pooling
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);
            
            return new HikariDataSource(config);
        }
    }
    */
    
    /**
     * Hash de password (NUNCA armazene senhas em plain text!)
     */
    private String hashPassword(String password) {
        // Use BCrypt, Argon2, ou PBKDF2
        // Exemplo simplificado (use biblioteca real em produção)
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer hash da senha", e);
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    // Métodos auxiliares
    private Connection getConnection() throws SQLException {
        // Implementação real usaria DataSource/Connection Pool
        return DriverManager.getConnection(
            System.getenv("DB_URL"),
            System.getenv("DB_USER"),
            System.getenv("DB_PASSWORD")
        );
    }
    
    private List<Product> executeQuery(String sql) throws SQLException {
        // Método auxiliar simplificado
        return new ArrayList<>();
    }
    
    private java.sql.Date getDateDaysAgo(int days) {
        return new java.sql.Date(System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000));
    }
}

// ========================================================================
// CLASSES DE MODELO
// ========================================================================

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String email;
    
    public User() {}
    
    public User(Integer id, String username) {
        this.id = id;
        this.username = username;
    }
    
    public User(Integer id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
    
    // Getters e Setters
    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}

@Entity
@Table(name = "products")
class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private Double price;
    
    public Product() {}
    
    public Product(Integer id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    
    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
}

/**
 * ============================================================================
 * CHECKLIST DE SEGURANÇA SQL:
 * ============================================================================
 * 
 * ✅ Sempre use PreparedStatement ou parâmetros nomeados (JPA)
 * ✅ NUNCA concatene strings com input do usuário
 * ✅ Valide e sanitize todos os inputs
 * ✅ Use whitelist para ORDER BY e nomes de tabelas dinâmicos
 * ✅ Implemente least privilege no banco de dados
 * ✅ Use ORM (JPA/Hibernate) quando possível
 * ✅ Hash de senhas (BCrypt, Argon2)
 * ✅ Conexões SSL/TLS para banco de dados
 * ✅ Logging e monitoramento de queries suspeitas
 * ✅ Rate limiting e throttling
 * ✅ Code review focado em segurança
 * ✅ Testes de segurança (SAST, DAST, penetration testing)
 * ✅ Mantenha dependências atualizadas
 * 
 * FERRAMENTAS ÚTEIS:
 * - OWASP Dependency Check
 * - SonarQube (detecta SQL injection)
 * - Snyk
 * - Burp Suite (pen testing)
 * - SQLMap (testar vulnerabilidades)
 */
