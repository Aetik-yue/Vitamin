package com.example.markdownreader.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.markdownreader.core.common.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    strings: AppStrings,
    onFontSizeChange: (Float) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onRememberRecentChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onClearRecent: () -> Unit,
    onBack: () -> Unit,
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settings) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(strings.back)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SettingsCard {
                Text(strings.language, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.languageCode == "zh",
                        onClick = { onLanguageChange("zh") },
                        label = { Text(strings.chinese) },
                    )
                    FilterChip(
                        selected = uiState.languageCode == "en",
                        onClick = { onLanguageChange("en") },
                        label = { Text(strings.english) },
                    )
                }
            }

            SettingsCard {
                Text(
                    text = "${strings.fontSize}: ${uiState.fontSize.toInt()}sp",
                    style = MaterialTheme.typography.titleMedium,
                )
                Slider(
                    value = uiState.fontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 12f..28f,
                    steps = 15,
                )
            }

            SettingsCard {
                SettingSwitchRow(
                    title = strings.darkMode,
                    checked = uiState.isDarkTheme,
                    onCheckedChange = onDarkThemeChange,
                )
                SettingSwitchRow(
                    title = strings.rememberRecent,
                    checked = uiState.rememberRecentFiles,
                    onCheckedChange = onRememberRecentChange,
                )
            }

            SettingsCard {
                Text(strings.aboutApp, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = strings.aboutAppBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = strings.clearRecent,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        text = strings.clearRecentWarning,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    OutlinedButton(
                        onClick = { showClearConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    ) {
                        Text(strings.clearRecent)
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(strings.clearRecentConfirmTitle) },
            text = { Text(strings.clearRecentConfirmBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearRecent()
                        showClearConfirm = false
                    },
                ) {
                    Text(strings.clear, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(strings.cancel)
                }
            },
        )
    }
}

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
