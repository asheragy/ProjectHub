import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.cerion.projecthub.model.Label
import org.cerion.projecthub.ui.AppTheme
import org.cerion.projecthub.ui.dialog.LabelsDialog


data class IssueEditState(
    val title: String = "",
    val body: String = "",
    val labels: List<Label> = listOf()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueEditScreen(
    initialState: IssueEditState,
    labels: List<Label>,
    onClose: () -> Unit,
    onSave: (IssueEditState) -> Unit,
) {
    var state by remember { mutableStateOf(initialState) }
    var showDialog by remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            TopAppBar(
                title = { Text(if(initialState.title.isEmpty()) "New Issue" else "Edit Issue") },
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        onSave(state)
                    }) {
                        Text("SAVE")
                    }
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Title",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            OutlinedTextField(
                value = state.title,
                onValueChange = {
                    state = state.copy(title = it)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            LabelList(labels = state.labels) {
                showDialog = true
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Body",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            OutlinedTextField(
                value = state.body,
                onValueChange = {
                    state = state.copy(body = it)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                placeholder = { Text(text = "Body") },
                maxLines = Int.MAX_VALUE,
            )

            if (showDialog) {
                Dialog(onDismissRequest = { }) {
                    LabelsDialog(
                        allLabels = labels,
                        selectedLabels = state.labels,
                        onSave = {
                            state = state.copy(labels = it)
                            showDialog = false
                        },
                        onClose = {
                            showDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LabelList(labels: List<Label>, onClick: () -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {
        Text(
            text = "Labels: ",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        labels.forEach { label ->
            val color = Color(label.color)
            AssistChip(
                onClick = { /* handle click */ },
                label = { Text(label.name, color = color) },
                colors = AssistChipDefaults.assistChipColors(containerColor = color.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(8.dp),
                border = null,
                //contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            )
        }
    }
}

@Preview
@Composable
fun IssueEditScreenPreview() {
    val labels = listOf(
        Label("", "bugs", Color.Red.toArgb()),
        Label("", "features", Color.Blue.toArgb())
    )
    val initialState = IssueEditState("New Issue", "some body text", labels)
    AppTheme {
        IssueEditScreen(initialState = initialState, labels = labels, onClose = {}, onSave = {})
    }
}