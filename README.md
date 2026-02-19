## 📱 CitySpots - Android Studio ##

### 🧭 Descripción del Proyecto 

**CitySpots** permite al usuario:
- Obtener su **ubicación en tiempo real**
- Tomar una **fotografía del entorno**
- Guardar un **Spot** (lugar) con un título secuencial
- Persistir la información **localmente**, sin conexión a internet

Todo el flujo está diseñado bajo el principio **Offline-First** y una **Single Source of Truth**.

---


## 🛠️ Tech Stack & Conceptos Clave

### Lenguaje & UI
- **Lenguaje:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)

### Arquitectura
- **MVVM**
- **Repository Pattern**
- **Single Source of Truth**

### Persistencia & Datos
- **Room Database** (SQLite)
- **Kotlin Coroutines**
- **Flow & StateFlow**

### Mapas & Ubicación
- **Google Maps SDK for Android**
- Maps Compose
- FusedLocationProviderClient (Google Play Services)

### Hardware
- **Cámara:** CameraX (ImageCapture)
- **GPS:** Ubicación precisa y continua

### Otros
- **Inyección de Dependencias:** Koin
- **Permisos:** ActivityResultLauncher
- **Manejo de Archivos:** Internal Storage & URIs

---


## ⭐ Características Principales

### 🛰️ Part 1: Hardware Integration & Real-Time Data

- **Rastreo GPS en Tiempo Real**  
  Actualizaciones continuas usando Flow con movimiento suave del marcador.

- **Gestión de Permisos Robusta**  
  Solicitud de permisos sin bloquear el hilo principal.

- **CameraX Integration**  
  Captura de imágenes en alta resolución guardadas en almacenamiento interno.

- **Google Maps Interactivo**
  - Marcadores dinámicos
  - Zoom nativo
  - Centrado automático

---

### 💾 Part 2: Arquitectura Modular & Persistencia

- **Repository Refactoring**  
  Lógica centralizada en `SpotRepository` respetando responsabilidad única.


#### 🛡️ Programación Defensiva

- **Borrado Seguro** (validación por ID)
- **Limpieza de Archivos** (evita residuos)
- **Títulos Secuenciales**
Spot #1
Spot #2
Spot #3
- **Validación de Datos**
- Coordenadas válidas
- Integridad de imágenes

---


## 🚀 Características Adicionales

- **UI Pulida** (FAB sin superposición)
- **High Contrast UX**
- **Error Handling** con Snackbars

---

### 🏗️ Implementación Técnica
Patrón Repository (Unificación de Fuentes de Datos)
El Repository Pattern actúa como intermediario entre la capa de presentación (ViewModels) y las múltiples fuentes de datos, garantizando una Single Source of Truth:
```
┌─────────────────────────────────────────────────────────────┐
│                        UI LAYER                             │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────┐    │
│  │  MapScreen  │    │ CameraScreen │    │ Permissions  │    │
│  └──────┬──────┘    └──────┬───────┘    └──────────────┘    │
│         │                   │                               │
│         └─────────┬─────────┘                               │
│                   ▼                                         │
│           ┌──────────────┐                                  │
│           │  MapViewModel │                                 │
│           └──────┬───────┘                                  │
└──────────────────┼──────────────────────────────────────────┘
                   │
┌──────────────────┼──────────────────────────────────────────┐
│                  ▼           DATA LAYER                     │
│           ┌──────────────┐                                  │
│           │ SpotRepository │  ◄── Single Source of Truth    │
│           └──────┬───────┘                                  │
│                  │                                          │
│     ┌────────────┼────────────┐                             │
│     ▼            ▼            ▼                             │
│ ┌────────┐  ┌──────────┐  ┌────────────┐                    │
│ │  Room  │  │ CameraX  │  │  Location  │                    │
│ │   DB   │  │  Utils   │  │   Utils    │                    │
│ └────────┘  └──────────┘  └────────────┘                    │
│     │            │             │                            │
│     ▼            ▼             ▼                            │
│  SQLite       Cámara        GPS/WiFi                        │
└─────────────────────────────────────────────────────────────┘
```

---
## 🧪 Cómo probar el proyecto ##

### Requisitos Previos

- **Android Studio Hedgehog (2023.1.1) o superior.**
  
- **Dispositivo Físico (Recomendado para probar Cámara y GPS real) o Emulador con soporte de Google Play Services.**

- **API KEY de Google Maps: Configurada en local.properties o AndroidManifest.xml.**

- **Clonar el repositorio:**

---

## 📦 Estructura del Proyecto ##
```
app/src/main/java/com/curso/android/module4/cityspots/
├── data/
│   ├── dao/
│   └── entity/
├── repository/
├── utils/
├── di/
└── ui/
```

---
### 📚 Recursos Utilizados 

- **Google Maps Platform**

- **Android Jetpack (CameraX, Room, Compose)**

- **Kotlin Coroutines & Flow**

**Desarrollado como parte del Módulo 4 de Fundamentos Avanzados de Aplicaciones Web**

---

**Link al video explicativo:** 
https://youtube.com/shorts/G5ZB0bz6R-Y?feature=share
