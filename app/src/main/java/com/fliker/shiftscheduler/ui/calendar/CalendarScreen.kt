package com.fliker.shiftscheduler.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fliker.shiftscheduler.R
import com.fliker.shiftscheduler.domain.model.ShiftType
import com.fliker.shiftscheduler.domain.model.WorkDay
import com.fliker.shiftscheduler.data.local.UserPreferences
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    state: CalendarUiState,
    preferences: UserPreferences?,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onOverrideClick: (LocalDate, ShiftType) -> Unit,
    onClearOverride: (LocalDate) -> Unit,
    onSettingsClick: () -> Unit,
    onUiSettingsClick: () -> Unit,
    onSelectPattern: (Long) -> Unit,
    onDeletePattern: (Long) -> Unit,
    availableShiftTypes: List<ShiftType>,
    modifier: Modifier = Modifier
) {
    var selectedDayForOverride by remember { mutableStateOf<LocalDate?>(null) }
    var patternToDelete by remember { mutableStateOf<com.fliker.shiftscheduler.domain.model.ShiftPattern?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                Text(
                    text = "Ваши графики",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                state.patterns.forEach { pattern ->
                    NavigationDrawerItem(
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pattern.name,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                if (state.patterns.size > 1) {
                                    IconButton(
                                        onClick = { patternToDelete = pattern },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        },
                        selected = pattern.id == state.selectedPattern?.id,
                        onClick = {
                            onSelectPattern(pattern.id)
                            scope.launch { drawerState.close() }
                        },
                        icon = {
                            if (pattern.id == state.selectedPattern?.id) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                NavigationDrawerItem(
                    label = { Text("Добавить график") },
                    selected = false,
                    onClick = {
                        onSettingsClick()
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Настройки") },
                    selected = false,
                    onClick = {
                        onUiSettingsClick()
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = state.selectedPattern?.name ?: stringResource(R.string.app_name),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Add, contentDescription = "Settings")
                }
            }
        ) { innerPadding ->
            val pagerState = rememberPagerState(initialPage = 500, pageCount = { 1000 })
            val initialYearMonth = remember { YearMonth.now() }

            LaunchedEffect(state.yearMonth) {
                val targetPage = 500 + (state.yearMonth.year - initialYearMonth.year) * 12 + (state.yearMonth.monthValue - initialYearMonth.monthValue)
                if (pagerState.currentPage != targetPage) {
                    pagerState.animateScrollToPage(targetPage)
                }
            }

            LaunchedEffect(pagerState.currentPage) {
                val offset = pagerState.currentPage - 500
                val targetMonth = initialYearMonth.plusMonths(offset.toLong())
                if (targetMonth != state.yearMonth) {
                    if (targetMonth.isAfter(state.yearMonth)) onNextMonth()
                    else onPreviousMonth()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                WeekDaysHeader(showWeekNumbers = preferences?.showWeekNumbers ?: false)

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f, fill = false)
                ) { page ->
                    val offset = page - 500
                    val pageMonth = initialYearMonth.plusMonths(offset.toLong())
                    
                    if (pageMonth == state.yearMonth) {
                        CalendarGrid(
                            yearMonth = state.yearMonth,
                            days = state.days,
                            showWeekNumbers = preferences?.showWeekNumbers ?: false,
                            dimWeekends = preferences?.dimWeekends ?: false,
                            onDayClick = { selectedDayForOverride = it }
                        )
                    } else {
                        Box(Modifier.fillMaxWidth().aspectRatio(7f/6f)) 
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                CalendarMonthPicker(
                    yearMonth = state.yearMonth,
                    onNextMonth = onNextMonth,
                    onPreviousMonth = onPreviousMonth
                )

                Spacer(modifier = Modifier.height(24.dp))

                MonthStatsCard(stats = state.stats)
            }
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

    if (patternToDelete != null) {
        AlertDialog(
            onDismissRequest = { patternToDelete = null },
            title = { Text(stringResource(R.string.delete_pattern_dialog_title)) },
            text = { 
                Text(stringResource(R.string.delete_pattern_dialog_message, patternToDelete!!.name)) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePattern(patternToDelete!!.id)
                        patternToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { patternToDelete = null }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}

@Composable
fun CalendarMonthPicker(
    yearMonth: YearMonth,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
        }

        Text(
            text = yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                .replaceFirstChar { it.uppercase() } + " " + yearMonth.year,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
        }
    }
}

@Composable
fun WeekDaysHeader(showWeekNumbers: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        if (showWeekNumbers) {
            Spacer(modifier = Modifier.width(32.dp))
        }
        val daysOfWeek = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    days: List<WorkDay>,
    showWeekNumbers: Boolean,
    dimWeekends: Boolean,
    onDayClick: (LocalDate) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        if (showWeekNumbers) {
            Column(
                modifier = Modifier.width(32.dp).padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val weeks = days.chunked(7)
                weeks.forEach { week ->
                    val weekNumber = week.firstOrNull()?.date?.get(WeekFields.of(Locale.getDefault()).weekOfYear()) ?: ""
                    Box(modifier = Modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = weekNumber.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .weight(1f)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(days) { day ->
                DayCell(
                    day = day,
                    isCurrentMonth = YearMonth.from(day.date) == yearMonth,
                    dimWeekends = dimWeekends,
                    onClick = { onDayClick(day.date) }
                )
            }
        }
    }
}

@Composable
fun DayCell(
    day: WorkDay,
    isCurrentMonth: Boolean,
    dimWeekends: Boolean,
    onClick: () -> Unit
) {
    val shiftType = day.shiftType
    val backgroundColor = when (shiftType) {
        is ShiftType.Work -> Color(android.graphics.Color.parseColor(shiftType.colorHex))
        is ShiftType.Off -> MaterialTheme.colorScheme.surface
        is ShiftType.Vacation -> Color(0xFF4CAF50)
        is ShiftType.SickLeave -> Color(0xFFF44336)
    }

    val isWeekend = day.date.dayOfWeek.value >= 6
    val isToday = day.date == LocalDate.now()
    
    val alpha = if (!isCurrentMonth) 0.4f 
                else if (dimWeekends && isWeekend && shiftType is ShiftType.Off) 0.6f 
                else 1f

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .alpha(alpha),
        shape = RectangleShape,
        color = backgroundColor,
        border = if (isToday) BorderStroke(2.dp, Color.White) 
                 else if (day.isCustomOverride) BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                 else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (shiftType is ShiftType.Work || shiftType is ShiftType.Vacation || shiftType is ShiftType.SickLeave) 
                            Color.White 
                        else 
                            MaterialTheme.colorScheme.onSurface
            )
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
