/**
 * QUESTÃO 6: Consultas SQL
 * 
 * TABELAS:
 * 
 * Salesperson (ID, Name, Age, Salary)
 * Customer (ID, Name, City, Industry_Type)
 * Orders (ID, order_date, customer_id, salesperson_id, Amount)
 */

-- ============================================================================
-- 6a. Retorna nomes dos Salesperson que NÃO têm pedidos com Samsonic
-- ============================================================================

/**
 * ESTRATÉGIA 1: NOT IN com Subquery
 * 
 * Lógica:
 * 1. Subquery: encontra IDs dos salespersons que TÊM pedidos com Samsonic
 * 2. Query principal: retorna salespersons que NÃO estão nesse conjunto
 */
SELECT Name
FROM Salesperson
WHERE ID NOT IN (
    SELECT DISTINCT o.salesperson_id
    FROM Orders o
    INNER JOIN Customer c ON o.customer_id = c.ID
    WHERE c.Name = 'Samsonic'
);

/**
 * ESTRATÉGIA 2: NOT EXISTS (mais eficiente em muitos casos)
 * 
 * Vantagem: Para em caso de match (não precisa processar todos os registros)
 */
SELECT s.Name
FROM Salesperson s
WHERE NOT EXISTS (
    SELECT 1
    FROM Orders o
    INNER JOIN Customer c ON o.customer_id = c.ID
    WHERE c.Name = 'Samsonic'
      AND o.salesperson_id = s.ID
);

/**
 * ESTRATÉGIA 3: LEFT JOIN com IS NULL
 * 
 * Lógica:
 * 1. LEFT JOIN traz todos os salespersons
 * 2. Filtra onde NÃO há match (IS NULL)
 */
SELECT s.Name
FROM Salesperson s
LEFT JOIN Orders o ON s.ID = o.salesperson_id
LEFT JOIN Customer c ON o.customer_id = c.ID AND c.Name = 'Samsonic'
WHERE c.ID IS NULL
GROUP BY s.ID, s.Name; -- GROUP BY para evitar duplicatas se houver múltiplos orders

/**
 * RESULTADO ESPERADO (baseado nos dados fornecidos):
 * - Abe (ID=1) - TEM pedido com Samsonic (order 30, customer 9≠4)
 * - Bob (ID=2) - TEM pedido com Samsonic (order 10, customer 4)
 * - Ken (ID=8) - TEM pedido com Samsonic (order 20, customer 4)
 * - Dan (ID=7) - NÃO tem pedido com Samsonic ✓
 * - Joe (ID=11) - NÃO tem pedido com Samsonic ✓
 * - Chris (ID=5) - NÃO aparece em Orders, então também se qualifica ✓
 * 
 * RESPOSTA: Dan, Joe, Chris
 */


-- ============================================================================
-- 6b. Adiciona '*' ao final do nome de Salesperson com 2+ pedidos
-- ============================================================================

/**
 * ESTRATÉGIA: UPDATE com JOIN e Subquery
 * 
 * Lógica:
 * 1. Subquery: identifica salespersons com COUNT(*) >= 2
 * 2. UPDATE: concatena '*' aos nomes identificados
 */
UPDATE Salesperson
SET Name = CONCAT(Name, '*')
WHERE ID IN (
    SELECT salesperson_id
    FROM Orders
    GROUP BY salesperson_id
    HAVING COUNT(*) >= 2
);

/**
 * ALTERNATIVA (mais explícita com JOIN):
 */
UPDATE Salesperson s
INNER JOIN (
    SELECT salesperson_id, COUNT(*) as order_count
    FROM Orders
    GROUP BY salesperson_id
    HAVING COUNT(*) >= 2
) o ON s.ID = o.salesperson_id
SET s.Name = CONCAT(s.Name, '*');

/**
 * VALIDAÇÃO (verificar quem tem 2+ pedidos):
 */
SELECT 
    s.Name,
    COUNT(o.ID) as order_count
FROM Salesperson s
LEFT JOIN Orders o ON s.ID = o.salesperson_id
GROUP BY s.ID, s.Name
ORDER BY order_count DESC;

/**
 * ANÁLISE DOS DADOS:
 * - Bob (ID=2): orders 10, 40 → 2 pedidos ✓ → "Bob*"
 * - Ken (ID=8): order 20 → 1 pedido (não atualiza)
 * - Abe (ID=1): order 30 → 1 pedido (não atualiza)
 * - Dan (ID=7): orders 50, 60, 70 → 3 pedidos ✓ → "Dan*"
 * 
 * RESULTADO: Bob e Dan recebem '*'
 */

/**
 * PREVENÇÃO DE DUPLICAÇÃO (se executar múltiplas vezes):
 * 
 * Adiciona verificação para não adicionar '*' repetidamente:
 */
UPDATE Salesperson
SET Name = CONCAT(Name, '*')
WHERE ID IN (
    SELECT salesperson_id
    FROM Orders
    GROUP BY salesperson_id
    HAVING COUNT(*) >= 2
)
AND Name NOT LIKE '%*'; -- Evita duplicação


-- ============================================================================
-- 6c. Deleta Salesperson que fizeram pedidos para cidade de Jackson
-- ============================================================================

/**
 * ⚠️ ATENÇÃO: DELETE é irreversível! Sempre faça:
 * 1. Backup antes
 * 2. SELECT para verificar o que será deletado
 * 3. BEGIN TRANSACTION antes do DELETE
 */

-- PASSO 1: Verificar quem será deletado (SELECT primeiro)
SELECT DISTINCT s.*
FROM Salesperson s
INNER JOIN Orders o ON s.ID = o.salesperson_id
INNER JOIN Customer c ON o.customer_id = c.ID
WHERE c.City = 'Jackson';

/**
 * ANÁLISE DOS DADOS:
 * - Customers em Jackson: Samony (ID=7), Orange (ID=9)
 * - Orders para Jackson:
 *   - Order 30: customer 9 (Orange), salesperson 1 (Abe) ✓
 *   - Order 40: customer 7 (Samony), salesperson 2 (Bob) ✓
 *   - Order 70: customer 9 (Orange), salesperson 7 (Dan) ✓
 * 
 * SERÃO DELETADOS: Abe, Bob, Dan
 */

-- PASSO 2: DELETE (com TRANSACTION para segurança)
BEGIN TRANSACTION;

DELETE FROM Salesperson
WHERE ID IN (
    SELECT DISTINCT o.salesperson_id
    FROM Orders o
    INNER JOIN Customer c ON o.customer_id = c.ID
    WHERE c.City = 'Jackson'
);

-- Verificar quantos foram deletados
-- Se correto, COMMIT; se não, ROLLBACK
COMMIT;
-- ou ROLLBACK;

/**
 * ⚠️ CONSIDERAÇÕES IMPORTANTES:
 * 
 * 1. FOREIGN KEY CONSTRAINTS:
 *    - Se Orders tem FK para Salesperson com ON DELETE RESTRICT,
 *      o DELETE falhará (precisa deletar Orders primeiro)
 *    - Se ON DELETE CASCADE, Orders também serão deletados
 *    - Se ON DELETE SET NULL, salesperson_id ficará NULL
 * 
 * 2. SOFT DELETE (recomendado em produção):
 *    Em vez de DELETE físico, usar flag:
 */
ALTER TABLE Salesperson ADD COLUMN deleted_at TIMESTAMP NULL;

UPDATE Salesperson
SET deleted_at = NOW()
WHERE ID IN (
    SELECT DISTINCT o.salesperson_id
    FROM Orders o
    INNER JOIN Customer c ON o.customer_id = c.ID
    WHERE c.City = 'Jackson'
);

-- Queries futuras usam WHERE deleted_at IS NULL


-- ============================================================================
-- 6d. Total de vendas por Salesperson (incluindo quem não vendeu = 0)
-- ============================================================================

/**
 * ESTRATÉGIA: LEFT JOIN para incluir todos os salespersons
 * 
 * Key points:
 * - LEFT JOIN garante que TODOS os salespersons aparecem
 * - COALESCE/IFNULL trata NULL como 0
 * - SUM(Amount) agrega vendas
 */
SELECT 
    s.ID,
    s.Name,
    COALESCE(SUM(o.Amount), 0) AS Total_Sales
FROM Salesperson s
LEFT JOIN Orders o ON s.ID = o.salesperson_id
GROUP BY s.ID, s.Name
ORDER BY Total_Sales DESC;

/**
 * ALTERNATIVA com IFNULL (MySQL):
 */
SELECT 
    s.ID,
    s.Name,
    IFNULL(SUM(o.Amount), 0) AS Total_Sales
FROM Salesperson s
LEFT JOIN Orders o ON s.ID = o.salesperson_id
GROUP BY s.ID, s.Name
ORDER BY Total_Sales DESC;



/**
 * VERSÃO COM FORMATAÇÃO E INFORMAÇÕES ADICIONAIS:
 */
SELECT 
    s.ID,
    s.Name,
    s.Age,
    s.Salary,
    COUNT(o.ID) AS Order_Count,
    COALESCE(SUM(o.Amount), 0) AS Total_Sales,
    ROUND(COALESCE(SUM(o.Amount), 0) / s.Salary * 100, 2) AS Sales_To_Salary_Ratio
FROM Salesperson s
LEFT JOIN Orders o ON s.ID = o.salesperson_id
GROUP BY s.ID, s.Name, s.Age, s.Salary
ORDER BY Total_Sales DESC;

/**
 * ANÁLISE DE PERFORMANCE:
 * 
 * Para melhorar performance:
 * 1. Criar índices:
 *    CREATE INDEX idx_orders_salesperson ON Orders(salesperson_id);
 *    CREATE INDEX idx_orders_customer ON Orders(customer_id);
 *    CREATE INDEX idx_customer_city ON Customer(City);
 * 
 * 2. Particionar tabela Orders se muito grande (por data)
 * 
 * 3. Materializar views para relatórios frequentes
 */


-- ============================================================================
-- QUERIES ADICIONAIS ÚTEIS (BÔNUS)
-- ============================================================================

-- Top 3 salespersons por valor total
SELECT s.Name, SUM(o.Amount) AS Total
FROM Salesperson s
INNER JOIN Orders o ON s.ID = o.salesperson_id
GROUP BY s.ID, s.Name
ORDER BY Total DESC
LIMIT 3;

-- Salespersons que venderam para múltiplos customers
SELECT 
    s.Name,
    COUNT(DISTINCT o.customer_id) AS Unique_Customers
FROM Salesperson s
INNER JOIN Orders o ON s.ID = o.salesperson_id
GROUP BY s.ID, s.Name
HAVING COUNT(DISTINCT o.customer_id) > 1;

-- Vendas por Industry Type
SELECT 
    c.Industry_Type,
    COUNT(o.ID) AS Order_Count,
    SUM(o.Amount) AS Total_Revenue
FROM Customer c
LEFT JOIN Orders o ON c.ID = o.customer_id
GROUP BY c.Industry_Type
ORDER BY Total_Revenue DESC;

-- Ticket médio por salesperson
SELECT 
    s.Name,
    COUNT(o.ID) AS Order_Count,
    AVG(o.Amount) AS Avg_Order_Value,
    MIN(o.Amount) AS Min_Order,
    MAX(o.Amount) AS Max_Order
FROM Salesperson s
INNER JOIN Orders o ON s.ID = o.salesperson_id
GROUP BY s.ID, s.Name
ORDER BY Avg_Order_Value DESC;

-- Customers sem pedidos (similar à questão 6a)
SELECT Name, City
FROM Customer
WHERE ID NOT IN (SELECT DISTINCT customer_id FROM Orders);

/**
 * ============================================================================
 * VALIDAÇÃO E TESTES
 * ============================================================================
 * 
 * Para validar as queries, executar:
 * 
 * 1. Criar tabelas de teste com dados fornecidos
 * 2. Executar cada query
 * 3. Validar resultados manualmente
 * 4. Verificar EXPLAIN PLAN para performance
 * 5. Testar edge cases (NULL, duplicatas, etc.)
 */

-- Script de criação de tabelas para teste:
/*
CREATE TABLE Salesperson (
    ID INT PRIMARY KEY,
    Name VARCHAR(50),
    Age INT,
    Salary DECIMAL(10,2)
);

CREATE TABLE Customer (
    ID INT PRIMARY KEY,
    Name VARCHAR(50),
    City VARCHAR(50),
    Industry_Type CHAR(1)
);

CREATE TABLE Orders (
    ID INT PRIMARY KEY,
    order_date DATE,
    customer_id INT,
    salesperson_id INT,
    Amount DECIMAL(10,2),
    FOREIGN KEY (customer_id) REFERENCES Customer(ID),
    FOREIGN KEY (salesperson_id) REFERENCES Salesperson(ID)
);

-- Inserir dados fornecidos...
INSERT INTO Salesperson VALUES
(1, 'Abe', 61, 140000),
(2, 'Bob', 34, 44000),
(5, 'Chris', 34, 40000),
(7, 'Dan', 41, 52000),
(8, 'Ken', 57, 115000),
(11, 'Joe', 38, 38000);

-- etc...
*/
