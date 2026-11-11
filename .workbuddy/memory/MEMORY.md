# 项目记忆：to-full-stack（6年前端转 Java 全栈系列）

## 文章规范
- 系列标题格式：`Day NN｜前端转全栈第 N 天：<JS→Java 差异点>`，写入文章开头 H1，文件重命名为同名 .md。
- 封面大字套路：前端写法（红色删除线）→ Java 写法（红色），等宽字体。已用：let→int / if(1)→if(true) / push()→int[5] / prototype→extends / throw "err"→new Exception() / ===→equals() / event loop→new Thread() / new Set()→new HashSet() / array.map()→stream().map() / readFile→FileInputStream / "// 注释"→@注解。大字超过 ~21 个等宽字符（112px 字号）会溢出右边，需缩字号（84~96px）或缩短文案；26 字符在 100px 刚好放下。
- **对照梗必须先验 JS 现状**（ES6 有 Set/Map/WeakMap），别写"JS 没有所以 Java 才有"式表述；两边一致的点要明说"一致"。
- 文章定稿风格（Day05 起确认）：markdown 列表/表格替代代码块笔记、💬 第一人称感受引用块 2~3 处、文末「今日学习总结」+ 下篇预告钩子。

## 封面规范（2026-11-06 更新）
- **每篇只出一张图**：`cover-dayNN.png`（2350×1000，2.35:1 公众号消息列表）。1:1（转发卡片/公众号主页）用户自己裁剪，不要主动出方版。
- 模板：covers/cover-dayNN.html，Chrome headless 渲染（必须加 --no-sandbox --disable-gpu --disable-software-rasterizer）。
- 视觉体系：米白底 #F6F3EC + 红 #E4391F + 黑 #16150F，右侧红书脊，SF Mono 小字标签，PingFang SC 大字。
- 布局注意：主体内容（DAY 编号 + 大字）集中在画面中部偏左，别贴边，方便用户裁 1:1。

## 渲染命令模板
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --headless=new --no-sandbox --disable-gpu --disable-software-rasterizer --hide-scrollbars --force-device-scale-factor=1 --window-size=W,H --screenshot=out.png "file:///path/cover.html"
