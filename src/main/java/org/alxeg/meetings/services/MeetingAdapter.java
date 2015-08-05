package org.alxeg.meetings.services;

import org.alxeg.meetings.models.Meeting;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.Appointment;

@Component
public class MeetingAdapter {
    private static Logger LOGGER = LoggerFactory.getLogger(MeetingAdapter.class);

    public Meeting meetingFromAppointment(Appointment appointment) {
        try {
            return new Meeting()
                .withTitle(appointment.getOrganizer().getName())
                .withSubject(appointment.getSubject())
                .withStart(new DateTime(appointment.getStart().getTime()))
                .withEnd(new DateTime(appointment.getEnd().getTime()));
        } catch (ServiceLocalException e) {
            LOGGER.error("Error: ", e);
            throw new RuntimeException(e);
        }
    }


    public List<Meeting> meetingListFromAppointments(List<Appointment> appointments) {
        return appointments.stream()
            .map(appt -> meetingFromAppointment(appt))
            .collect(Collectors.toList());
    }
}

