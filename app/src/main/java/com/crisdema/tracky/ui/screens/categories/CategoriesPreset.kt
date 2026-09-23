package com.crisdema.tracky.ui.screens.categories

import com.crisdema.tracky.R

val COLOR_FAMILIES = listOf(
    listOf("#F6CACD", "#E08585", "#E25D55", "#CE4A47", "#B03535", "#9B2626"), // Red
    listOf("#F7DAB2", "#F1B463", "#EA9624", "#E0861A", "#CB6714", "#B74D0E"), // Orange
    listOf("#FDF0A6", "#FCE475", "#F9D43F", "#EFC220", "#D9A812", "#B8890A"), // Yellow
    listOf("#C8E1C6", "#90C58D", "#62AC5E", "#52984E", "#3B7839", "#275A27"), // Green
    listOf("#B4DAD6", "#68B8AF", "#2A9B8E", "#21887C", "#1A6A5F", "#134E43"), // Teal
    listOf("#BAD7EF", "#76B3E4", "#3E95D9", "#3383C7", "#2663A5", "#1B4787"), // Blue
    listOf("#C7CBEA", "#9AA0D9", "#6D74C3", "#565DAE", "#3F4690", "#2A2F6B"), // Indigo
    listOf("#D9BCDF", "#B678C3", "#9943AA", "#86389C", "#652B87", "#471F74"), // Purple
    listOf("#F0BECE", "#E5799D", "#D84274", "#C33668", "#9B2959", "#771D4A"), // Pink
    listOf("#D9CFC8", "#AE998D", "#8B6C5C", "#7A5D4E", "#644B3E", "#44312A"), // Brown
    listOf("#D7D9DB", "#B8BBBE", "#93989D", "#797F85", "#5C6167", "#3D4247"), // Slate
)

val ICON_CATEGORIES: Map<Int, Map<String, Int>> = mapOf(
    R.string.category_group_home to mapOf(
        "home" to R.drawable.ic_home,
        "chair" to R.drawable.ic_chair,
        "electric_bolt" to R.drawable.ic_power,
        "lightbulb" to R.drawable.ic_lightbulb,
        "pets" to R.drawable.ic_pets,
    ),
    R.string.category_group_lifestyle to mapOf(
        "football" to R.drawable.ic_sports_and_outdoors,
        "fitness_center" to R.drawable.ic_fitness_center,
        "sports_esports" to R.drawable.ic_sports_esports,
        "directions_car" to R.drawable.ic_directions_car,
        "travel" to R.drawable.ic_travel,
    ),
    R.string.category_group_food to mapOf(
        "restaurant" to R.drawable.ic_restaurant,
        "local_pizza" to R.drawable.ic_local_pizza,
        "fastfood" to R.drawable.ic_fastfood,
        "local_bar" to R.drawable.ic_local_bar,
        "coffee" to R.drawable.ic_coffee,
    ),
    R.string.category_group_shopping to mapOf(
        "shopping_cart" to R.drawable.ic_shopping_cart,
        "receipt" to R.drawable.ic_receipt,
        "shopping_bag" to R.drawable.ic_shopping_bag,
        "bubble_chart" to R.drawable.ic_bubble_chart,
        "apparel" to R.drawable.ic_apparel,
    ),
    R.string.category_group_money to mapOf(
        "wallet" to R.drawable.ic_wallet,
        "payments" to R.drawable.ic_payments,
        "savings" to R.drawable.ic_savings,
        "credit_card" to R.drawable.ic_credit_card,
        "toll" to R.drawable.ic_toll,
    ),
    R.string.category_group_general to mapOf(
        "gift" to R.drawable.ic_gift,
        "local_gas_station" to R.drawable.ic_local_gas_station,
        "mobile" to R.drawable.ic_mobile,
        "pill" to R.drawable.ic_pill,
        "star" to R.drawable.ic_star,
    )
)

val ALL_ICONS_FLAT: Map<String, Int> = ICON_CATEGORIES.values
    .flatMap { it.entries }
    .associate { it.key to it.value }