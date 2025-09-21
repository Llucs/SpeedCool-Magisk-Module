// Configurações de idioma
const translations = {
  pt: {
    title: "SpeedCool",
    subtitle: "Resfriamento inteligente. Desempenho adaptativo. Total controle térmico no seu Android.",
    about_title: "Sobre o SpeedCool",
    about_text: "SpeedCool é um módulo Magisk feito para melhorar o desempenho do seu celular sem deixar ele esquentar demais. Com inteligência térmica, modos por aplicativo e perfis que se adaptam ao uso, ele entrega mais fluidez, menor aquecimento e mais controle sobre o sistema.",
    features_title: "Recursos Principais",
    feature1: "Perfis adaptativos: Eco, Desempenho, Balanceado e Aprendizado",
    feature2: "Modo automático por app em tempo real",
    feature3: "Controle de temperatura com sensores reais",
    feature4: "Motor de aprendizado baseado em horário, uso e carga do sistema",
    feature5: "Backup/restauração de perfis e histórico de desempenho",
    feature6: "Menu interativo via shell/Termux com design amigável",
    feature7: "Sistema anti-conflito que detecta e corrige problemas automaticamente",
    how_to_use_title: "Como Usar o SpeedCool",
    step1: "Baixe e instale o Termux na Play Store ou F-Droid.",
    step2: "Abra o Termux e digite os seguintes comandos:",
    step3: "Navegue pelo menu interativo usando os números e siga as instruções.",
    step4: "Configure seus perfis preferidos e ative o modo automático, se desejar.",
    how_to_use_note: "Nota: Você precisa ter root e o Magisk instalado para usar o SpeedCool. O módulo deve ser instalado via Magisk Manager antes de usar esses comandos.",
    warning_title: "Aviso sobre Atualização Automática",
    warning_text: "O recurso de atualização automática está disponível, mas ainda é instável e pode falhar em alguns dispositivos. Para garantir confiabilidade, recomendamos que você baixe manualmente as novas versões pelo botão abaixo.",
    download_button: "Baixar SpeedCool",
    footer_text: "Criado com dedicação por Llucs — leve, eficiente e sempre gelado.",
    compatibility_title: "Compatibilidade",
    compatibility_text: "SpeedCool funciona em uma ampla gama de dispositivos Android, desde smartphones básicos até flagships. Compatível com Android 6 ao 16 e todos os principais chipsets.",
    performance_title: "Desempenho Otimizado",
    performance_text: "Experimente um Android mais rápido e responsivo com otimizações inteligentes que se adaptam ao seu uso diário.",
    thermal_title: "Controle Térmico",
    thermal_text: "Mantenha seu dispositivo sempre na temperatura ideal com nosso sistema avançado de gerenciamento térmico."
  },
  en: {
    title: "SpeedCool",
    subtitle: "Smart cooling. Adaptive performance. Complete thermal control for your Android.",
    about_title: "About SpeedCool",
    about_text: "SpeedCool is a Magisk module designed to improve your phone's performance without letting it overheat. With thermal intelligence, per-app modes and adaptive profiles, it delivers more fluidity, less heating and more system control.",
    features_title: "Main Features",
    feature1: "Adaptive profiles: Eco, Performance, Balanced and Learning",
    feature2: "Real-time automatic app mode",
    feature3: "Temperature control with real sensors",
    feature4: "Learning engine based on time, usage and system load",
    feature5: "Profile backup/restore and performance history",
    feature6: "Interactive shell/Termux menu with friendly design",
    feature7: "Anti-conflict system that detects and fixes issues automatically",
    how_to_use_title: "How to Use SpeedCool",
    step1: "Download and install Termux from Play Store or F-Droid.",
    step2: "Open Termux and type the following commands:",
    step3: "Navigate the interactive menu using numbers and follow instructions.",
    step4: "Configure your preferred profiles and enable auto mode if desired.",
    how_to_use_note: "Note: You need root and Magisk installed to use SpeedCool. The module must be installed via Magisk Manager before using these commands.",
    warning_title: "Auto Update Warning",
    warning_text: "The auto-update feature is available but still unstable and may fail on some devices. For reliability, we recommend manually downloading new versions using the button below.",
    download_button: "Download SpeedCool",
    footer_text: "Created with dedication by Llucs — lightweight, efficient and always cool.",
    compatibility_title: "Compatibility",
    compatibility_text: "SpeedCool works on a wide range of Android devices, from basic smartphones to flagships. Compatible with Android 6 to 16 and all major chipsets.",
    performance_title: "Optimized Performance",
    performance_text: "Experience a faster and more responsive Android with smart optimizations that adapt to your daily usage.",
    thermal_title: "Thermal Control",
    thermal_text: "Keep your device always at the ideal temperature with our advanced thermal management system."
  },
  es: {
    title: "SpeedCool",
    subtitle: "Enfriamiento inteligente. Rendimiento adaptativo. Control térmico total para tu Android.",
    about_title: "Acerca de SpeedCool",
    about_text: "SpeedCool es un módulo Magisk diseñado para mejorar el rendimiento de tu teléfono sin que se sobrecaliente. Con inteligencia térmica, modos por aplicación y perfiles adaptativos, ofrece mayor fluidez, menor calentamiento y más control sobre el sistema.",
    features_title: "Características Principales",
    feature1: "Perfiles adaptativos: Eco, Rendimiento, Equilibrado y Aprendizaje",
    feature2: "Modo automático por aplicación en tiempo real",
    feature3: "Control de temperatura con sensores reales",
    feature4: "Motor de aprendizaje basado en tiempo, uso y carga del sistema",
    feature5: "Copia de seguridad/restauración de perfiles e historial de rendimiento",
    feature6: "Menú interactivo a través de shell/Termux con diseño amigable",
    feature7: "Sistema anti-conflicto que detecta y corrige problemas automáticamente",
    how_to_use_title: "Cómo Usar SpeedCool",
    step1: "Descarga e instala Termux desde Play Store o F-Droid.",
    step2: "Abre Termux y escribe los siguientes comandos:",
    step3: "Navega por el menú interactivo usando números y sigue las instrucciones.",
    step4: "Configura tus perfiles preferidos y activa el modo automático si lo deseas.",
    how_to_use_note: "Nota: Necesitas tener root y Magisk instalado para usar SpeedCool. El módulo debe instalarse a través de Magisk Manager antes de usar estos comandos.",
    warning_title: "Advertencia sobre Actualización Automática",
    warning_text: "La función de actualización automática está disponible, pero aún es inestable y puede fallar en algunos dispositivos. Para garantizar la fiabilidad, recomendamos que descargues manualmente las nuevas versiones usando el botón de abajo.",
    download_button: "Descargar SpeedCool",
    footer_text: "Creado con dedicación por Llucs — ligero, eficiente y siempre fresco.",
    compatibility_title: "Compatibilidad",
    compatibility_text: "SpeedCool funciona en una amplia gama de dispositivos Android, desde smartphones básicos hasta flagships. Compatible con Android 6 a 16 y todos los chipsets principales.",
    performance_title: "Rendimiento Optimizado",
    performance_text: "Experimenta un Android más rápido y responsivo con optimizaciones inteligentes que se adaptan a tu uso diario.",
    thermal_title: "Control Térmico",
    thermal_text: "Mantén tu dispositivo siempre a la temperatura ideal con nuestro sistema avanzado de gestión térmica."
  },
  ru: {
    title: "SpeedCool",
    subtitle: "Умное охлаждение. Адаптивная производительность. Полный тепловой контроль для вашего Android.",
    about_title: "О SpeedCool",
    about_text: "SpeedCool — это модуль Magisk, разработанный для повышения производительности вашего телефона без перегрева. Благодаря тепловому интеллекту, режимам для каждого приложения и адаптивным профилям, он обеспечивает большую плавность, меньший нагрев и больший контроль над системой.",
    features_title: "Основные возможности",
    feature1: "Адаптивные профили: Эко, Производительность, Сбалансированный и Обучение",
    feature2: "Автоматический режим для приложений в реальном времени",
    feature3: "Контроль температуры с помощью реальных датчиков",
    feature4: "Механизм обучения, основанный на времени, использовании и загрузке системы",
    feature5: "Резервное копирование/восстановление профилей и истории производительности",
    feature6: "Интерактивное меню через shell/Termux с удобным дизайном",
    feature7: "Система антиконфликта, которая автоматически обнаруживает и устраняет проблемы",
    how_to_use_title: "Как использовать SpeedCool",
    step1: "Загрузите и установите Termux из Play Store или F-Droid.",
    step2: "Откройте Termux и введите следующие команды:",
    step3: "Перемещайтесь по интерактивному меню, используя цифры, и следуйте инструкциям.",
    step4: "Настройте свои предпочтительные профили и включите автоматический режим, если хотите.",
    how_to_use_note: "Примечание: Вам нужен root и установленный Magisk для использования SpeedCool. Модуль должен быть установлен через Magisk Manager перед использованием этих команд.",
    warning_title: "Предупреждение об автоматическом обновлении",
    warning_text: "Функция автоматического обновления доступна, но все еще нестабильна и может давать сбои на некоторых устройствах. Для обеспечения надежности мы рекомендуем загружать новые версии вручную с помощью кнопки ниже.",
    download_button: "Скачать SpeedCool",
    footer_text: "Создано с любовью Llucs — легкий, эффективный и всегда холодный.",
    compatibility_title: "Совместимость",
    compatibility_text: "SpeedCool работает на широком спектре Android-устройств, от базовых смартфонов до флагманов. Совместим с Android 6-16 и всеми основными чипсетами.",
    performance_title: "Оптимизированная производительность",
    performance_text: "Испытайте более быстрый и отзывчивый Android с умными оптимизациями, которые адаптируются к вашему ежедневному использованию.",
    thermal_title: "Тепловой контроль",
    thermal_text: "Поддерживайте ваше устройство всегда при идеальной температуре с нашей продвинутой системой управления теплом."
  }
};

// Detecta o idioma do navegador
function detectLanguage() {
  const userLang = navigator.language || navigator.userLanguage;
  const supportedLangs = ['pt', 'pt-BR', 'pt-PT'];
  
  if (supportedLangs.includes(userLang)) {
    return 'pt';
  } else if (userLang.startsWith('es')) {
    return 'es';
  } else if (userLang.startsWith('ru')) {
    return 'ru';
  } else {
    return 'en';
  }
}

// Muda o idioma do site
function changeLanguage(lang) {
  document.documentElement.lang = lang;
  localStorage.setItem('language', lang);
  
  const elements = document.querySelectorAll('[data-i18n]');
  elements.forEach(el => {
    const key = el.getAttribute('data-i18n');
    if (translations[lang] && translations[lang][key]) {
      el.textContent = translations[lang][key];
    }
  });

  // Atualiza botões de idioma
  document.querySelectorAll(".language-selector button").forEach(btn => {
    btn.classList.remove("active");
  });
  const langBtn = document.getElementById(`lang-${lang}`);
  if (langBtn) {
    langBtn.classList.add("active");
  }
}

// Alterna tema
function toggleTheme() {
  const currentTheme = document.documentElement.getAttribute('data-theme');
  const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', newTheme);
  localStorage.setItem('theme', newTheme);
}

// Alterna menu
function toggleMenu() {
  const sidebar = document.getElementById('sidebar');
  sidebar.classList.toggle('open');
}

// Fecha menu ao clicar fora
document.addEventListener('click', (e) => {
  const sidebar = document.getElementById('sidebar');
  const menuButton = document.querySelector('.menu-button');
  
  if (!sidebar.contains(e.target) && !menuButton.contains(e.target)) {
    sidebar.classList.remove('open');
  }
});

// Controle do botão "voltar ao topo"
window.addEventListener('scroll', () => {
  const backToTopButton = document.getElementById('back-to-top');
  if (window.scrollY > 300) {
    backToTopButton.classList.add('visible');
  } else {
    backToTopButton.classList.remove('visible');
  }
});

// Função para voltar ao topo
function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Efeito de paralaxe no header
window.addEventListener('scroll', () => {
  const scrolled = window.pageYOffset;
  const header = document.querySelector('header');
  if (header) {
    header.style.transform = `translateY(${scrolled * 0.5}px)`;
  }
});

// Animação de entrada para elementos
function animateOnScroll() {
  const elements = document.querySelectorAll('section, .step');
  
  elements.forEach(element => {
    const elementTop = element.getBoundingClientRect().top;
    const elementVisible = 150;
    
    if (elementTop < window.innerHeight - elementVisible) {
      element.classList.add('animate');
    }
  });
}

window.addEventListener('scroll', animateOnScroll);

// Efeito de digitação no título
function typeWriter(element, text, speed = 100) {
  let i = 0;
  element.innerHTML = '';
  
  function type() {
    if (i < text.length) {
      element.innerHTML += text.charAt(i);
      i++;
      setTimeout(type, speed);
    }
  }
  
  type();
}

// Inicialização quando o DOM estiver carregado
document.addEventListener('DOMContentLoaded', () => {
  // Configuração inicial do tema
  const savedTheme = localStorage.getItem('theme') || 'dark';
  document.documentElement.setAttribute('data-theme', savedTheme);

  // Configuração inicial do idioma
  const savedLang = localStorage.getItem('language') || detectLanguage();
  changeLanguage(savedLang);

  // Adiciona event listeners
  const backToTopBtn = document.getElementById('back-to-top');
  if (backToTopBtn) {
    backToTopBtn.addEventListener('click', scrollToTop);
  }

  // Efeito de digitação no título (opcional)
  const title = document.querySelector('header h1');
  if (title) {
    const originalText = title.textContent;
    title.classList.add('glow');
    // typeWriter(title, originalText, 150);
  }

  // Animação inicial
  animateOnScroll();

  // Smooth scroll para links internos
  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
      e.preventDefault();
      const target = document.querySelector(this.getAttribute('href'));
      if (target) {
        target.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
      }
    });
  });

  // Adiciona efeitos de hover dinâmicos
  const sections = document.querySelectorAll('section');
  sections.forEach(section => {
    section.addEventListener('mouseenter', () => {
      section.style.transform = 'translateY(-5px) scale(1.02)';
    });
    
    section.addEventListener('mouseleave', () => {
      section.style.transform = 'translateY(0) scale(1)';
    });
  });
});

// Função para copiar comando do terminal
function copyCommand(command) {
  navigator.clipboard.writeText(command).then(() => {
    // Feedback visual
    const notification = document.createElement('div');
    notification.textContent = 'Comando copiado!';
    notification.style.cssText = `
      position: fixed;
      top: 20px;
      right: 20px;
      background: var(--azul-claro);
      color: white;
      padding: 10px 20px;
      border-radius: 5px;
      z-index: 10000;
      animation: slideIn 0.3s ease;
    `;
    document.body.appendChild(notification);
    
    setTimeout(() => {
      notification.remove();
    }, 2000);
  });
}

// Adiciona funcionalidade de cópia aos códigos de terminal
document.addEventListener('DOMContentLoaded', () => {
  const terminalCodes = document.querySelectorAll('.terminal-code');
  terminalCodes.forEach(code => {
    code.style.cursor = 'pointer';
    code.title = 'Clique para copiar';
    
    code.addEventListener('click', () => {
      const command = code.textContent.trim();
      copyCommand(command);
    });
  });
});

// Preloader (opcional)
window.addEventListener('load', () => {
  const preloader = document.getElementById('preloader');
  if (preloader) {
    preloader.style.opacity = '0';
    setTimeout(() => {
      preloader.style.display = 'none';
    }, 500);
  }
});

// Service Worker para cache (PWA básico)
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .then(registration => {
        console.log('SW registered: ', registration);
      })
      .catch(registrationError => {
        console.log('SW registration failed: ', registrationError);
      });
  });
}

