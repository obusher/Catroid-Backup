/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2014 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.uitest.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.widget.ListView;

import com.robotium.solo.Solo;

import org.catrobat.catroid.bluetooth.BTConnectDeviceActivity;
import org.catrobat.catroid.bluetooth.BTDeviceFactory;
import org.catrobat.catroid.bluetooth.BTDeviceService;
import org.catrobat.catroid.bluetooth.BluetoothConnection;
import org.catrobat.catroid.common.CatrobatService;
import org.catrobat.catroid.common.ServiceProvider;
import org.catrobat.catroid.test.utils.BluetoothConnectionWrapper;
import org.catrobat.catroid.test.utils.TestUtils;
import org.catrobat.catroid.uitest.annotation.Device;
import org.catrobat.catroid.uitest.util.BaseActivityInstrumentationTestCaseWithoutSolo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectorTest extends BaseActivityInstrumentationTestCaseWithoutSolo<BTConnectDeviceActivity> {

	public BluetoothConnectorTest() {
		super(BTConnectDeviceActivity.class);
	}

	// needed for testdevices
	// Bluetooth server is running with a name that starts with 'kitty'
	// e.g. kittyroid-0, kittyslave-0
	private static final String PAIRED_BLUETOOTH_SERVER_DEVICE_NAME = "kitty";

	// needed for testdevices
	// unavailable device is paired with a name that starts with 'SWEET'
	// e.g. SWEETHEART

//	private static final String PAIRED_UNAVAILABLE_DEVICE_NAME = "SWEET";
//	private static final String PAIRED_UNAVAILABLE_DEVICE_MAC = "00:23:4D:F5:A6:18";

	private static final UUID COMMON_BT_TEST_UUID = UUID.fromString("fd2835bb-9d80-41e0-9721-5372b90342da");

	public static final Class<BluetoothTestService> TEST_SERVICE  = BluetoothTestService.class;
	private BluetoothConnectionWrapper connectionWrapper;

	private Solo solo;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Intent intent = new Intent(getInstrumentation().getContext(), CatrobatService.BLUETOOTH_DEVICE_CONNECTOR);
		intent.putExtra(BTConnectDeviceActivity.SERVICE_TO_START, TEST_SERVICE);
		intent.putExtra(BTConnectDeviceActivity.AUTO_CONNECT, false);

		setActivityIntent(intent);

		BTConnectDeviceActivity.setDeviceFactory(new BTDeviceFactory() {
			@Override
			public <T extends BTDeviceService> BTDeviceService createDevice(Class<T> service, Context context) {
				return new BluetoothTestService();
			}

			@Override
			public <T extends BTDeviceService> BluetoothConnection createBTConnectionForDevice(Class<T> service, String address, UUID deviceUUID, Context applicationContext) {
				connectionWrapper = new BluetoothConnectionWrapper(address, deviceUUID, new CommonBluetoothTestClientHandler());
				connectionWrapper.startClientHandlerThread();
				return connectionWrapper;
			}
		});



		TestUtils.enableBluetooth();
		solo = new Solo(getInstrumentation(), getActivity());
		solo.unlockScreen();
	}

	@Override
	protected void tearDown() throws Exception {
		solo.finishOpenedActivities();

		ServiceProvider.getService(CatrobatService.BLUETOOTH_DEVICE_CONNECTOR).disconnectDevices();
		TestUtils.disableBluetooth();

		super.tearDown();
	}

	@Device
	public void testBluetoothConnector() throws IOException {

		final int requestCode = 11;

		solo.waitForActivity(BTConnectDeviceActivity.class);
		solo.sleep(2000);

		ListView deviceList = solo.getCurrentViews(ListView.class).get(0);
		String connectedDeviceName = null;
		for (int i = 0; i < deviceList.getCount(); i++) {
			String deviceName = (String) deviceList.getItemAtPosition(i);
			if (deviceName.startsWith(PAIRED_BLUETOOTH_SERVER_DEVICE_NAME)) {
				connectedDeviceName = deviceName;
				break;
			}
		}

		solo.clickOnText(connectedDeviceName);

		solo.sleep(2000); //yes, has to be that long! waiting for auto connection timeout!

		BluetoothTestService service = ServiceProvider.getService(TEST_SERVICE);

		assertNotNull("Service already registered, should not be null here.", service);
		service.connect();

		byte[] expectedMessage = new byte[] {1,2,3};

		service.sendTestMessage(expectedMessage);
		solo.sleep(2000);
		byte[] receivedMessage = service.receiveTestMessage();
		assertMessageEquals(expectedMessage, receivedMessage);

		assertMessageEquals(expectedMessage, connectionWrapper.getNextSentMessage(1));
		assertMessageEquals(expectedMessage, connectionWrapper.getNextReceivedMessage(1));
	}

	private void assertMessageEquals(byte[] expected, byte[] actual) {

		assertEquals("Bluetooth message is not equal, because of different message length.", expected.length, actual.length);

		for (int i = 0; i < expected.length; i++) {
			assertEquals("Bluetooth message is not equal, byte " + i + " is different", expected[i], actual[i]);
		}
	}

	private class BluetoothTestService implements BTDeviceService {

		private boolean isConnected = false;
		BluetoothConnection connection;

		private InputStream inStream;
		private OutputStream outStream;

		public boolean isConnected() {
			return isConnected;
		}

		@Override
		public String getName() {
			return "BT Test Service";
		}

		@Override
		public Class<? extends BTDeviceService> getServiceType() {
			return BluetoothTestService.class;
		}

		@Override
		public void setConnection(BluetoothConnection connection) {
			this.connection = connection;
		}

		public void connect() throws IOException{
			inStream = connection.getInputStream();
			outStream = connection.getOutputStream();

			isConnected = true;
		}

		@Override
		public void disconnect() {
			connection.disconnect();
			isConnected = false;
		}

		@Override
		public UUID getBluetoothDeviceUUID() {
			return COMMON_BT_TEST_UUID;
		}

		public void sendTestMessage(byte[] message) throws IOException {

			outStream.write((byte)(0xFF & message.length));
			outStream.write(message);
			outStream.flush();
		}

		public byte[] receiveTestMessage() throws IOException {
			byte[] messageLengthBuffer = new byte[1];

			inStream.read(messageLengthBuffer, 0, 1);
			int expectedMessageLength = messageLengthBuffer[0];

			byte[] payload = new byte[expectedMessageLength];

			inStream.read(payload, 0, expectedMessageLength);

			return payload;
		}

		@Override
		public void initialise() {

		}

		@Override
		public void start() {

		}

		@Override
		public void pause() {

		}

		@Override
		public void destroy() {

		}
	}

	// TODO: move this class to Bluetoothtest server or internal lib that is usable from tests and the bluetooth test server
	public static class CommonBluetoothTestClientHandler implements BluetoothConnectionWrapper.BTClientHandler {

		@Override
		public void handle(InputStream inStream, OutputStream outStream) throws IOException {
			byte[] messageLengthBuffer = new byte[1];

			while (inStream.read(messageLengthBuffer, 0, 1) != -1) {
				int expectedMessageLength = messageLengthBuffer[0];
				handleClientMessage(expectedMessageLength, new DataInputStream(inStream), outStream);
			}
		}

		private void handleClientMessage(int expectedMessageLength, DataInputStream inStream, OutputStream outStream) throws IOException {

			byte[] payload = new byte[expectedMessageLength];

			inStream.readFully(payload, 0, expectedMessageLength);

			byte[] testResult = payload;

			outStream.write(new byte[] {(byte)(0xFF & testResult.length)});
			outStream.write(testResult);
			outStream.flush();
		}
	}
}