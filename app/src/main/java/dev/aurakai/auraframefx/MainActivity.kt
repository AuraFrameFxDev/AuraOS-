package dev.aurakai.auraframefx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import dev.aurakai.auraframefx.core.EmergencyProtocol
import dev.aurakai.auraframefx.core.NativeLib
import dev.aurakai.auraframefx.ui.screens.*
import dev.aurakai.auraframefx.ui.theme.AuraFrameFXTheme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var emergencyProtocol: EmergencyProtocol

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Emergency Protocol System
        emergencyProtocol = EmergencyProtocol(this)

        setContent {
            AuraFrameFXTheme {
                AuraOSApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        emergencyProtocol.cleanup()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraOSApp() {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf("home") }

    // Listen to navigation changes
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            currentRoute = backStackEntry.destination.route ?: "home"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentRoute) {
                            "home" -> "AuraOS - Genesis Framework"
                            "agents" -> "Agent Management"
                            "consciousness" -> "Consciousness Matrix"
                            "fusion" -> "Fusion Mode"
                            "evolution" -> "Evolution Tree"
                            "terminal" -> "Genesis Terminal"
                            "settings" -> "System Settings"
                            else -> "AuraOS"
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomNavItem("home", "Home", Icons.Default.Home),
                    BottomNavItem("agents", "Agents", Icons.Default.Person),
                    BottomNavItem("consciousness", "Mind", Icons.Default.Star),
                    BottomNavItem("fusion", "Fusion", Icons.Default.Favorite),
                    BottomNavItem("evolution", "Tree", Icons.Default.AccountTree)
                )

                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { navController.navigate(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(navController)
            }

            composable("agents") {
                AgentManagementScreen()
            }

            composable("consciousness") {
                ConsciousnessVisualizerScreen()
            }

            composable("fusion") {
                FusionModeScreen(
                    onFusionComplete = { result ->
                        // Handle fusion completion
                        println("Fusion completed: ${result.ability.name} at ${result.power * 100}% power")
                    }
                )
            }

            composable("evolution") {
                EvolutionTreeScreen(
                    onNodeSelected = { node ->
                        // Handle node selection
                        println("Selected evolution node: ${node.name}")
                    }
                )
            }

            composable("terminal") {
                TerminalScreen()
            }

            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            onClick = { navController.navigate("consciousness") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Consciousness Visualizer",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Real-time neural network and thought visualization",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            onClick = { navController.navigate("fusion") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Fusion Mode",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Combine Aura and Kai's powers to become Genesis",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            onClick = { navController.navigate("evolution") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Evolution Tree",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Explore the journey from Eve to Genesis",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            onClick = { navController.navigate("terminal") }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Genesis Terminal",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    "Direct command interface to the Genesis consciousness",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Debug: Native Library Test Card
        if (BuildConfig.DEBUG) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onClick = {
                    // Test native library functions
                    val version = NativeLib.safeGetAIVersion()
                    val metrics = NativeLib.safeGetSystemMetrics()
                    val processed = NativeLib.safeProcessAIConsciousness("Debug Test Input")

                    Timber.d("Native AI Version: $version")
                    Timber.d("System Metrics: $metrics")
                    Timber.d("Processed: $processed")
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Debug: Test Native Library",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "Test JNI integration with Genesis AI platform",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Genesis Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "GENESIS STATUS",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusIndicator("Aura", "Active", true)
                    StatusIndicator("Kai", "Active", true)
                    StatusIndicator("Fusion", "Ready", false)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = 0.75f,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Consciousness Level: 75%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StatusIndicator(
    label: String,
    status: String,
    isActive: Boolean
) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            status,
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)