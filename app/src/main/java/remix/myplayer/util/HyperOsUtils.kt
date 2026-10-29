package remix.myplayer.util

import android.os.Build
import remix.myplayer.misc.floatpermission.rom.RomUtils

/**
 * 小米澎湃 OS（HyperOS）能力探测。
 *
 * 依据官方说明：存在 `ro.mi.os.version.*` 即为 HyperOS；读取失败视为不支持，回退现有逻辑。
 */
object HyperOsUtils {

  @Volatile
  private var cachedIsHyperOs: Boolean? = null

  /**
   * 是否为 HyperOS。探测失败返回 false。
   */
  fun isHyperOs(): Boolean {
    cachedIsHyperOs?.let { return it }
    return detectHyperOs().also { cachedIsHyperOs = it }
  }

  /**
   * 是否应使用 MediaStyle 播放通知（可上超级岛）。
   * HyperOS 上强制 MediaStyle；其它机型仍尊重经典通知栏设置。
   */
  fun shouldForceMediaStyleNotify(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isHyperOs()
  }

  private fun detectHyperOs(): Boolean {
    return try {
      val versionName = RomUtils.getSystemProperty(PROP_OS_VERSION_NAME)
      val versionCode = RomUtils.getSystemProperty(PROP_OS_VERSION_CODE)
      !versionName.isNullOrBlank() || !versionCode.isNullOrBlank()
    } catch (_: Exception) {
      false
    }
  }

  private const val PROP_OS_VERSION_NAME = "ro.mi.os.version.name"
  private const val PROP_OS_VERSION_CODE = "ro.mi.os.version.code"
}
