package com.fliker.shiftscheduler.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fliker.shiftscheduler.R
import com.fliker.shiftscheduler.domain.model.ShiftType
import com.fliker.shiftscheduler.domain.model.WorkDay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    state: CalendarUiState,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onOverrideClick: (LocalDate, ShiftType) -> Unit,
    onClearOverride: (LocalDate) -> Unit,
    onSettingsClick: () -> Unit,
    availableShiftTypes: List<ShiftType>,
    modifier: Modifier = Modifier
) {
    var selectedDayForOverride by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Add, contentDescription = "Settings")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            CalendarHeader(
                yearMonth = state.yearMonth,
                onNextMonth = onNextMonth,
                onPreviousMonth = onPreviousMonth
            )

            Spacer(modifier = Modifier.height(16.dp))

            WeekDaysHeader()

            CalendarGrid(
                yearMonth = state.yearMonth,
                days = state.days,
                onDayClick = { selectedDayForOverride = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            MonthStatsCard(stats = state.stats)
        }
    }

    if (selectedDayForOverride != null) {
        OverrideDialog(
            date = selectedDayForOverride!!,
            onDismiss = { selectedDayForOverride = null },
            onSelectType = { type ->
                onOverrideClick(selectedDayForOverride!!, type)
                selectedDayForOverride = null
            },
            onClear = {
                onClearOverride(selectedDayForOverride!!)
                selectedDayForOverride = null
            },
            availableTypes = availableShiftTypes
        )
    }
}

@Composable
fun CalendarHeader(
    yearMonth: YearMonth,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
        }

        Text(
            text = yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                .replaceFirstChar { it.uppercase() } + " " + yearMonth.year,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
        }
    }
}

@Composable
fun WeekDaysHeader() {
    val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    days: List<WorkDay>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7
    
    val totalGridItems = days.size + dayOfWeekOffset

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(dayOfWeekOffset) {
            Spacer(modifier = Modifier.aspectRatio(1f))
        }
        items(days) { day ->
            DayCell(day = day, onClick = { onDayClick(day.date) })
        }
    }
}

@Composable
fun DayCell(day: WorkDay, onClick: () -> Unit) {
    val shiftType = day.shiftType
    val color = if (shiftType is ShiftType.Work) {
        Color(android.graphics.Color.parseColor(shiftType.colorHex))
    } else if (shiftType is ShiftType.Off) {
        Color.Transparent
    } else {
        Color.LightGray
    }

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = if (day.isCustomOverride) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (day.date == LocalDate.now()) FontWeight.ExtraBold else FontWeight.Normal,
                color = if (day.date == LocalDate.now()) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
            
            if (shiftType !is ShiftType.Off) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(6.dp)
                        .background(color, CircleShape)
                )
                Text(
                    text = shiftType.name.take(3),
                    fontSize = 8.sp,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MonthStatsCard(stats: MonthStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.stats_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = stringResource(R.string.stats_work_days, stats.totalWorkDays), fontSize = 14.sp)
                    Text(text = stringResource(R.string.stats_work_hours, stats.totalWorkHours), fontSize = 14.sp)
                }
                Column {
                    Text(text = stringResource(R.string.stats_day_shifts, stats.dayShifts), fontSize = 14.sp)
                    Text(text = stringResource(R.string.stats_night_shifts, stats.nightShifts), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun OverrideDialog(
    date: LocalDate,
    onDismiss: () -> Unit,
    onSelectType: (ShiftType) -> Unit,
    onClear: () -> Unit,
    availableTypes: List<ShiftType>
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.override_dialog_title) + " ${date.dayOfMonth}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableTypes.forEach { type ->
                    Button(
                        onClick = { onSelectType(type) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type is ShiftType.Work) Color(android.graphics.Color.parseColor(type.colorHex)) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (type is ShiftType.Work) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(type.name)
                    }
                }
                TextButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.override_clear), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {}
    )
}
