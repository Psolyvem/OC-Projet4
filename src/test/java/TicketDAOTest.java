import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TicketDAOTest
{
	public static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static DataBasePrepareService dataBasePrepareService;
	private static TicketDAO ticketDAO;
	private static ParkingSpot parkingSpot;

	@BeforeAll
	public static void setUp()
	{
		ticketDAO = new TicketDAO();
		dataBasePrepareService = new DataBasePrepareService();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
	}

	@BeforeEach
	public void setUpPerTest()
	{
		dataBasePrepareService.clearDataBaseEntries();
		parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
	}

	@Test
	public void saveTicketTest()
	{
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("AZERTY");

		ticketDAO.saveTicket(ticket);

		assertNotNull(ticketDAO.getTicket("AZERTY"));
	}

	@Test
	public void updateTicketTest()
	{
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("AZERTY");
		ticketDAO.saveTicket(ticket);

		ticket = ticketDAO.getTicket("AZERTY");
		Date inTime = new Date(System.currentTimeMillis() - (120 * 60 * 1000));
		inTime = new Date(((inTime.getTime() + 500) / 1000) * 1000);
		ticket.setInTime(inTime);
		ticketDAO.updateTicket(ticket);

		assertEquals(ticketDAO.getTicket("AZERTY").getInTime().getTime(), inTime.getTime());
	}

	@Test
	public void getOccurrencesTest()
	{
		Ticket ticket = new Ticket();
		ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("AZERTY");
		ticketDAO.saveTicket(ticket);

		Ticket ticket2 = new Ticket();
		ticket2.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
		ticket2.setParkingSpot(parkingSpot);
		ticket2.setVehicleRegNumber("AZERTY");
		ticketDAO.saveTicket(ticket2);

		assertEquals(2, ticketDAO.getOccurrences("AZERTY"));
	}

	@AfterAll
	public static void tearDown()
	{
		dataBasePrepareService.clearDataBaseEntries();
	}
}
