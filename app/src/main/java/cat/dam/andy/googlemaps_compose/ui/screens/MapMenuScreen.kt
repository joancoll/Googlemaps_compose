package cat.dam.andy.googlemaps_compose.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cat.dam.andy.googlemaps_compose.R
import cat.dam.andy.googlemaps_compose.viewmodel.MapViewModel

@Composable
fun MapMenuScreen(context: Context, mapViewModel: MapViewModel, onLocationClick: () -> Unit) {
    Column(modifier = Modifier.background(Color.White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(context.getString(R.string.latitude))
                Text(context.getString(R.string.longitude))
            }
            Column(modifier = Modifier.weight(1f)) {
                if (mapViewModel.firstLocationFound) {
                    Text(mapViewModel.getLatitude())
                    Text(mapViewModel.getLongitude())
                } else {
                    Text("-  -")
                    Text("-  -")
                }
            }
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End
            ) {
                Button(onClick = { onLocationClick() }) {
                    Text(context.getString(R.string.find_me))
                }
            }
        }
    }
}