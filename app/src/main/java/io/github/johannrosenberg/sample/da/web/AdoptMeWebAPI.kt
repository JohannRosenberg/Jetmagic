package io.github.johannrosenberg.sample.da.web

import io.github.johannrosenberg.sample.models.PetListItemInfo
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API declarations.
 *
 * NOTE: If a path starts with a forward slash, it means that it is relative to the root domain.
 * Without a prefixed forward slash, the path is appended to whatever the base url is set to.
 */
interface AdoptMeWebAPI {

    /**
     * Retrieves a list of pets.
     */
    @GET("getPets")
    suspend fun getPets(@Query("sp") startPos: Int, @Query("ps") pageSize: Int, @Query("sd") sortDirection: String): List<PetListItemInfo>

    /**
     * Retrieves a pet given its id.
     */
    @GET("getPets")
    suspend fun getPetById(@Query("id") id: Int): List<PetListItemInfo>

    /**
     * Retrieves a pet given its name. Partial names can be provided. A pet will be returned if it starts with the
     * provided name.
     */
    @GET("getPets")
    suspend fun getPetByName(@Query("name") name: String): List<PetListItemInfo>
}