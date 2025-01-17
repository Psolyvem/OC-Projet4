package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class ParkingService
{
	private static final Logger logger = LogManager.getLogger("ParkingService");

	private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

	private InputReaderUtil inputReaderUtil;
	private ParkingSpotDAO parkingSpotDAO;
	private TicketDAO ticketDAO;

	public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO)
	{
		this.inputReaderUtil = inputReaderUtil;
		this.parkingSpotDAO = parkingSpotDAO;
		this.ticketDAO = ticketDAO;
	}

	public void processIncomingVehicle()
	{
		try
		{
			ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
			if (parkingSpot != null && parkingSpot.getId() > 0)
			{
				String vehicleRegNumber = getVehicleRegNumber();
				// If vehicle is already registered but hasn't left the parking lot yet
				if (getRegisteredUser(vehicleRegNumber) && ticketDAO.getTicket(vehicleRegNumber).getOutTime() == null)
				{
					System.out.println("Vehicle already in the parking");
				}
				// If user is a recurring user
				else if (getRegisteredUser(vehicleRegNumber) && ticketDAO.getTicket(vehicleRegNumber).getOutTime() != null)
				{
					parkingSpot.setAvailable(false);
					parkingSpotDAO.updateParking(parkingSpot);//allot this parking space and mark its availability as false

					Date inTime = new Date();
					Ticket ticket = new Ticket();
					//ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
					//ticket.setId(ticketID);
					ticket.setParkingSpot(parkingSpot);
					ticket.setVehicleRegNumber(vehicleRegNumber);
					ticket.setPrice(0);
					ticket.setInTime(inTime);
					ticket.setOutTime(null);
					ticketDAO.saveTicket(ticket);
					System.out.println("Welcome back! As a recurring user of our parking lot, you'll benefit from a 5% discount");
					System.out.println("Generated Ticket and saved in DB");
					System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
					System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
				}
				else
				{
					parkingSpot.setAvailable(false);
					parkingSpotDAO.updateParking(parkingSpot);//allot this parking space and mark its availability as false

					Date inTime = new Date();
					Ticket ticket = new Ticket();
					//ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
					//ticket.setId(ticketID);
					ticket.setParkingSpot(parkingSpot);
					ticket.setVehicleRegNumber(vehicleRegNumber);
					ticket.setPrice(0);
					ticket.setInTime(inTime);
					ticket.setOutTime(null);
					ticketDAO.saveTicket(ticket);
					System.out.println("Generated Ticket and saved in DB");
					System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
					System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
				}
			}
		} catch (Exception e)
		{
			logger.error("Unable to process incoming vehicle", e);
		}
	}

	private String getVehicleRegNumber() throws Exception
	{
		System.out.println("Please type the vehicle registration number and press enter key");
		return inputReaderUtil.readVehicleRegistrationNumber();
	}

	/**
	 * Check if the user is already present in the DB
	 *
	 * @param vehicleRegNumber vehicle registration number
	 * @return true if the user has already been registered at least once in the DB
	 */
	private boolean getRegisteredUser(String vehicleRegNumber)
	{
		return ticketDAO.getTicket(vehicleRegNumber) != null;
	}

	/**
	 * Verify if the user is a recurrent number or not by checking the number of occurrences of his reg number in the DB
	 *
	 * @param vehicleRegNumber vehicle registration number
	 * @return True if the user has already used the parking system before this time
	 */
	public boolean isRecurringUser(String vehicleRegNumber)
	{
		return ticketDAO.getOccurrences(vehicleRegNumber) > 1;
	}

	public ParkingSpot getNextParkingNumberIfAvailable()
	{
		int parkingNumber;
		ParkingSpot parkingSpot = null;
		try
		{
			ParkingType parkingType = getVehicleType();
			parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
			if (parkingNumber > 0)
			{
				parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
			}
			else
			{
				throw new Exception("Error fetching parking number from DB. Parking slots might be full");
			}
		} catch (IllegalArgumentException ie)
		{
			logger.error("Error parsing user input for type of vehicle", ie);
		} catch (Exception e)
		{
			logger.error("Error fetching next available parking slot", e);
		}
		return parkingSpot;
	}

	private ParkingType getVehicleType()
	{
		System.out.println("Please select vehicle type from menu");
		System.out.println("1 CAR");
		System.out.println("2 BIKE");
		int input = inputReaderUtil.readSelection();
		switch (input)
		{
			case 1:
			{
				return ParkingType.CAR;
			}
			case 2:
			{
				return ParkingType.BIKE;
			}
			default:
			{
				System.out.println("Incorrect input provided");
				throw new IllegalArgumentException("Entered input is invalid");
			}
		}
	}

	public void processExitingVehicle()
	{
		try
		{
			String vehicleRegNumber = getVehicleRegNumber();
			Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);

			//Rounding outTime to match the format of the DB (rounded to the second)
			Date outTime = new Date(((new Date().getTime() + 500) / 1000) * 1000);
			ticket.setOutTime(outTime);
			fareCalculatorService.calculateFare(ticket, isRecurringUser(ticket.getVehicleRegNumber()));
			if (ticketDAO.updateTicket(ticket))
			{
				ParkingSpot parkingSpot = ticket.getParkingSpot();
				parkingSpot.setAvailable(true);
				parkingSpotDAO.updateParking(parkingSpot);
				System.out.println("Please pay the parking fare:" + ticket.getPrice());
				System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
			}
			else
			{
				System.out.println("Unable to update ticket information. Error occurred");
			}
		} catch (Exception e)
		{
			logger.error("Unable to process exiting vehicle", e);
		}
	}
}
