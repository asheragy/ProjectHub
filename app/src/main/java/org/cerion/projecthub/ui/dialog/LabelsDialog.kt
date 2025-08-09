package org.cerion.projecthub.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.ui.AppTheme

data class LabelSelection(val label: Label, val selected: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsDialog(
    allLabels: List<Label>,
    selectedLabels: List<Label>,
    onClose: () -> Unit,
    onSave: (List<Label>) -> Unit) {

    val labelSelection = allLabels.map { globalLabel ->
        LabelSelection(globalLabel, selectedLabels.any { selected -> selected.id == globalLabel.id })
    }
    var labelState by remember { mutableStateOf(labelSelection) }

    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TopAppBar(
                title = { Text("Labels") },
                modifier = Modifier.fillMaxWidth(),
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(labelState) { label ->
                    LabelRow(
                        label = label,
                        onClick = {
                            labelState = labelState.map {
                               if (it.label.id == label.label.id)
                                    it.copy(selected = !it.selected)
                                else it
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSave(labelState.filter { it.selected }.map { it.label })
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
fun LabelRow(
    label: LabelSelection,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Saved",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(16.dp)
                .alpha(if (label.selected) 1f else 0f)
                .wrapContentSize()
        )

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(label.label.color), shape = CircleShape)
                        .padding(4.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(text = label.label.name)
            }

            Text(text = label.label.description)
        }
    }
}

@Preview
@Composable
fun LabelsDialogPreview() {
    val labelA = Label("", "Bugs", Color.Red.toArgb()).apply {
        description = "Bad Behaviors"
    }
    val labelB = Label("", "Features", Color.Blue.toArgb()).apply {
        description = "New things to add"
    }

    AppTheme {
        LabelsDialog(
            allLabels = listOf(labelA, labelB),
            selectedLabels = listOf(labelA),
            onClose = {},
            onSave = {}
        )
    }
}