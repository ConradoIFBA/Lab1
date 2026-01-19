-- ========================================
-- SCRIPT COMPLETO - SISTEMA MEI
-- COPIE TUDO E EXECUTE NO phpMyAdmin
-- ========================================

-- Apagar banco anterior e criar novo
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

-- Inserir categorias padrão
INSERT INTO Categoria (id_categoria, Nome_categoria, ativo) VALUES
(1, 'Revenda de Mercadorias', TRUE),
(2, 'Produtos Industrializados', TRUE),
(3, 'Prestação de Serviços', TRUE);

-- ========================================
-- TABELA: Vendas
-- ========================================
CREATE TABLE Vendas (
    id_Vendas INT AUTO_INCREMENT PRIMARY KEY,
    Data_Vendas TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Valor FLOAT NOT NULL,
    NotaFiscalEmitida CHAR(1) DEFAULT 'N' CHECK (NotaFiscalEmitida IN ('S', 'N')),
    Categoria INT NOT NULL,
    usuario_id INT NOT NULL,
    descricao TEXT,
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (Categoria) REFERENCES Categoria(id_categoria),
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    INDEX idx_usuario (usuario_id),
    INDEX idx_data (Data_Vendas),
    INDEX idx_categoria (Categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABELA: NotaFiscal
-- ========================================
CREATE TABLE NotaFiscal (
    id_NotaFiscal INT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50) NOT NULL,
    dataEmissao TIMESTAMP NOT NULL,
    valor FLOAT NOT NULL,
    vendas_id INT NOT NULL,
    usuario_id INT NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vendas_id) REFERENCES Vendas(id_Vendas) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    INDEX idx_numero (numero),
    INDEX idx_vendas (vendas_id),
    INDEX idx_usuario (usuario_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- TABELA: MetPag
-- ========================================
CREATE TABLE MetPag (
    id_MetPag INT AUTO_INCREMENT PRIMARY KEY,
    Descricao VARCHAR(100) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE
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
-- TABELA: Pagamento
-- ========================================
CREATE TABLE Pagamento (
    id_pag INT AUTO_INCREMENT PRIMARY KEY,
    Vendas_id_Vendas INT NOT NULL,
    MetPag_id_MetPag INT NOT NULL,
    Valor FLOAT NOT NULL,
    data_pagamento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (Vendas_id_Vendas) REFERENCES Vendas(id_Vendas) ON DELETE CASCADE,
    FOREIGN KEY (MetPag_id_MetPag) REFERENCES MetPag(id_MetPag),
    INDEX idx_vendas (Vendas_id_Vendas),
    INDEX idx_metpag (MetPag_id_MetPag),
    INDEX idx_data (data_pagamento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- DADOS DE TESTE
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
SET @venda5 = (SELECT id_Vendas FROM Vendas WHERE usuario_id = @usuario_teste AND NotaFiscalEmitida = 'S' ORDER BY id_Vendas LIMIT 1 OFFSET 2);

INSERT INTO NotaFiscal (numero, dataEmissao, valor, vendas_id, usuario_id, ativo) VALUES
('NF-2025-001', NOW(), 150.00, @venda1, @usuario_teste, TRUE),
('NF-2025-002', DATE_SUB(NOW(), INTERVAL 2 DAY), 350.00, @venda3, @usuario_teste, TRUE),
('NF-2025-003', DATE_SUB(NOW(), INTERVAL 7 DAY), 280.00, @venda5, @usuario_teste, TRUE);

-- ========================================
-- VERIFICAÇÕES FINAIS
-- ========================================

-- Exibir resumo
SELECT '====== BANCO CRIADO COM SUCESSO! ======' as Mensagem;
SELECT CONCAT('Usuários: ', COUNT(*)) as Info FROM Usuario;
SELECT CONCAT('Categorias: ', COUNT(*)) as Info FROM Categoria;
SELECT CONCAT('Métodos de Pagamento: ', COUNT(*)) as Info FROM MetPag;
SELECT CONCAT('Vendas: ', COUNT(*)) as Info FROM Vendas;
SELECT CONCAT('Notas Fiscais: ', COUNT(*)) as Info FROM NotaFiscal;

-- Mostrar dados do usuário de teste
SELECT '====== USUÁRIO DE TESTE ======' as '';
SELECT nome, cpf, email FROM Usuario WHERE cpf = '12345678901';

-- Mostrar últimas vendas
SELECT '====== ÚLTIMAS VENDAS ======' as '';
SELECT 
    v.id_Vendas as ID,
    v.Data_Vendas as Data,
    v.Valor,
    c.Nome_categoria as Categoria,
    v.NotaFiscalEmitida as NF,
    v.descricao
FROM Vendas v
JOIN Categoria c ON v.Categoria = c.id_categoria
WHERE v.usuario_id = @usuario_teste
ORDER BY v.Data_Vendas DESC;
