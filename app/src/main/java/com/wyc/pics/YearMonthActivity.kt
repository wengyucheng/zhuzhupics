package com.wyc.pics

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyc.pics.ui.theme.PicsTheme
import java.time.YearMonth

class YearMonthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PicsTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    YearMonthScreen { yearMonth ->
                        val intent = Intent(this@YearMonthActivity, MainActivity::class.java).apply {
                            putExtra("year", yearMonth.year)
                            putExtra("month", yearMonth.monthValue)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun YearMonthScreen(
    modifier: Modifier = Modifier,
    onMonthSelect: (YearMonth) -> Unit
) {
    var currentYear by remember { mutableStateOf(YearMonth.now().year) }
    var showYearPicker by remember { mutableStateOf(false) }
    val months = (1..12).toList()
    val monthNames = listOf("一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月")

    val handlePrevYear = {
        if (currentYear > 2020) {
            currentYear--
        }
    }

    val handleNextYear = {
        if (currentYear < 2100) {
            currentYear++
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "上一年",
                modifier = Modifier
                    .size(36.dp)
                    .clickable(enabled = currentYear > 2020) { handlePrevYear() },
                tint = if (currentYear > 2020) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showYearPicker = true }
            ) {
                Text(
                    text = "${currentYear}年",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "选择年份",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "下一年",
                modifier = Modifier
                    .size(36.dp)
                    .clickable(enabled = currentYear < 2100) { handleNextYear() },
                tint = if (currentYear < 2100) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.width(360.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(months.size) { index ->
                val month = months[index]
                val yearMonth = YearMonth.of(currentYear, month)

                Card(
                    modifier = Modifier
                        .width(100.dp)
                        .height(100.dp)
                        .clickable { onMonthSelect(yearMonth) },
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = monthNames[index],
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    if (showYearPicker) {
        YearPickerDialog(
            initialYear = currentYear,
            onConfirm = { year ->
                currentYear = year
                showYearPicker = false
            },
            onCancel = {
                showYearPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearPickerDialog(
    initialYear: Int,
    onConfirm: (Int) -> Unit,
    onCancel: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    val minYear = 2020
    val maxYear = 2100

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = "选择年份") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .width(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        state = rememberLazyListState(
                            initialFirstVisibleItemIndex = selectedYear - minYear
                        )
                    ) {
                        items((minYear..maxYear).toList()) { year ->
                            val isSelected = year == selectedYear
                            Text(
                                text = "${year}年",
                                fontSize = if (isSelected) 28.sp else 18.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedYear = year }
                                    .padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedYear)
            }) {
                Text(text = "确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = "取消")
            }
        }
    )
}