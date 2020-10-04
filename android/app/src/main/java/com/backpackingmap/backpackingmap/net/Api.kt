package com.backpackingmap.backpackingmap.net

import arrow.syntax.function.memoize
import com.backpackingmap.backpackingmap.BuildConfig
import com.backpackingmap.backpackingmap.net.auth.AuthInfo
import com.backpackingmap.backpackingmap.net.auth.RenewSessionResponseError
import com.backpackingmap.backpackingmap.net.tile.TileType
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = BuildConfig.API_BASE_URL

interface ApiService {
    suspend fun renewSession(@Header("Authorization") token: RenewalToken):
    @POST("session/renew")
            Response<AuthInfo, RenewSessionResponseError>

    @DELETE("session")
    suspend fun deleteSession()

    @GET("tile/{type}")
    suspend fun getTile(
        @Path("type") type: TileType,
        @Query("row") row: Int,
        @Query("col") col: Int,
    ): ResponseBody
}

object Api {
    val createService = ::createServiceUnmemoized.memoize()

    private fun createServiceUnmemoized(token: RenewalToken): ApiService {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(ApiTokenInterceptor(token))
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .build()

        return retrofit.create(ApiService::class.java)
    }
}