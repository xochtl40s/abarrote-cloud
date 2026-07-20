#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_DIR="$PROJECT/.backups/portal-premium-v2-$TIMESTAMP"

TEMPLATE_DIR="$PROJECT/src/main/resources/templates"
STATIC_DIR="$PROJECT/src/main/resources/static/commerce"

LANDING_FILE="$TEMPLATE_DIR/commerce-landing.html"
LOGIN_FILE="$TEMPLATE_DIR/login.html"
CSS_FILE="$STATIC_DIR/commerce-premium.css"
JS_FILE="$STATIC_DIR/commerce-premium.js"

echo "============================================================"
echo " COMMERCE CLOUD"
echo " Portal comercial premium V2"
echo "============================================================"

if [[ ! -d "$PROJECT" ]]; then
    echo "ERROR: no existe el proyecto:"
    echo "$PROJECT"
    exit 1
fi

if [[ ! -f "$PROJECT/pom.xml" ]]; then
    echo "ERROR: no se encontró pom.xml"
    exit 1
fi

cd "$PROJECT"

echo
echo "1. Creando respaldo..."

mkdir -p "$BACKUP_DIR"

for FILE in \
    "$LANDING_FILE" \
    "$LOGIN_FILE" \
    "$CSS_FILE" \
    "$JS_FILE"
do
    if [[ -f "$FILE" ]]; then
        RELATIVE="${FILE#$PROJECT/}"
        mkdir -p "$BACKUP_DIR/$(dirname "$RELATIVE")"
        cp "$FILE" "$BACKUP_DIR/$RELATIVE"
    fi
done

echo "Respaldo:"
echo "$BACKUP_DIR"

mkdir -p "$TEMPLATE_DIR"
mkdir -p "$STATIC_DIR"

echo
echo "2. Creando hoja de estilos premium..."

cat > "$CSS_FILE" <<'CSS'
:root {
    --cc-green: #008060;
    --cc-green-dark: #004c3f;
    --cc-green-soft: #e8f5f1;
    --cc-lime: #d2f34c;
    --cc-blue: #2c6ecb;
    --cc-purple: #6d3af2;
    --cc-orange: #ff8a3d;
    --cc-red: #e5484d;

    --cc-ink: #17221f;
    --cc-text: #34433f;
    --cc-muted: #667671;
    --cc-border: #dce5e2;
    --cc-background: #ffffff;
    --cc-soft-background: #f4f7f6;

    --cc-radius-sm: 12px;
    --cc-radius-md: 20px;
    --cc-radius-lg: 32px;

    --cc-shadow:
        0 18px 50px rgba(17, 47, 38, 0.10);

    --cc-shadow-hover:
        0 24px 70px rgba(17, 47, 38, 0.17);
}

* {
    box-sizing: border-box;
}

html {
    scroll-behavior: smooth;
}

body {
    margin: 0;
    color: var(--cc-text);
    background: var(--cc-background);
    font-family:
        Inter,
        ui-sans-serif,
        system-ui,
        -apple-system,
        BlinkMacSystemFont,
        "Segoe UI",
        sans-serif;
}

body.menu-open {
    overflow: hidden;
}

a {
    color: inherit;
}

button,
input {
    font: inherit;
}

img {
    max-width: 100%;
}

.cc-container {
    width: min(1180px, calc(100% - 40px));
    margin-inline: auto;
}

/* ============================================================
   BARRA SUPERIOR
   ============================================================ */

.cc-announcement {
    padding: 10px 20px;
    color: #ffffff;
    background: var(--cc-green-dark);
    text-align: center;
    font-size: 0.9rem;
    font-weight: 700;
}

.cc-announcement a {
    margin-left: 8px;
    color: var(--cc-lime);
    font-weight: 900;
}

/* ============================================================
   NAVEGACIÓN
   ============================================================ */

.cc-header {
    position: sticky;
    top: 0;
    z-index: 100;
    border-bottom: 1px solid rgba(220, 229, 226, 0.9);
    background: rgba(255, 255, 255, 0.94);
    backdrop-filter: blur(16px);
}

.cc-nav {
    min-height: 78px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 28px;
}

.cc-brand {
    display: inline-flex;
    align-items: center;
    gap: 12px;
    color: var(--cc-ink);
    text-decoration: none;
    font-size: 1.14rem;
    font-weight: 900;
    letter-spacing: -0.02em;
}

.cc-brand-mark {
    width: 43px;
    height: 43px;
    display: grid;
    place-items: center;
    border-radius: 13px;
    color: #ffffff;
    background:
        linear-gradient(
            135deg,
            var(--cc-green),
            var(--cc-green-dark)
        );
    box-shadow:
        0 10px 25px rgba(0, 128, 96, 0.24);
}

.cc-navigation {
    display: flex;
    align-items: center;
    gap: 27px;
}

.cc-navigation > a {
    color: var(--cc-text);
    text-decoration: none;
    font-size: 0.95rem;
    font-weight: 750;
}

.cc-navigation > a:hover {
    color: var(--cc-green);
}

.cc-nav-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.cc-menu-button {
    width: 46px;
    height: 46px;
    display: none;
    place-items: center;
    border: 1px solid var(--cc-border);
    border-radius: 12px;
    color: var(--cc-ink);
    background: #ffffff;
    cursor: pointer;
}

/* ============================================================
   BOTONES
   ============================================================ */

.cc-button {
    min-height: 50px;
    padding: 0 22px;
    border: 1px solid transparent;
    border-radius: 12px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 9px;
    text-decoration: none;
    font-weight: 850;
    cursor: pointer;
    transition:
        transform 180ms ease,
        box-shadow 180ms ease,
        background 180ms ease;
}

.cc-button:hover {
    transform: translateY(-2px);
}

.cc-button-primary {
    color: #ffffff;
    background: var(--cc-green);
    box-shadow:
        0 13px 28px rgba(0, 128, 96, 0.22);
}

.cc-button-primary:hover {
    background: var(--cc-green-dark);
}

.cc-button-dark {
    color: #ffffff;
    background: var(--cc-ink);
}

.cc-button-light {
    color: var(--cc-ink);
    border-color: var(--cc-border);
    background: #ffffff;
}

.cc-button-white {
    color: var(--cc-green-dark);
    background: #ffffff;
}

.cc-button-block {
    width: 100%;
}

/* ============================================================
   HERO
   ============================================================ */

.cc-hero {
    position: relative;
    overflow: hidden;
    padding: 90px 0 80px;
    background:
        radial-gradient(
            circle at 15% 5%,
            rgba(210, 243, 76, 0.24),
            transparent 30%
        ),
        radial-gradient(
            circle at 95% 30%,
            rgba(0, 128, 96, 0.17),
            transparent 34%
        ),
        linear-gradient(
            180deg,
            #f6fbf8,
            #ffffff
        );
}

.cc-hero-grid {
    display: grid;
    grid-template-columns:
        minmax(0, 1.04fr)
        minmax(420px, 0.96fr);
    align-items: center;
    gap: 70px;
}

.cc-kicker {
    margin-bottom: 22px;
    display: inline-flex;
    align-items: center;
    gap: 9px;
    padding: 8px 14px;
    border: 1px solid #c9e3da;
    border-radius: 999px;
    color: var(--cc-green-dark);
    background: rgba(255, 255, 255, 0.8);
    font-size: 0.86rem;
    font-weight: 900;
}

.cc-kicker-dot {
    width: 9px;
    height: 9px;
    border-radius: 999px;
    background: var(--cc-green);
    box-shadow:
        0 0 0 5px rgba(0, 128, 96, 0.12);
}

.cc-hero h1 {
    margin: 0;
    color: var(--cc-ink);
    font-size: clamp(3.2rem, 6.1vw, 5.7rem);
    line-height: 0.98;
    letter-spacing: -0.065em;
}

.cc-highlight {
    position: relative;
    display: inline-block;
    color: var(--cc-green);
}

.cc-highlight::after {
    position: absolute;
    right: 0;
    bottom: -5px;
    left: 0;
    height: 10px;
    z-index: -1;
    border-radius: 999px;
    background: var(--cc-lime);
    content: "";
    transform: rotate(-1deg);
}

.cc-hero-description {
    max-width: 680px;
    margin: 27px 0 0;
    color: var(--cc-muted);
    font-size: 1.18rem;
    line-height: 1.75;
}

.cc-hero-actions {
    margin-top: 33px;
    display: flex;
    flex-wrap: wrap;
    gap: 13px;
}

.cc-hero-note {
    margin-top: 18px;
    display: flex;
    flex-wrap: wrap;
    gap: 18px;
    color: var(--cc-muted);
    font-size: 0.9rem;
    font-weight: 700;
}

.cc-hero-note span::before {
    margin-right: 7px;
    color: var(--cc-green);
    content: "✓";
    font-weight: 900;
}

/* ============================================================
   MOCKUP PRINCIPAL
   ============================================================ */

.cc-product-preview {
    position: relative;
}

.cc-preview-shadow {
    position: absolute;
    right: 5%;
    bottom: -35px;
    left: 5%;
    height: 70px;
    border-radius: 50%;
    background: rgba(0, 76, 63, 0.18);
    filter: blur(30px);
}

.cc-browser {
    position: relative;
    overflow: hidden;
    border: 1px solid #cad9d4;
    border-radius: 24px;
    background: #ffffff;
    box-shadow:
        0 35px 90px rgba(28, 67, 55, 0.21);
    transform:
        perspective(1100px)
        rotateY(-4deg)
        rotateX(1deg);
}

.cc-browser-top {
    min-height: 55px;
    padding: 0 19px;
    display: flex;
    align-items: center;
    gap: 8px;
    border-bottom: 1px solid var(--cc-border);
    background: #f7f9f8;
}

.cc-browser-dot {
    width: 10px;
    height: 10px;
    border-radius: 999px;
}

.cc-browser-dot.red {
    background: #ff665e;
}

.cc-browser-dot.yellow {
    background: #ffbd2e;
}

.cc-browser-dot.green {
    background: #28c840;
}

.cc-browser-address {
    height: 30px;
    margin-left: 12px;
    padding: 0 16px;
    display: flex;
    align-items: center;
    flex: 1;
    border: 1px solid #d8e0dd;
    border-radius: 8px;
    color: #77827f;
    background: #ffffff;
    font-size: 0.78rem;
}

.cc-dashboard {
    display: grid;
    grid-template-columns: 105px 1fr;
    min-height: 470px;
}

.cc-dashboard-sidebar {
    padding: 24px 13px;
    color: #ffffff;
    background: var(--cc-green-dark);
}

.cc-dashboard-logo {
    margin-bottom: 28px;
    text-align: center;
    font-weight: 900;
}

.cc-dashboard-item {
    margin-bottom: 8px;
    padding: 9px;
    border-radius: 8px;
    font-size: 0.72rem;
    opacity: 0.76;
}

.cc-dashboard-item.active {
    color: var(--cc-green-dark);
    background: var(--cc-lime);
    opacity: 1;
    font-weight: 900;
}

.cc-dashboard-main {
    padding: 25px;
    background: #f5f8f7;
}

.cc-dashboard-title {
    color: var(--cc-ink);
    font-size: 1.15rem;
    font-weight: 900;
}

.cc-dashboard-subtitle {
    margin-top: 4px;
    color: var(--cc-muted);
    font-size: 0.74rem;
}

.cc-dashboard-cards {
    margin-top: 22px;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
}

.cc-dashboard-card {
    padding: 14px;
    border: 1px solid #dce5e2;
    border-radius: 13px;
    background: #ffffff;
}

.cc-dashboard-card-label {
    color: var(--cc-muted);
    font-size: 0.7rem;
}

.cc-dashboard-card-value {
    margin-top: 9px;
    color: var(--cc-ink);
    font-size: 1.25rem;
    font-weight: 900;
}

.cc-dashboard-card-value.green {
    color: var(--cc-green);
}

.cc-chart {
    height: 165px;
    margin-top: 16px;
    padding: 18px;
    border: 1px solid #dce5e2;
    border-radius: 15px;
    background: #ffffff;
}

.cc-chart-bars {
    height: 100%;
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 9px;
}

.cc-chart-bar {
    width: 11%;
    border-radius: 6px 6px 2px 2px;
    background:
        linear-gradient(
            180deg,
            var(--cc-green),
            #75cfb7
        );
}

.cc-floating-card {
    position: absolute;
    right: -25px;
    bottom: 30px;
    width: 190px;
    padding: 17px;
    border: 1px solid var(--cc-border);
    border-radius: 17px;
    background: #ffffff;
    box-shadow: var(--cc-shadow);
}

.cc-floating-label {
    color: var(--cc-muted);
    font-size: 0.74rem;
}

.cc-floating-value {
    margin-top: 7px;
    color: var(--cc-green);
    font-size: 1.45rem;
    font-weight: 900;
}

/* ============================================================
   CONFIANZA
   ============================================================ */

.cc-trust {
    padding: 32px 0;
    border-top: 1px solid var(--cc-border);
    border-bottom: 1px solid var(--cc-border);
}

.cc-trust-grid {
    display: grid;
    grid-template-columns:
        1.2fr repeat(4, 1fr);
    align-items: center;
    gap: 25px;
}

.cc-trust-title {
    color: var(--cc-muted);
    font-size: 0.9rem;
    font-weight: 800;
}

.cc-trust-item {
    color: var(--cc-ink);
    text-align: center;
    font-weight: 900;
}

/* ============================================================
   SECCIONES
   ============================================================ */

.cc-section {
    padding: 100px 0;
}

.cc-section-soft {
    background: var(--cc-soft-background);
}

.cc-section-dark {
    color: #ffffff;
    background: var(--cc-green-dark);
}

.cc-section-heading {
    max-width: 760px;
    margin-bottom: 46px;
}

.cc-section-heading.center {
    margin-right: auto;
    margin-left: auto;
    text-align: center;
}

.cc-section-eyebrow {
    display: block;
    margin-bottom: 12px;
    color: var(--cc-green);
    font-size: 0.82rem;
    font-weight: 950;
    letter-spacing: 0.13em;
    text-transform: uppercase;
}

.cc-section-dark .cc-section-eyebrow {
    color: var(--cc-lime);
}

.cc-section-heading h2 {
    margin: 0;
    color: var(--cc-ink);
    font-size: clamp(2.25rem, 4.4vw, 4rem);
    line-height: 1.05;
    letter-spacing: -0.05em;
}

.cc-section-dark .cc-section-heading h2 {
    color: #ffffff;
}

.cc-section-heading p {
    margin: 18px 0 0;
    color: var(--cc-muted);
    font-size: 1.08rem;
    line-height: 1.75;
}

.cc-section-dark .cc-section-heading p {
    color: rgba(255, 255, 255, 0.72);
}

/* ============================================================
   CARRUSEL DE VERTICALES
   ============================================================ */

.cc-carousel-wrapper {
    position: relative;
}

.cc-carousel-controls {
    margin-bottom: 18px;
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

.cc-carousel-button {
    width: 45px;
    height: 45px;
    border: 1px solid var(--cc-border);
    border-radius: 999px;
    color: var(--cc-ink);
    background: #ffffff;
    cursor: pointer;
    font-size: 1.15rem;
}

.cc-carousel {
    display: grid;
    grid-auto-flow: column;
    grid-auto-columns: minmax(320px, 1fr);
    gap: 22px;
    overflow-x: auto;
    padding: 10px 5px 25px;
    scroll-behavior: smooth;
    scroll-snap-type: x mandatory;
    scrollbar-width: thin;
}

.cc-solution-card {
    position: relative;
    min-height: 540px;
    overflow: hidden;
    border: 1px solid var(--cc-border);
    border-radius: 25px;
    background: #ffffff;
    box-shadow:
        0 14px 36px rgba(26, 55, 46, 0.07);
    scroll-snap-align: start;
    transition:
        transform 180ms ease,
        box-shadow 180ms ease;
}

.cc-solution-card:hover {
    transform: translateY(-6px);
    box-shadow: var(--cc-shadow-hover);
}

.cc-solution-cover {
    height: 190px;
    padding: 26px;
    display: flex;
    align-items: flex-end;
    color: #ffffff;
}

.cc-solution-cover.store {
    background:
        radial-gradient(
            circle at top right,
            rgba(255, 255, 255, 0.3),
            transparent 40%
        ),
        linear-gradient(
            135deg,
            #1d6d51,
            #003f35
        );
}

.cc-solution-cover.restaurant {
    background:
        radial-gradient(
            circle at top right,
            rgba(255, 255, 255, 0.24),
            transparent 40%
        ),
        linear-gradient(
            135deg,
            #f07835,
            #a63720
        );
}

.cc-solution-cover.gym {
    background:
        radial-gradient(
            circle at top right,
            rgba(255, 255, 255, 0.25),
            transparent 40%
        ),
        linear-gradient(
            135deg,
            #6d3af2,
            #33226e
        );
}

.cc-solution-icon {
    font-size: 4.2rem;
}

.cc-solution-content {
    padding: 27px;
}

.cc-solution-tag {
    display: inline-flex;
    margin-bottom: 15px;
    padding: 6px 10px;
    border-radius: 999px;
    color: var(--cc-green-dark);
    background: var(--cc-green-soft);
    font-size: 0.75rem;
    font-weight: 900;
}

.cc-solution-content h3 {
    margin: 0;
    color: var(--cc-ink);
    font-size: 1.7rem;
    letter-spacing: -0.035em;
}

.cc-solution-content > p {
    min-height: 77px;
    color: var(--cc-muted);
    line-height: 1.65;
}

.cc-feature-list {
    margin: 22px 0 27px;
    padding: 0;
    display: grid;
    gap: 11px;
    list-style: none;
}

.cc-feature-list li {
    display: flex;
    align-items: flex-start;
    gap: 10px;
}

.cc-feature-list li::before {
    width: 21px;
    height: 21px;
    flex: 0 0 21px;
    display: grid;
    place-items: center;
    border-radius: 999px;
    color: var(--cc-green-dark);
    background: var(--cc-green-soft);
    content: "✓";
    font-size: 0.74rem;
    font-weight: 950;
}

/* ============================================================
   BENEFICIOS
   ============================================================ */

.cc-benefit-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 22px;
}

.cc-benefit-card {
    padding: 30px;
    border: 1px solid var(--cc-border);
    border-radius: 22px;
    background: #ffffff;
}

.cc-benefit-number {
    width: 47px;
    height: 47px;
    margin-bottom: 22px;
    display: grid;
    place-items: center;
    border-radius: 14px;
    color: var(--cc-green-dark);
    background: var(--cc-green-soft);
    font-weight: 950;
}

.cc-benefit-card h3 {
    margin: 0 0 13px;
    color: var(--cc-ink);
    font-size: 1.3rem;
}

.cc-benefit-card p {
    margin: 0;
    color: var(--cc-muted);
    line-height: 1.7;
}

/* ============================================================
   MULTITENANT
   ============================================================ */

.cc-architecture {
    display: grid;
    grid-template-columns: 0.9fr 1.1fr;
    align-items: center;
    gap: 70px;
}

.cc-architecture-copy h2 {
    margin: 0;
    font-size: clamp(2.2rem, 4vw, 3.8rem);
    letter-spacing: -0.05em;
}

.cc-architecture-copy p {
    color: rgba(255, 255, 255, 0.72);
    line-height: 1.75;
}

.cc-tenant-board {
    padding: 25px;
    border: 1px solid rgba(255, 255, 255, 0.18);
    border-radius: 25px;
    background: rgba(255, 255, 255, 0.07);
}

.cc-tenant-row {
    margin-bottom: 14px;
    padding: 17px;
    display: grid;
    grid-template-columns: 45px 1fr auto;
    align-items: center;
    gap: 14px;
    border-radius: 15px;
    color: var(--cc-ink);
    background: #ffffff;
}

.cc-tenant-row:last-child {
    margin-bottom: 0;
}

.cc-tenant-icon {
    width: 43px;
    height: 43px;
    display: grid;
    place-items: center;
    border-radius: 13px;
    background: var(--cc-green-soft);
    font-size: 1.3rem;
}

.cc-tenant-name {
    font-weight: 900;
}

.cc-tenant-type {
    margin-top: 3px;
    color: var(--cc-muted);
    font-size: 0.76rem;
}

.cc-status {
    padding: 6px 10px;
    border-radius: 999px;
    color: var(--cc-green-dark);
    background: var(--cc-green-soft);
    font-size: 0.72rem;
    font-weight: 900;
}

/* ============================================================
   PRECIOS
   ============================================================ */

.cc-pricing-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    align-items: stretch;
    gap: 22px;
}

.cc-plan {
    position: relative;
    padding: 32px;
    border: 1px solid var(--cc-border);
    border-radius: 24px;
    background: #ffffff;
}

.cc-plan.featured {
    border: 2px solid var(--cc-green);
    box-shadow: var(--cc-shadow);
    transform: translateY(-10px);
}

.cc-plan-ribbon {
    position: absolute;
    top: 18px;
    right: 18px;
    padding: 6px 10px;
    border-radius: 999px;
    color: var(--cc-green-dark);
    background: var(--cc-lime);
    font-size: 0.72rem;
    font-weight: 950;
}

.cc-plan-name {
    color: var(--cc-green);
    font-weight: 950;
}

.cc-plan-price {
    margin: 18px 0 4px;
    color: var(--cc-ink);
    font-size: 3rem;
    font-weight: 950;
    letter-spacing: -0.05em;
}

.cc-plan-price small {
    color: var(--cc-muted);
    font-size: 0.83rem;
    font-weight: 700;
    letter-spacing: 0;
}

.cc-plan-description {
    min-height: 52px;
    color: var(--cc-muted);
    line-height: 1.6;
}

/* ============================================================
   CTA
   ============================================================ */

.cc-final-cta {
    padding: 55px;
    border-radius: 30px;
    color: #ffffff;
    background:
        radial-gradient(
            circle at top right,
            rgba(210, 243, 76, 0.25),
            transparent 36%
        ),
        var(--cc-green);
    text-align: center;
}

.cc-final-cta h2 {
    margin: 0;
    font-size: clamp(2.2rem, 4.4vw, 4rem);
    line-height: 1.05;
    letter-spacing: -0.05em;
}

.cc-final-cta p {
    max-width: 710px;
    margin: 18px auto 28px;
    color: rgba(255, 255, 255, 0.84);
    font-size: 1.08rem;
    line-height: 1.7;
}

/* ============================================================
   FOOTER
   ============================================================ */

.cc-footer {
    padding: 60px 0 30px;
    color: rgba(255, 255, 255, 0.75);
    background: #102a23;
}

.cc-footer-grid {
    display: grid;
    grid-template-columns: 1.5fr repeat(3, 1fr);
    gap: 35px;
}

.cc-footer h3,
.cc-footer h4 {
    margin-top: 0;
    color: #ffffff;
}

.cc-footer a {
    margin-bottom: 10px;
    display: block;
    color: rgba(255, 255, 255, 0.72);
    text-decoration: none;
}

.cc-footer a:hover {
    color: var(--cc-lime);
}

.cc-footer-bottom {
    margin-top: 38px;
    padding-top: 25px;
    border-top: 1px solid rgba(255, 255, 255, 0.13);
    display: flex;
    justify-content: space-between;
    gap: 20px;
    font-size: 0.85rem;
}

/* ============================================================
   RESPONSIVE
   ============================================================ */

@media (max-width: 1050px) {
    .cc-navigation {
        display: none;
    }

    .cc-menu-button {
        display: grid;
    }

    .cc-hero-grid {
        grid-template-columns: 1fr;
    }

    .cc-product-preview {
        max-width: 720px;
        margin-inline: auto;
    }

    .cc-trust-grid {
        grid-template-columns: repeat(2, 1fr);
    }

    .cc-trust-title {
        grid-column: 1 / -1;
        text-align: center;
    }

    .cc-benefit-grid,
    .cc-pricing-grid {
        grid-template-columns: 1fr;
    }

    .cc-plan.featured {
        transform: none;
    }

    .cc-architecture {
        grid-template-columns: 1fr;
    }

    .cc-footer-grid {
        grid-template-columns: repeat(2, 1fr);
    }

    .cc-navigation.mobile-active {
        position: fixed;
        inset: 117px 0 0;
        padding: 28px;
        display: flex;
        align-items: stretch;
        flex-direction: column;
        gap: 5px;
        background: #ffffff;
    }

    .cc-navigation.mobile-active > a {
        padding: 15px;
        border-bottom: 1px solid var(--cc-border);
        font-size: 1.05rem;
    }
}

@media (max-width: 700px) {
    .cc-container {
        width: min(100% - 24px, 1180px);
    }

    .cc-announcement {
        font-size: 0.78rem;
    }

    .cc-nav {
        min-height: 69px;
    }

    .cc-brand-text {
        display: none;
    }

    .cc-nav-actions .cc-button-light {
        display: none;
    }

    .cc-hero {
        padding: 58px 0 62px;
    }

    .cc-hero h1 {
        font-size: 3.15rem;
    }

    .cc-hero-description {
        font-size: 1.04rem;
    }

    .cc-hero-actions {
        display: grid;
    }

    .cc-hero-actions .cc-button {
        width: 100%;
    }

    .cc-browser {
        transform: none;
    }

    .cc-dashboard {
        grid-template-columns: 72px 1fr;
        min-height: 390px;
    }

    .cc-dashboard-sidebar {
        padding: 18px 7px;
    }

    .cc-dashboard-item {
        overflow: hidden;
        font-size: 0;
    }

    .cc-dashboard-item::first-letter {
        font-size: 1rem;
    }

    .cc-dashboard-main {
        padding: 17px;
    }

    .cc-dashboard-cards {
        grid-template-columns: 1fr;
    }

    .cc-dashboard-card:nth-child(3) {
        display: none;
    }

    .cc-floating-card {
        right: 8px;
        bottom: 10px;
        width: 160px;
    }

    .cc-trust-grid {
        grid-template-columns: 1fr;
    }

    .cc-section {
        padding: 70px 0;
    }

    .cc-carousel {
        grid-auto-columns: minmax(285px, 88vw);
    }

    .cc-solution-card {
        min-height: auto;
    }

    .cc-final-cta {
        padding: 38px 20px;
    }

    .cc-footer-grid {
        grid-template-columns: 1fr;
    }

    .cc-footer-bottom {
        flex-direction: column;
    }
}
CSS

echo
echo "3. Creando JavaScript del portal..."

cat > "$JS_FILE" <<'JAVASCRIPT'
"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const carousel = document.querySelector("[data-carousel]");
    const previousButton = document.querySelector("[data-carousel-prev]");
    const nextButton = document.querySelector("[data-carousel-next]");

    const menuButton = document.querySelector("[data-menu-button]");
    const navigation = document.querySelector("[data-navigation]");

    if (carousel && previousButton && nextButton) {
        const moveCarousel = (direction) => {
            const firstCard = carousel.querySelector(".cc-solution-card");

            if (!firstCard) {
                return;
            }

            const cardWidth = firstCard.getBoundingClientRect().width;
            const gap = 22;

            carousel.scrollBy({
                left: direction * (cardWidth + gap),
                behavior: "smooth"
            });
        };

        previousButton.addEventListener("click", () => {
            moveCarousel(-1);
        });

        nextButton.addEventListener("click", () => {
            moveCarousel(1);
        });
    }

    if (menuButton && navigation) {
        menuButton.addEventListener("click", () => {
            const opened =
                navigation.classList.toggle("mobile-active");

            document.body.classList.toggle("menu-open", opened);

            menuButton.setAttribute(
                "aria-expanded",
                opened ? "true" : "false"
            );
        });

        navigation.querySelectorAll("a").forEach((link) => {
            link.addEventListener("click", () => {
                navigation.classList.remove("mobile-active");
                document.body.classList.remove("menu-open");
                menuButton.setAttribute("aria-expanded", "false");
            });
        });
    }
});
JAVASCRIPT

echo
echo "4. Creando portal comercial premium..."

cat > "$LANDING_FILE" <<'HTML'
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1"
    >

    <meta
        name="description"
        content="Commerce Cloud: plataforma para administrar abarrotes, restaurantes y gimnasios desde cualquier dispositivo."
    >

    <title>Commerce Cloud | Haz crecer tu negocio</title>

    <link
        rel="stylesheet"
        th:href="@{/commerce/commerce-premium.css}"
        href="/commerce/commerce-premium.css"
    >
</head>

<body>

<div class="cc-announcement">
    Oferta para negocios fundadores:
    configuración inicial y capacitación incluidas.
    <a href="#precios">Conocer planes →</a>
</div>

<header class="cc-header">
    <div class="cc-container cc-nav">

        <a class="cc-brand" th:href="@{/}">
            <span class="cc-brand-mark">CC</span>
            <span class="cc-brand-text">Commerce Cloud</span>
        </a>

        <nav
            class="cc-navigation"
            data-navigation
            aria-label="Navegación principal"
        >
            <a href="#abarrotes">Abarrotes</a>
            <a href="#restaurantes">Restaurantes</a>
            <a href="#gimnasios">Gimnasios</a>
            <a href="#beneficios">Beneficios</a>
            <a href="#precios">Precios</a>
        </nav>

        <div class="cc-nav-actions">

            <a
                class="cc-button cc-button-light"
                th:href="@{/login}"
            >
                Iniciar sesión
            </a>

            <a
                class="cc-button cc-button-primary"
                th:href="${whatsappGeneral}"
                target="_blank"
                rel="noopener noreferrer"
            >
                Solicitar demo
            </a>

            <button
                class="cc-menu-button"
                type="button"
                aria-label="Abrir menú"
                aria-expanded="false"
                data-menu-button
            >
                ☰
            </button>

        </div>
    </div>
</header>

<main>

    <section class="cc-hero">

        <div class="cc-container cc-hero-grid">

            <div class="cc-hero-copy">

                <div class="cc-kicker">
                    <span class="cc-kicker-dot"></span>
                    Plataforma SaaS para pequeños negocios
                </div>

                <h1>
                    Tu negocio crece mejor cuando todo está
                    <span class="cc-highlight">bajo control.</span>
                </h1>

                <p class="cc-hero-description">
                    Administra ventas, inventario, clientes, membresías,
                    mesas, pedidos y reportes desde una sola plataforma
                    diseñada para abarrotes, restaurantes y gimnasios.
                </p>

                <div class="cc-hero-actions">

                    <a
                        class="cc-button cc-button-primary"
                        th:href="${whatsappGeneral}"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        Quiero una demostración
                    </a>

                    <a
                        class="cc-button cc-button-light"
                        href="#soluciones"
                    >
                        Explorar soluciones
                    </a>

                </div>

                <div class="cc-hero-note">
                    <span>Sin instalaciones complicadas</span>
                    <span>Funciona en celular y PC</span>
                    <span>Soporte directo</span>
                </div>

            </div>

            <div class="cc-product-preview">

                <div class="cc-preview-shadow"></div>

                <div class="cc-browser">

                    <div class="cc-browser-top">
                        <span class="cc-browser-dot red"></span>
                        <span class="cc-browser-dot yellow"></span>
                        <span class="cc-browser-dot green"></span>

                        <div class="cc-browser-address">
                            commercecloud.mx/dashboard
                        </div>
                    </div>

                    <div class="cc-dashboard">

                        <aside class="cc-dashboard-sidebar">
                            <div class="cc-dashboard-logo">CC</div>

                            <div class="cc-dashboard-item active">
                                📊 Dashboard
                            </div>

                            <div class="cc-dashboard-item">
                                🧾 Ventas
                            </div>

                            <div class="cc-dashboard-item">
                                📦 Inventario
                            </div>

                            <div class="cc-dashboard-item">
                                👥 Clientes
                            </div>

                            <div class="cc-dashboard-item">
                                ⚙️ Ajustes
                            </div>
                        </aside>

                        <div class="cc-dashboard-main">

                            <div class="cc-dashboard-title">
                                Resumen del negocio
                            </div>

                            <div class="cc-dashboard-subtitle">
                                Información actualizada en tiempo real
                            </div>

                            <div class="cc-dashboard-cards">

                                <article class="cc-dashboard-card">
                                    <div class="cc-dashboard-card-label">
                                        Ventas de hoy
                                    </div>

                                    <div class="cc-dashboard-card-value green">
                                        $12,480
                                    </div>
                                </article>

                                <article class="cc-dashboard-card">
                                    <div class="cc-dashboard-card-label">
                                        Operaciones
                                    </div>

                                    <div class="cc-dashboard-card-value">
                                        143
                                    </div>
                                </article>

                                <article class="cc-dashboard-card">
                                    <div class="cc-dashboard-card-label">
                                        Ticket promedio
                                    </div>

                                    <div class="cc-dashboard-card-value">
                                        $287
                                    </div>
                                </article>

                            </div>

                            <div class="cc-chart">

                                <div class="cc-dashboard-card-label">
                                    Ventas de los últimos siete días
                                </div>

                                <div class="cc-chart-bars">
                                    <span class="cc-chart-bar" style="height:35%"></span>
                                    <span class="cc-chart-bar" style="height:52%"></span>
                                    <span class="cc-chart-bar" style="height:45%"></span>
                                    <span class="cc-chart-bar" style="height:71%"></span>
                                    <span class="cc-chart-bar" style="height:64%"></span>
                                    <span class="cc-chart-bar" style="height:88%"></span>
                                    <span class="cc-chart-bar" style="height:77%"></span>
                                </div>

                            </div>
                        </div>

                    </div>
                </div>

                <div class="cc-floating-card">
                    <div class="cc-floating-label">
                        Crecimiento mensual
                    </div>

                    <div class="cc-floating-value">
                        +18.4%
                    </div>
                </div>

            </div>
        </div>
    </section>

    <section class="cc-trust">
        <div class="cc-container cc-trust-grid">

            <div class="cc-trust-title">
                Una sola plataforma para administrar:
            </div>

            <div class="cc-trust-item">🛒 Abarrotes</div>
            <div class="cc-trust-item">🍽️ Restaurantes</div>
            <div class="cc-trust-item">🏋️ Gimnasios</div>
            <div class="cc-trust-item">☕ Próximos giros</div>

        </div>
    </section>

    <section id="soluciones" class="cc-section cc-section-soft">

        <div class="cc-container">

            <div class="cc-section-heading">

                <span class="cc-section-eyebrow">
                    Soluciones especializadas
                </span>

                <h2>
                    Elige el sistema adecuado para tu negocio
                </h2>

                <p>
                    No instalamos un sistema genérico. Cada vertical
                    incorpora los procesos, indicadores y pantallas
                    que realmente necesita ese tipo de negocio.
                </p>

            </div>

            <div class="cc-carousel-wrapper">

                <div class="cc-carousel-controls">

                    <button
                        class="cc-carousel-button"
                        type="button"
                        aria-label="Anterior"
                        data-carousel-prev
                    >
                        ←
                    </button>

                    <button
                        class="cc-carousel-button"
                        type="button"
                        aria-label="Siguiente"
                        data-carousel-next
                    >
                        →
                    </button>

                </div>

                <div class="cc-carousel" data-carousel>

                    <article
                        id="abarrotes"
                        class="cc-solution-card"
                    >

                        <div class="cc-solution-cover store">
                            <div class="cc-solution-icon">🛒</div>
                        </div>

                        <div class="cc-solution-content">

                            <span class="cc-solution-tag">
                                Tiendas y comercios
                            </span>

                            <h3>Abarrotes Cloud</h3>

                            <p>
                                Controla productos, inventario, caja,
                                ventas y reportes desde cualquier dispositivo.
                            </p>

                            <ul class="cc-feature-list">
                                <li>Punto de venta rápido</li>
                                <li>Inventario y existencias</li>
                                <li>Corte de caja diario</li>
                                <li>Carga masiva con Excel</li>
                                <li>Dashboard administrativo</li>
                                <li>Asistente inteligente</li>
                            </ul>

                            <a
                                class="cc-button cc-button-primary cc-button-block"
                                th:href="${whatsappAbarrotes}"
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                Probar Abarrotes Cloud
                            </a>

                        </div>
                    </article>

                    <article
                        id="restaurantes"
                        class="cc-solution-card"
                    >

                        <div class="cc-solution-cover restaurant">
                            <div class="cc-solution-icon">🍽️</div>
                        </div>

                        <div class="cc-solution-content">

                            <span class="cc-solution-tag">
                                Restaurantes, fondas y cafeterías
                            </span>

                            <h3>Restaurante Cloud</h3>

                            <p>
                                Tus meseros toman pedidos desde el celular
                                y el administrador controla ventas y mesas.
                            </p>

                            <ul class="cc-feature-list">
                                <li>Pedidos desde celular</li>
                                <li>Mapa y control de mesas</li>
                                <li>Catálogo de platillos</li>
                                <li>Pedidos y cuentas abiertas</li>
                                <li>Ventas por mesero</li>
                                <li>Corte del día</li>
                            </ul>

                            <a
                                class="cc-button cc-button-primary cc-button-block"
                                th:href="${whatsappRestaurante}"
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                Probar Restaurante Cloud
                            </a>

                        </div>
                    </article>

                    <article
                        id="gimnasios"
                        class="cc-solution-card"
                    >

                        <div class="cc-solution-cover gym">
                            <div class="cc-solution-icon">🏋️</div>
                        </div>

                        <div class="cc-solution-content">

                            <span class="cc-solution-tag">
                                Gimnasios y centros deportivos
                            </span>

                            <h3>Gym Cloud</h3>

                            <p>
                                Administra clientes, planes, membresías,
                                pagos y productos en un solo lugar.
                            </p>

                            <ul class="cc-feature-list">
                                <li>Registro de clientes</li>
                                <li>Planes y membresías</li>
                                <li>Seguimiento de pagos</li>
                                <li>Venta de productos</li>
                                <li>Importación desde Excel</li>
                                <li>Indicadores de negocio</li>
                            </ul>

                            <a
                                class="cc-button cc-button-primary cc-button-block"
                                th:href="${whatsappGym}"
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                Probar Gym Cloud
                            </a>

                        </div>
                    </article>

                </div>
            </div>
        </div>
    </section>

    <section id="beneficios" class="cc-section">

        <div class="cc-container">

            <div class="cc-section-heading center">

                <span class="cc-section-eyebrow">
                    Menos complicaciones
                </span>

                <h2>
                    Todo lo necesario para operar y crecer
                </h2>

                <p>
                    Commerce Cloud reúne las herramientas que normalmente
                    se encuentran separadas en varios sistemas.
                </p>

            </div>

            <div class="cc-benefit-grid">

                <article class="cc-benefit-card">
                    <div class="cc-benefit-number">01</div>

                    <h3>Control desde cualquier lugar</h3>

                    <p>
                        Consulta ventas, inventario, clientes y operaciones
                        desde celular, tableta o computadora.
                    </p>
                </article>

                <article class="cc-benefit-card">
                    <div class="cc-benefit-number">02</div>

                    <h3>Datos separados por cliente</h3>

                    <p>
                        La arquitectura multi-tenant mantiene aislada
                        la información de cada negocio.
                    </p>
                </article>

                <article class="cc-benefit-card">
                    <div class="cc-benefit-number">03</div>

                    <h3>Sin instalaciones locales</h3>

                    <p>
                        Las actualizaciones y nuevas funciones se publican
                        sin reinstalar software en cada equipo.
                    </p>
                </article>

                <article class="cc-benefit-card">
                    <div class="cc-benefit-number">04</div>

                    <h3>Capacitación inicial</h3>

                    <p>
                        Configuramos el negocio, cargamos información
                        inicial y enseñamos al personal a utilizarlo.
                    </p>
                </article>

                <article class="cc-benefit-card">
                    <div class="cc-benefit-number">05</div>

                    <h3>Reportes claros</h3>

                    <p>
                        Convierte las operaciones diarias en información
                        útil para tomar mejores decisiones.
                    </p>
                </article>

                <article class="cc-benefit-card">
                    <div class="cc-benefit-number">06</div>

                    <h3>Una plataforma que evoluciona</h3>

                    <p>
                        Agrega sucursales, usuarios y nuevas herramientas
                        conforme crezca el negocio.
                    </p>
                </article>

            </div>
        </div>
    </section>

    <section class="cc-section cc-section-dark">

        <div class="cc-container cc-architecture">

            <div class="cc-architecture-copy">

                <span class="cc-section-eyebrow">
                    Arquitectura Commerce Cloud
                </span>

                <h2>
                    Treinta negocios. Una plataforma. Datos independientes.
                </h2>

                <p>
                    Diez abarrotes, diez gimnasios y diez restaurantes
                    pueden utilizar la misma aplicación, mientras cada
                    empresa ve exclusivamente su información.
                </p>

                <p>
                    Al iniciar sesión, Commerce Cloud identifica el tenant,
                    el tipo de negocio y el rol del usuario para abrir
                    automáticamente el módulo correcto.
                </p>

                <a
                    class="cc-button cc-button-white"
                    th:href="${whatsappGeneral}"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Conocer la plataforma
                </a>

            </div>

            <div class="cc-tenant-board">

                <div class="cc-tenant-row">
                    <div class="cc-tenant-icon">🛒</div>

                    <div>
                        <div class="cc-tenant-name">
                            Abarrotes Don Pepe
                        </div>

                        <div class="cc-tenant-type">
                            Tenant 001 · ABARROTES
                        </div>
                    </div>

                    <span class="cc-status">Activo</span>
                </div>

                <div class="cc-tenant-row">
                    <div class="cc-tenant-icon">🍽️</div>

                    <div>
                        <div class="cc-tenant-name">
                            Restaurante Los Compadres
                        </div>

                        <div class="cc-tenant-type">
                            Tenant 011 · RESTAURANTE
                        </div>
                    </div>

                    <span class="cc-status">Activo</span>
                </div>

                <div class="cc-tenant-row">
                    <div class="cc-tenant-icon">🏋️</div>

                    <div>
                        <div class="cc-tenant-name">
                            Titan Gym
                        </div>

                        <div class="cc-tenant-type">
                            Tenant 021 · GYM
                        </div>
                    </div>

                    <span class="cc-status">Activo</span>
                </div>

            </div>
        </div>
    </section>

    <section id="precios" class="cc-section cc-section-soft">

        <div class="cc-container">

            <div class="cc-section-heading center">

                <span class="cc-section-eyebrow">
                    Precios de lanzamiento
                </span>

                <h2>
                    Comienza con el plan que necesita tu negocio
                </h2>

                <p>
                    Sin contratos largos. Los primeros clientes recibirán
                    configuración y acompañamiento directo.
                </p>

            </div>

            <div class="cc-pricing-grid">

                <article class="cc-plan">

                    <div class="cc-plan-name">
                        Emprendedor
                    </div>

                    <div class="cc-plan-price">
                        $299
                        <small>MXN / mes</small>
                    </div>

                    <p class="cc-plan-description">
                        Para pequeños negocios que comienzan
                        su transformación digital.
                    </p>

                    <ul class="cc-feature-list">
                        <li>Una sucursal</li>
                        <li>Hasta dos usuarios</li>
                        <li>Operación principal</li>
                        <li>Reportes básicos</li>
                        <li>Soporte de inicio</li>
                    </ul>

                    <a
                        class="cc-button cc-button-light cc-button-block"
                        th:href="${whatsappGeneral}"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        Solicitar información
                    </a>

                </article>

                <article class="cc-plan featured">

                    <span class="cc-plan-ribbon">
                        Más recomendado
                    </span>

                    <div class="cc-plan-name">
                        Profesional
                    </div>

                    <div class="cc-plan-price">
                        $599
                        <small>MXN / mes</small>
                    </div>

                    <p class="cc-plan-description">
                        Para negocios en operación que necesitan
                        control y reportes completos.
                    </p>

                    <ul class="cc-feature-list">
                        <li>Una sucursal</li>
                        <li>Hasta diez usuarios</li>
                        <li>Dashboard completo</li>
                        <li>Exportación a Excel</li>
                        <li>Asistente inteligente</li>
                        <li>Soporte por WhatsApp</li>
                    </ul>

                    <a
                        class="cc-button cc-button-primary cc-button-block"
                        th:href="${whatsappGeneral}"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        Solicitar demostración
                    </a>

                </article>

                <article class="cc-plan">

                    <div class="cc-plan-name">
                        Business
                    </div>

                    <div class="cc-plan-price">
                        $999
                        <small>MXN / mes</small>
                    </div>

                    <p class="cc-plan-description">
                        Para empresas que manejan más usuarios,
                        cajas o sucursales.
                    </p>

                    <ul class="cc-feature-list">
                        <li>Varias sucursales</li>
                        <li>Usuarios adicionales</li>
                        <li>Panel ejecutivo</li>
                        <li>Respaldos ampliados</li>
                        <li>Soporte prioritario</li>
                    </ul>

                    <a
                        class="cc-button cc-button-light cc-button-block"
                        th:href="${whatsappGeneral}"
                        target="_blank"
                        rel="noopener noreferrer"
                    >
                        Hablar con un asesor
                    </a>

                </article>

            </div>
        </div>
    </section>

    <section class="cc-section">

        <div class="cc-container">

            <div class="cc-final-cta">

                <h2>
                    Tu primer mes de mejor control comienza hoy
                </h2>

                <p>
                    Agenda una demostración y revisamos cómo adaptar
                    Commerce Cloud a la operación real de tu negocio.
                </p>

                <a
                    class="cc-button cc-button-white"
                    th:href="${whatsappGeneral}"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Solicitar demostración por WhatsApp
                </a>

            </div>
        </div>
    </section>

</main>

<footer class="cc-footer">

    <div class="cc-container">

        <div class="cc-footer-grid">

            <div>
                <h3>Commerce Cloud</h3>

                <p>
                    Tecnología práctica para administrar y hacer crecer
                    pequeños negocios en México.
                </p>
            </div>

            <div>
                <h4>Soluciones</h4>
                <a href="#abarrotes">Abarrotes</a>
                <a href="#restaurantes">Restaurantes</a>
                <a href="#gimnasios">Gimnasios</a>
            </div>

            <div>
                <h4>Plataforma</h4>
                <a href="#beneficios">Beneficios</a>
                <a href="#precios">Precios</a>
                <a th:href="@{/login}">Acceso a clientes</a>
            </div>

            <div>
                <h4>Contacto</h4>

                <a
                    th:href="${whatsappGeneral}"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    WhatsApp
                </a>
            </div>

        </div>

        <div class="cc-footer-bottom">
            <span>© 2026 Commerce Cloud.</span>
            <span>Abarrotes · Restaurantes · Gimnasios</span>
        </div>

    </div>
</footer>

<script
    th:src="@{/commerce/commerce-premium.js}"
    src="/commerce/commerce-premium.js"
></script>

</body>
</html>
HTML

echo
echo "5. Actualizando mensaje del login..."

if [[ ! -f "$LOGIN_FILE" ]]; then
    echo "ERROR: no existe:"
    echo "$LOGIN_FILE"
    exit 1
fi

python3 - "$LOGIN_FILE" <<'PYTHON'
from pathlib import Path
import sys

login_file = Path(sys.argv[1])
text = login_file.read_text(encoding="utf-8")

replacements = {
    "🏪 Abarrotes · 🏋️ Gimnasios · Más verticales próximamente":
        "🏪 Abarrotes · 🍽️ Restaurantes · 🏋️ Gimnasios",

    "Abarrotes · Gimnasios · Más verticales próximamente":
        "Abarrotes · Restaurantes · Gimnasios",

    "Abarrotes • Gimnasios • Más verticales próximamente":
        "Abarrotes • Restaurantes • Gimnasios",

    "Abarrotes &middot; Gimnasios &middot; Más verticales próximamente":
        "Abarrotes &middot; Restaurantes &middot; Gimnasios"
}

changed = False

for old, new in replacements.items():
    if old in text:
        text = text.replace(old, new)
        changed = True

landing_link = """
<a
    href="/"
    style="
        margin-top: 18px;
        display: block;
        color: #69a7ff;
        text-align: center;
        text-decoration: none;
        font-size: 0.88rem;
        font-weight: 700;
    "
>
    ← Conoce Abarrotes, Restaurantes y Gym Cloud
</a>
"""

if "Conoce Abarrotes, Restaurantes y Gym Cloud" not in text:
    body_position = text.lower().rfind("</body>")

    if body_position != -1:
        text = (
            text[:body_position]
            + landing_link
            + "\n"
            + text[body_position:]
        )
        changed = True

if not changed:
    print(
        "AVISO: no se encontró el texto anterior, "
        "pero el login ya podría estar actualizado."
    )
else:
    login_file.write_text(text, encoding="utf-8")
    print("login.html actualizado correctamente.")
PYTHON

echo
echo "6. Verificando archivos..."

test -s "$LANDING_FILE"
test -s "$CSS_FILE"
test -s "$JS_FILE"
test -s "$LOGIN_FILE"

grep -q "Restaurante Cloud" "$LANDING_FILE"
grep -q "Gym Cloud" "$LANDING_FILE"
grep -q "Abarrotes Cloud" "$LANDING_FILE"

echo
echo "7. Validando diferencias..."

git diff --check

echo
echo "8. Compilando..."

mvn clean compile

echo
echo "============================================================"
echo " PORTAL PREMIUM V2 INSTALADO"
echo "============================================================"
echo
echo "Abre:"
echo "  http://localhost:8080/"
echo
echo "Login:"
echo "  http://localhost:8080/login"
echo
echo "Recarga completa del navegador:"
echo "  Ctrl + Shift + R"
echo
echo "Estado Git:"
git status --short
