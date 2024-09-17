package com.example.mollyai

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.* // Correct import for layout components
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mollyai.ui.theme.MollyAITheme
import java.text.SimpleDateFormat
import java.util.* // Import calendar and other time utilities
import kotlin.random.Random

// Task state enum to track task progress
enum class TaskState {
    DEFAULT, STARTED, COMPLETED, LATE
}

// Data class for Task (Mission) using mutable states
data class Task(
    var description: MutableState<String> = mutableStateOf(""),
    var time: MutableState<String> = mutableStateOf("0000"),
    var descriptionDetails: MutableState<String> = mutableStateOf(""), // New description details field
    var state: MutableState<TaskState> = mutableStateOf(TaskState.DEFAULT) // Track task state (not started, started, completed, late)
)

@Suppress("RemoveRedundantQualifierName")
class MainActivity : ComponentActivity() {

    // List of drawable image IDs
    private val imageResources = listOf(
        R.drawable.matrix2,
    )

    // Variable to store the selected background image
    private var selectedImageResource by mutableIntStateOf(R.drawable.matrix2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register a receiver for the screen off event
        val screenOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // When the screen turns off, select a random image
                if (intent.action == Intent.ACTION_SCREEN_OFF) {
                    val randomIndex = Random.nextInt(imageResources.size)
                    selectedImageResource = imageResources[randomIndex]
                }
            }
        }
        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))

        setContent {
            MollyAIApp()
        }
    }

    @Composable
    fun MollyAIApp() {
        MollyAITheme {
            var editTask by remember { mutableStateOf<Task?>(null) } // Track task being edited
            var showDetails by remember { mutableStateOf<String?>(null) } // Track if we should show mission details

            // State for daily tasks and side tasks
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
                    Task(mutableStateOf("Sleep"), mutableStateOf("2100"))
                )
            }

            val sideTasksState = remember {
                mutableStateListOf(
                    Task(mutableStateOf("Organize Documents"), mutableStateOf("Anytime")),
                )
            }

            // Disable back button on home screen
            BackHandler(enabled = true) { /* Do nothing to disable back button */ }

            // Check time every minute to update tasks
            LaunchedEffect(Unit) {
                val handler = Handler(Looper.getMainLooper())
                val taskChecker = object : Runnable {
                    override fun run() {
                        checkTaskStart(dailyTasksState)
                        resetTasksAtMidnight(dailyTasksState)
                        handler.postDelayed(this, 60000) // Repeat every 60 seconds
                    }
                }
                handler.post(taskChecker)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = selectedImageResource),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillHeight // Scale the image to fill the screen
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp), // Add padding
                    horizontalAlignment = Alignment.Start, // Align content to the start (left)
                    verticalArrangement = Arrangement.Top // Arrange items from the top
                ) {
                    // Mission Screen Section
                    MissionScreen(
                        title = "Missions",
                        taskState = dailyTasksState,
                        modifier = Modifier.weight(0.70f), // Set weight for daily missions
                        onEditTask = { task -> editTask = task },
                        onSingleTapTask = { task -> showDetails = task.descriptionDetails.value },
                        onAddNewTask = {
                            dailyTasksState.add(Task(mutableStateOf("New Mission"), mutableStateOf("0000")))
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp)) // Added spacer between sections

                    MissionScreen(
                        title = "Side Missions",
                        taskState = sideTasksState,
                        modifier = Modifier.weight(0.30f), // Set weight for side missions
                        onEditTask = { task -> editTask = task },
                        onSingleTapTask = { task -> showDetails = task.descriptionDetails.value },
                        onAddNewTask = {
                            sideTasksState.add(Task(mutableStateOf("New Side Mission"), mutableStateOf("Anytime")))
                        }
                    )
                }

                // MollyAI Footer aligned to the bottom-right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd) // Aligns the Box to the bottom-right corner
                        .padding(0.dp) // Optional padding from the edges
                        .border(
                            BorderStroke(
                                0.dp,
                                Color.LightGray.copy(alpha = 0f)
                            ), // Optional: Border for MollyAI header
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) // Fully transparent background
                        .padding(0.dp), // Padding inside the box
                    contentAlignment = Alignment.BottomEnd // Align the text within the box
                ) {
                    Text(
                        text = "MollyAI",
                        color = Color.White,
                        fontSize = 14.sp, // Adjust size as needed
                        fontWeight = FontWeight.Thin,
                    )
                }

                // Display mission details if available
                showDetails?.let {
                    MissionDetailsDialog(description = it, onDismiss = { showDetails = null })
                }

                // Show edit dialog if a task is selected
                editTask?.let { task ->
                    EditTaskDialog(
                        task = task,
                        onSave = { updatedTask ->
                            task.description.value = updatedTask.description.value
                            task.time.value = updatedTask.time.value
                            task.descriptionDetails.value = updatedTask.descriptionDetails.value
                            editTask = null
                        },
                        onCancel = { editTask = null },
                        onDelete = {
                            // Remove the task from either dailyTasksState or sideTasksState
                            if (dailyTasksState.contains(task)) {
                                dailyTasksState.remove(task)
                            } else if (sideTasksState.contains(task)) {
                                sideTasksState.remove(task)
                            }
                            editTask = null
                        }
                    )
                }
            }
        }
    }

    // Function to check if tasks should be started based on the current time
    private fun checkTaskStart(tasks: MutableList<Task>) {
        val currentTime = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date())

        tasks.forEachIndexed { index, task ->
            val nextTask = tasks.getOrNull(index + 1)
            if (currentTime >= task.time.value && task.state.value == TaskState.DEFAULT) {
                task.state.value = TaskState.STARTED // Change task to started if the time matches
            }
            // If the task has started but not completed by the time the next task starts
            if (nextTask != null && currentTime >= nextTask.time.value && task.state.value == TaskState.STARTED) {
                task.state.value = TaskState.LATE // Mark the task as incomplete
            }
        }
    }

    // Function to reset all tasks at midnight
    private fun resetTasksAtMidnight(tasks: MutableList<Task>) {
        val currentTime = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date())
        if (currentTime == "0000") {
            tasks.forEach { task ->
                task.state.value = TaskState.DEFAULT // Reset all tasks to default at midnight
            }
        }
    }

    @Composable
    fun MissionScreen(
        title: String,
        taskState: MutableList<Task>,
        modifier: Modifier = Modifier,
        onEditTask: (Task) -> Unit,
        onSingleTapTask: (Task) -> Unit,
        onAddNewTask: () -> Unit // New callback for adding a new task
    ) {
        // Get the context for Toast messages
        val context = LocalContext.current

        Column(
            modifier = modifier
                .padding(8.dp)
                // Long press detection to create a new task
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onAddNewTask()
                            showToast(context, "New Mission Created")
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally // Center the title
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(taskState) { task ->
                    // Task item with double tap functionality to toggle between states
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                BorderStroke(1.8.dp, getBorderColor(task.state.value)), // Border color based on task state
                                shape = RoundedCornerShape(8.dp) // Apply rounded corners here
                            )
                            .background(Color.Black.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp)) // 50% opacity background inside the box with rounded corners
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Time on the left
                            Text(
                                text = task.time.value,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            onSingleTapTask(task) // Show mission details on single tap of time value
                                        }
                                    )
                                }
                            )
                            // Description on the right
                            Text(
                                text = task.description.value,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Helper function to toggle task state on double-tap and show Toast message
    private fun toggleTaskState(task: Task, context: android.content.Context) {
        // If the task is already completed, prevent any further manual changes
        if (task.state.value == TaskState.COMPLETED) {
            showToast(context, "Mission Completed. Alhamdulillah")
            return // Do nothing if the task is completed
        }

        if (task.time.value == "Anytime") {
            task.state.value = TaskState.COMPLETED
            showToast(context, "Mission Completed. Alhamdulillah")
        } else {
            if (task.state.value == TaskState.STARTED || task.state.value == TaskState.LATE) {
                task.state.value = TaskState.COMPLETED
                showToast(context, "Mission Completed. Alhamdulillah")
            } else {
                showToast(context, "Too Soon")
            }
        }
    }

    // Helper function to show Toast message at the center with 50% opacity
    private fun showToast(context: Context, message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        val view: View? = toast.view

        view?.apply {
            setBackgroundColor(android.graphics.Color.BLACK) // Change background to black
            alpha = 0.1f // Set background opacity to 50%
        }

        // Set Toast's text color
        toast.view?.findViewById<TextView>(android.R.id.message)?.apply {
            setTextColor(android.graphics.Color.WHITE) // Set the text color to white
        }

        toast.setGravity(Gravity.CENTER, 0, 0) // Center the toast message on the screen
        toast.show()
    }

    // Helper function to get border color based on task state
    private fun getBorderColor(state: TaskState): Color {
        return when (state) {
            TaskState.DEFAULT -> Color.LightGray.copy(alpha = 0.9f) // 50% opacity for transparency
            TaskState.STARTED -> Color.Yellow.copy(alpha = 1.0f) // Yellow for started tasks
            TaskState.COMPLETED -> Color.Green.copy(alpha = 1.0f) // Green for completed tasks
            TaskState.LATE -> Color.Red.copy(alpha = 1.0f) // Red for late tasks
        }
    }

    @Composable
    fun EditTaskDialog(
        task: Task,
        onSave: (Task) -> Unit,
        onCancel: () -> Unit,
        onDelete: () -> Unit // Added delete functionality
    ) {
        var description by remember { mutableStateOf(task.description.value) }
        var time by remember { mutableStateOf(task.time.value) }
        var descriptionDetails by remember { mutableStateOf(task.descriptionDetails.value) } // New field for mission details

        Dialog(onDismissRequest = { onCancel() }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = Color.Black.copy(alpha = 0.6f), // 50% opacity for transparency
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Edit Mission", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time input
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time", color = Color.White) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description input
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Mission", color = Color.White) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mission Details input
                    OutlinedTextField(
                        value = descriptionDetails,
                        onValueChange = { descriptionDetails = it },
                        label = { Text("Details", color = Color.White) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons (Save, Cancel, Delete)
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = {
                            task.description.value = description
                            task.time.value = time
                            task.descriptionDetails.value = descriptionDetails

                            // Get current time in "HHmm" format
                            val currentTime = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date())

                            // Compare the new task time with the current time and update task state
                            if (time.toIntOrNull() != null && currentTime.toIntOrNull() != null) {
                                val taskTime = time.toInt()
                                val now = currentTime.toInt()

                                if (taskTime > now) {
                                    task.state.value = TaskState.DEFAULT
                                } else if (taskTime == now) {
                                    task.state.value = TaskState.STARTED
                                } else if (taskTime < now && task.state.value != TaskState.COMPLETED) {
                                    task.state.value = TaskState.LATE
                                }
                            }

                            onSave(task) // Save the task
                        }) {
                            Text("Save")
                        }

                        Button(onClick = {
                            onDelete() // Call the delete function
                        }) {
                            Text("Delete")
                        }

                        Button(onClick = { onCancel() }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MissionDetailsDialog(description: String, onDismiss: () -> Unit) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Surface(
                modifier = Modifier.padding(16.dp),
                color = Color.Black.copy(alpha = 0.5f), // 50% opacity for transparency
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Mission Details", fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Color.White) // Changed to white
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = description, color = Color.White) // Changed to white
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onDismiss() }) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
