package com.abarrote.abarroteapi.config;

import com.abarrote.abarroteapi.entity.Categoria;
import com.abarrote.abarroteapi.repository.CategoriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Order(20)
public class CategoriasPredeterminadasInitializer
        implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;

    public CategoriasPredeterminadasInitializer(
            CategoriaRepository categoriaRepository) {

        this.categoriaRepository =
                categoriaRepository;
    }

    @Override
    @Transactional
    public void run(
            String... args) {

        Map<String, String> categorias =
                new LinkedHashMap<>();

        categorias.put(
                "Abarrotes",
                "Arroz, frijol, azúcar, sal, aceite y productos básicos"
        );

        categorias.put(
                "Bebidas",
                "Refrescos, agua, jugos y bebidas preparadas"
        );

        categorias.put(
                "Lácteos",
                "Leche, yogurt, queso, crema y derivados"
        );

        categorias.put(
                "Enlatados",
                "Atún, sardinas, vegetales y alimentos en conserva"
        );

        categorias.put(
                "Sopas",
                "Sopas instantáneas, pastas y fideos"
        );

        categorias.put(
                "Condimentos",
                "Especias, consomés, salsas y sazonadores"
        );

        categorias.put(
                "Café y té",
                "Café soluble, café molido, té y chocolate"
        );

        categorias.put(
                "Galletas",
                "Galletas dulces, saladas y rellenas"
        );

        categorias.put(
                "Botanas",
                "Papas, frituras, cacahuates y botanas"
        );

        categorias.put(
                "Panificación",
                "Pan de caja, tortillas, tostadas y pan dulce"
        );

        categorias.put(
                "Aderezos",
                "Mayonesa, catsup, mostaza y aderezos"
        );

        categorias.put(
                "Dulces y chocolates",
                "Dulces, chocolates, caramelos y gomitas"
        );

        categorias.put(
                "Higiene",
                "Productos de higiene y cuidado personal"
        );

        categorias.put(
                "Limpieza",
                "Detergentes, cloro, jabón y limpieza del hogar"
        );

        categorias.put(
                "Desechables",
                "Vasos, platos, servilletas y bolsas"
        );

        categorias.put(
                "Mascotas",
                "Alimento y artículos básicos para mascotas"
        );

        categorias.put(
                "Congelados",
                "Helados y alimentos congelados"
        );

        categorias.put(
                "Frutas y verduras",
                "Frutas, verduras y productos frescos"
        );

        categorias.put(
                "Carnes y embutidos",
                "Jamón, salchicha, tocino y carnes frías"
        );

        categorias.put(
                "Farmacia",
                "Artículos básicos de farmacia y primeros auxilios"
        );

        for (
                Map.Entry<String, String> categoria :
                categorias.entrySet()
        ) {

            boolean existe =
                    categoriaRepository
                            .existsByNombreIgnoreCase(
                                    categoria.getKey()
                            );

            if (!existe) {

                Categoria nueva =
                        new Categoria();

                nueva.setNombre(
                        categoria.getKey()
                );

                nueva.setDescripcion(
                        categoria.getValue()
                );

                categoriaRepository.save(
                        nueva
                );
            }
        }
    }
}
