(function () {

    'use strict';

    const BUTTON_ID = 'abarroteMobileLogoutButton';
    const FORM_ID = 'abarroteMobileLogoutForm';
    const STYLE_ID = 'abarroteMobileLogoutStyle';

    function esDispositivoMovil() {

        const anchoMovil =
            window.innerWidth <= 1200;

        const punteroTactil =
            window.matchMedia(
                '(pointer: coarse)'
            ).matches;

        const agenteMovil =
            /Android|iPhone|iPad|iPod|Mobile/i
                .test(navigator.userAgent);

        return anchoMovil
                || punteroTactil
                || agenteMovil;
    }

    function crearEstilos() {

        if (document.getElementById(STYLE_ID)) {
            return;
        }

        const style =
            document.createElement('style');

        style.id = STYLE_ID;

        style.textContent = `

            #${FORM_ID} {
                display: none !important;
            }

            #${BUTTON_ID} {
                display: none;
            }

            body.abarrote-mobile-device
            #${BUTTON_ID} {

                position: fixed !important;

                right: 14px !important;

                bottom: calc(
                    16px
                    + env(
                        safe-area-inset-bottom,
                        0px
                    )
                ) !important;

                z-index: 2147483647 !important;

                display: flex !important;
                align-items: center !important;
                justify-content: center !important;
                gap: 8px !important;

                min-width: 158px !important;
                min-height: 52px !important;

                padding: 12px 18px !important;

                margin: 0 !important;

                border: 2px solid #f87171 !important;
                border-radius: 999px !important;

                background: #991b1b !important;
                color: #ffffff !important;

                font-family:
                    Arial,
                    "Segoe UI",
                    sans-serif !important;

                font-size: 15px !important;
                font-weight: 700 !important;
                line-height: 1 !important;

                opacity: 1 !important;
                visibility: visible !important;

                cursor: pointer !important;

                box-shadow:
                    0 10px 30px
                    rgba(0, 0, 0, 0.60) !important;

                transform: none !important;

                -webkit-appearance: none !important;
                appearance: none !important;

                -webkit-tap-highlight-color:
                    transparent !important;
            }

            body.abarrote-mobile-device
            #${BUTTON_ID}:active {

                transform:
                    scale(0.96) !important;
            }

            body.abarrote-mobile-device {

                padding-bottom:
                    90px !important;
            }
        `;

        document.head.appendChild(style);
    }

    function obtenerMeta(nombre) {

        const elemento =
            document.querySelector(
                `meta[name="${nombre}"]`
            );

        return elemento
            ? elemento.getAttribute('content')
            : null;
    }

    function crearFormularioLogout() {

        let form =
            document.getElementById(FORM_ID);

        if (form) {
            return form;
        }

        form =
            document.createElement('form');

        form.id = FORM_ID;
        form.method = 'post';
        form.action = '/logout';

        const token =
            obtenerMeta('_csrf');

        const parametro =
            obtenerMeta('_csrf_parameter')
            || '_csrf';

        if (token) {

            const input =
                document.createElement('input');

            input.type = 'hidden';
            input.name = parametro;
            input.value = token;

            form.appendChild(input);
        }

        document.body.appendChild(form);

        return form;
    }

    function cerrarSesion() {

        const confirmado =
            window.confirm(
                '¿Deseas cerrar tu sesión?'
            );

        if (!confirmado) {
            return;
        }

        const boton =
            document.getElementById(BUTTON_ID);

        if (boton) {

            boton.disabled = true;

            boton.innerHTML =
                '<span>⏳</span>'
                + '<span>Cerrando...</span>';
        }

        crearFormularioLogout().submit();
    }

    function crearBoton() {

        let boton =
            document.getElementById(BUTTON_ID);

        if (boton) {
            return boton;
        }

        boton =
            document.createElement('button');

        boton.id = BUTTON_ID;
        boton.type = 'button';

        boton.setAttribute(
            'aria-label',
            'Cerrar sesión'
        );

        boton.innerHTML =
            '<span aria-hidden="true">🚪</span>'
            + '<span>Cerrar sesión</span>';

        boton.addEventListener(
            'click',
            cerrarSesion
        );

        document.body.appendChild(boton);

        return boton;
    }

    function aplicarModoMovil() {

        if (esDispositivoMovil()) {

            document.body.classList.add(
                'abarrote-mobile-device'
            );

        } else {

            document.body.classList.remove(
                'abarrote-mobile-device'
            );
        }
    }

    function inicializar() {

        if (!document.body) {
            return;
        }

        crearEstilos();
        crearFormularioLogout();
        crearBoton();
        aplicarModoMovil();

        console.log(
            '[Abarrote Cloud] Botón móvil de cierre cargado.',
            {
                ancho: window.innerWidth,
                usuarioAgente: navigator.userAgent,
                movil: esDispositivoMovil()
            }
        );
    }

    if (document.readyState === 'loading') {

        document.addEventListener(
            'DOMContentLoaded',
            inicializar
        );

    } else {

        inicializar();
    }

    window.addEventListener(
        'resize',
        aplicarModoMovil
    );

    window.addEventListener(
        'orientationchange',
        function () {

            window.setTimeout(
                aplicarModoMovil,
                300
            );
        }
    );

})();
