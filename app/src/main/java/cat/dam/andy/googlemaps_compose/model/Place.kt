package cat.dam.andy.googlemaps_compose.model

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import cat.dam.andy.googlemaps_compose.utils.Constants.DEFAULT_LAT
import cat.dam.andy.googlemaps_compose.utils.Constants.DEFAULT_LONG

class Place(
    var latitude: Double= DEFAULT_LAT,
    var longitude: Double= DEFAULT_LONG,
    var title: String= "New Place",
    var snippet: String= "Custom Place",
    var icon: BitmapDescriptor = BitmapDescriptorFactory.defaultMarker()
)
