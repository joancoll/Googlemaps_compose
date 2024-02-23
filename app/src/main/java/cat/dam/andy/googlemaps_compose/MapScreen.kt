package cat.dam.andy.googlemaps_compose

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.viewModelFactory
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.DEFAULT_LAT
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.DEFAULT_LONG
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.FASTEST_INTERVAL
import cat.dam.andy.googlemaps_compose.MainActivity.MapParameters.Companion.UPDATE_INTERVAL
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState


@Composable
fun MapScreen(context: Context, mapViewModel: MapViewModel, permissionManager: PermissionManager) {

    // Es declaren les variables que necessitem per obtenir la localització
    val fusedLocationProviderClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            for (location in locationResult.locations) {
                if (location != null) {
                    // Hem rebut una nova localització
                    // Optem per actualitzar el marcador sense fer zoom per si l'usuari marca altres punts
                    mapViewModel.setLastKnownLocation(location)
                    if (!mapViewModel.firstLocationFound && mapViewModel.getlastKnownLocationLatLng() != LatLng(
                            DEFAULT_LAT,
                            DEFAULT_LONG
                        )
                    ) {
                        mapViewModel.updateCameraPosition()
                        mapViewModel.firstLocationFound = true
                    }
////                     Però podriem optar per fer zoom sempre o en determinades condicions
//                    mapViewModel.updateCameraPosition() //zoom a la posició
////                     per exemple si està fora de la zona visible del mapa o ha canviat una distància determinada
//                      val latLng = LatLng(location.latitude, location.longitude) ... if (!isLocationVisible(latLng))
//                      if (location.distanceTo(anotherLocation!!) > 20)
//                     Si volguessim aturar les actualitzacions, també podem fer-ho amb aquesta línia
//                     fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                }
            }
        }
    }

    // Inicialitzem el mapa per esperar la primera localització
    mapViewModel.firstLocationFound = false
    InitMap(context, mapViewModel, permissionManager)

    // Si tenim permisos, comencem a rebre actualitzacions de la localització
    // Si no, les parem
    LaunchedEffect(mapViewModel.permissionGranted) {
        if (mapViewModel.permissionGranted) {
            // per fer una sola petició de localització:
            // getLocation(context, mapViewModel, fusedLocationProviderClient, permissionManager)
            // per fer actualitzacions constants de localització:
            startLocationUpdates(
                context,
                mapViewModel,
                fusedLocationProviderClient,
                locationCallback,
                permissionManager
            )
            Toast.makeText(context, "Location updates started", Toast.LENGTH_SHORT).show()
        } else
            stopLocationUpdates(fusedLocationProviderClient, locationCallback)
        Toast.makeText(context, "Location updates stopped", Toast.LENGTH_SHORT).show()
    }

    // DisposeEffect que s'executarà quan es destrueixi el composable
    DisposableEffect(Unit) {
        // Funció que s'executarà quan es crea el composable
        onDispose {
            // Funció que s'executarà quan s'abandoni el composable (com quan l'aplicació passa a l'estat en segon pla)
            stopLocationUpdates(fusedLocationProviderClient, locationCallback)
            Toast.makeText(context, "Location updates stopped", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun InitMap(context: Context, mapViewModel: MapViewModel, permissionManager: PermissionManager) {
    askPermission(context, mapViewModel, permissionManager)
    if (!mapViewModel.permissionGranted) {
        // Si no tenim permisos, mostrem un missatge a dalt del mapa
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red)
        ) {
            Text(text = context.getString(R.string.PermLocationNeeded))
        }
    }
    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        cameraPositionState = mapViewModel.getCameraPositionState(),
        uiSettings = MapUiSettings(zoomControlsEnabled = true)
    )
    { //només es mostrarà marcador si tenim permisos i hem trobat la primera localització
        if (mapViewModel.permissionGranted && mapViewModel.firstLocationFound) {
            Marker(
                state = MarkerState(position = mapViewModel.getlastKnownLocationLatLng()),
                title = context.getString(R.string.currentLocation),
                snippet = context.getString(R.string.currentLocation)
            )
        }
        AddMarkers(context)
    }
}

@Composable
fun AddMarkers(context: Context) {
    Marker(
        state = MarkerState(position = LatLng(41.9802474, 2.78356)),
        title = "Girona",
        snippet = context.getString(R.string.jewishGirona),
        icon =
        BitmapDescriptorFactory.defaultMarker(
            BitmapDescriptorFactory.HUE_CYAN
        )
    )
    Marker(
        state = MarkerState(position = LatLng(42.1998706, 2.6890259)),
        title = "Besalú",
        snippet = context.getString(R.string.jewishBesalu),
        icon =
        BitmapDescriptorFactory.defaultMarker(
            BitmapDescriptorFactory.HUE_CYAN
        )
    )
}

fun askPermission(
    context: Context,
    mapViewModel: MapViewModel,
    permissionManager: PermissionManager
) {
    permissionManager
        .request(Permissions.PermLocation)
        .rationale(
            description = context.getString(R.string.PermLocationNeeded),
            title = context.getString(R.string.PermLocation)
        )
        .checkAndRequestPermission { isGranted ->
            mapViewModel.permissionGranted = isGranted
        }
}


fun getLocation(
    context: Context,
    mapViewModel: MapViewModel,
    fusedLocationProviderClient: FusedLocationProviderClient,
    permissionManager: PermissionManager
) {
    permissionManager
        .request(Permissions.PermLocation)
        .rationale(
            description = context.getString(R.string.PermLocationNeeded),
            title = context.getString(R.string.PermLocation)
        )
        .checkAndRequestPermission { isGranted ->
            if (isGranted) {
                mapViewModel.permissionGranted = true
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(context as Activity)
                { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        val lastKnownLocation = task.result
                        mapViewModel.setLastKnownLocation(lastKnownLocation)
                        mapViewModel.updateCameraPosition()
                    }
                }
            } else {
                mapViewModel.permissionGranted = false
            }
        }
}

private fun startLocationUpdates(
    context: Context,
    mapViewModel: MapViewModel,
    fusedLocationProviderClient: FusedLocationProviderClient,
    locationCallback: LocationCallback,
    permissionManager: PermissionManager
) {
    // mentre cerca la localització no es permet clicar de nou el botó
//        btnFindMe?.setText(R.string.waitingLocation)
//        btnFindMe?.isEnabled = false
    //Configura l'actualització de les peticions d'ubicació
    val locationRequest = LocationRequest.Builder(UPDATE_INTERVAL)
    //Aquest mètode estableix la velocitat en mil·lisegons en què l'aplicació prefereix rebre actualitzacions d'ubicació. Tingueu en compte que les actualitzacions d'ubicació poden ser una mica més ràpides o més lentes que aquesta velocitat per optimitzar l'ús de la bateria, o pot ser que no hi hagi actualitzacions (si el dispositiu no té connectivitat, per exemple).
    locationRequest.setMinUpdateIntervalMillis(FASTEST_INTERVAL)
    //Aquest mètode estableix la taxa més ràpida en mil·lisegons en què la vostra aplicació pot gestionar les actualitzacions d'ubicació gràcies a peticions d'altres apps. A menys que la vostra aplicació es beneficiï de rebre actualitzacions més ràpidament que la taxa especificada a setInterval (), no cal que toqueu a aquest mètode.
    locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY)
    /*
       PRIORITY_BALANCED_POWER_ACCURACY - Utilitzeu aquest paràmetre per sol·licitar la precisió de la ubicació a un bloc de la ciutat, que té una precisió aproximada de 100 metres. Es considera un nivell aproximat de precisió i és probable que consumeixi menys energia. Amb aquesta configuració, és probable que els serveis d’ubicació utilitzin el WiFi i el posicionament de la torre cel·lular. Tingueu en compte, però, que l'elecció del proveïdor d'ubicació depèn de molts altres factors, com ara quines fonts estan disponibles.
       PRIORITY_HIGH_ACCURACY - Utilitzeu aquesta configuració per sol·licitar la ubicació més precisa possible. Amb aquesta configuració, és més probable que els serveis d’ubicació utilitzin el GPS per determinar la ubicació i consumeixi molta més energia.
       PRIORITY_LOW_POWER - Utilitzeu aquest paràmetre per sol·licitar una precisió a nivell de ciutat, que té una precisió d'aproximadament 10 quilòmetres. Es considera un nivell aproximat de precisió i és probable que consumeixi menys energia.
       PRIORITY_NO_POWER - Utilitzeu aquesta configuració si necessiteu un impacte insignificant en el consum d'energia, però voleu rebre actualitzacions d'ubicació quan estiguin disponibles. Amb aquesta configuració, l'aplicació no activa cap actualització d'ubicació, sinó que rep ubicacions activades per altres aplicacions.
        */

    permissionManager
        .request(Permissions.PermLocation)
        .rationale(
            description = context.getString(R.string.PermLocationNeeded),
            title = context.getString(R.string.PermLocation)
        )
        .checkAndRequestPermission { isGranted ->
            if (isGranted) {
                mapViewModel.permissionGranted = true
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(context as Activity)
                { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        val lastKnownLocation = task.result
                        mapViewModel.setLastKnownLocation(lastKnownLocation)
                        mapViewModel.updateCameraPosition()
                    }
                }
            } else {
                mapViewModel.permissionGranted = false
            }
        }

    fusedLocationProviderClient.requestLocationUpdates(
        locationRequest.build(),
        locationCallback as LocationCallback,
        Looper.getMainLooper()
    )
}

private fun stopLocationUpdates(
    fusedLocationProviderClient: FusedLocationProviderClient,
    locationCallback: LocationCallback
) {
    // Atura les actualitzacions de la ubicació quan passa a segon pla
    fusedLocationProviderClient.removeLocationUpdates(locationCallback)
}


