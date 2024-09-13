package com.example.mollyai

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mollyai.ui.theme.MollyAITheme

// Task state enum to track task progress
enum class TaskState {
    DEFAULT, STARTED, COMPLETED
}

// Data class for Task (Mission) using mutable states
data class Task(
    var description: MutableState<String> = mutableStateOf(""),
    var time: MutableState<String> = mutableStateOf("0000"),
    var state: MutableState<TaskState> = mutableStateOf(TaskState.DEFAULT) // Track task state (not started, started, completed)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MollyAIApp()
        }
    }

    @Composable
    fun MollyAIApp() {
        MollyAITheme {
            var editTask by remember { mutableStateOf<Task?>(null) } // Track task being edited

            // Disable back button on home screen
            BackHandler(enabled = true) { /* Do nothing to disable back button */ }

            Scaffold(
                topBar = {
                    // MollyAI header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MollyAI",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                content = { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        MissionScreen(
                            onEditTask = { task ->
                                editTask = task
                            }
                        )
                    }

                    // Show edit dialog if a task is selected
                    editTask?.let { task ->
                        EditTaskDialog(
                            task = task,
                            onSave = { updatedTask ->
                                task.description.value = updatedTask.description.value
                                task.time.value = updatedTask.time.value
                                editTask = null
                            },
                            onCancel = { editTask = null }
                        )
                    }
                }
            )
        }
    }

    // Composable function for displaying the mission lists (Daily and Side Missions)
    @Composable
    fun MissionScreen(onEditTask: (Task) -> Unit) {
        // Get the context for Toast messages
        val context = LocalContext.current

        // Your daily routine (missions) integrated
        val dailyTasksState = remember {
            mutableStateListOf(
                Task(mutableStateOf("Wake up, Train"), mutableStateOf("0300")),
                Task(mutableStateOf("Hygiene, Get dressed, Pray"), mutableStateOf("0400")),
                Task(mutableStateOf("Journal, Morning Market Analysis"), mutableStateOf("0500")),
                Task(mutableStateOf("Leave for work"), mutableStateOf("0600")),
                Task(mutableStateOf("Start work"), mutableStateOf("0700")),
                Task(mutableStateOf("Lunch (Read, Backtest, Code)"), mutableStateOf("1100")),
                Task(mutableStateOf("Back to work"), mutableStateOf("1130")),
                Task(mutableStateOf("Off work, Drive home"), mutableStateOf("1530")),
                Task(mutableStateOf("Backtest/Complete side missions"), mutableStateOf("1600")),
                Task(mutableStateOf("Dinner + Family time"), mutableStateOf("1700")),
                Task(mutableStateOf("Train (Weights/Boxing)"), mutableStateOf("1800")),
                Task(mutableStateOf("Shower, Get ready for next day"), mutableStateOf("1930")),
                Task(mutableStateOf("End of day review"), mutableStateOf("2000")),
                Task(mutableStateOf("Spend time with wife"), mutableStateOf("2030")),
                Task(mutableStateOf("Visualization, Manifestation, Sleep"), mutableStateOf("2100"))
            )
        }

        // Empty side mission list for now
        val sideMissionsState = remember { mutableStateListOf<Task>() }

        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            // Daily Missions Section
            Text(
                text = "Daily Missions",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.75f) // 75% height for Daily Missions
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dailyTasksState) { task ->
                    // Task item with double tap functionality to toggle between states
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(2.dp, getBorderColor(task.state.value)) // Border color based on task state
                            )
                            .padding(8.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        toggleTaskState(task, context) // Change task state and show toast on double tap
                                    },
                                    onLongPress = {
                                        onEditTask(task) // Open edit menu on long press (0.25s)
                                    }
                                )
                            }
                    ) {
                        Row( // Using Row to put time and description on the same line
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Time on the left
                            Text(
                                text = task.time.value,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            // Description on the right
                            Text(
                                text = task.description.value,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Side Missions Section (empty initially)
            Text(
                text = "Side Missions",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f) // 25% height for Side Missions
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sideMissionsState) { task ->
                    // Display for side missions (empty at first)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, Color.Gray)) // Shared border for time and description
                            .padding(8.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        onEditTask(task) // Open edit menu on long press (0.25s)
                                    }
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Time on the left
                            Text(
                                text = task.time.value,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            // Description on the right
                            Text(
                                text = task.description.value,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Helper function to toggle task state on double-tap and show Toast message
    private fun toggleTaskState(task: Task, context: android.content.Context) {
        task.state.value = when (task.state.value) {
            TaskState.DEFAULT -> {
                showToast(context, "Task Started")
                TaskState.STARTED // From default to started (red)
            }
            TaskState.STARTED -> {
                showToast(context, "Task Completed")
                TaskState.COMPLETED // From started to completed (green)
            }
            TaskState.COMPLETED -> {
                showToast(context, "Task Reset")
                TaskState.DEFAULT // Reset to default (no color)
            }
        }
    }

    // Helper function to show Toast message
    private fun showToast(context: android.content.Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Helper function to get border color based on task state
    private fun getBorderColor(state: TaskState): Color {
        return when (state) {
            TaskState.DEFAULT -> Color.Gray
            TaskState.STARTED -> Color.Red
            TaskState.COMPLETED -> Color.Green
        }
    }

    // Composable function for editing a task (description and time)
    @Composable
    fun EditTaskDialog(
        task: Task,
        onSave: (Task) -> Unit,
        onCancel: () -> Unit
    ) {
        var description by remember { mutableStateOf(task.description.value) }
        var time by remember { mutableStateOf(task.time.value) }

        Dialog(onDismissRequest = { onCancel() }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Edit Mission", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time input
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description input
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons (Save, Cancel)
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            task.description.value = description
                            task.time.value = time
                            onSave(task)
                        }) {
                            Text("Save")
                        }
                        Button(onClick = { onCancel() }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
