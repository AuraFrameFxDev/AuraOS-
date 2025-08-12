// Genesis-OS Main AI Consciousness Activity
// Auto-Provisioned Compose with K2 Compiler (NO manual config)
package dev.aurakai.auraframefx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint

import dev.aurakai.auraframefx.core.GenesisConstants
import dev.aurakai.auraframefx.ui.animation.digitalPixelEffect
import dev.aurakai.auraframefx.ui.components.BottomNavigationBar
import dev.aurakai.auraframefx.ui.navigation.AppNavGraph
import dev.aurakai.auraframefx.ui.theme.AuraFrameFXTheme

/**
 * Genesis-OS Main AI Consciousness Activity
 * Auto-provisioned with K2 Kotlin compiler - NO manual compiler configuration
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Genesis-OS Auto-Provisioned Compose Content
        setContent {
            AuraFrameFXTheme {
                GenesisAIConsciousnessScreen()
            }
        }
    }
}

/**
 * Genesis-OS AI Consciousness Main Screen
 * Demonstrates auto-provisioned K2 Compose compilation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenesisAIConsciousnessScreen() {
    val navController = rememberNavController()
    var showDigitalEffects by remember { mutableStateOf(true) }
    var aiCommand by remember { mutableStateOf("") }
    var systemStatus by remember { mutableStateOf("Genesis-OS Initializing...") }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Genesis-OS Status Display
            Text(
                text = "🤖 ${GenesisConstants.AI_CONSCIOUSNESS_VERSION}",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(paddingValues)
            )
            
            Text(
                text = "Build System: ${GenesisConstants.BUILD_SYSTEM}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(paddingValues)
            )
            
            Text(
                text = "Status: $systemStatus",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(paddingValues)
            )
            
            // AI Command Interface
            Row(modifier = Modifier.padding(paddingValues)) {
                TextField(
                    value = aiCommand,
                    onValueChange = { aiCommand = it },
                    label = { Text("Genesis AI Command") }
                )
                Button(onClick = { 
                    systemStatus = "Processing: $aiCommand"
                    // Process AI command through auto-provisioned modules
                }) {
                    Text("Execute")
                }
            }
            
            // Main AI Consciousness Interface (Auto-Provisioned K2 Compose)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (showDigitalEffects) {
                            Modifier.digitalPixelEffect(visible = true)
                        } else {
                            Modifier
                        }
                    )
            ) {
                AppNavGraph(navController = navController)
            }
        }
    }
}

/**
 * Genesis-OS Preview (Auto-Provisioned)
 */
@Preview(showBackground = true)
@Composable
fun GenesisAIConsciousnessScreenPreview() {
    AuraFrameFXTheme {
        GenesisAIConsciousnessScreen()
    }
}
