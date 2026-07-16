(function () {

    'use strict';

    const MOBILE_MAX_WIDTH = 768;
    const BUTTON_ID = 'mobileLogoutButton';
    const FORM_ID = 'mobileLogoutForm';

    function esPantallaMovil() {

        return window.innerWidth <= MOBILE_MAX_WIDTH;
    }

    function obtenerTokenCsrf() {

        const metaToken =
            document.querySelector(
                'meta[name="_csrf"]'
            );

        return metaToken
            ? metaToken.getAttribute('content')
            : null;
    }

    function obtenerNombreCsrf() {

        const metaParameter =
            document.querySelector(
                'meta[name="_csrf_parameter"]'
            );

        return metaParameter
            ? metaParameter.getAttribute('content')
            : '_csrf';
    }

    function crearEstilos() {

        if (
            document.getElementById(
                'mobileLogoutStyles'
            )
        ) {
            return;
        }

        const style =
            document.createElement('style');

        style.id =
            'mobileLogoutStyles';

        style.textContent = `
            #${FORM_ID} {
                display: none;
            }

            #${BUTTON_ID} {
                display: none;
            }

            @media (max-width: ${MOBILE_MAX_WIDTH}px) {

                #${BUTTON_ID} {
                    position: fixed;
                    right: 16px;
                    bottom: 18px;
                    z-index: 99999;

                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    gap: 8px;

                    min-width: 145px;
                    min-height: 48px;
                    padding: 12px 18px;

                    border: 1px solid rgba(
                        248,
                        113,
                        113,
                        0.85
                    );
                    border-radius: 999px;

                    background: #7f1d1d;
                    color: #ffffff;

                    font-family:
                        "Segoe UI",
                        Arial,
                        sans-serif;
                    font-size: 15px;
                    font-weight: 700;

                    cursor: pointer;

                    box-shadow:
                        0 10px 25px
                        rgba(0, 0, 0, 0.40);

                    transition:
                        transform 0.18s ease,
                        background 0.18s ease;
                }

                #${BUTTON_ID}:active {
                    transform: scale(0.96);
                }

                #${BUTTON_ID}:hover {
                    background: #991b1b;
                }

                body {
                    padding-bottom: 86px;
                }
            }
        `;

        document.head.appendChild(
            style
        );
    }

    function confirmarCierreSesion() {

        return window.confirm(
            '¿Deseas cerrar tu sesión en Abarrote Cloud?'
        );
    }

    function crearFormularioLogout() {

        if (
            document.getElementById(
                FORM_ID
            )
        ) {
            return document.getElementById(
                FORM_ID
            );
        }

        const form =
            document.createElement('form');

        form.id =
            FORM_ID;

        form.method =
            'post';

        form.action =
            '/logout';

        const csrfToken =
            obtenerTokenCsrf();

        if (csrfToken) {

            const inputCsrf =
                document.createElement(
                    'input'
                );

            inputCsrf.type =
                'hidden';

            inputCsrf.name =
                obtenerNombreCsrf();

            inputCsrf.value =
                csrfToken;

            form.appendChild(
                inputCsrf
            );
        }

        document.body.appendChild(
            form
        );

        return form;
    }

    function crearBotonLogout() {

        if (
            document.getElementById(
                BUTTON_ID
            )
        ) {
            return;
        }

        const button =
            document.createElement(
                'button'
            );

        button.id =
            BUTTON_ID;

        button.type =
            'button';

        button.setAttribute(
            'aria-label',
            'Cerrar sesión'
        );

        button.innerHTML =
            '<span aria-hidden="true">🚪</span>'
            + '<span>Cerrar sesión</span>';

        button.addEventListener(
            'click',
            function () {

                if (
                    !confirmarCierreSesion()
                ) {
                    return;
                }

                const form =
                    crearFormularioLogout();

                button.disabled =
                    true;

                button.innerHTML =
                    '<span>⏳</span>'
                    + '<span>Cerrando...</span>';

                form.submit();
            }
        );

        document.body.appendChild(
            button
        );
    }

    function actualizarVisibilidad() {

        const button =
            document.getElementById(
                BUTTON_ID
            );

        if (!button) {
            return;
        }

        button.style.display =
            esPantallaMovil()
                ? 'inline-flex'
                : 'none';
    }

    function inicializar() {

        crearEstilos();
        crearFormularioLogout();
        crearBotonLogout();
        actualizarVisibilidad();
    }

    if (
        document.readyState
        === 'loading'
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

})();
