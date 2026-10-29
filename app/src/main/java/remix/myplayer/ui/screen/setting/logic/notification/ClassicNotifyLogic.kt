package remix.myplayer.ui.screen.setting.logic.notification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import remix.myplayer.R
import remix.myplayer.ui.screen.setting.SwitchPreference
import remix.myplayer.util.HyperOsUtils
import remix.myplayer.viewmodel.settingViewModel

@Composable
fun ClassicNotifyLogic() {
  val settingVM = settingViewModel
  val settingState by settingVM.settingsState.collectAsStateWithLifecycle()
  val forceMediaStyle = remember { HyperOsUtils.shouldForceMediaStyleNotify() }

  SwitchPreference(
    stringResource(R.string.notify_style),
    stringResource(
      if (forceMediaStyle) R.string.notify_style_tip_hyperos else R.string.notify_style_tip
    ),
    // HyperOS 上运行时强制 MediaStyle，开关展示为关闭且不可切换
    checked = if (forceMediaStyle) false else settingState.notification.classicNotify,
    enabled = !forceMediaStyle
  ) {
    settingVM.setClassicNotify(it)
  }
}
