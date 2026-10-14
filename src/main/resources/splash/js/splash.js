// avisa o JavaFX que a animação terminou (a ponte "app" é criada pela SplashView)
function finalizar() {
    if (window.app && window.app.finalizar) {
        window.app.finalizar();
    }
}

// separa o nome em letras para animar uma a uma
function dividirNomeEmLetras() {
    var nome = document.getElementById("nome");
    var texto = nome.textContent;
    nome.innerHTML = "";
    for (var i = 0; i < texto.length; i++) {
        var letra = document.createElement("span");
        letra.className = "letra";
        letra.textContent = texto.charAt(i) === " " ? "\u00A0" : texto.charAt(i);
        nome.appendChild(letra);
    }
}

function iniciarAnimacao() {
    dividirNomeEmLetras();

    // brilho que pulsa atrás do logo, durante toda a animação
    gsap.to("#brilho", { duration: 1.4, scale: 1.3, opacity: 0.5, repeat: -1, yoyo: true, ease: "sine.inOut" });

    // sequência principal (a duração de cada passo é definida aqui)
    var tl = gsap.timeline({ onComplete: finalizar });
    tl.from("#logo", { duration: 0.9, scale: 0, rotation: -180, opacity: 0, ease: "back.out(1.7)" })
        .from(".letra", { duration: 0.5, y: 40, opacity: 0, stagger: 0.08, ease: "power3.out" }, "-=0.3")
        .from("#slogan", { duration: 0.6, y: 14, opacity: 0, ease: "power2.out" }, "-=0.1")
        .from("#barra", { duration: 0.3, opacity: 0 }, "-=0.2")
        .to(".ponto", { duration: 0.35, y: -6, stagger: 0.12, repeat: 3, yoyo: true, ease: "sine.inOut" }, "<")
        .to("#preenchimento", { duration: 1.6, width: "100%", ease: "power1.inOut" }, "<")
        .to("#palco", { duration: 0.6, opacity: 0, scale: 1.05, ease: "power2.in" }, "+=0.2");
}

if (typeof gsap === "undefined") {
    // GSAP não carregou: não prende o usuário na tela de abertura
    finalizar();
} else {
    iniciarAnimacao();
}