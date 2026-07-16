'use strict';

(function () {

    const SVG_NS =
        'http://www.w3.org/2000/svg';

    function crearSvg(
        nombre,
        atributos = {}
    ) {

        const elemento =
            document.createElementNS(
                SVG_NS,
                nombre
            );

        Object.entries(atributos)
            .forEach(
                function (entrada) {

                    elemento.setAttribute(
                        entrada[0],
                        String(entrada[1])
                    );
                }
            );

        return elemento;
    }

    function agregarTexto(
        svg,
        texto,
        atributos
    ) {

        const elemento =
            crearSvg(
                'text',
                atributos
            );

        elemento.textContent =
            texto;

        svg.appendChild(
            elemento
        );

        return elemento;
    }

    function obtenerTendencia() {

        const elemento =
            document.getElementById(
                'inventarioTrendData'
            );

        if (!elemento) {

            return {
                etiquetas: [],
                series: []
            };
        }

        try {

            const datos =
                JSON.parse(
                    elemento.textContent
                );

            return {
                etiquetas:
                    Array.isArray(
                        datos.etiquetas
                    )
                        ? datos.etiquetas
                        : [],

                series:
                    Array.isArray(
                        datos.series
                    )
                        ? datos.series
                        : []
            };

        } catch (error) {

            console.error(
                'No fue posible leer la tendencia de inventario',
                error
            );

            return {
                etiquetas: [],
                series: []
            };
        }
    }

    /*
     * Genera un color automático y estable.
     *
     * No cambia en cada recarga porque se calcula
     * usando el identificador, código y nombre.
     */
    function obtenerColorSerie(
        serie,
        indice
    ) {

        const semilla =
            String(
                serie.sucursalId
                ?? serie.codigo
                ?? serie.nombre
                ?? indice
            );

        let hash = 0;

        for (
            let posicion = 0;
            posicion < semilla.length;
            posicion++
        ) {

            hash =
                semilla.charCodeAt(
                    posicion
                )
                + (
                    (hash << 5)
                    - hash
                );

            hash |= 0;
        }

        const tono =
            Math.abs(
                hash + indice * 137
            ) % 360;

        return `hsl(${tono} 88% 58%)`;
    }

    function obtenerMaximo(
        tendencia
    ) {

        const valores =
            tendencia.series
                .flatMap(
                    function (serie) {

                        return Array.isArray(
                            serie.valores
                        )
                            ? serie.valores
                            : [];
                    }
                )
                .map(Number)
                .filter(Number.isFinite);

        const maximo =
            Math.max(
                ...valores,
                1
            );

        const magnitud =
            Math.pow(
                10,
                Math.floor(
                    Math.log10(maximo)
                )
            );

        return Math.max(
            10,
            Math.ceil(
                maximo / magnitud
            ) * magnitud
        );
    }

    function crearLeyenda(
        tendencia,
        colores
    ) {

        const leyenda =
            document.getElementById(
                'graficaLeyenda'
            );

        if (!leyenda) {
            return;
        }

        leyenda.replaceChildren();

        tendencia.series.forEach(
            function (serie, indice) {

                const item =
                    document.createElement(
                        'div'
                    );

                item.style.display =
                    'inline-flex';

                item.style.alignItems =
                    'center';

                item.style.gap =
                    '0.45rem';

                item.style.marginRight =
                    '1.1rem';

                item.style.marginBottom =
                    '0.45rem';

                item.style.color =
                    '#cbd5e1';

                item.style.fontSize =
                    '0.82rem';

                const linea =
                    document.createElement(
                        'span'
                    );

                linea.style.display =
                    'inline-block';

                linea.style.width =
                    '28px';

                linea.style.height =
                    '3px';

                linea.style.borderRadius =
                    '999px';

                linea.style.background =
                    colores[indice];

                const punto =
                    document.createElement(
                        'span'
                    );

                punto.style.display =
                    'inline-block';

                punto.style.width =
                    '9px';

                punto.style.height =
                    '9px';

                punto.style.marginLeft =
                    '-23px';

                punto.style.marginRight =
                    '12px';

                punto.style.borderRadius =
                    '50%';

                punto.style.background =
                    colores[indice];

                const texto =
                    document.createElement(
                        'span'
                    );

                texto.textContent =
                    `${serie.nombre} (${serie.codigo})`;

                item.append(
                    linea,
                    punto,
                    texto
                );

                leyenda.appendChild(
                    item
                );
            }
        );
    }

    function dibujarGrafica() {

        const svg =
            document.getElementById(
                'graficaInventario'
            );

        if (!svg) {
            return;
        }

        const tendencia =
            obtenerTendencia();

        svg.replaceChildren();

        if (
            tendencia.etiquetas.length === 0
            || tendencia.series.length === 0
        ) {

            svg.setAttribute(
                'viewBox',
                '0 0 800 340'
            );

            agregarTexto(
                svg,
                'No existen datos históricos para mostrar',
                {
                    x: 400,
                    y: 170,
                    class: 'grafica-texto',
                    'text-anchor': 'middle'
                }
            );

            return;
        }

        const ancho =
            Math.max(
                Math.round(
                    svg.getBoundingClientRect()
                        .width
                ),
                620
            );

        const alto =
            Math.max(
                Math.round(
                    svg.getBoundingClientRect()
                        .height
                ),
                340
            );

        const margen = {
            superior: 32,
            derecho: 30,
            inferior: 64,
            izquierdo: 64
        };

        const anchoUtil =
            ancho
            - margen.izquierdo
            - margen.derecho;

        const altoUtil =
            alto
            - margen.superior
            - margen.inferior;

        const escalaMaxima =
            obtenerMaximo(
                tendencia
            );

        const colores =
            tendencia.series.map(
                obtenerColorSerie
            );

        crearLeyenda(
            tendencia,
            colores
        );

        svg.setAttribute(
            'viewBox',
            `0 0 ${ancho} ${alto}`
        );

        svg.setAttribute(
            'preserveAspectRatio',
            'xMidYMid meet'
        );

        const fondo =
            crearSvg(
                'rect',
                {
                    x: margen.izquierdo,
                    y: margen.superior,
                    width: anchoUtil,
                    height: altoUtil,
                    fill: 'rgba(15, 23, 42, 0.22)'
                }
            );

        svg.appendChild(
            fondo
        );

        const divisionesY = 5;

        for (
            let indice = 0;
            indice <= divisionesY;
            indice++
        ) {

            const proporcion =
                indice / divisionesY;

            const y =
                margen.superior
                + altoUtil
                - proporcion * altoUtil;

            const valor =
                Math.round(
                    escalaMaxima * proporcion
                );

            svg.appendChild(
                crearSvg(
                    'line',
                    {
                        x1: margen.izquierdo,
                        y1: y,
                        x2: ancho
                            - margen.derecho,
                        y2: y,
                        class: 'grafica-rejilla'
                    }
                )
            );

            agregarTexto(
                svg,
                valor.toLocaleString(
                    'es-MX'
                ),
                {
                    x: margen.izquierdo - 12,
                    y: y + 4,
                    class: 'grafica-texto',
                    'text-anchor': 'end'
                }
            );
        }

        const numeroPuntos =
            tendencia.etiquetas.length;

        const separacionX =
            numeroPuntos <= 1
                ? anchoUtil
                : anchoUtil
                  / (numeroPuntos - 1);

        tendencia.etiquetas.forEach(
            function (etiqueta, indice) {

                const x =
                    numeroPuntos === 1
                        ? margen.izquierdo
                          + anchoUtil / 2
                        : margen.izquierdo
                          + indice * separacionX;

                svg.appendChild(
                    crearSvg(
                        'line',
                        {
                            x1: x,
                            y1: margen.superior,
                            x2: x,
                            y2: margen.superior
                                + altoUtil,
                            stroke: 'rgba(51,65,85,0.35)',
                            'stroke-width': 1
                        }
                    )
                );

                agregarTexto(
                    svg,
                    etiqueta,
                    {
                        x: x,
                        y: margen.superior
                            + altoUtil
                            + 28,
                        class: 'grafica-texto',
                        'text-anchor': 'middle'
                    }
                );
            }
        );

        svg.appendChild(
            crearSvg(
                'line',
                {
                    x1: margen.izquierdo,
                    y1: margen.superior,
                    x2: margen.izquierdo,
                    y2: margen.superior
                        + altoUtil,
                    class: 'grafica-eje'
                }
            )
        );

        svg.appendChild(
            crearSvg(
                'line',
                {
                    x1: margen.izquierdo,
                    y1: margen.superior
                        + altoUtil,
                    x2: ancho
                        - margen.derecho,
                    y2: margen.superior
                        + altoUtil,
                    class: 'grafica-eje'
                }
            )
        );

        tendencia.series.forEach(
            function (serie, serieIndice) {

                const valores =
                    Array.isArray(
                        serie.valores
                    )
                        ? serie.valores
                        : [];

                const puntos =
                    tendencia.etiquetas.map(
                        function (_, indice) {

                            const valor =
                                Number(
                                    valores[indice]
                                    ?? 0
                                );

                            const x =
                                numeroPuntos === 1
                                    ? margen.izquierdo
                                      + anchoUtil / 2
                                    : margen.izquierdo
                                      + indice
                                      * separacionX;

                            const y =
                                margen.superior
                                + altoUtil
                                - (
                                    valor
                                    / escalaMaxima
                                ) * altoUtil;

                            return {
                                x,
                                y,
                                valor,
                                indice
                            };
                        }
                    );

                const puntosCadena =
                    puntos
                        .map(
                            punto =>
                                `${punto.x},${punto.y}`
                        )
                        .join(' ');

                const color =
                    colores[serieIndice];

                const area =
                    crearSvg(
                        'polygon',
                        {
                            points:
                                `${puntos[0].x},`
                                + `${margen.superior + altoUtil} `
                                + puntosCadena
                                + ` ${puntos[puntos.length - 1].x},`
                                + `${margen.superior + altoUtil}`,
                            fill: color,
                            opacity: 0.045
                        }
                    );

                svg.appendChild(
                    area
                );

                const linea =
                    crearSvg(
                        'polyline',
                        {
                            points: puntosCadena,
                            fill: 'none',
                            stroke: color,
                            'stroke-width': 3,
                            'stroke-linecap': 'round',
                            'stroke-linejoin': 'round'
                        }
                    );

                svg.appendChild(
                    linea
                );

                puntos.forEach(
                    function (punto) {

                        const circulo =
                            crearSvg(
                                'circle',
                                {
                                    cx: punto.x,
                                    cy: punto.y,
                                    r: 5.5,
                                    fill: color,
                                    stroke: '#16213e',
                                    'stroke-width': 3
                                }
                            );

                        const titulo =
                            crearSvg(
                                'title'
                            );

                        titulo.textContent =
                            `${serie.nombre} · `
                            + `${tendencia.etiquetas[punto.indice]}: `
                            + `${punto.valor} unidades`;

                        circulo.appendChild(
                            titulo
                        );

                        svg.appendChild(
                            circulo
                        );
                    }
                );

                const ultimoPunto =
                    puntos[puntos.length - 1];

                agregarTexto(
                    svg,
                    String(
                        ultimoPunto.valor
                    ),
                    {
                        x: ultimoPunto.x - 4,
                        y: Math.max(
                            ultimoPunto.y - 12,
                            16
                        ),
                        fill: color,
                        'font-size': 12,
                        'font-weight': 700,
                        'text-anchor': 'end'
                    }
                );
            }
        );
    }

    function normalizarTexto(
        valor
    ) {

        return String(
            valor ?? ''
        )
        .toLowerCase()
        .normalize('NFD')
        .replace(
            /[\u0300-\u036f]/g,
            ''
        )
        .trim();
    }

    function filtrarInventario() {

        const selector =
            document.getElementById(
                'filtroSucursal'
            );

        const buscador =
            document.getElementById(
                'busquedaInventario'
            );

        if (!selector || !buscador) {
            return;
        }

        const sucursal =
            String(
                selector.value ?? ''
            );

        const texto =
            normalizarTexto(
                buscador.value
            );

        const filas =
            Array.from(
                document.querySelectorAll(
                    '.fila-inventario'
                )
            );

        let visibles = 0;

        filas.forEach(
            function (fila) {

                const coincideSucursal =
                    sucursal === ''
                    || String(
                        fila.dataset.sucursal
                        ?? ''
                    ) === sucursal;

                const producto =
                    normalizarTexto(
                        fila.dataset.producto
                    );

                const codigo =
                    normalizarTexto(
                        fila.dataset.codigo
                    );

                const coincideTexto =
                    texto === ''
                    || producto.includes(
                        texto
                    )
                    || codigo.includes(
                        texto
                    );

                const mostrar =
                    coincideSucursal
                    && coincideTexto;

                fila.hidden =
                    !mostrar;

                if (mostrar) {
                    visibles++;
                }
            }
        );

        const sinResultados =
            document.getElementById(
                'filaSinResultados'
            );

        if (sinResultados) {

            sinResultados.hidden =
                visibles !== 0
                || filas.length === 0;
        }

        const contador =
            document.getElementById(
                'contadorResultados'
            );

        if (contador) {

            contador.textContent =
                `Mostrando ${visibles} de `
                + `${filas.length} registros`;
        }
    }

    function configurarFiltros() {

        const selector =
            document.getElementById(
                'filtroSucursal'
            );

        const buscador =
            document.getElementById(
                'busquedaInventario'
            );

        const boton =
            document.getElementById(
                'limpiarFiltros'
            );

        if (selector) {

            selector.value = '';

            selector.addEventListener(
                'change',
                filtrarInventario
            );
        }

        if (buscador) {

            buscador.value = '';

            buscador.addEventListener(
                'input',
                filtrarInventario
            );
        }

        if (boton) {

            boton.addEventListener(
                'click',
                function () {

                    if (selector) {
                        selector.value = '';
                    }

                    if (buscador) {
                        buscador.value = '';
                    }

                    filtrarInventario();
                }
            );
        }

        filtrarInventario();
    }

    let temporizador;

    function iniciar() {

        configurarFiltros();
        dibujarGrafica();

        window.addEventListener(
            'resize',
            function () {

                clearTimeout(
                    temporizador
                );

                temporizador =
                    setTimeout(
                        dibujarGrafica,
                        180
                    );
            }
        );
    }

    if (
        document.readyState === 'loading'
    ) {

        document.addEventListener(
            'DOMContentLoaded',
            iniciar
        );

    } else {

        iniciar();
    }

})();
