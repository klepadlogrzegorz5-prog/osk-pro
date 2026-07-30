with open("app/src/main/java/com/example/ui/components/StudentReservationModal.kt", "r") as f:
    content = f.read()

target = """                                val matchedPrice = categoryPrices.find { it.category == cat }
                                val priceText = matchedPrice?.let {
                                    val formattedPrice = String.format("%.0f", it.pricePln)
                                    if (it.priceType == "COURSE") "$formattedPrice zł" else "$formattedPrice zł/h"
                                } ?: "Zapytać"
                                val isCatActive = matchedPrice?.isActive ?: true"""

replacement = """                                val matchedPrice = categoryPrices.find { it.category == cat }
                                val priceText = matchedPrice?.let {
                                    "Kurs: ${String.format("%.0f", it.priceCoursePln)} / 1h: ${String.format("%.0f", it.priceHourPln)}"
                                } ?: "Zapytać"
                                val isCatActive = matchedPrice?.isActive ?: true"""

content = content.replace(target, replacement)
with open("app/src/main/java/com/example/ui/components/StudentReservationModal.kt", "w") as f:
    f.write(content)
