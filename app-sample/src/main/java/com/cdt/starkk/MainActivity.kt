package com.cdt.starkk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cdt.starkk.ui.theme.StarkKTheme
import com.starkk.sdk.models.Character
import com.starkk.sdk.models.House

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StarkKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StarkKSampleApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun StarkKSampleApp(modifier: Modifier = Modifier) {
    val viewModel = remember { StarkKSampleViewModel() }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "️\uD83D\uDC3A StarkK SDK Demo",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Tab Selection
        TabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Houses") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Characters") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Single Query") }
            )
        }


    }
}


@Composable
fun CharacterCard(character: Character) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = character.name.ifEmpty { "Unknown" },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Gender: ${character.gender.ifEmpty { "N/A" }}", fontSize = 12.sp)
                Text(text = "Culture: ${character.culture.ifEmpty { "N/A" }}", fontSize = 12.sp)
            }
            if (character.titles.isNotEmpty()) {
                Text(
                    text = "Titles: ${character.titles.joinToString(", ")}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun HouseCard(house: House) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = house.name.ifEmpty { "Unknown" },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(text = "Region: ${house.region.ifEmpty { "N/A" }}", fontSize = 12.sp)
            if (house.words.isNotEmpty()) {
                Text(
                    text = "\"${house.words}\"",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (house.titles.isNotEmpty()) {
                Text(
                    text = "Titles: ${house.titles.take(2).joinToString(", ")}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

