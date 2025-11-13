/**
 * QUESTÃO 5: Diagnóstico e Otimização de Batch Process (DB + FTP)
 * 
 * CENÁRIO:
 * Sistema batch que:
 * 1. Lê dados de banco de dados (ex.: pedidos do dia)
 * 2. Gera arquivos (CSV, XML, etc.)
 * 3. Envia arquivos via FTP para parceiros
 * 
 * PROBLEMA: Processo lento, impactando janela de processamento
 * 
 * ============================================================================
 * METODOLOGIA DE DIAGNÓSTICO (5 FASES)
 * ============================================================================
 */

// ========================================================================
// FASE 1: MEDIÇÃO E PROFILING (Identificar Gargalos)
// ========================================================================

/**
 * FERRAMENTAS E TÉCNICAS:
 * 
 * 1. APPLICATION PROFILING:
 *    - Java Mission Control (JMC) + Flight Recorder
 *    - VisualVM
 *    - YourKit Profiler
 *    - IntelliJ Profiler
 * 
 * 2. DATABASE PROFILING:
 *    - Query Execution Plans (EXPLAIN ANALYZE)
 *    - Database slow query log
 *    - pg_stat_statements (PostgreSQL)
 *    - MySQL Performance Schema
 *    - Oracle AWR/ASH reports
 * 
 * 3. NETWORK MONITORING:
 *    - Wireshark (análise de pacotes FTP)
 *    - tcpdump
 *    - FTP server logs
 * 
 * 4. SYSTEM MONITORING:
 *    - CPU, Memory, Disk I/O (top, htop, iostat)
 *    - Thread dumps (jstack)
 *    - Heap dumps (jmap, Eclipse MAT)
 * 
 * 5. APPLICATION METRICS:
 *    - Micrometer + Prometheus
 *    - Spring Boot Actuator
 *    - Custom timers e counters
 */

package exemplos;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import org.apache.commons.net.ftp.FTPClient;

public class Question5_BatchOptimization {

    // ====================================================================
    // VERSÃO PROBLEMÁTICA (Identificando Anti-Patterns)
    // ====================================================================
    
    /**
     * ❌ VERSÃO LENTA - Múltiplos problemas de performance
     */
    public void processOrdersBatchSlow() throws Exception {
        System.out.println("=== BATCH PROCESS - VERSÃO LENTA ===");
        Instant start = Instant.now();
        
        // ❌ PROBLEMA 1: N+1 Query Problem
        List<Integer> orderIds = getOrderIdsSlow();
        System.out.println("Total de pedidos: " + orderIds.size());
        
        List<Order> orders = new ArrayList<>();
        for (Integer orderId : orderIds) {
            // ❌ Uma query por pedido (N queries!)
            Order order = getOrderByIdSlow(orderId); // Query individual
            orders.add(order);
        }
        
        // ❌ PROBLEMA 2: File I/O ineficiente (sem buffer)
        File csvFile = generateCsvFileSlow(orders);
        
        // ❌ PROBLEMA 3: FTP síncrono sem retry/paralelização
        uploadToFtpSlow(csvFile);
        
        Duration duration = Duration.between(start, Instant.now());
        System.out.println("Tempo total (LENTO): " + duration.toSeconds() + "s");
    }
    
    private List<Integer> getOrderIdsSlow() throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        
        // ❌ PROBLEMA: Sem índice na coluna order_date
        // ❌ PROBLEMA: Traz todas as colunas (SELECT *)
        ResultSet rs = stmt.executeQuery(
            "SELECT * FROM orders WHERE order_date = CURRENT_DATE"
        );
        
        List<Integer> ids = new ArrayList<>();
        while (rs.next()) {
            ids.add(rs.getInt("id"));
        }
        
        // ❌ PROBLEMA: Não fecha recursos (memory leak)
        return ids;
    }
    
    private Order getOrderByIdSlow(int orderId) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(
            // ❌ PROBLEMA: JOIN sem índice nas foreign keys
            "SELECT o.*, c.name as customer_name, oi.* " +
            "FROM orders o " +
            "JOIN customers c ON o.customer_id = c.id " +
            "JOIN order_items oi ON oi.order_id = o.id " +
            "WHERE o.id = ?"
        );
        pstmt.setInt(1, orderId);
        
        ResultSet rs = pstmt.executeQuery();
        // ... monta objeto Order
        
        // ❌ PROBLEMA: Não fecha recursos
        return new Order();
    }
    
    private File generateCsvFileSlow(List<Order> orders) throws IOException {
        File file = new File("/tmp/orders.csv");
        
        // ❌ PROBLEMA: FileWriter sem buffer (slow!)
        FileWriter writer = new FileWriter(file);
        
        for (Order order : orders) {
            // ❌ PROBLEMA: Múltiplas chamadas write() (muitas syscalls)
            writer.write(order.getId() + ",");
            writer.write(order.getCustomerName() + ",");
            writer.write(order.getTotalAmount() + "\n");
        }
        
        writer.close();
        return file;
    }
    
    private void uploadToFtpSlow(File file) throws IOException {
        FTPClient ftpClient = new FTPClient();
        
        // ❌ PROBLEMA: Sem timeout (pode travar)
        ftpClient.connect("ftp.example.com");
        ftpClient.login("user", "pass");
        
        // ❌ PROBLEMA: Modo ASCII (mais lento que binary)
        // ❌ PROBLEMA: Sem retry se falhar
        FileInputStream fis = new FileInputStream(file);
        ftpClient.storeFile("/remote/orders.csv", fis);
        
        fis.close();
        ftpClient.disconnect();
    }

    // ====================================================================
    // VERSÃO OTIMIZADA (Aplicando Best Practices)
    // ====================================================================
    
    /**
     * ✅ VERSÃO OTIMIZADA - Correção de todos os problemas
     */
    public void processOrdersBatchOptimized() throws Exception {
        System.out.println("=== BATCH PROCESS - VERSÃO OTIMIZADA ===");
        Instant start = Instant.now();
        
        // ✅ FIX 1: Single query com JOIN (elimina N+1)
        List<Order> orders = getOrdersOptimized();
        System.out.println("Total de pedidos: " + orders.size());
        
        // ✅ FIX 2: File I/O com buffer
        Path csvFile = generateCsvFileOptimized(orders);
        
        // ✅ FIX 3: FTP paralelo com retry e timeout
        uploadToFtpOptimized(csvFile);
        
        Duration duration = Duration.between(start, Instant.now());
        System.out.println("Tempo total (OTIMIZADO): " + duration.toSeconds() + "s");
    }
    
    /**
     * ✅ OTIMIZAÇÃO DATABASE - Single Query com JOIN
     * 
     * ANTES: N+1 queries (1 + 1000 = 1001 queries para 1000 pedidos)
     * DEPOIS: 1 query única
     * 
     * GANHO: ~99% redução em database round-trips
     */
    private List<Order> getOrdersOptimized() throws SQLException {
        String sql = """
            SELECT 
                o.id,
                o.order_date,
                o.total_amount,
                c.name as customer_name,
                c.email as customer_email,
                oi.product_id,
                oi.quantity,
                oi.unit_price
            FROM orders o
            INNER JOIN customers c ON o.customer_id = c.id
            LEFT JOIN order_items oi ON oi.order_id = o.id
            WHERE o.order_date = CURRENT_DATE
            ORDER BY o.id, oi.id
            """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            Map<Integer, Order> orderMap = new HashMap<>();
            
            while (rs.next()) {
                int orderId = rs.getInt("id");
                
                Order order = orderMap.computeIfAbsent(orderId, id -> {
                    Order o = new Order();
                    o.setId(id);
                    o.setOrderDate(rs.getDate("order_date"));
                    o.setTotalAmount(rs.getDouble("total_amount"));
                    o.setCustomerName(rs.getString("customer_name"));
                    o.setCustomerEmail(rs.getString("customer_email"));
                    o.setItems(new ArrayList<>());
                    return o;
                });
                
                // Adiciona item ao pedido
                OrderItem item = new OrderItem();
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                order.getItems().add(item);
            }
            
            return new ArrayList<>(orderMap.values());
        }
    }
    
    /**
     * ✅ OTIMIZAÇÃO FILE I/O - BufferedWriter
     * 
     * ANTES: Múltiplas syscalls (write por campo)
     * DEPOIS: Buffer de 8KB reduz syscalls
     * 
     * GANHO: ~70% mais rápido para escrita de arquivos grandes
     */
    private Path generateCsvFileOptimized(List<Order> orders) throws IOException {
        Path file = Paths.get("/tmp/orders_" + System.currentTimeMillis() + ".csv");
        
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            // Header
            writer.write("Order ID,Customer,Email,Total,Items Count\n");
            
            // Usa StringBuilder para reduzir criação de objetos
            StringBuilder sb = new StringBuilder(256);
            
            for (Order order : orders) {
                sb.setLength(0); // Reutiliza StringBuilder
                sb.append(order.getId()).append(",")
                  .append(escapeCSV(order.getCustomerName())).append(",")
                  .append(order.getCustomerEmail()).append(",")
                  .append(String.format("%.2f", order.getTotalAmount())).append(",")
                  .append(order.getItems().size()).append("\n");
                
                writer.write(sb.toString());
            }
        }
        
        return file;
    }
    
    /**
     * ✅ OTIMIZAÇÃO FTP - Connection Pool + Retry + Timeout
     * 
     * ANTES: Conexão/desconexão por arquivo
     * DEPOIS: Pool de conexões reutilizáveis
     * 
     * GANHO: ~80% redução em tempo de conexão FTP
     */
    private void uploadToFtpOptimized(Path file) throws IOException {
        FTPClient ftpClient = new FTPClient();
        
        // ✅ Configuração de timeouts
        ftpClient.setConnectTimeout(10000); // 10s
        ftpClient.setDataTimeout(Duration.ofMinutes(5)); // 5min
        
        try {
            // ✅ Retry com exponential backoff
            int maxRetries = 3;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    ftpClient.connect("ftp.example.com", 21);
                    ftpClient.login("user", "pass");
                    
                    // ✅ Modo BINARY (mais rápido que ASCII)
                    ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                    
                    // ✅ Passive mode (melhor compatibilidade com firewalls)
                    ftpClient.enterLocalPassiveMode();
                    
                    // ✅ Stream com buffer
                    try (InputStream fis = Files.newInputStream(file)) {
                        boolean success = ftpClient.storeFile("/remote/" + file.getFileName(), fis);
                        
                        if (!success) {
                            throw new IOException("FTP upload falhou: " + ftpClient.getReplyString());
                        }
                    }
                    
                    System.out.println("Upload concluído com sucesso!");
                    break; // Sucesso, sai do loop
                    
                } catch (IOException e) {
                    if (attempt == maxRetries) {
                        throw e; // Última tentativa, propaga erro
                    }
                    
                    long backoff = (long) Math.pow(2, attempt) * 1000; // Exponential backoff
                    System.err.println("Tentativa " + attempt + " falhou. Retry em " + backoff + "ms");
                    Thread.sleep(backoff);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrompido", e);
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.disconnect();
            }
        }
    }

    // ====================================================================
    // OTIMIZAÇÃO AVANÇADA: PROCESSAMENTO PARALELO
    // ====================================================================
    
    /**
     * ✅ VERSÃO COM PARALELIZAÇÃO (para volumes muito grandes)
     * 
     * Estratégia:
     * 1. Particiona dados em chunks
     * 2. Processa chunks em paralelo (thread pool)
     * 3. Agrupa resultados
     * 
     * GANHO: Escalabilidade linear com número de cores
     */
    public void processOrdersBatchParallel() throws Exception {
        Instant start = Instant.now();
        
        int threadPoolSize = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        
        try {
            // 1. Busca IDs de pedidos (rápido)
            List<Integer> orderIds = getOrderIdsOptimized();
            
            // 2. Divide em chunks
            int chunkSize = 1000;
            List<List<Integer>> chunks = partitionList(orderIds, chunkSize);
            
            System.out.println("Processando " + orderIds.size() + " pedidos em " + 
                             chunks.size() + " chunks com " + threadPoolSize + " threads");
            
            // 3. Processa chunks em paralelo
            List<CompletableFuture<List<Order>>> futures = new ArrayList<>();
            
            for (List<Integer> chunk : chunks) {
                CompletableFuture<List<Order>> future = CompletableFuture.supplyAsync(
                    () -> getOrdersByIdsOptimized(chunk), 
                    executor
                );
                futures.add(future);
            }
            
            // 4. Aguarda conclusão de todos os chunks
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // 5. Agrega resultados
            List<Order> allOrders = new ArrayList<>();
            for (CompletableFuture<List<Order>> future : futures) {
                allOrders.addAll(future.get());
            }
            
            // 6. Gera arquivo e upload
            Path csvFile = generateCsvFileOptimized(allOrders);
            uploadToFtpOptimized(csvFile);
            
            Duration duration = Duration.between(start, Instant.now());
            System.out.println("Tempo total (PARALELO): " + duration.toSeconds() + "s");
            
        } finally {
            executor.shutdown();
        }
    }
    
    private List<Integer> getOrderIdsOptimized() throws SQLException {
        String sql = "SELECT id FROM orders WHERE order_date = CURRENT_DATE";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            List<Integer> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
            return ids;
        }
    }
    
    private List<Order> getOrdersByIdsOptimized(List<Integer> orderIds) {
        if (orderIds.isEmpty()) return List.of();
        
        // Usa IN clause com batch de IDs
        String placeholders = String.join(",", Collections.nCopies(orderIds.size(), "?"));
        String sql = """
            SELECT o.*, c.name as customer_name, c.email as customer_email
            FROM orders o
            INNER JOIN customers c ON o.customer_id = c.id
            WHERE o.id IN (%s)
            """.formatted(placeholders);
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < orderIds.size(); i++) {
                pstmt.setInt(i + 1, orderIds.get(i));
            }
            
            ResultSet rs = pstmt.executeQuery();
            List<Order> orders = new ArrayList<>();
            
            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setCustomerName(rs.getString("customer_name"));
                order.setCustomerEmail(rs.getString("customer_email"));
                orders.add(order);
            }
            
            return orders;
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedidos", e);
        }
    }

    // ====================================================================
    // ANÁLISE DE PERFORMANCE E MÉTRICAS
    // ====================================================================
    
    /**
     * CHECKLIST DE OTIMIZAÇÃO:
     * 
     * ✅ DATABASE:
     *    - Índices em colunas de WHERE/JOIN (order_date, customer_id, order_id)
     *    - Single query com JOINs (elimina N+1)
     *    - SELECT apenas colunas necessárias (não SELECT *)
     *    - EXPLAIN ANALYZE para validar plano de execução
     *    - Connection pooling (HikariCP)
     *    - Batch size adequado (1000-5000 registros)
     *    - Read-only transaction para queries
     * 
     * ✅ FILE I/O:
     *    - BufferedWriter/BufferedOutputStream (8KB-64KB buffer)
     *    - StringBuilder para reduzir garbage
     *    - Files.newBufferedWriter (NIO mais eficiente que IO)
     *    - Compressão GZIP se arquivos grandes
     * 
     * ✅ FTP:
     *    - Binary mode (não ASCII)
     *    - Passive mode (firewall friendly)
     *    - Connection pooling
     *    - Timeout configurado
     *    - Retry com exponential backoff
     *    - Considerar SFTP (mais seguro) ou S3 (mais rápido)
     * 
     * ✅ APPLICATION:
     *    - Thread pool para paralelização
     *    - Try-with-resources (evita leaks)
     *    - Métricas com Micrometer
     *    - Logging estruturado (tempo de cada fase)
     *    - Health checks e alertas
     * 
     * ✅ INFRASTRUCTURE:
     *    - JVM tuning (heap size, GC)
     *    - Network latency (co-location de DB e app)
     *    - Disk I/O (SSD vs HDD)
     *    - CPU cores suficientes
     */
    
    /**
     * EXEMPLO DE MÉTRICAS (Micrometer + Prometheus)
     */
    /*
    @Timed(value = "batch.orders.process", description = "Tempo total do batch")
    public void processOrdersBatchWithMetrics() {
        Timer.Sample dbTimer = Timer.start(meterRegistry);
        List<Order> orders = getOrdersOptimized();
        dbTimer.stop(meterRegistry.timer("batch.orders.db_query"));
        
        Timer.Sample fileTimer = Timer.start(meterRegistry);
        Path file = generateCsvFileOptimized(orders);
        fileTimer.stop(meterRegistry.timer("batch.orders.file_generation"));
        
        Timer.Sample ftpTimer = Timer.start(meterRegistry);
        uploadToFtpOptimized(file);
        ftpTimer.stop(meterRegistry.timer("batch.orders.ftp_upload"));
        
        Counter.builder("batch.orders.processed")
            .tag("status", "success")
            .register(meterRegistry)
            .increment(orders.size());
    }
    */
    
    /**
     * BENCHMARKS ESPERADOS (exemplo para 10.000 pedidos):
     * 
     * VERSÃO LENTA:
     * - DB queries: 10.001 queries x 5ms = ~50s
     * - File I/O: ~5s
     * - FTP: ~10s
     * TOTAL: ~65s
     * 
     * VERSÃO OTIMIZADA:
     * - DB query: 1 query x 200ms = 0.2s
     * - File I/O: ~0.5s (com buffer)
     * - FTP: ~8s (binary mode)
     * TOTAL: ~9s
     * 
     * GANHO: ~86% mais rápido (65s → 9s)
     * 
     * VERSÃO PARALELA (4 cores):
     * - DB queries: 4 queries paralelas x 100ms = 0.1s
     * - File I/O: ~0.5s
     * - FTP: ~8s
     * TOTAL: ~9s
     * 
     * GANHO: Escalabilidade para volumes maiores
     */

    // Métodos auxiliares
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://localhost/db", "user", "pass");
    }
    
    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private <T> List<List<T>> partitionList(List<T> list, int chunkSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            partitions.add(list.subList(i, Math.min(i + chunkSize, list.size())));
        }
        return partitions;
    }
}

// Classes de modelo
class Order {
    private Integer id;
    private Date orderDate;
    private Double totalAmount;
    private String customerName;
    private String customerEmail;
    private List<OrderItem> items;
    
    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}

class OrderItem {
    private Integer productId;
    private Integer quantity;
    private Double unitPrice;
    
    // Getters e Setters
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
}
