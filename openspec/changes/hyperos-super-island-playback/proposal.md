## Why

播放中的前台媒体通知是用户切出应用后的主要控制入口。小米澎湃 OS（HyperOS）提供「超级岛」媒体能力：符合系统媒体通知规范的播放会话可自动上岛，获得更高曝光与系统播控体验；不支持超级岛的设备/系统仍应继续走现有通知栏媒体通知，不能影响非小米机型的可用性。

## What Changes

- 在**正在播放**时，优先适配 HyperOS 超级岛媒体通知（标准 `MediaStyle` + `MediaSession`，由系统决定是否上岛）。
- 在不支持超级岛的设备、旧版 MIUI、或系统未识别为媒体通知时，**回退为现有通知栏媒体通知**，保持播放/暂停、上下首、关闭等行为不变。
- 校对并必要时增强现有 `MediaSession` / `PlaybackState` / `NotifyImpl24` 与 HyperOS 媒体通知要求的一致性（元数据、动作来源、仅在真实播放场景发送）。
- 处理「经典通知栏」样式（`NotifyImpl` RemoteViews）与超级岛互斥关系：支持超级岛时走 MediaStyle 路径，否则保留用户已选样式/通知栏回退。
- 本变更**不**引入 MIPUSH 云侧岛通知，也**不**强制接入通用 `miui.focus.param` 非媒体模板；可选的岛上拖拽分享（`miui.focus.param.media`）列为后续增强，不在首版必做范围。

## Capabilities

### New Capabilities

- `hyperos-media-island`: 播放态媒体通知在 HyperOS 超级岛与普通通知栏之间的能力选择、上岛条件与回退行为。

### Modified Capabilities

- （无）当前 `openspec/specs/` 下无既有能力规格。

## Impact

- **代码**：`Notify` / `NotifyImpl24` / `NotifyImpl`、`MusicService` 通知样式切换、`MediaSessionUpdater` 与播放生命周期中的通知更新路径。
- **设置**：可能影响「经典通知栏」在 HyperOS 上的生效策略（支持超级岛时优先 MediaStyle）。
- **系统依赖**：依赖设备是否为 HyperOS 且系统对标准媒体通知的上岛支持；无新第三方 SDK；无需开通通用超级岛客户端模板权限（媒体通知按官方说明可走系统媒体通道自动上岛）。
- **测试**：需在 HyperOS（支持超级岛）与非小米/不支持设备上分别验证播放中展示与播控。
