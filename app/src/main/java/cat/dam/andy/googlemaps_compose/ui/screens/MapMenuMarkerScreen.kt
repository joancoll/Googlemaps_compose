package cat.dam.andy.googlemaps_compose.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cat.dam.andy.googlemaps_compose.R
import cat.dam.andy.googlemaps_compose.viewmodel.MapViewModel

@Composable
fun MapMenuMarkerScreen(context: Context, mapViewModel: MapViewModel, onMarkerDelete: () -> Unit, onMarkerEdit:()->Unit) {
    Column(modifier = Modifier.background(Color.Cyan)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(onClick = { onMarkerDelete() }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = context.getString(R.string.delete_marker))
            }
            IconButton(onClick = { onMarkerEdit() }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = context.getString(R.string.edit_marker))
            }
            IconButton(onClick = { mapViewModel.shouldShowMarkerMenu=false }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = context.getString(R.string.exit))
            }
        }
    }
}