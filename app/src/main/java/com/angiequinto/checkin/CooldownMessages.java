package com.angiequinto.checkin;

/** Provides short, inspiring cooldown messages tailored for the app and widget. */
public final class CooldownMessages {

    private static final String[] OKAY_MESSAGES = {
            "Glad you're doing okay. Keep going!",
            "Continue being positive. Carry this steady light forward.",
            "Keep shining. Small steps build bright momentum.",
            "Hold onto this calm. You're moving with grace.",
            "Your positive energy ripples into everything you do.",
            "Celebrate this moment. You are right where you belong.",
            "Carry this warmth with you throughout your day.",
            "Stay present. Peace is built one moment at a time."
    };

    private static final String[] NOT_OKAY_MESSAGES = {
            "It's okay to have hard days. Take it one step at a time.",
            "Take a gentle breath. You're doing the best you can.",
            "Be kind to yourself today. This moment will pass.",
            "One step at a time. Softness is a form of strength.",
            "You don't have to carry it all right now. Rest easy.",
            "It's okay to slow down. You are worthy of gentleness.",
            "Honor how you feel. Strong roots grow in quiet soil.",
            "Give yourself grace. You are doing better than you think."
    };

    private CooldownMessages() { }

    /**
     * Returns a short, inspiring message derived deterministically from the check-in timestamp.
     * This keeps the message stable for a given check-in during its cooldown, while changing dynamically across check-ins.
     */
    public static String getMessage(boolean okay, long timestamp) {
        String[] pool = okay ? OKAY_MESSAGES : NOT_OKAY_MESSAGES;
        long timeSeed = timestamp > 0 ? timestamp : System.currentTimeMillis();
        int index = Math.abs((int) (timeSeed ^ (timeSeed >>> 12))) % pool.length;
        return pool[index];
    }
}
