package com.ec.keepalive.ui.components.cards

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ec.keepalive.R
import com.ec.keepalive.utils.formatPhoneNumber
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.Instant


@Composable
fun ContactOne(
    name: String,
    number: String,
    period: Int,
    lastseen: Long,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onManuelSeen: () -> Unit,
    onEdit: () -> Unit,
    onPause: () -> Unit,
) {

    val uiState = remember(lastseen, period) {
        val lastSeenDate = Instant.ofEpochMilli(lastseen)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val nowDate = LocalDate.now()
        //val oneDayMs = 24 * 60 * 60 * 1000L
        val daysPassed = ChronoUnit.DAYS.between(lastSeenDate, nowDate).toInt().coerceAtLeast(0)

        val daysRemaining = period - daysPassed

        val progress = (daysPassed.toFloat() / period.toFloat()).coerceIn(0f, 1f)

        Triple(daysPassed, daysRemaining, progress)
    }

    val(daysPassed, daysRemaining, progress) = uiState

    val isOverdue = daysRemaining < 0
    val isToday = daysRemaining == 0

    val progressColor = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    var isConfirming by remember { mutableStateOf(false) }

    LaunchedEffect(isConfirming) {
        if(isConfirming) {
            delay(3000)
            isConfirming = false
        }
    }

    val transition = updateTransition(targetState = isConfirming, label = "deleteButtonTransition")


    val scale by transition.animateFloat(
        transitionSpec = {
            if(targetState) {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            } else {
                tween(durationMillis = 500)
            }
        },
        label = "Scale"
    ){ confirming ->
        if(confirming) 1.5f else 1f
    }

    val iconColor by transition.animateColor(
        transitionSpec = { tween(durationMillis = 300)},
        label = "IconColor"
    ) {confirming ->
        if(confirming) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
    }
    val backgroundColor by transition.animateColor(
        transitionSpec = { tween(durationMillis = 300 )},
        label = "BgColor"
    ) { confirming ->
        if(confirming) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface)
        ,elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)

    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = name.firstOrNull()?.toString()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(text = number.formatPhoneNumber(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                Spacer(modifier = Modifier.weight(1f))

                // HEADER ACTIONS: Edit & Delete
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Edit Contact",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        if(isConfirming){
                            onDelete()
                            isConfirming = false
                        } else {
                            isConfirming = true
                        }
                    },
                    modifier = Modifier
                        .graphicsLayer{
                            scaleX= scale
                            scaleY = scale
                        }
                        .background(
                            backgroundColor,
                            CircleShape
                        )
                        .size(36.dp)
                ) {
                    AnimatedContent(
                        targetState = isConfirming,
                        label = "IconChange"
                    ) { confirming ->
                        if(confirming) {
                            Row {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = stringResource(R.string.des_btn_contact_delete),
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Icon(
                                    Icons.Rounded.QuestionMark,
                                    contentDescription = stringResource(R.string.des_btn_contact_delete),
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.des_btn_contact_delete),
                                    tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = stringResource(R.string.lbl_contact_lastseen),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.History,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if(daysPassed == 0) stringResource(R.string.lbl_contact_today)  else "$daysPassed ${stringResource(R.string.lbl_contact_days)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(60.dp),
                        color = trackColor,
                        strokeWidth = 6.dp,
                        trackColor = Color.Transparent,
                    )

                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(60.dp),
                        color = progressColor,
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round,
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$period",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
//                        Text(
//                            text = stringResource(R.string.lbl_contact_days),
//                            style = MaterialTheme.typography.labelSmall,
//                            fontSize = 8.sp,
//                            color = MaterialTheme.colorScheme.outline
//                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.lbl_contact_planned),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOverdue) "${daysRemaining} ${stringResource(R.string.lbl_contact_days)}"
                                else if(isToday) stringResource(R.string.lbl_contact_today)
                                else "$daysRemaining ${stringResource(R.string.lbl_contact_days)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverdue) MaterialTheme.colorScheme.error
                                else if(isToday) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Rounded.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isOverdue) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    FilledTonalIconButton(
                        onClick = onManuelSeen,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector =Icons.Rounded.Handshake,
                            contentDescription = stringResource(R.string.des_btn_contact_add_manuel_calllog),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalIconButton(
                        onClick = onPause,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Pause,
                            contentDescription = "Pause Tracking",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}


//@Composable
//fun LabelValueCompact(label: String, value: String, isHighlight: Boolean = false) {
//    Column {
//        Text(
//            text = label,
//            style = MaterialTheme.typography.labelSmall,
//            color = MaterialTheme.colorScheme.outline
//        )
//        Text(
//            text = value,
//            style = MaterialTheme.typography.labelLarge,
//            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
//            fontWeight = if (isHighlight) FontWeight.Medium else FontWeight.Normal
//        )
//    }
//}
