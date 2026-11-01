package com.example.bot;

/**
 * Tracks where each user is in the ride-request conversation.
 */
public enum ConversationState {
    /** No active conversation — waiting for a command. */
    IDLE,

    /** Bot asked for pickup location — waiting for the user to reply. */
    WAITING_FOR_PICKUP,

    /** Bot asked for destination — waiting for the user to reply. */
    WAITING_FOR_DESTINATION,

    /** Bot showed the summary — waiting for confirm / cancel. */
    WAITING_FOR_CONFIRMATION,

    /** Bot asked for emergency Telegram ID. */
    WAITING_FOR_EMERGENCY_ID,

    /** Bot asked for emergency Telegram username. */
    WAITING_FOR_EMERGENCY_NAME,

    /** Bot asked for emergency phone number. */
    WAITING_FOR_EMERGENCY_NUMBER
}
