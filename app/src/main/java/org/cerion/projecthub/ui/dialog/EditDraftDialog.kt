import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.cerion.projecthub.ui.AppTheme


data class EditDraftDialogState(
    val title: String = "",
    val body: String = "",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDraftDialog(
    initialState: EditDraftDialogState,
    onDismiss: (EditDraftDialogState?) -> Unit
) {
    var state by remember { mutableStateOf(initialState) }

    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            TopAppBar(
                title = { Text("Edit Draft") },
                modifier = Modifier.fillMaxWidth(),
                actions = {
                    IconButton(onClick = { onDismiss(null) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onDismiss(state)
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save")
            }
        }
    }

}

@Preview
@Composable
fun EditDraftDialogPreview() {
    AppTheme {
        EditDraftDialog(EditDraftDialogState("New Draft", "some body text")) { }
    }
}