'use strict';

(function () {

    const mensajes =
        document.getElementById(
            'chatMensajes'
        );

    const pregunta =
        document.getElementById(
            'preguntaAsistente'
        );

    const enviar =
        document.getElementById(
            'enviarPregunta'
        );

    const limpiar =
        document.getElementById(
            'limpiarChat'
        );

    const estado =
        document.getElementById(
            'estadoAsistente'
        );

    function agregarMensaje(
        texto,
        tipo
    ) {

        const elemento =
            document.createElement(
                'div'
            );

        elemento.className =
            `chat-mensaje ${tipo}`;

        elemento.textContent =
            texto;

        mensajes.appendChild(
            elemento
        );

        mensajes.scrollTop =
            mensajes.scrollHeight;
    }

    async function enviarPregunta() {

        const texto =
            pregunta.value.trim();

        if (!texto) {

            pregunta.focus();
            return;
        }

        agregarMensaje(
            texto,
            'usuario'
        );

        pregunta.value = '';

        enviar.disabled = true;

        estado.textContent =
            'Consultando inventario...';

        try {

            const response =
                await fetch(
                    '/admin/asistente/preguntar',
                    {
                        method: 'POST',
                        headers: {
                            'Content-Type':
                                'application/json'
                        },
                        body: JSON.stringify({
                            pregunta: texto
                        })
                    }
                );

            if (!response.ok) {

                throw new Error(
                    `HTTP ${response.status}`
                );
            }

            const datos =
                await response.json();

            agregarMensaje(
                datos.respuesta
                || 'No se recibió respuesta.',
                'asistente'
            );

            estado.textContent =
                'Respuesta calculada con datos actuales.';

        } catch (error) {

            console.error(
                error
            );

            agregarMensaje(
                'No fue posible consultar el asistente. '
                + 'Revisa el log de la aplicación.',
                'asistente'
            );

            estado.textContent =
                'Error al consultar.';

        } finally {

            enviar.disabled = false;
            pregunta.focus();
        }
    }

    enviar.addEventListener(
        'click',
        enviarPregunta
    );

    pregunta.addEventListener(
        'keydown',
        function (evento) {

            if (
                evento.key === 'Enter'
                && !evento.shiftKey
            ) {

                evento.preventDefault();
                enviarPregunta();
            }
        }
    );

    document
        .querySelectorAll(
            '.pregunta-sugerida'
        )
        .forEach(
            function (boton) {

                boton.addEventListener(
                    'click',
                    function () {

                        pregunta.value =
                            boton.dataset.pregunta
                            || '';

                        enviarPregunta();
                    }
                );
            }
        );

    limpiar.addEventListener(
        'click',
        function () {

            mensajes.innerHTML = '';

            agregarMensaje(
                'Conversación limpia. '
                + '¿Qué deseas consultar?',
                'asistente'
            );

            estado.textContent = '';
            pregunta.focus();
        }
    );

})();
