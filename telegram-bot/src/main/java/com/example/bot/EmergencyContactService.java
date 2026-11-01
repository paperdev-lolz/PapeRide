package com.example.bot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages emergency contacts for PapeRide riders.
 * Supports Telegram user IDs, Telegram usernames (@twin), and phone numbers.
 * Automatically persists contacts to emergency_contacts.json.
 */
public class EmergencyContactService {

    public enum ContactType {
        TELEGRAM_ID,
        USERNAME,
        PHONE
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactInfo {
        private long userId;
        private String rawInput;
        private ContactType type;
        private String target;
        private long updatedAt;

        public ContactInfo() {
        }

        public ContactInfo(long userId, String rawInput, ContactType type, String target, long updatedAt) {
            this.userId = userId;
            this.rawInput = rawInput;
            this.type = type;
            this.target = target;
            this.updatedAt = updatedAt;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public String getRawInput() {
            return rawInput;
        }

        public void setRawInput(String rawInput) {
            this.rawInput = rawInput;
        }

        public ContactType getType() {
            return type;
        }

        public void setType(ContactType type) {
            this.type = type;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public long getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
        }

        /** Formatted string for UI display. */
        @JsonIgnore
        public String getDisplayTarget() {
            if (type == ContactType.USERNAME) {
                return "@" + target;
            }
            return target;
        }
    }

    private final File storageFile;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final Map<Long, ContactInfo> contacts = new ConcurrentHashMap<>();

    public EmergencyContactService() {
        this(new File("emergency_contacts.json"));
    }

    public EmergencyContactService(File storageFile) {
        this.storageFile = storageFile;
        load();
    }

    private synchronized void load() {
        if (!storageFile.exists() || storageFile.length() == 0) {
            return;
        }
        try {
            Map<String, ContactInfo> loaded = mapper.readValue(
                    storageFile,
                    new TypeReference<Map<String, ContactInfo>>() {}
            );
            if (loaded != null) {
                for (Map.Entry<String, ContactInfo> entry : loaded.entrySet()) {
                    try {
                        long uid = Long.parseLong(entry.getKey());
                        contacts.put(uid, entry.getValue());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            System.out.println("[EmergencyContactService] Loaded " + contacts.size() + " emergency contact(s).");
        } catch (Exception e) {
            System.err.println("[EmergencyContactService] Failed to load emergency contacts: " + e.getMessage());
        }
    }

    private synchronized void persist() {
        try {
            Map<String, ContactInfo> toSave = new ConcurrentHashMap<>();
            for (Map.Entry<Long, ContactInfo> entry : contacts.entrySet()) {
                toSave.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(storageFile, toSave);
        } catch (IOException e) {
            System.err.println("[EmergencyContactService] Failed to persist emergency contacts: " + e.getMessage());
        }
    }

    /**
     * Checks if the raw input is merely a template placeholder like ':username:' or ':userid:'.
     */
    public boolean isPlaceholder(String input) {
        if (input == null) return false;
        String trimmed = input.trim().toLowerCase();
        return trimmed.equals(":username:") || trimmed.equals(":userid:")
                || trimmed.equals(":number:") || trimmed.equals(":phone:")
                || trimmed.equals("<username>") || trimmed.equals("<userid>")
                || trimmed.equals("<number>") || trimmed.equals("<phone>")
                || trimmed.equals("username") || trimmed.equals("userid")
                || trimmed.equals("number") || trimmed.equals("phone");
    }

    /**
     * Parse raw contact input into a validated ContactInfo.
     */
    public ContactInfo parse(long userId, String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String cleaned = raw.trim();
        // Remove enclosing colons if user entered :twin: or :12345:
        if (cleaned.startsWith(":") && cleaned.endsWith(":") && cleaned.length() > 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }

        ContactType type;
        String target;

        if (cleaned.startsWith("@")) {
            type = ContactType.USERNAME;
            target = cleaned.substring(1).trim();
        } else if (cleaned.startsWith("+")) {
            type = ContactType.PHONE;
            target = "+" + cleaned.replaceAll("[^0-9]", "");
        } else if (cleaned.matches("^\\d+$")) {
            // All digits: determine whether Telegram numeric ID or phone number
            if (cleaned.length() >= 10 && (cleaned.startsWith("998") || cleaned.startsWith("7") || cleaned.startsWith("1"))) {
                type = ContactType.PHONE;
                target = "+" + cleaned;
            } else {
                type = ContactType.TELEGRAM_ID;
                target = cleaned;
            }
        } else if (cleaned.matches("^[a-zA-Z0-9_]{3,32}$")) {
            type = ContactType.USERNAME;
            target = cleaned;
        } else if (cleaned.replaceAll("[^0-9]", "").length() >= 9) {
            type = ContactType.PHONE;
            target = "+" + cleaned.replaceAll("[^0-9]", "");
        } else {
            type = ContactType.USERNAME;
            target = cleaned.replaceAll("[^a-zA-Z0-9_]", "");
        }

        if (target.isBlank()) {
            return null;
        }

        return new ContactInfo(userId, raw.trim(), type, target, System.currentTimeMillis());
    }

    public ContactInfo setContact(long userId, String rawInput) {
        ContactInfo info = parse(userId, rawInput);
        if (info != null) {
            contacts.put(userId, info);
            persist();
        }
        return info;
    }

    public ContactInfo setTelegramId(long userId, String raw) {
        if (raw == null || raw.isBlank() || isPlaceholder(raw)) return null;
        String cleaned = raw.trim();
        if (cleaned.startsWith(":") && cleaned.endsWith(":") && cleaned.length() > 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        if (!cleaned.matches("^\\d+$")) return null;
        ContactInfo info = new ContactInfo(userId, raw.trim(), ContactType.TELEGRAM_ID, cleaned, System.currentTimeMillis());
        contacts.put(userId, info);
        persist();
        return info;
    }

    public ContactInfo setUsername(long userId, String raw) {
        if (raw == null || raw.isBlank() || isPlaceholder(raw)) return null;
        String cleaned = raw.trim();
        if (cleaned.startsWith(":") && cleaned.endsWith(":") && cleaned.length() > 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        if (cleaned.startsWith("@")) {
            cleaned = cleaned.substring(1).trim();
        }
        cleaned = cleaned.replaceAll("[^a-zA-Z0-9_]", "");
        if (cleaned.isBlank()) return null;
        ContactInfo info = new ContactInfo(userId, raw.trim(), ContactType.USERNAME, cleaned, System.currentTimeMillis());
        contacts.put(userId, info);
        persist();
        return info;
    }

    public ContactInfo setPhoneNumber(long userId, String raw) {
        if (raw == null || raw.isBlank() || isPlaceholder(raw)) return null;
        String cleaned = raw.trim();
        if (cleaned.startsWith(":") && cleaned.endsWith(":") && cleaned.length() > 2) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        String digits = cleaned.replaceAll("[^0-9]", "");
        if (digits.length() < 7) return null;
        String target = "+" + digits;
        ContactInfo info = new ContactInfo(userId, raw.trim(), ContactType.PHONE, target, System.currentTimeMillis());
        contacts.put(userId, info);
        persist();
        return info;
    }

    public ContactInfo getContact(long userId) {
        return contacts.get(userId);
    }

    public ContactInfo removeContact(long userId) {
        ContactInfo removed = contacts.remove(userId);
        if (removed != null) {
            persist();
        }
        return removed;
    }
}
