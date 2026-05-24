package com.example.data

import androidx.compose.ui.graphics.Color

enum class GarmentType {
    TEE, HOODIE, JACKET, CAP, PANTS
}

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val description: String,
    val availableColors: List<ProductColor>,
    val defaultColor: ProductColor,
    val garmentType: GarmentType,
    val rating: Float,
    val reviewCount: Int,
    val isNewRelease: Boolean = false,
    val isBestSeller: Boolean = false
)

data class ProductColor(
    val name: String,
    val hexColor: Long,
    val accentHexColor: Long
) {
    fun toComposeColor() = Color(hexColor)
    fun toAccentColor() = Color(accentHexColor)
}

object ProductCatalog {
    val COLORS_OBSIDIAN = ProductColor("Obsidian Black", 0xFF121212, 0xFFFAF7F2)
    val COLORS_SAND = ProductColor("Khaki Sand", 0xFFE1DCD3, 0xFF121212)
    val COLORS_OFFWHITE = ProductColor("Off White", 0xFFFAF7F2, 0xFFFF5722)
    val COLORS_OLIVE = ProductColor("Forest Olive", 0xFF3D4E3A, 0xFFFAF7F2)
    val COLORS_ORANGE = ProductColor("Sunset Orange", 0xFFFF5722, 0xFF121212)
    val COLORS_CHARCOAL = ProductColor("Charcoal Grey", 0xFF2F2F30, 0xFFDFFF00)
    val COLORS_SLATE = ProductColor("Slate Blue", 0xFF4A5568, 0xFFFAF7F2)
    val COLORS_ACID_GREEN = ProductColor("Acid Green", 0xFFDFFF00, 0xFF121212)

    val products = listOf(
        Product(
            id = "1",
            name = "Locha Signature Hoodie",
            category = "Hoodies",
            price = 89.00,
            description = "Oversized heavyweight French Terry hoodie with front drop shoulders, signature high-density logo print on the chest, and double-layered relaxed hood. Engineered for comfort and longevity.",
            availableColors = listOf(COLORS_OBSIDIAN, COLORS_OLIVE, COLORS_ORANGE, COLORS_OFFWHITE),
            defaultColor = COLORS_OBSIDIAN,
            garmentType = GarmentType.HOODIE,
            rating = 4.8f,
            reviewCount = 124,
            isNewRelease = true,
            isBestSeller = true
        ),
        Product(
            id = "2",
            name = "Rogue Utility Jacket",
            category = "Outerwear",
            price = 145.00,
            description = "Heavy canvas military-inspired streetwear jacket. Features triple 3D utility pocketing, distressed silver modular rivets, high storm collar, and heavy duty double-zip closure.",
            availableColors = listOf(COLORS_SLATE, COLORS_OBSIDIAN, COLORS_SAND),
            defaultColor = COLORS_SLATE,
            garmentType = GarmentType.JACKET,
            rating = 4.9f,
            reviewCount = 58,
            isNewRelease = true
        ),
        Product(
            id = "3",
            name = "Rebel Modern Typo Tee",
            category = "T-Shirts",
            price = 45.00,
            description = "100% premium combed organic cotton box-fit tee featuring bold vertical typographic branding backprint, drop chest patch pocket, and pre-shrunk tactical wash.",
            availableColors = listOf(COLORS_OFFWHITE, COLORS_OBSIDIAN, COLORS_ACID_GREEN),
            defaultColor = COLORS_OFFWHITE,
            garmentType = GarmentType.TEE,
            rating = 4.7f,
            reviewCount = 203,
            isBestSeller = true
        ),
        Product(
            id = "4",
            name = "Urban Patrol Cap",
            category = "Accessories",
            price = 35.00,
            description = "Five-panel active tactical flat-brim cap made in ultra-durable ripstop weave with central embroidered Locha label, water-resistant seams, and low-profile buckle adjuster.",
            availableColors = listOf(COLORS_OLIVE, COLORS_SAND, COLORS_OBSIDIAN),
            defaultColor = COLORS_OLIVE,
            garmentType = GarmentType.CAP,
            rating = 4.6f,
            reviewCount = 89
        ),
        Product(
            id = "5",
            name = "Slacker Pleated Trousers",
            category = "Pants",
            price = 110.00,
            description = "Extremely relaxed-fit wide pleated crop pants with custom elastic action waist inserts, heavy-weight structured cotton diagonal weave, and woven signature Locha label.",
            availableColors = listOf(COLORS_CHARCOAL, COLORS_SAND, COLORS_OBSIDIAN),
            defaultColor = COLORS_CHARCOAL,
            garmentType = GarmentType.PANTS,
            rating = 4.8f,
            reviewCount = 74,
            isNewRelease = true
        ),
        Product(
            id = "6",
            name = "Asymmetric Splat Tee",
            category = "T-Shirts",
            price = 48.00,
            description = "Heavy custom raw-cut boxy t-shirt displaying stylized ink distortion dye markings, custom neon overlock stitching, and reinforced seamless high collars.",
            availableColors = listOf(COLORS_CHARCOAL, COLORS_OLIVE),
            defaultColor = COLORS_CHARCOAL,
            garmentType = GarmentType.TEE,
            rating = 4.9f,
            reviewCount = 42
        )
    )

    fun getProductById(id: String): Product? = products.find { it.id == id }
    
    val categories = listOf("All", "Hoodies", "Outerwear", "T-Shirts", "Pants", "Accessories")
}
