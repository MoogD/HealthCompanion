package com.dom.healthcompanion.ui.featureList

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dom.healthcompanion.R
import com.dom.healthcompanion.ui.theme.Purple80
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun FeatureListScreen(featureFlow: Flow<List<FeatureItem>>) {
    val item = featureFlow.collectAsState(initial = emptyList()).value
    LazyColumn {
        items(item.size) {
            Row(
                modifier =
                    Modifier
                        .testTag("featureItem")
                        .fillMaxWidth()
                        .padding(12.dp)
                        .background(color = Purple80, shape = RoundedCornerShape(8.dp))
                        .clickable(onClick = {
                            item[it].onClick.invoke()
                        }),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(text = stringResource(item[it].textRes), Modifier.padding(12.dp))
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    apiLevel = 34,
    showBackground = true,
    device = "id:pixel_5",
)
@Composable
fun FeatureListScreenPreview() {
    val flow = MutableStateFlow(emptyList<FeatureItem>())
    FeatureListScreen(featureFlow = MutableStateFlow(listOf(FeatureItem(R.string.breathing_screen_title) {})))
    flow.tryEmit(listOf(FeatureItem(R.string.breathing_screen_title) {}))
}
