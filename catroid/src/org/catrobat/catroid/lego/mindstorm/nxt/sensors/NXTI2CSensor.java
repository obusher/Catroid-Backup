/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.lego.mindstorm.nxt.sensors;

import org.catrobat.catroid.lego.mindstorm.MindstormConnection;
import org.catrobat.catroid.lego.mindstorm.nxt.Command;
import org.catrobat.catroid.lego.mindstorm.nxt.CommandByte;
import org.catrobat.catroid.lego.mindstorm.nxt.CommandType;
import org.catrobat.catroid.lego.mindstorm.nxt.NXTError;
import org.catrobat.catroid.lego.mindstorm.nxt.NXTException;
import org.catrobat.catroid.lego.mindstorm.nxt.NXTReply;

public abstract class NXTI2CSensor extends NXTSensor {

	private byte address;
	private int pollTime;
	private final int I2CTimeOut = 500; //in MS

	private static final byte BYTES_READ_BYTE = 3;

	public NXTI2CSensor(byte sensorAddress, NXTSensorType sensorType, MindstormConnection connection) {
		super(3, sensorType, NXTSensorMode.RAW, connection);
		address = sensorAddress;
		pollTime = 25;
	}

	public byte getI2CAddress()
	{
		return address;
	}

	@Override
	protected void initialize()
	{
		super.initialize();
		readRegister(0x00, 0x01);
	}

	protected void writeRegister(byte register, byte data, boolean reply) {
		if (!hasInit) {
			initialize();
		}
		byte[] command = { address, register, data};
		write(command, (byte)0, reply);
	}

	protected byte[] readRegister(int register, int rxLength)
	{
		if (!hasInit) {
			initialize();
		}
		byte[] command = { address, (byte)register };
		return writeAndRead(command, (byte) rxLength);
	}

	private void waitForBytes(byte numberOfBytes)
	{
		Stopwatch stopWatch = new Stopwatch();
		byte bytesRead = 0;
		stopWatch.start();
		do {
			wait(pollTime);
			bytesRead = getNumberOfBytesAreReadyToRead();
		} while (bytesRead != numberOfBytes && stopWatch.getElapsedMilliseconds() < I2CTimeOut);

		if (stopWatch.getElapsedMilliseconds() > I2CTimeOut) {
			throw new NXTException("I2CTimeOut wail waiting on bytes Ready, waited " + stopWatch.getElapsedMilliseconds() + "ms");
		}
	}

	protected byte[] writeAndRead(byte[] data, byte rxLength)
	{
		write(data, rxLength, false);
		waitForBytes(rxLength);
		return read();
	}

	protected void write(byte[] txData, byte rxLength, boolean reply)
	{
		Command command = new Command(CommandType.DIRECT_COMMAND, CommandByte.LS_WRITE, reply);
		command.append((byte) port);
		command.append((byte) txData.length);
		command.append(rxLength);
		command.append(txData);
		connection.send(command);
		if(reply){
			NXTReply brickReply = new NXTReply(connection.receive());
			NXTError.checkForError(brickReply, 5);
		}
	}

	private byte[] read()
	{
		Command command = new Command(CommandType.DIRECT_COMMAND, CommandByte.LS_READ, true);
		command.append((byte) port);
		NXTReply reply = new NXTReply(connection.sendAndReceive(command));
		NXTError.checkForError(reply, 20);
		byte size = reply.getByte(BYTES_READ_BYTE);
		return reply.getData(4, size);
	}

	private byte getNumberOfBytesAreReadyToRead()
	{
		Command command = new Command(CommandType.DIRECT_COMMAND, CommandByte.LS_GET_STATUS, true);
		command.append((byte) port);
		NXTReply reply = new NXTReply(connection.sendAndReceive(command));
		NXTError.checkForError(reply, 4);
		return reply.getByte(BYTES_READ_BYTE);
	}

	protected void wait(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// can not be interrupted
		}
	}

	private static class Stopwatch {

		private long start;

		public void start() {
			start = System.currentTimeMillis();
		}

		public long getElapsedMilliseconds() {
			return (System.currentTimeMillis() - start);
		}
	}
}
