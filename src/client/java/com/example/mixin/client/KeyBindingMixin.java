package com.example.mixin.client;

import com.example.ChaosModClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
    
    @Shadow
    public abstract String getTranslationKey();
    
    @Shadow 
    public abstract boolean isPressed();
    
    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    private void chaos$interceptIsPressed(CallbackInfoReturnable<Boolean> cir) {
        // 如果正在重置，允许真实状态通过
        if (ChaosModClient.isResetting()) {
            return;
        }
        
        String translationKey = this.getTranslationKey();
        String keyType = getKeyTypeFromTranslationKey(translationKey);
        
        if (keyType != null && ChaosModClient.isKeyDisabled(keyType)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
    
    @Inject(method = "wasPressed", at = @At("HEAD"), cancellable = true)
    private void chaos$interceptWasPressed(CallbackInfoReturnable<Boolean> cir) {
        // 如果正在重置，允许真实状态通过
        if (ChaosModClient.isResetting()) {
            return;
        }
        
        String translationKey = this.getTranslationKey();
        String keyType = getKeyTypeFromTranslationKey(translationKey);
        
        if (keyType != null && ChaosModClient.isKeyDisabled(keyType)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
    
    @Inject(method = "setPressed", at = @At("HEAD"), cancellable = true)
    private void chaos$interceptSetPressed(boolean pressed, CallbackInfo ci) {
        // 如果正在重置，允许所有操作通过
        if (ChaosModClient.isResetting()) {
            return;
        }
        
        String translationKey = this.getTranslationKey();
        String keyType = getKeyTypeFromTranslationKey(translationKey);
        
        // 如果按键被禁用，阻止设置为按下状态
        if (pressed && keyType != null && ChaosModClient.isKeyDisabled(keyType)) {
            ci.cancel();
        }
    }
    
    /**
     * 根据翻译键确定按键类型
     */
    private String getKeyTypeFromTranslationKey(String translationKey) {
        return switch (translationKey) {
            case "key.forward" -> "forward";
            case "key.back" -> "back";  
            case "key.left" -> "left";
            case "key.right" -> "right";
            case "key.jump" -> "jump";
            case "key.sprint" -> "sprint";
            case "key.attack" -> "attack";
            case "key.use" -> "use";
            case "key.drop" -> "drop";
            case "key.sneak" -> "sneak";
            default -> null;
        };
    }
}
