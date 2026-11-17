package chat.simplex.common.views.usersettings

import SectionTextFooter
import SectionView
import android.view.WindowManager
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import chat.simplex.common.model.ChatModel
import chat.simplex.common.security.CiszaGuard
import chat.simplex.common.views.helpers.DefaultSwitch
import chat.simplex.common.views.helpers.LAResult
import chat.simplex.common.views.helpers.authenticate
import chat.simplex.common.views.helpers.generalGetString
import chat.simplex.res.MR
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
actual fun PrivacyDeviceSection(
  showSettingsModal: (@Composable (ChatModel) -> Unit) -> (() -> Unit),
  setPerformLA: (Boolean) -> Unit,
) {
  val context = LocalContext.current.applicationContext
  var guardStatus by remember { mutableStateOf(CiszaGuard.status(context)) }
  var confirmArm by remember { mutableStateOf(false) }
  val lockPrefs = ChatModel.controller.appPrefs
  val canArm = lockPrefs.performLA.state.value && lockPrefs.laMode.state.value == LAMode.PASSCODE

  fun authorizeThen(change: () -> Boolean) {
    authenticate(
      promptTitle = generalGetString(MR.strings.cisza_guard_auth_title),
      promptSubtitle = "",
      selfDestruct = false,
      usingLAMode = LAMode.PASSCODE,
      oneTime = true
    ) { result ->
      if (result == LAResult.Success) {
        change()
        guardStatus = CiszaGuard.status(context)
      }
    }
  }

  SectionView(stringResource(MR.strings.settings_section_title_device)) {
    ChatLockItem(showSettingsModal, setPerformLA)
    SettingsPreferenceItem(painterResource(MR.images.ic_visibility_off), stringResource(MR.strings.protect_app_screen), lockPrefs.privacyProtectScreen) { on ->
      val activity = chat.simplex.common.platform.mainActivity.get() as? FragmentActivity
      if (on) {
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
      } else {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
      }
    }

    SettingsActionItemWithContent(painterResource(MR.images.ic_security), stringResource(MR.strings.cisza_guard_title)) {
      DefaultSwitch(
        checked = guardStatus == CiszaGuard.Status.ARMED,
        onCheckedChange = { requested ->
          if (guardStatus == CiszaGuard.Status.LOCKED) return@DefaultSwitch
          if (requested) {
            if (canArm) confirmArm = true
          } else {
            authorizeThen { CiszaGuard.disable(context) }
          }
        }
      )
    }
  }

  SectionTextFooter(
    when {
      guardStatus == CiszaGuard.Status.LOCKED -> stringResource(MR.strings.cisza_guard_locked)
      !canArm -> stringResource(MR.strings.cisza_guard_require_pin)
      else -> stringResource(MR.strings.cisza_guard_description)
    }
  )

  if (confirmArm) {
    AlertDialog(
      onDismissRequest = { confirmArm = false },
      title = { Text(stringResource(MR.strings.cisza_guard_confirm_title)) },
      text = { Text(stringResource(MR.strings.cisza_guard_confirm_body)) },
      confirmButton = {
        TextButton(onClick = {
          confirmArm = false
          if (canArm) authorizeThen { CiszaGuard.enable(context) }
        }) { Text(stringResource(MR.strings.cisza_guard_confirm_button)) }
      },
      dismissButton = {
        TextButton(onClick = { confirmArm = false }) { Text(stringResource(MR.strings.cancel_verb)) }
      }
    )
  }
}
