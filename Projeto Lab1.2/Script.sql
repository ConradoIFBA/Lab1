-- ========================================
-- SCRIPT SQL CORRIGIDO - SISTEMA MEI
-- Execute este script COMPLETO no MySQL
-- ========================================

-- Apagar banco anterior e criar novo com UTF-8
DROP DATABASE IF EXISTS MEI;
CREATE DATABASE MEI CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE MEI;

-- ========================================
-- TABELA: Usuario
-- ========================================
CREATE TABLE Usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE,
    INDEX idx_cpf (cpf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABELA: Categoria
-- ========================================
CREATE TABLE Categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    Nome_categoria VARCHAR(100) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    INDEX idx_nome (Nome_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inserir categorias padrão (conforme PDF do projeto)
INSERT INTO Categoria (id_categoria, Nome_categoria, ativo) VALUES
(1, 'Revenda de Mercadorias', TRUE),
(2, 'Produtos Industrializados', TRUE),
(3, 'Prestação de Serviços', TRUE);

-- ========================================
-- TABELA: Vendas (CORRIGIDA)
-- ========================================
CREATE TABLE Vendas (
    id_Vendas INT AUTO_INCREMENT PRIMARY KEY,
    Data_Vendas TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Valor FLOAT NOT NULL,
    NotaFiscalEmitida CHAR(1) DEFAULT 'N' CHECK (NotaFiscalEmitida IN ('S', 'N')),
    Categoria INT NOT NULL,
    usuario_id INT NOT NULL,          -- ✅ ADICIONADO: FK para Usuario
    descricao TEXT,                   -- ✅ ADICIONADO: descrição da venda
    ativo BOOLEAN DEFAULT TRUE,       -- ✅ ADICIONADO: exclusão lógica
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (Categoria) REFERENCES Categoria(id_categoria),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    
    INDEX idx_usuario (usuario_id),
    INDEX idx_data (Data_Vendas),
    INDEX idx_categoria (Categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABELA: NotaFiscal (CORRIGIDA)
-- ========================================
CREATE TABLE NotaFiscal (
    id_NotaFiscal INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50) NOT NULL,           -- ✅ ALTERADO: INT → VARCHAR
    dataEmissao TIMESTAMP NOT NULL,
    valor FLOAT NOT NULL,
    vendas_id INT NOT NULL,                -- ✅ ADICIONADO: FK para Vendas
    usuario_id INT NOT NULL,               -- ✅ ADICIONADO: FK para Usuario
    ativo BOOLEAN DEFAULT TRUE,            -- ✅ ADICIONADO: exclusão lógica
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (vendas_id) REFERENCES Vendas(id_Vendas) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    
    INDEX idx_numero (numero),
    INDEX idx_vendas (vendas_id),
    INDEX idx_usuario (usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABELA: MetPag (CORRIGIDA)
-- ========================================
CREATE TABLE MetPag (
    id_MetPag INT AUTO_INCREMENT PRIMARY KEY,
    Descricao VARCHAR(100) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE            -- ✅ ADICIONADO: exclusão lógica
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inserir métodos de pagamento padrão
INSERT INTO MetPag (id_MetPag, Descricao, ativo) VALUES
(1, 'Dinheiro', TRUE),
(2, 'Cartão de Crédito', TRUE),
(3, 'Cartão de Débito', TRUE),
(4, 'PIX', TRUE),
(5, 'Transferência Bancária', TRUE),
(6, 'Boleto', TRUE);

-- ========================================
-- TABELA: Pagamento (CORRIGIDA)
-- ========================================
CREATE TABLE Pagamento (
    id_pag INT AUTO_INCREMENT PRIMARY KEY,
    Vendas_id_Vendas INT NOT NULL,
    MetPag_id_MetPag INT NOT NULL,
    Valor FLOAT NOT NULL,
    data_pagamento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- ✅ ADICIONADO
    ativo BOOLEAN DEFAULT TRUE,                          -- ✅ ADICIONADO
    
    FOREIGN KEY (Vendas_id_Vendas) REFERENCES Vendas(id_Vendas) ON DELETE CASCADE,
    FOREIGN KEY (MetPag_id_MetPag) REFERENCES MetPag(id_MetPag),
    
    INDEX idx_vendas (Vendas_id_Vendas),
    INDEX idx_metpag (MetPag_id_MetPag),
    INDEX idx_data (data_pagamento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- VIEWS ÚTEIS PARA RELATÓRIOS
-- ========================================

-- View: Resumo mensal de vendas
CREATE OR REPLACE VIEW vw_resumo_mensal AS
SELECT 
    u.id_usuario,
    u.nome as usuario_nome,
    YEAR(v.Data_Vendas) as ano,
    MONTH(v.Data_Vendas) as mes,
    c.Nome_categoria,
    v.NotaFiscalEmitida,
    COUNT(*) as quantidade_vendas,
    SUM(v.Valor) as total_valor
FROM Vendas v
JOIN Usuario u ON v.usuario_id = u.id_usuario
JOIN Categoria c ON v.Categoria = c.id_categoria
WHERE v.ativo = TRUE
GROUP BY u.id_usuario, ano, mes, c.Nome_categoria, v.NotaFiscalEmitida;

-- View: Últimas vendas com detalhes
CREATE OR REPLACE VIEW vw_ultimas_vendas AS
SELECT 
    v.id_Vendas,
    v.Data_Vendas,
    v.Valor,
    v.descricao,
    v.NotaFiscalEmitida,
    c.Nome_categoria,
    u.nome as usuario_nome,
    nf.numero as numero_nota_fiscal
FROM Vendas v
JOIN Usuario u ON v.usuario_id = u.id_usuario
JOIN Categoria c ON v.Categoria = c.id_categoria
LEFT JOIN NotaFiscal nf ON v.id_Vendas = nf.vendas_id
WHERE v.ativo = TRUE
ORDER BY v.Data_Vendas DESC;

-- ========================================
-- STORED PROCEDURES
-- ========================================

DELIMITER //

-- Procedure: Calcular total do mês para um usuário
CREATE PROCEDURE sp_total_mes_usuario(
    IN p_usuario_id INT,
    IN p_mes INT,
    IN p_ano INT,
    OUT p_total FLOAT
)
BEGIN
    SELECT COALESCE(SUM(Valor), 0) INTO p_total
    FROM Vendas
    WHERE usuario_id = p_usuario_id
      AND MONTH(Data_Vendas) = p_mes
      AND YEAR(Data_Vendas) = p_ano
      AND ativo = TRUE;
END//

-- Procedure: Excluir venda (soft delete)
CREATE PROCEDURE sp_excluir_venda(
    IN p_venda_id INT,
    IN p_usuario_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Erro ao excluir venda';
    END;
    
    START TRANSACTION;
    
    -- Verificar se a venda pertence ao usuário
    IF EXISTS (SELECT 1 FROM Vendas WHERE id_Vendas = p_venda_id AND usuario_id = p_usuario_id) THEN
        -- Marcar nota fiscal como excluída
        UPDATE NotaFiscal SET ativo = FALSE WHERE vendas_id = p_venda_id;
        
        -- Marcar pagamentos como excluídos
        UPDATE Pagamento SET ativo = FALSE WHERE Vendas_id_Vendas = p_venda_id;
        
        -- Marcar venda como excluída
        UPDATE Vendas SET ativo = FALSE WHERE id_Vendas = p_venda_id;
        
        COMMIT;
    ELSE
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Venda não encontrada ou acesso negado';
    END IF;
END//

DELIMITER ;

-- ========================================
-- DADOS DE TESTE (OPCIONAL)
-- ========================================

-- Usuário de teste
-- CPF: 12345678901
-- Senha: 123456
-- Hash BCrypt: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyUI/rYs8K8i

INSERT INTO Usuario (cpf, nome, email, senha, ativo) VALUES
('12345678901', 'João Silva - Teste', 'joao.teste@mei.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyUI/rYs8K8i', TRUE);

-- Obter ID do usuário de teste
SET @usuario_teste = (SELECT id_usuario FROM Usuario WHERE cpf = '12345678901');

-- Vendas de exemplo
INSERT INTO Vendas (Data_Vendas, Valor, NotaFiscalEmitida, Categoria, usuario_id, descricao, ativo) VALUES
(NOW(), 150.00, 'S', 1, @usuario_teste, 'Venda de produtos de limpeza', TRUE),
(DATE_SUB(NOW(), INTERVAL 1 DAY), 200.00, 'N', 2, @usuario_teste, 'Produto artesanal', TRUE),
(DATE_SUB(NOW(), INTERVAL 2 DAY), 350.00, 'S', 3, @usuario_teste, 'Serviço de consultoria', TRUE),
(DATE_SUB(NOW(), INTERVAL 5 DAY), 120.00, 'N', 1, @usuario_teste, 'Revenda de roupas', TRUE),
(DATE_SUB(NOW(), INTERVAL 7 DAY), 280.00, 'S', 2, @usuario_teste, 'Produto manufaturado', TRUE);

-- Notas fiscais para vendas que têm NF
SET @venda1 = (SELECT id_Vendas FROM Vendas WHERE usuario_id = @usuario_teste AND NotaFiscalEmitida = 'S' ORDER BY id_Vendas LIMIT 1);
SET @venda3 = (SELECT id_Vendas FROM Vendas WHERE usuario_id = @usuario_teste AND NotaFiscalEmitida = 'S' ORDER BY id_Vendas LIMIT 1 OFFSET 1);

INSERT INTO NotaFiscal (numero, dataEmissao, valor, vendas_id, usuario_id, ativo) VALUES
('NF-2025-001', NOW(), 150.00, @venda1, @usuario_teste, TRUE),
('NF-2025-002', DATE_SUB(NOW(), INTERVAL 2 DAY), 350.00, @venda3, @usuario_teste, TRUE);

-- ========================================
-- VERIFICAÇÕES FINAIS
-- ========================================

-- Listar todas as tabelas criadas
SELECT 
    TABLE_NAME as 'Tabela', 
    TABLE_ROWS as 'Registros',
    ENGINE as 'Engine'
FROM information_schema.tables 
WHERE table_schema = 'MEI' 
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- Verificar dados inseridos
SELECT '====== RESUMO DO BANCO ======' as '';
SELECT CONCAT('Usuários: ', COUNT(*)) as Info FROM Usuario;
SELECT CONCAT('Categorias: ', COUNT(*)) as Info FROM Categoria;
SELECT CONCAT('Métodos de Pagamento: ', COUNT(*)) as Info FROM MetPag;
SELECT CONCAT('Vendas: ', COUNT(*)) as Info FROM Vendas;
SELECT CONCAT('Notas Fiscais: ', COUNT(*)) as Info FROM NotaFiscal;

-- Testar view
SELECT '====== ÚLTIMAS VENDAS (VIEW) ======' as '';
SELECT * FROM vw_ultimas_vendas LIMIT 5;

SELECT '✅ BANCO DE DADOS MEI CRIADO COM SUCESSO!' as Mensagem;