package com.smartshift.smartshift_backend.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import com.smartshift.smartshift_backend.account.entity.Account;
import com.smartshift.smartshift_backend.account.entity.Transaction;
import com.smartshift.smartshift_backend.auth.entity.User;
import com.smartshift.smartshift_backend.organization.entity.Organization;
import com.smartshift.smartshift_backend.organization.entity.UserOrganizationMembership;

class EntityContractTest {

    @Test
    void userUsesAccountScopeAndNoGlobalRole() throws NoSuchFieldException {
        assertNotNull(User.class.getDeclaredField("accountId"));
        assertFalse(hasField(User.class, "role"));
    }

    @Test
    void accountAndOrganizationIdsAreLongs() throws NoSuchFieldException {
        assertTrue(Long.class.equals(Account.class.getDeclaredField("id").getType()));
        assertTrue(Long.class.equals(Transaction.class.getDeclaredField("accountId").getType()));
        assertTrue(Long.class.equals(Organization.class.getDeclaredField("accountId").getType()));
        assertTrue(Long.class.equals(UserOrganizationMembership.class.getDeclaredField("userId").getType()));
    }

    private boolean hasField(Class<?> type, String name) {
        try {
            type.getDeclaredField(name);
            return true;
        } catch (NoSuchFieldException exception) {
            return false;
        }
    }
}