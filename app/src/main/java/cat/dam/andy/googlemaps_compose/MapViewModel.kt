package cat.dam.andy.googlemaps_compose

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.DEFAULT_LAT
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.DEFAULT_LONG
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.MAP_LOCATION_ZOOM
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.MAP_ZOOM
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState

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

    fun updateCameraPosition(location:Location?=_lastKnownLocation, zoom:Float=MAP_LOCATION_ZOOM) {
        if (location != null) {
            val newLatLng = LatLng(location.latitude, location.longitude)
            val newCameraPosition = CameraPosition.fromLatLngZoom(newLatLng,zoom)
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
        }
        else {
            return LatLng(_lastKnownLocation!!.latitude, _lastKnownLocation!!.longitude)
        }
    }

    fun getLatitude(): String {
        if (_lastKnownLocation == null) {
            return "-  -"
        }
        else {
            return String.format("%.4f", _lastKnownLocation?.latitude)
        }
    }

    fun getLongitude(): String {
        if (_lastKnownLocation == null) {
            return "-  -"
        }
        else {
            return String.format("%.4f", _lastKnownLocation?.longitude)
        }
    }

    fun addPlace(newPlace: Place) {
        places = places.toMutableList().apply {
            add(newPlace)
        }
    }

}