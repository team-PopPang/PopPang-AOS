package com.poppang.PopPang.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertViewModelTest {
    @Test fun keywordAlertsStartEnabled() {
        assertTrue(AlertViewModel().alertedState)
    }

    @Test fun anExistingOffPreferenceIsRespected() {
        val vm = AlertViewModel()
        vm.loadAlertStatus("member", false)
        assertFalse(vm.alertedState)
    }

    @Test fun anUnknownPreferenceStartsEnabledAndAcceptsTheLaterServerValue() {
        val vm = AlertViewModel()
        vm.loadAlertStatus("member", null)
        assertTrue(vm.alertedState)
        vm.loadAlertStatus("member", false)
        assertFalse(vm.alertedState)
    }

    @Test fun reenteringWithAnOldProfileDoesNotReplaceTheCurrentPreference() {
        val vm = AlertViewModel()
        vm.loadAlertStatus("member", false)
        vm.loadAlertStatus("member", true)
        vm.loadAlertStatus("member", null)
        assertFalse(vm.alertedState)
    }

    @Test fun switchingAccountsLoadsEachAccountsSavedPreference() {
        val vm = AlertViewModel()
        vm.loadAlertStatus("first", false)
        vm.loadAlertStatus("second", null)
        assertTrue(vm.alertedState)
        vm.loadAlertStatus("second", false)
        assertFalse(vm.alertedState)
        vm.loadAlertStatus("first", true)
        assertTrue(vm.alertedState)
    }

    @Test fun aMissingProfileDoesNotInitializeAnotherUsersPreference() {
        val vm = AlertViewModel()
        vm.loadAlertStatus("", false)
        assertTrue(vm.alertedState)
        vm.loadAlertStatus("member", false)
        assertFalse(vm.alertedState)
    }
}
