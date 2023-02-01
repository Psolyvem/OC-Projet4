package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT
{
	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception
	{
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	private void setUpPerTest() throws Exception
	{
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();
	}

	@AfterAll
	private static void tearDown()
	{

	}

	@Test
	public void testParkingACar()
	{
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		int nextParkingSpotId = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
		parkingService.processIncomingVehicle();

		// Parking Spot is the one that must have been assigned to the vehicle
		boolean parkingSpotIsCorrect = ticketDAO.getTicket("ABCDEF").getParkingSpot().getId() == nextParkingSpotId;
		// Parking Spot is correctly marked as unavailable
		boolean parkingSpotIsUnavailable = !ticketDAO.getTicket("ABCDEF").getParkingSpot().isAvailable();

		assertTrue(parkingSpotIsCorrect);
		assertTrue(parkingSpotIsUnavailable);
	}

	@Test
	public void testParkingLotExit()
	{
		testParkingACar();
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processExitingVehicle();
		// Check that the fare generated and out time are populated correctly in the database
		assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
		assertNotNull(ticketDAO.getTicket("ABCDEF").getPrice());
	}

	@Test
	public void testRecurrentUser()
	{
		testParkingLotExit();
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		//TODO: Check if an user that is already registered in the DB is correctly handled

	}

}
