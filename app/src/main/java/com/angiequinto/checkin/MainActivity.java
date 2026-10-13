package com.angiequinto.checkin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** A small, private space for a check-in and its on-device history with exact Lovable AI visual fidelity. */
public class MainActivity extends android.app.Activity {
    private static final int REQUEST_NOTIFICATIONS = 31;

    private static final int TAB_CHECKIN = 0;
    private static final int TAB_HISTORY = 1;
    private static final int TAB_RHYTHM = 2;
    private int currentTab = TAB_CHECKIN;

    private final int canvas = Color.rgb(246, 249, 246);
    private final int card = Color.WHITE;
    private final int ink = Color.rgb(18, 24, 22);
    private final int mutedInk = Color.rgb(82, 99, 91);
    private final int sage = Color.rgb(208, 230, 211);
    private final int sageInk = Color.rgb(22, 58, 44);
    private final int sageSubtext = Color.rgb(35, 77, 60);
    private final int lavender = Color.rgb(227, 222, 250);
    private final int lavenderInk = Color.rgb(61, 47, 107);
    private final int lavenderSubtext = Color.rgb(82, 68, 130);
    private final int actionCardBg = Color.rgb(239, 243, 240);
    private final int line = Color.rgb(226, 235, 228);

    private TextView reassurance;
    private LinearLayout reassuranceContainer;
    private View okayButton;
    private View notOkayButton;
    private View undoButton;
    private TextView reminderDetail;
    private TextView intervalDetail;
    private TextView welcomeIntervalDetail;
    private LinearLayout historyPreviewEntries;
    private TextView historyPreviewEmpty;
    private LinearLayout statisticsCardContainer;
    private Switch reminderSwitch;
    private boolean settingSwitch;
    private long welcomeInterval = UserPreferences.HOUR_MS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(canvas);
        getWindow().setNavigationBarColor(canvas);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (UserPreferences.isSetupComplete(this)) {
            renderCurrentTab();
        } else {
            showWelcomeScreen();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (reminderSwitch != null) {
            updateReminderCopy();
        }
        refreshHistoryPreview();
    }

    private void renderCurrentTab() {
        RelativeLayout root = new RelativeLayout(this);
        root.setBackgroundColor(canvas);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(canvas);
        LinearLayout page = vertical();
        page.setPadding(dp(24), dp(28), dp(24), dp(90)); // Bottom padding for navigation bar
        scrollView.addView(page, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        RelativeLayout.LayoutParams scrollParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        scrollParams.addRule(RelativeLayout.ABOVE, 9999);
        root.addView(scrollView, scrollParams);

        if (currentTab == TAB_CHECKIN) {
            buildCheckInScreen(page);
        } else if (currentTab == TAB_HISTORY) {
            buildHistoryScreen(page);
        } else if (currentTab == TAB_RHYTHM) {
            buildRhythmScreen(page);
        }

        View navBar = createBottomNavigationBar();
        navBar.setId(9999);
        RelativeLayout.LayoutParams navParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        navParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        root.addView(navBar, navParams);

        setContentView(root);
    }

    private View createBottomNavigationBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setBackgroundColor(canvas);
        bar.setPadding(dp(12), dp(10), dp(12), dp(16));

        bar.addView(createNavItem("Check in", TAB_CHECKIN), navItemParams());
        bar.addView(createNavItem("History", TAB_HISTORY), navItemParams());
        bar.addView(createNavItem("Rhythm", TAB_RHYTHM), navItemParams());

        // Top subtle divider line for nav bar
        LinearLayout container = vertical();
        View divider = new View(this);
        divider.setBackgroundColor(line);
        container.addView(divider, fixedHeightMargins(1, 0, 0, 0, 0));
        container.addView(bar, fullWidth());
        return container;
    }

    private LinearLayout.LayoutParams navItemParams() {
        return new LinearLayout.LayoutParams(0, dp(52), 1f);
    }

    private View createNavItem(String title, int tabIndex) {
        boolean selected = (currentTab == tabIndex);
        LinearLayout item = vertical();
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(4), dp(4), dp(4), dp(4));

        if (selected) {
            item.setBackground(roundRect(sage, 22, sage, 0));
        }

        TextView label = text(title, 13, selected ? sageInk : mutedInk, selected ? Typeface.BOLD : Typeface.NORMAL);
        label.setGravity(Gravity.CENTER);
        item.addView(label);

        item.setClickable(true);
        item.setFocusable(true);
        item.setContentDescription(title + " tab");
        item.setOnClickListener(v -> {
            currentTab = tabIndex;
            renderCurrentTab();
        });
        return item;
    }

    private void showWelcomeScreen() {
        welcomeInterval = UserPreferences.getInterval(this);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(canvas);
        LinearLayout page = vertical();
        page.setPadding(dp(24), dp(28), dp(24), dp(32));
        scrollView.addView(page, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        TextView brand = label("CHECK IN", 12, sageInk);
        brand.setLetterSpacing(0.18f);
        page.addView(brand);

        TextView title = serifText("A gentle space\nbuilt for you.", 34, ink);
        page.addView(title, margins(0, 16, 0, 0));

        TextView subtitle = text("Let’s personalize your daily check-in rhythm and reflection space.", 16, mutedInk, Typeface.NORMAL);
        subtitle.setLineSpacing(dp(4), 1f);
        page.addView(subtitle, margins(0, 10, 0, 28));

        TextView nameLabel = label("WHAT SHOULD WE CALL YOU?", 11, mutedInk);
        nameLabel.setLetterSpacing(0.12f);
        page.addView(nameLabel);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Your name or nickname");
        nameInput.setHintTextColor(Color.rgb(150, 160, 155));
        nameInput.setTextColor(ink);
        nameInput.setTextSize(18);
        nameInput.setSingleLine(true);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameInput.setFilters(new InputFilter[] { new InputFilter.LengthFilter(32) });
        nameInput.setPadding(dp(20), dp(4), dp(20), dp(4));
        nameInput.setMinHeight(dp(60));
        nameInput.setBackground(roundRect(card, 20, line, 1));
        page.addView(nameInput, margins(0, 10, 0, 24));

        page.addView(createWelcomeRhythmCard(), margins(0, 0, 0, 24));

        View begin = actionButton("Start checking in", "Your data stays 100% private on this phone", sage, sage, sageInk, sageInk);
        begin.setOnClickListener(v -> {
            String name = cleanedName(nameInput.getText().toString());
            if (name.isEmpty()) {
                nameInput.setError("Add a name or nickname");
                nameInput.requestFocus();
                return;
            }
            UserPreferences.completeSetup(this, name, welcomeInterval);
            ReminderScheduler.enable(this);
            requestNotificationPermissionIfNeeded();
            currentTab = TAB_CHECKIN;
            renderCurrentTab();
        });
        page.addView(begin, fullWidth());

        TextView privacy = text("Your name, rhythm, and future check-ins live only on this device. Nothing is ever uploaded.", 14, mutedInk, Typeface.NORMAL);
        privacy.setGravity(Gravity.CENTER);
        privacy.setLineSpacing(dp(3), 1f);
        page.addView(privacy, margins(12, 20, 12, 0));
        setContentView(scrollView);
    }

    private View createWelcomeRhythmCard() {
        LinearLayout rhythm = vertical();
        rhythm.setPadding(dp(22), dp(20), dp(22), dp(20));
        rhythm.setBackground(roundRect(card, 26, line, 1));
        rhythm.addView(text("Choose your rhythm", 18, ink, Typeface.BOLD));
        rhythm.addView(text("You can adjust or pause this anytime.", 14, mutedInk, Typeface.NORMAL), margins(0, 4, 0, 16));
        welcomeIntervalDetail = settingButton(UserPreferences.intervalLabel(welcomeInterval));
        welcomeIntervalDetail.setContentDescription("Choose how often to check in");
        welcomeIntervalDetail.setOnClickListener(v -> showIntervalPicker(true));
        rhythm.addView(welcomeIntervalDetail, fullWidth());
        return rhythm;
    }

    private void buildCheckInScreen(LinearLayout page) {
        String name = UserPreferences.getName(this);

        TextView brand = label("CHECK IN", 12, sageInk);
        brand.setLetterSpacing(0.18f);
        page.addView(brand);

        // Editorial serif title with italicized name
        TextView title = serifText("How are you feeling,\n" + name + "?", 32, ink);
        page.addView(title, margins(0, 16, 0, 0));

        TextView introduction = text("A quiet moment of reflection, just for you.", 16, mutedInk, Typeface.NORMAL);
        introduction.setLineSpacing(dp(4), 1f);
        page.addView(introduction, margins(0, 10, 0, 0));

        page.addView(createMoodCard(), margins(0, 26, 0, 16));
        page.addView(createHistoryCard(), margins(0, 0, 0, 16));
        page.addView(createStatisticsCard(), margins(0, 0, 0, 16));
        page.addView(createNameRow(), margins(0, 0, 0, 20));
        page.addView(createPrivacyNote(), fullWidth());
        updateReminderCopy();
    }

    private View createMoodCard() {
        LinearLayout moodCard = vertical();
        moodCard.setPadding(dp(24), dp(24), dp(24), dp(24));
        moodCard.setBackground(roundRect(card, 30, line, 1));

        TextView overline = label("RIGHT NOW, WHAT FEELS MOST TRUE?", 11, mutedInk);
        overline.setLetterSpacing(0.14f);
        moodCard.addView(overline, margins(0, 0, 0, 16));

        okayButton = actionButtonWithIcon("I’m okay", "A simple moment of recognition", sage, Color.rgb(212, 235, 216), sageInk, sageSubtext, R.drawable.ic_sun);
        okayButton.setOnClickListener(v -> respond(true));
        moodCard.addView(okayButton, fullWidth());

        notOkayButton = actionButtonWithIcon("Not really", "It’s okay not to be okay", lavender, Color.rgb(220, 213, 248), lavenderInk, lavenderSubtext, R.drawable.ic_cloud);
        notOkayButton.setOnClickListener(v -> respond(false));
        moodCard.addView(notOkayButton, margins(0, 12, 0, 0));

        reassuranceContainer = vertical();
        reassuranceContainer.setPadding(dp(20), dp(18), dp(20), dp(18));
        reassuranceContainer.setBackground(roundRect(sage, 22, line, 1));
        reassuranceContainer.setVisibility(View.GONE);

        reassurance = text("", 15, sageInk, Typeface.BOLD);
        reassurance.setLineSpacing(dp(4), 1f);
        reassuranceContainer.addView(reassurance);
        moodCard.addView(reassuranceContainer, margins(0, 16, 0, 0));

        undoButton = actionButton("Undo last check-in", "Choose a different response right away", actionCardBg, line, ink, mutedInk);
        undoButton.setOnClickListener(v -> undoLastCheckIn());
        undoButton.setVisibility(View.GONE);
        moodCard.addView(undoButton, margins(0, 16, 0, 0));

        updateMoodCardState();
        return moodCard;
    }

    private void updateMoodCardState() {
        if (okayButton == null || notOkayButton == null || reassurance == null) return;
        long lastTime = MoodHistoryStore.getLastCheckInTime(this);
        long interval = UserPreferences.getInterval(this);
        long cooldown = Math.max(5 * 60 * 1000L, interval);
        long elapsed = System.currentTimeMillis() - lastTime;
        boolean inCooldown = (lastTime > 0 && elapsed < cooldown);

        List<MoodHistoryStore.Entry> entries = MoodHistoryStore.loadAll(this);
        boolean lastOkay = entries.isEmpty() || entries.get(0).okay;

        if (inCooldown) {
            okayButton.setVisibility(View.GONE);
            notOkayButton.setVisibility(View.GONE);
            reassurance.setText("“" + CooldownMessages.getMessage(lastOkay, lastTime) + "”");
            reassurance.setTextColor(lastOkay ? sageInk : lavenderInk);
            if (reassuranceContainer != null) {
                reassuranceContainer.setBackground(roundRect(lastOkay ? sage : lavender, 22, lastOkay ? Color.rgb(205, 230, 210) : Color.rgb(215, 208, 245), 1));
                reassuranceContainer.setVisibility(View.VISIBLE);
            }
            if (undoButton != null) undoButton.setVisibility(View.VISIBLE);
        } else {
            okayButton.setVisibility(View.VISIBLE);
            notOkayButton.setVisibility(View.VISIBLE);
            if (reassuranceContainer != null) reassuranceContainer.setVisibility(View.GONE);
            if (undoButton != null) undoButton.setVisibility(View.GONE);
        }
    }

    private void undoLastCheckIn() {
        MoodHistoryStore.deleteLatest(this);
        ReminderScheduler.scheduleNext(this);
        CheckInWidgetProvider.updateWidgets(this);
        refreshHistoryPreview();
        updateMoodCardState();
        android.widget.Toast.makeText(this, "Check-in undone", android.widget.Toast.LENGTH_SHORT).show();
    }

    private View createHistoryCard() {
        LinearLayout history = vertical();
        history.setPadding(dp(22), dp(20), dp(22), dp(20));
        history.setBackground(roundRect(card, 26, line, 1));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(text("Recent", 18, ink, Typeface.BOLD),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        TextView viewAll = text("View all →", 15, sageInk, Typeface.BOLD);
        viewAll.setPadding(dp(12), dp(8), 0, dp(8));
        viewAll.setClickable(true);
        viewAll.setFocusable(true);
        viewAll.setContentDescription("View all mood check-ins");
        viewAll.setOnClickListener(v -> {
            currentTab = TAB_HISTORY;
            renderCurrentTab();
        });
        header.addView(viewAll);
        history.addView(header);

        historyPreviewEmpty = text("Your check-ins will appear here with their timestamp.", 14, mutedInk, Typeface.NORMAL);
        historyPreviewEmpty.setLineSpacing(dp(3), 1f);
        history.addView(historyPreviewEmpty, margins(0, 10, 0, 0));
        historyPreviewEntries = vertical();
        history.addView(historyPreviewEntries, margins(0, 10, 0, 0));
        refreshHistoryPreview();
        return history;
    }

    private void refreshHistoryPreview() {
        if (historyPreviewEntries == null || historyPreviewEmpty == null) return;
        List<MoodHistoryStore.Entry> entries = MoodHistoryStore.loadAll(this);
        historyPreviewEntries.removeAllViews();
        historyPreviewEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        int previewCount = Math.min(entries.size(), 3);
        for (int i = 0; i < previewCount; i++) {
            historyPreviewEntries.addView(createHistoryRow(entries.get(i)),
                    margins(0, i == 0 ? 0 : 8, 0, 0));
        }
        updateMoodCardState();
        if (statisticsCardContainer != null) {
            updateStatisticsCard(statisticsCardContainer);
        }
    }

    private View createStatisticsCard() {
        LinearLayout statsCard = vertical();
        statsCard.setPadding(dp(22), dp(20), dp(22), dp(20));
        statsCard.setBackground(roundRect(card, 26, line, 1));
        statisticsCardContainer = statsCard;
        updateStatisticsCard(statsCard);
        return statsCard;
    }

    private void updateStatisticsCard(LinearLayout statsCard) {
        statsCard.removeAllViews();
        MoodHistoryStore.Stats stats = MoodHistoryStore.loadStats(this);
        if (stats.total == 0) {
            LinearLayout emptyBox = vertical();
            emptyBox.addView(text("—", 16, mutedInk, Typeface.BOLD), margins(0, 0, 0, 8));
            LinearLayout rowInfo = new LinearLayout(this);
            rowInfo.setGravity(Gravity.CENTER_VERTICAL);
            rowInfo.addView(text("of check-ins felt okay", 14, mutedInk, Typeface.NORMAL),
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            rowInfo.addView(text("0 total", 14, mutedInk, Typeface.NORMAL));
            emptyBox.addView(rowInfo);

            // Progress bar bar
            View bar = new View(this);
            bar.setBackground(roundRect(Color.rgb(240, 225, 218), 8, Color.rgb(240, 225, 218), 0));
            emptyBox.addView(bar, fixedHeightMargins(8, 0, 12, 0, 12));

            LinearLayout countsRow = new LinearLayout(this);
            countsRow.setGravity(Gravity.CENTER_VERTICAL);
            countsRow.addView(text("● I’m okay · 0", 13, sageInk, Typeface.BOLD),
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            countsRow.addView(text("● Not really · 0", 13, lavenderInk, Typeface.BOLD));
            emptyBox.addView(countsRow);
            statsCard.addView(emptyBox);
        } else {
            LinearLayout topBox = vertical();
            topBox.addView(text(stats.okayPercentage() + "%", 20, ink, Typeface.BOLD), margins(0, 0, 0, 4));
            LinearLayout rowInfo = new LinearLayout(this);
            rowInfo.setGravity(Gravity.CENTER_VERTICAL);
            rowInfo.addView(text("of check-ins felt okay", 14, mutedInk, Typeface.NORMAL),
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            rowInfo.addView(text(stats.total + " total", 14, mutedInk, Typeface.NORMAL));
            topBox.addView(rowInfo);

            View barBg = new View(this);
            barBg.setBackground(roundRect(lavender, 8, lavender, 0));
            topBox.addView(barBg, fixedHeightMargins(8, 0, 12, 0, 12));

            LinearLayout countsRow = new LinearLayout(this);
            countsRow.setGravity(Gravity.CENTER_VERTICAL);
            countsRow.addView(text("● I’m okay · " + stats.okayCount, 13, sageInk, Typeface.BOLD),
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            countsRow.addView(text("● Not really · " + stats.notOkayCount, 13, lavenderInk, Typeface.BOLD));
            topBox.addView(countsRow);
            statsCard.addView(topBox);
        }
    }

    private void buildHistoryScreen(LinearLayout page) {
        TextView back = text("‹  Check In", 16, sageInk, Typeface.BOLD);
        back.setPadding(0, 0, 0, dp(8));
        back.setClickable(true);
        back.setFocusable(true);
        back.setContentDescription("Back to Check In");
        back.setOnClickListener(v -> {
            currentTab = TAB_CHECKIN;
            renderCurrentTab();
        });
        page.addView(back);

        TextView overline = label("HISTORY", 11, mutedInk);
        overline.setLetterSpacing(0.14f);
        page.addView(overline, margins(0, 10, 0, 0));

        TextView title = serifText("Your check-ins", 32, ink);
        page.addView(title, margins(0, 4, 0, 0));

        TextView subtitle = text("Private, stored only on this phone.", 16, mutedInk, Typeface.NORMAL);
        subtitle.setLineSpacing(dp(3), 1f);
        page.addView(subtitle, margins(0, 8, 0, 20));

        page.addView(createStatisticsCard(), margins(0, 0, 0, 16));

        List<MoodHistoryStore.Entry> entries = MoodHistoryStore.loadAll(this);
        if (entries.isEmpty()) {
            TextView empty = text("No check-ins yet. Your first one starts the story.", 16, mutedInk, Typeface.NORMAL);
            empty.setLineSpacing(dp(4), 1f);
            page.addView(empty, margins(0, 12, 0, 0));
        } else {
            TextView clear = text("Clear history", 15, lavenderInk, Typeface.BOLD);
            clear.setGravity(Gravity.CENTER);
            clear.setPadding(dp(18), dp(14), dp(18), dp(14));
            clear.setClickable(true);
            clear.setFocusable(true);
            clear.setBackground(roundRect(lavender, 18, lavender, 0));
            clear.setOnClickListener(v -> confirmClearHistory());
            page.addView(clear, margins(0, 0, 0, 20));
            for (int i = 0; i < entries.size(); i++) {
                page.addView(createHistoryRow(entries.get(i)), margins(0, i == 0 ? 0 : 10, 0, 0));
            }
        }
    }

    private void buildRhythmScreen(LinearLayout page) {
        TextView back = text("‹  Check In", 16, sageInk, Typeface.BOLD);
        back.setPadding(0, 0, 0, dp(8));
        back.setClickable(true);
        back.setFocusable(true);
        back.setContentDescription("Back to Check In");
        back.setOnClickListener(v -> {
            currentTab = TAB_CHECKIN;
            renderCurrentTab();
        });
        page.addView(back);

        TextView overline = label("RHYTHM", 11, mutedInk);
        overline.setLetterSpacing(0.14f);
        page.addView(overline, margins(0, 10, 0, 0));

        TextView title = serifText("Your check-in\nrhythm", 32, ink);
        page.addView(title, margins(0, 4, 0, 0));

        TextView subtitle = text("Pick what feels helpful, not intrusive.", 16, mutedInk, Typeface.NORMAL);
        subtitle.setLineSpacing(dp(3), 1f);
        page.addView(subtitle, margins(0, 8, 0, 20));

        // Rhythm options cards
        long[] intervals = UserPreferences.intervals();
        String[] labels = UserPreferences.intervalLabels();
        long currentInterval = UserPreferences.getInterval(this);

        for (int i = 0; i < intervals.length; i++) {
            final long interval = intervals[i];
            boolean selected = (interval == currentInterval);

            LinearLayout cardView = new LinearLayout(this);
            cardView.setGravity(Gravity.CENTER_VERTICAL);
            cardView.setPadding(dp(20), dp(16), dp(20), dp(16));
            cardView.setBackground(roundRect(selected ? sageInk : card, 22, selected ? sageInk : line, selected ? 0 : 1));

            TextView labelView = text(labels[i], 16, selected ? Color.WHITE : ink, Typeface.BOLD);
            cardView.addView(labelView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            if (selected) {
                ImageView checkIcon = new ImageView(this);
                checkIcon.setImageResource(R.drawable.ic_check_mark);
                checkIcon.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                cardView.addView(checkIcon, new LinearLayout.LayoutParams(dp(22), dp(22)));
            }

            cardView.setClickable(true);
            cardView.setFocusable(true);
            cardView.setOnClickListener(v -> {
                UserPreferences.setInterval(this, interval);
                ReminderScheduler.scheduleNext(this);
                updateReminderCopy();
                renderCurrentTab();
            });
            page.addView(cardView, margins(0, 0, 0, 10));
        }

        TextView notice = text("After “Not really”, we'll check back a little sooner.", 14, mutedInk, Typeface.NORMAL);
        page.addView(notice, margins(4, 8, 4, 20));

        // Widget Card
        LinearLayout widgetCard = vertical();
        widgetCard.setPadding(dp(22), dp(20), dp(18), dp(20));
        widgetCard.setBackground(roundRect(card, 26, line, 1));

        LinearLayout reminderRow = new LinearLayout(this);
        reminderRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout words = vertical();
        words.addView(text("Home screen widget", 17, ink, Typeface.BOLD));
        TextView widgetSub = text("Check in without opening the app. Replaces reminders.", 14, mutedInk, Typeface.NORMAL);
        widgetSub.setLineSpacing(dp(2), 1f);
        words.addView(widgetSub, margins(0, 4, 0, 0));
        reminderRow.addView(words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        reminderSwitch = new Switch(this);
        reminderSwitch.setContentDescription("Enable home screen widget and reminders");
        reminderSwitch.setThumbTintList(new ColorStateList(
                new int[][] { new int[] { android.R.attr.state_checked }, new int[] {} },
                new int[] { sageInk, Color.rgb(195, 205, 200) }));
        reminderSwitch.setTrackTintList(new ColorStateList(
                new int[][] { new int[] { android.R.attr.state_checked }, new int[] {} },
                new int[] { Color.rgb(175, 210, 180), Color.rgb(220, 225, 222) }));
        settingSwitch = true;
        reminderSwitch.setChecked(ReminderScheduler.isEnabled(this));
        settingSwitch = false;
        reminderSwitch.setOnCheckedChangeListener(this::onReminderChanged);
        reminderRow.addView(reminderSwitch);
        widgetCard.addView(reminderRow, fullWidth());
        page.addView(widgetCard, margins(0, 0, 0, 16));
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(this)
                .setTitle("Clear all check-ins?")
                .setMessage("This removes the mood and timestamp of every saved check-in from this device. This cannot be undone.")
                .setNegativeButton("Keep history", null)
                .setPositiveButton("Clear history", (dialog, which) -> {
                    MoodHistoryStore.clear(this);
                    renderCurrentTab();
                })
                .show();
    }

    private View createHistoryRow(MoodHistoryStore.Entry entry) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int background = entry.okay ? Color.rgb(240, 246, 242) : Color.rgb(245, 243, 252);
        int foreground = entry.okay ? sageInk : lavenderInk;
        row.setPadding(dp(18), dp(14), dp(18), dp(14));
        row.setBackground(roundRect(background, 20, background, 0));
        TextView dot = text("●", 15, foreground, Typeface.NORMAL);
        row.addView(dot, new LinearLayout.LayoutParams(dp(24), LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout words = vertical();
        words.addView(text(entry.okay ? "I’m okay" : "Not really", 16, foreground, Typeface.BOLD));
        words.addView(text(formatCheckInTime(entry.checkedAt), 14, mutedInk, Typeface.NORMAL), margins(0, 3, 0, 0));
        row.addView(words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        return row;
    }

    private String formatCheckInTime(long timestamp) {
        Date moment = new Date(timestamp);
        String date = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(moment);
        String time = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(moment);
        return date + " · " + time;
    }

    private View createNameRow() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(22), dp(18), dp(18), dp(18));
        row.setBackground(roundRect(card, 26, line, 1));
        LinearLayout words = vertical();
        words.addView(text("Call me", 14, mutedInk, Typeface.NORMAL));
        words.addView(text(UserPreferences.getName(this), 17, ink, Typeface.BOLD), margins(0, 3, 0, 0));
        row.addView(words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        TextView edit = text("Edit", 15, sageInk, Typeface.BOLD);
        edit.setPadding(dp(14), dp(10), dp(4), dp(10));
        edit.setClickable(true);
        edit.setFocusable(true);
        edit.setContentDescription("Edit preferred name");
        edit.setOnClickListener(v -> showNameEditor());
        row.addView(edit);
        return row;
    }

    private View createPrivacyNote() {
        LinearLayout note = new LinearLayout(this);
        note.setGravity(Gravity.CENTER_VERTICAL);
        note.setPadding(dp(6), dp(8), dp(6), dp(8));
        note.addView(text("●", 15, sageInk, Typeface.NORMAL), new LinearLayout.LayoutParams(dp(26), LinearLayout.LayoutParams.WRAP_CONTENT));
        TextView privacy = text("Your name, rhythm, and history stay privately on this phone. No account or online data.", 14, mutedInk, Typeface.NORMAL);
        privacy.setLineSpacing(dp(3), 1f);
        note.addView(privacy, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        return note;
    }

    private void showIntervalPicker(boolean forWelcome) {
        long[] intervals = UserPreferences.intervals();
        String[] labels = UserPreferences.intervalLabels();
        long current = forWelcome ? welcomeInterval : UserPreferences.getInterval(this);
        new AlertDialog.Builder(this)
                .setTitle("How often should we check in?")
                .setSingleChoiceItems(labels, UserPreferences.intervalIndex(current), (dialog, which) -> {
                    if (forWelcome) {
                        welcomeInterval = intervals[which];
                        welcomeIntervalDetail.setText(labels[which] + "  ›");
                    } else {
                        UserPreferences.setInterval(this, intervals[which]);
                        ReminderScheduler.scheduleNext(this);
                        updateReminderCopy();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showNameEditor() {
        EditText input = new EditText(this);
        input.setText(UserPreferences.getName(this));
        input.setSingleLine(true);
        input.setTextSize(18);
        input.setTextColor(ink);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setFilters(new InputFilter[] { new InputFilter.LengthFilter(32) });
        LinearLayout holder = vertical();
        holder.setPadding(dp(24), 0, dp(24), 0);
        holder.addView(input, fullWidth());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("What should we call you?")
                .setMessage("This name is stored securely on your device.")
                .setView(holder)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = cleanedName(input.getText().toString());
            if (name.isEmpty()) {
                input.setError("Add a name or nickname");
                return;
            }
            UserPreferences.setName(this, name);
            dialog.dismiss();
            renderCurrentTab();
        }));
        dialog.show();
    }

    private void onReminderChanged(CompoundButton button, boolean enabled) {
        if (settingSwitch) return;
        if (enabled) {
            ReminderScheduler.enable(this);
            requestNotificationPermissionIfNeeded();
        } else {
            ReminderScheduler.disable(this);
        }
        updateReminderCopy();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.POST_NOTIFICATIONS }, REQUEST_NOTIFICATIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATIONS) updateReminderCopy();
    }

    private void updateReminderCopy() {
        if (intervalDetail != null) {
            intervalDetail.setText(UserPreferences.intervalLabel(UserPreferences.getInterval(this)) + "  ›");
        }
    }

    private void respond(boolean okay) {
        long lastTime = MoodHistoryStore.getLastCheckInTime(this);
        long interval = UserPreferences.getInterval(this);
        long cooldown = Math.max(5 * 60 * 1000L, interval);
        long elapsed = System.currentTimeMillis() - lastTime;
        if (lastTime > 0 && elapsed < cooldown) {
            updateMoodCardState();
            return;
        }

        MoodHistoryStore.record(this, okay);
        refreshHistoryPreview();
        updateMoodCardState();
        CheckInWidgetProvider.updateWidgets(this);
        if (reassurance != null) {
            reassurance.setAlpha(0f);
            reassurance.animate().alpha(1f).setDuration(200).start();
        }
    }

    private TextView settingButton(String value) {
        TextView button = text(value + "  ›", 16, sageInk, Typeface.BOLD);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(dp(18), dp(15), dp(18), dp(15));
        button.setMinHeight(dp(54));
        button.setBackground(roundRect(Color.rgb(240, 246, 242), 18, Color.rgb(215, 230, 218), 1));
        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private View actionButtonWithIcon(String title, String subtitle, int background, int stroke, int titleColor, int subtitleColor, int iconRes) {
        LinearLayout button = vertical();
        button.setPadding(dp(20), dp(18), dp(20), dp(18));
        button.setMinimumHeight(dp(76));
        button.setBackground(roundRect(background, 22, stroke, 1));

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);

        ImageView iconView = new ImageView(this);
        iconView.setImageResource(iconRes);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(28), dp(28));
        iconParams.setMargins(0, 0, dp(14), 0);
        row.addView(iconView, iconParams);

        LinearLayout words = vertical();
        words.addView(text(title, 17, titleColor, Typeface.BOLD));
        words.addView(text(subtitle, 13, subtitleColor, Typeface.NORMAL), margins(0, 2, 0, 0));
        row.addView(words, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        button.addView(row);

        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private View actionButton(String title, String subtitle, int background, int stroke, int titleColor, int subtitleColor) {
        LinearLayout button = vertical();
        button.setPadding(dp(20), dp(18), dp(20), dp(18));
        button.setMinimumHeight(dp(76));
        button.setBackground(roundRect(background, 22, stroke, 1));

        TextView titleView = text(title, 17, titleColor, Typeface.BOLD);
        TextView subView = text(subtitle, 13, subtitleColor, Typeface.NORMAL);

        button.addView(titleView);
        button.addView(subView, margins(0, 4, 0, 0));

        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private TextView text(String value, float size, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("sans", style));
        return view;
    }

    private TextView serifText(String value, float size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.create("serif", Typeface.BOLD));
        return view;
    }

    private TextView label(String value, float size, int color) {
        return text(value, size, color, Typeface.BOLD);
    }

    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private GradientDrawable roundRect(int color, int radiusDp, int strokeColor, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) drawable.setStroke(dp(strokeDp), strokeColor);
        return drawable;
    }

    private LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams margins(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = fullWidth();
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private LinearLayout.LayoutParams fixedHeightMargins(int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(height));
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private String cleanedName(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
