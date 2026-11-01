package com.example.bot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class EmergencyContactServiceTest {

    private File tempFile;
    private EmergencyContactService service;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("emergency_test_", ".json").toFile();
        service = new EmergencyContactService(tempFile);
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testPlaceholderDetection() {
        assertTrue(service.isPlaceholder(":username:"));
        assertTrue(service.isPlaceholder(":userid:"));
        assertTrue(service.isPlaceholder(":number:"));
        assertTrue(service.isPlaceholder(":phone:"));
        assertTrue(service.isPlaceholder("<username>"));
        assertTrue(service.isPlaceholder("username"));
        assertTrue(service.isPlaceholder("number"));
        assertFalse(service.isPlaceholder("@twin"));
        assertFalse(service.isPlaceholder("123456789"));
        assertFalse(service.isPlaceholder("+998901234567"));
    }

    @Test
    void testParseUsernameWithAtAndColons() {
        EmergencyContactService.ContactInfo c1 = service.parse(1001L, "@twin");
        assertNotNull(c1);
        assertEquals(EmergencyContactService.ContactType.USERNAME, c1.getType());
        assertEquals("twin", c1.getTarget());
        assertEquals("@twin", c1.getDisplayTarget());

        EmergencyContactService.ContactInfo c2 = service.parse(1001L, ":twin:");
        assertNotNull(c2);
        assertEquals(EmergencyContactService.ContactType.USERNAME, c2.getType());
        assertEquals("twin", c2.getTarget());

        EmergencyContactService.ContactInfo c3 = service.parse(1001L, "my_friend_2026");
        assertNotNull(c3);
        assertEquals(EmergencyContactService.ContactType.USERNAME, c3.getType());
        assertEquals("my_friend_2026", c3.getTarget());
    }

    @Test
    void testParseNumericTelegramUserId() {
        EmergencyContactService.ContactInfo c1 = service.parse(1002L, "123456789");
        assertNotNull(c1);
        assertEquals(EmergencyContactService.ContactType.TELEGRAM_ID, c1.getType());
        assertEquals("123456789", c1.getTarget());

        EmergencyContactService.ContactInfo c2 = service.parse(1002L, ":987654321:");
        assertNotNull(c2);
        assertEquals(EmergencyContactService.ContactType.TELEGRAM_ID, c2.getType());
        assertEquals("987654321", c2.getTarget());
    }

    @Test
    void testParsePhoneNumber() {
        EmergencyContactService.ContactInfo c1 = service.parse(1003L, "+998901234567");
        assertNotNull(c1);
        assertEquals(EmergencyContactService.ContactType.PHONE, c1.getType());
        assertEquals("+998901234567", c1.getTarget());

        EmergencyContactService.ContactInfo c2 = service.parse(1003L, ":+998901234567:");
        assertNotNull(c2);
        assertEquals(EmergencyContactService.ContactType.PHONE, c2.getType());
        assertEquals("+998901234567", c2.getTarget());

        EmergencyContactService.ContactInfo c3 = service.parse(1003L, "998901234567");
        assertNotNull(c3);
        assertEquals(EmergencyContactService.ContactType.PHONE, c3.getType());
        assertEquals("+998901234567", c3.getTarget());
    }

    @Test
    void testSetRemoveAndPersistence() {
        long userId = 555L;
        assertNull(service.getContact(userId));

        service.setContact(userId, "@twin");
        EmergencyContactService.ContactInfo saved = service.getContact(userId);
        assertNotNull(saved);
        assertEquals("twin", saved.getTarget());

        // Re-read from disk
        EmergencyContactService reloaded = new EmergencyContactService(tempFile);
        EmergencyContactService.ContactInfo fromDisk = reloaded.getContact(userId);
        assertNotNull(fromDisk);
        assertEquals("twin", fromDisk.getTarget());

        // Remove
        service.removeContact(userId);
        assertNull(service.getContact(userId));

        EmergencyContactService afterDelete = new EmergencyContactService(tempFile);
        assertNull(afterDelete.getContact(userId));
    }

    @Test
    void testExplicitSetters() {
        long userId = 777L;

        // Set ID
        EmergencyContactService.ContactInfo idContact = service.setTelegramId(userId, "987654321");
        assertNotNull(idContact);
        assertEquals(EmergencyContactService.ContactType.TELEGRAM_ID, idContact.getType());
        assertEquals("987654321", idContact.getTarget());
        assertNull(service.setTelegramId(userId, "not_a_number"));

        // Set Name
        EmergencyContactService.ContactInfo nameContact = service.setUsername(userId, "@best_twin");
        assertNotNull(nameContact);
        assertEquals(EmergencyContactService.ContactType.USERNAME, nameContact.getType());
        assertEquals("best_twin", nameContact.getTarget());
        assertNull(service.setUsername(userId, ":username:"));

        // Set Number
        EmergencyContactService.ContactInfo phoneContact = service.setPhoneNumber(userId, "+998 90 123-45-67");
        assertNotNull(phoneContact);
        assertEquals(EmergencyContactService.ContactType.PHONE, phoneContact.getType());
        assertEquals("+998901234567", phoneContact.getTarget());
        assertNull(service.setPhoneNumber(userId, "123")); // too short
    }
}

