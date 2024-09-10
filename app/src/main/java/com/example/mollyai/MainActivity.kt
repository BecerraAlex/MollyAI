package com.example.mollyai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)  // Suppressing Experimental API warning
class MainActivity : ComponentActivity() {
    private var isOnHomeScreen = true // Variable to track if the user is on the home screen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use OnBackPressedDispatcher to handle back press
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isOnHomeScreen) {
                    finish() // Handle back press logic
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

        setContent {
            MollyAIMainScreen()
        }
    }

    override fun onUserLeaveHint() {
        // Override the home button behavior
        moveTaskToBack(true) // Keeps MollyAI as the launcher when the home button is pressed
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Opting into Experimental Material3 API
@Composable
fun MollyAIMainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { // Drawer content here
            DrawerContent(onCloseDrawer = { scope.launch { drawerState.close() } })
        },
        gesturesEnabled = false // Disable gestures to prevent opening drawer by swiping
    ) {
        // Main content with TopAppBar
        Scaffold(
            containerColor = Color.Transparent, // Ensure the Scaffold has a transparent background
            topBar = {
                TopAppBar(
                    title = { Text("MollyAI", color = Color.White) },
                    navigationIcon = { // Add the navigation icon to the left side
                        IconButton(onClick = {
                            scope.launch { drawerState.open() } // Open drawer on hamburger press
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent // Make the TopAppBar fully transparent
                    )
                )
            },
            content = { padding ->
                MollyAIHomePage(padding)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(onCloseDrawer: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(16.dp)
    ) {
        IconButton(onClick = { onCloseDrawer() }) { // Hamburger icon inside the drawer to close it
            Icon(Icons.Filled.Menu, contentDescription = "Close Drawer")
        }
        Text(
            "Settings",
            modifier = Modifier
                .padding(16.dp)
                .clickable { onCloseDrawer() } // Close drawer when an item is clicked
        )
    }
}

data class Task(
    var name: String
)

@Composable
fun TaskRow(
    task: Task,
    onLongPress: (Task) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onLongPress(task) } // Ensure long press is handled properly
    ) {
        Text(
            text = task.name,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
        )
    }
}

@Composable
fun TaskListScreen(tasks: MutableList<Task>) {
    var showDialog by remember { mutableStateOf(false) }
    var currentTask by remember { mutableStateOf(Task("")) }
    var isAddingTask by remember { mutableStateOf(false) }

    if (showDialog) {
        TaskInputDialog(
            task = currentTask,
            isAddingTask = isAddingTask,
            onDismiss = { showDialog = false },
            onSave = { updatedTask ->
                if (isAddingTask) {
                    tasks.add(updatedTask)
                }
                showDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent) // Transparent so background image is visible
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(tasks) { task ->
                TaskRow(
                    task = task,
                    onLongPress = { selectedTask ->
                        currentTask = selectedTask
                        showDialog = true
                        isAddingTask = false
                    }
                )
                HorizontalDivider(
                    color = Color(0xFF6200EA),
                    thickness = 1.dp
                )
            }
        }

        // Button to Add New Task
        Button(
            onClick = {
                currentTask = Task("")
                isAddingTask = true
                showDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EA)
            )
        ) {
            Text(text = "Add Task", color = Color.White)
        }
    }
}

@Composable
fun TaskInputDialog(
    task: Task,
    isAddingTask: Boolean,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var taskName by remember { mutableStateOf(TextFieldValue(task.name)) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp), // Use RoundedCornerShape from the correct import
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = if (isAddingTask) "Add Task" else "Edit Task")
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        task.name = taskName.text
                        onSave(task)
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun MollyAIHomePage(padding: PaddingValues) {
    val tasks = remember { mutableStateListOf<Task>() }

    Box(modifier = Modifier
        .padding(padding)
        .background(Color.Transparent) // Ensuring transparent background
    ) {
        TaskListScreen(tasks = tasks)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMollyAIHomePage() {
    MollyAIHomePage(PaddingValues())
}
