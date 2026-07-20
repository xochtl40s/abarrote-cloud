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
