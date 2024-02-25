package cat.dam.andy.googlemaps_compose

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cat.dam.andy.googlemaps_compose.ui.theme.Googlemaps_composeTheme


class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManager
    private val mapViewModel by viewModels<MapViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)
        setContent {
            Googlemaps_composeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        context = this,
                        mapViewModel = mapViewModel,
                        onLocationClick = {
                            permissionManager
                                .request(Permissions.PermLocation)
                                .rationale(
                                    description = getString(R.string.PermLocationNeeded),
                                    title = getString(R.string.PermLocation)
                                )
                                .checkAndRequestPermission { isGranted ->
                                    mapViewModel.permissionGranted = isGranted
                                    mapViewModel.updateCameraPosition()
                                }
                            }
                    )
                }
            }
        }
    }

    @Composable
    fun MainScreen(context: Context, mapViewModel: MapViewModel, onLocationClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
        ) {
            MenuScreen(context,mapViewModel,onLocationClick)
            MapScreen(context, mapViewModel, permissionManager )
        }
    }

    class MapParameters {
        companion object {
            val UPDATE_INTERVAL: Long = 10000 /* 10 segons */
            val FASTEST_INTERVAL: Long = 5000 /* 5 segons */
            val DEFAULT_LAT = 0.0
            val DEFAULT_LONG = 0.0 //Ubicació per defecte
            val MAP_ZOOM = 10f //ampliació de zoom al marcador (més gran, més zoom)
            val MAP_LOCATION_ZOOM = 17f //ampliació de zoom al marcador ubicació
        }
    }

}




