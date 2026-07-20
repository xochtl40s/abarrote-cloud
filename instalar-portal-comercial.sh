#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_DIR="$PROJECT/.backups/portal-comercial-$TIMESTAMP"

JAVA_BASE="$PROJECT/src/main/java/com/abarrote/abarroteapi"
RESOURCES="$PROJECT/src/main/resources"

CONTROLLER_DIR="$JAVA_BASE/controller"
TEMPLATE_DIR="$RESOURCES/templates"
STATIC_DIR="$RESOURCES/static/commerce"
SECURITY_FILE="$JAVA_BASE/config/SecurityConfig.java"
PROPERTIES_FILE="$RESOURCES/application.properties"

echo "============================================================"
echo " COMMERCE CLOUD"
echo " Instalación del portal comercial"
echo "============================================================"

if [[ ! -d "$PROJECT" ]]; then
    echo "ERROR: no existe el proyecto:"
    echo "$PROJECT"
    exit 1
fi

if [[ ! -f "$PROJECT/pom.xml" ]]; then
    echo "ERROR: no se encontró pom.xml en:"
    echo "$PROJECT"
    exit 1
fi

cd "$PROJECT"

echo
echo "1. Creando respaldo..."
mkdir -p "$BACKUP_DIR"

for FILE in \
    "$CONTROLLER_DIR/CommerceLandingController.java" \
    "$TEMPLATE_DIR/commerce-landing.html" \
    "$STATIC_DIR/commerce.css" \
    "$SECURITY_FILE" \
    "$PROPERTIES_FILE"
do
    if [[ -f "$FILE" ]]; then
        RELATIVE_PATH="${FILE#$PROJECT/}"
        mkdir -p "$BACKUP_DIR/$(dirname "$RELATIVE_PATH")"
        cp "$FILE" "$BACKUP_DIR/$RELATIVE_PATH"
    fi
done

echo "Respaldo creado en:"
echo "$BACKUP_DIR"

echo
echo "2. Creando directorios..."
mkdir -p "$CONTROLLER_DIR"
mkdir -p "$TEMPLATE_DIR"
mkdir -p "$STATIC_DIR"

echo
echo "3. Creando controlador comercial..."

cat > "$CONTROLLER_DIR/CommerceLandingController.java" <<'JAVA'
package com.abarrote.abarroteapi.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommerceLandingController {

    private final String whatsapp;
    private final String nombreComercial;

    public CommerceLandingController(
            @Value("${commerce.marketing.whatsapp:5215512345678}")
            String whatsapp,
            @Value("${commerce.marketing.nombre:Commerce Cloud}")
            String nombreComercial) {

        this.whatsapp = limpiarTelefono(whatsapp);
        this.nombreComercial = nombreComercial;
    }

    @GetMapping({
            "/",
            "/inicio",
            "/commerce-cloud",
            "/productos"
    })
    public String mostrarPortalComercial(Model model) {

        model.addAttribute("nombreComercial", nombreComercial);

        model.addAttribute(
                "whatsappGeneral",
                crearWhatsappUrl(
                        "Hola, me interesa conocer Commerce Cloud y solicitar una demostración."
                )
        );

        model.addAttribute(
                "whatsappAbarrotes",
                crearWhatsappUrl(
                        "Hola, me interesa una demostración de Abarrotes Cloud para mi negocio."
                )
        );

        model.addAttribute(
                "whatsappGym",
                crearWhatsappUrl(
                        "Hola, me interesa una demostración de Gym Cloud para administrar mi gimnasio."
                )
        );

        model.addAttribute(
                "whatsappRestaurante",
                crearWhatsappUrl(
                        "Hola, me interesa una demostración de Restaurante Cloud para mi restaurante."
                )
        );

        return "commerce-landing";
    }

    private String crearWhatsappUrl(String mensaje) {
        String mensajeCodificado =
                URLEncoder.encode(mensaje, StandardCharsets.UTF_8);

        return "https://wa.me/" + whatsapp + "?text=" + mensajeCodificado;
    }

    private String limpiarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return "5215512345678";
        }

        return telefono.replaceAll("[^0-9]", "");
    }
}
JAVA

echo
echo "4. Creando estilos comerciales..."

cat > "$STATIC_DIR/commerce.css" <<'CSS'
:root {
    --cc-bg: #07111f;
    --cc-bg-secondary: #0c192a;
    --cc-card: rgba(18, 35, 56, 0.92);
    --cc-card-soft: rgba(20, 42, 68, 0.72);
    --cc-text: #f8fafc;
    --cc-muted: #aab8ca;
    --cc-primary: #24d6a3;
    --cc-primary-dark: #13ac82;
    --cc-blue: #45a6ff;
    --cc-warning: #ffbd59;
    --cc-border: rgba(167, 197, 230, 0.18);
    --cc-danger: #ff6b7a;
    --cc-shadow: 0 20px 60px rgba(0, 0, 0, 0.32);
}

* {
    box-sizing: border-box;
}

html {
    scroll-behavior: smooth;
}

body {
    margin: 0;
    min-height: 100vh;
    background:
        radial-gradient(circle at top left, rgba(36, 214, 163, 0.12), transparent 34%),
        radial-gradient(circle at top right, rgba(69, 166, 255, 0.12), transparent 30%),
        var(--cc-bg);
    color: var(--cc-text);
    font-family:
        Inter,
        system-ui,
        -apple-system,
        BlinkMacSystemFont,
        "Segoe UI",
        sans-serif;
}

a {
    color: inherit;
}

.cc-container {
    width: min(1180px, calc(100% - 32px));
    margin: 0 auto;
}

.cc-header {
    position: sticky;
    top: 0;
    z-index: 20;
    border-bottom: 1px solid var(--cc-border);
    background: rgba(7, 17, 31, 0.88);
    backdrop-filter: blur(18px);
}

.cc-nav {
    min-height: 72px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
}

.cc-brand {
    display: flex;
    align-items: center;
    gap: 12px;
    text-decoration: none;
    font-size: 1.12rem;
    font-weight: 800;
}

.cc-logo {
    width: 42px;
    height: 42px;
    display: grid;
    place-items: center;
    border-radius: 13px;
    color: #07111f;
    background: linear-gradient(135deg, var(--cc-primary), #78f1ce);
    font-weight: 900;
    box-shadow: 0 10px 28px rgba(36, 214, 163, 0.28);
}

.cc-links {
    display: flex;
    align-items: center;
    gap: 22px;
}

.cc-links a {
    text-decoration: none;
    color: var(--cc-muted);
    font-weight: 650;
}

.cc-links a:hover {
    color: var(--cc-text);
}

.cc-button {
    min-height: 46px;
    padding: 0 20px;
    border: 1px solid transparent;
    border-radius: 12px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 9px;
    text-decoration: none;
    font-weight: 800;
    cursor: pointer;
    transition:
        transform 0.18s ease,
        border-color 0.18s ease,
        background 0.18s ease;
}

.cc-button:hover {
    transform: translateY(-2px);
}

.cc-button-primary {
    color: #05130f;
    background: linear-gradient(135deg, var(--cc-primary), #72efcc);
    box-shadow: 0 12px 30px rgba(36, 214, 163, 0.22);
}

.cc-button-secondary {
    color: var(--cc-text);
    border-color: var(--cc-border);
    background: rgba(255, 255, 255, 0.04);
}

.cc-hero {
    min-height: 680px;
    padding: 100px 0 70px;
    display: grid;
    grid-template-columns: 1.15fr 0.85fr;
    align-items: center;
    gap: 52px;
}

.cc-eyebrow {
    margin-bottom: 20px;
    display: inline-flex;
    padding: 8px 13px;
    border: 1px solid rgba(36, 214, 163, 0.25);
    border-radius: 999px;
    color: #a9f7df;
    background: rgba(36, 214, 163, 0.08);
    font-size: 0.88rem;
    font-weight: 800;
    letter-spacing: 0.04em;
    text-transform: uppercase;
}

.cc-hero h1 {
    margin: 0;
    max-width: 780px;
    font-size: clamp(2.7rem, 6vw, 5.3rem);
    line-height: 0.98;
    letter-spacing: -0.06em;
}

.cc-gradient-text {
    color: transparent;
    background: linear-gradient(
        100deg,
        var(--cc-primary),
        #7cefd0,
        var(--cc-blue)
    );
    background-clip: text;
    -webkit-background-clip: text;
}

.cc-hero-description {
    max-width: 700px;
    margin: 28px 0 0;
    color: var(--cc-muted);
    font-size: 1.15rem;
    line-height: 1.75;
}

.cc-hero-actions {
    margin-top: 34px;
    display: flex;
    flex-wrap: wrap;
    gap: 13px;
}

.cc-hero-note {
    margin-top: 18px;
    color: #8293aa;
    font-size: 0.92rem;
}

.cc-hero-panel {
    position: relative;
    padding: 24px;
    border: 1px solid var(--cc-border);
    border-radius: 26px;
    background:
        linear-gradient(
            145deg,
            rgba(22, 44, 70, 0.96),
            rgba(10, 24, 42, 0.94)
        );
    box-shadow: var(--cc-shadow);
}

.cc-window-header {
    padding-bottom: 19px;
    display: flex;
    align-items: center;
    gap: 8px;
    border-bottom: 1px solid var(--cc-border);
}

.cc-dot {
    width: 10px;
    height: 10px;
    border-radius: 999px;
}

.cc-dot-red {
    background: var(--cc-danger);
}

.cc-dot-yellow {
    background: var(--cc-warning);
}

.cc-dot-green {
    background: var(--cc-primary);
}

.cc-dashboard-preview {
    padding-top: 22px;
    display: grid;
    gap: 14px;
}

.cc-preview-row {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
}

.cc-preview-card {
    min-height: 120px;
    padding: 18px;
    border: 1px solid var(--cc-border);
    border-radius: 16px;
    background: var(--cc-card-soft);
}

.cc-preview-label {
    color: var(--cc-muted);
    font-size: 0.86rem;
}

.cc-preview-value {
    margin-top: 13px;
    font-size: 1.65rem;
    font-weight: 900;
}

.cc-preview-value.green {
    color: var(--cc-primary);
}

.cc-preview-value.blue {
    color: var(--cc-blue);
}

.cc-section {
    padding: 88px 0;
}

.cc-section-soft {
    border-top: 1px solid var(--cc-border);
    border-bottom: 1px solid var(--cc-border);
    background: rgba(255, 255, 255, 0.018);
}

.cc-section-heading {
    max-width: 760px;
    margin-bottom: 42px;
}

.cc-section-heading.centered {
    margin-right: auto;
    margin-left: auto;
    text-align: center;
}

.cc-section-heading h2 {
    margin: 0;
    font-size: clamp(2rem, 4vw, 3.4rem);
    letter-spacing: -0.045em;
}

.cc-section-heading p {
    margin: 17px 0 0;
    color: var(--cc-muted);
    font-size: 1.08rem;
    line-height: 1.7;
}

.cc-products {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 22px;
}

.cc-product-card {
    position: relative;
    overflow: hidden;
    padding: 28px;
    border: 1px solid var(--cc-border);
    border-radius: 22px;
    background: var(--cc-card);
    box-shadow: 0 14px 40px rgba(0, 0, 0, 0.16);
}

.cc-product-card.featured {
    border-color: rgba(36, 214, 163, 0.48);
    transform: translateY(-8px);
}

.cc-product-card::before {
    position: absolute;
    top: -70px;
    right: -70px;
    width: 180px;
    height: 180px;
    border-radius: 999px;
    background: rgba(69, 166, 255, 0.1);
    content: "";
}

.cc-product-card.featured::before {
    background: rgba(36, 214, 163, 0.12);
}

.cc-product-badge {
    display: inline-flex;
    margin-bottom: 20px;
    padding: 7px 11px;
    border-radius: 999px;
    color: #bff9e8;
    background: rgba(36, 214, 163, 0.1);
    font-size: 0.79rem;
    font-weight: 850;
}

.cc-product-icon {
    margin-bottom: 20px;
    font-size: 2.5rem;
}

.cc-product-card h3 {
    margin: 0;
    font-size: 1.55rem;
}

.cc-product-card p {
    min-height: 72px;
    color: var(--cc-muted);
    line-height: 1.65;
}

.cc-features {
    margin: 24px 0;
    padding: 0;
    display: grid;
    gap: 12px;
    list-style: none;
}

.cc-features li {
    display: flex;
    gap: 10px;
    color: #d7e0eb;
}

.cc-features li::before {
    color: var(--cc-primary);
    content: "✓";
    font-weight: 900;
}

.cc-price {
    margin: 26px 0 18px;
}

.cc-price strong {
    font-size: 2.2rem;
}

.cc-price span {
    color: var(--cc-muted);
}

.cc-product-card .cc-button {
    width: 100%;
}

.cc-benefits {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 18px;
}

.cc-benefit {
    padding: 24px;
    border: 1px solid var(--cc-border);
    border-radius: 18px;
    background: rgba(255, 255, 255, 0.028);
}

.cc-benefit strong {
    display: block;
    margin-bottom: 10px;
    font-size: 1.05rem;
}

.cc-benefit span {
    color: var(--cc-muted);
    line-height: 1.55;
}

.cc-pricing {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 22px;
}

.cc-plan {
    padding: 30px;
    border: 1px solid var(--cc-border);
    border-radius: 22px;
    background: var(--cc-card);
}

.cc-plan.recommended {
    border-color: rgba(36, 214, 163, 0.55);
    box-shadow: 0 18px 54px rgba(36, 214, 163, 0.11);
}

.cc-plan-name {
    color: var(--cc-primary);
    font-weight: 900;
}

.cc-plan h3 {
    margin: 16px 0 6px;
    font-size: 2.4rem;
}

.cc-plan h3 span {
    color: var(--cc-muted);
    font-size: 0.94rem;
    font-weight: 600;
}

.cc-plan p {
    min-height: 54px;
    color: var(--cc-muted);
    line-height: 1.55;
}

.cc-cta {
    padding: 50px;
    border: 1px solid rgba(36, 214, 163, 0.25);
    border-radius: 28px;
    text-align: center;
    background:
        radial-gradient(
            circle at center top,
            rgba(36, 214, 163, 0.16),
            transparent 60%
        ),
        var(--cc-card);
    box-shadow: var(--cc-shadow);
}

.cc-cta h2 {
    margin: 0;
    font-size: clamp(2rem, 4vw, 3.35rem);
    letter-spacing: -0.045em;
}

.cc-cta p {
    max-width: 680px;
    margin: 18px auto 28px;
    color: var(--cc-muted);
    line-height: 1.7;
}

.cc-footer {
    padding: 34px 0;
    border-top: 1px solid var(--cc-border);
    color: var(--cc-muted);
}

.cc-footer-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 18px;
}

.cc-mobile-login {
    display: none;
}

@media (max-width: 960px) {
    .cc-links {
        display: none;
    }

    .cc-mobile-login {
        display: inline-flex;
    }

    .cc-hero {
        min-height: auto;
        grid-template-columns: 1fr;
        padding-top: 72px;
    }

    .cc-products,
    .cc-pricing {
        grid-template-columns: 1fr;
    }

    .cc-product-card.featured {
        transform: none;
    }

    .cc-benefits {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

@media (max-width: 620px) {
    .cc-container {
        width: min(100% - 22px, 1180px);
    }

    .cc-nav {
        min-height: 64px;
    }

    .cc-brand span:last-child {
        display: none;
    }

    .cc-hero {
        padding-top: 48px;
    }

    .cc-hero h1 {
        font-size: 2.85rem;
    }

    .cc-hero-actions {
        display: grid;
    }

    .cc-hero-actions .cc-button {
        width: 100%;
    }

    .cc-hero-panel {
        padding: 15px;
    }

    .cc-preview-row,
    .cc-benefits {
        grid-template-columns: 1fr;
    }

    .cc-section {
        padding: 64px 0;
    }

    .cc-product-card,
    .cc-plan {
        padding: 23px;
    }

    .cc-cta {
        padding: 35px 22px;
    }

    .cc-footer-content {
        align-items: flex-start;
        flex-direction: column;
    }
}
CSS

echo
echo "5. Creando landing page..."

cat > "$TEMPLATE_DIR/commerce-landing.html" <<'HTML'
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0"
    >

    <meta
        name="description"
        content="Commerce Cloud: punto de venta, inventario, ventas y administración para abarrotes, gimnasios y restaurantes."
    >

    <title>Commerce Cloud | Administra y haz crecer tu negocio</title>

    <link
        rel="stylesheet"
        th:href="@{/commerce/commerce.css}"
        href="/commerce/commerce.css"
    >
</head>

<body>

<header class="cc-header">
    <div class="cc-container cc-nav">

        <a class="cc-brand" th:href="@{/}">
            <span class="cc-logo">CC</span>
            <span th:text="${nombreComercial}">Commerce Cloud</span>
        </a>

        <nav class="cc-links" aria-label="Navegación principal">
            <a href="#productos">Productos</a>
            <a href="#beneficios">Beneficios</a>
            <a href="#precios">Precios</a>
            <a
                class="cc-button cc-button-secondary"
                th:href="@{/login}"
            >
                Entrar
            </a>
        </nav>

        <a
            class="cc-button cc-button-secondary cc-mobile-login"
            th:href="@{/login}"
        >
            Entrar
        </a>
    </div>
</header>

<main>

    <section class="cc-container cc-hero">

        <div>
            <span class="cc-eyebrow">
                Tecnología mexicana para pequeños negocios
            </span>

            <h1>
                Administra todo tu negocio desde
                <span class="cc-gradient-text">una sola plataforma.</span>
            </h1>

            <p class="cc-hero-description">
                Punto de venta, inventario, clientes, pedidos, membresías,
                reportes y control administrativo para tiendas, gimnasios
                y restaurantes.
            </p>

            <div class="cc-hero-actions">
                <a
                    class="cc-button cc-button-primary"
                    th:href="${whatsappGeneral}"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Solicitar demostración
                </a>

                <a
                    class="cc-button cc-button-secondary"
                    href="#productos"
                >
                    Conocer productos
                </a>
            </div>

            <p class="cc-hero-note">
                Sin instalaciones complicadas. Funciona desde celular,
                tableta y computadora.
            </p>
        </div>

        <div class="cc-hero-panel" aria-label="Vista previa del sistema">

            <div class="cc-window-header">
                <span class="cc-dot cc-dot-red"></span>
                <span class="cc-dot cc-dot-yellow"></span>
                <span class="cc-dot cc-dot-green"></span>
            </div>

            <div class="cc-dashboard-preview">

                <div class="cc-preview-row">
                    <article class="cc-preview-card">
                        <span class="cc-preview-label">Ventas de hoy</span>
                        <div class="cc-preview-value green">$12,480</div>
                    </article>

                    <article class="cc-preview-card">
                        <span class="cc-preview-label">Operaciones</span>
                        <div class="cc-preview-value blue">143</div>
                    </article>
                </div>

                <div class="cc-preview-row">
                    <article class="cc-preview-card">
                        <span class="cc-preview-label">Negocio activo</span>
                        <div class="cc-preview-value">Restaurante</div>
                    </article>

                    <article class="cc-preview-card">
                        <span class="cc-preview-label">Estado</span>
                        <div class="cc-preview-value green">En línea</div>
                    </article>
                </div>

            </div>
        </div>

    </section>

    <section id="productos" class="cc-section cc-section-soft">

        <div class="cc-container">

            <div class="cc-section-heading centered">
                <h2>Elige la solución para tu negocio</h2>

                <p>
                    Cada producto utiliza la misma plataforma segura,
                    pero incluye herramientas especializadas para cada
                    tipo de comercio.
                </p>
            </div>

            <div class="cc-products">

                <article class="cc-product-card">

                    <div class="cc-product-icon" aria-hidden="true">🛒</div>

                    <h3>Abarrotes Cloud</h3>

                    <p>
                        Controla ventas, existencias, productos y cajas
                        desde una interfaz sencilla.
                    </p>

                    <ul class="cc-features">
                        <li>Punto de venta</li>
                        <li>Inventario y existencias</li>
                        <li>Corte de caja</li>
                        <li>Productos y categorías</li>
                        <li>Reportes de ventas</li>
                        <li>Asistente inteligente</li>
                    </ul>

                    <div class="cc-price">
                        <strong>$399</strong>
                        <span>MXN al mes</span>
                    </div>

                    <a
                        class="cc-button cc-button-secondary"
                        th:href="${whatsappAbarrotes}"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        Solicitar demo
                    </a>
                </article>

                <article class="cc-product-card">

                    <div class="cc-product-icon" aria-hidden="true">🏋️</div>

                    <h3>Gym Cloud</h3>

                    <p>
                        Administra clientes, membresías, pagos, productos
                        y el crecimiento de tu gimnasio.
                    </p>

                    <ul class="cc-features">
                        <li>Registro de clientes</li>
                        <li>Planes y membresías</li>
                        <li>Control de pagos</li>
                        <li>Venta de productos</li>
                        <li>Dashboard administrativo</li>
                        <li>Carga masiva desde Excel</li>
                    </ul>

                    <div class="cc-price">
                        <strong>$499</strong>
                        <span>MXN al mes</span>
                    </div>

                    <a
                        class="cc-button cc-button-secondary"
                        th:href="${whatsappGym}"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        Solicitar demo
                    </a>
                </article>

                <article class="cc-product-card featured">

                    <span class="cc-product-badge">
                        Nuevo producto
                    </span>

                    <div class="cc-product-icon" aria-hidden="true">🍽️</div>

                    <h3>Restaurante Cloud</h3>

                    <p>
                        Meseros, mesas, pedidos y ventas trabajando juntos
                        desde celulares y computadoras.
                    </p>

                    <ul class="cc-features">
                        <li>Pedidos desde el celular</li>
                        <li>Control de mesas</li>
                        <li>Catálogo de platillos</li>
                        <li>Ventas por mesero</li>
                        <li>Corte diario</li>
                        <li>Reportes administrativos</li>
                    </ul>

                    <div class="cc-price">
                        <strong>$599</strong>
                        <span>MXN al mes</span>
                    </div>

                    <a
                        class="cc-button cc-button-primary"
                        th:href="${whatsappRestaurante}"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        Solicitar demo
                    </a>
                </article>

            </div>
        </div>
    </section>

    <section id="beneficios" class="cc-section">

        <div class="cc-container">

            <div class="cc-section-heading">
                <h2>Una plataforma preparada para crecer</h2>

                <p>
                    Commerce Cloud funciona bajo una arquitectura
                    multi-tenant que mantiene separados los datos de
                    cada cliente y permite administrar distintos tipos
                    de negocio.
                </p>
            </div>

            <div class="cc-benefits">

                <article class="cc-benefit">
                    <strong>Acceso desde cualquier lugar</strong>
                    <span>
                        Utiliza el sistema desde celular, tableta o computadora.
                    </span>
                </article>

                <article class="cc-benefit">
                    <strong>Información separada</strong>
                    <span>
                        Cada empresa consulta únicamente sus propios usuarios,
                        productos y ventas.
                    </span>
                </article>

                <article class="cc-benefit">
                    <strong>Actualizaciones automáticas</strong>
                    <span>
                        Las mejoras se publican sin reinstalar el sistema
                        en cada equipo.
                    </span>
                </article>

                <article class="cc-benefit">
                    <strong>Soporte cercano</strong>
                    <span>
                        Atención directa para configuración, capacitación
                        y resolución de dudas.
                    </span>
                </article>

            </div>
        </div>
    </section>

    <section id="precios" class="cc-section cc-section-soft">

        <div class="cc-container">

            <div class="cc-section-heading centered">
                <h2>Planes sencillos y sin costos ocultos</h2>

                <p>
                    Los precios iniciales pueden ajustarse conforme
                    validemos el producto con clientes reales.
                </p>
            </div>

            <div class="cc-pricing">

                <article class="cc-plan">
                    <span class="cc-plan-name">Emprendedor</span>
                    <h3>$299 <span>MXN/mes</span></h3>
                    <p>Para negocios que comienzan a digitalizarse.</p>

                    <ul class="cc-features">
                        <li>1 sucursal</li>
                        <li>2 usuarios</li>
                        <li>Operaciones básicas</li>
                        <li>Reportes esenciales</li>
                    </ul>
                </article>

                <article class="cc-plan recommended">
                    <span class="cc-plan-name">Profesional</span>
                    <h3>$599 <span>MXN/mes</span></h3>
                    <p>La mejor opción para pequeños negocios activos.</p>

                    <ul class="cc-features">
                        <li>1 sucursal</li>
                        <li>Hasta 10 usuarios</li>
                        <li>Reportes completos</li>
                        <li>Exportación Excel</li>
                        <li>Asistente inteligente</li>
                        <li>Soporte por WhatsApp</li>
                    </ul>
                </article>

                <article class="cc-plan">
                    <span class="cc-plan-name">Business</span>
                    <h3>$999 <span>MXN/mes</span></h3>
                    <p>Para empresas con más usuarios o sucursales.</p>

                    <ul class="cc-features">
                        <li>Varias sucursales</li>
                        <li>Usuarios adicionales</li>
                        <li>Dashboard ejecutivo</li>
                        <li>Soporte prioritario</li>
                    </ul>
                </article>

            </div>
        </div>
    </section>

    <section class="cc-section">

        <div class="cc-container">

            <div class="cc-cta">

                <h2>Empieza a controlar mejor tu negocio</h2>

                <p>
                    Solicita una demostración personalizada. Te mostramos
                    cómo Commerce Cloud puede adaptarse a tus operaciones
                    actuales.
                </p>

                <a
                    class="cc-button cc-button-primary"
                    th:href="${whatsappGeneral}"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Hablar por WhatsApp
                </a>

            </div>
        </div>
    </section>

</main>

<footer class="cc-footer">
    <div class="cc-container cc-footer-content">

        <span>
            © 2026 Commerce Cloud. Plataforma para pequeños negocios.
        </span>

        <a th:href="@{/login}">
            Acceso para clientes
        </a>

    </div>
</footer>

</body>
</html>
HTML

echo
echo "6. Configurando datos comerciales..."

if [[ ! -f "$PROPERTIES_FILE" ]]; then
    touch "$PROPERTIES_FILE"
fi

if ! grep -q '^commerce.marketing.nombre=' "$PROPERTIES_FILE"; then
    cat >> "$PROPERTIES_FILE" <<'PROPERTIES'

# ============================================================
# Commerce Cloud - configuración comercial
# Reemplazar el teléfono con el número real, incluyendo 52.
# Ejemplo México: 5215512345678
# ============================================================
commerce.marketing.nombre=${COMMERCE_MARKETING_NOMBRE:Commerce Cloud}
commerce.marketing.whatsapp=${COMMERCE_MARKETING_WHATSAPP:5215512345678}
PROPERTIES
fi

echo
echo "7. Revisando Spring Security..."

if [[ ! -f "$SECURITY_FILE" ]]; then
    echo "ERROR: no existe SecurityConfig.java:"
    echo "$SECURITY_FILE"
    echo
    echo "Los archivos comerciales fueron creados, pero no se modificó seguridad."
    exit 1
fi

python3 - "$SECURITY_FILE" <<'PYTHON'
from pathlib import Path
import sys

security_path = Path(sys.argv[1])
text = security_path.read_text(encoding="utf-8")

public_routes = (
    '"/", "/inicio", "/commerce-cloud", "/productos", '
    '"/commerce/**", "/css/**", "/js/**", "/images/**"'
)

if '"/commerce/**"' in text and '"/commerce-cloud"' in text:
    print("Las rutas comerciales ya estaban declaradas en SecurityConfig.")
    sys.exit(0)

patterns = [
    ".anyRequest().authenticated()",
    ".anyRequest().authenticated();"
]

replacement_base = (
    f'.requestMatchers({public_routes}).permitAll()\n'
    '                    .anyRequest().authenticated()'
)

for pattern in patterns:
    if pattern in text:
        replacement = replacement_base
        if pattern.endswith(";"):
            replacement += ";"

        text = text.replace(pattern, replacement, 1)
        security_path.write_text(text, encoding="utf-8")
        print("SecurityConfig actualizado automáticamente.")
        sys.exit(0)

print("""
ERROR: no se encontró el patrón .anyRequest().authenticated()
en SecurityConfig.java.

No se aplicó una modificación insegura o ambigua.
Revisa el archivo y agrega las rutas públicas antes de anyRequest().
""".strip())

sys.exit(1)
PYTHON

echo
echo "8. Validando archivos creados..."

test -f "$CONTROLLER_DIR/CommerceLandingController.java"
test -f "$TEMPLATE_DIR/commerce-landing.html"
test -f "$STATIC_DIR/commerce.css"

echo
echo "9. Ejecutando validación Git..."
git diff --check

echo
echo "10. Compilando proyecto..."
mvn clean compile

echo
echo "============================================================"
echo " PORTAL COMERCIAL INSTALADO CORRECTAMENTE"
echo "============================================================"
echo
echo "Rutas:"
echo "  http://localhost:8080/"
echo "  http://localhost:8080/inicio"
echo "  http://localhost:8080/productos"
echo "  http://localhost:8080/login"
echo
echo "IMPORTANTE:"
echo "Configura tu número real:"
echo
echo "  export COMMERCE_MARKETING_WHATSAPP=521XXXXXXXXXX"
echo
echo "Después inicia la aplicación."
echo
echo "Estado Git:"
git status --short
