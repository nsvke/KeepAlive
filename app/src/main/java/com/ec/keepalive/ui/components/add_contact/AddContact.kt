package com.ec.keepalive.ui.components.add_contact

import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import android.widget.NumberPicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ec.keepalive.R
import com.ec.keepalive.ui.components.cards.ContactPreviewCard


@Composable
fun AddContact(
    viewModel: AddContactViewModel = viewModel(),
    expandedState: (s: Boolean ) -> Unit,
    onContactAdded: (Int) -> Unit,

) {
    val contactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contactUri = result.data?.data
            if (contactUri != null) {
                viewModel.parseContactUri(contactUri)
            }
        }
    }

    Column(modifier = Modifier
        .width(300.dp)
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(stringResource(id = R.string.lbl_new_following_contact), style = MaterialTheme.typography.titleLarge)

        AnimatedContent(
            targetState = viewModel.isContactSelected,
            label = "ContactSelection",
            transitionSpec = {
                (fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                    )
                    .using(
                        SizeTransform(clip = false)
                    )
            }
        ) { isSelected ->
            if (isSelected) {
                ContactPreviewCard(
                    name = viewModel.selectedName,
                    number = viewModel.displayPhoneNumber,
                    onClear = { viewModel.isContactSelected = false }
                )
            } else {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_PICK).apply {
                            data = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                        }
                        contactLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Contacts, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.btn_choose_contact))
                }
            }
        }


        PeriodSelector(viewModel.periodAsDays, { viewModel.periodAsDays = it })

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { expandedState(false) }) {
                Text(stringResource(id = R.string.btn_cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.saveContact() { newId ->
                    expandedState(false)
                    onContactAdded(newId)
                }},
                enabled = viewModel.isContactSelected) {
                Text(stringResource(id = R.string.btn_save))
            }
        }

    }
}


@Composable
fun PeriodSelector(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val currentVal: Int = value

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(id = R.string.lbl_period),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(12.dp)
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilledIconButton(
                onClick = {
                    if (currentVal > 1) onValueChange((currentVal - 1))
                    else onValueChange(90)
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon( Icons.Rounded.Remove, contentDescription = null)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$currentVal",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = R.string.lbl_period_unit),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }


            FilledIconButton(
                onClick = {
                    onValueChange((currentVal + 1))
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickPeriodChip(label = stringResource(R.string.lbl_quick_two_week), days = 14, onSelect = onValueChange)
            QuickPeriodChip(label = stringResource(R.string.lbl_quick_one_month), days = 30, onSelect = onValueChange)
            QuickPeriodChip(label = stringResource(R.string.lbl_quick_six_week), days = 45, onSelect = onValueChange)
        }
    }
}

@Composable
fun QuickPeriodChip(label: String, days: Int, onSelect: (Int) -> Unit) {
    SuggestionChip(
        onClick = { onSelect(days) },
        label = { Text(label) }
    )
}

//        Text(stringResource(R.string.lbl_period), style = MaterialTheme.typography.labelLarge)
//        WheelNumberPicker(
//            value = if(viewModel.periodAsDays > 0) viewModel.periodAsDays else 7,
//            onValueChange = { newValue ->
//                viewModel.periodAsDays = newValue
//            }
//        )

//@Composable
//fun WheelNumberPicker(
//    min: Int = 1,
//    max: Int = 365,
//    value: Int,
//    onValueChange: (Int) -> Unit
//) {
//    AndroidView(
//        modifier = Modifier.fillMaxWidth(),
//        factory = { context ->
//            NumberPicker(context).apply {
//                minValue = min
//                maxValue = max
//                wrapSelectorWheel = true
//                setOnValueChangedListener { _, _, newVal ->
//                    onValueChange(newVal)
//                }
//            }
//        },
//        update = { view ->
//            if (view.value != value) {
//                view.value = value
//            }
//        }
//    )
//}
