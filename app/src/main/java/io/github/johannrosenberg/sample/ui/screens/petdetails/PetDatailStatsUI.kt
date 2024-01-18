package io.github.johannrosenberg.sample.ui.screens.petdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.johannrosenberg.jetmagic.R
import io.github.johannrosenberg.sample.models.PetListItemInfo
import io.github.johannrosenberg.sample.ui.components.DetailProperty
import io.github.johannrosenberg.sample.ui.components.MultiLineText

@Composable
fun PetDetailStatsUI(
    pet: PetListItemInfo,
    modifier: Modifier = Modifier,
    onAdoptClick:  () -> Unit
) {
    Text(
        pet.name,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        DetailProperty(R.string.gender, if (pet.gender == "m") stringResource(R.string.male) else stringResource(R.string.female))
        DetailProperty(R.string.born, pet.birthdate)
        DetailProperty(R.string.color, pet.color)
    }
    MultiLineText(
        pet.description,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
    )
    Row(
        horizontalArrangement = Arrangement.Center, modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Button(
            modifier = Modifier.widthIn(max = 200.dp),
            //colors = AppTheme.getButtonColors(),
            elevation = ButtonDefaults.buttonElevation(5.dp),
            onClick = onAdoptClick
        ) {
            Text(
                text = stringResource(R.string.adopt) + " " + pet.name,
                modifier = modifier.padding(start = 10.dp, top = 7.dp, end = 10.dp, bottom = 7.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}