cat << 'INNER' > script3.py
with open("app/src/main/java/com/example/ui/screens/FinanceScreen.kt", "r") as f:
    content = f.read()

target = """                        val priceFormatted = String.format("%.0f zł", it.pricePln)
                        val text = "${it.category} - ${if(it.priceType == "COURSE") "Kurs" else "Godzina"} ($priceFormatted)"
                        Text("• $text", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))"""

replacement = """                        val text = "${it.category} - Kurs: ${String.format("%.0f zł", it.priceCoursePln)} / 1h: ${String.format("%.0f zł", it.priceHourPln)}"
                        Text("• $text", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))"""

content = content.replace(target, replacement)
with open("app/src/main/java/com/example/ui/screens/FinanceScreen.kt", "w") as f:
    f.write(content)
INNER
python3 script3.py
