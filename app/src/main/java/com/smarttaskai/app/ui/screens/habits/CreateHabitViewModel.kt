package com.smarttaskai.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import com.smarttaskai.app.data.repository.HabitRepository
import com.smarttaskai.app.domain.model.Habit
import com.smarttaskai.app.domain.model.HabitFrequency
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {

    suspend fun createHabit(name: String, frequency: HabitFrequency) {
        habitRepository.createHabit(
            Habit(
                name = name,
                frequency = frequency
            )
        )
    }
}
