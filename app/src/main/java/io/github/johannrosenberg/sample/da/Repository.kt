package io.github.johannrosenberg.sample.da

import com.google.gson.Gson
import io.github.johannrosenberg.jetmagic.App
import io.github.johannrosenberg.jetmagic.R
import io.github.johannrosenberg.sample.da.web.AdoptMeWebAPI
import io.github.johannrosenberg.sample.da.web.PetAPIOptions
import io.github.johannrosenberg.sample.da.web.RetrofitClient
import io.github.johannrosenberg.sample.models.PetListItemInfo
import java.io.InputStream

class Repository {
    companion object {
        private var webApi: AdoptMeWebAPI = RetrofitClient.createRetrofitClient()

        var pets: List<PetListItemInfo>? = null

        suspend fun getPets(options: PetAPIOptions): List<PetListItemInfo> {
            // The web service that was used for retrieving cat data was shut down, so cat
            // data is now stored as assets in the app. The data is taken from:
            // https://furkids.org/cat-adoptions
            // https://www.battersea.org.uk/cats/cat-rehoming-gallery
            // The api is commented out in the event that an api is used again in the future.

            //pets = webApi.getPets(startPos = options.startPos, options.pageSize, if (options.sortDesc) "desc" else "asc")

            val inputStream: InputStream = App.context.resources.openRawResource(R.raw.pets)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)

            val jsonText = String(buffer)  //inputStream.bufferedReader().use { it.readText() }

            val gson = Gson()
            val pets = gson.fromJson(jsonText, Array<PetListItemInfo>::class.java)

            inputStream.close()
            return pets.toList()
        }

        suspend fun getPetById(id: Int): PetListItemInfo? {
            val pets = io.github.johannrosenberg.sample.da.Repository.Companion.webApi.getPetById(id = id)

            if (pets.isNotEmpty()) {
                return pets[0]
            } else {
                return null
            }
        }

        suspend fun getPetByName(name: String): PetListItemInfo? {
            val pets = io.github.johannrosenberg.sample.da.Repository.Companion.webApi.getPetByName(name = name)

            // The getPetByName actually returns a list although only one item will ever be in the list.
            if (pets.isNotEmpty()) {
                return pets[0]
            } else {
                return null
            }
        }
    }
}