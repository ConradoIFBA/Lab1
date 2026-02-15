// ============================================================================
// VALIDAÇÕES E CONFIRMAÇÕES JAVASCRIPT
// ============================================================================

// Confirmar antes de deletar
function confirmarDelecao(tipo, id) {
    const nomeTipo = tipo === 'venda' ? 'Venda' : tipo === 'nota' ? 'Nota Fiscal' : 'Item';

    if (confirm('Tem certeza que deseja deletar esta ' + nomeTipo + '?\nEsta ação não pode ser desfeita!')) {
        if (tipo === 'venda') {
            window.location.href = 'dashboard?acao=deletar&id=' + id;
        } else if (tipo === 'nota') {
            window.location.href = 'notas?acao=deletar&id=' + id;
        }
    }
}

// Formatar valor em tempo real
function formatarValor(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;

    input.addEventListener('input', function() {
        let value = this.value.replace(/\D/g, '');

        if (value.length === 0) {
            this.value = '';
            return;
        }

        value = (parseInt(value) / 100).toFixed(2);
        this.value = value;
    });
}

// Validar formulário de venda
function validarFormularioVenda() {
    const categoria = document.querySelector('[name="categoria"]');
    const valor = document.querySelector('[name="valor"]');

    if (!categoria || !categoria.value) {
        alert('Por favor, selecione uma categoria!');
        return false;
    }

    if (!valor || !valor.value) {
        alert('Por favor, informe o valor!');
        return false;
    }

    const valorNum = parseFloat(valor.value.replace(',', '.'));
    if (isNaN(valorNum) || valorNum <= 0) {
        alert('Valor inválido! Use formato: 123,45');
        return false;
    }

    return true;
}

// Validar alteração de senha
function validarAlteracaoSenha() {
    const senhaAtual = document.querySelector('[name="senhaAtual"]');
    const novaSenha = document.querySelector('[name="novaSenha"]');
    const confirmarSenha = document.querySelector('[name="confirmarSenha"]');

    if (!senhaAtual || !senhaAtual.value) {
        alert('Senha atual é obrigatória!');
        return false;
    }

    if (!novaSenha || !novaSenha.value) {
        alert('Nova senha é obrigatória!');
        return false;
    }

    if (novaSenha.value.length < 6) {
        alert('Nova senha deve ter no mínimo 6 caracteres!');
        return false;
    }

    if (novaSenha.value !== confirmarSenha.value) {
        alert('As senhas não conferem!');
        return false;
    }

    if (senhaAtual.value === novaSenha.value) {
        alert('A nova senha deve ser diferente da senha atual!');
        return false;
    }

    return true;
}

// Validar email
function validarEmail(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

// Validar nome
function validarNome(nome) {
    return nome && nome.trim().length >= 3;
}

// Confirmar ação
function confirmarAcao(mensagem) {
    return confirm(mensagem);
}

// Limpar formulário
function limparFormulario(formId) {
    const form = document.getElementById(formId);
    if (form) {
        form.reset();
    }
}

// Inicializar ao carregar página
document.addEventListener('DOMContentLoaded', function() {
    // Aplicar formatação de valor
    const inputsValor = document.querySelectorAll('input[name="valor"]');
    inputsValor.forEach(function(input) {
        input.addEventListener('input', function() {
            let value = this.value.replace(/\D/g, '');
            if (value.length === 0) {
                this.value = '';
                return;
            }
            value = (parseInt(value) / 100).toFixed(2);
            this.value = value;
        });
    });

    console.log('✅ Validações carregadas!');
});