// 
// Decompiled by Procyon v0.5.36
// 

package me.zeroeightsix.kami.gui.kami.component;

import java.util.Iterator;
import java.util.Arrays;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton.CheckButtonPoof;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton;
import me.zeroeightsix.kami.gui.rgui.poof.IPoof;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.setting.impl.numerical.DoubleSetting;
import me.zeroeightsix.kami.setting.impl.numerical.FloatSetting;
import me.zeroeightsix.kami.gui.rgui.component.use.Slider.SliderPoof;
import me.zeroeightsix.kami.gui.rgui.component.use.Slider;
import me.zeroeightsix.kami.setting.impl.numerical.IntegerSetting;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.util.Bind;
import me.zeroeightsix.kami.setting.impl.EnumSetting;
import me.zeroeightsix.kami.setting.impl.BooleanSetting;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.gui.rgui.layout.Layout;
import me.zeroeightsix.kami.gui.kami.Stretcherlayout;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.gui.rgui.component.container.OrganisedContainer;

public class SettingsPanel extends OrganisedContainer
{
    Module module;
    
    public SettingsPanel(final Theme theme, final Module module) {
        super(theme, new Stretcherlayout(1));
        this.setAffectLayout(false);
        this.module = module;
        this.prepare();
    }
    
    @Override
    public void renderChildren() {
        super.renderChildren();
    }
    
    public Module getModule() {
        return this.module;
    }
    
    private void prepare() {
        this.getChildren().clear();
        if (this.module == null) {
            this.setVisible(false);
            return;
        }
        if (!this.module.settingList.isEmpty()) {
            for (final Setting setting : this.module.settingList) {
                if (!setting.isVisible()) {
                    continue;
                }
                final String name = setting.getName();
                final boolean isNumber = setting instanceof NumberSetting;
                final boolean isBoolean = setting instanceof BooleanSetting;
                final boolean isEnum = setting instanceof EnumSetting;
                if (setting.getValue() instanceof Bind) {
                    this.addChild(new BindButton("Bind", this.module));
                }
                if (isNumber) {
                    final NumberSetting numberSetting = (NumberSetting)setting;
                    final boolean isBound = numberSetting.isBound();
                    final double value = Double.parseDouble(numberSetting.getValue().toString());
                    if (!isBound) {
                        final UnboundSlider slider = new UnboundSlider(value, name, setting instanceof IntegerSetting);
                        slider.addPoof(new Slider.SliderPoof<UnboundSlider, Slider.SliderPoof.SliderPoofInfo>() {
                            @Override
                            public void execute(final UnboundSlider component, final SliderPoofInfo info) {
                                if (setting instanceof IntegerSetting) {
                                    setting.setValue((int)info.getNewValue());
                                }
                                else if (setting instanceof FloatSetting) {
                                    setting.setValue((float)info.getNewValue());
                                }
                                else if (setting instanceof DoubleSetting) {
                                    setting.setValue(info.getNewValue());
                                }
                                SettingsPanel.this.setModule(SettingsPanel.this.module);
                            }
                        });
                        if (numberSetting.getMax() != null) {
                            slider.setMax(numberSetting.getMax().doubleValue());
                        }
                        if (numberSetting.getMin() != null) {
                            slider.setMin(numberSetting.getMin().doubleValue());
                        }
                        this.addChild(slider);
                    }
                    else {
                        final double min = Double.parseDouble(numberSetting.getMin().toString());
                        final double max = Double.parseDouble(numberSetting.getMax().toString());
                        final Slider slider2 = new Slider(value, min, max, Slider.getDefaultStep(min, max), name, setting instanceof IntegerSetting);
                        slider2.addPoof(new Slider.SliderPoof<Slider, Slider.SliderPoof.SliderPoofInfo>() {
                            @Override
                            public void execute(final Slider component, final SliderPoofInfo info) {
                                if (setting instanceof IntegerSetting) {
                                    setting.setValue((int)info.getNewValue());
                                }
                                else if (setting instanceof FloatSetting) {
                                    setting.setValue((float)info.getNewValue());
                                }
                                else if (setting instanceof DoubleSetting) {
                                    setting.setValue(info.getNewValue());
                                }
                            }
                        });
                        this.addChild(slider2);
                    }
                }
                else if (isBoolean) {
                    final CheckButton checkButton = new CheckButton(name);
                    checkButton.setToggled(((BooleanSetting)setting).getValue());
                    checkButton.addPoof(new CheckButton.CheckButtonPoof<CheckButton, CheckButton.CheckButtonPoof.CheckButtonPoofInfo>() {
                        @Override
                        public void execute(final CheckButton checkButton1, final CheckButtonPoofInfo info) {
                            if (info.getAction() == CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE) {
                                setting.setValue(checkButton.isToggled());
                                SettingsPanel.this.setModule(SettingsPanel.this.module);
                            }
                        }
                    });
                    this.addChild(checkButton);
                }
                else {
                    if (!isEnum) {
                        continue;
                    }
                    final Class<? extends Enum> type = ((EnumSetting)setting).clazz;
                    final Object[] con = (Object[])type.getEnumConstants();
                    final String[] modes = Arrays.stream(con).map(o -> o.toString().toUpperCase()).toArray(String[]::new);
                    final EnumButton enumbutton = new EnumButton(name, modes);
                    enumbutton.addPoof(new EnumButton.EnumbuttonIndexPoof<EnumButton, EnumButton.EnumbuttonIndexPoof.EnumbuttonInfo>() {
                        @Override
                        public void execute(final EnumButton component, final EnumbuttonInfo info) {
                            setting.setValue(con[info.getNewIndex()]);
                            SettingsPanel.this.setModule(SettingsPanel.this.module);
                        }
                    });
                    enumbutton.setIndex(Arrays.asList(con).indexOf(setting.getValue()));
                    this.addChild(enumbutton);
                }
            }
        }
        if (this.children.isEmpty()) {
            this.setVisible(false);
            return;
        }
        this.setVisible(true);
    }
    
    public void setModule(final Module module) {
        this.module = module;
        this.setMinimumWidth((int)(this.getParent().getWidth() * 0.9f));
        this.prepare();
        this.setAffectLayout(false);
        for (final Component component : this.children) {
            component.setWidth(this.getWidth() - 10);
            component.setX(5);
        }
    }
}
