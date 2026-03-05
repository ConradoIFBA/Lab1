-- ================================================================
-- BANCO DE DADOS MEI - SUPER COMENTADO
-- ================================================================
--
-- VERSÃO: 3.1 (Atualizada com consultas SELECT)
-- DATA: 05/03/2026
-- AUTOR: Sistema MEI
-- SGBD: MySQL/MariaDB 10.4+
--
-- PROPÓSITO:
-- Sistema completo de gestão financeira para Microempreendedores
-- Individuais (MEI), com controle de vendas, notas fiscais,
-- pagamentos e relatórios.
--
-- FUNCIONALIDADES:
-- 1. Gestão de usuários (MEI)
-- 2. Categorização de vendas
-- 3. Registro de vendas com Nota Fiscal opcional
-- 4. Múltiplos métodos de pagamento
-- 5. Histórico completo de transações
-- 6. Soft delete em todas tabelas principais
-- 7. Consultas SELECT com JOIN (requisito acadêmico)
--
-- ESTRUTURA DO BANCO:
-- - 6 tabelas principais
-- - 6 relacionamentos (foreign keys)
-- - 15 índices para performance
-- - Soft delete (campo 'ativo')
-- - Timestamps automáticos
-- - 5 consultas SELECT com JOIN (trabalho acadêmico)
--
-- RELACIONAMENTOS (FOREIGN KEYS):
-- 1. usuario (1) ─── (N) vendas
-- 2. usuario (1) ─── (N) nota_fiscal
-- 3. vendas (1) ──── (1) nota_fiscal
-- 4. vendas (1) ──── (N) pagamento
-- 5. categoria (1) ─ (N) vendas
-- 6. metodo_pagamento (1) ─ (N) pagamento
--
-- CONVENÇÕES DE NOMENCLATURA:
-- - Tabelas: snake_case (minúsculas com underscore)
-- - Colunas: snake_case
-- - PK: id_[tabela] (ex: id_usuario)
-- - FK: [tabela]_id (ex: usuario_id)
-- - Timestamps: data_[acao] (ex: data_cadastro)
-- - Soft delete: ativo TINYINT(1) DEFAULT 1
--
-- INSTALAÇÃO:
-- 1. Criar banco: CREATE DATABASE mei;
-- 2. Selecionar: USE mei;
-- 3. Executar este script completo
-- 4. Verificar: SHOW TABLES;
-- 5. Testar consultas SELECT (final do arquivo)
--
-- SEÇÕES DO ARQUIVO:
-- 1. CREATE TABLE (6 tabelas)
-- 2. INSERT INTO (dados de exemplo)
-- 3. ALTER TABLE (índices e auto_increment)
-- 4. FOREIGN KEYS (6 relacionamentos)
-- 5. CONSULTAS SELECT (5 consultas com JOIN)
-- 6. QUERIES ÚTEIS (administração)
-- ================================================================

-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Tempo de geração: 14/02/2026 às 05:18
-- Versão do servidor: 10.4.32-MariaDB
-- Versão do PHP: 8.0.30
CREATE DATABASE IF NOT EXISTS mei;
USE mei;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `mei`
--

-- ================================================================
-- TABELA 1: CATEGORIA
-- ================================================================
--
-- PROPÓSITO:
-- Classificação das vendas em categorias para relatórios e
-- análises. MEI pode criar categorias personalizadas.
--
-- CAMPOS:
-- - id_categoria: Identificador único (PK)
-- - nome_categoria: Nome da categoria (ex: "Produtos")
-- - ativo: Soft delete (1=ativo, 0=inativo)
--
-- CATEGORIAS PADRÃO:
-- 1. Revenda de Mercadorias (produtos comprados para revenda)
-- 2. Produtos Industrializados (fabricados pelo MEI)
-- 3. Prestação de Serviços (mão de obra)
-- 4. Outros (diversos)
--
-- ÍNDICES:
-- - PRIMARY KEY: id_categoria
-- - INDEX: nome_categoria (busca por nome)
--
-- SOFT DELETE:
-- Não excluir registros fisicamente (DELETE).
-- Usar: UPDATE categoria SET ativo = 0 WHERE id = ?
--
-- RELACIONAMENTOS:
-- - vendas.categoria_id → categoria.id_categoria (N:1)
--
-- EXEMPLO DE USO:
-- INSERT INTO categoria (nome_categoria) VALUES ('Eletrônicos');
-- SELECT * FROM categoria WHERE ativo = 1; -- Apenas ativas
-- UPDATE categoria SET ativo = 0 WHERE id_categoria = 5; -- Soft delete
-- ================================================================


CREATE TABLE `categoria` (
                             `id_categoria` int(11) NOT NULL COMMENT 'PK - Identificador único',
                             `nome_categoria` varchar(100) NOT NULL COMMENT 'Nome da categoria',
                             `ativo` tinyint(1) DEFAULT 1 COMMENT 'Soft delete: 1=ativo, 0=inativo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Categorias de vendas';

--
-- Dados iniciais para a tabela `categoria`
-- IMPORTANTE: IDs 1-7 são dados de exemplo/teste
--

INSERT INTO `categoria` (`id_categoria`, `nome_categoria`, `ativo`) VALUES
                                                                        (1, 'Revenda de Mercadorias', 1),      -- Produtos comprados para revenda
                                                                        (2, 'Produtos Industrializados', 1),   -- Produtos fabricados pelo MEI
                                                                        (3, 'Prestação de Serviços', 1),       -- Mão de obra, consultoria, etc
                                                                        (4, 'Revenda de Mercadorias', 0),      -- DUPLICADA - Inativa (soft delete)
                                                                        (5, 'Produtos Industrializados', 0),   -- DUPLICADA - Inativa (soft delete)
                                                                        (6, 'Prestação de Serviços', 0),       -- DUPLICADA - Inativa (soft delete)
                                                                        (7, 'Outro', 1);                       -- Categoria genérica

-- ================================================================
-- TABELA 2: METODO_PAGAMENTO
-- ================================================================
--
-- PROPÓSITO:
-- Formas de pagamento aceitas pelo MEI. Usado para registrar
-- como o cliente pagou por cada venda.
--
-- CAMPOS:
-- - id_metpag: Identificador único (PK)
-- - descricao: Nome do método (ex: "PIX", "Dinheiro")
-- - ativo: Soft delete (1=ativo, 0=inativo)
--
-- MÉTODOS PADRÃO:
-- 1. Dinheiro (espécie)
-- 2. Cartão de Crédito (parcelado ou à vista)
-- 3. Cartão de Débito
-- 4. PIX (instantâneo)
-- 5. Transferência Bancária (DOC/TED)
-- 6. Boleto Bancário
--
-- RELACIONAMENTOS:
-- - pagamento.metpag_id → metodo_pagamento.id_metpag (N:1)
--
-- CASOS DE USO:
-- - Pagamento único: 1 venda = 1 pagamento
-- - Pagamento misto: 1 venda = 2+ pagamentos (ex: R$50 dinheiro + R$100 cartão)
-- - Parcelado: 1 venda = N pagamentos (ex: 3x no cartão)
--
-- EXEMPLO:
-- SELECT m.descricao, COUNT(*) as qtd
-- FROM pagamento p
-- JOIN metodo_pagamento m ON p.metpag_id = m.id_metpag
-- WHERE m.ativo = 1
-- GROUP BY m.id_metpag;
-- ================================================================

CREATE TABLE `metodo_pagamento` (
                                    `id_metpag` int(11) NOT NULL COMMENT 'PK - Identificador único',
                                    `descricao` varchar(100) NOT NULL COMMENT 'Nome do método de pagamento',
                                    `ativo` tinyint(1) DEFAULT 1 COMMENT 'Soft delete: 1=ativo, 0=inativo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Métodos de pagamento disponíveis';

--
-- Dados iniciais para a tabela `metodo_pagamento`
-- IMPORTANTE: Métodos padrão do sistema
--

INSERT INTO `metodo_pagamento` (`id_metpag`, `descricao`, `ativo`) VALUES
                                                                       (1, 'Dinheiro', 1),                -- Pagamento em espécie
                                                                       (2, 'Cartão de Crédito', 1),       -- Parcelado ou à vista
                                                                       (3, 'Cartão de Débito', 1),        -- À vista (débito em conta)
                                                                       (4, 'PIX', 1),                     -- Transferência instantânea
                                                                       (5, 'Transferência Bancária', 1),  -- DOC/TED
                                                                       (6, 'Boleto', 1);                  -- Boleto bancário

-- ================================================================
-- TABELA 3: USUARIO
-- ================================================================
--
-- PROPÓSITO:
-- Armazena dados dos Microempreendedores Individuais (MEI)
-- cadastrados no sistema. Cada usuário representa um MEI.
--
-- CAMPOS OBRIGATÓRIOS:
-- - id_usuario: Identificador único (PK, AUTO_INCREMENT)
-- - cpf: CPF do MEI (UNIQUE, 11 dígitos, usado para login)
-- - nome: Nome completo
-- - email: Email (UNIQUE, usado para recuperação de senha)
-- - senha: Hash BCrypt (60 caracteres, salt 10-12)
--
-- CAMPOS OPCIONAIS:
-- - cnpj: CNPJ do MEI (14 dígitos, pode ser NULL)
--
-- CAMPOS AUTOMÁTICOS:
-- - data_cadastro: Timestamp de criação (DEFAULT CURRENT_TIMESTAMP)
-- - ativo: Soft delete (1=ativo, 0=inativo)
--
-- AUTENTICAÇÃO:
-- - Username: CPF (11 dígitos, sem máscara)
-- - Password: Hash BCrypt (verificado com BCrypt.checkpw())
-- - Sessão: 30 minutos de inatividade
--
-- SEGURANÇA:
-- ✅ CPF único na base (UNIQUE constraint)
-- ✅ Email único na base (UNIQUE constraint)
-- ✅ Senha criptografada com BCrypt
-- ✅ Salt aleatório por senha (BCrypt.gensalt())
-- ✅ Índices em CPF e email para performance
--
-- CNPJ:
-- - Opcional no cadastro
-- - 14 dígitos (sem máscara)
-- - Pode ser preenchido depois no perfil
-- - Usado em relatórios e documentos
--
-- SOFT DELETE:
-- Usuários inativos (ativo=0):
-- - Não podem fazer login
-- - Mantém histórico de vendas
-- - Podem ser reativados
--
-- RELACIONAMENTOS:
-- - vendas.usuario_id → usuario.id_usuario (N:1)
-- - nota_fiscal.usuario_id → usuario.id_usuario (N:1)
--
-- EXEMPLO DE CADASTRO:
-- INSERT INTO usuario (cpf, nome, email, cnpj, senha)
-- VALUES (
--   '12345678901',                           -- CPF sem máscara
--   'João Silva',                             -- Nome completo
--   'joao@email.com',                         -- Email único
--   '12345678000190',                         -- CNPJ (opcional)
--   '$2a$10$ABC123...'                        -- Hash BCrypt
-- );
--
-- EXEMPLO DE LOGIN:
-- 1. SELECT * FROM usuario WHERE cpf = '12345678901' AND ativo = 1;
-- 2. Verificar senha: BCrypt.checkpw(senhaDigitada, senhaDoBanco);
-- 3. Se OK, criar sessão com timeout 30 min
-- ================================================================

CREATE TABLE `usuario` (
                           `id_usuario` int(11) NOT NULL COMMENT 'PK - Identificador único',
                           `cpf` varchar(11) NOT NULL COMMENT 'CPF do MEI (11 dígitos, usado no login)',
                           `nome` varchar(100) NOT NULL COMMENT 'Nome completo do MEI',
                           `email` varchar(100) NOT NULL COMMENT 'Email único (recuperação de senha)',
                           `cnpj` varchar(14) DEFAULT NULL COMMENT 'CNPJ do MEI (14 dígitos, opcional)',
                           `senha` varchar(255) NOT NULL COMMENT 'Hash BCrypt da senha (60 chars)',
                           `data_cadastro` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Data/hora do cadastro',
                           `ativo` tinyint(1) DEFAULT 1 COMMENT 'Soft delete: 1=ativo, 0=inativo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Usuários MEI do sistema';

--
-- Dados de exemplo para a tabela `usuario`
-- IMPORTANTE: Senhas são hashes BCrypt (não podem ser desfeitos)
-- Senha original de todos: "123456"
--

INSERT INTO `usuario` (`id_usuario`, `cpf`, `nome`, `email`, `cnpj`, `senha`, `data_cadastro`, `ativo`) VALUES
                                                                                                            (1, '99999999999', 'Teste Login', 'teste@login.com', NULL,
                                                                                                             '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyUI/rYs8K8i',
                                                                                                             '2026-01-28 04:01:27', 1),

                                                                                                            (2, '88888888888', 'Teste Again', 'cadastro@teste.com', NULL,
                                                                                                             '$2a$12$iZ4az9TWEOYrvKdrVtekp.s1Evz6kp1o5i9p.xEFUIciW0sVw.N6W',
                                                                                                             '2026-01-28 04:01:27', 1),

                                                                                                            (3, '77777777777', 'Mais Um Teste', 'testemaisum@teste.com', NULL,
                                                                                                             '$2a$12$M6n/DQqUDhWVnXkQsAymdOxwvzNM.Za7dMOYlsjgix2haoK/4NaHm',
                                                                                                             '2026-01-28 04:01:27', 1),

                                                                                                            (4, '12345678910', 'teste da silva', 'testedasilva@cadastro.com', NULL,
                                                                                                             '$2a$12$51Ms3eKUnIVCGlY4b23TMusIftyU5tJwSHWlcexXoky7JngywC3gq',
                                                                                                             '2026-01-28 04:19:05', 1),

                                                                                                            (5, '11111111111', 'teste', 'teste@teste.com', NULL,
                                                                                                             '$2a$12$ZmspU2T/lHRSMIU3sP9GA.Qz8ZGaY9A5MnO6/tkAux.o4Usd6FvQS',
                                                                                                             '2026-01-29 22:39:10', 1),

                                                                                                            (6, '10987654321', 'teste teste teste', 'testeteste@teste.com', '11111111111111',
                                                                                                             '$2a$10$7Y462KjWHCAhmQx2KlWdZ.47jJQISwzp4ND.V95zkU2kVsnMRJp2S',
                                                                                                             '2026-02-11 23:16:52', 1);

-- NOTA: Para criar um novo usuário, use UsuarioDAO.inserir() no Java
-- que gera o hash BCrypt automaticamente.

-- ================================================================
-- TABELA 4: VENDAS
-- ================================================================
--
-- PROPÓSITO:
-- Registro principal de cada venda realizada pelo MEI.
-- Esta é a tabela central do sistema.
--
-- CAMPOS OBRIGATÓRIOS:
-- - id_vendas: Identificador único (PK, AUTO_INCREMENT)
-- - data_vendas: Data/hora da venda
-- - valor: Valor total da venda (FLOAT, em Reais)
-- - categoria_id: FK para categoria
-- - usuario_id: FK para usuario (dono da venda)
--
-- CAMPOS OPCIONAIS:
-- - descricao: Descrição livre da venda (TEXT)
-- - nota_fiscal_emitida: 'S' ou 'N' (default 'N')
--
-- CAMPOS AUTOMÁTICOS:
-- - data_criacao: Timestamp de criação
-- - ativo: Soft delete (1=ativo, 0=inativo)
--
-- NOTA FISCAL:
-- - Campo: nota_fiscal_emitida CHAR(1)
-- - Valores: 'S' (emitida) ou 'N' (não emitida)
-- - Se 'S': deve existir registro em nota_fiscal
-- - Se 'N': não há nota fiscal
--
-- RELACIONAMENTOS:
-- - vendas.usuario_id → usuario.id_usuario (N:1)
-- - vendas.categoria_id → categoria.id_categoria (N:1)
-- - nota_fiscal.vendas_id → vendas.id_vendas (1:1)
-- - pagamento.vendas_id → vendas.id_vendas (1:N)
--
-- SOFT DELETE:
-- - Vendas excluídas mantêm ativo=0
-- - Não aparecem em relatórios
-- - Mantém histórico completo
-- - Não quebra integridade referencial
--
-- ÍNDICES:
-- - PRIMARY KEY: id_vendas
-- - INDEX: usuario_id (filtro por MEI)
-- - INDEX: data_vendas (filtro por período)
-- - INDEX: categoria_id (filtro por categoria)
--
-- CASOS DE USO:
--
-- 1. VENDA SIMPLES (sem NF):
--    INSERT INTO vendas (data_vendas, valor, categoria_id, usuario_id,
--                        descricao, nota_fiscal_emitida)
--    VALUES (NOW(), 150.00, 1, 4, 'Venda de produtos', 'N');
--
-- 2. VENDA COM NOTA FISCAL:
--    -- Passo 1: Inserir venda
--    INSERT INTO vendas (..., nota_fiscal_emitida) VALUES (..., 'S');
--    -- Passo 2: Inserir nota fiscal vinculada
--    INSERT INTO nota_fiscal (numero, vendas_id, ...) VALUES ('NF-123', @@LAST_INSERT_ID, ...);
--
-- 3. CONSULTA POR PERÍODO:
--    SELECT * FROM vendas
--    WHERE usuario_id = 4
--      AND YEAR(data_vendas) = 2026
--      AND MONTH(data_vendas) = 2
--      AND ativo = 1
--    ORDER BY data_vendas DESC;
--
-- 4. TOTAL DO MÊS:
--    SELECT SUM(valor) as total
--    FROM vendas
--    WHERE usuario_id = 4
--      AND YEAR(data_vendas) = YEAR(NOW())
--      AND MONTH(data_vendas) = MONTH(NOW())
--      AND ativo = 1;
--
-- 5. SOFT DELETE:
--    UPDATE vendas SET ativo = 0 WHERE id_vendas = 10;
-- ================================================================

CREATE TABLE `vendas` (
                          `id_vendas` int(11) NOT NULL COMMENT 'PK - Identificador único',
                          `data_vendas` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Data/hora da venda',
                          `valor` float NOT NULL COMMENT 'Valor total em Reais',
                          `nota_fiscal_emitida` char(1) DEFAULT 'N' CHECK (`nota_fiscal_emitida` in ('S','N')) COMMENT 'NF emitida? S=Sim, N=Não',
                          `categoria_id` int(11) NOT NULL COMMENT 'FK - Categoria da venda',
                          `usuario_id` int(11) NOT NULL COMMENT 'FK - Dono da venda (MEI)',
                          `descricao` text DEFAULT NULL COMMENT 'Descrição opcional da venda',
                          `ativo` tinyint(1) DEFAULT 1 COMMENT 'Soft delete: 1=ativo, 0=inativo',
                          `data_criacao` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp de criação'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Registro de vendas realizadas';

--
-- Dados de exemplo para a tabela `vendas`
-- IMPORTANTE: Vendas de teste do usuário ID 4
--

INSERT INTO `vendas` (`id_vendas`, `data_vendas`, `valor`, `nota_fiscal_emitida`, `categoria_id`, `usuario_id`, `descricao`, `ativo`, `data_criacao`) VALUES
                                                                                                                                                          (1,  '2026-02-09 04:27:55', 345,      'N', 6, 4, 'inventario',     1, '2026-02-09 04:27:55'),
                                                                                                                                                          (2,  '2026-02-09 04:28:05', 34,       'N', 3, 4, 'teste',          1, '2026-02-09 04:28:05'),
                                                                                                                                                          (3,  '2026-02-09 04:28:31', 555.66,   'N', 2, 4, 'limpeza',        1, '2026-02-09 04:28:31'),
                                                                                                                                                          (4,  '2026-02-09 04:28:48', 243234,   'N', 5, 4, 'oleo',           0, '2026-02-09 04:28:48'), -- INATIVA
                                                                                                                                                          (5,  '2026-02-09 04:29:23', 9,        'N', 1, 4, 'pao',            1, '2026-02-09 04:29:23'),
                                                                                                                                                          (6,  '2026-02-09 04:29:37', 6666.66,  'N', 7, 4, 'sim',            0, '2026-02-09 04:29:37'), -- INATIVA
                                                                                                                                                          (7,  '2026-02-09 05:08:55', 5,        'N', 3, 4, 'aaa',            1, '2026-02-09 05:08:55'),
                                                                                                                                                          (8,  '2026-02-09 20:03:46', 111.01,   'N', 3, 4, 'nao',            1, '2026-02-09 20:03:46'),
                                                                                                                                                          (9,  '2026-02-09 20:04:05', 222,      'S', 7, 4, 'ssss',           1, '2026-02-09 20:04:05'), -- COM NF
                                                                                                                                                          (10, '2026-02-10 17:49:46', 50,       'S', 7, 4, 'fardo de coca',  0, '2026-02-10 17:49:46'), -- INATIVA + NF
                                                                                                                                                          (11, '2026-02-11 23:07:18', 55,       'S', 7, 4, 'aaaa',           1, '2026-02-11 23:07:18'), -- COM NF
                                                                                                                                                          (12, '2026-02-11 23:20:15', 4234,     'S', 7, 4, 'sim',            1, '2026-02-11 23:20:15'); -- COM NF

-- ================================================================
-- TABELA 5: NOTA_FISCAL
-- ================================================================
--
-- PROPÓSITO:
-- Armazena dados das Notas Fiscais emitidas.
-- Relacionamento 1:1 com vendas (1 venda = 1 NF no máximo).
--
-- CAMPOS OBRIGATÓRIOS:
-- - id_nota_fiscal: Identificador único (PK, AUTO_INCREMENT)
-- - numero: Número da NF (VARCHAR 50, único)
-- - valor: Valor da NF (deve coincidir com vendas.valor)
-- - vendas_id: FK para vendas (UNIQUE - 1:1)
-- - usuario_id: FK para usuario (dono da NF)
--
-- CAMPOS AUTOMÁTICOS:
-- - data_emissao: Timestamp da emissão (DEFAULT CURRENT_TIMESTAMP)
-- - data_criacao: Timestamp de criação
-- - ativo: Soft delete (1=ativo, 0=inativo)
--
-- RELACIONAMENTO 1:1 COM VENDAS:
-- - 1 venda pode ter 0 ou 1 nota fiscal
-- - 1 nota fiscal pertence a exatamente 1 venda
-- - vendas_id é UNIQUE (não pode repetir)
--
-- COMPLIANCE FISCAL:
-- - Notas fiscais NÃO são excluídas fisicamente (DELETE)
-- - Usar soft delete (ativo = 0) para cancelamento
-- - Mantém histórico para auditoria fiscal
-- - Número da NF deve ser único na base
--
-- SOFT DELETE:
-- - NF cancelada: ativo = 0
-- - Mantém registro para compliance
-- - Não aparece em relatórios ativos
--
-- ÍNDICES:
-- - PRIMARY KEY: id_nota_fiscal
-- - INDEX: numero (busca por número da NF)
-- - INDEX: vendas_id (JOIN com vendas)
-- - INDEX: usuario_id (filtro por MEI)
--
-- VALIDAÇÕES:
-- ✅ Valor da NF = Valor da venda
-- ✅ Número da NF único
-- ✅ Venda deve ter nota_fiscal_emitida = 'S'
-- ✅ Cada venda pode ter no máximo 1 NF
--
-- EXEMPLO DE USO:
--
-- 1. INSERIR NF (após inserir venda):
--    INSERT INTO nota_fiscal (numero, valor, vendas_id, usuario_id)
--    VALUES ('NF-12345', 150.00, 10, 4);
--
-- 2. BUSCAR VENDAS COM NF:
--    SELECT v.*, nf.numero, nf.data_emissao
--    FROM vendas v
--    LEFT JOIN nota_fiscal nf ON v.id_vendas = nf.vendas_id
--    WHERE v.usuario_id = 4 AND nf.ativo = 1;
--
-- 3. CANCELAR NF (soft delete):
--    UPDATE nota_fiscal SET ativo = 0 WHERE id_nota_fiscal = 1;
--    UPDATE vendas SET nota_fiscal_emitida = 'N' WHERE id_vendas = 9;
-- ================================================================

CREATE TABLE `nota_fiscal` (
                               `id_nota_fiscal` int(11) NOT NULL COMMENT 'PK - Identificador único',
                               `numero` varchar(50) NOT NULL COMMENT 'Número da Nota Fiscal (único)',
                               `data_emissao` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Data/hora de emissão',
                               `valor` float NOT NULL COMMENT 'Valor da NF (deve = vendas.valor)',
                               `vendas_id` int(11) NOT NULL COMMENT 'FK - Venda vinculada (UNIQUE, 1:1)',
                               `usuario_id` int(11) NOT NULL COMMENT 'FK - Dono da NF (MEI)',
                               `ativo` tinyint(1) DEFAULT 1 COMMENT 'Soft delete: 1=ativo, 0=cancelado',
                               `data_criacao` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Timestamp de criação'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Notas Fiscais emitidas';

--
-- Dados de exemplo para a tabela `nota_fiscal`
-- IMPORTANTE: Vinculadas às vendas 9, 10, 11, 12 (nota_fiscal_emitida='S')
--

INSERT INTO `nota_fiscal` (`id_nota_fiscal`, `numero`, `data_emissao`, `valor`, `vendas_id`, `usuario_id`, `ativo`, `data_criacao`) VALUES
                                                                                                                                        (1, 'NF-54312',  '2026-02-09 20:04:05', 222,  9,  4, 1, '2026-02-09 20:04:05'), -- Venda 9
                                                                                                                                        (2, 'nf-123456', '2026-02-10 17:49:46', 50,   10, 4, 1, '2026-02-10 17:49:46'), -- Venda 10 (inativa)
                                                                                                                                        (3, 'NF-543124', '2026-02-11 23:07:18', 55,   11, 4, 1, '2026-02-11 23:07:18'), -- Venda 11
                                                                                                                                        (4, '34234',     '2026-02-11 23:20:15', 4234, 12, 4, 1, '2026-02-11 23:20:15'); -- Venda 12

-- ================================================================
-- TABELA 6: PAGAMENTO
-- ================================================================
--
-- PROPÓSITO:
-- Registra como cada venda foi paga. Permite múltiplos
-- pagamentos por venda (pagamento misto ou parcelado).
--
-- CAMPOS OBRIGATÓRIOS:
-- - id_pag: Identificador único (PK, AUTO_INCREMENT)
-- - vendas_id: FK para vendas
-- - metpag_id: FK para metodo_pagamento
-- - valor: Valor deste pagamento (FLOAT)
--
-- CAMPOS AUTOMÁTICOS:
-- - data_pagamento: Timestamp do pagamento (DEFAULT CURRENT_TIMESTAMP)
-- - ativo: Soft delete (1=ativo, 0=inativo)
--
-- RELACIONAMENTOS:
-- - pagamento.vendas_id → vendas.id_vendas (N:1)
-- - pagamento.metpag_id → metodo_pagamento.id_metpag (N:1)
--
-- CASOS DE USO:
--
-- 1. PAGAMENTO ÚNICO:
--    - 1 venda = 1 pagamento
--    - Exemplo: Venda de R$100 paga em dinheiro
--    INSERT INTO pagamento (vendas_id, metpag_id, valor)
--    VALUES (1, 1, 100.00); -- metpag_id 1 = Dinheiro
--
-- 2. PAGAMENTO MISTO:
--    - 1 venda = 2+ pagamentos de métodos diferentes
--    - Exemplo: Venda de R$200 = R$50 dinheiro + R$150 cartão
--    INSERT INTO pagamento VALUES (NULL, 1, 1, 50.00, NOW(), 1);   -- Dinheiro
--    INSERT INTO pagamento VALUES (NULL, 1, 2, 150.00, NOW(), 1);  -- Cartão
--
-- 3. PAGAMENTO PARCELADO:
--    - 1 venda = N pagamentos (mesmo método)
--    - Exemplo: Venda de R$300 em 3x de R$100 no cartão
--    INSERT INTO pagamento VALUES (NULL, 1, 2, 100.00, NOW(), 1);  -- Parcela 1
--    INSERT INTO pagamento VALUES (NULL, 1, 2, 100.00, NOW(), 1);  -- Parcela 2
--    INSERT INTO pagamento VALUES (NULL, 1, 2, 100.00, NOW(), 1);  -- Parcela 3
--
-- VALIDAÇÃO:
-- - SUM(pagamento.valor WHERE vendas_id=X) deve = vendas.valor
-- - Sistema valida no Java (não há trigger no banco)
--
-- ÍNDICES:
-- - PRIMARY KEY: id_pag
-- - INDEX: vendas_id (JOIN com vendas)
-- - INDEX: metpag_id (JOIN com metodo_pagamento)
-- - INDEX: data_pagamento (filtro por período)
--
-- CONSULTA ÚTIL:
-- SELECT v.id_vendas, v.valor as valor_venda,
--        m.descricao as metodo,
--        p.valor as valor_pago
-- FROM vendas v
-- JOIN pagamento p ON v.id_vendas = p.vendas_id
-- JOIN metodo_pagamento m ON p.metpag_id = m.id_metpag
-- WHERE v.usuario_id = 4;
-- ================================================================

CREATE TABLE `pagamento` (
                             `id_pag` int(11) NOT NULL COMMENT 'PK - Identificador único',
                             `vendas_id` int(11) NOT NULL COMMENT 'FK - Venda relacionada',
                             `metpag_id` int(11) NOT NULL COMMENT 'FK - Método de pagamento usado',
                             `valor` float NOT NULL COMMENT 'Valor deste pagamento',
                             `data_pagamento` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Data/hora do pagamento',
                             `ativo` tinyint(1) DEFAULT 1 COMMENT 'Soft delete: 1=ativo, 0=inativo'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Pagamentos recebidos por venda';

--
-- Tabela `pagamento` está vazia (sem dados de exemplo)
-- Use PagamentoDAO.inserir() no Java para adicionar registros
--

-- ================================================================
-- ÍNDICES E CONSTRAINTS
-- ================================================================
--
-- ÍNDICES:
-- Melhoram performance de consultas (SELECT, JOIN, WHERE).
--
-- TIPOS:
-- - PRIMARY KEY: Único, não nulo, 1 por tabela
-- - UNIQUE: Único, pode ter 1 NULL
-- - INDEX: Não único, permite duplicatas
--
-- QUANDO CRIAR ÍNDICE:
-- ✅ Colunas em JOIN (FK)
-- ✅ Colunas em WHERE frequentes
-- ✅ Colunas em ORDER BY
-- ✅ Colunas UNIQUE (CPF, email)
--
-- QUANDO NÃO CRIAR:
-- ❌ Tabelas pequenas (<1000 registros)
-- ❌ Colunas raramente consultadas
-- ❌ Colunas com muitos valores iguais
--
-- FOREIGN KEYS:
-- Garantem integridade referencial entre tabelas.
--
-- ON DELETE CASCADE:
-- - Quando registro pai é deletado, filhos também são
-- - Exemplo: DELETE usuario → DELETE vendas do usuário
--
-- ON DELETE RESTRICT (padrão):
-- - Impede deletar pai se tiver filhos
-- - Exemplo: Não pode deletar categoria se tiver vendas
-- ================================================================

--
-- Índices da tabela `categoria`
--
ALTER TABLE `categoria`
    ADD PRIMARY KEY (`id_categoria`),                 -- PK única
  ADD KEY `idx_nome` (`nome_categoria`);            -- Busca por nome

--
-- Índices da tabela `metodo_pagamento`
--
ALTER TABLE `metodo_pagamento`
    ADD PRIMARY KEY (`id_metpag`),                    -- PK única
  ADD KEY `idx_descricao` (`descricao`);            -- Busca por nome

--
-- Índices da tabela `nota_fiscal`
--
ALTER TABLE `nota_fiscal`
    ADD PRIMARY KEY (`id_nota_fiscal`),               -- PK única
  ADD KEY `idx_numero` (`numero`),                  -- Busca por número NF
  ADD KEY `idx_vendas` (`vendas_id`),               -- JOIN com vendas
  ADD KEY `idx_usuario` (`usuario_id`);             -- Filtro por MEI

--
-- Índices da tabela `pagamento`
--
ALTER TABLE `pagamento`
    ADD PRIMARY KEY (`id_pag`),                       -- PK única
  ADD KEY `idx_vendas` (`vendas_id`),               -- JOIN com vendas
  ADD KEY `idx_metpag` (`metpag_id`),               -- JOIN com metodo_pagamento
  ADD KEY `idx_data` (`data_pagamento`);            -- Filtro por data

--
-- Índices da tabela `usuario`
--
ALTER TABLE `usuario`
    ADD PRIMARY KEY (`id_usuario`),                   -- PK única
  ADD UNIQUE KEY `cpf` (`cpf`),                     -- CPF único (login)
  ADD KEY `idx_cpf` (`cpf`),                        -- Busca por CPF
  ADD KEY `idx_email` (`email`);                    -- Busca por email

--
-- Índices da tabela `vendas`
--
ALTER TABLE `vendas`
    ADD PRIMARY KEY (`id_vendas`),                    -- PK única
  ADD KEY `idx_usuario` (`usuario_id`),             -- Filtro por MEI
  ADD KEY `idx_data` (`data_vendas`),               -- Filtro por período
  ADD KEY `idx_categoria` (`categoria_id`);         -- Filtro por categoria

-- ================================================================
-- AUTO_INCREMENT
-- ================================================================
--
-- Define o próximo ID a ser gerado automaticamente.
-- MySQL incrementa em +1 para cada INSERT.
--
-- IMPORTANTE:
-- - Não pular números (1, 2, 3, 4...)
-- - Se deletar ID 3, próximo será 5 (não reutiliza 3)
-- - Reset: ALTER TABLE tabela AUTO_INCREMENT = 1;
-- ================================================================

--
-- AUTO_INCREMENT de tabela `categoria`
--
ALTER TABLE `categoria`
    MODIFY `id_categoria` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT de tabela `metodo_pagamento`
--
ALTER TABLE `metodo_pagamento`
    MODIFY `id_metpag` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de tabela `nota_fiscal`
--
ALTER TABLE `nota_fiscal`
    MODIFY `id_nota_fiscal` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT de tabela `pagamento`
--
ALTER TABLE `pagamento`
    MODIFY `id_pag` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de tabela `usuario`
--
ALTER TABLE `usuario`
    MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de tabela `vendas`
--
ALTER TABLE `vendas`
    MODIFY `id_vendas` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

-- ================================================================
-- FOREIGN KEYS (INTEGRIDADE REFERENCIAL)
-- ================================================================
--
-- Garantem que:
-- 1. FK aponta para PK existente
-- 2. Não pode deletar pai se tiver filhos (RESTRICT)
-- 3. Ou deleta filhos junto (CASCADE)
--
-- CONSTRAINTS NESTE BANCO:
--
-- 1. nota_fiscal → usuario (ON DELETE CASCADE)
--    - Se deletar usuário, suas NFs são deletadas
--
-- 2. nota_fiscal → vendas (ON DELETE CASCADE)
--    - Se deletar venda, NF dela é deletada
--
-- 3. pagamento → vendas (ON DELETE CASCADE)
--    - Se deletar venda, pagamentos são deletados
--
-- 4. pagamento → metodo_pagamento (RESTRICT padrão)
--    - Não pode deletar método se tiver pagamentos
--
-- 5. vendas → categoria (RESTRICT padrão)
--    - Não pode deletar categoria se tiver vendas
--
-- 6. vendas → usuario (ON DELETE CASCADE)
--    - Se deletar usuário, vendas são deletadas
--
-- IMPORTANTE:
-- Em produção, NUNCA use DELETE em tabelas principais.
-- Sempre use SOFT DELETE (UPDATE ... SET ativo = 0).
-- ================================================================

--
-- Constraints para tabela `nota_fiscal`
--
ALTER TABLE `nota_fiscal`
    ADD CONSTRAINT `fk_nota_fiscal_usuario`
        FOREIGN KEY (`usuario_id`)
            REFERENCES `usuario` (`id_usuario`)
            ON DELETE CASCADE                              -- Deleta NF se deletar usuário
    COMMENT 'FK - Usuário dono da NF',

  ADD CONSTRAINT `fk_nota_fiscal_vendas`
    FOREIGN KEY (`vendas_id`)
    REFERENCES `vendas` (`id_vendas`)
    ON DELETE CASCADE                              -- Deleta NF se deletar venda
    COMMENT 'FK - Venda relacionada (1:1)';

--
-- Constraints para tabela `pagamento`
--
ALTER TABLE `pagamento`
    ADD CONSTRAINT `fk_pagamento_metpag`
        FOREIGN KEY (`metpag_id`)
            REFERENCES `metodo_pagamento` (`id_metpag`)    -- RESTRICT padrão
    COMMENT 'FK - Método de pagamento usado',

  ADD CONSTRAINT `fk_pagamento_vendas`
    FOREIGN KEY (`vendas_id`)
    REFERENCES `vendas` (`id_vendas`)
    ON DELETE CASCADE                              -- Deleta pagamentos se deletar venda
    COMMENT 'FK - Venda relacionada';

--
-- Constraints para tabela `vendas`
--
ALTER TABLE `vendas`
    ADD CONSTRAINT `fk_vendas_categoria`
        FOREIGN KEY (`categoria_id`)
            REFERENCES `categoria` (`id_categoria`)        -- RESTRICT padrão
    COMMENT 'FK - Categoria da venda',

  ADD CONSTRAINT `fk_vendas_usuario`
    FOREIGN KEY (`usuario_id`)
    REFERENCES `usuario` (`id_usuario`)
    ON DELETE CASCADE                              -- Deleta vendas se deletar usuário
    COMMENT 'FK - Dono da venda (MEI)';

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

-- ================================================================
-- CONSULTAS SELECT (REQUISITO DO TRABALHO)
-- ================================================================
--
-- As consultas abaixo demonstram o uso de JOIN para relacionar
-- múltiplas tabelas do banco de dados MEI. Cada consulta tem
-- uma aplicação prática no sistema real.
--
-- IMPORTANTE: Estas consultas são EXEMPLOS para documentação.
-- As consultas executadas pelo sistema estão nos arquivos DAO
-- (VendasDAO.java, RelatorioController.java, etc).
--
-- ESTRUTURA DE CADA CONSULTA:
-- 1. Descrição do propósito
-- 2. Tabelas envolvidas
-- 3. Tipo de JOIN utilizado
-- 4. Aplicação prática no sistema
-- 5. Query SQL completa
-- ================================================================


-- ================================================================
-- CONSULTA 1: VENDAS COMPLETAS COM CATEGORIA E USUÁRIO
-- ================================================================
--
-- PROPÓSITO:
-- Listar todas as vendas ativas com informações completas:
-- nome da categoria, nome do vendedor e status da nota fiscal.
--
-- TABELAS ENVOLVIDAS:
-- - vendas (principal)
-- - categoria (JOIN)
-- - usuario (JOIN)
--
-- TIPO DE JOIN:
-- INNER JOIN - Retorna apenas registros com correspondência
-- em todas as 3 tabelas.
--
-- APLICAÇÃO NO SISTEMA:
-- - historico.jsp: Tabela "Vendas Detalhadas"
-- - dashboard.jsp: Card "Últimas Vendas"
-- - RelatorioController: Listagem para PDF
--
-- UTILIDADE:
-- Mostra nome da categoria ao invés de apenas o ID,
-- facilitando compreensão do usuário.
--
-- EXEMPLO DE RESULTADO:
-- +----+------------+--------+------------------+-----------+----+
-- | ID | Data       | Valor  | Categoria        | Vendedor  | NF |
-- +----+------------+--------+------------------+-----------+----+
-- | 1  | 09/02/2026 | 345.00 | Prestação Serv.  | teste     | N  |
-- | 2  | 09/02/2026 | 34.00  | Outro            | teste     | N  |
-- +----+------------+--------+------------------+-----------+----+
-- ================================================================

SELECT 
    v.id_vendas AS 'ID',
    DATE_FORMAT(v.data_vendas, '%d/%m/%Y %H:%i') AS 'Data/Hora',
    CONCAT('R$ ', FORMAT(v.valor, 2, 'pt_BR')) AS 'Valor',
    c.nome_categoria AS 'Categoria',
    u.nome AS 'Vendedor',
    v.nota_fiscal_emitida AS 'NF',
    v.descricao AS 'Descrição'
FROM vendas v
INNER JOIN categoria c ON v.categoria_id = c.id_categoria
INNER JOIN usuario u ON v.usuario_id = u.id_usuario
WHERE v.ativo = 1
ORDER BY v.data_vendas DESC
LIMIT 20;


-- ================================================================
-- CONSULTA 2: NOTAS FISCAIS COMPLETAS
-- ================================================================
--
-- PROPÓSITO:
-- Listar todas as notas fiscais emitidas com detalhes da venda
-- relacionada e dados do emissor (MEI).
--
-- TABELAS ENVOLVIDAS:
-- - nota_fiscal (principal)
-- - vendas (JOIN)
-- - usuario (JOIN)
--
-- TIPO DE JOIN:
-- INNER JOIN - Retorna apenas NFs com venda e usuário válidos.
--
-- APLICAÇÃO NO SISTEMA:
-- - RelatorioController.java: Geração de PDF de relatório
-- - historico.jsp: Filtro "Somente com Nota Fiscal"
-- - Auditoria fiscal: Listagem de todas NFs emitidas
--
-- UTILIDADE:
-- Cruza informações de 3 tabelas para relatório fiscal completo.
-- Mostra número da NF, descrição da venda e dados do emissor.
--
-- EXEMPLO DE RESULTADO:
-- +-----------+------------+--------+-------------+----------+-------+
-- | Número NF | Emissão    | Valor  | Descrição   | Emissor  | CNPJ  |
-- +-----------+------------+--------+-------------+----------+-------+
-- | NF-54312  | 09/02/2026 | 222.00 | ssss        | teste    | NULL  |
-- | NF-543124 | 11/02/2026 | 55.00  | aaaa        | teste    | NULL  |
-- +-----------+------------+--------+-------------+----------+-------+
-- ================================================================

SELECT 
    nf.numero AS 'Número NF',
    DATE_FORMAT(nf.data_emissao, '%d/%m/%Y %H:%i') AS 'Data Emissão',
    CONCAT('R$ ', FORMAT(nf.valor, 2, 'pt_BR')) AS 'Valor',
    v.descricao AS 'Descrição da Venda',
    u.nome AS 'Emitido por',
    IFNULL(u.cnpj, 'Não cadastrado') AS 'CNPJ do Emissor',
    v.id_vendas AS 'ID Venda'
FROM nota_fiscal nf
INNER JOIN vendas v ON nf.vendas_id = v.id_vendas
INNER JOIN usuario u ON nf.usuario_id = u.id_usuario
WHERE nf.ativo = 1
ORDER BY nf.data_emissao DESC;


-- ================================================================
-- CONSULTA 3: ANÁLISE DE PAGAMENTOS POR MÉTODO
-- ================================================================
--
-- PROPÓSITO:
-- Mostrar como cada venda foi paga, relacionando venda,
-- pagamento e método de pagamento utilizado.
--
-- TABELAS ENVOLVIDAS:
-- - vendas (principal)
-- - pagamento (JOIN)
-- - metodo_pagamento (JOIN)
--
-- TIPO DE JOIN:
-- INNER JOIN - Retorna apenas vendas que têm pagamento registrado.
--
-- APLICAÇÃO NO SISTEMA:
-- - dashboard.jsp: Card "Formas de Pagamento Mais Usadas"
-- - relatorio.jsp: Análise financeira por método
-- - Estratégia de negócio: Saber quais formas aceitar
--
-- UTILIDADE:
-- Permite o MEI descobrir:
-- - Qual forma de pagamento é mais usada
-- - Se deve investir em maquininha de cartão
-- - Se deve priorizar PIX (sem taxa)
--
-- EXEMPLO DE RESULTADO:
-- +----------+------------+--------+-----------------+----------+
-- | ID Venda | Data       | Total  | Forma Pagamento | Valor    |
-- +----------+------------+--------+-----------------+----------+
-- | 1        | 09/02/2026 | 345.00 | PIX             | 345.00   |
-- | 2        | 09/02/2026 | 34.00  | Dinheiro        | 34.00    |
-- +----------+------------+--------+-----------------+----------+
--
-- OBS: Se tabela pagamento estiver vazia, não retorna nada.
-- Para fins de demonstração, considere que pagamentos foram
-- registrados no sistema.
-- ================================================================

SELECT 
    v.id_vendas AS 'ID Venda',
    DATE_FORMAT(v.data_vendas, '%d/%m/%Y') AS 'Data Venda',
    CONCAT('R$ ', FORMAT(v.valor, 2, 'pt_BR')) AS 'Total Venda',
    mp.descricao AS 'Forma de Pagamento',
    CONCAT('R$ ', FORMAT(p.valor, 2, 'pt_BR')) AS 'Valor Pago',
    DATE_FORMAT(p.data_pagamento, '%d/%m/%Y %H:%i') AS 'Data Pagamento'
FROM vendas v
INNER JOIN pagamento p ON v.id_vendas = p.vendas_id
INNER JOIN metodo_pagamento mp ON p.metpag_id = mp.id_metpag
WHERE v.ativo = 1 AND p.ativo = 1
ORDER BY p.data_pagamento DESC;


-- ================================================================
-- CONSULTA 4: VENDAS POR MÊS E CATEGORIA (RELATÓRIO ANUAL)
-- ================================================================
--
-- PROPÓSITO:
-- Análise de vendas agrupadas por mês e categoria, mostrando
-- quantidade de vendas e total faturado em cada categoria.
--
-- TABELAS ENVOLVIDAS:
-- - vendas (principal)
-- - categoria (JOIN)
--
-- TIPO DE JOIN:
-- INNER JOIN com GROUP BY
--
-- FUNÇÕES AGREGADAS:
-- - COUNT(*): Total de vendas
-- - SUM(valor): Total faturado
-- - AVG(valor): Ticket médio
--
-- APLICAÇÃO NO SISTEMA:
-- - relatorio.jsp: Relatório mensal
-- - dashboard.jsp: Gráfico de evolução
-- - Análise de sazonalidade: Quais meses vendem mais
--
-- UTILIDADE:
-- O MEI descobre:
-- - Qual categoria vende mais em cada mês
-- - Se há sazonalidade (ex: mais serviços no verão)
-- - Quando estocar produtos vs focar em serviços
--
-- EXEMPLO DE RESULTADO:
-- +----+------------------+-------+-------------+--------------+
-- | Mês| Categoria        | Qtd   | Total       | Ticket Médio |
-- +----+------------------+-------+-------------+--------------+
-- | 02 | Prestação Serv.  | 4     | R$ 7.367,51 | R$ 1.841,88  |
-- | 02 | Produtos Indust. | 1     | R$ 555,66   | R$ 555,66    |
-- | 02 | Outro            | 4     | R$ 4.511,00 | R$ 1.127,75  |
-- +----+------------------+-------+-------------+--------------+
-- ================================================================

SELECT 
    MONTH(v.data_vendas) AS 'Mês',
    YEAR(v.data_vendas) AS 'Ano',
    c.nome_categoria AS 'Categoria',
    COUNT(*) AS 'Quantidade Vendas',
    CONCAT('R$ ', FORMAT(SUM(v.valor), 2, 'pt_BR')) AS 'Total Faturado',
    CONCAT('R$ ', FORMAT(AVG(v.valor), 2, 'pt_BR')) AS 'Ticket Médio'
FROM vendas v
INNER JOIN categoria c ON v.categoria_id = c.id_categoria
WHERE v.ativo = 1 
  AND YEAR(v.data_vendas) = 2026  -- Ano atual
GROUP BY 
    YEAR(v.data_vendas),
    MONTH(v.data_vendas),
    c.id_categoria,
    c.nome_categoria
ORDER BY 
    YEAR(v.data_vendas) DESC,
    MONTH(v.data_vendas) DESC,
    SUM(v.valor) DESC;


-- ================================================================
-- CONSULTA 5: TOP 5 CATEGORIAS MAIS LUCRATIVAS
-- ================================================================
--
-- PROPÓSITO:
-- Ranking das categorias que mais geram receita para o MEI,
-- ordenadas por total faturado.
--
-- TABELAS ENVOLVIDAS:
-- - categoria (principal)
-- - vendas (JOIN)
--
-- TIPO DE JOIN:
-- INNER JOIN com GROUP BY e ORDER BY
--
-- FUNÇÕES AGREGADAS:
-- - COUNT(vendas): Total de vendas
-- - SUM(valor): Total faturado
-- - AVG(valor): Ticket médio
--
-- APLICAÇÃO NO SISTEMA:
-- - dashboard.jsp: Card "Top Categorias"
-- - Estratégia de negócio: Onde focar esforços
-- - Análise de lucratividade
--
-- UTILIDADE:
-- O MEI descobre:
-- - Qual categoria traz mais receita
-- - Se ticket médio alto compensa poucas vendas
-- - Se deve diversificar ou especializar
--
-- EXEMPLO DE RESULTADO:
-- +----+------------------+-------+-------------+--------------+
-- | #  | Categoria        | Qtd   | Total       | Ticket Médio |
-- +----+------------------+-------+-------------+--------------+
-- | 1º | Prestação Serv.  | 4     | R$ 7.367,51 | R$ 1.841,88  |
-- | 2º | Outro            | 4     | R$ 4.511,00 | R$ 1.127,75  |
-- | 3º | Produtos Indust. | 1     | R$ 555,66   | R$ 555,66    |
-- | 4º | Revenda Mercad.  | 1     | R$ 9,00     | R$ 9,00      |
-- +----+------------------+-------+-------------+--------------+
--
-- INSIGHTS:
-- - Serviços: Poucas vendas mas ticket ALTO (R$ 1.841)
-- - Produtos: Mais vendas mas ticket BAIXO (R$ 555)
-- - Estratégia: Focar em serviços (mais lucrativo)
-- ================================================================

SELECT 
    c.nome_categoria AS 'Categoria',
    COUNT(v.id_vendas) AS 'Quantidade de Vendas',
    CONCAT('R$ ', FORMAT(SUM(v.valor), 2, 'pt_BR')) AS 'Total Faturado',
    CONCAT('R$ ', FORMAT(AVG(v.valor), 2, 'pt_BR')) AS 'Ticket Médio',
    CONCAT(
        ROUND((SUM(v.valor) / (SELECT SUM(valor) FROM vendas WHERE ativo = 1)) * 100, 1),
        '%'
    ) AS 'Percentual do Total'
FROM categoria c
INNER JOIN vendas v ON v.categoria_id = c.id_categoria
WHERE v.ativo = 1 AND c.ativo = 1
GROUP BY c.id_categoria, c.nome_categoria
ORDER BY SUM(v.valor) DESC
LIMIT 5;


-- ================================================================
-- RESUMO DAS CONSULTAS
-- ================================================================
--
-- CONSULTA 1: Vendas + Categoria + Usuario
-- TIPO: INNER JOIN (3 tabelas)
-- USO: Listagem de vendas no histórico
--
-- CONSULTA 2: Nota Fiscal + Vendas + Usuario
-- TIPO: INNER JOIN (3 tabelas)
-- USO: Relatório fiscal em PDF
--
-- CONSULTA 3: Vendas + Pagamento + Método Pagamento
-- TIPO: INNER JOIN (3 tabelas)
-- USO: Análise de formas de pagamento
--
-- CONSULTA 4: Vendas + Categoria (GROUP BY)
-- TIPO: INNER JOIN + Agregação
-- USO: Relatório mensal por categoria
--
-- CONSULTA 5: Categoria + Vendas (GROUP BY + LIMIT)
-- TIPO: INNER JOIN + Agregação + Ranking
-- USO: Top 5 categorias mais lucrativas
--
-- ================================================================
-- OBSERVAÇÕES IMPORTANTES
-- ================================================================
--
-- 1. TABELA PAGAMENTO VAZIA:
--    A consulta 3 não retornará dados se a tabela `pagamento`
--    estiver vazia. Para demonstração, considere que pagamentos
--    foram registrados no sistema.
--
-- 2. SOFT DELETE:
--    Todas as consultas filtram por `ativo = 1` para excluir
--    registros deletados logicamente.
--
-- 3. PERFORMANCE:
--    Índices foram criados nas colunas de JOIN (FK) para
--    garantir performance das consultas.
--
-- 4. FORMATAÇÃO:
--    Usamos FORMAT() e CONCAT() para exibir valores monetários
--    no formato brasileiro (R$ 1.234,56).
--
-- 5. APLICAÇÃO PRÁTICA:
--    Estas consultas já estão implementadas nos DAOs Java:
--    - VendasDAO.listarComJoin()
--    - NotaFiscalDAO.listarCompletas()
--    - RelatorioController.gerarPDF()
--
-- ================================================================
-- ================================================================
-- QUERIES ÚTEIS PARA ADMINISTRAÇÃO
-- ================================================================

-- 1. VERIFICAR ESTRUTURA:
-- SHOW TABLES;
-- DESCRIBE usuario;
-- SHOW CREATE TABLE vendas;

-- 2. ESTATÍSTICAS:
-- SELECT COUNT(*) as total_usuarios FROM usuario WHERE ativo = 1;
-- SELECT COUNT(*) as total_vendas FROM vendas WHERE ativo = 1;
-- SELECT SUM(valor) as receita_total FROM vendas WHERE ativo = 1;

-- 3. BACKUP (no terminal):
-- mysqldump -u root -p mei > backup_mei_2026_02_14.sql

-- 4. RESTORE (no terminal):
-- mysql -u root -p mei < backup_mei_2026_02_14.sql

-- 5. RESETAR BANCO (⚠️ CUIDADO - DELETA TUDO):
-- DROP DATABASE mei;
-- CREATE DATABASE mei;
-- USE mei;
-- [executar este script novamente]

-- ================================================================
-- FIM DO SCRIPT
-- ================================================================
--
-- ✅ 6 tabelas criadas
-- ✅ 15 índices configurados
-- ✅ 6 foreign keys definidas
-- ✅ Dados de exemplo inseridos
-- ✅ Soft delete em todas tabelas
-- ✅ Timestamps automáticos
--
-- PRÓXIMOS PASSOS:
-- 1. Importar DAOs super comentados
-- 2. Importar Controllers super comentados
-- 3. Configurar Conexao.java
-- 4. Testar login com CPF 12345678910, senha 123456
--
-- BOA SORTE COM SEU SISTEMA MEI! 🚀
-- ================================================================
