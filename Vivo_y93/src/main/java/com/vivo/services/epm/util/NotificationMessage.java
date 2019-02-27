package com.vivo.services.epm.util;

import android.os.Bundle;

public class NotificationMessage {
    private static final String BTN1 = "btn1";
    private static final String BTN1_ARGS = "btnArgs";
    private static final String BTN2 = "btn2";
    private static final String BTN2_ARGS = "btn2Args";
    private static final String BTN3 = "btn3";
    private static final String BTN3_ARGS = "btn3Args";
    private static final String CHECKED = "checked";
    private static final String CHECK_TEXT = "checkText";
    private static final String CONTENT = "content";
    private static final String CONTENT_ARGS = "contentArgs";
    private static final String DISGUISE_NAME = "disguiseName";
    private static final String FLOATING = "floating";
    private static final String MESSAGEID = "messageId";
    private static final String NOTIFICATION = "notification";
    private static final String ON_GOING = "onGoing";
    private static final String PATTERN = "pattern";
    private static final String SOUND = "sound";
    private static final String TITLE = "title";
    private String btn1;
    private String[] btn1Args;
    private String btn2;
    private String[] btn2Args;
    private String btn3;
    private String[] btn3Args;
    private Bundle bundle = new Bundle();
    private String checkText;
    private int checked;
    private String content;
    private String[] contentArgs;
    private String disguiseName;
    private int floating;
    private int messageId;
    private int onGoing;
    private int pattern;
    private int sound;
    private String title;

    public static class DisplayID {
        public static final int DISPLAY_ID_0 = 1006;
        public static final int DISPLAY_ID_1 = 1030;
        public static final int DISPLAY_ID_10 = 1039;
        public static final int DISPLAY_ID_2 = 1031;
        public static final int DISPLAY_ID_3 = 1032;
        public static final int DISPLAY_ID_4 = 1033;
        public static final int DISPLAY_ID_5 = 1034;
        public static final int DISPLAY_ID_6 = 1035;
        public static final int DISPLAY_ID_7 = 1036;
        public static final int DISPLAY_ID_8 = 1037;
        public static final int DISPLAY_ID_9 = 1038;
    }

    public int getPattern() {
        return this.pattern;
    }

    public NotificationMessage setPattern(int pattern) {
        this.pattern = pattern;
        return this;
    }

    public int getMessageId() {
        return this.messageId;
    }

    public NotificationMessage setMessageId(int messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getTitle() {
        return this.title;
    }

    public NotificationMessage setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDisguiseName() {
        return this.disguiseName;
    }

    public NotificationMessage setDisguiseName(String disguiseName) {
        this.disguiseName = disguiseName;
        return this;
    }

    public int getOnGoing() {
        return this.onGoing;
    }

    public NotificationMessage setOnGoing(int onGoing) {
        this.onGoing = onGoing;
        return this;
    }

    public int getSound() {
        return this.sound;
    }

    public NotificationMessage setSound(int sound) {
        this.sound = sound;
        return this;
    }

    public int getFloating() {
        return this.floating;
    }

    public NotificationMessage setFloating(int floating) {
        this.floating = floating;
        return this;
    }

    public int getChecked() {
        return this.checked;
    }

    public NotificationMessage setChecked(int checked) {
        this.checked = checked;
        return this;
    }

    public String getCheckText() {
        return this.checkText;
    }

    public NotificationMessage setCheckText(String checkText) {
        this.checkText = checkText;
        return this;
    }

    public String getContent() {
        return this.content;
    }

    public NotificationMessage setContent(String content) {
        this.content = content;
        return this;
    }

    public String[] getContentArgs() {
        return this.contentArgs;
    }

    public NotificationMessage setContentArgs(String[] contentArgs) {
        this.contentArgs = contentArgs;
        return this;
    }

    public String getBtn1() {
        return this.btn1;
    }

    public NotificationMessage setBtn1(String btn1) {
        this.btn1 = btn1;
        return this;
    }

    public String[] getBtn1Args() {
        return this.btn1Args;
    }

    public NotificationMessage setBtn1Args(String[] btn1Args) {
        this.btn1Args = btn1Args;
        return this;
    }

    public String getBtn2() {
        return this.btn2;
    }

    public NotificationMessage setBtn2(String btn2) {
        this.btn2 = btn2;
        return this;
    }

    public String[] getBtn2Args() {
        return this.btn2Args;
    }

    public NotificationMessage setBtn2Args(String[] btn2Args) {
        this.btn2Args = btn2Args;
        return this;
    }

    public String getBtn3() {
        return this.btn3;
    }

    public NotificationMessage setBtn3(String btn3) {
        this.btn3 = btn3;
        return this;
    }

    public String[] getBtn3Args() {
        return this.btn3Args;
    }

    public NotificationMessage setBtn3Args(String[] btn3Args) {
        this.btn3Args = btn3Args;
        return this;
    }

    public Bundle pack() {
        this.bundle.putInt(PATTERN, this.pattern);
        this.bundle.putString(TITLE, this.title);
        this.bundle.putInt(MESSAGEID, this.messageId);
        this.bundle.putInt(ON_GOING, this.onGoing);
        this.bundle.putInt(SOUND, this.sound);
        this.bundle.putInt(FLOATING, this.floating);
        this.bundle.putInt(CHECKED, this.checked);
        this.bundle.putString(CHECK_TEXT, this.checkText);
        this.bundle.putString(DISGUISE_NAME, this.disguiseName);
        this.bundle.putString(CONTENT, this.content);
        this.bundle.putStringArray(CONTENT_ARGS, this.contentArgs);
        this.bundle.putString(BTN1, this.btn1);
        this.bundle.putStringArray(BTN1_ARGS, this.btn1Args);
        this.bundle.putString(BTN2, this.btn2);
        this.bundle.putStringArray(BTN2_ARGS, this.btn2Args);
        this.bundle.putString(BTN3, this.btn3);
        this.bundle.putStringArray(BTN3_ARGS, this.btn3Args);
        return this.bundle;
    }
}
