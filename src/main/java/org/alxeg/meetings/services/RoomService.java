package org.alxeg.meetings.services;

import org.alxeg.meetings.models.Meeting;
import org.alxeg.meetings.models.Room;

import java.util.Date;
import java.util.List;


public interface RoomService {

    boolean isConfigured();

    Room getRoom();

    List<Meeting> getMeetings(Date startDate, Date endDate);

}
