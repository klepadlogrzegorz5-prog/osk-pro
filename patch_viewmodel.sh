sed -i 's/val pricePln:/val priceCoursePln: Double = 0.0,\n    val priceHourPln: Double = 0.0,/g' app/src/main/java/com/example/ui/components/StudentReservationModal.kt
sed -i 's/val priceType: String/val isActive: Boolean = true/g' app/src/main/java/com/example/ui/components/StudentReservationModal.kt
