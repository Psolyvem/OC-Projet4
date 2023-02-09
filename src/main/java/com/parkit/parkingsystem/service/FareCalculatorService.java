package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import static java.lang.Math.round;

public class FareCalculatorService
{
	public void calculateFare(Ticket ticket, boolean isRecurrent)
	{
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())))
		{
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}

		long inHour = ticket.getInTime().getTime();
		long outHour = ticket.getOutTime().getTime();

		// Duration is hours passed + percentage of started hour
		long durationInMilliseconds = outHour - inHour;
		float duration = (durationInMilliseconds / 3600000) + (durationInMilliseconds % 3600000 / 60000 / 60.f);
		// Arrondi a 3 décimales
		duration = (float) ((double) round(duration * 100) / 100);
		if (duration <= 0.5) // If duration is under 30 min, parking is free
		{
			ticket.setPrice(0);
		}
		else
		{
			switch (ticket.getParkingSpot().getParkingType())
			{
				case CAR:
				{
					ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
					break;
				}
				case BIKE:
				{
					ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
					break;
				}
				default:
					throw new IllegalArgumentException("Unknown Parking Type");
			}
			// 5% discount if the user is recurrent
			if (isRecurrent)
			{
				ticket.setPrice(ticket.getPrice() / 100 * 95);
			}
		}
		// Arrondi à 3 décimales
		ticket.setPrice((double) round(ticket.getPrice() * 1000) / 1000);
	}
}