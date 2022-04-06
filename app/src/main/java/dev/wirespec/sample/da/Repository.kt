package dev.wirespec.sample.da

import dev.wirespec.sample.da.web.AdoptMeWebAPI
import dev.wirespec.sample.da.web.PetAPIOptions
import dev.wirespec.sample.da.web.RetrofitClient
import dev.wirespec.sample.models.PetListItemInfo

class Repository {
    companion object {
        private var webApi: AdoptMeWebAPI = RetrofitClient.createRetrofitClient()

        var pets: List<PetListItemInfo>? = null

        suspend fun getPets(options: PetAPIOptions): List<PetListItemInfo> {
            pets = webApi.getPets(startPos = options.startPos, options.pageSize, if (options.sortDesc) "desc" else "asc")
            return pets!!
        }

        suspend fun getPetById(id: Int): PetListItemInfo? {
            val pets = webApi.getPetById(id = id)

            if (pets.isNotEmpty()) {
                return pets[0]
            } else {
                return null
            }
        }

        suspend fun getPetByName(name: String): PetListItemInfo? {
            val pets = webApi.getPetByName(name = name)

            // The getPetByName actually returns a list although only one item will ever be in the list.
            if (pets.isNotEmpty()) {
                return pets[0]
            } else {
                return null
            }
        }
    }
}