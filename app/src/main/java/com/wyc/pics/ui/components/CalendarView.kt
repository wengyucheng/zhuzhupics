package com.wyc.pics.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyc.pics.utils.ImageUtils
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    onDayClick: (LocalDate) -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
    
    val handlePrevMonth = { currentMonth = currentMonth.minusMonths(1) }
    val handleNextMonth = { currentMonth = currentMonth.plusMonths(1) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "上一月",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { handlePrevMonth() }
            )
            
            Text(
                text = "${currentMonth.year}年${currentMonth.monthValue}月",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable { showDatePicker = true }
            )
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "下一月",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { handleNextMonth() }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            weekDays.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            content = {
                itemsIndexed(generateCalendarData(currentMonth)) { index, dayData ->
                    val dayOfMonth = index - currentMonth.atDay(1).dayOfWeek.value % 7 + 1
                    val date = if (dayData.isCurrentMonth) {
                        currentMonth.atDay(dayOfMonth)
                    } else {
                        null
                    }
                    CalendarDay(
                        dayData = dayData,
                        onClick = {
                            date?.let { onDayClick(it) }
                        }
                    )
                }
            }
        )
    }
    
    if (showDatePicker) {
        YearMonthPickerDialog(
            initialYearMonth = currentMonth,
            onConfirm = { yearMonth ->
                currentMonth = yearMonth
                showDatePicker = false
            },
            onCancel = {
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearMonthPickerDialog(
    initialYearMonth: YearMonth,
    onConfirm: (YearMonth) -> Unit,
    onCancel: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialYearMonth.year) }
    var selectedMonth by remember { mutableStateOf(initialYearMonth.monthValue) }
    
    val years = (1900..2100).toList()
    val months = (1..12).toList()
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = "选择年月") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(100.dp)) {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.height(150.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(years) { year ->
                                Text(
                                    text = "${year}年",
                                    fontSize = if (year == selectedYear) 20.sp else 16.sp,
                                    fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal,
                                    color = if (year == selectedYear) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedYear = year }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Box(modifier = Modifier.width(80.dp)) {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.height(150.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(months) { month ->
                                Text(
                                    text = "${month}月",
                                    fontSize = if (month == selectedMonth) 20.sp else 16.sp,
                                    fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                    color = if (month == selectedMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedMonth = month }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(YearMonth.of(selectedYear, selectedMonth))
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

fun generateCalendarData(yearMonth: YearMonth): List<CalendarDayData> {
    val data = mutableListOf<CalendarDayData>()
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7
    
    for (i in 0 until firstDayOfWeek) {
        data.add(CalendarDayData(day = 0, imagePath = null, isCurrentMonth = false))
    }
    
    for (day in 1..daysInMonth) {
        val date = yearMonth.atDay(day)
        val imagePath = getFirstImageForDate(date)
        data.add(CalendarDayData(day = day, imagePath = imagePath, isCurrentMonth = true))
    }
    
    while (data.size < 42) {
        data.add(CalendarDayData(day = 0, imagePath = null, isCurrentMonth = false))
    }
    
    return data
}

fun getFirstImageForDate(date: LocalDate): String? {
    val dir = ImageUtils.getImageDirectory(date)
    if (dir.exists()) {
        val files = dir.listFiles()?.filter { it.extension.equals("png", ignoreCase = true) }
        return files?.firstOrNull()?.absolutePath
    }
    return null
}

@Preview(showBackground = true)
@Composable
fun CalendarViewPreview() {
    MaterialTheme {
        CalendarView(modifier = Modifier)
    }
}