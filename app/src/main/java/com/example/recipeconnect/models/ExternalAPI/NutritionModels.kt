data class NutritionResponse(val foods: List<FoodItem>)

data class FoodItem(
    val food_name: String,
    val nf_calories: Double,
    val serving_qty: Double,
    val serving_unit: String
)