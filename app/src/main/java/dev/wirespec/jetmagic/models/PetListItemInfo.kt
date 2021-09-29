package dev.wirespec.jetmagic.models

/**
 * Contains all the data about a specific pet.
 */
data class PetListItemInfo (
    var id: Int = 0,
    var name: String = "",
    var birthdate: String = "",
    var gender: String = "",
    var color: String = "",
    var type: String = "",
    var description: String = "",
    var imageCount: Int = 1
)