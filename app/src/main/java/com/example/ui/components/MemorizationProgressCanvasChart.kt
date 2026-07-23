package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReadingGoalEntity

data class DailyMemorizationDataPoint(
    val dayLabelAr: String,
    val dayLabelEn: String,
    val pagesOrAyahsCount: Float
)

@Composable
fun MemorizationProgressCanvasChart(
    readingGoal: ReadingGoalEntity?,
    isEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    val sampleData = remember {
        listOf(
            DailyMemorizationDataPoint("سبت", "Sat", 3f),
            DailyMemorizationDataPoint("أحد", "Sun", 5f),
            DailyMemorizationDataPoint("إثنين", "Mon", 4f),
            DailyMemorizationDataPoint("ثلاثاء", "Tue", 8f),
            DailyMemorizationDataPoint("أربعاء", "Wed", 6f),
            DailyMemorizationDataPoint("خميس", "Thu", 10f),
            DailyMemorizationDataPoint("جمعة", "Fri", 12f)
        )
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200)
        )
    }

    val streakDays = readingGoal?.currentStreakDays ?: 7
    val pagesCompleted = readingGoal?.pagesCompleted ?: 42
    val targetDays = readingGoal?.targetDays ?: 30

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(primaryColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Chart",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isEnglish) "Memorization & Reading Chart" else "مخطط الحفظ والرصيد اليومي",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = if (isEnglish) "Daily pages & streak analytics" else "تحليل عدد الصفحات والتتابع اليومي",
                            fontSize = 11.sp,
                            color = onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Streak Badge
                Surface(
                    color = Color(0xFFFF9800).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak Flame",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEnglish) "$streakDays Days Streak" else "تتابع $streakDays أيام 🔥",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val stepX = width / (sampleData.size - 1)
                                val index = ((offset.x + stepX / 2) / stepX).toInt().coerceIn(0, sampleData.size - 1)
                                selectedIndex = if (selectedIndex == index) null else index
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height - 30.dp.toPx()
                    val maxVal = 15f
                    val stepX = width / (sampleData.size - 1)

                    // Draw Horizontal Grid Lines
                    val gridLineCount = 3
                    for (i in 0..gridLineCount) {
                        val y = height * (i.toFloat() / gridLineCount)
                        drawLine(
                            color = surfaceVariant.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Build Line & Area Paths
                    val linePath = Path()
                    val areaPath = Path()

                    val points = sampleData.mapIndexed { index, item ->
                        val x = index * stepX
                        val normalizedY = (item.pagesOrAyahsCount / maxVal) * animationProgress.value
                        val y = height - (normalizedY * height)
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        linePath.moveTo(points.first().x, points.first().y)
                        areaPath.moveTo(points.first().x, height)
                        areaPath.lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val controlPoint1 = Offset(p1.x + stepX / 2f, p1.y)
                            val controlPoint2 = Offset(p1.x + stepX / 2f, p2.y)

                            linePath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p2.x, p2.y
                            )
                            areaPath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p2.x, p2.y
                            )
                        }

                        areaPath.lineTo(points.last().x, height)
                        areaPath.close()

                        // Draw Area Gradient
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.35f), primaryColor.copy(alpha = 0.02f)),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // Draw Main Line
                        drawPath(
                            path = linePath,
                            color = primaryColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw Data Points & Highlights
                        points.forEachIndexed { idx, pt ->
                            val isSel = selectedIndex == idx
                            drawCircle(
                                color = if (isSel) Color.White else primaryColor,
                                radius = if (isSel) 7.dp.toPx() else 4.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = if (isSel) secondaryColor else primaryColor,
                                radius = if (isSel) 5.dp.toPx() else 3.dp.toPx(),
                                center = pt,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }

                // Day Labels at Bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    sampleData.forEachIndexed { idx, item ->
                        val isSel = selectedIndex == idx
                        Text(
                            text = if (isEnglish) item.dayLabelEn else item.dayLabelAr,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) primaryColor else onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Selected Point Tooltip
            selectedIndex?.let { idx ->
                val item = sampleData[idx]
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = primaryColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnglish) "Day: ${item.dayLabelEn}" else "اليوم: ${item.dayLabelAr}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor
                        )
                        Text(
                            text = if (isEnglish) "${item.pagesOrAyahsCount.toInt()} Pages Memorized" else "${item.pagesOrAyahsCount.toInt()} صفحة تم حفظها 📖",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Stats Row (Canvas Progress Ring)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress Arc in Canvas
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 5.dp.toPx()
                        drawArc(
                            color = surfaceVariant,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(listOf(primaryColor, secondaryColor)),
                            startAngle = -90f,
                            sweepAngle = (pagesCompleted.toFloat() / 604f * 360f).coerceIn(10f, 360f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${((pagesCompleted.toFloat() / 604f) * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                Column {
                    Text(
                        text = if (isEnglish) "Total Completed" else "إجمالي المنجز الحفظ",
                        fontSize = 11.sp,
                        color = onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (isEnglish) "$pagesCompleted / 604 Pages" else "$pagesCompleted من 604 صفحة",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp),
                    color = surfaceVariant
                )

                Column {
                    Text(
                        text = if (isEnglish) "Target Khatma" else "هدف الختمة",
                        fontSize = 11.sp,
                        color = onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (isEnglish) "$targetDays Days Plan" else "خطة $targetDays يوماً",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor
                    )
                }
            }
        }
    }
}
