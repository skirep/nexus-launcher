package cat.skirep.nexus

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.Canvas
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NexusHome() }
    }
}

private enum class NexusTheme { NEXUS, CYBER, MINIMAL }
private data class LaunchApp(val label: String, val packageName: String, val activityName: String, val icon: Drawable)

private fun drawableBitmap(drawable: Drawable): ImageBitmap {
    val width = 96
    val height = 96
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)
    return bitmap.asImageBitmap()
}

@Composable
private fun NexusHome() {
    val context = LocalContext.current
    val pm = context.packageManager
    val apps = remember {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(intent, 0).map { info ->
            LaunchApp(info.loadLabel(pm).toString(), info.activityInfo.packageName,
                info.activityInfo.name, info.loadIcon(pm))
        }.distinctBy { it.packageName to it.activityName }.sortedBy { it.label.lowercase() }
    }
    var theme by remember { mutableStateOf(NexusTheme.NEXUS) }
    var search by remember { mutableStateOf("") }
    var showAll by remember { mutableStateOf(false) }
    val dark = theme != NexusTheme.MINIMAL
    val foreground = if (dark) Color(0xFFF0F6FF) else Color(0xFF192439)
    val muted = if (dark) Color(0xFFB0C2DB) else Color(0xFF56677E)
    val accent = when(theme) {
        NexusTheme.NEXUS -> Color(0xFF6FDEFF)
        NexusTheme.CYBER -> Color(0xFFFF6BDD)
        NexusTheme.MINIMAL -> Color(0xFF3366AA)
    }
    val background = when(theme) {
        NexusTheme.NEXUS -> listOf(Color(0xFF080D1B), Color(0xFF172B4B), Color(0xFF090D1B))
        NexusTheme.CYBER -> listOf(Color(0xFF14071F), Color(0xFF35143F), Color(0xFF090A1A))
        NexusTheme.MINIMAL -> listOf(Color(0xFFF0F4F9), Color(0xFFDDE7F2), Color(0xFFF0F4F9))
    }
    val tile = if (dark) Color.White.copy(alpha = .11f) else Color.White.copy(alpha = .85f)
    val visibleApps = apps.filter { it.label.contains(search, ignoreCase = true) }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(background))) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("NEXUS", color = accent, fontSize = 36.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                    Text("BEYOND THE SCREEN", color = muted, fontSize = 10.sp, letterSpacing = 3.sp)
                }
                IconButton(onClick = { theme = NexusTheme.entries[(theme.ordinal + 1) % NexusTheme.entries.size] }) {
                    Icon(Icons.Default.Palette, "Canvia el tema", tint = accent)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(if (showAll) "Les teves aplicacions" else "El teu espai", color = foreground, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(value = search, onValueChange = { search = it; if(it.isNotBlank()) showAll = true },
                placeholder = { Text("Cerca aplicacions", color = muted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = accent) },
                singleLine = true, shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = foreground, unfocusedTextColor = foreground,
                    focusedBorderColor = accent, unfocusedBorderColor = muted.copy(alpha = .45f)),
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !showAll, onClick = { showAll = false; search = "" },
                    label = { Text("Inici") })
                FilterChip(selected = showAll, onClick = { showAll = true },
                    label = { Text("Totes les apps") })
                Spacer(Modifier.weight(1f))
                Text("${apps.size} apps", color = muted, modifier = Modifier.align(Alignment.CenterVertically), fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            val displayed = if (showAll || search.isNotBlank()) visibleApps else visibleApps.take(8)
            LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 112.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 30.dp)) {
                items(displayed, key = { it.packageName + "/" + it.activityName }) { app ->
                    Column(
                        modifier = Modifier.background(tile, RoundedCornerShape(22.dp))
                            .clickable {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_MAIN)
                                        .addCategory(Intent.CATEGORY_LAUNCHER)
                                        .setClassName(app.packageName, app.activityName)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                } catch (_: Exception) { }
                            }.padding(14.dp).heightIn(min = 104.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val bitmap = remember(app.packageName, app.activityName) { drawableBitmap(app.icon) }
                        Image(bitmap, app.label, Modifier.size(48.dp), contentScale = ContentScale.Fit)
                        Spacer(Modifier.height(10.dp))
                        Text(app.label, color = foreground, fontSize = 12.sp, maxLines = 2,
                            overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}
