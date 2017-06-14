package es.upm.ies.goniophotometer.serial;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.fazecast.jSerialComm.*;

import es.upm.ies.goniophotometer.exceptions.CommPortException;
import es.upm.ies.goniophotometer.exceptions.DeviceNotFoundException;

/**
 * An implementation of the <code>KNDLSerialComm</code> interface that uses the
 * <a href="http://fazecast.github.io/jSerialComm/">jserialcomm library</a>.
 * 
 * @author Abd&oacute;n Alejandro Vivas Imparato
 *
 */
public class KNDLSerialCommImpl implements KNDLSerialComm {

	/*
	 * If modifying this class: Make sure that if you use attributes like these,
	 * you use a static modifier.
	 */
	private static ArrayList<SerialPort> comPorts = new ArrayList<SerialPort>();
	private static SerialPort comPort;

	/**
	 * Creates a new <code>SerialCommImpl</code> instance.
	 */
	public KNDLSerialCommImpl() {
		comPorts = new ArrayList<SerialPort>(Arrays.asList(SerialPort.getCommPorts()));
	}

	/**
	 * @throws DeviceNotFoundException
	 *             If the device is not accessible (see
	 *             <code>checkSerialPort()</code>).
	 * @throws CommPortException
	 *             If there is a problem while opening the serial port.
	 */
	public void openSerialPort() throws DeviceNotFoundException, CommPortException {
		if (comPort != null) {
			checkSerialPort(comPort.getSystemPortName());
			if (!comPort.openPort()) {
				throw new CommPortException();
			}
		} else {
			throw new DeviceNotFoundException();
		}
	}

	public void closeSerialPort() {
		if (comPort != null) {
			if (comPort.isOpen()) {
				comPort.closePort();
			}
		}
	}

	/**
	 * @throws InterruptedException
	 *             If the thread that sends data is interrupted.
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public void sendData(String data) throws InterruptedException, IOException {
		byte[] bytes = data.getBytes();

		try {
			comPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_SEMI_BLOCKING, KNDLSerialComm.SERIAL_COMM_TIMEOUT,
					KNDLSerialComm.SERIAL_COMM_TIMEOUT);
			OutputStream out = comPort.getOutputStream();
			Thread.sleep(1500);
			out.write(bytes);
			out.close();
		} catch (InterruptedException | IOException e) {
			throw e;
		}
	}

	/**
	 * @throws IOException
	 *             If an I/O error occurs.
	 */
	public String receiveData() throws IOException {
		StringBuilder res = new StringBuilder("");
		try {
			comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, KNDLSerialComm.SERIAL_COMM_TIMEOUT,
					KNDLSerialComm.SERIAL_COMM_TIMEOUT);
			InputStream in = comPort.getInputStream();
			while (in.available() != 0) {
				res.append((char) in.read());
			}
			in.close();
		} catch (IOException e) {
			throw e;
		}
		return res.toString();
	}

	public ArrayList<String> getComPorts() {
		ArrayList<String> spn = new ArrayList<String>(); // SytemPortNames
		List<SerialPort> auxList = Arrays.asList(SerialPort.getCommPorts());

		comPorts.clear();
		comPorts.addAll(auxList);

		for (SerialPort port : comPorts) {
			spn.add(port.getSystemPortName());
		}

		return spn;
	}

	public ArrayList<String> getComPortDescriptions() {
		ArrayList<String> dpn = new ArrayList<String>(); // descriptivePortNames
		List<SerialPort> auxList = Arrays.asList(SerialPort.getCommPorts());

		comPorts.clear();
		comPorts.addAll(auxList);

		for (SerialPort port : comPorts) {
			dpn.add(port.getDescriptivePortName());
		}

		return dpn;
	}

	public String getComPortDescription(String portName) {
		String description = "";
		List<SerialPort> auxList = Arrays.asList(SerialPort.getCommPorts());

		comPorts.clear();
		comPorts.addAll(auxList);

		for (SerialPort port : comPorts) {
			if (portName.equals(port.getSystemPortName())) {
				description = port.getDescriptivePortName();
			}
		}

		return description;
	}

	/**
	 * Checks if the port with the specified specified port name is between the
	 * available serial ports with connected devices. If it is, then it is
	 * defined as the port to be used.
	 */
	public boolean checkSerialPort(String portName) {
		boolean valido = false;
		List<SerialPort> auxList = Arrays.asList(SerialPort.getCommPorts());

		comPorts.clear();
		comPorts.addAll(auxList);

		if (!"".equals(portName)) {
			for (SerialPort serialPort : comPorts) {
				if (portName.equals(serialPort.getSystemPortName())) {
					comPort = serialPort;
					valido = true;
				}
			}
		}
		return valido;
	}

	public String matchRegEx(String regularExpression) {
		String port = "";
		for (SerialPort serialPort : comPorts) {
			String description = serialPort.getDescriptivePortName();
			boolean b = Pattern.matches(regularExpression, description);
			if (b) {
				port = serialPort.getSystemPortName();
				break;
			}
		}
		return port;
	}
}
