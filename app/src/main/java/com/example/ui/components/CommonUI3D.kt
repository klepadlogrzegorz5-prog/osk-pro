package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Neon-glowing white circle header with bold 'L' letter for driving school identity.
 * Requested by user: "Nagłówek na panelu głównym powinien zawierać dużą literę l w białym okręgu podświetloną neonowo"
 */
@Composable
fun NeonLHeaderCircle(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.testTag("neon_l_header_badge")
    ) {
        // Outer Neon Glow Aura
        Box(
            modifier = Modifier
                .size(size + 14.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF00F0FF),
                    spotColor = Color(0xFF2563EB)
                )
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00F0FF).copy(alpha = 0.6f),
                            Color(0xFF2563EB).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Neon Border Ring
        Box(
            modifier = Modifier
                .size(size + 4.dp)
                .clip(CircleShape)
                .border(
                    width = 2.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF00F0FF),
                            Color(0xFF38BDF8),
                            Color(0xFF60A5FA)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Inner Solid White Circle
        Box(
            modifier = Modifier
                .size(size)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFF1F5F9)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "L",
                fontSize = (size.value * 0.58).sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1D4ED8), // Driving School Blue 'L'
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-1).dp)
            )
        }
    }
}

/**
 * 3D-styled Icon Badge component giving realistic dimensional depth, multi-layered glossy gradients,
 * and high contrast to all app icons.
 */
@Composable
fun Icon3DBadge(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    iconSize: Dp = 24.dp,
    gradientColors: List<Color> = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)),
    iconTint: Color = Color.White
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp), ambientColor = gradientColors.first())
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = gradientColors
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        // Inner Bevel Soft Highlight
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Bottom footer component with company name and quote.
 * Requested by user: "na dole powinien być napis nazwą firmy oraz jakimś miłym cytatem"
 */
@Composable
fun FooterCompanyQuote(
    modifier: Modifier = Modifier,
    companyName: String = "proOsk Enterprise System • Oprogramowanie dla Szkół Jazdy",
    quoteText: String = "„Bezpieczeństwo na drodze zaczyna się od dobrego szkolenia. Szerokiej drogi i sukcesów na egzaminie z proOsk!”"
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("footer_company_quote_card"),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.4f),
                    Color(0xFF2563EB).copy(alpha = 0.6f),
                    Color(0xFF00F0FF).copy(alpha = 0.4f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                NeonLHeaderCircle(size = 32.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = companyName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = quoteText,
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "© 2026 proOsk • Wszelkie prawa zastrzeżone • Bezpieczna Chmura OSK",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}
