package com.speedcool.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.speedcool.app.data.Profile
import com.speedcool.app.ui.theme.*

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String = "",
    icon: @Composable () -> Unit = {},
    color: Color = Primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon
            Spacer(Modifier.height(8.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            if (unit.isNotEmpty()) {
                Text(text = unit, fontSize = 12.sp, color = OnSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(text = title, fontSize = 12.sp, color = OnSurfaceVariant)
        }
    }
}

@Composable
fun ProfileSelector(
    currentProfile: Profile,
    onProfileSelected: (Profile) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Perfil de Otimização", color = OnSurfaceVariant, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Profile.entries.take(3).forEach { profile ->
                val isSelected = profile == currentProfile
                val bgColor by animateColorAsState(
                    targetValue = when {
                        isSelected && profile == Profile.ECO -> EcoColor
                        isSelected && profile == Profile.BALANCED -> BalancedColor
                        isSelected && profile == Profile.PERFORMANCE -> PerformanceColor
                        else -> SurfaceVariant
                    },
                    animationSpec = tween(300, easing = LinearEasing)
                )
                Card(
                    onClick = { onProfileSelected(profile) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = profile.displayName,
                            color = if (isSelected) Color.White else OnSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TempGauge(temp: Int, modifier: Modifier = Modifier) {
    val color = when {
        temp < 45 -> TempNormal
        temp < 65 -> TempWarm
        else -> TempHot
    }
    val pct = (temp.toFloat() / 100f).coerceIn(0f, 1f)

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Temperatura", color = OnSurfaceVariant, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(120.dp)) {
                drawArc(
                    color = Color(0xFF333333),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12f)
                )
                drawArc(
                    color = color,
                    startAngle = 135f,
                    sweepAngle = 270f * pct,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$temp", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = color)
                Text("°C", fontSize = 14.sp, color = OnSurfaceVariant)
            }
        }
    }
}

@Composable
fun RamBar(usagePercent: Int, modifier: Modifier = Modifier) {
    val color = when {
        usagePercent < 60 -> TempNormal
        usagePercent < 85 -> TempWarm
        else -> TempHot
    }
    Column(modifier = modifier) {
        Text("RAM", color = OnSurfaceVariant, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box {
            Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                drawRoundRect(Color(0xFF333333), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f))
                drawRoundRect(
                    color = color,
                    size = Size(size.width * (usagePercent / 100f), size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)
                )
            }
            Text(
                "$usagePercent%",
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
