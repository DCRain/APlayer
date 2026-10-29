## 1. HyperOS 探测与通知路径选择

- [x] 1.1 新增 HyperOS 能力探测工具（读取 `ro.mi.os.version.*` 等系统属性，失败时视为不支持）
- [x] 1.2 在 `MusicService` 通知实现选择处接入探测：HyperOS 上优先/强制 `NotifyImpl24`（MediaStyle），非 HyperOS 保持现有 `classicNotify` 逻辑
- [x] 1.3 处理设置中切换「经典通知栏」时的行为：HyperOS 上不回退到 `NotifyImpl`，或切换后立即按探测结果重建通知

## 2. MediaStyle / MediaSession 合规校对

- [x] 2.1 核对 `NotifyImpl24`：`MediaStyle.setMediaSession`、compact actions、标题/副标题/封面、ongoing 与播放态一致
- [x] 2.2 核对 `MediaSessionUpdater`：`PlaybackState` 含 play/pause、skip、seek；元数据与当前曲目同步；切歌/暂停/恢复时与通知同步更新
- [x] 2.3 确认仅在有效曲目且用户处于播放会话时发送/维持播放通知，关闭通知路径仍调用 `stopForegroundAndNotification`

## 3. 设置与文案（如需要）

- [x] 3.1 为 HyperOS 用户补充设置说明（经典通知栏在澎湃 OS 上可能被系统媒体通知覆盖以支持超级岛）
- [x] 3.2 如设置 UI 需禁用或提示经典样式，完成对应 preference 交互

## 4. 验证

- [ ] 4.1 在 HyperOS（支持超级岛）真机验证：起播上岛、切歌更新、暂停/恢复、关闭通知、从岛/通知栏播控
- [ ] 4.2 在非小米或探测失败路径验证：通知栏媒体通知与播控正常，无崩溃
- [ ] 4.3 回归：状态栏歌词通知、桌面歌词 CustomAction、前台服务类型 `MEDIA_PLAYBACK` 仍正常
