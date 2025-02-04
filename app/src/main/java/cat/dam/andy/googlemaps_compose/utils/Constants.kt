package cat.dam.andy.googlemaps_compose.utils
        object Constants {
            const val SAVED_PERM_ALREADY_ASKED = "permissions_already_asked" // Per guardar si ja s'ha preguntat per permisos
            const val UPDATE_INTERVAL: Long = 10000 // 10 segons
            const val FASTEST_INTERVAL: Long = 5000 // 5 segons
            const val DEFAULT_LAT = 0.0
            const val DEFAULT_LONG = 0.0 //Ubicació per defecte
            const val MAP_ZOOM = 10f //ampliació de zoom al marcador (més gran, més zoom)
            const val MAP_LOCATION_ZOOM = 17f //ampliació de zoom al marcador ubicació
        }
