package cat.dam.andy.googlemaps_compose

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cat.dam.andy.googlemaps_compose.model.Place
import cat.dam.andy.googlemaps_compose.permissions.PermissionManager
import cat.dam.andy.googlemaps_compose.permissions.Permissions
import cat.dam.andy.googlemaps_compose.ui.screens.MapMenuMarkerScreen
import cat.dam.andy.googlemaps_compose.ui.screens.MapMenuScreen
import cat.dam.andy.googlemaps_compose.ui.screens.MapScreen
import cat.dam.andy.googlemaps_compose.ui.theme.Googlemaps_composeTheme
import cat.dam.andy.googlemaps_compose.viewmodel.MapViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng

sealed class DialogState {
    // To control the state of the dialog (add, edit, delete or any)
    object None : DialogState()
    data class Add(val latLng: LatLng) : DialogState()
    data class Edit(val item: Place) : DialogState()
    data class Delete(val item: Place) : DialogState()
}

private var dialogState by mutableStateOf<DialogState>(DialogState.None)

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
                        permissionManager = permissionManager
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(context: Context, mapViewModel: MapViewModel, permissionManager: PermissionManager) {
        Scaffold(
            content = { innerPadding ->
                // Contingut principal
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    MapMenuScreen(context, mapViewModel,
                        onLocationClick = {
                            mapViewModel.hideMarkerMenu()
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
                        })
                    MapScreen(context, mapViewModel, permissionManager)
                    when (val currentState = dialogState) {
                        is DialogState.Add -> ShowAddDialog(currentState.latLng)
                        is DialogState.Edit -> ShowEditDialog(place = currentState.item)
                        is DialogState.Delete -> ShowDeleteConfirmationDialog(place = currentState.item)
                        is DialogState.None -> {}
                    }
                }
            },
            bottomBar = {
                if (mapViewModel.shouldShowMarkerMenu) {
                    MapMenuMarkerScreen(context, mapViewModel, onMarkerDelete = {
                        val selectedMarker = mapViewModel.selectedMarker
                        val places = mapViewModel.places
                        if (selectedMarker != null) {
                            val selectedLatitude = selectedMarker.position.latitude
                            val selectedLongitude = selectedMarker.position.longitude
                            val selectedPlace: Place? =
                                places.find { it.latitude == selectedLatitude && it.longitude == selectedLongitude }
                            if (selectedPlace != null) {
                                dialogState = DialogState.Delete(selectedPlace)
                            }
                        }
                    },
                        onMarkerEdit = {
                            val selectedMarker = mapViewModel.selectedMarker
                            val places = mapViewModel.places
                            if (selectedMarker != null) {
                                val selectedLatitude = selectedMarker.position.latitude
                                val selectedLongitude = selectedMarker.position.longitude
                                val selectedPlace: Place? =
                                    places.find { it.latitude == selectedLatitude && it.longitude == selectedLongitude }
                                if (selectedPlace != null) {
                                    dialogState = DialogState.Edit(selectedPlace)
                                }
                            }
                        })
                }
            }
        )
        if (mapViewModel.newMarker != null) {
            dialogState = DialogState.Add(mapViewModel.newMarker!!)
        }
    }

    @Composable
    private fun ShowDeleteConfirmationDialog(place: Place) {
        AlertDialog(
            onDismissRequest = { dialogState = DialogState.None },
            title = { Text(stringResource(id = R.string.delete_place)) },
            text = { Text("${stringResource(id = R.string.delete_confirmation)} ${place.title}?") },
            confirmButton = {
                Button(
                    onClick = {
                        mapViewModel.removePlace(place)
                        mapViewModel.hideMarkerMenu()
                        dialogState = DialogState.None
                    }
                ) {
                    Text(stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                Button(onClick = { dialogState = DialogState.None }) {
                    Text(stringResource(id = R.string.cancel))
                }
            })
    }

    @Composable
    private fun ShowEditDialog(place: Place) {
        var editedTitle by remember { mutableStateOf(place.title) }
        var editedSnippet by remember { mutableStateOf(place.snippet) }
        val errorTitleValidation = stringResource(id = R.string.errorTitleValidation)
        val context = LocalContext.current

        Dialog(onDismissRequest = {
            // Close the dialog
            dialogState = DialogState.None
        }) {
            AlertDialog(
                onDismissRequest = { dialogState = DialogState.None },
                title = {
                    Text(stringResource(id = R.string.edit_place))
                },
                text = {
                    LazyColumn {
                        item {
                            // Defining a focusManager in the AlertDialog
                            val focusManager = LocalFocusManager.current
                            TextField(
                                value = editedTitle,
                                onValueChange = {
                                    editedTitle = it
                                },
                                label = { Text(stringResource(id = R.string.title)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        // Moving the focus to the next field when "Next" is pressed in the keyboard
                                        focusManager.moveFocus(FocusDirection.Next)
                                    }
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = editedSnippet,
                                onValueChange = {
                                    editedSnippet = it
                                },
                                label = { Text(stringResource(id = R.string.snippet)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (editedTitle.isNotEmpty()) {
                                            val editedPlace = Place(
                                                place.latitude,
                                                place.longitude,
                                                editedTitle,
                                                editedSnippet,
                                                place.icon
                                            )
                                            mapViewModel.updatePlace(place, editedPlace)
                                            mapViewModel.hideMarkerMenu()
                                            // Close the dialog
                                            dialogState = DialogState.None
                                        } else {
                                            Toast.makeText(
                                                context,
                                                errorTitleValidation,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editedTitle.isNotEmpty()) {
                                val editedPlace = Place(
                                    place.latitude,
                                    place.longitude,
                                    editedTitle,
                                    editedSnippet,
                                    place.icon
                                )
                                mapViewModel.updatePlace(place, editedPlace)
                                mapViewModel.hideMarkerMenu()
                                // Close the dialog
                                dialogState = DialogState.None
                            } else {
                                Toast.makeText(
                                    context,
                                    errorTitleValidation,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.save_changes))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            // Close the dialog without saving the changes
                            dialogState = DialogState.None
                        }
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    }

    @Composable
    private fun ShowAddDialog(latLng: LatLng) {
        var title by remember { mutableStateOf("") }
        var snippet by remember { mutableStateOf("") }
        val errorTitleValidation = stringResource(id = R.string.errorTitleValidation)
        val context = LocalContext.current

        Dialog(onDismissRequest = {
            // Close the dialog
            dialogState = DialogState.None
        }) {
            AlertDialog(
                onDismissRequest = { dialogState = DialogState.None },
                title = {
                    Text(stringResource(id = R.string.add_place))
                },
                text = {
                    LazyColumn {
                        item {
                            // Defining a focusManager in the AlertDialog
                            Text(text = stringResource(id = R.string.latitude) + " : " + latLng.latitude)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = stringResource(id = R.string.longitude) + " : " + latLng.longitude)
                            Spacer(modifier = Modifier.height(8.dp))
                            val focusManager = LocalFocusManager.current
                            TextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                },
                                label = { Text(stringResource(id = R.string.title)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        // Moving the focus to the next field when "Next" is pressed in the keyboard
                                        focusManager.moveFocus(FocusDirection.Next)
                                    }
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = snippet,
                                onValueChange = {
                                    snippet = it
                                },
                                label = { Text(stringResource(id = R.string.snippet)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (title.isNotEmpty()) {
                                            val place = Place(
                                                latLng.latitude,
                                                latLng.longitude,
                                                title,
                                                snippet,
                                                BitmapDescriptorFactory.fromResource(R.drawable.custom_marker)
                                            )
                                            mapViewModel.addPlace(place)
                                            mapViewModel.newMarker = null
                                            dialogState = DialogState.None
                                        } else {
                                            Toast.makeText(
                                                context,
                                                errorTitleValidation,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                val place = Place(
                                    latLng.latitude,
                                    latLng.longitude,
                                    title,
                                    snippet,
                                    BitmapDescriptorFactory.fromResource(R.drawable.custom_marker)
                                )
                                mapViewModel.addPlace(place)
                                mapViewModel.newMarker = null
                                dialogState = DialogState.None
                            } else {
                                Toast.makeText(
                                    context,
                                    errorTitleValidation,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            mapViewModel.newMarker = null
                        }
                    ) {
                        Text(stringResource(id = R.string.add_place))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            // Close the dialog
                            dialogState = DialogState.None
                            mapViewModel.newMarker = null
                        }
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    }
}




