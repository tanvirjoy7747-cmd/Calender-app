package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

enum class CalendarViewMode {
    MONTH,
    AGENDA
}

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val searchQuery: String = "",
    val selectedCategory: EventCategory? = null,
    val isSearchActive: Boolean = false,
    val isAddEditOpen: Boolean = false,
    val eventBeingEdited: CalendarEvent? = null,
    val eventPendingDelete: CalendarEvent? = null,
    val daysInMonth: List<CalendarDay> = emptyList(),
    val selectedDateEvents: List<CalendarEvent> = emptyList(),
    val filteredAgendaEvents: List<CalendarEvent> = emptyList()
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalendarRepository

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<EventCategory?>(null)
    private val _isSearchActive = MutableStateFlow(false)
    private val _isAddEditOpen = MutableStateFlow(false)
    private val _eventBeingEdited = MutableStateFlow<CalendarEvent?>(null)
    private val _eventPendingDelete = MutableStateFlow<CalendarEvent?>(null)

    val uiState: StateFlow<CalendarUiState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CalendarRepository(db.eventDao())

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty(LocalDate.now())
        }

        val allEventsFlow = repository.allEvents

        uiState = combine(
            combine(
                _currentMonth,
                _selectedDate,
                _viewMode,
                _searchQuery,
                _selectedCategory
            ) { month, selDate, mode, query, cat ->
                Tuple5(month, selDate, mode, query, cat)
            },
            combine(
                _isSearchActive,
                _isAddEditOpen,
                _eventBeingEdited,
                _eventPendingDelete,
                allEventsFlow
            ) { searchActive, addEditOpen, editEvent, delEvent, events ->
                Tuple5(searchActive, addEditOpen, editEvent, delEvent, events)
            }
        ) { t1, t2 ->
            val month = t1.a
            val selDate = t1.b
            val mode = t1.c
            val query = t1.d
            val cat = t1.e

            val isSearch = t2.a
            val isAddEdit = t2.b
            val editEvent = t2.c
            val delEvent = t2.d
            val events = t2.e

            val eventsByDate = CalendarDateUtils.groupEventsByDate(events)
            val days = CalendarDateUtils.generateMonthDays(month, selDate, eventsByDate)

            val selDateIso = CalendarDateUtils.toIsoDate(selDate)
            val eventsForDate = eventsByDate[selDateIso]?.let {
                CalendarEventManager.sortEvents(it)
            } ?: emptyList()

            val filteredAgenda = CalendarEventManager.filterEvents(events, query, cat)

            CalendarUiState(
                currentMonth = month,
                selectedDate = selDate,
                viewMode = mode,
                searchQuery = query,
                selectedCategory = cat,
                isSearchActive = isSearch,
                isAddEditOpen = isAddEdit,
                eventBeingEdited = editEvent,
                eventPendingDelete = delEvent,
                daysInMonth = days,
                selectedDateEvents = eventsForDate,
                filteredAgendaEvents = filteredAgenda
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CalendarUiState()
        )
    }

    fun previousMonth() {
        val newMonth = _currentMonth.value.minusMonths(1)
        _currentMonth.value = newMonth
        // Adjust selected date to the new month if outside
        if (_selectedDate.value.year != newMonth.year || _selectedDate.value.month != newMonth.month) {
            _selectedDate.value = newMonth.atDay(1)
        }
    }

    fun nextMonth() {
        val newMonth = _currentMonth.value.plusMonths(1)
        _currentMonth.value = newMonth
        if (_selectedDate.value.year != newMonth.year || _selectedDate.value.month != newMonth.month) {
            _selectedDate.value = newMonth.atDay(1)
        }
    }

    fun jumpToToday() {
        val today = LocalDate.now()
        _currentMonth.value = YearMonth.from(today)
        _selectedDate.value = today
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        // If selecting a date in a neighboring month, navigate there
        val targetMonth = YearMonth.from(date)
        if (targetMonth != _currentMonth.value) {
            _currentMonth.value = targetMonth
        }
    }

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun setCategoryFilter(category: EventCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun openAddEventDialog(prefillDate: LocalDate? = null) {
        if (prefillDate != null) {
            _selectedDate.value = prefillDate
        }
        _eventBeingEdited.value = null
        _isAddEditOpen.value = true
    }

    fun openEditEventDialog(event: CalendarEvent) {
        _eventBeingEdited.value = event
        _isAddEditOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditOpen.value = false
        _eventBeingEdited.value = null
    }

    fun promptDeleteEvent(event: CalendarEvent) {
        _eventPendingDelete.value = event
    }

    fun cancelDelete() {
        _eventPendingDelete.value = null
    }

    fun confirmDelete() {
        val event = _eventPendingDelete.value ?: return
        viewModelScope.launch {
            repository.deleteEvent(event)
            _eventPendingDelete.value = null
        }
    }

    fun saveEvent(
        title: String,
        description: String,
        date: String,
        startTime: String,
        endTime: String,
        isAllDay: Boolean,
        category: EventCategory,
        location: String
    ) {
        val current = _eventBeingEdited.value
        val event = CalendarEvent(
            current?.id ?: 0,
            title.trim(),
            description.trim(),
            date,
            startTime,
            endTime,
            isAllDay,
            category,
            location.trim()
        )

        viewModelScope.launch {
            if (current == null || current.id == 0L) {
                repository.insertEvent(event)
            } else {
                repository.updateEvent(event)
            }
            closeAddEditDialog()
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)
