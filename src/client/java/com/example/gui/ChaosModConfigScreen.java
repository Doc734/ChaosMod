package com.example.gui;

import com.example.ChaosMod;
import com.example.network.ConfigToggleC2SPacket;
import com.example.screen.ChaosModScreenHandler;
// import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking; // Simplified
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChaosModConfigScreen extends HandledScreen<ChaosModScreenHandler> {
    private static final Map<String, String> LABELS = new LinkedHashMap<>();
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24; // MC 标准间距
    
    static {
        LABELS.put("allHostileEnabled", "所有生物敌对");
        LABELS.put("mobIgniteEnabled", "被怪命中点燃");
        LABELS.put("mobSlownessEnabled", "被怪命中缓慢II");
        LABELS.put("mobBlindnessEnabled", "被怪命中失明");
        LABELS.put("mobThornsEnabled", "反伤=50%");
        LABELS.put("foodPoisonEnabled", "吃食物概率中毒");
        LABELS.put("enderDragonBucketEnabled", "被龙打→水桶变牛奶");
        LABELS.put("enderDragonKillEnabled", "击杀末影龙者自杀");
        LABELS.put("playerDamageShareEnabled", "贴身平摊伤害");
        LABELS.put("sharedHealthEnabled", "共享生命(镜像)");
        LABELS.put("sharedDamageSplitEnabled", "全服平摊伤害");
        LABELS.put("randomDamageEnabled", "随机转移伤害");
        LABELS.put("shieldNerfEnabled", "盾牌仅吸收80%");
        LABELS.put("lowHealthNoHealEnabled", "≤1♥禁回血(10s)");
        LABELS.put("waterToLavaEnabled", "放水50%变岩浆(仅玩家)");
        LABELS.put("endKeepOverrideEnabled", "末地死亡掉落/其他维度保留物品");
        LABELS.put("reverseDamageEnabled", "反向伤害：不受伤扣血");
        LABELS.put("sunburnEnabled", "晴天白天阳光下自燃");
        LABELS.put("healReverseEnabled", "回血时50%概率变扣血");
        LABELS.put("fallTrapEnabled", "平地跳跃落地20%概率扣0.5♥");
        LABELS.put("acrophobiaEnabled", "恐高症：Y>80越高伤害越大(最高2♥)");
        LABELS.put("blockRevengeEnabled", "破坏方块10%概率被反伤");
        LABELS.put("containerCurseEnabled", "开箱子/熔炉25%概率扣1♥");
        LABELS.put("inventoryCurseEnabled", "切换物品槽12%概率扣0.5♥");
        LABELS.put("craftingTrapEnabled", "合成物品10%概率扣1♥");
    }
    
    private final List<ButtonWidget> configButtons = new ArrayList<>();
    private final boolean hasPermission;
    private int currentPage = 0;
    private final int itemsPerPage = 10; // 每页显示的按钮数（减少一些避免重叠）
    private int totalPages;
    
    public ChaosModConfigScreen(ChaosModScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, Text.empty()); // 传入空标题
        // 从 ScreenHandler 获取服务端同步的权限状态
        this.hasPermission = handler.hasPermission();
        this.backgroundWidth = 256; // MC 标准宽度
        this.backgroundHeight = 256; // MC 标准高度
    }
    
    @Override
    protected void init() {
        super.init();
        
        this.configButtons.clear();
        
        if (!hasPermission) {
            // 渲染"无管理员权限无法设置"的受限页面
            initRestrictedInterface();
        } else {
            // 渲染 MC 风格的主菜单布局
            initMainMenuStyle();
        }
    }
    
    private void initRestrictedInterface() {
        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;
        
        // 权限不足提示 - MC 风格居中
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("权限不足").formatted(Formatting.RED),
            btn -> {}
        ).dimensions(centerX - 100, startY, 200, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("需要管理员权限").formatted(Formatting.YELLOW),
            btn -> {}
        ).dimensions(centerX - 100, startY + 30, 200, 20).build());
        
        // 返回按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("返回"),
            btn -> this.close()
        ).dimensions(centerX - 100, startY + 80, 200, 20).build());
    }
    
    private void initMainMenuStyle() {
        int centerX = this.width / 2;
        int startY = 30; // 提高起始位置，因为没有标题了
        
        // 计算总页数
        totalPages = (LABELS.size() + itemsPerPage - 1) / itemsPerPage;
        
        // 按照 MC 主菜单风格排列当前页的配置按钮
        List<Map.Entry<String, String>> entries = new ArrayList<>(LABELS.entrySet());
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, entries.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<String, String> entry = entries.get(i);
            String key = entry.getKey();
            String label = entry.getValue();
            
            // 修复布局问题：使用正确的相对位置计算
            int relativeIndex = i - startIndex; // 在当前页的相对位置
            int yPos = startY + relativeIndex * BUTTON_SPACING;
            
            ButtonWidget button = ButtonWidget.builder(
                getButtonText(key, label),
                btn -> toggleConfig(key)
            )
            .dimensions(centerX - BUTTON_WIDTH / 2, yPos, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();
            
            this.addDrawableChild(button);
            this.configButtons.add(button);
        }
        
        // 控制按钮 - MC 风格底部布局
        int actualItems = Math.min(itemsPerPage, endIndex - startIndex);
        int bottomY = startY + actualItems * BUTTON_SPACING + 30; // 增加间距
        
        // 分页按钮（如果需要的话）
        if (totalPages > 1) {
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("< 上一页"),
                btn -> previousPage()
            ).dimensions(centerX - 150, bottomY, 80, 20).build());
            
            // 页码显示
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal(String.format("%d/%d", currentPage + 1, totalPages)),
                btn -> {}
            ).dimensions(centerX - 30, bottomY, 60, 20).build());
            
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("下一页 >"),
                btn -> nextPage()
            ).dimensions(centerX + 70, bottomY, 80, 20).build());
            
            bottomY += 30;
        }
        
        // 批量操作按钮
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("全部启用"),
            btn -> toggleAllConfigs(true)
        ).dimensions(centerX - 150, bottomY, 80, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("全部禁用"),
            btn -> toggleAllConfigs(false)
        ).dimensions(centerX - 40, bottomY, 80, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("完成"),
            btn -> this.close()
        ).dimensions(centerX + 70, bottomY, 80, 20).build());
    }
    
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            this.clearAndInit();
        }
    }
    
    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            this.clearAndInit();
        }
    }
    
    private Text getButtonText(String key, String label) {
        boolean enabled = ChaosMod.config.get(key);
        String state = enabled ? "✓" : "✗";
        Formatting color = enabled ? Formatting.GREEN : Formatting.WHITE;
        
        return Text.literal(state + " " + label).formatted(color);
    }
    
    private void toggleConfig(String key) {
        boolean current = ChaosMod.config.get(key);
        boolean newValue = !current;
        
        // 立即更新本地配置以提供即时反馈
        ChaosMod.config.set(key, newValue);
        
        // 在集成服务器中，直接调用服务端方法
        // 在专用服务器中需要实现 C2S 网络包
        // ConfigToggleC2SPacket.updateConfig(key, newValue, serverPlayer);
        
        // 更新按钮文本
        updateButtonTexts();
    }
    
    private void toggleAllConfigs(boolean enable) {
        for (String key : LABELS.keySet()) {
            // 立即更新本地配置
            ChaosMod.config.set(key, enable);
            
            // 在集成服务器中，直接调用服务端方法
            // 在专用服务器中需要实现 C2S 网络包
            // ConfigToggleC2SPacket.updateConfig(key, enable, serverPlayer);
        }
        
        // 更新按钮文本
        updateButtonTexts();
    }
    
    private void updateButtonTexts() {
        List<Map.Entry<String, String>> entries = new ArrayList<>(LABELS.entrySet());
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, entries.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            int buttonIndex = i - startIndex;
            if (buttonIndex < this.configButtons.size()) {
                Map.Entry<String, String> entry = entries.get(i);
                ButtonWidget button = this.configButtons.get(buttonIndex);
                button.setMessage(getButtonText(entry.getKey(), entry.getValue()));
            }
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 完全透明背景 - 不渲染任何背景，直接看到游戏世界
        
        // 去掉标题 - 不显示任何文字，避免重叠
        
        // 去掉页码信息 - 不显示任何文字，避免重叠
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // 完全透明 - 不绘制任何背景
    }
    
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 不绘制任何前景文字（包括Inventory等标签）
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}