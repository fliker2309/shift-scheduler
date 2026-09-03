package com.fliker.shiftscheduler.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fliker.shiftscheduler.R
import com.fliker.shiftscheduler.data.local.UserPreferences
import com.fliker.shiftscheduler.domain.model.ShiftType
import com.fliker.shiftscheduler.domain.model.WorkDay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

private const val INITIAL_PAGE = 500

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    state: CalendarUiState,
    preferences: UserPreferences?,
    onMonthUpdate: (YearMonth) -> Unit,
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
    
    val initialYearMonth = remember { YearMonth.now() }
    val pagerState = rememberPagerState(initialPage = INITIAL_PAGE, pageCount = { 1000 })

    // PERFORMANCE: Sync month title with current page (instant feedback)
    val displayedMonth by remember {
        derivedStateOf {
            initialYearMonth.plusMonths((pagerState.currentPage - INITIAL_PAGE).toLong())
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.7f)) {
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
                                Text(text = pattern.name, modifier = Modifier.weight(1f), maxLines = 1)
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
            // PERFORMANCE: Load data only when swipe settles
            LaunchedEffect(pagerState.settledPage) {
                val offset = pagerState.settledPage - INITIAL_PAGE
                onMonthUpdate(initialYearMonth.plusMonths(offset.toLong()))
            }

            // External sync (e.g. arrow buttons)
            LaunchedEffect(state.yearMonth) {
                val offset = (state.yearMonth.year - initialYearMonth.year) * 12 + (state.yearMonth.monthValue - initialYearMonth.monthValue)
                val targetPage = INITIAL_PAGE + offset
                if (pagerState.currentPage != targetPage) {
                    pagerState.animateScrollToPage(targetPage)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                CalendarMonthPicker(
                    yearMonth = displayedMonth,
                    onNextMonth = { 
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    onPreviousMonth = { 
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                val showWeekNumbers = preferences?.showWeekNumbers ?: false
                val dimWeekends = preferences?.dimWeekends ?: false
                
                val surfaceColor = MaterialTheme.colorScheme.surface
                val onSurface = MaterialTheme.colorScheme.onSurface
                val outlineVariant = MaterialTheme.colorScheme.outlineVariant
                val primaryColor = MaterialTheme.colorScheme.primary

                Row(modifier = Modifier.fillMaxWidth()) {
                    if (showWeekNumbers) {
                        Column(
                            modifier = Modifier
                                .width(40.dp)
                                .padding(top = 32.dp)
                        ) {
                            val currentMonthDays = state.monthData[displayedMonth]
                            val weeks = remember(currentMonthDays) { currentMonthDays?.chunked(7) ?: emptyList() }
                            
                            repeat(6) { index ->
                                val week = weeks.getOrNull(index)
                                val weekNumber = remember(week) {
                                    week?.firstOrNull()?.date?.get(WeekFields.of(Locale.getDefault()).weekOfYear()) ?: ""
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(surfaceColor.copy(alpha = 0.5f))
                                        .border(0.5.dp, outlineVariant.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (weekNumber != "") {
                                        Text(
                                            text = weekNumber.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val daysOfWeek = remember { listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс") }
                            daysOfWeek.forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth().aspectRatio(7f/6f),
                            beyondBoundsPageCount = 1
                        ) { page ->
                            val pageMonth = remember(page) { initialYearMonth.plusMonths((page - INITIAL_PAGE).toLong()) }
                            val days = state.monthData[pageMonth]
                            
                            StaticCalendarGrid(
                                days = days,
                                pageMonth = pageMonth,
                                dimWeekends = dimWeekends,
                                onDayClick = { selectedDayForOverride = it },
                                surfaceColor = surfaceColor,
                                onSurface = onSurface,
                                outlineVariant = outlineVariant
                            )
                        }
                    }
                }

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
            text = { Text(stringResource(R.string.delete_pattern_dialog_message, patternToDelete!!.name)) },
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
fun StaticCalendarGrid(
    days: List<WorkDay>?,
    pageMonth: YearMonth,
    dimWeekends: Boolean,
    onDayClick: (LocalDate) -> Unit,
    surfaceColor: Color,
    onSurface: Color,
    outlineVariant: Color
) {
    val weeks = remember(days) { days?.chunked(7) ?: emptyList() }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, outlineVariant)
    ) {
        repeat(6) { weekIndex ->
            Row(modifier = Modifier.fillMaxWidth()) {
                val week = weeks.getOrNull(weekIndex)
                repeat(7) { dayIndex ->
                    val day = week?.getOrNull(dayIndex)
                    Box(modifier = Modifier.weight(1f)) {
                        if (day != null) {
                            DayCell(
                                day = day,
                                isCurrentMonth = YearMonth.from(day.date) == pageMonth,
                                dimWeekends = dimWeekends,
                                onClick = onDayClick,
                                surfaceColor = surfaceColor,
                                onSurface = onSurface,
                                outlineVariant = outlineVariant
                            )
                        } else {
                            // Ghost cell to maintain grid while loading
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .background(surfaceColor)
                                    .border(0.5.dp, outlineVariant.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: WorkDay,
    isCurrentMonth: Boolean,
    dimWeekends: Boolean,
    onClick: (LocalDate) -> Unit,
    surfaceColor: Color,
    onSurface: Color,
    outlineVariant: Color
) {
    val shiftType = day.shiftType
    val isToday = remember(day.date) { day.date == LocalDate.now() }
    val dayText = remember(day.date) { day.date.dayOfMonth.toString() }
    
    val backgroundColor = remember(shiftType, surfaceColor) {
        when (shiftType) {
            is ShiftType.Work -> Color(shiftType.colorInt)
            is ShiftType.Off -> surfaceColor
            is ShiftType.Vacation -> Color(0xFF4CAF50)
            is ShiftType.SickLeave -> Color(0xFFF44336)
        }
    }

    val isWeekend = remember(day.date) { day.date.dayOfWeek.value >= 6 }
    val alpha = remember(isCurrentMonth, dimWeekends, isWeekend, shiftType) {
        if (!isCurrentMonth) 0.4f 
        else if (dimWeekends && isWeekend && shiftType is ShiftType.Off) 0.6f 
        else 1f
    }

    val textColor = remember(shiftType, isToday, onSurface) {
        if (shiftType is ShiftType.Work || shiftType is ShiftType.Vacation || shiftType is ShiftType.SickLeave) 
            Color.White 
        else 
            onSurface
    }

    // PERFORMANCE: Use drawBehind for background and borders to flatten hierarchy
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer { this.alpha = alpha }
            .drawBehind {
                drawRect(color = backgroundColor)
                
                // Borders
                if (isToday) {
                    drawRect(
                        color = Color.White,
                        style = Stroke(width = 2.dp.toPx())
                    )
                } else if (day.isCustomOverride) {
                    drawRect(
                        color = Color.White.copy(alpha = 0.5f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                } else {
                    drawRect(
                        color = outlineVariant.copy(alpha = 0.5f),
                        style = Stroke(width = 0.5.dp.toPx())
                    )
                }
            }
            .clickable(onClick = { onClick(day.date) }),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
            color = textColor
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
                    val color = remember(type) { 
                        if (type is ShiftType.Work) Color(type.colorInt) else Color.Transparent 
                    }
                    Button(
                        onClick = { onSelectType(type) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type is ShiftType.Work) color else MaterialTheme.colorScheme.surfaceVariant,
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
