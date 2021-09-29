package dev.wirespec.jetmagic.da.web

/**
 * Used to hold options for the backend api.
 */
data class PetAPIOptions (
    var startPos: Int = 0,
    var pageSize: Int = PetAPIConfig.PagingSize,
    var sortDesc: Boolean = false
)

class PetAPIConfig {
    companion object {
        const val PagingSize = 60
    }
}

