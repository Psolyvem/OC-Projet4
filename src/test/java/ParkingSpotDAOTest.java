import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ParkingSpotDAOTest
{
	public static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static DataBasePrepareService dataBasePrepareService;
	private static TicketDAO ticketDAO;

	private static ParkingSpotDAO parkingSpotDAO;

	@BeforeAll
	public static void setUp()
	{
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();
	}

	@BeforeEach
	public void setUpPerTest()
	{
		dataBasePrepareService.clearDataBaseEntries();
	}

	@Test
	public void getNextAvailableSlotTest()
	{
		assertEquals(1, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
	}

	@Test
	public void updateParkingTest()
	{
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
		parkingSpotDAO.updateParking(parkingSpot);
		assertEquals(2, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
	}

	@AfterAll
	public static void tearDown()
	{
		dataBasePrepareService.clearDataBaseEntries();
	}
}
