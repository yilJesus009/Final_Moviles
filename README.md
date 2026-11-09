# Eventia - Aplicación Móvil para Descubrir Eventos y Lugares

**Asignatura:** Programación de Aplicaciones Móviles 1 (2026-2)  
**Modalidad:** Individual  
**Repositorio:** https://github.com/yilJesus009/Final_Moviles.git  

---

## 1. Descripción del Proyecto
**Eventia** es una aplicación móvil nativa desarrollada en **Android** con **Kotlin** y **Jetpack Compose** que permite:
* Explorar actividades, eventos y lugares de la ciudad.
* Consultar detalles completos y ubicarlos en un mapa interactivo.
* Guardar eventos en favoritos y consultar el catálogo en modo sin conexión (**Offline-First** con Room).
* Inscribirse a eventos con cupos limitados y gestionar la cuenta de usuario con sesión persistente (DataStore).

---

## 2. Tecnologías y Versiones Utilizadas
* **IDE:** Android Studio (2025.3 / 2026.1+)
* **Lenguaje:** Kotlin 2.2.10
* **Android Gradle Plugin (AGP):** 9.3.3
* **JDK:** Java 11
* **Compatibilidad Android SDK:** `minSdk = 33` (Android 13), `targetSdk = 37`, `compileSdk = 37`
* **Interfaz de Usuario:** Jetpack Compose + Material 3 (Compose BOM 2026.02.01)
* **Navegación:** Navigation Compose 2.8.9
* **Inyección de Dependencias:** Hilt 2.60.1 (`hilt-lifecycle-viewmodel-compose` 1.4.0)
* **Persistencia Local:** Room 3.0.3 (KSP 2.3.4)
* **Gestión de Sesión / Preferencias:** AndroidX DataStore Preferences 1.1.3
* **Red / Consumo API REST:** Retrofit 3.0.0 + Gson Converter
* **Carga de Imágenes:** Coil Compose 2.7.0
* **Mapas:** osmdroid (OpenStreetMap) 6.1.20 — no requiere clave de API externa

---

## 3. Arquitectura del Proyecto
La aplicación implementa el patrón **MVVM (Model - View - ViewModel)** combinado con el **Patrón Repositorio** e inyección de dependencias con **Hilt**:

```text
[ UI (Jetpack Compose) ]  <-- StateFlow<UiState> --  [ ViewModel (@HiltViewModel) ]
          |                                                       |
   Eventos de usuario                                   Modelos de Dominio
          |                                                       |
          +----------------------------------------> [ Repository (Interfaz) ]
                                                         /        |        \
                                                        /         |         \
                                           [ Retrofit API ]  [ Room DB ]  [ DataStore ]
```

* **UI Layer (`ui/screens`, `ui/components`):** Pantallas declarativas en Jetpack Compose que observan flujos inmutables `StateFlow<UiState>` con `collectAsStateWithLifecycle()`.
* **ViewModel (`@HiltViewModel`):** Gestión del estado de presentación (carga, contenido, lista vacía y error), corrutinas en `viewModelScope` y flujo unidireccional de datos (UDF).
* **Domain Models (`models/`):** Modelos limpios (`Evento`, `Lugar`, `Categoria`, `Usuario`). Los DTOs de red nunca llegan a la UI.
* **Repositories (`repositories/`):** Interfaces (`EventoRepository`, `LugarRepository`, `FavoritoRepository`, `AuthRepository`, `InscripcionRepository`) e implementaciones inyectadas por Hilt que orquestan la red y el almacenamiento local.
* **Data Layer:**
  * **API (`api/`):** `EventiaAPI` (Retrofit) y DTOs con `@SerializedName`.
  * **Local DB (`db/`):** `AppDatabase` (Room) con `FavoritoDao` y `EventoCacheDao`.
  * **Preferences (`datastore/`):** `SesionDataStore` para el token Bearer y datos del usuario.

---

## 4. Estructura de Módulos (Requisitos)
- [x] **Módulo A (Catálogo):** Lista de eventos próximos, detalle, búsqueda con debounce (300 ms), filtros por categoría y actualización con soporte offline (RF-01, RF-02, RF-03, RF-06).
- [x] **Módulo B (Lugares):** Lista y detalle de lugares, próximos eventos vinculados, navegación bidireccional Evento ↔ Lugar y reutilización de `TarjetaEvento` (RF-13).
- [x] **Módulo C (Favoritos y Caché):** Persistencia en Room, opción "Deshacer" en Snackbar y caché del catálogo con aviso sin conexión (RF-04, RF-05, RF-14).
- [x] **Módulo D (Cuenta e Inscripciones):** Registro, login y cierre de sesión con DataStore, validaciones de formulario, inscripción/cancelación con manejo de errores 401/404/409 y "Mis inscripciones" separadas en próximas y pasadas (RF-11, RF-12).
- [x] **Módulo E (Mapa y Ubicación):** Mapa con marcadores para eventos con coordenadas válidas, filtro por categoría, hoja inferior (`ModalBottomSheet`) con resumen e integración con apps externas de mapas (RF-07, RF-08, RF-09).

---

## 5. Configuración de la API y del Mapa
1. **URL base del backend:** se configura en la constante `BASE_URL` dentro de `app/src/main/java/com/example/eventia/repositories/RetrofitRepository.kt`:
   ```kotlin
   const val BASE_URL = "https://eventia-api.example.com/"
   ```
2. **Mapa:** utiliza **osmdroid (OpenStreetMap)**, por lo que funciona directamente sin necesidad de configurar una API Key de Google Cloud.

---

## 6. Instrucciones de Compilación y Ejecución
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/yilJesus009/Final_Moviles.git
   ```
2. Abrir la carpeta en **Android Studio**.
3. Configurar la constante `BASE_URL` en `RetrofitRepository.kt` con la URL publicada en la plataforma del curso.
4. Sincronizar el proyecto con Gradle (**Sync Project with Gradle Files**).
5. Ejecutar en un emulador o dispositivo físico con **Android 13 (API 33)** o superior.

---

## 7. Escenarios Verificados Manualmente
* **Modo avión / sin conexión:** el catálogo muestra los datos guardados en Room con el aviso *"Sin conexión · datos de hace X min/h"*; deslizar para actualizar no borra la lista visible; los favoritos siguen accesibles y permiten agregar/quitar con opción "Deshacer".
* **Datos incompletos:** eventos con `imageUrl = null` muestran imagen sustituta; lugares con coordenadas `null` o `(0,0)` no generan marcadores inválidos en el mapa.
* **Rotación de pantalla:** el estado de búsqueda, filtros, formularios y posición de lista se conserva mediante ViewModels y `rememberLazyListState`.
* **Inscripción y errores de sesión:** manejo de evento sin cupos (`HTTP 409`), recurso inexistente (`HTTP 404`) y sesión expirada (`HTTP 401` borra la sesión en DataStore y redirige a Login sin cerrar la app).

---

## 8. Limitaciones Conocidas
* Los mosaicos (tiles) del mapa de OpenStreetMap requieren conexión a Internet la primera vez que se visualiza una zona geográfica; las zonas ya visitadas quedan en la caché local de `osmdroid`.
* La inscripción y cancelación de asistencia a eventos requieren conexión activa con el servidor para validar la disponibilidad de cupos en tiempo real.
* El requisito opcional RF-10 (cálculo de distancia por GPS del usuario) no está activado por defecto para no requerir permisos de ubicación en tiempo de ejecución.
