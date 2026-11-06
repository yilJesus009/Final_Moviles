# Eventia - Aplicación Móvil para Descubrir Eventos y Lugares

**Asignatura:** Programación de Aplicaciones Móviles 1 (2026-2)  
**Modalidad:** Individual  
**Versión Base:** v0.1-init  

---

## 1. Descripción del Proyecto
**Eventia** es una aplicación móvil nativa desarrollada en **Android** con **Kotlin** y **Jetpack Compose** que permite:
* Explorar actividades, eventos y lugares de la ciudad.
* Consultar detalles completos y ubicarlos en un mapa interactivo.
* Guardar eventos en favoritos y consultar el catálogo en modo sin conexión (**Offline-First** con Room).
* Inscribirse a eventos con cupos limitados y gestionar la cuenta de usuario con sesión persistente (DataStore).

---

## 2. Tecnologías y Versiones Utilizadas
* **Lenguaje:** Kotlin 2.2.10
* **Interfaz de Usuario:** Jetpack Compose + Material 3 (Compose BOM 2026.02.01)
* **Persistencia Local:** Room 3.0.3 (KSP 2.3.4)
* **Gestión de Sesión / Preferencias:** AndroidX DataStore Preferences 1.1.3
* **Red / Consumo API REST:** Retrofit 3.0.0 + Gson Converter
* **Carga de Imágenes:** Coil Compose 2.7.0
* **Navegación:** Navigation Compose 2.8.9
* **Inyección de Dependencias / Arquitectura:** MVVM + Repository Pattern + Kotlin Coroutines & StateFlow
* **Compatibilidad Android:** `minSdk = 33`, `targetSdk = 37`, `compileSdk = 37`
* **JDK:** Java 11 / 17 / 21

---

## 3. Arquitectura del Proyecto
La aplicación implementa el patrón **MVVM (Model - View - ViewModel)** combinado con el **Patrón Repositorio**:
* **UI Layer:** Pantallas declarativas en Jetpack Compose que reaccionan a flujos inmutables `StateFlow<UiState>`.
* **ViewModel:** Gestión del estado de presentación, corrutinas y manejo de eventos de usuario con flujo unidireccional de datos (UDF).
* **Repository:** Orquesta las fuentes de datos (Red vs. Caché local en Room) garantizando que la UI sea agnóstica a la procedencia de los datos.
* **Data Layer:**
  * **API (Retrofit):** Comunicación asíncrona con el backend REST.
  * **Local DB (Room):** Entidades y DAOs para favoritos y almacenamiento en caché del catálogo.
  * **Preferences (DataStore):** Almacenamiento seguro del token de autenticación.

---

## 4. Estructura de Módulos (Requisitos)
- [ ] **Módulo A (Catálogo):** Lista de eventos, detalle, búsqueda con debounce (300 ms), filtros por categoría y actualización con soporte offline.
- [ ] **Módulo B (Lugares):** Lista y detalle de venues, eventos vinculados y navegación bidireccional.
- [ ] **Módulo C (Favoritos y Caché):** Persistencia en Room, opción de deshacer eliminación y aviso de datos sin conexión.
- [ ] **Módulo D (Cuenta e Inscripciones):** Autenticación con Bearer token, validaciones de formulario, inscripciones con manejo de errores 401 y 409.
- [ ] **Módulo E (Mapa y Ubicación):** Marcadores en mapa para eventos con coordenadas válidas, bottom sheet con resumen e integración con apps de mapas externas.

---

## 5. Instrucciones de Compilación y Ejecución
1. Clonar el repositorio:
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   ```
2. Abrir la carpeta en **Android Studio**.
3. Asegurarse de tener configurado el archivo `local.properties` con la ruta de tu SDK de Android:
   ```properties
   sdk.dir=C\:\\Users\\<TuUsuario>\\AppData\\Local\\Android\\Sdk
   ```
4. Sincronizar el proyecto con Gradle (**Sync Project with Gradle Files**).
5. Ejecutar en un emulador o dispositivo físico con **Android 13 (API 33)** o superior.
