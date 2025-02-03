package cat.dam.andy.googlemaps_compose.viewmodel

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.DEFAULT_LAT
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.DEFAULT_LONG
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.MAP_LOCATION_ZOOM
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.MAP_ZOOM
import cat.dam.andy.googlemaps_compose.Place
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraPositionState
import java.util.Locale

class MapViewModel : ViewModel() {
    private var _lastKnownLocation by mutableStateOf<Location?>(null)
    private var _deviceLatLng by mutableStateOf(LatLng(DEFAULT_LAT, DEFAULT_LONG))
    private var _cameraPositionState by mutableStateOf<CameraPositionState>(
        CameraPositionState(
            CameraPosition.fromLatLngZoom(_deviceLatLng, MAP_ZOOM)
        )
    )
    var permissionGranted by mutableStateOf(false)
    var firstLocationFound by mutableStateOf(false)
    var places by mutableStateOf<List<Place>>(emptyList())
    var selectedMarker by mutableStateOf<Marker?>(null)
    var shouldShowMarkerMenu by mutableStateOf(false)
    var newMarker by mutableStateOf<LatLng?>(null)

    fun hideMarkerMenu() {
        shouldShowMarkerMenu = false
        selectedMarker = null
    }

    fun showMarkerMenu(marker: Marker) {
        shouldShowMarkerMenu = true
        selectedMarker = marker
    }


    fun updateCameraPosition(
        location: Location? = _lastKnownLocation,
        zoom: Float = MAP_LOCATION_ZOOM
    ) {
        if (location != null) {
            val newLatLng = LatLng(location.latitude, location.longitude)
            val newCameraPosition = CameraPosition.fromLatLngZoom(newLatLng, zoom)
            setCameraPositionState(CameraPositionState(newCameraPosition))
        }
    }

    fun getCameraPositionState(): CameraPositionState {
        return _cameraPositionState
    }

    fun setCameraPositionState(cameraPosition: CameraPositionState) {
        _cameraPositionState = cameraPosition
    }

    fun getLastKnownLocation(): Location? {
        return _lastKnownLocation
    }

    fun setLastKnownLocation(location: Location) {
        _lastKnownLocation = location
    }

    fun getlastKnownLocationLatLng(): LatLng {
        if (_lastKnownLocation == null) {
            return LatLng(DEFAULT_LAT, DEFAULT_LONG)
        } else {
            return LatLng(_lastKnownLocation!!.latitude, _lastKnownLocation!!.longitude)
        }
    }

    fun getLatitude(): String {
        if (_lastKnownLocation == null) {
            return "-  -"
        } else {
            return String.format(Locale.getDefault(),"%.4f", _lastKnownLocation?.latitude)
        }
    }

    fun getLongitude(): String {
        if (_lastKnownLocation == null) {
            return "-  -"
        } else {
            return String.format(Locale.getDefault(), "%.4f", _lastKnownLocation?.longitude)
        }
    }

    fun addPlace(newPlace: Place) {
        places = places.toMutableList().apply {
            add(newPlace)
        }
    }
    fun removePlace(placeToRemove: Place) {
        places = places.filter { it != placeToRemove }
    }

    fun updatePlace(place: Place, editedPlace: Place) {
        places = places.map { existingPlace ->
            if (existingPlace == place) {
                editedPlace
            } else {
                existingPlace
            }
        }
    }

}