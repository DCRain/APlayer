## Context

APlayer 已具备完整播放前台服务与媒体会话：

- `MusicService` 持有 `MediaSessionCompat`，由 `MediaSessionUpdater` 更新元数据与 `PlaybackState`。
- 通知实现分两路：`NotifyImpl24`（`MediaStyle` + `setMediaSession`）与经典 `NotifyImpl`（自定义 `RemoteViews`）。
- 默认在 API ≥ N 且未开启「经典通知栏」时使用 `NotifyImpl24`。

小米官方文档（《Xiaomi HyperOS 媒体通知适配说明》）明确：音乐类应用使用标准 Android 媒体通知（`MediaStyle` + `MediaSession`）即可适配系统媒体通知，并在支持设备上**自动上超级岛**；无需走通用岛模板的 `miui.focus.param` 客户端申报流程。不支持上岛时，同一套通知仍以通知栏媒体通知展示。

当前风险点：用户开启经典通知栏后，HyperOS 可能无法识别为系统媒体通知，因而无法上岛；`PlaybackState` 的 CustomAction（桌面歌词等）与通知 `addAction` 双轨并存，需与 HyperOS「播控动作以 PlaybackState 为准」的说明对齐校验。

## Goals / Non-Goals

**Goals:**

- 播放中优先走可上超级岛的标准媒体通知路径。
- 不支持超级岛或不识别为媒体通知时，回退到现有通知栏媒体通知，播控可用。
- 能力探测轻量、无新 SDK；失败路径静默回退，不影响非小米机型。
- 仅在真实播放/有效曲目场景发送/维持媒体通知（符合官方内容规范）。

**Non-Goals:**

- 不接入 MIPUSH / 通用超级岛模板（`miui.focus.param` 非媒体场景）。
- 首版不做岛上拖拽分享（`miui.focus.param.media`）。
- 不改造桌面歌词、状态栏歌词的独立通知通道（除非与媒体通知冲突必须处理）。
- 不申请/依赖小米开发者平台「通用超级岛」白名单权限作为上岛前提（媒体通道按文档走系统识别）。

## Decisions

### 1. 上岛策略：标准 MediaStyle，而非通用岛 JSON

- **选择**：继续并以 `NotifyImpl24` 路径为 HyperOS 首选——`NotificationCompat.MediaStyle().setMediaSession(token)` + 同步的 `MediaSession` 元数据/`PlaybackState`。
- **理由**：官方媒体通知文档要求使用系统 Media API；通用岛 `miui.focus.param` 面向订单/出行等模板，媒体场景另有通道且可自动上岛。
- **备选**：手写 `miui.focus.param` 媒体类模板 → 需平台场景审核与模板字段，成本高且与已有 MediaSession 重复。

### 2. 能力探测与样式选择

- **选择**：引入轻量探测（如读取 `ro.mi.os.version.name` / `ro.mi.os.version.code` 判断 HyperOS；结合 API ≥ N）。在探测为 HyperOS 且需要上岛体验时，**强制使用 MediaStyle 实现**（忽略「经典通知栏」偏好或对其做运行时覆盖并可选提示）。
- **回退**：非 HyperOS、探测失败、或系统仅展示通知栏时，行为等同今日：`NotifyImpl24` 或用户选择的 `NotifyImpl` 通知栏展示。
- **备选**：完全尊重经典通知栏设置 → 用户在小米机上可能永远上不了岛，与本需求冲突。

### 3. PlaybackState 与通知 Action 对齐

- **选择**：保证 `PlaybackState` 至少包含 play/pause、skip next/prev、seek；自定义桌面歌词动作继续通过 `CustomAction`（现有 Android 13+ 路径），与通知栏按钮语义一致。
- **理由**：HyperOS 说明播控以 `PlaybackState`（含 CustomAction）为来源；通知上的 `addAction` 仍保留以兼容非小米通知栏。
- **备选**：去掉通知 `addAction` 只靠 MediaSession → 可能影响经典机型/旧系统展示，暂不采纳。

### 4. 生命周期

- **选择**：沿用 `Notify.startForegroundOrNotify`：播放中 `startForeground`；暂停时按现有策略 detach/notify；停止/关闭通知时 `stopForegroundAndNotification`。不上岛时不额外发送第二条岛通知。
- **理由**：单一通知 ID、单一媒体会话，避免双通知抢占。

### 5. 可选增强（首版不做，设计预留）

- 在 MediaStyle 通知 `extras` 写入 `miui.focus.param.media`（分享 title/content/shareContent）以支持岛上拖拽分享；需有可分享链接策略（本地曲可能无 URL）。

## Risks / Trade-offs

- [HyperOS 版本/机型未对某包名开放媒体上岛] → 依赖系统侧策略；应用侧保证 MediaStyle 合规，失败时通知栏仍可用；真机验证并记录机型/OS 版本。
- [强制 MediaStyle 覆盖经典通知偏好] → 小米用户失去自定义 RemoteViews 外观；用设置文案说明「澎湃 OS 上为支持超级岛将使用系统媒体通知」。
- [CustomAction 图标不符合小米素材规范被隐藏] → 桌面歌词按钮可能在岛上不可见；核心播控仍可用，后续可换官方切图。
- [状态栏歌词独立 notify 干扰焦点] → 保持现有 ticker 通道独立 ID；若实测冲突再收敛。
- [误判非 HyperOS] → 探测失败一律走现有逻辑，不引入破坏性分支。

## Migration Plan

1. 实现探测与通知实现选择逻辑，默认行为对非小米设备无感。
2. 在 HyperOS 真机验证：起播上岛、暂停/切歌更新、关闭通知、经典设置被覆盖时的表现。
3. 若需回滚：恢复「仅按 `classicNotify` 选择 NotifyImpl/NotifyImpl24」，去掉 HyperOS 强制分支即可。

## Open Questions

- 经典通知栏在 HyperOS 上是「静默强制 MediaStyle」还是「设置项禁用/提示」？建议实现时采用静默强制 + 设置说明，避免打断播放。
- 是否在设置中增加显式「优先超级岛」开关？首版可不做，由系统能力自动决定。
- 本地音乐岛上拖拽分享的 `shareContent` 用什么（文件 URI / 仅文案）？留待后续变更。
