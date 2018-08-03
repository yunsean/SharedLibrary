package com.dylan.common.service;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.List;

public class SelfProtect {
    
    //Android 6.0+ Doze 模式
    public static final int DOZE = 98;
    //华为 自启动管理
    public static final int HUAWEI = 99;
    //华为 受保护的应用
    public static final int HUAWEI_GOD = 100;
    //小米 自启动管理
    public static final int XIAOMI = 101;
    //小米 神隐模式 (建议只在 App 的核心功能需要后台连接网络/后台定位的情况下使用)
    public static final int XIAOMI_GOD = 102;
    //三星 5.0+ 智能管理器
    public static final int SAMSUNG = 103;
    //魅族 自启动管理
    public static final int MEIZU = 104;
    //魅族 待机耗电管理
    public static final int MEIZU_GOD = 105;
    //Oppo 自启动管理
    public static final int OPPO = 106;
    //Oppo 纯净后台应用管控
    public static final int OPPO_GOD = 107;
    //Vivo 自启动管理
    public static final int VIVO = 108;
    //Vivo 后台高耗电
    public static final int VIVO_GOD = 109;
    //金立 应用自启
    public static final int GIONEE = 110;
    //乐视 自启动管理
    public static final int LETV = 111;
    //乐视 应用保护
    public static final int LETV_GOD = 112;
    //酷派 自启动管理
    public static final int COOLPAD = 113;
    //联想 后台管理
    public static final int LENOVO = 114;
    //联想 后台耗电优化
    public static final int LENOVO_GOD = 115;
    //中兴 自启管理
    public static final int ZTE = 116;
    //中兴 锁屏加速受保护应用
    public static final int ZTE_GOD = 117;

    private class ProtectIntent {
        public ProtectIntent(Intent intent, int type, String title, String content) {
            this.intent = intent;
            this.type = type;
            this.title = title;
            this.content = content;
        }
        Intent intent;
        int type;
        String title;
        String content;
        public void startActivity(Context context) {
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public boolean doesActivityExists(Context context) {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list != null && list.size() > 0;
        }
    }
    public List<ProtectIntent> protectIntents = new ArrayList<>();
    
    public void init(Context context) {
        String applicationName = getApplicationName(context);
        protectIntents.clear();

        //Android 6.0+ Doze 模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            boolean ignoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(context.getPackageName());
            if (!ignoringBatteryOptimizations) {
                Intent dozeIntent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                dozeIntent.setData(Uri.parse("package:" + context.getPackageName()));
                protectIntents.add(new ProtectIntent(dozeIntent, DOZE,
                        "需要忽略 " + applicationName + " 的电池优化",
                        "需要 " + applicationName + " 加入到电池优化的忽略名单。\n\n请点击『确定』，在弹出的『忽略电池优化』对话框中，选择『是』。"));
            }
        }

        //华为 自启动管理
        Intent huaweiIntent = new Intent();
        huaweiIntent.setAction("huawei.intent.action.HSM_BOOTAPP_MANAGER");
        protectIntents.add(new ProtectIntent(huaweiIntent, HUAWEI,
                "需要允许 " + applicationName + " 自动启动",
                "需要允许 " + applicationName + " 的后台自动启动。\n\n请点击『确定』，在弹出的『自动启动管理』中，将 " + applicationName + " 对应的开关打开。"));

        //华为 受保护的应用
        Intent huaweiGodIntent = new Intent();
        huaweiGodIntent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
        protectIntents.add(new ProtectIntent(huaweiGodIntent, HUAWEI_GOD,
                "" + applicationName + " 需要加入受保护的应用名单",
                "需要 " + applicationName + " 加入到受保护的应用名单。\n\n请点击『确定』，在弹出的『受保护应用』列表中，将 " + applicationName + " 对应的开关打开。"));

        //小米 自启动管理
        Intent xiaomiIntent = new Intent();
        xiaomiIntent.setAction("miui.intent.action.OP_AUTO_START");
        xiaomiIntent.addCategory(Intent.CATEGORY_DEFAULT);
        protectIntents.add(new ProtectIntent(xiaomiIntent, XIAOMI,
                "需要允许 " + applicationName + " 的自启动",
                "需要 " + applicationName + " 加入到自启动白名单。\n\n请点击『确定』，在弹出的『自启动管理』中，将 " + applicationName + " 对应的开关打开。"));

        //小米 神隐模式 (建议只在 App 的核心功能需要后台连接网络/后台定位的情况下使用)
        Intent xiaomiGodIntent = new Intent();
        xiaomiGodIntent.setComponent(new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity"));
        protectIntents.add(new ProtectIntent(xiaomiGodIntent, XIAOMI_GOD,
                "需要关闭 " + applicationName + " 的神隐模式",
                "需要 " + applicationName + " 的神隐模式关闭。\n\n请点击『确定』，在弹出的神隐模式应用列表中，点击 " + applicationName + " ，然后选择『无限制』和『允许定位』。"));

        //三星 5.0+ 智能管理器
        Intent samsungIntent = context.getPackageManager().getLaunchIntentForPackage("com.samsung.android.sm");
        if (samsungIntent != null) protectIntents.add(new ProtectIntent(samsungIntent, SAMSUNG,
                "需要允许 " + applicationName + " 的自启动",
                "需要 " + applicationName + " 在屏幕关闭时继续运行。\n\n请点击『确定』，在弹出的『智能管理器』中，点击『内存』，选择『自启动应用程序』选项卡，将 " + applicationName + " 对应的开关打开。"));

        //魅族 自启动管理
        Intent meizuIntent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        meizuIntent.addCategory(Intent.CATEGORY_DEFAULT);
        meizuIntent.putExtra("packageName", context.getPackageName());
        protectIntents.add(new ProtectIntent(meizuIntent, MEIZU,
                "需要允许 " + applicationName + " 的自启动",
                "需要允许 " + applicationName + " 的自启动。\n\n请点击『确定』，在弹出的应用信息界面中，将『自启动』开关打开。"));

        //魅族 待机耗电管理
        Intent meizuGodIntent = new Intent();
        meizuGodIntent.setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.powerui.AppPowerManagerActivity"));
        protectIntents.add(new ProtectIntent(meizuGodIntent, MEIZU_GOD,
                "" + applicationName + " 需要在待机时保持运行",
                "需要 " + applicationName + " 在待机时保持运行。\n\n请点击『确定』，在弹出的『待机耗电管理』中，将 " + applicationName + " 对应的开关打开。"));

        //Oppo 自启动管理
        Intent oppoIntent = new Intent();
        oppoIntent.setComponent(new ComponentName("com.color.safecenter", "com.color.safecenter.permission.startup.StartupAppListActivity"));
        protectIntents.add(new ProtectIntent(oppoIntent, OPPO,
                "需要允许 " + applicationName + " 的自启动",
                "需要 " + applicationName + " 加入到自启动白名单。\n\n请点击『确定』，在弹出的『自启动管理』中，将 " + applicationName + " 对应的开关打开。"));

        //Oppo 纯净后台应用管控
        Intent oppoGodIntent = new Intent();
        oppoGodIntent.setComponent(new ComponentName("com.color.safecenter", "com.color.purebackground.PureBackgroundSettingActivity"));
        protectIntents.add(new ProtectIntent(oppoGodIntent, OPPO_GOD,
                "需要允许 " + applicationName + " 在后台运行",
                "需要允许 " + applicationName + " 在后台运行。\n\n请点击『确定』，在弹出的『纯净后台应用管控』中，将 " + applicationName + " 对应的开关打开。"));

        //Vivo 自启动管理
        Intent vivoIntent = new Intent();
        vivoIntent.setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.MainActivity"));
        protectIntents.add(new ProtectIntent(vivoIntent, VIVO,
                "需要允许 " + applicationName + " 的自启动",
                "需要允许 " + applicationName + " 的自启动。\n\n请点击『确定』，在弹出的 i管家 中，找到『软件管理』->『自启动管理』，将 " + applicationName + " 对应的开关打开。"));

        //Vivo 后台高耗电
        Intent vivoGodIntent = new Intent();
        vivoGodIntent.setComponent(new ComponentName("com.vivo.abe", "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"));
        protectIntents.add(new ProtectIntent(vivoGodIntent, VIVO_GOD,
                "" + applicationName + " 需要在后台高耗电时允许运行",
                "需要允许 " + applicationName + " 在后台高耗电时运行。\n\n请点击『确定』，在弹出的『后台高耗电』中，将 " + applicationName + " 对应的开关打开。"));

        //金立 应用自启
        Intent gioneeIntent = new Intent();
        gioneeIntent.setComponent(new ComponentName("com.gionee.softmanager", "com.gionee.softmanager.MainActivity"));
        protectIntents.add(new ProtectIntent(gioneeIntent, GIONEE,
                "" + applicationName + " 需要加入应用自启和绿色后台白名单",
                "需要允许 " + applicationName + " 的自启动和后台运行。\n\n请点击『确定』，在弹出的『系统管家』中，分别找到『应用管理』->『应用自启』和『绿色后台』->『清理白名单』，将 " + applicationName + " 添加到白名单。"));

        //乐视 自启动管理
        Intent letvIntent = new Intent();
        letvIntent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
        protectIntents.add(new ProtectIntent(letvIntent, LETV,
                "需要允许 " + applicationName + " 的自启动",
                "需要 " + applicationName + " 加入到自启动白名单。\n\n请点击『确定』，在弹出的『自启动管理』中，将 " + applicationName + " 对应的开关打开。"));

        //乐视 应用保护
        Intent letvGodIntent = new Intent();
        letvGodIntent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.BackgroundAppManageActivity"));
        protectIntents.add(new ProtectIntent(letvGodIntent, LETV_GOD,
                "需要禁止 " + applicationName + " 被自动清理",
                "需要禁止 " + applicationName + " 被自动清理。\n\n请点击『确定』，在弹出的『应用保护』中，将 " + applicationName + " 对应的开关关闭。"));

        //酷派 自启动管理
        Intent coolpadIntent = new Intent();
        coolpadIntent.setComponent(new ComponentName("com.yulong.android.security", "com.yulong.android.seccenter.tabbarmain"));
        protectIntents.add(new ProtectIntent(coolpadIntent, COOLPAD,
                "需要允许 " + applicationName + " 的自启动",
                "需要允许 " + applicationName + " 的自启动。\n\n请点击『确定』，在弹出的『酷管家』中，找到『软件管理』->『自启动管理』，取消勾选 " + applicationName + "，将 " + applicationName + " 的状态改为『已允许』。"));

        //联想 后台管理
        Intent lenovoIntent = new Intent();
        lenovoIntent.setComponent(new ComponentName("com.lenovo.security", "com.lenovo.security.purebackground.PureBackgroundActivity"));
        protectIntents.add(new ProtectIntent(lenovoIntent, LENOVO,
                "需要允许 " + applicationName + " 的后台 GPS 和后台运行",
                "需要允许 " + applicationName + " 的后台自启和后台运行。\n\n请点击『确定』，在弹出的『后台管理』中，分别找到『后台自启』和『后台运行』，将 " + applicationName + " 对应的开关打开。"));

        //联想 后台耗电优化
        Intent lenovoGodIntent = new Intent();
        lenovoGodIntent.setComponent(new ComponentName("com.lenovo.powersetting", "com.lenovo.powersetting.ui.Settings$HighPowerApplicationsActivity"));
        protectIntents.add(new ProtectIntent(lenovoGodIntent, LENOVO_GOD,
                "需要关闭 " + applicationName + " 的后台耗电优化",
                "需要关闭 " + applicationName + " 的后台耗电优化。\n\n请点击『确定』，在弹出的『后台耗电优化』中，将 " + applicationName + " 对应的开关关闭。"));

        //中兴 自启管理
        Intent zteIntent = new Intent();
        zteIntent.setComponent(new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.autorun.AppAutoRunManager"));
        protectIntents.add(new ProtectIntent(zteIntent, ZTE,
                "需要允许 " + applicationName + " 的自启动",
                "需要 " + applicationName + " 加入到自启动白名单。\n\n请点击『确定』，在弹出的『自启动管理』中，将 " + applicationName + " 对应的开关打开。"));

        //中兴 锁屏加速受保护应用
        Intent zteGodIntent = new Intent();
        zteGodIntent.setComponent(new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.setting.ClearAppSettingsActivity"));
        protectIntents.add(new ProtectIntent(zteGodIntent, ZTE_GOD,
                "" + applicationName + " 需要加入受保护的应用名单",
                "需要 " + applicationName + " 加入到受保护的应用名单。\n\n请点击『确定』，在弹出的『受保护应用』列表中，将 " + applicationName + " 对应的开关打开。"));
    }

    public boolean hasProtect(Context context) {
        boolean nothingMatches = true;
        for (final ProtectIntent intentWrapper : protectIntents) {
            if (!intentWrapper.doesActivityExists(context)) continue;
            nothingMatches = false;
        }
        return !nothingMatches;
    }
    public void remindProtect(Context context, String action) {
        if (action == null || action.trim().length() < 1) {
            action = getApplicationName(context) + "的持续运行";
        }
        remainProtect(context, action, 0);
    }
    private void remainProtect(final Context context, final String action, int index) {
        if (index < 0 || index >= protectIntents.size()) {
            return;
        }
        for (int i = index; i < protectIntents.size(); i++) {
            final ProtectIntent intent = protectIntents.get(i);
            if (intent.doesActivityExists(context)) {
                final int _index = i;
                new AlertDialog.Builder(context)
                        .setCancelable(true)
                        .setTitle(intent.title)
                        .setMessage(action + intent.content)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                intent.startActivity(context);
                                remainProtect(context, action, _index + 1);
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                remainProtect(context, action, _index + 1);
                            }
                        })
                        .show();
                break;
            }
        }
    }

    public String getApplicationName(Context context) {
        PackageManager packageManager;
        ApplicationInfo applicationInfo;
        try {
            packageManager = context.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            String name = packageManager.getApplicationLabel(applicationInfo).toString();
            return name;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return context.getPackageName();
        }
    }

    private static SelfProtect mInstance = null;
    public static SelfProtect getDefault() {
        synchronized (SelfProtect.class) {
            if (mInstance == null) {
                mInstance = new SelfProtect();
            }
        }
        return mInstance;
    }
}
