package es.uma.vuelink.ui.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import es.uma.vuelink.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDialog(
    onDismiss: () -> Unit, onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            val selectedDate = datePickerState.selectedDateMillis?.convertMillisToDate() ?: ""
            onDateSelected(selectedDate)
        }) {
            Text(stringResource(R.string.ok))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.cancel))
        }
    }) {
        DatePicker(
            state = datePickerState
        )
    }
}

fun Long.convertMillisToDate(): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@convertMillisToDate
        val zoneOffset = get(Calendar.ZONE_OFFSET)
        val dstOffset = get(Calendar.DST_OFFSET)
        add(Calendar.MILLISECOND, -(zoneOffset + dstOffset))
    }
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(calendar.time)
}