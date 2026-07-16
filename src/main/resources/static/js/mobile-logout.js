(function () {

    'use strict';

    const MOBILE_MAX_WIDTH = 900;

    const BUTTON_ID =
        'mobileLogoutButton';

    const FORM_ID =
        'mobileLogoutForm';

    const STYLE_ID =
        'mobileLogoutStyles';

    function esDispositivoMovil() {

        return window.matchMedia(
            `(max-width: ${MOBILE_MAX_WIDTH}px)`
        ).matches;
    }

    function obtenerTokenCsrf() {

        const meta =
            document.querySelector(
                'meta[name="_csrf"]'
            );

        return meta
            ? meta.getAttribute('content')
            : null;
    }

    function obtenerParametroCsrf() {

        const meta =
            document.querySelector(
                'meta[name="_csrf_parameter"]'
            );

        return meta
            ? meta.getAttribute('content')
            : '_csrf';
    }

    function crearEstilos() {

        if (
            document.getElementById(
                STYLE_ID
            )
        ) {
            return;
        }

        const estilos =
            document.createElement('style');

        estilos.id =
            STYLE_ID;

        estilos.textContent = `

            #${FORM_ID} {
                display: none !important;
            }

            #${BUTTON_ID} {
                display: none !important;
            }

            @media screen and (max-width: ${MOBILE_MAX_WIDTH}px) {

                #${BUTTON_ID} {
                    position: fixed !important;

                    right: 16px !important;
                    bottom: calc(
                        18px
                        + env(
                            safe-area-inset-bottom,
                            0px
                        )
                    ) !important;

                    z-index: 2147483647 !important;

                    display: inline-flex !important;
                    align-items: center !important;
                    justify-content: center !important;
                    gap: 8px !important;

                    min-width: 150px !important;
                    min-height: 50px !important;

                    padding: 12px 18px !important;

                    border: 1px solid #f87171 !important;
                    border-radius: 999px !important;

                    background: #991b1b !important;
                    color: #ffffff !important;

                    font-family:
                        "Segoe UI",
                        Arial,
                        sans-serif !important;

                    font-size: 15px !important;
                    font-weight: 700 !important;

                    line-height: 1 !important;

                    cursor: pointer !important;

                    opacity: 1 !important;
                    visibility: visible !important;

                    box-shadow:
                        0 10px 28px
                        rgba(0, 0, 0, 0.55) !important;

                    -webkit-tap-highlight-color:
                        transparent !important;
                }

                #${BUTTON_ID}:active {
                    transform: scale(0.96);
                }

                body {
                    padding-bottom: 90px !important;
                }
            }
        `;

        document.head.appendChild(
            estilos
        );
    }

    function crearFormulario() {

        let formulario =
            document.getElementById(
                FORM_ID
            );

        if (formulario) {
            return formulario;
        }

        formulario =
            document.createElement('form');

        formulario.id =
            FORM_ID;

        formulario.method =
            'post';

        formulario.action =
            '/logout';

        formulario.style.display =
            'none';

        const csrfToken =
            obtenerTokenCsrf();

        if (csrfToken) {

            const csrfInput =
                document.createElement(
                    'input'
                );

            csrfInput.type =
                'hidden';

            csrfInput.name =
                obtenerParametroCsrf();

            csrfInput.value =
                csrfToken;

            formulario.appendChild(
                csrfInput
            );
        }

        document.body.appendChild(
            formulario
        );

        return formulario;
    }

    function crearBoton() {

        let boton =
            document.getElementById(
                BUTTON_ID
            );

        if (boton) {
            return boton;
        }

        boton =
            document.createElement(
                'button'
            );

        boton.id =
            BUTTON_ID;

        boton.type =
            'button';

        boton.setAttribute(
            'aria-label',
            'Cerrar sesión'
        );

        boton.innerHTML =
            '<span aria-hidden="true">🚪</span>'
            + '<span>Cerrar sesión</span>';

        boton.addEventListener(
            'click',
            function () {

                const confirmar =
                    window.confirm(
                        '¿Deseas cerrar tu sesión?'
                    );

                if (!confirmar) {
                    return;
                }

                boton.disabled =
                    true;

                boton.innerHTML =
                    '<span aria-hidden="true">⏳</span>'
                    + '<span>Cerrando...</span>';

                crearFormulario().submit();
            }
        );

        document.body.appendChild(
            boton
        );

        return boton;
    }

    function actualizarVisibilidad() {

        const boton =
            document.getElementById(
                BUTTON_ID
            );

        if (!boton) {
            return;
        }

        if (esDispositivoMovil()) {

            boton.style.setProperty(
                'display',
                'inline-flex',
                'important'
            );

            boton.style.setProperty(
                'visibility',
                'visible',
                'important'
            );

            boton.style.setProperty(
                'opacity',
                '1',
                'important'
            );

        } else {

            boton.style.setProperty(
                'display',
                'none',
                'important'
            );
        }
    }

    function inicializar() {

        if (!document.body) {
            return;
        }

        crearEstilos();
        crearFormulario();
        crearBoton();
        actualizarVisibilidad();
    }

    if (
        document.readyState === 'loading'
    ) {

        document.addEventListener(
            'DOMContentLoaded',
            inicializar
        );

    } else {

        inicializar();
    }

    window.addEventListener(
        'resize',
        actualizarVisibilidad
    );

    window.addEventListener(
        'orientationchange',
        function () {

            window.setTimeout(
                actualizarVisibilidad,
                250
            );
        }
    );

})();
