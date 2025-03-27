import retrofit2.http.*

interface NutritionixApi {
    @POST("natural/nutrients")
    suspend fun getNutritionInfo(
        @Header("x-app-id") appId: String,
        @Header("x-app-key") appKey: String,
        @Body body: Map<String, String>
    ): NutritionResponse
}