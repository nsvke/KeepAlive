package com.ec.keepalive.ui.screens.home

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ec.keepalive.R
import com.ec.keepalive.ui.components.add_button.AddButton
import com.ec.keepalive.ui.components.add_contact.AddContact
import com.ec.keepalive.ui.components.add_contact.AddContactViewModel
import com.ec.keepalive.ui.components.contacts_list.ContactList
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ec.keepalive.ui.components.TimePIcker.ReminderTimeDialog
import com.ec.keepalive.ui.components.contacts_list.ContactListViewModel
import com.ec.keepalive.utils.KLog
import com.ec.keepalive.utils.SyncPreferences
import com.ec.keepalive.worker.WorkScheduler
import java.util.jar.Manifest

@Composable
fun HomeScreen(
  contactListViewModel: ContactListViewModel = viewModel(),
  addContactViewModel: AddContactViewModel = viewModel()
) {
    var isExpanded by remember { mutableStateOf(false) }
    // var isButtonVisible by remember { mutableStateOf(true)}

    /*val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if(available.y < -5){
                    isButtonVisible = false
                } else if (available.y > 5) {
                    isButtonVisible = true
                }
                return Offset.Zero
            }
        }
    }*/

    BackHandler(enabled = isExpanded) {
        isExpanded = false
    }

    var showSettingsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactListViewModel.syncWithCallLogs()
        } else {
            KLog.d("permission denied!")
            (context as? Activity)?.finish()
        }
    }

    LaunchedEffect(Unit) {
        if(!contactListViewModel.wasSyncedInSplash) {
            permissionLauncher.launch(android.Manifest.permission.READ_CALL_LOG)
        }
    }

    Scaffold{ innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            //.nestedScroll(nestedScrollConnection)
        ) {
            Column (modifier = Modifier.fillMaxSize()) {

                HeaderSection({ showSettingsDialog = true})

                ContactList(
                    contentPadding = PaddingValues(bottom = 50.dp),
                    viewModel = contactListViewModel,
                    onEditClick = { contact ->
                        addContactViewModel.loadContact(contact)
                        isExpanded = true
                    }
                )
            }

            if (showSettingsDialog) {
                val (savedHour, savedMinute) = remember {
                    SyncPreferences.getRemindTime(context)
                }

                ReminderTimeDialog(
                    initialHour = savedHour,
                    initialMinute = savedMinute,
                    onDismiss = { showSettingsDialog = false },
                    onConfirm = { hour, minute ->
                        SyncPreferences.setRemindTime(context, hour, minute)

                        WorkScheduler.scheduleNextWork(context)

                        showSettingsDialog = false
                    }
                )
            }


            AnimatedFabContainer(
                // isButtonVisible = isButtonVisible,
                isExpanded = isExpanded,
                onExpandChange = { isExpanded = it },
                modifier = Modifier,
                contactListViewModel = contactListViewModel,
                addContactViewModel = addContactViewModel
            )
        }
    }
}

@Composable
fun BoxScope.AnimatedFabContainer(
    // isButtonVisible: Boolean,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contactListViewModel: ContactListViewModel,
    addContactViewModel: AddContactViewModel
) {
    val transition = updateTransition(targetState = isExpanded, label = "FabTransition")
    val cornerRadius by transition.animateDp(
        transitionSpec = {
            if (targetState) spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
            else spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
        },
        label = "CornerRadius"
    ) { expanded ->
        if (expanded) 16.dp else 50.dp
    }
    val backgroundColor by transition.animateColor(
        transitionSpec = { tween(250) },
        label = "BackgroundColor"
    ) { expanded ->
        if (expanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    }

    AnimatedVisibility(
        visible = true, //isButtonVisible,
//        enter = slideInHorizontally(
//            initialOffsetX = { it * 2 },
//            animationSpec = tween(500, easing = FastOutSlowInEasing)
//        ),
//        exit = slideOutHorizontally(
//            targetOffsetX = { it * 2 },
//            animationSpec = tween(500, easing = LinearOutSlowInEasing)
//        ),
        modifier = modifier.align(Alignment.BottomEnd)
    ) {

        Box(
            modifier = Modifier
                .padding(if (isExpanded) 16.dp else 24.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(cornerRadius),
                    spotColor = MaterialTheme.colorScheme.primary
                )
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clip(RoundedCornerShape(cornerRadius))
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                )
                .clickable(
                    enabled = !isExpanded,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // Tıklama efektini kapatmak istersen (opsiyonel)
                ) {
                    onExpandChange(true)
                }
        ) {

            AnimatedContent(
                targetState = isExpanded,
                label = "ContentFade",
                transitionSpec = {
                    fadeIn(animationSpec = tween(300, 100)) togetherWith
                            fadeOut(animationSpec = tween(100))
                },
            ) { expanded ->
                if (!expanded) {
                    AddButton()
                } else {
                    Box(modifier = Modifier.widthIn(max = 320.dp)) {
                        AddContact(
                            viewModel = addContactViewModel,
                            expandedState = { curr -> onExpandChange(curr) },
                            onContactAdded = { newId ->
                            contactListViewModel.onContactAdded(newId)
                        })
                    }
                }

            }
        }
    }
}

@Composable
fun HeaderSection(
    onSettingsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 24.dp, bottom = 12.dp, start = 24.dp, end = 24.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,

        ) {
            Row {
                Icon(
                    imageVector = Icons.Rounded.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(id = R.string.lbl_follow_list_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.weight(1f))

            Surface(
                onClick = onSettingsClick,
                modifier = Modifier.size(36.dp),
                shape = CircleShape,


                shadowElevation = 6.dp,

                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),

                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        // İkon rengini Primary yapıyoruz ki camın içinde parlasın
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}