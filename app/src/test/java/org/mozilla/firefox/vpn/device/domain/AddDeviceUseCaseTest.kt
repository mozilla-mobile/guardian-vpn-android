package org.mozilla.firefox.vpn.device.domain

import org.junit.Assert.*
import org.junit.Test

class AddDeviceUseCaseTest {

    @Test
    fun testFindAvailableDeviceName() {
        val deviceName = "model"

        /** no device */
        var exists = listOf<String>()
        assertEquals(deviceName, AddDeviceUseCase.findAvailableDeviceName(deviceName, exists))

        /** {"model"} returns "model (2)" */
        exists = listOf(deviceName)
        assertEquals("$deviceName (2)", AddDeviceUseCase.findAvailableDeviceName(deviceName, exists))

        /** {"model", "model (2)"} returns "model (3)" */
        exists = listOf(deviceName, "$deviceName (2)")
        assertEquals("$deviceName (3)", AddDeviceUseCase.findAvailableDeviceName(deviceName, exists))

        /** {"model", "model (3)"} returns "model (4)" */
        exists = listOf(deviceName, "$deviceName (3)")
        assertEquals("$deviceName (4)", AddDeviceUseCase.findAvailableDeviceName(deviceName, exists))

        /** {"model", "dog", "model (3)"} returns "model (4)" */
        exists = listOf(deviceName, "dog", "$deviceName (3)")
        assertEquals("$deviceName (4)", AddDeviceUseCase.findAvailableDeviceName(deviceName, exists))

        /** {"model", "dog", "model (1)"} returns "model (2)" */
        exists = listOf(deviceName, "dog", "$deviceName (1)")
        assertEquals("$deviceName (2)", AddDeviceUseCase.findAvailableDeviceName(deviceName, exists))

        /** {"dog", "model (2)"} returns "model (3)" */
        exists = listOf("$deviceName (2)")
        assertEquals("$deviceName (3)", AddDeviceUseCase.findAvailableDeviceName(deviceName, exists))
    }
}
