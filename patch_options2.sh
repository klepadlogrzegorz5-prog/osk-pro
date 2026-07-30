sed -i 's/if (pr.priceType == "COURSE") "Za cały kurs" else "Za godzinę jazdy"/"Kurs: ${String.format("%.0f zł", pr.priceCoursePln)} | 1h: ${String.format("%.0f zł", pr.priceHourPln)}"/g' app/src/main/java/com/example/ui/screens/OptionsScreen.kt
sed -i '/String.format("%.0f zł", pr.pricePln)/d' app/src/main/java/com/example/ui/screens/OptionsScreen.kt
sed -i '/color = Color(0xFF10B981),/d' app/src/main/java/com/example/ui/screens/OptionsScreen.kt
sed -i '/fontWeight = FontWeight.Black,/d' app/src/main/java/com/example/ui/screens/OptionsScreen.kt
sed -i '/fontSize = 16.sp/d' app/src/main/java/com/example/ui/screens/OptionsScreen.kt
