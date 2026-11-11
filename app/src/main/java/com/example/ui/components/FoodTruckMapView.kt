package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TruckLocation
import com.example.ui.theme.AgaveSecondary
import com.example.ui.theme.GoldenCheddar
import com.example.ui.theme.TerracottaPrimary
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun FoodTruckMapView(
    location: TruckLocation,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val truckPosition = remember(location.latitude, location.longitude) {
        LatLng(location.latitude, location.longitude)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(truckPosition, 16f)
    }

    LaunchedEffect(truckPosition) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(truckPosition, 16f)
    }

    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var useInteractiveRadarFallback by remember { mutableStateOf(false) }

    // Pulsing radar animation for live truck marker
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 48f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .testTag("food_truck_map_container")
    ) {
        if (!useInteractiveRadarFallback) {
            // Google Maps SDK Compose View
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = mapType,
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = false
                ),
                onMapClick = { /* User clicked map */ }
            ) {
                // Custom Marker for MoodBloom Food Truck
                MarkerComposable(
                    keys = arrayOf(location.id, location.name),
                    state = rememberMarkerState(position = truckPosition),
                    title = "MoodBloom Food Truck",
                    snippet = "${location.name} • ~${location.waitTimeMinutes}m wait"
                ) {
                    CustomFoodTruckMarker(
                        truckName = "MoodBloom #1",
                        waitTime = location.waitTimeMinutes,
                        pulseRadius = pulseRadius,
                        pulseAlpha = pulseAlpha
                    )
                }
            }
        } else {
            // Interactive Radar Map
            InteractiveRadarMapFallback(
                location = location,
                pulseRadius = pulseRadius,
                pulseAlpha = pulseAlpha
            )
        }

        // Top Floating Chip: Live GPS Telemetry
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.75f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${String.format("%.4f", location.latitude)}° N, ${String.format("%.4f", -location.longitude)}° W",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top Right Controls (Satellite Toggle & Fallback Toggle)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 3.dp,
                modifier = Modifier.size(36.dp)
            ) {
                IconButton(
                    onClick = {
                        mapType = if (mapType == MapType.NORMAL) MapType.SATELLITE else MapType.NORMAL
                    }
                ) {
                    Icon(
                        imageVector = if (mapType == MapType.NORMAL) Icons.Outlined.Layers else Icons.Filled.Layers,
                        contentDescription = "Toggle Satellite",
                        modifier = Modifier.size(18.dp),
                        tint = TerracottaPrimary
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shadowElevation = 3.dp,
                modifier = Modifier.size(36.dp)
            ) {
                IconButton(
                    onClick = {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(truckPosition, 16f)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Center on Truck",
                        modifier = Modifier.size(18.dp),
                        tint = TerracottaPrimary
                    )
                }
            }
        }

        // Bottom Bar on Map: "Navigate with Google Maps App" Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${location.address} • ~${location.distanceMiles} mi",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                FilledTonalButton(
                    onClick = {
                        // Open turn-by-turn directions in external Google Maps
                        val gmmIntentUri = Uri.parse("geo:${location.latitude},${location.longitude}?q=${Uri.encode("${location.name}, ${location.address}")}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            // Fallback to browser or generic geo intent
                            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode("${location.name}, ${location.address}")}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = TerracottaPrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("open_external_directions_button")
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Directions", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CustomFoodTruckMarker(
    truckName: String,
    waitTime: Int,
    pulseRadius: Float,
    pulseAlpha: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        // Truck Badge Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = TerracottaPrimary,
            shadowElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🌮", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = truckName,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldenCheddar
                ) {
                    Text(
                        text = "${waitTime}m",
                        color = Color(0xFF3B1E00),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Radar Pulse Circle & Pin Tip
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            // Animated Pulse Wave
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = TerracottaPrimary.copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Center Pin Head
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(TerracottaPrimary)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
fun InteractiveRadarMapFallback(
    location: TruckLocation,
    pulseRadius: Float,
    pulseAlpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFE9DF))
    ) {
        // Stylized street grid drawing
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridColor = Color(0xFFDFD5C7)
            val streetColor = Color(0xFFFFFDF9)
            val avenueColor = Color(0xFFFFE8D6)

            // Background streets
            for (x in 0..size.width.toInt() step 60) {
                drawLine(
                    color = gridColor,
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), size.height),
                    strokeWidth = 2f
                )
            }
            for (y in 0..size.height.toInt() step 60) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 2f
                )
            }

            // Main Avenue (Santa Fe Ave)
            drawLine(
                color = avenueColor,
                start = Offset(0f, size.height * 0.55f),
                end = Offset(size.width, size.height * 0.45f),
                strokeWidth = 24f
            )

            // Cross Street (7th St)
            drawLine(
                color = streetColor,
                start = Offset(size.width * 0.45f, 0f),
                end = Offset(size.width * 0.55f, size.height),
                strokeWidth = 20f
            )

            // Radar pulse ring
            drawCircle(
                color = TerracottaPrimary.copy(alpha = pulseAlpha),
                radius = pulseRadius * 2,
                center = Offset(size.width / 2f, size.height / 2f),
                style = Stroke(width = 4f)
            )
        }

        // Custom Food Truck Pin at Center
        Box(
            modifier = Modifier.align(Alignment.Center)
        ) {
            CustomFoodTruckMarker(
                truckName = "MoodBloom #1",
                waitTime = location.waitTimeMinutes,
                pulseRadius = pulseRadius,
                pulseAlpha = pulseAlpha
            )
        }
    }
}
