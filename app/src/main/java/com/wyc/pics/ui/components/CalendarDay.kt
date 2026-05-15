package com.wyc.pics.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.wyc.pics.R

data class CalendarDayData(
    val day: Int,
    val imagePath: String? = null,
    val isCurrentMonth: Boolean = true
)

@Composable
fun CalendarDay(
    dayData: CalendarDayData,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(
                width = 1.dp,
                color = if (dayData.isCurrentMonth) Color.LightGray.copy(alpha = 0.3f) else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (dayData.isCurrentMonth) {
            if (dayData.imagePath != null) {
                Image(
                    painter = rememberImagePainter(dayData.imagePath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_defalut),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(
            text = if (dayData.isCurrentMonth) dayData.day.toString() else "",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (dayData.isCurrentMonth) Color.Black else Color.Transparent,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Preview
@Composable
fun CalendarDayPreview() {
    MaterialTheme {
        CalendarDay(
            dayData = CalendarDayData(
                day = 15,
                imagePath = null,
                isCurrentMonth = true
            )
        )
    }
}