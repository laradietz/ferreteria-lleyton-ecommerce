package com.ecommerce.api.config;

import com.ecommerce.api.entity.*;
import com.ecommerce.api.repository.CategoryRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Carga datos iniciales para poder probar la tienda de Ferretería Lleyton
 * apenas se levanta el proyecto: un usuario admin y un catálogo de ejemplo.
 * Se puede desactivar con app.seed.enabled=false.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.admin-username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin-password:admin123}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) return;

        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = User.builder()
                    .username(adminUsername)
                    .email("admin@ferreterialleyton.com")
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .firstName("Administrador")
                    .lastName("Lleyton")
                    .active(true)
                    .role(RoleName.ADMIN)
                    .build();
            userRepository.save(admin);
        }

        if (categoryRepository.count() > 0) return; // catálogo ya cargado

        Category herramientasManuales = category("Herramientas Manuales", "Martillos, destornilladores, llaves, alicates y más.");
        Category herramientasElectricas = category("Herramientas Eléctricas", "Taladros, amoladoras, sierras y equipos a batería.");
        Category tornilleria = category("Tornillería y Fijaciones", "Tornillos, tarugos, bulones y anclajes.");
        Category pinturas = category("Pinturas y Accesorios", "Pinturas, esmaltes, rodillos y pinceles.");
        Category electricidad = category("Electricidad", "Cables, llaves térmicas, tomas y accesorios eléctricos.");
        Category plomeria = category("Plomería", "Caños, conexiones, grifería y accesorios sanitarios.");
        Category seguridad = category("Seguridad e Higiene", "Guantes, anteojos, cascos y elementos de protección.");

        product("HM-0001", "Martillo carpintero 20 oz", "Mango de fibra de vidrio, cabeza forjada.", "8500", 40, herramientasManuales);
        product("HM-0002", "Juego de destornilladores x6", "Puntas planas y Phillips, mango ergonómico.", "12300", 25, herramientasManuales);
        product("HM-0003", "Alicate universal 8\"", "Acero al cromo vanadio.", "6400", 30, herramientasManuales);
        product("HM-0004", "Llave inglesa ajustable 10\"", "Apertura de mordaza hasta 30mm.", "9200", 20, herramientasManuales);

        product("HE-0001", "Taladro percutor 750W", "Incluye maletín y set de mechas.", "58900", 15, herramientasElectricas);
        product("HE-0002", "Amoladora angular 4 1/2\"", "820W, incluye disco de corte.", "42500", 18, herramientasElectricas);
        product("HE-0003", "Atornillador a batería 12V", "Con cargador y 2 baterías.", "39900", 12, herramientasElectricas);
        product("HE-0004", "Sierra caladora 650W", "Velocidad variable, corte en madera y metal.", "47800", 10, herramientasElectricas);

        product("TF-0001", "Tornillos autorroscantes 8x1\" (caja x100)", "Punta broca, para madera y metal.", "3200", 80, tornilleria);
        product("TF-0002", "Tarugos plásticos 8mm (bolsa x50)", "Para fijaciones en mampostería.", "1800", 100, tornilleria);
        product("TF-0003", "Bulones hexagonales 1/4\" x2\" (caja x25)", "Acero zincado.", "2900", 60, tornilleria);

        product("PA-0001", "Pintura látex interior blanco 4L", "Alto poder cubritivo, secado rápido.", "15600", 22, pinturas);
        product("PA-0002", "Esmalte sintético brillante negro 1L", "Para metal y madera.", "8900", 30, pinturas);
        product("PA-0003", "Rodillo antigota 23cm", "Con mango y bandeja incluida.", "3400", 40, pinturas);

        product("EL-0001", "Cable unipolar 2.5mm (rollo x100m)", "Normalizado IRAM.", "45200", 8, electricidad);
        product("EL-0002", "Llave térmica bipolar 25A", "Para tablero eléctrico.", "6100", 25, electricidad);
        product("EL-0003", "Toma corriente doble con tierra", "Línea blanca, embutir.", "2100", 50, electricidad);

        product("PL-0001", "Caño PVC 110mm (barra x3m)", "Para desagüe cloacal.", "9800", 15, plomeria);
        product("PL-0002", "Grifería monocomando cocina", "Terminación cromada.", "23400", 12, plomeria);
        product("PL-0003", "Cinta de teflón 1/2\"", "Para uniones roscadas.", "450", 200, plomeria);

        product("SH-0001", "Guantes de nitrilo (caja x100)", "Talle M, uso general.", "7200", 30, seguridad);
        product("SH-0002", "Anteojos de seguridad transparentes", "Protección UV, antirrayaduras.", "1900", 45, seguridad);
        product("SH-0003", "Casco de seguridad blanco", "Ajuste con matraca.", "5600", 20, seguridad);
    }

    private Category category(String name, String description) {
        return categoryRepository.save(Category.builder()
                .name(name).description(description).active(true).build());
    }

    private void product(String sku, String name, String description, String price,
                          int stock, Category category) {
        productRepository.save(Product.builder()
                .sku(sku).name(name).description(description)
                .price(new BigDecimal(price)).stock(stock)
                .category(category).active(true).build());
    }
}
