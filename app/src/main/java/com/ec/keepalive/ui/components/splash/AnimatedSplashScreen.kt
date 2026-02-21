package com.ec.keepalive.ui.components.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ec.keepalive.R
import com.ec.keepalive.utils.KLog
import kotlinx.coroutines.delay


@Composable
fun AnimatedSplashScreen(
    animationTrigger: Boolean,
    onAnimationFinished: () -> Unit,
) {
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if(startAnimation) 2f else 1.01f,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label= "Scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if(startAnimation) 0f else 1f,
        animationSpec = tween(durationMillis =400, delayMillis = 100),
        label = "Alpha"
    )
    val backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = alpha)

    LaunchedEffect(animationTrigger) {
        if(animationTrigger) {
            startAnimation = true
            delay(500)
            onAnimationFinished()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor),
        contentAlignment = Alignment.Center)
    {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(288.dp)
                .scale(scale)
                .alpha(alpha),
            contentScale = ContentScale.Fit
            //colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
    }
}