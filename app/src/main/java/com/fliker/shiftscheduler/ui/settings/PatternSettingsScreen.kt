package com.fliker.shiftscheduler.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fliker.shiftscheduler.R
import com.fliker.shiftscheduler.domain.model.ShiftType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternSettingsScreen(
    state: PatternSettingsUiState,
    onNameChange: (String) -> Unit,
    onStartDateChange: (LocalDate) -> Unit,
    onAddShift: (ShiftType) -> Unit,
    onRemoveShift: (Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.pattern_settings_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.pattern_name_label)) },
            placeholder = { Text(stringResource(R.string.pattern_name_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            onValueChange = {},
            label = { Text(stringResource(R.string.start_date_label)) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                            onStartDateChange(date)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.add_shift_label),
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.availableTypes.forEach { type ->
                InputChip(
                    selected = false,
                    onClick = { onAddShift(type) },
                    label = { Text(type.name) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = if (type is ShiftType.Work) Color(android.graphics.Color.parseColor(type.colorHex)) else Color.Gray,
                                    shape = CircleShape
                                )
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.current_pattern_label),
            style = MaterialTheme.typography.titleMedium
        )

        if (state.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_pattern_hint),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(state.items) { index, type ->
                    ShiftItem(
                        type = type,
                        onRemove = { onRemoveShift(index) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.name.isNotBlank() && state.items.isNotEmpty(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.save_button))
        }
    }
}

@Composable
fun ShiftItem(
    type: ShiftType,
    onRemove: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (type is ShiftType.Work) Color(android.graphics.Color.parseColor(type.colorHex)) else Color.Gray,
                        shape = CircleShape
                    )
            )
            Text(
                text = type.name,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PatternSettingsScreenPreview() {
    val state = PatternSettingsUiState(
        name = "2 через 2",
        startDate = LocalDate.of(2024, 1, 1),
        items = listOf(
            ShiftType.Work("1", "Дневная", java.time.LocalTime.of(8, 0), java.time.LocalTime.of(20, 0), "#FFEB3B"),
            ShiftType.Work("2", "Дневная", java.time.LocalTime.of(8, 0), java.time.LocalTime.of(20, 0), "#FFEB3B"),
            ShiftType.Off,
            ShiftType.Off
        ),
        availableTypes = listOf(
            ShiftType.Work("1", "Дневная", java.time.LocalTime.of(8, 0), java.time.LocalTime.of(20, 0), "#FFEB3B"),
            ShiftType.Work("3", "Ночная", java.time.LocalTime.of(20, 0), java.time.LocalTime.of(8, 0), "#3F51B5"),
            ShiftType.Off
        )
    )
    MaterialTheme {
        PatternSettingsScreen(
            state = state,
            onNameChange = {},
            onStartDateChange = {},
            onAddShift = {},
            onRemoveShift = {},
            onSave = {}
        )
    }
}
