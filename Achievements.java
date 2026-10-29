package cat.narezany.rootmymargyt;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import cat.narezany.margyt.plugin.PluginContext;

/**
 * Achievements — ачивки Clawd Run и Clawd-питомца.
 *
 * Хранятся в настройках плагина: «ach_<id>» — получена ли, «achc_<имя>» — накопленные счётчики.
 * unlock() возвращает true только в момент получения — по нему игра показывает плашку.
 */
public final class Achievements {

    public static final class Def {
        public final String id, icon, title, desc;

        Def(String id, String icon, String title, String desc) {
            this.id = id;
            this.icon = icon;
            this.title = title;
            this.desc = desc;
        }
    }

    public static final Def[] ALL = {
        new Def("first_run", "🏁", "Первый забег", "Сыграть в Clawd Run"),
        new Def("score_100", "🐣", "Разминка", "Набрать 100 очков"),
        new Def("score_500", "🐛", "Багхантер", "Набрать 500 очков"),
        new Def("score_1000", "💯", "Тысячник", "Набрать 1000 очков"),
        new Def("score_2500", "📖", "Легенда Вайбпедии", "Набрать 2500 очков"),
        new Def("night", "🌙", "Ночной бегун", "Добежать до ночи"),
        new Def("tokens_run", "🪙", "Токеноед", "Съесть 10 токенов за один забег"),
        new Def("tokens_total", "💰", "Пожиратель лимитов", "Съесть 100 токенов всего"),
        new Def("bugs_total", "🪲", "Багоборец", "Перепрыгнуть 300 багов"),
        new Def("duck_10", "🦆", "Пригнись!", "10 раз пролезть под летающим вирусом"),
        new Def("marathon", "⏱️", "Марафонец", "Продержаться 2 минуты в одном забеге"),
        new Def("max_speed", "⚡", "Скорость света", "Разогнаться до максимальной скорости"),
        new Def("quick_death", "🤡", "Это фича", "Проиграть в первые 3 секунды"),
        new Def("games_25", "🔁", "Упорный", "Сыграть 25 забегов"),
        new Def("pet_50", "🧡", "Лучший друг", "Потыкать Clawd 50 раз"),
        new Def("all_emotions", "🎭", "Эмоциональный интеллект", "Увидеть все эмоции Clawd"),
        new Def("wake_up", "⏰", "Будильник", "Разбудить спящего Clawd"),
    };

    private Achievements() {}

    public static Def find(String id) {
        for (Def d : ALL) if (d.id.equals(id)) return d;
        return null;
    }

    public static boolean has(PluginContext m, String id) {
        return m != null && m.getBoolean("ach_" + id, false);
    }

    /** @return true, если ачивка получена прямо сейчас (раньше её не было). */
    public static boolean unlock(PluginContext m, String id) {
        if (m == null || has(m, id) || find(id) == null) return false;
        m.prefs().edit().putBoolean("ach_" + id, true).apply();
        ClawdPetView.react(ClawdPetView.EV_ACHIEVEMENT, 0, find(id).title);
        return true;
    }

    public static int count(PluginContext m) {
        int n = 0;
        for (Def d : ALL) if (has(m, d.id)) n++;
        return n;
    }

    public static int counter(PluginContext m, String name) {
        return m != null ? m.getInt("achc_" + name, 0) : 0;
    }

    /** Прибавляет к счётчику и возвращает новое значение. */
    public static int add(PluginContext m, String name, int delta) {
        if (m == null || delta == 0) return counter(m, name);
        int v = counter(m, name) + delta;
        m.prefs().edit().putInt("achc_" + name, v).apply();
        return v;
    }

    /** Для мест без своей плашки (питомец): короткое уведомление. */
    public static void unlockWithToast(Activity activity, PluginContext m, String id) {
        if (!unlock(m, id) || activity == null) return;
        Def d = find(id);
        try {
            Toast.makeText(activity, "🏆 Ачивка: " + d.title, Toast.LENGTH_SHORT).show();
        } catch (Throwable ignored) {}
    }

    // ───────────────────────────── экран ачивок

    public static void show(Activity activity, PluginContext m) {
        if (activity == null || activity.isFinishing()) return;
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        int surface = 0xFF1E1E28;
        int accent = 0xFF8B5CF6;
        try {
            if (m != null) accent = m.accentColour() | 0xFF000000;
        } catch (Throwable ignored) {}

        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = M3X.dp(activity, 20);
        box.setPadding(p, p, p, p);
        box.setBackground(M3X.shape(surface, M3X.dp(activity, 28)));

        TextView title = new TextView(activity);
        title.setText("Ачивки · " + count(m) + " из " + ALL.length);
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        title.setPadding(0, 0, 0, M3X.dp(activity, 12));
        box.addView(title);

        LinearLayout list = new LinearLayout(activity);
        list.setOrientation(LinearLayout.VERTICAL);
        float big = M3X.dp(activity, 22), small = M3X.dp(activity, 6);
        for (int i = 0; i < ALL.length; i++) {
            Def d = ALL[i];
            boolean got = has(m, d.id);
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(M3X.dp(activity, 14), M3X.dp(activity, 12), M3X.dp(activity, 14), M3X.dp(activity, 12));
            float top = i == 0 ? big : small, bottom = i == ALL.length - 1 ? big : small;
            row.setBackground(M3X.shape(got ? M3X.tonal(accent, surface) : 0x12FFFFFF,
                new float[] {top, top, top, top, bottom, bottom, bottom, bottom}));

            TextView icon = new TextView(activity);
            icon.setText(got ? d.icon : "🔒");
            icon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            icon.setGravity(Gravity.CENTER);
            icon.setAlpha(got ? 1f : 0.55f);
            row.addView(icon, new LinearLayout.LayoutParams(M3X.dp(activity, 44), M3X.dp(activity, 44)));

            LinearLayout texts = new LinearLayout(activity);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.setPadding(M3X.dp(activity, 12), 0, 0, 0);
            TextView t = new TextView(activity);
            t.setText(d.title);
            t.setTextColor(got ? 0xFFFFFFFF : 0x99FFFFFF);
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            t.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            texts.addView(t);
            TextView s = new TextView(activity);
            s.setText(d.desc);
            s.setTextColor(got ? 0xCCFFFFFF : 0x66FFFFFF);
            s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            texts.addView(s);
            row.addView(texts, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.topMargin = M3X.dp(activity, 2);
            list.addView(row, lp);
        }
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(list);
        int maxH = Math.round(activity.getResources().getDisplayMetrics().heightPixels * 0.62f);
        box.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, maxH));

        TextView close = M3X.button(activity, "Закрыть", M3X.TONAL, accent, surface);
        close.setOnClickListener(v -> dialog.dismiss());
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.topMargin = M3X.dp(activity, 14);
        box.addView(close, clp);

        dialog.setContentView(box);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int w = Math.round(activity.getResources().getDisplayMetrics().widthPixels * 0.92f);
            dialog.getWindow().setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.show();
    }
}
