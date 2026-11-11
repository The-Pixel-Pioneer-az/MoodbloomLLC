package com.example.data

import com.example.R
import com.example.model.AddOnOption
import com.example.model.DrinkSize
import com.example.model.MenuCategory
import com.example.model.MenuItem

object MenuCatalog {

    val standardAddOns = listOf(
        AddOnOption("guac", "Extra Guacamole", 1.00),
        AddOnOption("cheese", "Extra Melted Cheese", 0.75),
        AddOnOption("patty", "Extra Smashed Patty", 3.00),
        AddOnOption("bacon", "Crispy Bacon", 1.50)
    )

    val quesadillaAddOns = listOf(
        AddOnOption("chicken", "Add Chicken to Quesadilla", 2.50),
        AddOnOption("guac", "Extra Guacamole", 1.00),
        AddOnOption("extra_cheese", "Extra Melted Cheese", 0.75)
    )

    val burritoAddOns = listOf(
        AddOnOption("guac", "Extra Guacamole", 1.00),
        AddOnOption("extra_cheese", "Extra Melted Cheese", 0.75),
        AddOnOption("hashbrown", "Extra Crispy Hash Browns", 1.50)
    )

    val friesAddOns = listOf(
        AddOnOption("guac", "Side of Fresh Guacamole", 1.00),
        AddOnOption("cheese_sauce", "Warm Melted Cheese", 1.25),
        AddOnOption("bacon_bits", "Bacon Crumbles", 1.00)
    )

    val standardCondiments = listOf(
        "Ketchup",
        "Mayonnaise",
        "Mustard",
        "Relish",
        "Chipotle Crema",
        "Salsa Verde",
        "Salsa Roja"
    )

    val hotDogCondiments = listOf(
        "Yellow Mustard",
        "Sweet Relish",
        "Diced Onion",
        "Mayonnaise",
        "Ketchup",
        "Pico de Gallo",
        "Pinto Beans"
    )

    val drinkFlavors = listOf(
        "Mexican Coke (Glass Bottle)",
        "House Horchata (Cinnamon Rice Milk)",
        "Jarritos Mandarina",
        "Jarritos Lime",
        "Hibiscus Agua Fresca (Jamaica)",
        "Fresh Squeezed Lemonade",
        "Cold Brew Iced Coffee",
        "Craft Fountain Soda"
    )

    val allMenuItems: List<MenuItem> = listOf(
        // ================= APPETIZERS =================
        MenuItem(
            id = "item_3",
            itemNumber = "3",
            name = "Crispy Fries",
            category = MenuCategory.APPETIZERS,
            price = 2.75,
            description = "Golden and crispy, served hot. Great on their own, even better with guacamole.",
            imageRes = R.drawable.menu_catalog_page_1,
            isPopular = true,
            availableAddOns = friesAddOns,
            availableCondiments = standardCondiments
        ),
        MenuItem(
            id = "item_6",
            itemNumber = "6",
            name = "Cheese Quesadilla",
            category = MenuCategory.APPETIZERS,
            price = 5.50,
            description = "A griddled tortilla with melted cheese, crisp at the edges and gooey in the middle. Add chicken for $2.50.",
            imageRes = R.drawable.menu_catalog_page_1,
            availableAddOns = quesadillaAddOns,
            availableCondiments = listOf("Chipotle Crema", "Salsa Verde", "Salsa Roja", "Sour Cream")
        ),
        MenuItem(
            id = "item_guac_chips",
            itemNumber = "App-1",
            name = "Fresh Guacamole & Chips",
            category = MenuCategory.APPETIZERS,
            price = 4.50,
            description = "Freshly crushed ripe avocados with lime, cilantro, tomato, onion, and sea salt, served with warm house tortilla chips.",
            imageRes = R.drawable.menu_catalog_page_3,
            isPopular = true,
            availableAddOns = listOf(
                AddOnOption("extra_guac", "Double Guacamole Portion", 2.00),
                AddOnOption("jalapenos", "Extra Jalapenos", 0.00)
            ),
            availableCondiments = listOf("Salsa Verde", "Salsa Roja")
        ),

        // ================= MAIN COURSES =================
        MenuItem(
            id = "item_1",
            itemNumber = "1",
            name = "Classic Smash Burger",
            category = MenuCategory.MAIN_COURSES,
            price = 7.50,
            description = "A street-food classic, smashed and stacked with lettuce, tomato, and onion. Finish it your way at the condiment bar.",
            imageRes = R.drawable.dish_smash_burger,
            isPopular = true,
            availableAddOns = standardAddOns,
            availableCondiments = standardCondiments
        ),
        MenuItem(
            id = "item_2",
            itemNumber = "2",
            name = "Baja Burger",
            category = MenuCategory.MAIN_COURSES,
            price = 10.50,
            description = "Pepper jack, guacamole, and jalapenos on a smashed patty. The Baja kick to the MoodBloom burger lineup.",
            imageRes = R.drawable.dish_baja_burger,
            isSpicy = true,
            isPopular = true,
            availableAddOns = standardAddOns,
            availableCondiments = standardCondiments
        ),
        MenuItem(
            id = "item_5",
            itemNumber = "5",
            name = "Sonoran Dog",
            category = MenuCategory.MAIN_COURSES,
            price = 4.00,
            description = "Bacon-wrapped, with pinto beans, pico de gallo, mayo, and mustard. The Sonoran way.",
            imageRes = R.drawable.dish_sonoran_dog,
            isPopular = true,
            availableAddOns = listOf(
                AddOnOption("guac", "Add Guacamole", 1.00),
                AddOnOption("jalapenos", "Extra Jalapenos", 0.00)
            ),
            availableCondiments = hotDogCondiments
        ),
        MenuItem(
            id = "item_4",
            itemNumber = "4",
            name = "Classic Hot Dog",
            category = MenuCategory.MAIN_COURSES,
            price = 4.00,
            description = "A classic dog in a soft bun, ready for mustard, relish, onion - or all three.",
            imageRes = R.drawable.menu_catalog_page_1,
            availableAddOns = listOf(
                AddOnOption("guac", "Add Guacamole", 1.00),
                AddOnOption("bacon", "Add Crispy Bacon", 1.50)
            ),
            availableCondiments = hotDogCondiments
        ),
        MenuItem(
            id = "item_7",
            itemNumber = "7",
            name = "Chorizo & Egg Breakfast Burrito",
            category = MenuCategory.MAIN_COURSES,
            price = 8.50,
            description = "Chorizo and egg wrapped warm in a tortilla. The Mexican-style half of the MoodBloom morning.",
            imageRes = R.drawable.dish_burrito,
            isPopular = true,
            availableAddOns = burritoAddOns,
            availableCondiments = listOf("Salsa Verde", "Salsa Roja", "Sour Cream", "Chipotle Crema")
        ),
        MenuItem(
            id = "item_8",
            itemNumber = "8",
            name = "Spam, Egg & Hash Brown Breakfast Burrito",
            category = MenuCategory.MAIN_COURSES,
            price = 8.50,
            description = "Spam and crispy hash browns with egg, wrapped in a warm tortilla.",
            imageRes = R.drawable.dish_burrito,
            availableAddOns = burritoAddOns,
            availableCondiments = listOf("Salsa Verde", "Salsa Roja", "Ketchup", "Chipotle Crema")
        ),

        // COMBOS (Main Course Combos - With Fries & a Drink)
        MenuItem(
            id = "combo_1",
            itemNumber = "#1",
            name = "#1 Classic Smash Burger Combo",
            category = MenuCategory.COMBOS,
            price = 15.50,
            description = "Classic Smash Burger with crispy fries and a drink of your choice.",
            imageRes = R.drawable.dish_smash_burger,
            isCombo = true,
            comboIncludes = "Includes: Smash Burger, Crispy Fries, & Drink",
            isPopular = true,
            availableAddOns = standardAddOns,
            availableCondiments = standardCondiments,
            defaultDrinkIncluded = true
        ),
        MenuItem(
            id = "combo_2",
            itemNumber = "#2",
            name = "#2 Baja Burger Combo",
            category = MenuCategory.COMBOS,
            price = 17.00,
            description = "Baja Burger stacked with pepper jack, guac, and jalapenos, served with crispy fries and a drink.",
            imageRes = R.drawable.dish_baja_burger,
            isCombo = true,
            comboIncludes = "Includes: Baja Burger, Crispy Fries, & Drink",
            isSpicy = true,
            isPopular = true,
            availableAddOns = standardAddOns,
            availableCondiments = standardCondiments,
            defaultDrinkIncluded = true
        ),
        MenuItem(
            id = "combo_3",
            itemNumber = "#3",
            name = "#3 Cheese Quesadilla Combo",
            category = MenuCategory.COMBOS,
            price = 12.00,
            description = "Cheese Quesadilla with crispy golden fries and a refreshing drink.",
            imageRes = R.drawable.menu_catalog_page_2,
            isCombo = true,
            comboIncludes = "Includes: Cheese Quesadilla, Crispy Fries, & Drink",
            availableAddOns = quesadillaAddOns,
            availableCondiments = standardCondiments,
            defaultDrinkIncluded = true
        ),
        MenuItem(
            id = "combo_4",
            itemNumber = "#4",
            name = "#4 Hot Dog Combo",
            category = MenuCategory.COMBOS,
            price = 8.50,
            description = "Classic Hot Dog or Sonoran Dog with crispy fries and a drink.",
            imageRes = R.drawable.dish_sonoran_dog,
            isCombo = true,
            comboIncludes = "Includes: Dog of your choice, Crispy Fries, & Drink",
            availableAddOns = listOf(
                AddOnOption("bacon_wrap", "Bacon-Wrapped Sonoran Style", 0.00),
                AddOnOption("guac", "Extra Guacamole", 1.00)
            ),
            availableCondiments = hotDogCondiments,
            defaultDrinkIncluded = true
        ),

        // ================= DESSERTS =================
        MenuItem(
            id = "item_9",
            itemNumber = "9",
            name = "Vanilla Milkshake",
            category = MenuCategory.DESSERTS,
            price = 3.50,
            description = "Thick, cold, and classic. The vanilla shake, done simple and done right.",
            imageRes = R.drawable.dish_milkshake,
            availableAddOns = listOf(
                AddOnOption("extra_whip", "Extra Whipped Cream", 0.50),
                AddOnOption("sprinkles", "Rainbow Sprinkles", 0.50)
            )
        ),
        MenuItem(
            id = "item_10",
            itemNumber = "10",
            name = "Chocolate Milkshake",
            category = MenuCategory.DESSERTS,
            price = 3.50,
            description = "Rich chocolate, blended thick and cold, with whipped cream on top.",
            imageRes = R.drawable.dish_milkshake,
            isPopular = true,
            availableAddOns = listOf(
                AddOnOption("extra_whip", "Extra Whipped Cream", 0.50),
                AddOnOption("choc_drizzle", "Extra Chocolate Drizzle", 0.50)
            )
        ),
        MenuItem(
            id = "item_churros",
            itemNumber = "Des-1",
            name = "Cinnamon Sugar Churros",
            category = MenuCategory.DESSERTS,
            price = 4.00,
            description = "Crispy golden Mexican churros dusted in cinnamon sugar, served with warm spiced chocolate dipping sauce.",
            imageRes = R.drawable.dish_milkshake,
            availableAddOns = listOf(
                AddOnOption("extra_choc", "Extra Mexican Chocolate Dip", 1.00),
                AddOnOption("dulce_leche", "Dulce de Leche Caramel Dip", 1.00)
            )
        ),

        // ================= BEVERAGES =================
        MenuItem(
            id = "drinks_custom",
            itemNumber = "Drinks",
            name = "MoodBloom Fountain & Agua Fresca",
            category = MenuCategory.BEVERAGES,
            price = 2.00,
            description = "Small ($2.00), Medium ($2.50), or Large ($3.00). Made fresh daily. Every combo comes with crispy fries and a drink.",
            imageRes = R.drawable.menu_catalog_page_3,
            allowedSizes = listOf(DrinkSize.SMALL, DrinkSize.MEDIUM, DrinkSize.LARGE),
            availableCondiments = emptyList()
        ),
        MenuItem(
            id = "drink_mex_coke",
            itemNumber = "Dr-1",
            name = "Mexican Coke (Heirloom Glass Bottle)",
            category = MenuCategory.BEVERAGES,
            price = 3.00,
            description = "Imported ice-cold Coca-Cola sweetened with 100% pure cane sugar in the classic glass bottle.",
            imageRes = R.drawable.menu_catalog_page_3,
            isPopular = true
        ),
        MenuItem(
            id = "drink_horchata",
            itemNumber = "Dr-2",
            name = "House Horchata",
            category = MenuCategory.BEVERAGES,
            price = 2.50,
            description = "Traditional slow-brewed rice milk with toasted Ceylon cinnamon, Madagascar vanilla, and evaporated milk.",
            imageRes = R.drawable.menu_catalog_page_3,
            isPopular = true
        ),
        MenuItem(
            id = "drink_jarritos",
            itemNumber = "Dr-3",
            name = "Jarritos Mexican Soda",
            category = MenuCategory.BEVERAGES,
            price = 2.50,
            description = "Crisp, naturally fruit-flavored Mexican soda. Choose Mandarina (mandarin orange) or Lime.",
            imageRes = R.drawable.menu_catalog_page_3
        )
    )

    val truckStops = listOf(
        com.example.model.TruckLocation(
            id = "loc_1",
            name = "Arts District Food Yard",
            address = "742 S. Santa Fe Ave",
            cityState = "Los Angeles, CA 90021",
            scheduleDay = "Today (Wednesday)",
            hours = "11:00 AM – 9:00 PM",
            isCurrent = true,
            distanceMiles = 0.4,
            waitTimeMinutes = 12,
            parkingTip = "Dedicated food truck parking lot + street meters on Santa Fe.",
            latitude = 34.0353,
            longitude = -118.2325
        ),
        com.example.model.TruckLocation(
            id = "loc_2",
            name = "Culver City Brewery Row",
            address = "8850 Washington Blvd",
            cityState = "Culver City, CA 90232",
            scheduleDay = "Thursday & Friday",
            hours = "4:00 PM – 10:00 PM",
            isCurrent = false,
            distanceMiles = 5.2,
            waitTimeMinutes = 15,
            parkingTip = "Park in structure behind the taprooms.",
            latitude = 34.0259,
            longitude = -118.3900
        ),
        com.example.model.TruckLocation(
            id = "loc_3",
            name = "Downtown Farmers & Artisan Market",
            address = "532 S. Olive St (Pershing Square)",
            cityState = "Los Angeles, CA 90013",
            scheduleDay = "Saturday",
            hours = "9:00 AM – 3:30 PM",
            isCurrent = false,
            distanceMiles = 1.8,
            waitTimeMinutes = 18,
            parkingTip = "Underground Pershing Square garage validation available.",
            latitude = 34.0483,
            longitude = -118.2512
        ),
        com.example.model.TruckLocation(
            id = "loc_4",
            name = "Venice Beach Sunset Rodeo",
            address = "1800 Ocean Front Walk",
            cityState = "Venice, CA 90291",
            scheduleDay = "Sunday",
            hours = "12:00 PM – 8:30 PM",
            isCurrent = false,
            distanceMiles = 11.0,
            waitTimeMinutes = 20,
            parkingTip = "Beach parking lot at Rose Ave.",
            latitude = 33.9877,
            longitude = -118.4735
        )
    )
}
