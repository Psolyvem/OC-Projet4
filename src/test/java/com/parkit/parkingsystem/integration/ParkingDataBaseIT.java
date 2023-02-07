package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT
{
	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	public static void setUp() throws Exception
	{
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	public void setUpPerTest() throws Exception
	{
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
		dataBasePrepareService.clearDataBaseEntries();
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
	}

	@AfterAll
	public static void tearDown()
	{

	}

	@Test
	public void testParkingACar()
	{
		int nextParkingSpotId = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
		parkingService.processIncomingVehicle();
		Ticket ticket = ticketDAO.getTicket("ABCDEF");;
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticketDAO.updateTicket(ticket);

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
		parkingService.processExitingVehicle();
		// Check that the fare generated and out time are populated correctly in the database
		assertNotNull(ticketDAO.getTicket("ABCDEF").getOutTime());
		assertNotNull(ticketDAO.getTicket("ABCDEF").getPrice());
	}

	@Test
	public void testRecurringUser()
	{
		testParkingLotExit();
		testParkingLotExit();
		parkingService.processIncomingVehicle();
		// Check if a user that is already registered in the DB is correctly handled
		assertTrue(parkingService.isRecurringUser("ABCDEF"));
		// Check that 5% discount is applied on a recurrent user fare
		Ticket ticket = ticketDAO.getTicket("ABCDEF");;
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticketDAO.updateTicket(ticket);
		parkingService.processExitingVehicle();
		assertEquals(Fare.CAR_RATE_PER_HOUR / 100 * 95, ticketDAO.getTicket("ABCDEF").getPrice());
	}

}
