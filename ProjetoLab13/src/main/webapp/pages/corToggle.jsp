<%--
    THEME TOGGLE - INVERSÃO SIMPLES
    Modo claro = inverte TODAS as cores automaticamente
--%>

<style>
/* Botão do tema */
.theme-toggle-btn {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    border-radius: 8px;
    width: 36px;
    height: 36px;
    cursor: pointer;
    font-size: 18px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    margin-left: 12px;
    transition: transform 0.3s;
    vertical-align: middle;
}

.theme-toggle-btn:hover {
    transform: scale(1.1);
}

/* Garantir que topbar-left usa flexbox */
.topbar-left {
    display: flex !important;
    align-items: center !important;
    gap: 12px !important;
}

/* MODO CLARO - INVERSÃO TOTAL! */
:root.light-mode {
    filter: invert(1) hue-rotate(180deg);
}

/* Reverter inversão em imagens (se tiver) */
:root.light-mode img,
:root.light-mode video {
    filter: invert(1) hue-rotate(180deg);
}
</style>

<script>
function toggleTheme() {
    const root = document.documentElement;
    const icon = document.getElementById('theme-icon');
    
    if (root.classList.contains('light-mode')) {
        root.classList.remove('light-mode');
        localStorage.setItem('theme', 'dark');
        if (icon) icon.textContent = 'L';
    } else {
        root.classList.add('light-mode');
        localStorage.setItem('theme', 'light');
        if (icon) icon.textContent = 'D';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const theme = localStorage.getItem('theme');
    
    // Aplicar tema salvo
    if (theme === 'light') {
        document.documentElement.classList.add('light-mode');
    }
    
    // ADICIONAR BOTÃO AUTOMATICAMENTE
    const topbarLeft = document.querySelector('.topbar-left');
    if (topbarLeft && !document.getElementById('theme-icon')) {
        // Criar botão
        const btn = document.createElement('button');
        btn.className = 'theme-toggle-btn';
        btn.onclick = toggleTheme;
        btn.title = 'Alternar tema';
        
        // Criar ícone
        const span = document.createElement('span');
        span.id = 'theme-icon';
        span.textContent = (theme === 'light') ? 'D' : 'L';
        
        btn.appendChild(span);
        
        // Adicionar ao topbar-left (não ao h2)
        topbarLeft.appendChild(btn);
    }
});
</script>
