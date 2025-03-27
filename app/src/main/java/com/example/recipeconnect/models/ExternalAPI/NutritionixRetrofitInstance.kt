import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NutritionixRetrofitInstance {
    private const val BASE_URL = "https://trackapi.nutritionix.com/v2/"

    val api: NutritionixApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NutritionixApi::class.java)
    }
}