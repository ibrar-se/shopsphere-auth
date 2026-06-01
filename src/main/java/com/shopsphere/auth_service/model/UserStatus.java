package com.shopsphere.auth_service.model;

public enum UserStatus {
    ACTIVE,    // User is fully verified and can log in normally
    SUSPENDED, // User violated terms; cannot log in, but admins can still see their history
    DELETED    // User requested account deletion; hidden from the system entirely
}