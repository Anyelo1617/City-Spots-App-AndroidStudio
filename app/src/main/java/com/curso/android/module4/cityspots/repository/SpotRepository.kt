package com.curso.android.module4.cityspots.repository

import android.location.Location
import android.net.Uri
import androidx.camera.core.ImageCapture
import com.curso.android.module4.cityspots.data.dao.SpotDao
import com.curso.android.module4.cityspots.data.entity.SpotEntity
import com.curso.android.module4.cityspots.utils.CameraUtils
import com.curso.android.module4.cityspots.utils.CoordinateValidator
import com.curso.android.module4.cityspots.utils.LocationUtils
import kotlinx.coroutines.flow.Flow

/**
 * =============================================================================
 * SpotRepository - Capa de Abstracción de Datos
 * =============================================================================
 *
 * CONCEPTO: Repository Pattern
 * El patrón Repository actúa como intermediario entre la capa de datos
 * (Room, APIs, Sensores) y la capa de dominio/presentación (ViewModels).
 *
 * BENEFICIOS:
 * 1. Abstracción: Los ViewModels no conocen las fuentes de datos
 * 2. Single Source of Truth: Un solo punto para obtener datos
 * 3. Desacoplamiento: Fácil cambiar implementaciones (ej: Room → Firebase)
 * 4. Testabilidad: Fácil de mockear para pruebas unitarias
 * 5. Centralización: Lógica de negocio de datos en un solo lugar
 *
 * UNIFICACIÓN DE FUENTES:
 * En este proyecto, el Repository unifica:
 * - Base de Datos Local (Room): Persistencia de spots
 * - Hardware (CameraX): Captura de fotos
 * - Servicios de Ubicación (FusedLocation): Coordenadas GPS
 *
 * ARQUITECTURA:
 * UI (Compose) → ViewModel → Repository → { Room, CameraUtils, LocationUtils }
 *
 * INYECCIÓN DE DEPENDENCIAS
 * -------------------------
 * Este Repository recibe sus dependencias via constructor, en lugar de
 * crearlas internamente. Esto se conoce como "Constructor Injection" y es
 * la forma preferida de DI porque:
 *
 * 1. **Testabilidad**: Puedes inyectar mocks/fakes en tests
 * 2. **Flexibilidad**: Koin decide qué implementación usar
 * 3. **Transparencia**: Las dependencias son explícitas en la firma
 *
 * =============================================================================
 */
class SpotRepository(
    // Dependencias inyectadas por Koin
    private val spotDao: SpotDao,
    private val cameraUtils: CameraUtils,
    private val locationUtils: LocationUtils,
    private val coordinateValidator: CoordinateValidator
) {

    // =========================================================================
    // OPERACIONES DE BASE DE DATOS (Room)
    // =========================================================================

    /**
     * Obtiene todos los spots como Flow reactivo
     *
     * El Flow emite automáticamente cuando hay cambios en la BD,
     * permitiendo que la UI se actualice sin polling manual.
     *
     * @return Flow<List<SpotEntity>> que emite la lista actualizada de spots
     */
    fun getAllSpots(): Flow<List<SpotEntity>> {
        return spotDao.getAllSpots()
    }

    /**
     * Obtiene un spot específico por ID
     *
     * @param id ID del spot a buscar
     * @return SpotEntity o null si no existe
     */
    suspend fun getSpotById(id: Long): SpotEntity? {
        return spotDao.getSpotById(id)
    }

    /**
     * Inserta un nuevo spot en la base de datos
     *
     * @param spot SpotEntity a insertar
     * @return ID del nuevo spot
     */
    suspend fun insertSpot(spot: SpotEntity): Long {
        return spotDao.insertSpot(spot)
    }



    /**
     * Obtiene el número de spots para generar títulos secuenciales
     *
     * @return Conteo total de spots
     */
    suspend fun getSpotCount(): Int {
        return spotDao.getSpotCount()
    }

    // =========================================================================
    // OPERACIONES DE HARDWARE (Cámara)
    // =========================================================================

    /**
     * Captura una foto usando CameraX
     *
     * Este método abstrae la complejidad de CameraX para los ViewModels.
     * El archivo se guarda automáticamente en el almacenamiento interno.
     *
     * @param imageCapture Use case de ImageCapture configurado en la UI
     * @return URI del archivo de imagen guardado
     */
    suspend fun capturePhoto(imageCapture: ImageCapture): Uri {
        return cameraUtils.capturePhoto(imageCapture)
    }

    // =========================================================================
    // OPERACIONES DE UBICACIÓN (GPS)
    // =========================================================================

    /**
     * Obtiene la ubicación actual del dispositivo
     *
     * Intenta obtener una ubicación fresca. Si falla, intenta
     * obtener la última ubicación conocida como fallback.
     *
     * @return Location con lat/lng o null si no hay ubicación disponible
     */
    suspend fun getCurrentLocation(): Location? {
        // Primero intentar ubicación fresca
        return locationUtils.getCurrentLocation()
        // Si falla, intentar última conocida
            ?: locationUtils.getLastLocation()
    }

    /**
     * Obtiene actualizaciones continuas de ubicación
     *
     * Útil para mostrar la posición del usuario en el mapa en tiempo real.
     *
     * @param intervalMs Intervalo entre actualizaciones
     * @return Flow de Location
     */
    fun getLocationUpdates(intervalMs: Long = 5000L) =
        locationUtils.getLocationUpdates(intervalMs)

    // =========================================================================
    // OPERACIONES COMBINADAS (Flujo completo de creación de Spot)
    // =========================================================================

    /**
     * Crea un nuevo Spot completo: captura foto + obtiene ubicación + guarda en BD
     *
     * Este método encapsula todo el flujo de creación de un spot:
     * 1. Captura la foto con CameraX
     * 2. Obtiene la ubicación GPS actual
     * 3. Valida las coordenadas GPS
     * 4. Genera un título secuencial
     * 5. Guarda todo en Room
     *
     * CONCEPTO: Este es un ejemplo de cómo el Repository puede orquestar
     * múltiples fuentes de datos en una sola operación cohesiva.
     *
     * @param imageCapture Use case de ImageCapture para la captura
     * @return CreateSpotResult con el spot creado o error detallado
     * @throws Exception si falla la captura de foto
     */
    suspend fun createSpot(imageCapture: ImageCapture): CreateSpotResult {
        // Capturamos la foto (ahora se maneja errores)
        val photoUri = try {
            cameraUtils.capturePhoto(imageCapture)
        } catch (e: Exception) {
            // Retornamos el error encapsulado
            return CreateSpotResult.CameraError(e)
        }

        // Obtenemos la ubi
        val location = locationUtils.getCurrentLocation()
        if (location == null) {
            cameraUtils.deleteImage(photoUri)
            return CreateSpotResult.NoLocation
        }

        // Validamos coordenadas
        val validationResult = coordinateValidator.validate(location.latitude, location.longitude)
        if (validationResult != CoordinateValidator.ValidationResult.Valid) {
            cameraUtils.deleteImage(photoUri)
            return CreateSpotResult.InvalidCoordinates(coordinateValidator.getErrorMessage(validationResult))
        }

        // se guarda en BD
        val spot = SpotEntity(
            title = "Spot #${spotDao.getSpotCount() + 1}",
            imageUri = photoUri.toString(),
            latitude = location.latitude,
            longitude = location.longitude
        )
        val id = spotDao.insertSpot(spot)
        return CreateSpotResult.Success(spot.copy(id = id))
    }
}

/**
 * Resultado de la creación de un Spot
 *
 * Sealed class que representa los posibles resultados de createSpot():
 * - Success: Spot creado exitosamente
 * - NoLocation: No se pudo obtener la ubicación GPS
 * - InvalidCoordinates: Las coordenadas GPS son inválidas
 */
sealed class CreateSpotResult {
    data class Success(val spot: SpotEntity) : CreateSpotResult()
    data object NoLocation : CreateSpotResult()
    data class InvalidCoordinates(val message: String) : CreateSpotResult()

    //Camera Error
    data class CameraError(val exception: Exception) : CreateSpotResult()
}

//Agregamos la capacidad de borrar tanto el archivo físico como el registro de la BD
suspend fun deleteSpot(spot: SpotEntity) {
    // Intenta borrar el archivo de imagen
    try {
        // Parseamos el String a Uri
        cameraUtils.deleteImage(android.net.Uri.parse(spot.imageUri))
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Se Borra de la base de datos
    spotDao.deleteSpotById(spot.id)
}