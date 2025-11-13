package exemplos;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Exemplo demonstrando a necessidade de override de equals() e hashCode().
 * 
 * CENÁRIO: Sistema de gerenciamento de produtos onde produtos são considerados
 * iguais se possuem o mesmo SKU, independente da referência do objeto.
 */
public class Question1_EqualsHashCode {

    public static void main(String[] args) {
        // Sem override adequado
        ProductoSemOverride p1 = new ProductoSemOverride("SKU123", "Notebook");
        ProductoSemOverride p2 = new ProductoSemOverride("SKU123", "Notebook");
        
        System.out.println("=== SEM OVERRIDE CORRETO ===");
        System.out.println("p1.equals(p2): " + p1.equals(p2)); // false (deveria ser true!)
        
        Set<ProductoSemOverride> set1 = new HashSet<>();
        set1.add(p1);
        set1.add(p2);
        System.out.println("Produtos no Set: " + set1.size()); // 2 (deveria ser 1!)
        
        // Com override adequado
        Product p3 = new Product("SKU123", "Notebook");
        Product p4 = new Product("SKU123", "Notebook");
        
        System.out.println("\n=== COM OVERRIDE CORRETO ===");
        System.out.println("p3.equals(p4): " + p3.equals(p4)); // true
        
        Set<Product> set2 = new HashSet<>();
        set2.add(p3);
        set2.add(p4);
        System.out.println("Produtos no Set: " + set2.size()); // 1
    }
}

/**
 * Classe SEM override adequado - comportamento incorreto.
 */
class ProductoSemOverride {
    private String sku;
    private String name;
    
    public ProductoSemOverride(String sku, String name) {
        this.sku = sku;
        this.name = name;
    }
    // Usa equals() e hashCode() padrão de Object (comparação por referência)
}

/**
 * Classe COM override adequado seguindo as melhores práticas.
 * 
 * CONSIDERAÇÕES CHAVE:
 * 1. SIMETRIA: a.equals(b) == b.equals(a)
 * 2. REFLEXIVIDADE: a.equals(a) deve ser true
 * 3. TRANSITIVIDADE: se a.equals(b) e b.equals(c), então a.equals(c)
 * 4. CONSISTÊNCIA: múltiplas chamadas retornam o mesmo resultado
 * 5. NULL: a.equals(null) deve ser false
 * 6. CONTRATO hashCode(): objetos iguais devem ter mesmo hashCode
 */
class Product {
    private final String sku;  // final para imutabilidade
    private String name;
    private Double price;
    
    public Product(String sku, String name) {
        this.sku = Objects.requireNonNull(sku, "SKU não pode ser nulo");
        this.name = name;
    }
    
    /**
     * Override de equals() seguindo as melhores práticas.
     * 
     * DECISÕES DE DESIGN:
     * - Apenas SKU é usado na comparação (regra de negócio)
     * - name e price podem mudar sem afetar a identidade
     */
    @Override
    public boolean equals(Object obj) {
        // 1. Verifica se é a mesma referência (otimização)
        if (this == obj) {
            return true;
        }
        
        // 2. Verifica null e tipo correto
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        // 3. Cast seguro
        Product other = (Product) obj;
        
        // 4. Compara os campos relevantes (SKU é nossa chave de negócio)
        return Objects.equals(this.sku, other.sku);
    }
    
    /**
     * Override de hashCode() OBRIGATÓRIO quando equals() é sobrescrito.
     * 
     * CONTRATO:
     * - Se a.equals(b) == true, então a.hashCode() == b.hashCode()
     * - Se a.hashCode() != b.hashCode(), então a.equals(b) == false
     * 
     * Usa Objects.hash() para gerar hash consistente dos mesmos campos usados em equals().
     */
    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }
    
    /**
     * toString() útil para debugging.
     */
    @Override
    public String toString() {
        return String.format("Product[sku=%s, name=%s, price=%s]", sku, name, price);
    }
    
    // Getters e setters
    public String getSku() { return sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}

/**
 * CONSIDERAÇÕES ADICIONAIS:
 * 
 * 1. IMUTABILIDADE: Preferível usar campos final nos atributos usados em equals/hashCode
 *    para evitar que o hashCode mude após inserção em coleções.
 * 
 * 2. HERANÇA: Cuidado ao sobrescrever equals em hierarquias de classes.
 *    Considere usar getClass() em vez de instanceof para evitar violação de simetria.
 * 
 * 3. PERFORMANCE: HashCode deve ser eficiente. Para objetos imutáveis,
 *    considere fazer lazy initialization e cache do hash.
 * 
 * 4. FRAMEWORKS: Lombok (@EqualsAndHashCode), Records (Java 14+) e IDEs
 *    podem gerar esses métodos automaticamente.
 * 
 * 5. COLLECTIONS: HashMap, HashSet, Hashtable dependem de hashCode/equals corretos.
 */
