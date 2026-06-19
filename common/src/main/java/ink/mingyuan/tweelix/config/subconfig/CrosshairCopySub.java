package ink.mingyuan.tweelix.config.subconfig;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import ink.mingyuan.tweelix.Reference;
import ink.mingyuan.tweelix.feature.CrosshairCopyHandler;
import java.util.List;

public class CrosshairCopySub{

    private static final String TRANSLATION_KEY = Reference.MOD_ID + ".sub_config.crosshair_copy";

    public static final ConfigOptionList TARGET_COPY_MODE =
            new ConfigOptionList("targetCopyMode",
                    CrosshairCopyHandler.TargetCopyMode.REGISTRY_NAME,
                    "Sets which part of the targeted block/item will be copied by default")
                    .apply(TRANSLATION_KEY);

    public static final ConfigBoolean SEND_ALL_COPYABLE =
            new ConfigBoolean("sendAllCopyable", false,
                    "Display all copyable information in chat")
                    .apply(TRANSLATION_KEY);

    public static final List<IConfigBase> OPTIONS = ImmutableList.of(TARGET_COPY_MODE, SEND_ALL_COPYABLE);

}