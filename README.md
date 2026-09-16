# ⏰ Reloj Despertador Digital (Android)

Una aplicación nativa para Android desarrollada en **Kotlin** y **Jetpack Compose** que transforma tu móvil en un reloj despertador digital para mesilla de noche con orientación apaisada fija, soporte NFC, datos meteorológicos en tiempo real y sistema de actualizaciones OTA mediante **GitHub Releases**.

---

## ✨ Características Principales

- 🕒 **Hora Matemáticamente Centrada**: Pantalla digital con balanceo simétrico que mantiene la hora `HH:MM` en el centro exacto de la pantalla.
- 🎨 **8 Paletas de Color LED**: Verde Neón, Rojo Noche (amigable con la melatonina), Azul Cian, Ámbar Cálido, Blanco Puro, Púrpura, Lima y Rosa.
- 🌙 **Atenuación Nocturna Ultra-Baja**: Control deslizante de brillo mínimo (5% - 50%) con filtro OLED negro para no deslumbrar en la oscuridad.
- ☀️ **Tiempo Meteorológico en Directo**: Integración con la API abierta de Open-Meteo (temperatura, estado del cielo y humedad).
- ⏰ **Gestor de Alarmas**: Programación exacta con `AlarmManager`, repetición por días y pantalla de alarma sonando por encima del bloqueo.
- 📡 **Activación por NFC**: Basta con apoyar el teléfono sobre una etiqueta NFC en la mesilla para que el reloj se abra automáticamente.
- 🛡️ **Protección contra Quemado OLED**: Desplazamiento periódico imperceptible de píxeles para evitar desgaste en pantallas AMOLED/OLED.
- 🚀 **Actualizaciones OTA vía GitHub Releases**: La app consulta automáticamente este repositorio en GitHub para alertar si hay un nuevo APK disponible y permitir su descarga con un solo toque.

---

## 📲 Descarga e Instalación

Puedes descargar el último archivo `.apk` directamente desde la sección de [Releases](../../releases).

---

## 🛠️ Tecnologías Utilizadas

- **Lenguaje**: Kotlin 2.0+
- **Interfaz**: Jetpack Compose con Material 3
- **Arquitectura**: Clean StateFlow / Coroutines
- **APIs**: Android AlarmManager, Open-Meteo REST API, GitHub Releases REST API
- **Build System**: Gradle con Kotlin DSL (AGP 8.x)

---

## 👤 Autor

Desarrollado por **David Rodelgo**.
