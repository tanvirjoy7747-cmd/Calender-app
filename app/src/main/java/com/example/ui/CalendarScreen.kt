package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CalendarDateUtils
import com.example.CalendarDay
import com.example.CalendarEvent
import com.example.CalendarViewMode
import com.example.CalendarViewModel
import com.example.EventCategory
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars),
        topBar = {
            CalendarTopBar(
                monthYearText = CalendarDateUtils.formatMonthYear(uiState.currentMonth),
                viewMode = uiState.viewMode,
                isSearchActive = uiState.isSearchActive,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onTodayClick = { viewModel.jumpToToday() },
                onToggleViewMode = {
                    viewModel.setViewMode(
                        if (uiState.viewMode == CalendarViewMode.MONTH) CalendarViewMode.AGENDA
                        else CalendarViewMode.MONTH
                    )
                },
                onToggleSearch = { viewModel.toggleSearchActive(!uiState.isSearchActive) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddEventDialog(uiState.selectedDate) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_event_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Event"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field (if active)
            AnimatedVisibility(visible = uiState.isSearchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search events, titles, locations...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input")
                    )
                }
            }

            // Category Filter Pills
            CategoryFilterBar(
                selectedCategory = uiState.selectedCategory,
                onSelectCategory = { viewModel.setCategoryFilter(it) }
            )

            // Content: Month View vs Agenda View
            if (uiState.viewMode == CalendarViewMode.MONTH) {
                MonthViewContent(
                    days = uiState.daysInMonth,
                    selectedDate = uiState.selectedDate,
                    eventsForDate = uiState.selectedDateEvents,
                    onDayClick = { viewModel.selectDate(it) },
                    onEditEvent = { viewModel.openEditEventDialog(it) },
                    onDeleteEvent = { viewModel.promptDeleteEvent(it) },
                    onAddEventClick = { viewModel.openAddEventDialog(uiState.selectedDate) }
                )
            } else {
                AgendaViewContent(
                    events = uiState.filteredAgendaEvents,
                    onEditEvent = { viewModel.openEditEventDialog(it) },
                    onDeleteEvent = { viewModel.promptDeleteEvent(it) },
                    onAddEventClick = { viewModel.openAddEventDialog(uiState.selectedDate) }
                )
            }
        }
    }

    // Add or Edit Dialog
    if (uiState.isAddEditOpen) {
        AddEditEventDialog(
            initialEvent = uiState.eventBeingEdited,
            defaultDate = uiState.selectedDate,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { title, desc, date, start, end, allDay, cat, loc ->
                viewModel.saveEvent(title, desc, date, start, end, allDay, cat, loc)
            }
        )
    }

    // Delete Confirmation Dialog
    uiState.eventPendingDelete?.let { event ->
        DeleteConfirmationDialog(
            event = event,
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.cancelDelete() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopBar(
    monthYearText: String,
    viewMode: CalendarViewMode,
    isSearchActive: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    onToggleViewMode: () -> Unit,
    onToggleSearch: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Calendar",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = monthYearText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            // Previous & Next month arrows
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier.testTag("prev_month_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month"
                )
            }

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier.testTag("next_month_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month"
                )
            }

            // Quick "Today" jump button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("today_quick_btn")
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onTodayClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Today",
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Today",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Toggle view mode (Month vs Agenda)
            IconButton(
                onClick = onToggleViewMode,
                modifier = Modifier.testTag("toggle_view_btn")
            ) {
                Icon(
                    imageVector = if (viewMode == CalendarViewMode.MONTH) Icons.Default.ViewAgenda else Icons.Default.CalendarMonth,
                    contentDescription = if (viewMode == CalendarViewMode.MONTH) "Switch to Agenda View" else "Switch to Month View"
                )
            }

            // Search toggle
            IconButton(
                onClick = onToggleSearch,
                modifier = Modifier.testTag("search_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Events",
                    tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun CategoryFilterBar(
    selectedCategory: EventCategory?,
    onSelectCategory: (EventCategory?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onSelectCategory(null) },
            label = { Text("All", fontSize = 12.sp) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("filter_all")
        )

        EventCategory.values().forEach { cat ->
            val isSelected = selectedCategory == cat
            val catColor = Color(cat.colorValue)

            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(cat) },
                label = { Text(cat.displayName, fontSize = 12.sp) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = catColor.copy(alpha = 0.18f),
                    selectedLabelColor = catColor
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("filter_${cat.name.lowercase()}")
            )
        }
    }
}

@Composable
private fun MonthViewContent(
    days: List<CalendarDay>,
    selectedDate: LocalDate,
    eventsForDate: List<CalendarEvent>,
    onDayClick: (LocalDate) -> Unit,
    onEditEvent: (CalendarEvent) -> Unit,
    onDeleteEvent: (CalendarEvent) -> Unit,
    onAddEventClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Weekday Headers (SUN, MON, ...)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        CalendarDateUtils.getDayOfWeekHeaders().forEach { header ->
                            Text(
                                text = header,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Calendar Grid Rows (chunks of 7 days)
                    val weeks = days.chunked(7)
                    weeks.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { day ->
                                DayCell(
                                    day = day,
                                    onDayClick = onDayClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Agenda Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = CalendarDateUtils.formatHeaderDate(selectedDate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${eventsForDate.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Events for the selected date
        if (eventsForDate.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "No events",
                            modifier = Modifier.size(44.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No events scheduled",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap + to plan an event or reminder for this day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onAddEventClick,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Event",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Event")
                        }
                    }
                }
            }
        } else {
            items(eventsForDate, key = { it.id }) { event ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    EventCard(
                        event = event,
                        onEditClick = onEditEvent,
                        onDeleteClick = onDeleteEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaViewContent(
    events: List<CalendarEvent>,
    onEditEvent: (CalendarEvent) -> Unit,
    onDeleteEvent: (CalendarEvent) -> Unit,
    onAddEventClick: () -> Unit
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "No events found",
                    modifier = Modifier.size(54.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No matching events found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Try clearing filters or search keywords",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddEventClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create New Event")
                }
            }
        }
    } else {
        // Group events by date for a pristine agenda timeline
        val groupedByDate = events.groupBy { it.date }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        ) {
            groupedByDate.forEach { (dateStr, dateEvents) ->
                val localDate = CalendarDateUtils.parseIsoDate(dateStr)

                item {
                    Text(
                        text = CalendarDateUtils.formatFullDate(localDate),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 14.dp, bottom = 6.dp)
                    )
                }

                items(dateEvents, key = { it.id }) { event ->
                    Box(modifier = Modifier.padding(vertical = 4.dp)) {
                        EventCard(
                            event = event,
                            onEditClick = onEditEvent,
                            onDeleteClick = onDeleteEvent
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
