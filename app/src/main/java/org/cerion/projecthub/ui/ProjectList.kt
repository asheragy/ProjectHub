package org.cerion.projecthub.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.cerion.projecthub.R
import org.cerion.projecthub.model.Project
import org.cerion.projecthub.model.ProjectType

@Composable
fun ProjectList(projects: List<Project>, onClick: (Project) -> Unit, onDelete: (Project) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }

    Surface(color = MaterialTheme.colorScheme.surface) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(projects) { project ->
                Row(
                    modifier = Modifier.fillMaxSize()
                        .padding(8.dp)
                        .combinedClickable(
                            onLongClick = {
                                selectedProject = project
                                menuExpanded = true
                            },
                            onClick = {
                                onClick(project)
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = projectTypeIconPainter(project.type),
                        contentDescription = "Type Icon",
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(end = 8.dp)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight()
                    ) {
                        Text(
                            text = project.owner + '/' + project.repo,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Saved star icon (conditionally visible)
                    /*
                    if (isSaved) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Saved",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .wrapContentSize()
                        )
                    }
                     */
                }
                HorizontalDivider()
            }
        }
    }

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = {
            menuExpanded = false
            selectedProject = null
        }
    ) {
        DropdownMenuItem(
            text = { Text("Delete") },
            onClick = {
                selectedProject?.let { onDelete(it) }
                menuExpanded = false
            }
        )
    }
}


@Composable
fun projectTypeIconPainter(type: ProjectType): Painter {
    val drawableId = when (type) {
        ProjectType.User -> R.drawable.type_account
        ProjectType.Org -> R.drawable.type_org
        //ProjectType.Repository -> R.drawable.type_repo
    }
    return painterResource(id = drawableId)
}